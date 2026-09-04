/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.internal.upgrade.v6_3_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.service.CTCollectionLocalService;
import com.liferay.counter.kernel.service.CounterLocalService;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructure;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructureRel;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalService;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureRelLocalService;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.layout.utility.page.model.LayoutUtilityPageEntry;
import com.liferay.layout.utility.page.service.LayoutUtilityPageEntryLocalService;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.cache.MultiVMPool;
import com.liferay.portal.kernel.change.tracking.CTCollectionThreadLocal;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ResourceLocalService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;
import com.liferay.segments.constants.SegmentsExperienceConstants;
import com.liferay.segments.model.SegmentsExperience;
import com.liferay.segments.service.SegmentsExperienceLocalService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Balázs Sáfrány-Kovalik
 */
@RunWith(Arquillian.class)
public class DefaultSegmentsExperienceUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@Test
	@TestInfo("LPD-103969")
	public void testUpgradeAddsDefaultSegmentsExperienceForAmbiguousLayout()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		long segmentsExperienceId1 = RandomTestUtil.randomLong();
		long segmentsExperienceId2 = RandomTestUtil.randomLong();

		FragmentEntryLink fragmentEntryLink1 = _addFragmentEntryLink(
			layout, segmentsExperienceId1);
		FragmentEntryLink fragmentEntryLink2 = _addFragmentEntryLink(
			layout, segmentsExperienceId2);

		_deleteDefaultSegmentsExperience(layout);

		_runUpgrade();

		Assert.assertNotNull(
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperience(
				layout.getPlid()));

		Assert.assertEquals(
			segmentsExperienceId1,
			_getFragmentEntryLinkSegmentsExperienceId(
				fragmentEntryLink1.getFragmentEntryLinkId()));
		Assert.assertEquals(
			segmentsExperienceId2,
			_getFragmentEntryLinkSegmentsExperienceId(
				fragmentEntryLink2.getFragmentEntryLinkId()));
	}

	@Test
	@TestInfo("LPD-103969")
	public void testUpgradeAddsMissingDefaultSegmentsExperience()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		_deleteDefaultSegmentsExperience(layout);

		Assert.assertNull(
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperience(
				layout.getPlid()));

		_runUpgrade();

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperience(
				layout.getPlid());

		Assert.assertEquals(
			layout.getExternalReferenceCode() +
				LayoutConstants.EXTERNAL_REFERENCE_CODE_SUFFIX_DEFAULT,
			segmentsExperience.getExternalReferenceCode());
	}

	@Test
	@TestInfo("LPD-103969")
	public void testUpgradeAddsMissingDefaultSegmentsExperienceForUtilityPage()
		throws Exception {

		LayoutUtilityPageEntry layoutUtilityPageEntry =
			_layoutUtilityPageEntryLocalService.addLayoutUtilityPageEntry(
				null, TestPropsValues.getUserId(), _group.getGroupId(), 0, 0,
				false, RandomTestUtil.randomString(),
				RandomTestUtil.randomString(), null,
				ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		Layout layout = _layoutLocalService.getLayout(
			layoutUtilityPageEntry.getPlid());

		_deleteDefaultSegmentsExperience(layout);

		Assert.assertNull(
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperience(
				layout.getPlid()));

		_runUpgrade();

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperience(
				layout.getPlid());

		Assert.assertEquals(
			layout.getExternalReferenceCode() +
				LayoutConstants.EXTERNAL_REFERENCE_CODE_SUFFIX_DEFAULT,
			segmentsExperience.getExternalReferenceCode());
	}

	@Test
	@TestInfo("LPD-103969")
	public void testUpgradeAddsSingleDefaultSegmentsExperienceForPublication()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		_deleteDefaultSegmentsExperience(layout);

		CTCollection ctCollection = _addCTCollection();

		try {
			_updateLayoutInCTCollection(ctCollection, layout);

			_runUpgrade();

			Assert.assertEquals(
				1L, _getDefaultSegmentsExperienceCount(layout.getPlid()));
		}
		finally {
			_ctCollectionLocalService.deleteCTCollection(ctCollection);
		}
	}

	@Test
	@TestInfo("LPD-103969")
	public void testUpgradeDeletesDuplicateLayoutPageTemplateStructureRel()
		throws Exception {

		Layout layout1 = LayoutTestUtil.addTypeContentLayout(_group);
		Layout layout2 = LayoutTestUtil.addTypeContentLayout(_group);

		long segmentsExperienceId1 = _getDefaultSegmentsExperienceId(
			layout1.getPlid());
		long segmentsExperienceId2 = _getDefaultSegmentsExperienceId(
			layout2.getPlid());

		_deleteDefaultSegmentsExperience(layout1);

		_addLayoutPageTemplateStructureRel(layout1, segmentsExperienceId2);

		_runUpgrade();

		Assert.assertEquals(
			1,
			_getLayoutPageTemplateStructureRelCount(
				layout1.getPlid(),
				_getDefaultSegmentsExperienceId(layout1.getPlid())));
		Assert.assertEquals(
			0,
			_getLayoutPageTemplateStructureRelCount(
				layout1.getPlid(), segmentsExperienceId1));
		Assert.assertEquals(
			0,
			_getLayoutPageTemplateStructureRelCount(
				layout1.getPlid(), segmentsExperienceId2));
	}

	@Test
	@TestInfo("LPD-103969")
	public void testUpgradeDeletesOrphanedLayoutPageTemplateStructureRel()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		long segmentsExperienceId = RandomTestUtil.randomLong();

		_addLayoutPageTemplateStructureRel(layout, segmentsExperienceId);

		_runUpgrade();

		Assert.assertEquals(
			1,
			_getLayoutPageTemplateStructureRelCount(
				layout.getPlid(),
				_getDefaultSegmentsExperienceId(layout.getPlid())));
		Assert.assertEquals(
			0,
			_getLayoutPageTemplateStructureRelCount(
				layout.getPlid(), segmentsExperienceId));
	}

	@Test
	@TestInfo("LPD-103969")
	public void testUpgradeKeepsExistingDefaultSegmentsExperience()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperience(
				layout.getPlid());

		_runUpgrade();

		SegmentsExperience upgradedSegmentsExperience =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperience(
				layout.getPlid());

		Assert.assertEquals(
			segmentsExperience.getSegmentsExperienceId(),
			upgradedSegmentsExperience.getSegmentsExperienceId());
	}

	@Test
	@TestInfo("LPD-103969")
	public void testUpgradeRepointsMisScopedDefaultSegmentsExperience()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperience(
				layout.getPlid());

		Layout draftLayout = layout.fetchDraftLayout();

		_deleteDefaultSegmentsExperience(draftLayout);

		_updateLayoutPageTemplateStructureRel(
			draftLayout.getPlid(),
			segmentsExperience.getSegmentsExperienceId());

		FragmentEntryLink fragmentEntryLink = _addFragmentEntryLink(
			draftLayout, RandomTestUtil.randomLong());

		_runUpgrade();

		long defaultSegmentsExperienceId = _getDefaultSegmentsExperienceId(
			draftLayout.getPlid());

		Assert.assertNotEquals(
			segmentsExperience.getSegmentsExperienceId(),
			defaultSegmentsExperienceId);

		Assert.assertEquals(
			defaultSegmentsExperienceId,
			_getLayoutPageTemplateStructureRelSegmentsExperienceId(
				0, draftLayout.getPlid()));
		Assert.assertEquals(
			defaultSegmentsExperienceId,
			_getFragmentEntryLinkSegmentsExperienceId(
				fragmentEntryLink.getFragmentEntryLinkId()));
	}

	@Test
	@TestInfo("LPD-103969")
	public void testUpgradeUpdatesFragmentEntryLink() throws Exception {
		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperience(
				layout.getPlid());

		FragmentEntryLink fragmentEntryLink = _addFragmentEntryLink(
			layout, segmentsExperience.getSegmentsExperienceId());

		_deleteDefaultSegmentsExperience(layout);

		_runUpgrade();

		SegmentsExperience upgradedSegmentsExperience =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperience(
				layout.getPlid());

		Assert.assertEquals(
			upgradedSegmentsExperience.getSegmentsExperienceId(),
			_getFragmentEntryLinkSegmentsExperienceId(
				fragmentEntryLink.getFragmentEntryLinkId()));
	}

	@Test
	@TestInfo("LPD-103969")
	public void testUpgradeUpdatesLayoutPageTemplateStructureRel()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		_deleteDefaultSegmentsExperience(layout);

		_runUpgrade();

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperience(
				layout.getPlid());

		Assert.assertEquals(
			segmentsExperience.getSegmentsExperienceId(),
			_getLayoutPageTemplateStructureRelSegmentsExperienceId(
				0, layout.getPlid()));
	}

	@Test
	@TestInfo("LPD-103969")
	public void testUpgradeUpdatesLayoutPageTemplateStructureRelInPublication()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperience(
				layout.getPlid());

		LayoutPageTemplateStructure layoutPageTemplateStructure =
			_layoutPageTemplateStructureLocalService.
				fetchLayoutPageTemplateStructure(
					layout.getGroupId(), layout.getPlid());

		CTCollection ctCollection = _addCTCollection();

		try {
			try (SafeCloseable safeCloseable =
					CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
						ctCollection.getCtCollectionId())) {

				LayoutPageTemplateStructureRel layoutPageTemplateStructureRel =
					_layoutPageTemplateStructureRelLocalService.
						fetchLayoutPageTemplateStructureRel(
							layoutPageTemplateStructure.
								getLayoutPageTemplateStructureId(),
							segmentsExperience.getSegmentsExperienceId());

				layoutPageTemplateStructureRel.setSegmentsExperienceId(
					RandomTestUtil.randomLong());

				_layoutPageTemplateStructureRelLocalService.
					updateLayoutPageTemplateStructureRel(
						layoutPageTemplateStructureRel);
			}

			_updateLayoutInCTCollection(ctCollection, layout);

			_runUpgrade();

			Assert.assertEquals(
				segmentsExperience.getSegmentsExperienceId(),
				_getLayoutPageTemplateStructureRelSegmentsExperienceId(
					ctCollection.getCtCollectionId(), layout.getPlid()));
		}
		finally {
			_ctCollectionLocalService.deleteCTCollection(ctCollection);
		}
	}

	private CTCollection _addCTCollection() throws Exception {
		return _ctCollectionLocalService.addCTCollection(
			null, TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			0, RandomTestUtil.randomString(), RandomTestUtil.randomString());
	}

	private FragmentEntryLink _addFragmentEntryLink(
			Layout layout, long segmentsExperienceId)
		throws Exception {

		FragmentEntryLink fragmentEntryLink =
			_fragmentEntryLinkLocalService.createFragmentEntryLink(
				_counterLocalService.increment(
					FragmentEntryLink.class.getName()));

		fragmentEntryLink.setExternalReferenceCode(
			RandomTestUtil.randomString());
		fragmentEntryLink.setGroupId(layout.getGroupId());
		fragmentEntryLink.setCompanyId(layout.getCompanyId());
		fragmentEntryLink.setUserId(TestPropsValues.getUserId());
		fragmentEntryLink.setCreateDate(new Date());
		fragmentEntryLink.setModifiedDate(new Date());
		fragmentEntryLink.setSegmentsExperienceId(segmentsExperienceId);
		fragmentEntryLink.setPlid(layout.getPlid());

		return _fragmentEntryLinkLocalService.addFragmentEntryLink(
			fragmentEntryLink);
	}

	private void _addLayoutPageTemplateStructureRel(
			Layout layout, long segmentsExperienceId)
		throws Exception {

		LayoutPageTemplateStructure layoutPageTemplateStructure =
			_layoutPageTemplateStructureLocalService.
				fetchLayoutPageTemplateStructure(
					layout.getGroupId(), layout.getPlid());

		LayoutPageTemplateStructureRel layoutPageTemplateStructureRel =
			_layoutPageTemplateStructureRelLocalService.
				createLayoutPageTemplateStructureRel(
					_counterLocalService.increment(
						LayoutPageTemplateStructureRel.class.getName()));

		layoutPageTemplateStructureRel.setGroupId(layout.getGroupId());
		layoutPageTemplateStructureRel.setCompanyId(layout.getCompanyId());
		layoutPageTemplateStructureRel.setUserId(TestPropsValues.getUserId());
		layoutPageTemplateStructureRel.setCreateDate(new Date());
		layoutPageTemplateStructureRel.setModifiedDate(new Date());
		layoutPageTemplateStructureRel.setLayoutPageTemplateStructureId(
			layoutPageTemplateStructure.getLayoutPageTemplateStructureId());
		layoutPageTemplateStructureRel.setSegmentsExperienceId(
			segmentsExperienceId);
		layoutPageTemplateStructureRel.setData(StringPool.BLANK);

		_layoutPageTemplateStructureRelLocalService.
			addLayoutPageTemplateStructureRel(layoutPageTemplateStructureRel);
	}

	private void _deleteDefaultSegmentsExperience(Layout layout)
		throws Exception {

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperience(
				layout.getPlid());

		_resourceLocalService.deleteResource(
			segmentsExperience.getCompanyId(),
			SegmentsExperience.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			segmentsExperience.getSegmentsExperienceId());

		DB db = DBManagerUtil.getDB();

		db.runSQL(
			"delete from SegmentsExperience where segmentsExperienceId = " +
				segmentsExperience.getSegmentsExperienceId());

		_entityCache.clearCache();
		_multiVMPool.clear();
	}

	private long _getDefaultSegmentsExperienceCount(long plid)
		throws Exception {

		try (Connection connection = DataAccess.getConnection()) {
			try (PreparedStatement preparedStatement =
					connection.prepareStatement(
						"select count(*) as count from SegmentsExperience " +
							"where plid = ? and segmentsExperienceKey = ?")) {

				preparedStatement.setLong(1, plid);
				preparedStatement.setString(
					2, SegmentsExperienceConstants.KEY_DEFAULT);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) {
						return resultSet.getLong("count");
					}
				}
			}
		}

		return 0;
	}

	private long _getDefaultSegmentsExperienceId(long plid) throws Exception {
		try (Connection connection = DataAccess.getConnection()) {
			try (PreparedStatement preparedStatement =
					connection.prepareStatement(
						"select segmentsExperienceId from SegmentsExperience " +
							"where plid = ? and segmentsExperienceKey = ?")) {

				preparedStatement.setLong(1, plid);
				preparedStatement.setString(
					2, SegmentsExperienceConstants.KEY_DEFAULT);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) {
						return resultSet.getLong("segmentsExperienceId");
					}
				}
			}
		}

		return 0;
	}

	private long _getFragmentEntryLinkSegmentsExperienceId(
			long fragmentEntryLinkId)
		throws Exception {

		try (Connection connection = DataAccess.getConnection()) {
			try (PreparedStatement preparedStatement =
					connection.prepareStatement(
						"select segmentsExperienceId from FragmentEntryLink " +
							"where fragmentEntryLinkId = ?")) {

				preparedStatement.setLong(1, fragmentEntryLinkId);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) {
						return resultSet.getLong("segmentsExperienceId");
					}
				}
			}
		}

		return 0;
	}

	private long _getLayoutPageTemplateStructureRelCount(
			long plid, long segmentsExperienceId)
		throws Exception {

		try (Connection connection = DataAccess.getConnection()) {
			try (PreparedStatement preparedStatement =
					connection.prepareStatement(
						StringBundler.concat(
							"select count(*) as count from ",
							"LayoutPageTemplateStructureRel where ",
							"segmentsExperienceId = ? and ",
							"layoutPageTemplateStructureId in (select ",
							"distinct layoutPageTemplateStructureId from ",
							"LayoutPageTemplateStructure where plid = ?)"))) {

				preparedStatement.setLong(1, segmentsExperienceId);
				preparedStatement.setLong(2, plid);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) {
						return resultSet.getLong("count");
					}
				}
			}
		}

		return 0;
	}

	private long _getLayoutPageTemplateStructureRelSegmentsExperienceId(
			long ctCollectionId, long plid)
		throws Exception {

		try (Connection connection = DataAccess.getConnection()) {
			try (PreparedStatement preparedStatement =
					connection.prepareStatement(
						StringBundler.concat(
							"select segmentsExperienceId from ",
							"LayoutPageTemplateStructureRel where ",
							"ctCollectionId = ? and ",
							"layoutPageTemplateStructureId in (select ",
							"distinct layoutPageTemplateStructureId from ",
							"LayoutPageTemplateStructure where plid = ?)"))) {

				preparedStatement.setLong(1, ctCollectionId);
				preparedStatement.setLong(2, plid);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) {
						return resultSet.getLong("segmentsExperienceId");
					}
				}
			}
		}

		return 0;
	}

	private void _runUpgrade() throws Exception {
		UpgradeProcess upgradeProcess = UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator, _CLASS_NAME);

		upgradeProcess.upgrade();
	}

	private void _updateLayoutInCTCollection(
			CTCollection ctCollection, Layout layout)
		throws Exception {

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			Layout ctCollectionLayout = _layoutLocalService.getLayout(
				layout.getPlid());

			ctCollectionLayout.setModifiedDate(new Date());

			_layoutLocalService.updateLayout(ctCollectionLayout);
		}
	}

	private void _updateLayoutPageTemplateStructureRel(
			long plid, long segmentsExperienceId)
		throws Exception {

		DB db = DBManagerUtil.getDB();

		db.runSQL(
			StringBundler.concat(
				"update LayoutPageTemplateStructureRel set ",
				"segmentsExperienceId = ", segmentsExperienceId,
				" where layoutPageTemplateStructureId in (select distinct ",
				"layoutPageTemplateStructureId from ",
				"LayoutPageTemplateStructure where plid = ", plid, ")"));
	}

	private static final String _CLASS_NAME =
		"com.liferay.layout.page.template.internal.upgrade.v6_3_0." +
			"DefaultSegmentsExperienceUpgradeProcess";

	@Inject
	private CounterLocalService _counterLocalService;

	@Inject
	private CTCollectionLocalService _ctCollectionLocalService;

	@Inject
	private EntityCache _entityCache;

	@Inject
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject
	private LayoutPageTemplateStructureLocalService
		_layoutPageTemplateStructureLocalService;

	@Inject
	private LayoutPageTemplateStructureRelLocalService
		_layoutPageTemplateStructureRelLocalService;

	@Inject
	private LayoutUtilityPageEntryLocalService
		_layoutUtilityPageEntryLocalService;

	@Inject
	private MultiVMPool _multiVMPool;

	@Inject
	private ResourceLocalService _resourceLocalService;

	@Inject
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

	@Inject(
		filter = "(&(component.name=com.liferay.layout.page.template.internal.upgrade.registry.LayoutPageTemplateServiceUpgradeStepRegistrator))"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}