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
public class UpdateEmailAddressDisplayContext
	extends BaseEmailAddressDisplayContext {

	public UpdateEmailAddressDisplayContext(
		HttpServletRequest httpServletRequest, ThemeDisplay themeDisplay) {

		super(httpServletRequest, themeDisplay);
	}

	public String getReferer() {
		String referer = ParamUtil.getString(
			httpServletRequest, WebKeys.REFERER,
			PortalUtil.getCurrentURL(httpServletRequest));

		if (referer.equals(
				themeDisplay.getPathMain() + "/portal/update_email_address")) {

			referer =
				themeDisplay.getPathMain() + "?doAsUserId=" +
					themeDisplay.getDoAsUserId();
		}

		return referer;
	}

	public String getUserEmailAddressExceptionMessageKey() {
		if (SessionErrors.contains(
				httpServletRequest,
				UserEmailAddressException.MustBeEqual.class.getName())) {

			return "the-email-addresses-you-entered-do-not-match";
		}

		if (SessionErrors.contains(
				httpServletRequest,
				UserEmailAddressException.MustNotBeDuplicate.class.getName())) {

			return "the-email-address-you-requested-is-already-taken";
		}

		if (SessionErrors.contains(
				httpServletRequest,
				UserEmailAddressException.MustNotBeNull.class.getName())) {

			return "please-enter-an-email-address";
		}

		if (SessionErrors.contains(
				httpServletRequest,
				UserEmailAddressException.MustNotBePOP3User.class.getName()) ||
			SessionErrors.contains(
				httpServletRequest,
				UserEmailAddressException.MustNotBeReserved.class.getName())) {

			return "the-email-address-you-requested-is-reserved";
		}

		if (SessionErrors.contains(
				httpServletRequest,
				UserEmailAddressException.MustNotUseCompanyMx.class.
					getName())) {

			return "the-email-address-you-requested-is-not-valid-because-its-" +
				"domain-is-reserved";
		}

		return "please-enter-a-valid-email-address";
	}

}