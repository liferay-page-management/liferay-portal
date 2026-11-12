/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.segments.internal.upgrade.v3_2_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.service.CTCollectionLocalService;
import com.liferay.change.tracking.service.CTCollectionService;
import com.liferay.change.tracking.test.util.BaseCTUpgradeProcessTestCase;
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
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.change.tracking.CTModel;
import com.liferay.portal.kernel.service.ResourceLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.change.tracking.CTService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;
import com.liferay.segments.constants.SegmentsExperienceConstants;
import com.liferay.segments.model.SegmentsExperience;
import com.liferay.segments.service.SegmentsExperienceLocalService;
import com.liferay.segments.test.util.SegmentsTestUtil;

import java.sql.Connection;

import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Eudaldo Alonso
 */
@RunWith(Arquillian.class)
public class SegmentsExperienceUpgradeProcessTest
	extends BaseCTUpgradeProcessTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		_connection = DataAccess.getConnection();
		_db = DBManagerUtil.getDB();

		_db.alterTableAddColumn(
			_connection, "SegmentsExperience", "segmentsEntryId", "LONG");
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		_db.alterTableDropColumn(
			_connection, "SegmentsExperience", "segmentsEntryId");

		DataAccess.cleanUp(_connection);
	}

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_publishedLayout = LayoutTestUtil.addTypeContentLayout(_group);

		_draftLayout = _publishedLayout.fetchDraftLayout();

		try {
			ServiceContextThreadLocal.pushServiceContext(
				ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

			ContentLayoutTestUtil.addFragmentEntryLinkToLayout(
				JSONFactoryUtil.getNullJSON(), _draftLayout,
				_segmentsExperienceLocalService.
					fetchDefaultSegmentsExperienceId(_draftLayout.getPlid()));

			SegmentsExperience segmentsExperience =
				SegmentsTestUtil.addSegmentsExperience(
					_group.getGroupId(), _draftLayout.getPlid());

			ContentLayoutTestUtil.addFragmentEntryLinkToLayout(
				JSONFactoryUtil.getNullJSON(), _draftLayout,
				segmentsExperience.getSegmentsExperienceId());

			ContentLayoutTestUtil.publishLayout(_draftLayout, _publishedLayout);
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
		}
	}

	@Test
	@TestInfo("LPD-54314")
	public void testUpgrade() throws Exception {
		_deleteSegmentsExperiences();

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.addSegmentsExperience(
				null, TestPropsValues.getUserId(), _group.getGroupId(), null,
				null, SegmentsExperienceConstants.KEY_DEFAULT,
				RandomTestUtil.randomLong(),
				RandomTestUtil.randomLocaleStringMap(), 0, true,
				new UnicodeProperties(true),
				ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		runUpgrade();

		_assertSegmentsExperiences();

		Assert.assertNotNull(
			_segmentsExperienceLocalService.getSegmentsExperience(
				segmentsExperience.getSegmentsExperienceId()));
	}

	@Test
	@TestInfo("LPD-111365")
	public void testUpgradeWithCTCollection() throws Exception {
		_deleteSegmentsExperiences();

		runUpgrade();

		_updateLayoutPageTemplateStructureRels();

		CTCollection ctCollection = _addCTCollection();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_addLayoutPageTemplateStructureRels();
		}

		runUpgrade();

		_assertLayoutPageTemplateStructureRels(_draftLayout.getPlid());
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithCTCollectionDraftLayout() throws Exception {
		_deleteSegmentsExperiences();

		CTCollection ctCollection = _addCTCollection();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_updateFragmentEntryLinks();
			_updateLayoutPageTemplateStructureRels();
		}

		runUpgrade();

		_assertSegmentsExperiences(ctCollection);

		runUpgrade();

		_assertSegmentsExperiences(ctCollection);
	}

	@Test
	public void testUpgradeWithClassPKColumns() throws Exception {
		_deleteSegmentsExperiences();

		try {
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructure", "plid",
				"classPK LONG");
			_db.alterColumnName(
				_connection, "FragmentEntryLink", "plid", "plid2 LONG");

			runUpgrade();
		}
		finally {
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructure", "classPK",
				"plid LONG");
			_db.alterColumnName(
				_connection, "FragmentEntryLink", "plid2", "plid LONG");
		}

		_assertSegmentsExperiences();
	}

	@Test
	@TestInfo("LPD-111365")
	public void testUpgradeWithExistingDraftLayoutSegmentsExperiences()
		throws Exception {

		_deleteSegmentsExperiences();

		runUpgrade();

		_updateFragmentEntryLinks();
		_updateLayoutPageTemplateStructureRels();

		runUpgrade();

		_assertSegmentsExperiences();
	}

	@Test
	public void testUpgradeWithFragmentEntryLinkClassPKColumn()
		throws Exception {

		_deleteSegmentsExperiences();

		try {
			_db.alterColumnName(
				_connection, "FragmentEntryLink", "plid", "plid2 LONG");

			runUpgrade();
		}
		finally {
			_db.alterColumnName(
				_connection, "FragmentEntryLink", "plid2", "plid LONG");
		}

		_assertSegmentsExperiences();
	}

	@Test
	public void testUpgradeWithLayoutPageTemplateStructureClassPKColumn()
		throws Exception {

		_deleteSegmentsExperiences();

		try {
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructure", "plid",
				"classPK LONG");

			runUpgrade();
		}
		finally {
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructure", "classPK",
				"plid LONG");
		}

		_assertSegmentsExperiences();
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithPublishedCTCollectionSegmentsExperiences()
		throws Exception {

		_deleteSegmentsExperiences();

		CTCollection ctCollection = _addCTCollection();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_updateFragmentEntryLinks();
			_updateLayoutPageTemplateStructureRels();

			for (SegmentsExperience segmentsExperience :
					_segmentsExperienceLocalService.getSegmentsExperiences(
						_group.getGroupId(), _publishedLayout.getPlid())) {

				updateCTModel(segmentsExperience);
			}
		}

		runUpgrade();
		runUpgrade();

		_ctCollectionService.publishCTCollection(
			TestPropsValues.getUserId(), ctCollection.getCtCollectionId());

		_multiVMPool.clear();

		_assertSegmentsExperiences();
	}

	@Test
	public void testUpgradeWithoutCTCollectionIdAndSegmentsExperienceIdColumns()
		throws Exception {

		_deleteSegmentsExperiences();

		try {
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructure", "plid",
				"classPK LONG");
			_db.alterTableDropColumn(
				_connection, "FragmentEntryLink", "segmentsExperienceId");
			_db.alterTableDropColumn(
				_connection, "LayoutPageTemplateStructureRel",
				"ctCollectionId");

			runUpgrade();
		}
		finally {
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructure", "classPK",
				"plid LONG");
			_db.alterTableAddColumn(
				_connection, "FragmentEntryLink", "segmentsExperienceId",
				"LONG");
			_db.alterTableAddColumn(
				_connection, "LayoutPageTemplateStructureRel", "ctCollectionId",
				"LONG default 0 not null");
		}

		_assertSegmentsExperiences(0);
	}

	@Test
	public void testUpgradeWithoutCTCollectionIdColumns() throws Exception {
		_deleteSegmentsExperiences();

		try {
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructure", "plid",
				"classPK LONG");
			_db.alterTableDropColumn(
				_connection, "FragmentEntryLink", "ctCollectionId");
			_db.alterTableDropColumn(
				_connection, "LayoutPageTemplateStructureRel",
				"ctCollectionId");

			runUpgrade();
		}
		finally {
			_db.alterColumnName(
				_connection, "LayoutPageTemplateStructure", "classPK",
				"plid LONG");
			_db.alterTableAddColumn(
				_connection, "FragmentEntryLink", "ctCollectionId",
				"LONG default 0 not null");
			_db.alterTableAddColumn(
				_connection, "LayoutPageTemplateStructureRel", "ctCollectionId",
				"LONG default 0 not null");
		}

		_assertSegmentsExperiences();
	}

	@Override
	protected CTModel<?> addCTModel() throws Exception {
		return SegmentsTestUtil.addSegmentsExperience(
			_group.getGroupId(), SegmentsExperienceConstants.KEY_DEFAULT, null,
			_draftLayout.getPlid());
	}

	@Override
	protected void deleteCTModel(long primaryKey) throws Exception {
		_segmentsExperienceLocalService.deleteSegmentsExperience(primaryKey);
	}

	@Override
	protected CTService<?> getCTService() {
		return _segmentsExperienceLocalService;
	}

	@Override
	protected void runUpgrade() throws Exception {
		UpgradeProcess upgradeProcess = UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator, _CLASS_NAME);

		upgradeProcess.upgrade();

		_multiVMPool.clear();
	}

	@Override
	protected CTModel<?> updateCTModel(CTModel<?> ctModel) throws Exception {
		SegmentsExperience segmentsExperience = (SegmentsExperience)ctModel;

		segmentsExperience.setName(RandomTestUtil.randomString());

		return _segmentsExperienceLocalService.updateSegmentsExperience(
			segmentsExperience);
	}

	private CTCollection _addCTCollection() throws Exception {
		return _ctCollectionLocalService.addCTCollection(
			null, _group.getCompanyId(), TestPropsValues.getUserId(), 0,
			RandomTestUtil.randomString(), RandomTestUtil.randomString());
	}

	private void _addLayoutPageTemplateStructureRels() throws Exception {
		long layoutPageTemplateStructureId =
			_getLayoutPageTemplateStructureId();
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(_group.getGroupId());

		for (SegmentsExperience segmentsExperience :
				_segmentsExperienceLocalService.getSegmentsExperiences(
					_group.getGroupId(), _draftLayout.getPlid())) {

			_layoutPageTemplateStructureRelLocalService.
				addLayoutPageTemplateStructureRel(
					TestPropsValues.getUserId(), _group.getGroupId(),
					layoutPageTemplateStructureId,
					segmentsExperience.getSegmentsExperienceId(),
					JSONFactoryUtil.getNullJSON(), serviceContext);
		}
	}

	private void _assertFragmentEntryLinks(
		int expectedCount, List<SegmentsExperience> segmentsExperiences) {

		for (SegmentsExperience segmentsExperience : segmentsExperiences) {
			List<FragmentEntryLink> fragmentEntryLinks =
				_fragmentEntryLinkLocalService.
					getFragmentEntryLinksBySegmentsExperienceId(
						_group.getGroupId(),
						segmentsExperience.getSegmentsExperienceId(),
						_draftLayout.getPlid());

			Assert.assertEquals(
				fragmentEntryLinks.toString(), expectedCount,
				fragmentEntryLinks.size());
		}
	}

	private void _assertLayoutPageTemplateStructureRels(long expectedPlid) {
		for (LayoutPageTemplateStructureRel layoutPageTemplateStructureRel :
				_layoutPageTemplateStructureRelLocalService.
					getLayoutPageTemplateStructureRels(
						_getLayoutPageTemplateStructureId())) {

			SegmentsExperience segmentsExperience =
				_segmentsExperienceLocalService.fetchSegmentsExperience(
					layoutPageTemplateStructureRel.getSegmentsExperienceId());

			Assert.assertEquals(expectedPlid, segmentsExperience.getPlid());
		}
	}

	private void _assertSegmentsExperiences() {
		_assertSegmentsExperiences(1);
	}

	private void _assertSegmentsExperiences(CTCollection ctCollection) {
		_assertSegmentsExperiences();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_assertSegmentsExperiences();
		}
	}

	private void _assertSegmentsExperiences(
		int fragmentEntryLinksExpectedCount) {

		List<SegmentsExperience> segmentsExperiences =
			_segmentsExperienceLocalService.getSegmentsExperiences(
				_group.getGroupId(), _draftLayout.getPlid());

		_assertFragmentEntryLinks(
			fragmentEntryLinksExpectedCount, segmentsExperiences);

		_assertLayoutPageTemplateStructureRels(_draftLayout.getPlid());
		_assertSegmentsExperiences(2, segmentsExperiences);
	}

	private void _assertSegmentsExperiences(
		int expectedCount, List<SegmentsExperience> segmentsExperiences) {

		Assert.assertEquals(
			segmentsExperiences.toString(), expectedCount,
			segmentsExperiences.size());

		for (SegmentsExperience segmentsExperience : segmentsExperiences) {
			Assert.assertNotNull(
				_segmentsExperienceLocalService.fetchSegmentsExperience(
					_group.getGroupId(),
					segmentsExperience.getSegmentsExperienceKey(),
					_publishedLayout.getPlid()));
		}
	}

	private void _deleteSegmentsExperiences() throws Exception {
		_updateFragmentEntryLinks();
		_updateLayoutPageTemplateStructureRels();

		List<SegmentsExperience> segmentsExperiences =
			_segmentsExperienceLocalService.getSegmentsExperiences(
				_group.getGroupId(), _draftLayout.getPlid());

		for (SegmentsExperience segmentsExperience : segmentsExperiences) {
			_resourceLocalService.deleteResource(
				segmentsExperience, ResourceConstants.SCOPE_INDIVIDUAL);

			_db.runSQL(
				"delete from SegmentsExperience where segmentsExperienceId = " +
					segmentsExperience.getSegmentsExperienceId());
		}

		_multiVMPool.clear();

		_assertFragmentEntryLinks(0, segmentsExperiences);

		_assertLayoutPageTemplateStructureRels(_publishedLayout.getPlid());
		_assertSegmentsExperiences(0, Collections.emptyList());
	}

	private long _getLayoutPageTemplateStructureId() {
		LayoutPageTemplateStructure layoutPageTemplateStructure =
			_layoutPageTemplateStructureLocalService.
				fetchLayoutPageTemplateStructure(
					_group.getGroupId(), _draftLayout.getPlid());

		return layoutPageTemplateStructure.getLayoutPageTemplateStructureId();
	}

	private long _getPublishedSegmentsExperienceId(long segmentsExperienceId) {
		SegmentsExperience draftSegmentsExperience =
			_segmentsExperienceLocalService.fetchSegmentsExperience(
				segmentsExperienceId);

		SegmentsExperience publishedSegmentsExperience =
			_segmentsExperienceLocalService.fetchSegmentsExperience(
				_group.getGroupId(),
				draftSegmentsExperience.getSegmentsExperienceKey(),
				_publishedLayout.getPlid());

		return publishedSegmentsExperience.getSegmentsExperienceId();
	}

	private void _updateFragmentEntryLinks() {
		for (FragmentEntryLink fragmentEntryLink :
				_fragmentEntryLinkLocalService.getFragmentEntryLinksByPlid(
					_group.getGroupId(), _draftLayout.getPlid())) {

			fragmentEntryLink.setSegmentsExperienceId(
				_getPublishedSegmentsExperienceId(
					fragmentEntryLink.getSegmentsExperienceId()));

			_fragmentEntryLinkLocalService.updateFragmentEntryLink(
				fragmentEntryLink);
		}
	}

	private void _updateLayoutPageTemplateStructureRels() {
		for (LayoutPageTemplateStructureRel layoutPageTemplateStructureRel :
				_layoutPageTemplateStructureRelLocalService.
					getLayoutPageTemplateStructureRels(
						_getLayoutPageTemplateStructureId())) {

			layoutPageTemplateStructureRel.setSegmentsExperienceId(
				_getPublishedSegmentsExperienceId(
					layoutPageTemplateStructureRel.getSegmentsExperienceId()));

			_layoutPageTemplateStructureRelLocalService.
				updateLayoutPageTemplateStructureRel(
					layoutPageTemplateStructureRel);
		}
	}

	private static final String _CLASS_NAME =
		"com.liferay.segments.internal.upgrade.v3_2_0." +
			"SegmentsExperienceUpgradeProcess";

	private static Connection _connection;
	private static DB _db;

	@Inject
	private CTCollectionLocalService _ctCollectionLocalService;

	@Inject
	private CTCollectionService _ctCollectionService;

	private Layout _draftLayout;

	@Inject
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private LayoutPageTemplateStructureLocalService
		_layoutPageTemplateStructureLocalService;

	@Inject
	private LayoutPageTemplateStructureRelLocalService
		_layoutPageTemplateStructureRelLocalService;

	@Inject
	private MultiVMPool _multiVMPool;

	private Layout _publishedLayout;

	@Inject
	private ResourceLocalService _resourceLocalService;

	@Inject
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

	@Inject(
		filter = "(&(component.name=com.liferay.segments.internal.upgrade.registry.SegmentsServiceUpgradeStepRegistrator))"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}