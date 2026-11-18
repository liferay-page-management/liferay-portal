/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.segments.internal.upgrade.v4_2_2.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.change.tracking.constants.CTConstants;
import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.model.CTEntry;
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
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.cache.MultiVMPool;
import com.liferay.portal.kernel.change.tracking.CTCollectionThreadLocal;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upgrade.UpgradeException;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.kernel.version.Version;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.model.SegmentsExperience;
import com.liferay.segments.service.SegmentsExperienceLocalService;
import com.liferay.segments.test.util.SegmentsTestUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

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

		_fragmentEntryLink1 = _addFragmentEntryLink(
			draftLayout, _draftSegmentsExperienceId1);

		_layoutPageTemplateStructureRel1 = _fetchLayoutPageTemplateStructureRel(
			draftLayout.getPlid(), _draftSegmentsExperienceId1);

		_publishedSegmentsExperienceId1 =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperienceId(
				_layout.getPlid());

		SegmentsExperience draftSegmentsExperience =
			SegmentsTestUtil.addSegmentsExperience(
				_group.getGroupId(), draftLayout.getPlid());

		_draftSegmentsExperienceId2 =
			draftSegmentsExperience.getSegmentsExperienceId();

		_fragmentEntryLink2 = _addFragmentEntryLink(
			draftLayout, _draftSegmentsExperienceId2);

		_layoutPageTemplateStructureRel2 = _fetchLayoutPageTemplateStructureRel(
			draftLayout.getPlid(), _draftSegmentsExperienceId2);

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
		FragmentEntryLink fragmentEntryLink = _addFragmentEntryLink(
			_layout, _publishedSegmentsExperienceId1);

		LayoutPageTemplateStructureRel layoutPageTemplateStructureRel =
			_fetchLayoutPageTemplateStructureRel(
				_layout.getPlid(), _publishedSegmentsExperienceId1);

		_runUpgrade();

		_assertSegmentsExperienceId(
			_draftSegmentsExperienceId1, _fragmentEntryLink1,
			_layoutPageTemplateStructureRel1);

		_updateSegmentsExperienceIds();

		_runUpgrade();

		_assertDraftSegmentsExperienceIds();
		_assertSegmentsExperienceId(
			_publishedSegmentsExperienceId1, fragmentEntryLink,
			layoutPageTemplateStructureRel);
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeFrom3_1_1WithCTCollectionAddedLayout()
		throws Exception {

		_db.alterTableAddColumn(
			_connection, "SegmentsExperience", "segmentsEntryId", "LONG");

		try {
			CTCollection ctCollection = _addCTCollection();

			Layout layout = _addLayoutWithoutDraftSegmentsExperience(
				ctCollection);

			_runUpgrade(new Version(3, 2, 0));
			_runUpgrade();

			_publishCTCollection(ctCollection);

			_assertDraftLayoutSegmentsExperience(layout);
		}
		finally {
			_db.alterTableDropColumn(
				_connection, "SegmentsExperience", "segmentsEntryId");
		}
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeFrom3_1_1WithCTCollectionDeletedSegmentsExperience()
		throws Exception {

		_db.alterTableAddColumn(
			_connection, "SegmentsExperience", "segmentsEntryId", "LONG");

		try {
			CTCollection ctCollection = _addCTCollection();

			try (SafeCloseable safeCloseable =
					CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
						ctCollection.getCtCollectionId())) {

				_segmentsExperienceLocalService.deleteSegmentsExperience(
					_publishedSegmentsExperienceId2);
			}

			_runUpgrade(new Version(3, 2, 0));
			_runUpgrade();

			_publishCTCollection(ctCollection);

			Assert.assertNull(
				_fragmentEntryLinkLocalService.fetchFragmentEntryLink(
					_fragmentEntryLink2.getFragmentEntryLinkId()));
			Assert.assertNull(
				_segmentsExperienceLocalService.fetchSegmentsExperience(
					_draftSegmentsExperienceId2));
		}
		finally {
			_db.alterTableDropColumn(
				_connection, "SegmentsExperience", "segmentsEntryId");
		}
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeFrom3_1_1WithCTCollectionDeletedSegmentsExperiencePriorities()
		throws Exception {

		_db.alterTableAddColumn(
			_connection, "SegmentsExperience", "segmentsEntryId", "LONG");

		try {
			SegmentsExperience publishedSegmentsExperience =
				SegmentsTestUtil.addSegmentsExperience(
					_group.getGroupId(), _layout.getPlid());

			CTCollection ctCollection = _addCTCollection();

			try (SafeCloseable safeCloseable =
					CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
						ctCollection.getCtCollectionId())) {

				_segmentsExperienceLocalService.deleteSegmentsExperience(
					_publishedSegmentsExperienceId2);
			}

			_runUpgrade(new Version(3, 2, 0));
			_runUpgrade();

			_publishCTCollection(ctCollection);

			Layout draftLayout = _layout.fetchDraftLayout();

			SegmentsExperience draftSegmentsExperience =
				_segmentsExperienceLocalService.fetchSegmentsExperience(
					_group.getGroupId(),
					publishedSegmentsExperience.getSegmentsExperienceKey(),
					draftLayout.getPlid());

			Assert.assertEquals(-1, draftSegmentsExperience.getPriority());

			publishedSegmentsExperience =
				_segmentsExperienceLocalService.getSegmentsExperience(
					publishedSegmentsExperience.getSegmentsExperienceId());

			Assert.assertEquals(-1, publishedSegmentsExperience.getPriority());
		}
		finally {
			_db.alterTableDropColumn(
				_connection, "SegmentsExperience", "segmentsEntryId");
		}
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeFrom3_1_1WithCTCollectionModifiedSegmentsExperience()
		throws Exception {

		_db.alterTableAddColumn(
			_connection, "SegmentsExperience", "segmentsEntryId", "LONG");

		try {
			CTCollection ctCollection = _addCTCollection();
			String name = RandomTestUtil.randomString();

			try (SafeCloseable safeCloseable =
					CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
						ctCollection.getCtCollectionId())) {

				_updateSegmentsExperienceIds();

				_updateSegmentsExperienceName(
					name, _publishedSegmentsExperienceId2);
			}

			_runUpgrade(new Version(3, 2, 0));
			_runUpgrade();

			_publishCTCollection(ctCollection);

			_assertNames(name);
		}
		finally {
			_db.alterTableDropColumn(
				_connection, "SegmentsExperience", "segmentsEntryId");
		}
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeFrom3_1_1WithCTCollectionSegmentsExperiences()
		throws Exception {

		_db.alterTableAddColumn(
			_connection, "SegmentsExperience", "segmentsEntryId", "LONG");

		try {
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

			_deleteCTEntry(
				ctCollection,
				_addSegmentsExperience(
					ctCollection, segmentsExperience.getPlid(),
					segmentsExperience));

			_runUpgrade(new Version(3, 2, 0));
			_runUpgrade();

			_publishCTCollection(ctCollection);

			_assertDraftSegmentsExperienceIds();
		}
		finally {
			_db.alterTableDropColumn(
				_connection, "SegmentsExperience", "segmentsEntryId");
		}
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
	public void testUpgradeWithCTCollectionAddedLayout() throws Exception {
		CTCollection ctCollection = _addCTCollection();

		Layout layout = _addLayoutWithoutDraftSegmentsExperience(ctCollection);

		_runUpgrade();
		_runUpgrade();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_assertDraftLayoutSegmentsExperience(layout);
		}

		_publishCTCollection(ctCollection);

		_assertDraftLayoutSegmentsExperience(layout);
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithCTCollectionAddedLayoutAfterFailedInsert()
		throws Exception {

		CTCollection ctCollection = _addCTCollection();

		SegmentsExperience segmentsExperience = null;

		Layout layout = _addLayoutWithoutDraftSegmentsExperience(ctCollection);

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			segmentsExperience =
				_segmentsExperienceLocalService.getSegmentsExperience(
					_segmentsExperienceLocalService.
						fetchDefaultSegmentsExperienceId(layout.getPlid()));

			segmentsExperience = (SegmentsExperience)segmentsExperience.clone();

			segmentsExperience.setSegmentsExperienceKey(
				RandomTestUtil.randomString());

			Layout draftLayout = layout.fetchDraftLayout();

			segmentsExperience = _addSegmentsExperience(
				ctCollection, draftLayout.getPlid(), segmentsExperience);
		}

		Assert.assertThrows(UpgradeException.class, this::_runUpgrade);

		_assertSegmentsExperienceCTEntries(ctCollection);

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_segmentsExperienceLocalService.deleteSegmentsExperience(
				segmentsExperience);
		}

		_runUpgrade();

		_publishCTCollection(ctCollection);

		_assertDraftLayoutSegmentsExperience(layout);
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithCTCollectionAddedLayoutsAfterFailedUpgrade()
		throws Exception {

		CTCollection ctCollection = _addCTCollection();

		Layout layout1 = _addLayoutWithoutDraftSegmentsExperience(ctCollection);
		Layout layout2 = _addLayoutWithoutDraftSegmentsExperience(ctCollection);

		_assertUpgradeException(
			_createCTEntryModelListener(CTConstants.CT_CHANGE_TYPE_ADDITION));

		_assertSegmentsExperienceCTEntries(ctCollection);

		_runUpgrade();

		_publishCTCollection(ctCollection);

		_assertDraftLayoutSegmentsExperience(layout1);
		_assertDraftLayoutSegmentsExperience(layout2);
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithCTCollectionDeletedSegmentsExperience()
		throws Exception {

		_resourcePermissionLocalService.deleteResourcePermissions(
			_group.getCompanyId(), SegmentsExperience.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL, _draftSegmentsExperienceId2);

		CTCollection ctCollection = _addCTCollection();

		FragmentEntryLink fragmentEntryLink = null;

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			fragmentEntryLink = _addFragmentEntryLink(
				_layout.fetchDraftLayout(), _draftSegmentsExperienceId2);

			_fragmentEntryLink2.setCss(RandomTestUtil.randomString());

			_fragmentEntryLink2 =
				_fragmentEntryLinkLocalService.updateFragmentEntryLink(
					_fragmentEntryLink2);

			_segmentsExperienceLocalService.deleteSegmentsExperience(
				_publishedSegmentsExperienceId2);

			_layoutPageTemplateStructureRelLocalService.
				deleteLayoutPageTemplateStructureRel(
					_layoutPageTemplateStructureRel2);
		}

		_runUpgrade();
		_runUpgrade();

		_publishCTCollection(ctCollection);

		Assert.assertNull(
			_fragmentEntryLinkLocalService.fetchFragmentEntryLink(
				fragmentEntryLink.getFragmentEntryLinkId()));
		Assert.assertNull(
			_fragmentEntryLinkLocalService.fetchFragmentEntryLink(
				_fragmentEntryLink2.getFragmentEntryLinkId()));
		Assert.assertNull(
			_layoutPageTemplateStructureRelLocalService.
				fetchLayoutPageTemplateStructureRel(
					_layoutPageTemplateStructureRel2.
						getLayoutPageTemplateStructureRelId()));
		Assert.assertEquals(
			0,
			_resourcePermissionLocalService.getResourcePermissionsCount(
				_group.getCompanyId(), SegmentsExperience.class.getName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				String.valueOf(_draftSegmentsExperienceId2)));
		Assert.assertNull(
			_segmentsExperienceLocalService.fetchSegmentsExperience(
				_draftSegmentsExperienceId2));
		Assert.assertNull(
			_segmentsExperienceLocalService.fetchSegmentsExperience(
				_publishedSegmentsExperienceId2));
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithCTCollectionDeletedSegmentsExperienceAfterFailedUpgrade()
		throws Exception {

		FragmentEntryLink fragmentEntryLink = _addFragmentEntryLink(
			_layout.fetchDraftLayout(), _draftSegmentsExperienceId2);

		CTCollection ctCollection = _addCTCollection();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			fragmentEntryLink.setCss(RandomTestUtil.randomString());

			fragmentEntryLink =
				_fragmentEntryLinkLocalService.updateFragmentEntryLink(
					fragmentEntryLink);

			_fragmentEntryLink2.setCss(RandomTestUtil.randomString());

			_fragmentEntryLink2 =
				_fragmentEntryLinkLocalService.updateFragmentEntryLink(
					_fragmentEntryLink2);

			_segmentsExperienceLocalService.deleteSegmentsExperience(
				_publishedSegmentsExperienceId2);
		}

		_assertUpgradeException(
			new BaseModelListener<CTEntry>() {

				@Override
				public void onBeforeUpdate(
						CTEntry originalCTEntry, CTEntry ctEntry)
					throws ModelListenerException {

					if ((ctEntry.getChangeType() ==
							CTConstants.CT_CHANGE_TYPE_DELETION) &&
						(++_count == 2)) {

						throw new ModelListenerException();
					}
				}

				private int _count;

			});

		_runUpgrade();

		_publishCTCollection(ctCollection);

		Assert.assertNull(
			_fragmentEntryLinkLocalService.fetchFragmentEntryLink(
				_fragmentEntryLink2.getFragmentEntryLinkId()));
		Assert.assertNull(
			_fragmentEntryLinkLocalService.fetchFragmentEntryLink(
				fragmentEntryLink.getFragmentEntryLinkId()));
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
	public void testUpgradeWithCTCollectionModifiedSegmentsExperience()
		throws Exception {

		CTCollection ctCollection = _addCTCollection();
		String name = RandomTestUtil.randomString();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_updateSegmentsExperienceIds();

			_updateSegmentsExperienceName(
				name, _publishedSegmentsExperienceId2);
		}

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		segmentsExperience.setName(name, LocaleUtil.getSiteDefault());

		_deleteCTEntry(
			ctCollection,
			_addSegmentsExperience(
				ctCollection, segmentsExperience.getPlid(),
				segmentsExperience));

		_runUpgrade();
		_runUpgrade();

		_publishCTCollection(ctCollection);

		_assertNames(name);
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithCTCollectionModifiedSegmentsExperienceWithoutDraftSegmentsExperience()
		throws Exception {

		CTCollection ctCollection1 = _addCTCollection();
		String name1 = RandomTestUtil.randomString();

		_updateSegmentsExperienceName(
			ctCollection1, name1, _publishedSegmentsExperienceId2);

		CTCollection ctCollection2 = _addCTCollection();
		String name2 = RandomTestUtil.randomString();

		_updateSegmentsExperienceName(
			ctCollection2, name2, _publishedSegmentsExperienceId2);

		_updateSegmentsExperienceIds();

		_resourcePermissionLocalService.deleteResourcePermissions(
			_group.getCompanyId(), SegmentsExperience.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL, _draftSegmentsExperienceId2);

		_executeUpdate(
			"delete from SegmentsExperience where segmentsExperienceId = ?",
			_draftSegmentsExperienceId2);

		_entityCache.clearCache();
		_multiVMPool.clear();

		_runUpgrade();

		_assertDraftSegmentsExperienceName(ctCollection1, name1);
		_assertDraftSegmentsExperienceName(ctCollection2, name2);

		_publishCTCollection(ctCollection1);

		SegmentsExperience publishedSegmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_publishedSegmentsExperienceId2);

		SegmentsExperience draftSegmentsExperience =
			_fetchDraftSegmentsExperience(publishedSegmentsExperience);

		_draftSegmentsExperienceId2 =
			draftSegmentsExperience.getSegmentsExperienceId();

		_assertNames(name1);

		_runUpgrade();

		_assertNames(name1);
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithCTCollectionModifiedSegmentsExperiencesAfterFailedUpgrade()
		throws Exception {

		_updateSegmentsExperienceIds();

		CTCollection ctCollection = _addCTCollection();
		String name = RandomTestUtil.randomString();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_updateSegmentsExperienceName(
				name, _publishedSegmentsExperienceId1);
			_updateSegmentsExperienceName(
				name, _publishedSegmentsExperienceId2);
		}

		_assertUpgradeException(
			_createCTEntryModelListener(
				CTConstants.CT_CHANGE_TYPE_MODIFICATION));

		_assertSegmentsExperienceCTEntries(ctCollection);

		_runUpgrade();

		_publishCTCollection(ctCollection);

		_assertNames(name);
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

		_deleteCTEntry(
			ctCollection,
			_addSegmentsExperience(
				ctCollection, segmentsExperience.getPlid(),
				segmentsExperience));

		_runUpgrade();
		_runUpgrade();

		_assertDraftSegmentsExperienceIds(ctCollection);

		String name = RandomTestUtil.randomString();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			Assert.assertEquals(
				segmentsExperience,
				_segmentsExperienceLocalService.fetchSegmentsExperience(
					_group.getGroupId(),
					segmentsExperience.getSegmentsExperienceKey(),
					segmentsExperience.getPlid()));

			segmentsExperience.setName(name, LocaleUtil.getSiteDefault());

			segmentsExperience =
				_segmentsExperienceLocalService.updateSegmentsExperience(
					segmentsExperience);
		}

		_publishCTCollection(ctCollection);

		_assertDraftSegmentsExperienceIds();

		segmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		Assert.assertEquals(
			name, segmentsExperience.getName(LocaleUtil.getSiteDefault()));
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

		_publishCTCollection(ctCollection);

		_assertSegmentsExperience(segmentsExperience);
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithPublishedCTCollectionBeforeUpgradeAddedLayout()
		throws Exception {

		CTCollection ctCollection = _addCTCollection();

		Layout layout = _addLayoutWithoutDraftSegmentsExperience(ctCollection);

		_publishCTCollection(ctCollection);

		_runUpgrade();
		_runUpgrade();

		_assertDraftLayoutSegmentsExperience(layout);
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithPublishedCTCollectionBeforeUpgradeAddedSegmentsExperience()
		throws Exception {

		CTCollection ctCollection = _addCTCollection();

		SegmentsExperience segmentsExperience = _addSegmentsExperience(
			ctCollection);

		SegmentsExperience publishedSegmentsExperience = _addSegmentsExperience(
			ctCollection, _layout.getPlid(), segmentsExperience);

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			for (FragmentEntryLink fragmentEntryLink :
					_fragmentEntryLinkLocalService.
						getFragmentEntryLinksBySegmentsExperienceId(
							_group.getGroupId(),
							segmentsExperience.getSegmentsExperienceId(),
							segmentsExperience.getPlid())) {

				_updateSegmentsExperienceId(
					fragmentEntryLink,
					publishedSegmentsExperience.getSegmentsExperienceId());
			}
		}

		_publishCTCollection(ctCollection);

		_runUpgrade();

		_assertSegmentsExperience(segmentsExperience);

		_runUpgrade();

		_assertSegmentsExperience(segmentsExperience);
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithPublishedCTCollectionBeforeUpgradeAddedSegmentsExperienceWithSamePriority()
		throws Exception {

		CTCollection ctCollection = _addCTCollection();

		SegmentsExperience segmentsExperience = _addSegmentsExperience(
			ctCollection);

		_addSegmentsExperience(
			ctCollection, _layout.getPlid(), segmentsExperience);

		_publishCTCollection(ctCollection);

		Layout draftLayout = _layout.fetchDraftLayout();

		SegmentsExperience draftSegmentsExperience =
			SegmentsTestUtil.addSegmentsExperience(
				_group.getGroupId(), draftLayout.getPlid());

		_runUpgrade();
		_runUpgrade();

		_assertSegmentsExperience(segmentsExperience);

		segmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				segmentsExperience.getSegmentsExperienceId());

		Assert.assertEquals(-3, segmentsExperience.getPriority());

		draftSegmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				draftSegmentsExperience.getSegmentsExperienceId());

		Assert.assertEquals(-2, draftSegmentsExperience.getPriority());
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithPublishedCTCollectionBeforeUpgradeAppendedSegmentsExperienceWithSamePriority()
		throws Exception {

		CTCollection ctCollection = _addCTCollection();
		Layout draftLayout = _layout.fetchDraftLayout();

		SegmentsExperience segmentsExperience = null;

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			segmentsExperience = _appendSegmentsExperience(
				draftLayout.getPlid());

			_addFragmentEntryLink(
				draftLayout, segmentsExperience.getSegmentsExperienceId());
		}

		_deleteCTEntry(ctCollection, segmentsExperience);

		_addSegmentsExperience(
			ctCollection, _layout.getPlid(), segmentsExperience);

		_publishCTCollection(ctCollection);

		SegmentsExperience draftSegmentsExperience = _appendSegmentsExperience(
			draftLayout.getPlid());

		_runUpgrade();
		_runUpgrade();

		_assertSegmentsExperience(segmentsExperience);

		segmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				segmentsExperience.getSegmentsExperienceId());

		Assert.assertEquals(2, segmentsExperience.getPriority());

		draftSegmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				draftSegmentsExperience.getSegmentsExperienceId());

		Assert.assertEquals(1, draftSegmentsExperience.getPriority());
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

		_publishCTCollection(ctCollection);

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
		String name = RandomTestUtil.randomString();

		_updateSegmentsExperienceName(
			ctCollection, name, _publishedSegmentsExperienceId2);

		SegmentsExperience draftSegmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		draftSegmentsExperience.setName(name, LocaleUtil.getSiteDefault());

		SegmentsExperience segmentsExperience = _addSegmentsExperience(
			ctCollection, draftSegmentsExperience.getPlid(),
			draftSegmentsExperience);

		_deleteCTEntry(ctCollection, segmentsExperience);

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_fragmentEntryLink2 = _updateSegmentsExperienceId(
				_fragmentEntryLink2,
				segmentsExperience.getSegmentsExperienceId());
			_layoutPageTemplateStructureRel2 = _updateSegmentsExperienceId(
				_layoutPageTemplateStructureRel2,
				segmentsExperience.getSegmentsExperienceId());
		}

		_publishCTCollection(ctCollection);

		_assertSegmentsExperienceId(
			segmentsExperience.getSegmentsExperienceId(), _fragmentEntryLink2,
			_layoutPageTemplateStructureRel2);

		_runUpgrade();
		_runUpgrade();

		Assert.assertNull(
			_segmentsExperienceLocalService.fetchSegmentsExperience(
				segmentsExperience.getSegmentsExperienceId()));

		_assertSegmentsExperienceId(
			_draftSegmentsExperienceId2, _fragmentEntryLink2,
			_layoutPageTemplateStructureRel2);
		_assertNames(name);
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithPublishedCTCollectionBeforeUpgradeModifiedSegmentsExperiencePriority()
		throws Exception {

		CTCollection ctCollection = _addCTCollection();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			SegmentsExperience publishedSegmentsExperience =
				_segmentsExperienceLocalService.getSegmentsExperience(
					_publishedSegmentsExperienceId2);

			publishedSegmentsExperience.setPriority(-2);

			_segmentsExperienceLocalService.updateSegmentsExperience(
				publishedSegmentsExperience);
		}

		SegmentsExperience draftSegmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		draftSegmentsExperience.setPriority(-2);

		_deleteCTEntry(
			ctCollection,
			_addSegmentsExperience(
				ctCollection, draftSegmentsExperience.getPlid(),
				draftSegmentsExperience));

		_publishCTCollection(ctCollection);

		_runUpgrade();
		_runUpgrade();

		draftSegmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		Assert.assertEquals(-2, draftSegmentsExperience.getPriority());
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithPublishedCTCollectionBeforeUpgradeModifiedSegmentsExperienceWithSamePriority()
		throws Exception {

		CTCollection ctCollection = _addCTCollection();
		String name = RandomTestUtil.randomString();

		_updateSegmentsExperienceName(
			ctCollection, name, _publishedSegmentsExperienceId2);

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		segmentsExperience.setName(name, LocaleUtil.getSiteDefault());
		segmentsExperience.setPriority(0);

		_deleteCTEntry(
			ctCollection,
			_addSegmentsExperience(
				ctCollection, segmentsExperience.getPlid(),
				segmentsExperience));

		_publishCTCollection(ctCollection);

		_runUpgrade();
		_runUpgrade();

		segmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		Assert.assertEquals(
			name, segmentsExperience.getName(LocaleUtil.getSiteDefault()));
		Assert.assertEquals(-1, segmentsExperience.getPriority());
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithPublishedCTCollectionBeforeUpgradeSwappedSegmentsExperiencePriorities()
		throws Exception {

		CTCollection ctCollection = _addCTCollection();

		SegmentsExperience draftSegmentsExperience1 =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId1);
		SegmentsExperience draftSegmentsExperience2 =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		draftSegmentsExperience1.setPriority(-1);
		draftSegmentsExperience2.setPriority(0);

		_deleteCTEntry(
			ctCollection,
			_addSegmentsExperience(
				ctCollection, draftSegmentsExperience1.getPlid(),
				draftSegmentsExperience1));
		_deleteCTEntry(
			ctCollection,
			_addSegmentsExperience(
				ctCollection, draftSegmentsExperience2.getPlid(),
				draftSegmentsExperience2));

		_updateSegmentsExperienceName(
			ctCollection, RandomTestUtil.randomString(),
			_publishedSegmentsExperienceId1);

		_publishCTCollection(ctCollection);

		_runUpgrade();
		_runUpgrade();

		draftSegmentsExperience1 =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId1);

		Assert.assertEquals(-1, draftSegmentsExperience1.getPriority());

		draftSegmentsExperience2 =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		Assert.assertEquals(0, draftSegmentsExperience2.getPriority());
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithPublishedCTCollectionsBeforeUpgradeModifiedSegmentsExperience()
		throws Exception {

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		CTCollection ctCollection1 = _addCTCollection();

		_updateCTCollectionSegmentsExperiencePriority(
			ctCollection1, segmentsExperience,
			segmentsExperience.getPriority() - 1);

		CTCollection ctCollection2 = _addCTCollection();

		_updateCTCollectionSegmentsExperiencePriority(
			ctCollection2, segmentsExperience,
			segmentsExperience.getPriority());

		_publishCTCollection(ctCollection1);
		_publishCTCollection(ctCollection2);

		_segmentsExperienceLocalService.deleteSegmentsExperience(
			segmentsExperience);

		_runUpgrade();
		_runUpgrade();

		Assert.assertEquals(
			2,
			_segmentsExperienceLocalService.getSegmentsExperiencesCount(
				_group.getGroupId(), segmentsExperience.getPlid()));
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithPublishedCTCollectionsBeforeUpgradeModifiedSegmentsExperienceName()
		throws Exception {

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		CTCollection ctCollection1 = _addCTCollection();

		_updateCTCollectionSegmentsExperienceName(
			ctCollection1, segmentsExperience, RandomTestUtil.randomString());

		CTCollection ctCollection2 = _addCTCollection();
		String name2 = RandomTestUtil.randomString();

		_updateCTCollectionSegmentsExperienceName(
			ctCollection2, segmentsExperience, name2);

		_publishCTCollection(ctCollection1);
		_publishCTCollection(ctCollection2);

		_runUpgrade();

		segmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		Assert.assertEquals(
			name2, segmentsExperience.getName(LocaleUtil.getSiteDefault()));

		_runUpgrade();

		_assertNames(name2);
	}

	@Test
	@TestInfo("LPD-113912")
	public void testUpgradeWithScheduledCTCollectionAddedLayout()
		throws Exception {

		CTCollection ctCollection = _addCTCollection();

		Layout layout = _addLayoutWithoutDraftSegmentsExperience(ctCollection);

		ctCollection.setStatus(WorkflowConstants.STATUS_SCHEDULED);

		ctCollection = _ctCollectionLocalService.updateCTCollection(
			ctCollection);

		_runUpgrade();
		_runUpgrade();

		_ctProcessLocalService.addCTProcess(
			TestPropsValues.getUserId(), ctCollection.getCtCollectionId());

		_assertDraftLayoutSegmentsExperience(layout);
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
	@TestInfo("LPD-113912")
	public void testUpgradeWithUndoneCTCollection() throws Exception {
		_updateSegmentsExperienceIds();

		CTCollection ctCollection = _addCTCollection();

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_updateSegmentsExperienceName(
				RandomTestUtil.randomString(), _draftSegmentsExperienceId1);
			_updateSegmentsExperienceName(
				RandomTestUtil.randomString(), _publishedSegmentsExperienceId1);

			_fragmentEntryLink1.setCss(RandomTestUtil.randomString());

			_fragmentEntryLink1 =
				_fragmentEntryLinkLocalService.updateFragmentEntryLink(
					_fragmentEntryLink1);

			_layoutPageTemplateStructureRel1.setData(
				RandomTestUtil.randomString());

			_layoutPageTemplateStructureRel1 =
				_layoutPageTemplateStructureRelLocalService.
					updateLayoutPageTemplateStructureRel(
						_layoutPageTemplateStructureRel1);
		}

		_publishCTCollection(ctCollection);

		Assert.assertEquals(
			_publishedSegmentsExperienceId1,
			_getSegmentsExperienceId(
				ctCollection.getCtCollectionId(),
				_fragmentEntryLink1.getFragmentEntryLinkId(),
				"fragmentEntryLinkId", "FragmentEntryLink"));
		Assert.assertEquals(
			_publishedSegmentsExperienceId1,
			_getSegmentsExperienceId(
				ctCollection.getCtCollectionId(),
				_layoutPageTemplateStructureRel1.
					getLayoutPageTemplateStructureRelId(),
				"lPageTemplateStructureRelId",
				"LayoutPageTemplateStructureRel"));

		_runUpgrade();

		_assertSegmentsExperienceId(
			_draftSegmentsExperienceId1, _fragmentEntryLink1,
			_layoutPageTemplateStructureRel1);
		Assert.assertEquals(
			_draftSegmentsExperienceId1,
			_getSegmentsExperienceId(
				ctCollection.getCtCollectionId(),
				_fragmentEntryLink1.getFragmentEntryLinkId(),
				"fragmentEntryLinkId", "FragmentEntryLink"));
		Assert.assertEquals(
			_draftSegmentsExperienceId1,
			_getSegmentsExperienceId(
				ctCollection.getCtCollectionId(),
				_layoutPageTemplateStructureRel1.
					getLayoutPageTemplateStructureRelId(),
				"lPageTemplateStructureRelId",
				"LayoutPageTemplateStructureRel"));

		_publishCTCollection(
			_ctCollectionService.undoCTCollection(
				ctCollection.getCtCollectionId(), TestPropsValues.getUserId(),
				RandomTestUtil.randomString(), RandomTestUtil.randomString()));

		_entityCache.clearCache();

		_assertSegmentsExperienceId(
			_draftSegmentsExperienceId1, _fragmentEntryLink1,
			_layoutPageTemplateStructureRel1);
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

	private FragmentEntryLink _addFragmentEntryLink(
			Layout layout, long segmentsExperienceId)
		throws Exception {

		try {
			ServiceContextThreadLocal.pushServiceContext(
				ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

			return ContentLayoutTestUtil.addFragmentEntryLinkToLayout(
				JSONFactoryUtil.getNullJSON(), layout, segmentsExperienceId);
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
		}
	}

	private Layout _addLayoutWithoutDraftSegmentsExperience(
			CTCollection ctCollection)
		throws Exception {

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

			Layout draftLayout = layout.fetchDraftLayout();

			SegmentsExperience segmentsExperience =
				_segmentsExperienceLocalService.getSegmentsExperience(
					_segmentsExperienceLocalService.
						fetchDefaultSegmentsExperienceId(
							draftLayout.getPlid()));

			_addFragmentEntryLink(
				draftLayout, segmentsExperience.getSegmentsExperienceId());

			_deleteCTEntry(ctCollection, segmentsExperience);

			_resourcePermissionLocalService.deleteResourcePermissions(
				_group.getCompanyId(), SegmentsExperience.class.getName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				segmentsExperience.getSegmentsExperienceId());

			long newSegmentsExperienceId =
				_segmentsExperienceLocalService.
					fetchDefaultSegmentsExperienceId(layout.getPlid());
			long oldSegmentsExperienceId =
				segmentsExperience.getSegmentsExperienceId();

			_executeUpdate(
				"update FragmentEntryLink set segmentsExperienceId = ? where " +
					"segmentsExperienceId = ?",
				newSegmentsExperienceId, oldSegmentsExperienceId);
			_executeUpdate(
				"update LayoutPageTemplateStructureRel set " +
					"segmentsExperienceId = ? where segmentsExperienceId = ?",
				newSegmentsExperienceId, oldSegmentsExperienceId);

			_executeUpdate(
				"delete from SegmentsExperience where segmentsExperienceId = ?",
				oldSegmentsExperienceId);

			_entityCache.clearCache();
			_multiVMPool.clear();

			return layout;
		}
	}

	private SegmentsExperience _addSegmentsExperience(CTCollection ctCollection)
		throws Exception {

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			Layout draftLayout = _layout.fetchDraftLayout();

			SegmentsExperience segmentsExperience =
				SegmentsTestUtil.addSegmentsExperience(
					_group.getGroupId(), draftLayout.getPlid());

			_addFragmentEntryLink(
				draftLayout, segmentsExperience.getSegmentsExperienceId());

			_deleteCTEntry(ctCollection, segmentsExperience);

			return segmentsExperience;
		}
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

	private SegmentsExperience _appendSegmentsExperience(long plid)
		throws Exception {

		SegmentsEntry segmentsEntry = SegmentsTestUtil.addSegmentsEntry(
			_group.getGroupId());

		return _segmentsExperienceLocalService.appendSegmentsExperience(
			TestPropsValues.getUserId(), _group.getGroupId(),
			segmentsEntry.getExternalReferenceCode(), null, plid,
			RandomTestUtil.randomLocaleStringMap(), true,
			new UnicodeProperties(true),
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));
	}

	private void _assertDraftLayoutSegmentsExperience(Layout layout) {
		Layout draftLayout = layout.fetchDraftLayout();

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.fetchSegmentsExperience(
				_segmentsExperienceLocalService.
					fetchDefaultSegmentsExperienceId(draftLayout.getPlid()));

		_assertSegmentsExperience(segmentsExperience);

		Assert.assertNotNull(
			_fetchLayoutPageTemplateStructureRel(
				draftLayout.getPlid(),
				segmentsExperience.getSegmentsExperienceId()));

		Assert.assertEquals(
			3,
			_resourcePermissionLocalService.getResourcePermissionsCount(
				_group.getCompanyId(), SegmentsExperience.class.getName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				String.valueOf(segmentsExperience.getSegmentsExperienceId())));
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

	private void _assertDraftSegmentsExperienceName(
			CTCollection ctCollection, String name)
		throws Exception {

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			SegmentsExperience publishedSegmentsExperience =
				_segmentsExperienceLocalService.getSegmentsExperience(
					_publishedSegmentsExperienceId2);

			SegmentsExperience draftSegmentsExperience =
				_fetchDraftSegmentsExperience(publishedSegmentsExperience);

			Assert.assertEquals(
				name,
				draftSegmentsExperience.getName(LocaleUtil.getSiteDefault()));
		}
	}

	private void _assertNames(String name) throws Exception {
		SegmentsExperience draftSegmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_draftSegmentsExperienceId2);

		Assert.assertEquals(
			name, draftSegmentsExperience.getName(LocaleUtil.getSiteDefault()));

		ContentLayoutTestUtil.publishLayout(
			_layout.fetchDraftLayout(), _layout);

		_multiVMPool.clear();

		SegmentsExperience publishedSegmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				_publishedSegmentsExperienceId2);

		Assert.assertEquals(
			name,
			publishedSegmentsExperience.getName(LocaleUtil.getSiteDefault()));
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

	private void _assertSegmentsExperienceCTEntries(CTCollection ctCollection)
		throws Exception {

		try (PreparedStatement preparedStatement = _connection.prepareStatement(
				StringBundler.concat(
					"select count(*) as count from CTEntry where ",
					"ctCollectionId = ? and modelClassNameId = ? and ",
					"changeType != ? and not exists (select 1 from ",
					"SegmentsExperience where ",
					"SegmentsExperience.ctCollectionId = ",
					"CTEntry.ctCollectionId and ",
					"SegmentsExperience.segmentsExperienceId = ",
					"CTEntry.modelClassPK)"))) {

			preparedStatement.setLong(1, ctCollection.getCtCollectionId());
			preparedStatement.setLong(
				2, PortalUtil.getClassNameId(SegmentsExperience.class));
			preparedStatement.setInt(3, CTConstants.CT_CHANGE_TYPE_DELETION);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				Assert.assertTrue(resultSet.next());
				Assert.assertEquals(0, resultSet.getLong("count"));
			}
		}
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

	private void _assertUpgradeException(ModelListener<CTEntry> modelListener)
		throws Exception {

		Bundle bundle = FrameworkUtil.getBundle(
			SegmentsExperienceUpgradeProcessTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		ServiceRegistration<?> serviceRegistration =
			bundleContext.registerService(
				ModelListener.class, modelListener, null);

		try {
			Assert.assertThrows(UpgradeException.class, this::_runUpgrade);
		}
		finally {
			serviceRegistration.unregister();
		}
	}

	private ModelListener<CTEntry> _createCTEntryModelListener(int changeType) {
		return new BaseModelListener<CTEntry>() {

			@Override
			public void onBeforeCreate(CTEntry ctEntry)
				throws ModelListenerException {

				if ((ctEntry.getModelClassNameId() == PortalUtil.getClassNameId(
						SegmentsExperience.class)) &&
					(ctEntry.getChangeType() == changeType) &&
					(++_count == 2)) {

					throw new ModelListenerException();
				}
			}

			private int _count;

		};
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

	private void _executeUpdate(String sql, long... parameters)
		throws Exception {

		try (PreparedStatement preparedStatement = _connection.prepareStatement(
				sql)) {

			for (int i = 0; i < parameters.length; i++) {
				preparedStatement.setLong(i + 1, parameters[i]);
			}

			preparedStatement.executeUpdate();
		}
	}

	private SegmentsExperience _fetchDraftSegmentsExperience(
		SegmentsExperience publishedSegmentsExperience) {

		Layout draftLayout = _layout.fetchDraftLayout();

		return _segmentsExperienceLocalService.fetchSegmentsExperience(
			_group.getGroupId(),
			publishedSegmentsExperience.getSegmentsExperienceKey(),
			draftLayout.getPlid());
	}

	private LayoutPageTemplateStructureRel _fetchLayoutPageTemplateStructureRel(
		long plid, long segmentsExperienceId) {

		LayoutPageTemplateStructure layoutPageTemplateStructure =
			_layoutPageTemplateStructureLocalService.
				fetchLayoutPageTemplateStructure(_group.getGroupId(), plid);

		return _layoutPageTemplateStructureRelLocalService.
			fetchLayoutPageTemplateStructureRel(
				layoutPageTemplateStructure.getLayoutPageTemplateStructureId(),
				segmentsExperienceId);
	}

	private long _getSegmentsExperienceId(
			long ctCollectionId, long primaryKey, String primaryKeyName,
			String tableName)
		throws Exception {

		try (PreparedStatement preparedStatement = _connection.prepareStatement(
				StringBundler.concat(
					"select segmentsExperienceId from ", tableName,
					" where ctCollectionId = ? and ", primaryKeyName,
					" = ?"))) {

			preparedStatement.setLong(1, ctCollectionId);
			preparedStatement.setLong(2, primaryKey);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				Assert.assertTrue(resultSet.next());

				return resultSet.getLong("segmentsExperienceId");
			}
		}
	}

	private void _publishCTCollection(CTCollection ctCollection)
		throws Exception {

		_ctCollectionService.publishCTCollection(
			TestPropsValues.getUserId(), ctCollection.getCtCollectionId());

		_multiVMPool.clear();

		ctCollection = _ctCollectionLocalService.getCTCollection(
			ctCollection.getCtCollectionId());

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED, ctCollection.getStatus());
	}

	private void _runUpgrade() throws Exception {
		_runUpgrade(new Version(4, 2, 2));
	}

	private void _runUpgrade(Version version) throws Exception {
		UpgradeProcess[] upgradeProcesses = UpgradeTestUtil.getUpgradeSteps(
			_upgradeStepRegistrator, version);

		for (UpgradeProcess upgradeProcess : upgradeProcesses) {
			upgradeProcess.upgrade();
		}

		_entityCache.clearCache();
		_multiVMPool.clear();
	}

	private void _updateCTCollectionSegmentsExperienceName(
			CTCollection ctCollection,
			SegmentsExperience draftSegmentsExperience, String name)
		throws Exception {

		_updateSegmentsExperienceName(
			ctCollection, name, _publishedSegmentsExperienceId2);

		SegmentsExperience ctCollectionSegmentsExperience =
			(SegmentsExperience)draftSegmentsExperience.clone();

		ctCollectionSegmentsExperience.setName(
			name, LocaleUtil.getSiteDefault());

		_deleteCTEntry(
			ctCollection,
			_addSegmentsExperience(
				ctCollection, draftSegmentsExperience.getPlid(),
				ctCollectionSegmentsExperience));
	}

	private void _updateCTCollectionSegmentsExperiencePriority(
			CTCollection ctCollection,
			SegmentsExperience draftSegmentsExperience, int priority)
		throws Exception {

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

		SegmentsExperience ctCollectionSegmentsExperience =
			(SegmentsExperience)draftSegmentsExperience.clone();

		ctCollectionSegmentsExperience.setPriority(priority);

		_deleteCTEntry(
			ctCollection,
			_addSegmentsExperience(
				ctCollection, draftSegmentsExperience.getPlid(),
				ctCollectionSegmentsExperience));
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

	private void _updateSegmentsExperienceName(
			CTCollection ctCollection, String name, long segmentsExperienceId)
		throws Exception {

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_updateSegmentsExperienceName(name, segmentsExperienceId);
		}
	}

	private void _updateSegmentsExperienceName(
			String name, long segmentsExperienceId)
		throws Exception {

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.getSegmentsExperience(
				segmentsExperienceId);

		segmentsExperience.setName(name, LocaleUtil.getSiteDefault());

		_segmentsExperienceLocalService.updateSegmentsExperience(
			segmentsExperience);
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
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Inject
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

	@Inject(
		filter = "(&(component.name=com.liferay.segments.internal.upgrade.registry.SegmentsServiceUpgradeStepRegistrator))"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}