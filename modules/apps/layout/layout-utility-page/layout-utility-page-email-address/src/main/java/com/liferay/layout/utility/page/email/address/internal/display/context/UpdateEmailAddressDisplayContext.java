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

import com.liferay.portal.kernel.exception.UserEmailAddressException;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Lourdes Fernández Besada
 */
public class UpdateEmailAddressDisplayContext {

	public UpdateEmailAddressDisplayContext(
		HttpServletRequest httpServletRequest, ThemeDisplay themeDisplay) {

		_httpServletRequest = httpServletRequest;
		_themeDisplay = themeDisplay;
	}

	public String getReferer() {
		String referer = ParamUtil.getString(
			_httpServletRequest, WebKeys.REFERER,
			PortalUtil.getCurrentURL(_httpServletRequest));

		if (referer.equals(
				_themeDisplay.getPathMain() + "/portal/update_email_address")) {

			referer =
				_themeDisplay.getPathMain() + "?doAsUserId=" +
					_themeDisplay.getDoAsUserId();
		}

		return referer;
	}

	public String getUserEmailAddressExceptionMessageKey() {
		if (SessionErrors.contains(
				_httpServletRequest,
				UserEmailAddressException.MustBeEqual.class.getName())) {

			return "the-email-addresses-you-entered-do-not-match";
		}

		if (SessionErrors.contains(
				_httpServletRequest,
				UserEmailAddressException.MustNotBeDuplicate.class.getName())) {

			return "the-email-address-you-requested-is-already-taken";
		}

		if (SessionErrors.contains(
				_httpServletRequest,
				UserEmailAddressException.MustNotBeNull.class.getName())) {

			return "please-enter-an-email-address";
		}

		if (SessionErrors.contains(
				_httpServletRequest,
				UserEmailAddressException.MustNotBePOP3User.class.getName()) ||
			SessionErrors.contains(
				_httpServletRequest,
				UserEmailAddressException.MustNotBeReserved.class.getName())) {

			return "the-email-address-you-requested-is-reserved";
		}

		if (SessionErrors.contains(
				_httpServletRequest,
				UserEmailAddressException.MustNotUseCompanyMx.class.
					getName())) {

			return "the-email-address-you-requested-is-not-valid-because-its-" +
				"domain-is-reserved";
		}

		return "please-enter-a-valid-email-address";
	}

	private final HttpServletRequest _httpServletRequest;
	private final ThemeDisplay _themeDisplay;

}