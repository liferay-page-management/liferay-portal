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

package com.liferay.layout.utility.page.email.address.internal.display.context;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Jürgen Kappler
 */
public class BaseEmailAddressDisplayContext {

	public BaseEmailAddressDisplayContext(
		HttpServletRequest httpServletRequest, ThemeDisplay themeDisplay) {

		this.httpServletRequest = httpServletRequest;
		this.themeDisplay = themeDisplay;
	}

	public String[] getLanguageIds() {
		return LocaleUtil.toLanguageIds(
			LanguageUtil.getAvailableLocales(themeDisplay.getSiteGroupId()));
	}

	public String getUpdateLanguageFormAction() {
		String updateLanguageFormAction = HttpComponentsUtil.addParameter(
			themeDisplay.getPathMain() + "/portal/update_language", "p_l_id",
			themeDisplay.getPlid());

		String updateLanguageRedirect = HttpComponentsUtil.addParameter(
			PortalUtil.getCurrentURL(httpServletRequest), "ticketKey",
			ParamUtil.getString(httpServletRequest, "ticketKey"));

		return HttpComponentsUtil.addParameter(
			updateLanguageFormAction, "redirect", updateLanguageRedirect);
	}

	protected final HttpServletRequest httpServletRequest;
	protected final ThemeDisplay themeDisplay;

}