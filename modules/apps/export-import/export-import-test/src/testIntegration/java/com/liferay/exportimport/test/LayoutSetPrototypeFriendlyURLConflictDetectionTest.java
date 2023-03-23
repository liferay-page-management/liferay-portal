/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.exportimport.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.exportimport.kernel.staging.MergeLayoutPrototypesThreadLocal;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutSetPrototype;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.sites.kernel.util.SitesUtil;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Vendel Toreki
 */
@RunWith(Arquillian.class)
public class LayoutSetPrototypeFriendlyURLConflictDetectionTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		ServiceContextThreadLocal.pushServiceContext(
			ServiceContextTestUtil.getServiceContext());

		UserTestUtil.setUser(TestPropsValues.getUser());

		_group = GroupTestUtil.addGroup();

		_layoutSetPrototype = LayoutTestUtil.addLayoutSetPrototype(
			RandomTestUtil.randomString());

		_layoutSetPrototypeGroup = _layoutSetPrototype.getGroup();

		setLinkEnabled(true);
	}

	@Test
	public void testLayoutSetPrototypeLayoutFriendlyURLConflictDetectionBeforeChange()
		throws Exception {

		setLinkEnabled(true);

		Layout siteLayout = LayoutTestUtil.addTypePortletLayout(
			_group.getGroupId(), "test", false);

		Layout layoutSetPrototypeLayout = LayoutTestUtil.addTypePortletLayout(
			_layoutSetPrototypeGroup.getGroupId(), "testNoConflict", true);

		List<Layout> conflicts =
			SitesUtil.getLayoutSetPrototypeFriendlyURLConflictSitesLayouts(
				layoutSetPrototypeLayout, "/test");

		Assert.assertEquals(conflicts.toString(), 1, conflicts.size());

		Layout conflictLayout = conflicts.get(0);

		Assert.assertEquals(conflictLayout.getPlid(), siteLayout.getPlid());
	}

	@Test
	public void testLayoutSetPrototypeLayoutFriendlyURLConflictDetectionBeforePropagate()
		throws Exception {

		setLinkEnabled(true);

		Layout siteLayout = LayoutTestUtil.addTypePortletLayout(
			_group.getGroupId(), "test", false);

		Layout layoutSetPrototypeLayout = LayoutTestUtil.addTypePortletLayout(
			_layoutSetPrototypeGroup.getGroupId(), "test", true);

		List<Layout> conflicts =
			SitesUtil.getLayoutSetPrototypeFriendlyURLConflictSitesLayouts(
				layoutSetPrototypeLayout);

		Assert.assertEquals(conflicts.toString(), 1, conflicts.size());

		Layout conflictLayout = conflicts.get(0);

		Assert.assertEquals(conflictLayout.getPlid(), siteLayout.getPlid());
	}

	@Test
	public void testSiteLayoutFriendlyURLConflictDetectionBeforeChange()
		throws Exception {

		Layout siteLayout = LayoutTestUtil.addTypePortletLayout(
			_group.getGroupId(), "testNoConflict", false);

		Layout layoutSetPrototypeLayout = LayoutTestUtil.addTypePortletLayout(
			_layoutSetPrototypeGroup.getGroupId(), "test", true);

		Layout conflictLayout =
			SitesUtil.getLayoutSetPrototypeFriendlyURLConflictPrototypeLayout(
				siteLayout, "/test");

		Assert.assertNotNull(conflictLayout);

		Assert.assertEquals(
			conflictLayout.getPlid(), layoutSetPrototypeLayout.getPlid());
	}

	@Test
	public void testSiteLayoutFriendlyURLConflictDetectionBeforePropagate()
		throws Exception {

		Layout siteLayout = LayoutTestUtil.addTypePortletLayout(
			_group.getGroupId(), "test", false);

		Layout layoutSetPrototypeLayout = LayoutTestUtil.addTypePortletLayout(
			_layoutSetPrototypeGroup.getGroupId(), "test", true);

		Layout conflictLayout =
			SitesUtil.getLayoutSetPrototypeFriendlyURLConflictPrototypeLayout(
				siteLayout);

		Assert.assertNotNull(conflictLayout);

		Assert.assertEquals(
			conflictLayout.getPlid(), layoutSetPrototypeLayout.getPlid());
	}

	protected void setLinkEnabled(boolean linkEnabled) throws Exception {
		MergeLayoutPrototypesThreadLocal.clearMergeComplete();

		SitesUtil.updateLayoutSetPrototypesLinks(
			_group, _layoutSetPrototype.getLayoutSetPrototypeId(), 0,
			linkEnabled, false);

		Thread.sleep(2000);
	}

	@DeleteAfterTestRun
	private Group _group;

	@DeleteAfterTestRun
	private LayoutSetPrototype _layoutSetPrototype;

	@DeleteAfterTestRun
	private Group _layoutSetPrototypeGroup;

}