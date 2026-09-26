/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.content.page.editor.web.internal.portlet.action.test;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.fragment.constants.FragmentConstants;
import com.liferay.fragment.entry.processor.constants.FragmentEntryProcessorConstants;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentCollectionLocalService;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.layout.test.util.ContentLayoutTestUtil;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.test.util.ObjectDefinitionTestUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.servlet.PortletServlet;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ScopeUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.segments.service.SegmentsExperienceLocalService;

import jakarta.portlet.ActionRequest;
import jakarta.portlet.ActionResponse;

import java.io.InputStream;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Eudaldo Alonso
 */
@RunWith(Arquillian.class)
@Sync
public class UpdateConfigurationValuesMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() {
		_objectMapper = new ObjectMapper() {
			{
				configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
			}
		};
	}

	@Test
	public void testDoTransactionalCommand() throws Exception {
		Group group = GroupTestUtil.addGroup();

		GroupTestUtil.updateDisplaySettings(
			group.getGroupId(),
			ListUtil.fromArray(LocaleUtil.US, LocaleUtil.SPAIN), LocaleUtil.US);

		Layout layout = LayoutTestUtil.addTypeContentLayout(group);

		Layout draftLayout = layout.fetchDraftLayout();

		FragmentEntryLink fragmentEntryLink = _addFragmentEntryLink(
			draftLayout, group, "localizable_fragment.html",
			"localizable_fragment_configuration.json");

		JSONObject configurationValuesJSONObject = JSONUtil.put(
			"numberOfLinks",
			JSONUtil.put(LocaleUtil.toLanguageId(LocaleUtil.US), "1"));

		JSONObject editableValuesJSONObject = _transactionalCommand(
			configurationValuesJSONObject, draftLayout, fragmentEntryLink,
			group, LocaleUtil.US);

		Assert.assertEquals(
			SetUtil.fromArray("link-1"),
			_getEditableIds(editableValuesJSONObject));

		JSONObject numberOfLinksJSONObject =
			configurationValuesJSONObject.getJSONObject("numberOfLinks");

		numberOfLinksJSONObject.put(
			LocaleUtil.toLanguageId(LocaleUtil.SPAIN), "3");

		editableValuesJSONObject = _transactionalCommand(
			configurationValuesJSONObject, draftLayout, fragmentEntryLink,
			group, LocaleUtil.SPAIN);

		Assert.assertEquals(
			SetUtil.fromArray("link-1", "link-2", "link-3"),
			_getEditableIds(editableValuesJSONObject));
		Assert.assertEquals(
			"Links 1",
			_getEditableDefaultValue(editableValuesJSONObject, "link-1"));
		Assert.assertEquals(
			"Links 3",
			_getEditableDefaultValue(editableValuesJSONObject, "link-3"));

		numberOfLinksJSONObject.put(
			LocaleUtil.toLanguageId(LocaleUtil.US), "3"
		).put(
			LocaleUtil.toLanguageId(LocaleUtil.SPAIN), "1"
		);

		editableValuesJSONObject = _transactionalCommand(
			configurationValuesJSONObject, draftLayout, fragmentEntryLink,
			group, LocaleUtil.SPAIN);

		Assert.assertEquals(
			SetUtil.fromArray("link-1", "link-2", "link-3"),
			_getEditableIds(editableValuesJSONObject));
	}

	@Test
	public void testDoTransactionalCommandWithInfoItemReference()
		throws Exception {

		Group group = GroupTestUtil.addGroup();

		Layout layout = LayoutTestUtil.addTypeContentLayout(group);

		Layout draftLayout = layout.fetchDraftLayout();

		FragmentEntryLink fragmentEntryLink = _addFragmentEntryLink(
			draftLayout, group, "info_item_fragment.html",
			"info_item_fragment_configuration.json");

		ObjectDefinition objectDefinition =
			ObjectDefinitionTestUtil.publishObjectDefinition();

		ObjectEntry objectEntry = _objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			ObjectEntryFolderConstants.PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
			null, Collections.emptyMap(), new ServiceContext());

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			_getMockLiferayPortletActionRequest(
				JSONUtil.put("item", StringPool.BLANK), draftLayout,
				fragmentEntryLink, group, LocaleUtil.US);

		String content = _getContent(mockLiferayPortletActionRequest);

		Assert.assertFalse(
			content.contains(objectEntry.getExternalReferenceCode()));

		mockLiferayPortletActionRequest.setParameter(
			"itemClassName", objectDefinition.getClassName());
		mockLiferayPortletActionRequest.setParameter(
			"itemClassPK", String.valueOf(objectEntry.getObjectEntryId()));

		content = _getContent(mockLiferayPortletActionRequest);

		Assert.assertTrue(
			content.contains(objectEntry.getExternalReferenceCode()));
	}

	@Test
	public void testMergeEditableValuesJSONObject() throws Exception {
		JSONObject defaultEditableValuesJSONObject =
			JSONFactoryUtil.createJSONObject(
				_read("default_editable_values.json"));

		JSONObject mergeEditableValuesJSONObject = ReflectionTestUtil.invoke(
			_mvcActionCommand, "_mergeEditableValuesJSONObject",
			new Class<?>[] {JSONObject.class, String.class},
			defaultEditableValuesJSONObject, _read("editable_values.json"));

		Assert.assertEquals(
			_objectMapper.readTree(
				_read("merged_editable_values_with_same_elements.json")),
			_objectMapper.readTree(mergeEditableValuesJSONObject.toString()));
	}

	@Test
	public void testMergeEditableValuesJSONObjectWithNewElementDefaultEditableValues()
		throws Exception {

		JSONObject defaultEditableValuesJSONObject =
			JSONFactoryUtil.createJSONObject(
				_read("default_editable_values_with_new_element.json"));

		JSONObject mergeEditableValuesJSONObject = ReflectionTestUtil.invoke(
			_mvcActionCommand, "_mergeEditableValuesJSONObject",
			new Class<?>[] {JSONObject.class, String.class},
			defaultEditableValuesJSONObject, _read("editable_values.json"));

		Assert.assertEquals(
			_objectMapper.readTree(
				_read(
					"merged_editable_values_with_new_element_default_" +
						"editable_values.json")),
			_objectMapper.readTree(mergeEditableValuesJSONObject.toString()));
	}

	@Test
	public void testMergeEditableValuesJSONObjectWithNewElementEditableValues()
		throws Exception {

		JSONObject defaultEditableValuesJSONObject =
			JSONFactoryUtil.createJSONObject(
				_read("default_editable_values.json"));

		JSONObject mergeEditableValuesJSONObject = ReflectionTestUtil.invoke(
			_mvcActionCommand, "_mergeEditableValuesJSONObject",
			new Class<?>[] {JSONObject.class, String.class},
			defaultEditableValuesJSONObject,
			_read("editable_values_with_new_element.json"));

		Assert.assertEquals(
			_objectMapper.readTree(
				_read(
					"merged_editable_values_with_new_element_editable_values." +
						"json")),
			_objectMapper.readTree(mergeEditableValuesJSONObject.toString()));
	}

	private FragmentEntryLink _addFragmentEntryLink(
			Layout draftLayout, Group group, String htmlFileName,
			String configurationFileName)
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				group.getGroupId(), TestPropsValues.getUserId());

		FragmentCollection fragmentCollection =
			_fragmentCollectionLocalService.addFragmentCollection(
				null, TestPropsValues.getUserId(), group.getGroupId(),
				StringUtil.randomString(), StringPool.BLANK, serviceContext);

		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.addFragmentEntry(
				null, TestPropsValues.getUserId(), group.getGroupId(),
				fragmentCollection.getFragmentCollectionId(),
				StringUtil.randomString(), StringUtil.randomString(),
				StringPool.BLANK, _read(htmlFileName), StringPool.BLANK, false,
				_read(configurationFileName), null, 0, false, false,
				FragmentConstants.TYPE_COMPONENT, null,
				WorkflowConstants.STATUS_APPROVED, serviceContext);

		return ContentLayoutTestUtil.addFragmentEntryLinkToLayout(
			StringPool.BLANK, fragmentEntry.getCss(),
			fragmentEntry.getConfiguration(),
			fragmentEntry.getExternalReferenceCode(),
			ScopeUtil.getItemScopeExternalReferenceCode(
				fragmentEntry.getGroupId(), draftLayout.getGroupId()),
			fragmentEntry.getHtml(), fragmentEntry.getJs(), draftLayout,
			fragmentEntry.getFragmentEntryKey(), fragmentEntry.getType(), null,
			0,
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperienceId(
				draftLayout.getPlid()));
	}

	private String _getContent(
			MockLiferayPortletActionRequest mockLiferayPortletActionRequest)
		throws Exception {

		JSONObject fragmentEntryLinkJSONObject =
			_getFragmentEntryLinkJSONObject(mockLiferayPortletActionRequest);

		return fragmentEntryLinkJSONObject.getString("content");
	}

	private String _getEditableDefaultValue(
		JSONObject editableValuesJSONObject, String editableId) {

		JSONObject editableFragmentEntryProcessorJSONObject =
			editableValuesJSONObject.getJSONObject(
				FragmentEntryProcessorConstants.
					KEY_EDITABLE_FRAGMENT_ENTRY_PROCESSOR);

		JSONObject editableJSONObject =
			editableFragmentEntryProcessorJSONObject.getJSONObject(editableId);

		return editableJSONObject.getString("defaultValue");
	}

	private Set<String> _getEditableIds(JSONObject editableValuesJSONObject) {
		JSONObject editableFragmentEntryProcessorJSONObject =
			editableValuesJSONObject.getJSONObject(
				FragmentEntryProcessorConstants.
					KEY_EDITABLE_FRAGMENT_ENTRY_PROCESSOR);

		return editableFragmentEntryProcessorJSONObject.keySet();
	}

	private JSONObject _getFragmentEntryLinkJSONObject(
			MockLiferayPortletActionRequest mockLiferayPortletActionRequest)
		throws Exception {

		JSONObject jsonObject = ReflectionTestUtil.invoke(
			_mvcActionCommand, "doTransactionalCommand",
			new Class<?>[] {ActionRequest.class, ActionResponse.class},
			mockLiferayPortletActionRequest,
			new MockLiferayPortletActionResponse());

		return jsonObject.getJSONObject("fragmentEntryLink");
	}

	private MockLiferayPortletActionRequest _getMockLiferayPortletActionRequest(
			JSONObject configurationValuesJSONObject, Layout draftLayout,
			FragmentEntryLink fragmentEntryLink, Group group, Locale locale)
		throws Exception {

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			ContentLayoutTestUtil.getMockLiferayPortletActionRequest(
				_companyLocalService.getCompany(group.getCompanyId()), group,
				draftLayout);

		mockLiferayPortletActionRequest.setParameter(
			"editableValues",
			JSONUtil.put(
				FragmentEntryProcessorConstants.
					KEY_FREEMARKER_FRAGMENT_ENTRY_PROCESSOR,
				configurationValuesJSONObject
			).toString());
		mockLiferayPortletActionRequest.setParameter(
			"fragmentEntryLinkId",
			String.valueOf(fragmentEntryLink.getFragmentEntryLinkId()));
		mockLiferayPortletActionRequest.setParameter(
			"languageId", LocaleUtil.toLanguageId(locale));

		MockHttpServletRequest mockHttpServletRequest =
			(MockHttpServletRequest)
				mockLiferayPortletActionRequest.getAttribute(
					PortletServlet.PORTLET_SERVLET_REQUEST);

		mockHttpServletRequest.setParameter(
			"languageId", LocaleUtil.toLanguageId(locale));

		return mockLiferayPortletActionRequest;
	}

	private String _read(String fileName) throws Exception {
		Class<?> clazz = getClass();

		InputStream inputStream = clazz.getResourceAsStream(
			"dependencies/" + fileName);

		return StringUtil.read(inputStream);
	}

	private JSONObject _transactionalCommand(
			JSONObject configurationValuesJSONObject, Layout draftLayout,
			FragmentEntryLink fragmentEntryLink, Group group, Locale locale)
		throws Exception {

		JSONObject fragmentEntryLinkJSONObject =
			_getFragmentEntryLinkJSONObject(
				_getMockLiferayPortletActionRequest(
					configurationValuesJSONObject, draftLayout,
					fragmentEntryLink, group, locale));

		return fragmentEntryLinkJSONObject.getJSONObject("editableValues");
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private FragmentCollectionLocalService _fragmentCollectionLocalService;

	@Inject
	private FragmentEntryLocalService _fragmentEntryLocalService;

	@Inject(
		filter = "mvc.command.name=/layout_content_page_editor/update_configuration_values"
	)
	private MVCActionCommand _mvcActionCommand;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	private ObjectMapper _objectMapper;

	@Inject
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

}