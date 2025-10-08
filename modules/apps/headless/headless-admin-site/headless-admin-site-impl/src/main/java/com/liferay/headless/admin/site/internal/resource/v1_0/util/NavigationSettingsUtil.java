/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.resource.v1_0.util;

import com.liferay.headless.admin.site.dto.v1_0.NavigationSettings;
import com.liferay.headless.admin.site.dto.v1_0.SitePageNavigationSettings;
import com.liferay.headless.admin.site.dto.v1_0.WidgetPageTemplateNavigationSettings;
import com.liferay.layout.admin.kernel.model.LayoutTypePortletConstants;
import com.liferay.portal.kernel.util.UnicodeProperties;

import java.util.Objects;

/**
 * @author Jürgen Kappler
 * @author Javier de Arcos
 */
public class NavigationSettingsUtil {

	public static NavigationSettings toNavigationSettings(
		NavigationSettings.NavigationSettingsType navigationSettingsType,
		UnicodeProperties unicodeProperties) {

		NavigationSettings navigationSettings = null;

		if (navigationSettingsType ==
				NavigationSettings.NavigationSettingsType.SITE_PAGE) {

			navigationSettings = new SitePageNavigationSettings();

			SitePageNavigationSettings sitePageNavigationSettings =
				(SitePageNavigationSettings)navigationSettings;

			sitePageNavigationSettings.setQueryString(
				() -> unicodeProperties.getProperty(
					LayoutTypePortletConstants.QUERY_STRING));
		}
		else {
			navigationSettings = new WidgetPageTemplateNavigationSettings();
		}

		navigationSettings.setTarget(
			() -> unicodeProperties.getProperty("target"));
		navigationSettings.setTargetType(
			() -> {
				if (Objects.equals(
						unicodeProperties.getProperty("targetType"),
						"useNewTab")) {

					return NavigationSettings.TargetType.NEW_TAB;
				}

				return NavigationSettings.TargetType.SPECIFIC_FRAME;
			});

		return navigationSettings;
	}

}