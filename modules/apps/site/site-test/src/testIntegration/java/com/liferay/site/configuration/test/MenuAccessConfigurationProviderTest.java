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

package com.liferay.site.configuration.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.site.configuration.MenuAccessConfigurationProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Mikel Lorza
 */
@RunWith(Arquillian.class)
public class MenuAccessConfigurationProviderTest {

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
	public void testAddRoleToMenuAccess() throws Exception {
		Role role = _roleLocalService.addRole(
			TestPropsValues.getUserId(), null, 0, StringUtil.randomString(),
			null, null, RoleConstants.TYPE_SITE, null,
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		_menuAccessConfigurationProvider.addRoleToMenuAccess(role);

		Assert.assertArrayEquals(
			new String[] {role.getName()},
			_menuAccessConfigurationProvider.getRolesCanSeeControlMenu(
				_group.getGroupId()));
	}

	@Test
	public void testDeleteRoleToMenuAccess() throws Exception {
		Role role1 = _roleLocalService.addRole(
			TestPropsValues.getUserId(), null, 0, StringUtil.randomString(),
			null, null, RoleConstants.TYPE_SITE, null,
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		Role role2 = _roleLocalService.addRole(
			TestPropsValues.getUserId(), null, 0, StringUtil.randomString(),
			null, null, RoleConstants.TYPE_SITE, null,
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		_menuAccessConfigurationProvider.addRoleToMenuAccess(role1);
		_menuAccessConfigurationProvider.addRoleToMenuAccess(role2);

		_menuAccessConfigurationProvider.deleteRoleFromMenuAccess(role1);

		Assert.assertArrayEquals(
			new String[] {role2.getName()},
			_menuAccessConfigurationProvider.getRolesCanSeeControlMenu(
				_group.getGroupId()));
	}

	@Test
	public void testUpdateMenuAccessConfiguration() throws Exception {
		String[] expectedRolesCanSeeControlMenu = {"test1", "test2"};

		_menuAccessConfigurationProvider.updateMenuAccessConfiguration(
			_group.getGroupId(), expectedRolesCanSeeControlMenu, true);

		Assert.assertArrayEquals(
			expectedRolesCanSeeControlMenu,
			_menuAccessConfigurationProvider.getRolesCanSeeControlMenu(
				_group.getGroupId()));

		Assert.assertTrue(
			_menuAccessConfigurationProvider.isShowControlMenuByRole(
				_group.getGroupId()));
	}

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private MenuAccessConfigurationProvider _menuAccessConfigurationProvider;

	@Inject
	private RoleLocalService _roleLocalService;

}