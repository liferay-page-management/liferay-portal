/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.data.cleanup.internal.upgrade.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.fragment.constants.FragmentConstants;
import com.liferay.fragment.entry.processor.constants.FragmentEntryProcessorConstants;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentCollectionLocalService;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.layout.model.LayoutClassedModelUsage;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructure;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalService;
import com.liferay.layout.provider.LayoutStructureProvider;
import com.liferay.layout.service.LayoutClassedModelUsageLocalService;
import com.liferay.layout.test.util.ContentLayoutTestUtil;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.layout.util.constants.LayoutDataItemTypeConstants;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.segments.service.SegmentsExperienceLocalService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Georgel Pop
 */
@RunWith(Arquillian.class)
public class RemoveOrphanedLayoutClassedModelUsagesUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		UserTestUtil.setUser(TestPropsValues.getUser());

		_group = GroupTestUtil.addGroup();

		_layout = LayoutTestUtil.addTypeContentPublishedLayout(
			_group, RandomTestUtil.randomString(),
			WorkflowConstants.STATUS_APPROVED);

		_draftLayout = _layout.fetchDraftLayout();

		_segmentsExperienceId =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperienceId(
				_draftLayout.getPlid());

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group, TestPropsValues.getUserId());

		ServiceContextThreadLocal.pushServiceContext(_serviceContext);
	}

	@After
	public void tearDown() {
		ServiceContextThreadLocal.popServiceContext();
	}

	@Test
	@TestInfo({"LPD-60259", "LPD-62154"})
	public void testRemoveOrphanedLayoutClassedModelUsagesUpgradeProcess()
		throws Exception {

		JournalArticle journalArticle = JournalTestUtil.addArticle(
			_group.getGroupId(), 0);

		ContentLayoutTestUtil.addItemToLayout(
			JSONUtil.put(
				"styles",
				JSONUtil.put(
					"backgroundImage",
					JSONUtil.put(
						"className", JournalArticle.class.getName()
					).put(
						"classNameId",
						_portal.getClassNameId(JournalArticle.class)
					).put(
						"classPK", journalArticle.getResourcePrimKey()
					).put(
						"fieldId", "JournalArticle_authorProfileImage"
					))
			).toString(),
			LayoutDataItemTypeConstants.TYPE_CONTAINER, _draftLayout,
			_layoutStructureProvider, _segmentsExperienceId);

		FragmentCollection fragmentCollection =
			_fragmentCollectionLocalService.addFragmentCollection(
				null, TestPropsValues.getUserId(), _group.getGroupId(),
				RandomTestUtil.randomString(), StringPool.BLANK,
				_serviceContext);

		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.addFragmentEntry(
				null, TestPropsValues.getUserId(), _group.getGroupId(),
				fragmentCollection.getFragmentCollectionId(),
				"fragment-entry-key", RandomTestUtil.randomString(),
				StringPool.BLANK,
				"<h1 data-lfr-editable-id=\"element-text\" " +
					"data-lfr-editable-type=\"text\">Heading Example</h1>",
				StringPool.BLANK, false, StringPool.BLANK, null, 0, false,
				false, FragmentConstants.TYPE_COMPONENT, null,
				WorkflowConstants.STATUS_APPROVED, _serviceContext);

		ContentLayoutTestUtil.addFragmentEntryLinkToLayout(
			JSONUtil.put(
				FragmentEntryProcessorConstants.
					KEY_EDITABLE_FRAGMENT_ENTRY_PROCESSOR,
				JSONUtil.put(
					"element-text",
					JSONUtil.put(
						"className", JournalArticle.class.getName()
					).put(
						"classNameId",
						_portal.getClassNameId(JournalArticle.class)
					).put(
						"classPK", journalArticle.getResourcePrimKey()
					).put(
						"fieldId", "JournalArticle_title"
					))
			).toString(),
			fragmentEntry.getCss(), fragmentEntry.getConfiguration(),
			fragmentEntry.getFragmentEntryId(), fragmentEntry.getHtml(),
			fragmentEntry.getJs(), _draftLayout,
			fragmentEntry.getFragmentEntryKey(), _segmentsExperienceId,
			fragmentEntry.getType());

		ContentLayoutTestUtil.publishLayout(_draftLayout, _layout);

		_assertLayoutClassedModelUsages(
			journalArticle.getResourcePrimKey(), _draftLayout.getPlid());
		_assertLayoutClassedModelUsages(
			journalArticle.getResourcePrimKey(), _layout.getPlid());

		_addLayoutClassedModelUsagesRandomValues(_draftLayout.getPlid());
		_addLayoutClassedModelUsagesRandomValues(_layout.getPlid());

		_upgrade(journalArticle);

		_addLayoutClassedModelUsagesSwitchExistingValues();

		_upgrade(journalArticle);
	}

	private void _addLayoutClassedModelUsagesRandomValues(long plid) {
		List<LayoutClassedModelUsage> layoutClassedModelUsages =
			_layoutClassedModelUsageLocalService.
				getLayoutClassedModelUsagesByPlid(plid);

		for (LayoutClassedModelUsage layoutClassedModelUsage :
				layoutClassedModelUsages) {

			layoutClassedModelUsage.setContainerKey(
				String.valueOf(RandomTestUtil.randomLong()));

			_layoutClassedModelUsageLocalService.updateLayoutClassedModelUsage(
				layoutClassedModelUsage);
		}
	}

	private void _addLayoutClassedModelUsagesSwitchExistingValues() {
		List<LayoutClassedModelUsage> layoutClassedModelUsages =
			_layoutClassedModelUsageLocalService.
				getLayoutClassedModelUsagesByPlid(_layout.getPlid());

		List<LayoutClassedModelUsage> draftLayoutClassedModelUsages =
			_layoutClassedModelUsageLocalService.
				getLayoutClassedModelUsagesByPlid(_draftLayout.getPlid());

		Map<Long, LayoutClassedModelUsage> layoutClassedModelUsageHashMap =
			new HashMap<>();

		for (LayoutClassedModelUsage draftLayoutClassedModelUsage :
				draftLayoutClassedModelUsages) {

			layoutClassedModelUsageHashMap.put(
				draftLayoutClassedModelUsage.getContainerType(),
				draftLayoutClassedModelUsage);
		}

		for (LayoutClassedModelUsage layoutClassedModelUsage :
				layoutClassedModelUsages) {

			LayoutClassedModelUsage draftLayoutClassedModelUsage =
				layoutClassedModelUsageHashMap.get(
					layoutClassedModelUsage.getContainerType());

			String layoutContainerKey =
				layoutClassedModelUsage.getContainerKey();

			layoutClassedModelUsage.setContainerKey(
				draftLayoutClassedModelUsage.getContainerKey());

			draftLayoutClassedModelUsage.setContainerKey(layoutContainerKey);

			_layoutClassedModelUsageLocalService.updateLayoutClassedModelUsage(
				layoutClassedModelUsage);

			_layoutClassedModelUsageLocalService.updateLayoutClassedModelUsage(
				draftLayoutClassedModelUsage);
		}
	}

	private void _assertLayoutClassedModelUsages(long classPK, long plid)
		throws Exception {

		List<LayoutClassedModelUsage> layoutClassedModelUsages =
			_layoutClassedModelUsageLocalService.
				getLayoutClassedModelUsagesByPlid(plid);

		Assert.assertEquals(
			layoutClassedModelUsages.toString(), 2,
			layoutClassedModelUsages.size());

		for (int i = 0; i < 2; i++) {
			LayoutClassedModelUsage layoutClassedModelUsage =
				layoutClassedModelUsages.get(i);

			Assert.assertEquals(classPK, layoutClassedModelUsage.getClassPK());

			if (_portal.getClassNameId(FragmentEntryLink.class) ==
					layoutClassedModelUsage.getContainerType()) {

				FragmentEntryLink fragmentEntryLink =
					_fragmentEntryLinkLocalService.getFragmentEntryLink(
						GetterUtil.getLong(
							layoutClassedModelUsage.getContainerKey()));

				Assert.assertEquals(plid, fragmentEntryLink.getPlid());
			}
			else {
				Assert.assertEquals(
					_portal.getClassNameId(LayoutPageTemplateStructure.class),
					layoutClassedModelUsage.getContainerType());

				LayoutPageTemplateStructure layoutPageTemplateStructure =
					_layoutPageTemplateStructureLocalService.
						getLayoutPageTemplateStructure(
							GetterUtil.getLong(
								layoutClassedModelUsage.getContainerKey()));

				Assert.assertEquals(
					plid, layoutPageTemplateStructure.getPlid());
			}
		}
	}

	private void _upgrade(JournalArticle journalArticle) throws Exception {
		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					"com.liferay.data.cleanup.internal.configuration." +
						"DataRemovalConfiguration",
					HashMapDictionaryBuilder.<String, Object>put(
						"removeOrphanedLayoutClassedModelUsages", true
					).build())) {

			_assertLayoutClassedModelUsages(
				journalArticle.getResourcePrimKey(), _draftLayout.getPlid());
			_assertLayoutClassedModelUsages(
				journalArticle.getResourcePrimKey(), _layout.getPlid());
		}
	}

	private Layout _draftLayout;

	@Inject
	private FragmentCollectionLocalService _fragmentCollectionLocalService;

	@Inject
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@Inject
	private FragmentEntryLocalService _fragmentEntryLocalService;

	@DeleteAfterTestRun
	private Group _group;

	private Layout _layout;

	@Inject
	private LayoutClassedModelUsageLocalService
		_layoutClassedModelUsageLocalService;

	@Inject
	private LayoutPageTemplateStructureLocalService
		_layoutPageTemplateStructureLocalService;

	@Inject
	private LayoutStructureProvider _layoutStructureProvider;

	@Inject
	private Portal _portal;

	private long _segmentsExperienceId;

	@Inject
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

	private ServiceContext _serviceContext;

}