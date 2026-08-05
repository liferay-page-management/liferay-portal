/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.test.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.module.util.BundleUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderResponse;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletURL;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.portlet.RenderRequest;
import jakarta.portlet.RenderResponse;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Constructor;

import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Georgel Pop
 */
public class DisplayContextTestUtil {

	public static Object createDisplayContext(
			Constructor<?> constructor, Group group,
			Map<String, String> parameters)
		throws Exception {

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			CompanyLocalServiceUtil.getCompany(group.getCompanyId()));
		themeDisplay.setLocale(LocaleUtil.getDefault());
		themeDisplay.setPermissionChecker(
			PermissionThreadLocal.getPermissionChecker());
		themeDisplay.setScopeGroupId(group.getGroupId());
		themeDisplay.setUser(TestPropsValues.getUser());

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			mockHttpServletRequest.setParameter(
				entry.getKey(), entry.getValue());
		}

		MockLiferayPortletRenderRequest mockLiferayPortletRenderRequest =
			new MockLiferayPortletRenderRequest(mockHttpServletRequest);

		mockLiferayPortletRenderRequest.setAttribute(
			StringBundler.concat(
				mockLiferayPortletRenderRequest.getPortletName(), "-",
				WebKeys.CURRENT_PORTLET_URL),
			new MockLiferayPortletURL());

		return constructor.newInstance(
			mockHttpServletRequest, mockLiferayPortletRenderRequest,
			new MockLiferayPortletRenderResponse());
	}

	public static Constructor<?> getConstructor(String className)
		throws Exception {

		Bundle bundle = FrameworkUtil.getBundle(DisplayContextTestUtil.class);

		bundle = BundleUtil.getBundle(
			bundle.getBundleContext(), "com.liferay.fragment.web");

		Class<?> clazz = bundle.loadClass(className);

		return clazz.getConstructor(
			HttpServletRequest.class, RenderRequest.class,
			RenderResponse.class);
	}

}