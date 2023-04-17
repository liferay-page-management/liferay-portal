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

package com.liferay.site.manager.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.site.configuration.MenuAccessConfigurationProvider;
import com.liferay.site.manager.MenuAccessManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Mikel Lorza
 */
@RunWith(Arquillian.class)
public class MenuAccessManagerTest {

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
	public void testIsShowControlMenuWithAdministratorInContentPage()
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled("LPS-176136")) {
			return;
		}

		_menuAccessConfigurationProvider.updateMenuAccessConfiguration(
			_group.getGroupId(), new String[0], true);

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY,
			_getThemeDisplay(UserTestUtil.getAdminUser(_group.getCompanyId())));

		MenuAccessManager menuAccessManager = new MenuAccessManager();

		Assert.assertTrue(
			menuAccessManager.isShowControlMenu(mockHttpServletRequest));
	}

	@Test
	public void testIsShowControlMenuWithNormalUserWithoutRoleAccessInContentPage()
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled("LPS-176136")) {
			return;
		}

		_menuAccessConfigurationProvider.updateMenuAccessConfiguration(
			_group.getGroupId(), new String[0], true);

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, _getThemeDisplay(UserTestUtil.addUser()));

		MenuAccessManager menuAccessManager = new MenuAccessManager();

		Assert.assertFalse(
			menuAccessManager.isShowControlMenu(mockHttpServletRequest));
	}

	@Test
	public void testIsShowControlMenuWithNormalUserWithRoleAccessInContentPage()
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled("LPS-176136")) {
			return;
		}

		User user = UserTestUtil.addUser();

		Role role = RoleTestUtil.addRole(RoleConstants.TYPE_SITE);

		_menuAccessConfigurationProvider.updateMenuAccessConfiguration(
			_group.getGroupId(), new String[] {role.getName()}, true);

		_roleLocalService.addUserRole(user.getUserId(), role);

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, _getThemeDisplay(user));

		MenuAccessManager menuAccessManager = new MenuAccessManager();

		Assert.assertTrue(
			menuAccessManager.isShowControlMenu(mockHttpServletRequest));
	}

	@Test
	public void testIsShowControlMenuWithRandomUserInAdminPage()
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled("LPS-176136")) {
			return;
		}

		_menuAccessConfigurationProvider.updateMenuAccessConfiguration(
			_group.getGroupId(), new String[0], true);

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY,
			_getThemeDisplay(
				_layoutLocalService.getLayout(
					_portal.getControlPanelPlid(_group.getCompanyId())),
				UserTestUtil.addUser()));

		MenuAccessManager menuAccessManager = new MenuAccessManager();

		Assert.assertTrue(
			menuAccessManager.isShowControlMenu(mockHttpServletRequest));
	}

	@Test
	public void testIsShowControlMenuWithSiteAdministratorInContentPage()
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled("LPS-176136")) {
			return;
		}

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY,
			_getThemeDisplay(UserTestUtil.addGroupAdminUser(_group)));

		_menuAccessConfigurationProvider.updateMenuAccessConfiguration(
			_group.getGroupId(), new String[0], true);

		MenuAccessManager menuAccessManager = new MenuAccessManager();

		Assert.assertTrue(
			menuAccessManager.isShowControlMenu(mockHttpServletRequest));
	}

	private ThemeDisplay _getThemeDisplay(Layout layout, User user) {
		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setLayout(layout);
		themeDisplay.setScopeGroupId(_group.getGroupId());
		themeDisplay.setSiteGroupId(_group.getGroupId());
		themeDisplay.setUser(user);

		return themeDisplay;
	}

	private ThemeDisplay _getThemeDisplay(User user) throws Exception {
		return _getThemeDisplay(
			LayoutTestUtil.addTypeContentLayout(_group), user);
	}

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject
	private MenuAccessConfigurationProvider _menuAccessConfigurationProvider;

	@Inject
	private Portal _portal;

	@Inject
	private RoleLocalService _roleLocalService;

}