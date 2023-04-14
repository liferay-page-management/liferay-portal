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

package com.liferay.site.manager;

import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.configuration.MenuAccessConfiguration;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Mikel Lorza
 */
public class MenuAccessManager {

	public static boolean isShowControlMenu(
		HttpServletRequest httpServletRequest) {

		if (!FeatureFlagManagerUtil.isEnabled("LPS-176136")) {
			return true;
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		Layout layout = themeDisplay.getLayout();

		if (layout.isTypeControlPanel() || layout.isDraftLayout()) {
			return true;
		}

		try {
			MenuAccessConfiguration menuAccessConfiguration =
				ConfigurationProviderUtil.getGroupConfiguration(
					MenuAccessConfiguration.class,
					themeDisplay.getScopeGroupId());

			if ((menuAccessConfiguration != null) &&
				menuAccessConfiguration.showControlMenuByRole()) {

				User user = themeDisplay.getUser();

				List<Role> roles = user.getRoles();

				String[] rolesCanSeeControlMenu =
					menuAccessConfiguration.rolesCanSeeControlMenu();

				for (Role role : roles) {
					if (ArrayUtil.contains(
							rolesCanSeeControlMenu, role.getName()) ||
						RoleConstants.SITE_ADMINISTRATOR.equals(
							role.getName()) ||
						RoleConstants.ADMINISTRATOR.equals(role.getName())) {

						return true;
					}
				}

				return false;
			}
		}
		catch (ConfigurationException configurationException) {
			_log.error(configurationException);
		}

		return true;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MenuAccessManager.class);

}