/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.segments.internal.upgrade.v4_2_2.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.service.CTCollectionLocalService;
import com.liferay.change.tracking.service.CTCollectionService;
import com.liferay.change.tracking.service.CTEntryLocalService;
import com.liferay.change.tracking.service.CTProcessLocalService;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructure;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructureRel;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalService;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureRelLocalService;
import com.liferay.layout.test.util.ContentLayoutTestUtil;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.cache.MultiVMPool;
import com.liferay.portal.kernel.change.tracking.CTCollectionThreadLocal;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.kernel.version.Version;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;
import com.liferay.segments.model.SegmentsExperience;
import com.liferay.segments.service.SegmentsExperienceLocalService;
import com.liferay.segments.test.util.SegmentsTestUtil;

import java.sql.Connection;

import java.util.List;

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
public class SegmentsExperienceUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_connection = DataAccess.getConnection();
		_db = DBManagerUtil.getDB();

		_group = GroupTestUtil.addGroup();

		_layout = LayoutTestUtil.addTypeContentLayout(_group);

		Layout draftLayout = _layout.fetchDraftLayout();

		_draftSegmentsExperienceId1 =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperienceId(
				draftLayout.getPlid());

		_fragmentEntryLink1 =
			ContentLayoutTestUtil.addFragmentEntryLinkToLayout(
				JSONFactoryUtil.getNullJSON(), draftLayout,
				_draftSegmentsExperienceId1);

		LayoutPageTemplateStructure layoutPageTemplateStructure =
			_layoutPageTemplateStructureLocalService.
				fetchLayoutPageTemplateStructure(
					_group.getGroupId(), draftLayout.getPlid());

		_layoutPageTemplateStructureRel1 =
			_layoutPageTemplateStructureRelLocalService.
				fetchLayoutPageTemplateStructureRel(
					layoutPageTemplateStructure.
						getLayoutPageTemplateStructureId(),
					_draftSegmentsExperienceId1);

		_publishedSegmentsExperienceId1 =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperienceId(
				_layout.getPlid());

		SegmentsExperience draftSegmentsExperience =
			SegmentsTestUtil.addSegmentsExperience(
				_group.getGroupId(), draftLayout.getPlid());

		_draftSegmentsExperienceId2 =
			draftSegmentsExperience.getSegmentsExperienceId();

		try {
			ServiceContextThreadLocal.pushServiceContext(
				ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

			_fragmentEntryLink2 =
				ContentLayoutTestUtil.addFragmentEntryLinkToLayout(
					JSONFactoryUtil.getNullJSON(), draftLayout,
					_draftSegmentsExperienceId2);
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
		}

		_layoutPageTemplateStructureRel2 =
			_layoutPageTemplateStructureRelLocalService.
				fetchLayoutPageTemplateStructureRel(
					layoutPageTemplateStructure.
						getLayoutPageTemplateStructureId(),
					_draftSegmentsExperienceId2);

		ContentLayoutTestUtil.publishLayout(draftLayout, _layout);

		SegmentsExperience publishedSegmentsExperience =
			_segmentsExperienceLocalService.fetchSegmentsExperience(
				_group.getGroupId(),
				draftSegmentsExperience.getSegmentsExperienceKey(),
				_layout.getPlid());

		_publishedSegmentsExperienceId2 =
			publishedSegmentsExperience.getSegmentsExperienceId();
	}

	@After
	public void tearDown() throws Exception {
		DataAccess.cleanUp(_connection);
	}

	@Test
	@TestInfo("LPD-111365")
	public void testUpgrade() throws Exception {
		FragmentEntryLink publishedFragmentEntryLink =
			ContentLayoutTestUtil.addFragmentEntryLinkToLayout(
				JSONFactoryUtil.getNullJSON(), _layout,
				_publishedSegmentsExperienceId1);

		LayoutPageTemplateStructure layoutPageTemplateStructure =
			_layoutPageTemplateStructureLocalService.
				fetchLayoutPageTemplateStructure(
					_group.getGroupId(), _layout.getPlid());

		LayoutPageTemplateStructureRel publishedLayoutPageTemplateStructureRel =
			_layoutPageTemplateStructureRelLocalService.
				fetchLayoutPageTemplateStructureRel(
					layoutPageTemplateStructure.
						getLayoutPageTemplateStructureId(),
					_publishedSegmentsExperienceId1);

		_runUpgrade();

		_assertSegmentsExperienceId(
			_draftSegmentsExperienceId1, _fragmentEntryLink1,
			_layoutPageTemplateStructureRel1);

		_updateSegmentsExperienceIds();

		_runUpgrade();

		_assertDraftSegmentsExperienceIds();
		_assertSegmentsExperienceId(
			_publishedSegmentsExperienceId1, publishedFragmentEntryLink,
			publishedLayoutPageTemplateStructureRel);
	}

	@Test
	@TestInfo("LPD-111365")
	public void testUpgradeWithCTCollection() throws Exception {
		_updateSegmentsExperienceIds();

		CTCollection ctCollection = _addCTCollection();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_layoutPageTemplateStructureRelLocalService.
				addLayoutPageTemplateStructureRel(
					TestPropsValues.getUserId(), _group.getGroupId(),
					_layoutPageTemplateStructureRel1.
						getLayoutPageTemplateStructureId(),
					_draftSegmentsExperienceId1,
					_layoutPageTemplateStructureRel1.getData(),
					ServiceContextTestUtil.getServiceContext(
						_group.getGroupId()));
		}

		_runUpgrade();

		_assertSegmentsExperienceId(
			_draftSegmentsExperienceId1, _fragmentEntryLink1,
			_layoutPageTemplateStructureRel1);
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithCTCollectionDraftLayout() throws Exception {
		CTCollection ctCollection = _addCTCollection();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_updateSegmentsExperienceIds();
		}

		_runUpgrade();

		_assertDraftSegmentsExperienceIds(ctCollection);

		_runUpgrade();

		_assertDraftSegmentsExperienceIds(ctCollection);
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithCTCollectionSegmentsExperiences()
		throws Exception {

		CTCollection ctCollection = _addCTCollection();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_updateSegmentsExperienceIds();

			_segmentsExperienceLocalService.updateSegmentsExperience(
				_segmentsExperienceLocalService.getSegmentsExperience(
					_publishedSegmentsExperienceId2));
		}

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		SegmentsExperience ctCollectionSegmentsExperience =
			_addSegmentsExperience(
				ctCollection, segmentsExperience.getPlid(), segmentsExperience);

		_runUpgrade();

		_assertDraftSegmentsExperienceIds();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_assertSegmentsExperienceId(
				_draftSegmentsExperienceId1, _fragmentEntryLink1,
				_layoutPageTemplateStructureRel1);
			_assertSegmentsExperienceId(
				ctCollectionSegmentsExperience.getSegmentsExperienceId(),
				_fragmentEntryLink2, _layoutPageTemplateStructureRel2);
		}
	}

	@Test
	@TestInfo("LPD-111365")
	public void testUpgradeWithClassPKColumns() throws Exception {
		_updateSegmentsExperienceIds();

		try {
			_db.alterColumnName(
				_connection, "FragmentEntryLink", "plid", "plid2 LONG");
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructure", "plid",
				"classPK LONG");

			_runUpgrade();
		}
		finally {
			_db.alterColumnName(
				_connection, "FragmentEntryLink", "plid2", "plid LONG");
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructure", "classPK",
				"plid LONG");
		}

		_assertSegmentsExperienceId(
			_draftSegmentsExperienceId1, _fragmentEntryLink1,
			_layoutPageTemplateStructureRel1);
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithPublishedCTCollectionAddedSegmentsExperience()
		throws Exception {

		CTCollection ctCollection = _addCTCollection();

		SegmentsExperience segmentsExperience = _addSegmentsExperience(
			ctCollection);

		_runUpgrade();
		_runUpgrade();

		_ctCollectionService.publishCTCollection(
			TestPropsValues.getUserId(), ctCollection.getCtCollectionId());

		_assertSegmentsExperience(segmentsExperience);
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithPublishedCTCollectionBeforeUpgradeAddedSegmentsExperience()
		throws Exception {

		CTCollection ctCollection = _addCTCollection();

		SegmentsExperience segmentsExperience = _addSegmentsExperience(
			ctCollection);

		_addSegmentsExperience(
			ctCollection, _layout.getPlid(), segmentsExperience);

		_ctCollectionService.publishCTCollection(
			TestPropsValues.getUserId(), ctCollection.getCtCollectionId());

		_runUpgrade();
		_runUpgrade();

		_assertSegmentsExperience(segmentsExperience);
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithPublishedCTCollectionBeforeUpgradeDeletedSegmentsExperience()
		throws Exception {

		SegmentsExperience draftSegmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		_segmentsExperienceLocalService.deleteSegmentsExperience(
			draftSegmentsExperience);

		CTCollection ctCollection = _addCTCollection();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_segmentsExperienceLocalService.deleteSegmentsExperience(
				_publishedSegmentsExperienceId2);
		}

		SegmentsExperience segmentsExperience = _addSegmentsExperience(
			ctCollection, draftSegmentsExperience.getPlid(),
			draftSegmentsExperience);

		_deleteCTEntry(ctCollection, segmentsExperience);

		_ctCollectionService.publishCTCollection(
			TestPropsValues.getUserId(), ctCollection.getCtCollectionId());

		_runUpgrade();
		_runUpgrade();

		Assert.assertNull(
			_segmentsExperienceLocalService.fetchSegmentsExperience(
				segmentsExperience.getSegmentsExperienceId()));
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithPublishedCTCollectionBeforeUpgradeModifiedSegmentsExperience()
		throws Exception {

		CTCollection ctCollection = _addCTCollection();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_segmentsExperienceLocalService.updateSegmentsExperience(
				_segmentsExperienceLocalService.getSegmentsExperience(
					_publishedSegmentsExperienceId2));
		}

		SegmentsExperience draftSegmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		SegmentsExperience segmentsExperience = _addSegmentsExperience(
			ctCollection, draftSegmentsExperience.getPlid(),
			draftSegmentsExperience);

		_deleteCTEntry(ctCollection, segmentsExperience);

		_ctCollectionService.publishCTCollection(
			TestPropsValues.getUserId(), ctCollection.getCtCollectionId());

		_runUpgrade();
		_runUpgrade();

		Assert.assertNull(
			_segmentsExperienceLocalService.fetchSegmentsExperience(
				segmentsExperience.getSegmentsExperienceId()));
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithPublishedCTCollectionsBeforeUpgradeModifiedSegmentsExperience()
		throws Exception {

		SegmentsExperience draftSegmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		CTCollection ctCollection1 = _addCTCollection();
		CTCollection ctCollection2 = _addCTCollection();

		for (CTCollection ctCollection :
				new CTCollection[] {ctCollection1, ctCollection2}) {

			int priority = draftSegmentsExperience.getPriority();

			if (ctCollection == ctCollection1) {
				priority = priority - 1;
			}

			try (SafeCloseable safeCloseable =
					CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
						ctCollection.getCtCollectionId())) {

				SegmentsExperience publishedSegmentsExperience =
					_segmentsExperienceLocalService.getSegmentsExperience(
						_publishedSegmentsExperienceId2);

				publishedSegmentsExperience.setPriority(priority);

				_segmentsExperienceLocalService.updateSegmentsExperience(
					publishedSegmentsExperience);
			}

			SegmentsExperience ctCollectionDraftSegmentsExperience =
				(SegmentsExperience)draftSegmentsExperience.clone();

			ctCollectionDraftSegmentsExperience.setPriority(priority);

			SegmentsExperience segmentsExperience = _addSegmentsExperience(
				ctCollection, draftSegmentsExperience.getPlid(),
				ctCollectionDraftSegmentsExperience);

			_deleteCTEntry(ctCollection, segmentsExperience);
		}

		_ctCollectionService.publishCTCollection(
			TestPropsValues.getUserId(), ctCollection1.getCtCollectionId());
		_ctCollectionService.publishCTCollection(
			TestPropsValues.getUserId(), ctCollection2.getCtCollectionId());

		_segmentsExperienceLocalService.deleteSegmentsExperience(
			draftSegmentsExperience);

		_runUpgrade();
		_runUpgrade();

		Assert.assertEquals(
			2,
			_segmentsExperienceLocalService.getSegmentsExperiencesCount(
				_group.getGroupId(), draftSegmentsExperience.getPlid()));
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithScheduledCTCollectionAddedSegmentsExperience()
		throws Exception {

		CTCollection ctCollection = _addCTCollection();

		SegmentsExperience segmentsExperience = _addSegmentsExperience(
			ctCollection);

		ctCollection.setStatus(WorkflowConstants.STATUS_SCHEDULED);

		ctCollection = _ctCollectionLocalService.updateCTCollection(
			ctCollection);

		_runUpgrade();
		_runUpgrade();

		_ctProcessLocalService.addCTProcess(
			TestPropsValues.getUserId(), ctCollection.getCtCollectionId());

		_assertSegmentsExperience(segmentsExperience);
	}

	@Test
	@TestInfo("LPD-111365")
	public void testUpgradeWithoutCTCollectionIdAndSegmentsExperienceIdColumns()
		throws Exception {

		_updateSegmentsExperienceIds();

		try {
			_db.alterColumnName(
				_connection, "FragmentEntryLink", "segmentsExperienceId",
				"segmentsExperienceId2 LONG");
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructure", "plid",
				"classPK LONG");
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructureRel", "ctCollectionId",
				"ctCollectionId2 LONG default 0 not null");

			_runUpgrade();
		}
		finally {
			_db.alterColumnName(
				_connection, "FragmentEntryLink", "segmentsExperienceId2",
				"segmentsExperienceId LONG");
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructure", "classPK",
				"plid LONG");
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructureRel",
				"ctCollectionId2", "ctCollectionId LONG default 0 not null");
		}

		_assertSegmentsExperienceId(
			_draftSegmentsExperienceId1, _layoutPageTemplateStructureRel1);
		_assertSegmentsExperienceId(
			_publishedSegmentsExperienceId1, _fragmentEntryLink1);
	}

	@Test
	@TestInfo("LPD-111365")
	public void testUpgradeWithoutCTCollectionIdColumns() throws Exception {
		_updateSegmentsExperienceIds();

		try {
			_db.alterColumnName(
				_connection, "FragmentEntryLink", "ctCollectionId",
				"ctCollectionId2 LONG default 0 not null");
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructure", "plid",
				"classPK LONG");
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructureRel", "ctCollectionId",
				"ctCollectionId2 LONG default 0 not null");

			_runUpgrade();
		}
		finally {
			_db.alterColumnName(
				_connection, "FragmentEntryLink", "ctCollectionId2",
				"ctCollectionId LONG default 0 not null");
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructure", "classPK",
				"plid LONG");
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructureRel",
				"ctCollectionId2", "ctCollectionId LONG default 0 not null");
		}

		_assertSegmentsExperienceId(
			_draftSegmentsExperienceId1, _fragmentEntryLink1,
			_layoutPageTemplateStructureRel1);
	}

	private CTCollection _addCTCollection() throws Exception {
		return _ctCollectionLocalService.addCTCollection(
			null, _group.getCompanyId(), TestPropsValues.getUserId(), 0,
			RandomTestUtil.randomString(), RandomTestUtil.randomString());
	}

	private SegmentsExperience _addSegmentsExperience(CTCollection ctCollection)
		throws Exception {

		SegmentsExperience segmentsExperience = null;

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			ServiceContextThreadLocal.pushServiceContext(
				ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

			Layout draftLayout = _layout.fetchDraftLayout();

			segmentsExperience = SegmentsTestUtil.addSegmentsExperience(
				_group.getGroupId(), draftLayout.getPlid());

			ContentLayoutTestUtil.addFragmentEntryLinkToLayout(
				JSONFactoryUtil.getNullJSON(), draftLayout,
				segmentsExperience.getSegmentsExperienceId());
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
		}

		_deleteCTEntry(ctCollection, segmentsExperience);

		return segmentsExperience;
	}

	private SegmentsExperience _addSegmentsExperience(
		CTCollection ctCollection, long plid,
		SegmentsExperience segmentsExperience) {

		SegmentsExperience ctCollectionSegmentsExperience =
			(SegmentsExperience)segmentsExperience.clone();

		ctCollectionSegmentsExperience.setUuid(PortalUUIDUtil.generate());
		ctCollectionSegmentsExperience.setExternalReferenceCode(
			PortalUUIDUtil.generate());
		ctCollectionSegmentsExperience.setSegmentsExperienceId(
			RandomTestUtil.nextLong());
		ctCollectionSegmentsExperience.setPlid(plid);
		ctCollectionSegmentsExperience.setNew(true);

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			return _segmentsExperienceLocalService.addSegmentsExperience(
				ctCollectionSegmentsExperience);
		}
	}

	private void _assertDraftSegmentsExperienceIds() {
		_assertSegmentsExperienceId(
			_draftSegmentsExperienceId1, _fragmentEntryLink1,
			_layoutPageTemplateStructureRel1);
		_assertSegmentsExperienceId(
			_draftSegmentsExperienceId2, _fragmentEntryLink2,
			_layoutPageTemplateStructureRel2);
	}

	private void _assertDraftSegmentsExperienceIds(CTCollection ctCollection) {
		_assertDraftSegmentsExperienceIds();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_assertDraftSegmentsExperienceIds();
		}
	}

	private void _assertSegmentsExperience(
		SegmentsExperience segmentsExperience) {

		_multiVMPool.clear();

		Assert.assertNotNull(
			_segmentsExperienceLocalService.fetchSegmentsExperience(
				segmentsExperience.getSegmentsExperienceId()));

		List<FragmentEntryLink> fragmentEntryLinks =
			_fragmentEntryLinkLocalService.
				getFragmentEntryLinksBySegmentsExperienceId(
					_group.getGroupId(),
					segmentsExperience.getSegmentsExperienceId(),
					segmentsExperience.getPlid());

		Assert.assertEquals(
			fragmentEntryLinks.toString(), 1, fragmentEntryLinks.size());
	}

	private void _assertSegmentsExperienceId(
		long expectedSegmentsExperienceId,
		FragmentEntryLink fragmentEntryLink) {

		fragmentEntryLink =
			_fragmentEntryLinkLocalService.fetchFragmentEntryLink(
				fragmentEntryLink.getFragmentEntryLinkId());

		Assert.assertEquals(
			expectedSegmentsExperienceId,
			fragmentEntryLink.getSegmentsExperienceId());
	}

	private void _assertSegmentsExperienceId(
		long expectedSegmentsExperienceId, FragmentEntryLink fragmentEntryLink,
		LayoutPageTemplateStructureRel layoutPageTemplateStructureRel) {

		_assertSegmentsExperienceId(
			expectedSegmentsExperienceId, fragmentEntryLink);
		_assertSegmentsExperienceId(
			expectedSegmentsExperienceId, layoutPageTemplateStructureRel);
	}

	private void _assertSegmentsExperienceId(
		long expectedSegmentsExperienceId,
		LayoutPageTemplateStructureRel layoutPageTemplateStructureRel) {

		layoutPageTemplateStructureRel =
			_layoutPageTemplateStructureRelLocalService.
				fetchLayoutPageTemplateStructureRel(
					layoutPageTemplateStructureRel.
						getLayoutPageTemplateStructureRelId());

		Assert.assertEquals(
			expectedSegmentsExperienceId,
			layoutPageTemplateStructureRel.getSegmentsExperienceId());
	}

	private void _deleteCTEntry(
			CTCollection ctCollection, SegmentsExperience segmentsExperience)
		throws Exception {

		_ctEntryLocalService.deleteCTEntry(
			_ctEntryLocalService.fetchCTEntry(
				ctCollection.getCtCollectionId(),
				PortalUtil.getClassNameId(SegmentsExperience.class),
				segmentsExperience.getSegmentsExperienceId()));
	}

	private void _runUpgrade() throws Exception {
		UpgradeProcess[] upgradeProcesses = UpgradeTestUtil.getUpgradeSteps(
			_upgradeStepRegistrator, new Version(4, 2, 2));

		for (UpgradeProcess upgradeProcess : upgradeProcesses) {
			upgradeProcess.upgrade();
		}

		_entityCache.clearCache();
		_multiVMPool.clear();
	}

	private FragmentEntryLink _updateSegmentsExperienceId(
		FragmentEntryLink fragmentEntryLink, long segmentsExperienceId) {

		fragmentEntryLink.setSegmentsExperienceId(segmentsExperienceId);

		return _fragmentEntryLinkLocalService.updateFragmentEntryLink(
			fragmentEntryLink);
	}

	private LayoutPageTemplateStructureRel _updateSegmentsExperienceId(
		LayoutPageTemplateStructureRel layoutPageTemplateStructureRel,
		long segmentsExperienceId) {

		layoutPageTemplateStructureRel.setSegmentsExperienceId(
			segmentsExperienceId);

		return _layoutPageTemplateStructureRelLocalService.
			updateLayoutPageTemplateStructureRel(
				layoutPageTemplateStructureRel);
	}

	private void _updateSegmentsExperienceIds() {
		_fragmentEntryLink1 = _updateSegmentsExperienceId(
			_fragmentEntryLink1, _publishedSegmentsExperienceId1);
		_fragmentEntryLink2 = _updateSegmentsExperienceId(
			_fragmentEntryLink2, _publishedSegmentsExperienceId2);
		_layoutPageTemplateStructureRel1 = _updateSegmentsExperienceId(
			_layoutPageTemplateStructureRel1, _publishedSegmentsExperienceId1);
		_layoutPageTemplateStructureRel2 = _updateSegmentsExperienceId(
			_layoutPageTemplateStructureRel2, _publishedSegmentsExperienceId2);
	}

	private Connection _connection;

	@Inject
	private CTCollectionLocalService _ctCollectionLocalService;

	@Inject
	private CTCollectionService _ctCollectionService;

	@Inject
	private CTEntryLocalService _ctEntryLocalService;

	@Inject
	private CTProcessLocalService _ctProcessLocalService;

	private DB _db;
	private long _draftSegmentsExperienceId1;
	private long _draftSegmentsExperienceId2;

	@Inject
	private EntityCache _entityCache;

	private FragmentEntryLink _fragmentEntryLink1;
	private FragmentEntryLink _fragmentEntryLink2;

	@Inject
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@DeleteAfterTestRun
	private Group _group;

	private Layout _layout;

	@Inject
	private LayoutPageTemplateStructureLocalService
		_layoutPageTemplateStructureLocalService;

	private LayoutPageTemplateStructureRel _layoutPageTemplateStructureRel1;
	private LayoutPageTemplateStructureRel _layoutPageTemplateStructureRel2;

	@Inject
	private LayoutPageTemplateStructureRelLocalService
		_layoutPageTemplateStructureRelLocalService;

	@Inject
	private MultiVMPool _multiVMPool;

	private long _publishedSegmentsExperienceId1;
	private long _publishedSegmentsExperienceId2;

	@Inject
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

	@Inject(
		filter = "(&(component.name=com.liferay.segments.internal.upgrade.registry.SegmentsServiceUpgradeStepRegistrator))"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}