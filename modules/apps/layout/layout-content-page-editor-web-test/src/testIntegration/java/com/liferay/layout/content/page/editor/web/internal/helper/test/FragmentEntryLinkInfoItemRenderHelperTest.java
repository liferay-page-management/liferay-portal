/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.content.page.editor.web.internal.helper.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.fragment.constants.FragmentConstants;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentCollectionLocalService;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.info.constants.InfoDisplayWebKeys;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.InfoItemReference;
import com.liferay.journal.constants.JournalFolderConstants;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.layout.display.page.constants.LayoutDisplayPageWebKeys;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructure;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalService;
import com.liferay.layout.test.util.ContentLayoutTestUtil;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.ScopeUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.segments.service.SegmentsExperienceLocalService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Chaitanya Sammetla
 */
@RunWith(Arquillian.class)
public class FragmentEntryLinkInfoItemRenderHelperTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		_draftLayout = layout.fetchDraftLayout();

		_fragmentEntryLink = _addFragmentEntryLink();

		_journalArticle = JournalTestUtil.addArticle(
			_group.getGroupId(),
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID);

		_mockHttpServletRequest =
			ContentLayoutTestUtil.getMockHttpServletRequest(
				_companyLocalService.getCompany(_group.getCompanyId()), _group,
				_draftLayout);
	}

	@Test
	public void testGetFragmentEntryLinkJSONObject() throws Exception {
		String content = _getContent(null);

		Assert.assertFalse(content.contains(_journalArticle.getArticleId()));

		content = _getContent(
			new InfoItemReference(
				JournalArticle.class.getName(),
				new ClassPKInfoItemIdentifier(
					_journalArticle.getResourcePrimKey())));

		Assert.assertTrue(content.contains(_journalArticle.getArticleId()));

		Assert.assertNull(
			_mockHttpServletRequest.getAttribute(
				InfoDisplayWebKeys.INFO_ITEM_REFERENCE));
		Assert.assertNull(
			_mockHttpServletRequest.getAttribute(
				LayoutDisplayPageWebKeys.LAYOUT_DISPLAY_PAGE_PROVIDER));
	}

	private FragmentEntryLink _addFragmentEntryLink() throws Exception {
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				_group.getGroupId(), TestPropsValues.getUserId());

		FragmentCollection fragmentCollection =
			_fragmentCollectionLocalService.addFragmentCollection(
				null, TestPropsValues.getUserId(), _group.getGroupId(),
				RandomTestUtil.randomString(), StringPool.BLANK,
				serviceContext);

		String fieldName = RandomTestUtil.randomString();

		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.addFragmentEntry(
				null, TestPropsValues.getUserId(), _group.getGroupId(),
				fragmentCollection.getFragmentCollectionId(),
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				StringPool.BLANK,
				StringBundler.concat(
					"<div>[#if ", fieldName, "Object??]${", fieldName,
					"Object.articleId}[/#if]</div>"),
				StringPool.BLANK, false,
				JSONUtil.put(
					"fieldSets",
					JSONUtil.putAll(
						JSONUtil.put(
							"fields",
							JSONUtil.putAll(
								JSONUtil.put(
									"label", RandomTestUtil.randomString()
								).put(
									"name", fieldName
								).put(
									"type", "itemSelector"
								))))
				).toString(),
				null, 0, false, false, FragmentConstants.TYPE_COMPONENT, null,
				WorkflowConstants.STATUS_APPROVED, serviceContext);

		return ContentLayoutTestUtil.addFragmentEntryLinkToLayout(
			StringPool.BLANK, fragmentEntry.getCss(),
			fragmentEntry.getConfiguration(),
			fragmentEntry.getExternalReferenceCode(),
			ScopeUtil.getItemScopeExternalReferenceCode(
				fragmentEntry.getGroupId(), _draftLayout.getGroupId()),
			fragmentEntry.getHtml(), fragmentEntry.getJs(), _draftLayout,
			fragmentEntry.getFragmentEntryKey(), fragmentEntry.getType(), null,
			0,
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperienceId(
				_draftLayout.getPlid()));
	}

	private String _getContent(InfoItemReference infoItemReference)
		throws Exception {

		LayoutPageTemplateStructure layoutPageTemplateStructure =
			_layoutPageTemplateStructureLocalService.
				fetchLayoutPageTemplateStructure(
					_draftLayout.getGroupId(), _draftLayout.getPlid());

		JSONObject fragmentEntryLinkJSONObject = ReflectionTestUtil.invoke(
			_fragmentEntryLinkInfoItemRenderHelper,
			"getFragmentEntryLinkJSONObject",
			new Class<?>[] {
				FragmentEntryLink.class, HttpServletRequest.class,
				HttpServletResponse.class, InfoItemReference.class,
				LayoutStructure.class
			},
			_fragmentEntryLink, _mockHttpServletRequest,
			new MockHttpServletResponse(), infoItemReference,
			LayoutStructure.of(
				layoutPageTemplateStructure.
					getDefaultSegmentsExperienceData()));

		return fragmentEntryLinkJSONObject.getString("content");
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	private Layout _draftLayout;

	@Inject
	private FragmentCollectionLocalService _fragmentCollectionLocalService;

	private FragmentEntryLink _fragmentEntryLink;

	@Inject(
		filter = "component.name=com.liferay.layout.content.page.editor.web.internal.helper.FragmentEntryLinkInfoItemRenderHelper",
		type = Inject.NoType.class
	)
	private Object _fragmentEntryLinkInfoItemRenderHelper;

	@Inject
	private FragmentEntryLocalService _fragmentEntryLocalService;

	@DeleteAfterTestRun
	private Group _group;

	private JournalArticle _journalArticle;

	@Inject
	private LayoutPageTemplateStructureLocalService
		_layoutPageTemplateStructureLocalService;

	private MockHttpServletRequest _mockHttpServletRequest;

	@Inject
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

}