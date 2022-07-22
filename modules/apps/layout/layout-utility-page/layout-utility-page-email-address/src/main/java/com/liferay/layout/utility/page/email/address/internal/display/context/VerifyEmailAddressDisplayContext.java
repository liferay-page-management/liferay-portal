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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Jürgen Kappler
 */
public class VerifyEmailAddressDisplayContext {

	public VerifyEmailAddressDisplayContext(
		HttpServletRequest httpServletRequest, ThemeDisplay themeDisplay) {

		_httpServletRequest = httpServletRequest;
		_themeDisplay = themeDisplay;
	}

	public String getReferer() throws PortalException {
		String currentURL = PortalUtil.getCurrentURL(_httpServletRequest);

		String referer = ParamUtil.getString(
			_httpServletRequest, WebKeys.REFERER, currentURL);

		if (referer.equals(
				_themeDisplay.getPathMain() + "/portal/update_email_address")) {

			referer =
				_themeDisplay.getPathMain() + "?doAsUserId=" +
					_themeDisplay.getDoAsUserId();
		}
		else if (currentURL.startsWith(
					_themeDisplay.getPathMain() +
						"/portal/verify_email_address")) {

			long requestPlid = ParamUtil.getLong(_httpServletRequest, "p_l_id");

			if (requestPlid > 0) {
				referer = PortalUtil.getLayoutURL(
					LayoutLocalServiceUtil.getLayout(requestPlid),
					_themeDisplay);
			}
			else {
				referer = _themeDisplay.getPathMain();
			}
		}

		return referer;
	}

	private final HttpServletRequest _httpServletRequest;
	private final ThemeDisplay _themeDisplay;

}