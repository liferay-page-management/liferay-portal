/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.internal.upgrade.v6_2_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructure;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructureRel;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalService;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureRelLocalService;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.cache.MultiVMPool;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.version.Version;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;
import com.liferay.segments.constants.SegmentsExperienceConstants;
import com.liferay.segments.service.SegmentsExperienceLocalService;

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
public class LayoutPageTemplateStructureRelUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@Test
	public void testUpgradeDeletesOrphanedLayoutPageTemplateStructureRel()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		Layout draftLayout = layout.fetchDraftLayout();

		LayoutPageTemplateStructure layoutPageTemplateStructure =
			_layoutPageTemplateStructureLocalService.
				fetchLayoutPageTemplateStructure(
					draftLayout.getGroupId(), draftLayout.getPlid());

		long defaultSegmentsExperienceId =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperienceId(
				draftLayout.getPlid());

		_layoutPageTemplateStructureRelLocalService.
			addLayoutPageTemplateStructureRel(
				TestPropsValues.getUserId(), _group.getGroupId(),
				layoutPageTemplateStructure.getLayoutPageTemplateStructureId(),
				SegmentsExperienceConstants.ID_DEFAULT, null,
				ServiceContextTestUtil.getServiceContext(
					_group.getGroupId(), TestPropsValues.getUserId()));

		Assert.assertNotNull(
			_fetchLayoutPageTemplateStructureRel(
				layoutPageTemplateStructure,
				SegmentsExperienceConstants.ID_DEFAULT));

		_runUpgrade();

		Assert.assertNull(
			_fetchLayoutPageTemplateStructureRel(
				layoutPageTemplateStructure,
				SegmentsExperienceConstants.ID_DEFAULT));

		Assert.assertNotNull(
			_fetchLayoutPageTemplateStructureRel(
				layoutPageTemplateStructure, defaultSegmentsExperienceId));
	}

	private LayoutPageTemplateStructureRel _fetchLayoutPageTemplateStructureRel(
		LayoutPageTemplateStructure layoutPageTemplateStructure,
		long segmentsExperienceId) {

		return _layoutPageTemplateStructureRelLocalService.
			fetchLayoutPageTemplateStructureRel(
				layoutPageTemplateStructure.getLayoutPageTemplateStructureId(),
				segmentsExperienceId);
	}

	private void _runUpgrade() throws Exception {
		UpgradeProcess[] upgradeProcesses = UpgradeTestUtil.getUpgradeSteps(
			_upgradeStepRegistrator, new Version(6, 2, 0));

		upgradeProcesses[0].upgrade();

		_entityCache.clearCache();

		_multiVMPool.clear();
	}

	@Inject
	private EntityCache _entityCache;

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

	@Inject
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

	@Inject(
		filter = "(&(component.name=com.liferay.layout.page.template.internal.upgrade.registry.LayoutPageTemplateServiceUpgradeStepRegistrator))"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}