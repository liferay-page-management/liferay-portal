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

package com.liferay.layout.utility.page.email.address.internal.struts;

import com.liferay.petra.io.unsync.UnsyncStringWriter;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.NoSuchUserException;
import com.liferay.portal.kernel.exception.UserEmailAddressException;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.security.auth.AuthTokenUtil;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.service.LayoutSetLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.UserService;
import com.liferay.portal.kernel.servlet.PipingServletResponse;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.struts.StrutsAction;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.theme.ThemeUtil;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portlet.admin.util.AdminUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
@Component(
	immediate = true, property = "path=/portal/update_email_address",
	service = StrutsAction.class
)
public class UpdateEmailAddressStrutsAction implements StrutsAction {

	@Override
	public String execute(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception {

		String cmd = ParamUtil.getString(httpServletRequest, Constants.CMD);

		if (Validator.isNull(cmd)) {
			_includeView(httpServletRequest, httpServletResponse);

			return null;
		}

		try {
			_updateEmailAddress(httpServletRequest);

			String referer = ParamUtil.getString(
				httpServletRequest, WebKeys.REFERER,
				_portal.getCurrentURL(httpServletRequest));

			httpServletResponse.sendRedirect(referer);
		}
		catch (Exception exception) {
			if (exception instanceof UserEmailAddressException) {
				SessionErrors.add(httpServletRequest, exception.getClass());

				_includeView(httpServletRequest, httpServletResponse);

				return null;
			}
			else if (exception instanceof NoSuchUserException ||
					 exception instanceof PrincipalException) {

				SessionErrors.add(httpServletRequest, exception.getClass());

				ThemeDisplay themeDisplay =
					(ThemeDisplay)httpServletRequest.getAttribute(
						WebKeys.THEME_DISPLAY);

				StringBundler sb = new StringBundler(3);

				sb.append(themeDisplay.getPathMain());
				sb.append("/portal/error?redirect=");
				sb.append(
					URLCodec.encodeURL(httpServletRequest.getRequestURI()));

				httpServletResponse.sendRedirect(sb.toString());

				return null;
			}

			_portal.sendError(
				exception, httpServletRequest, httpServletResponse);

			return null;
		}

		return null;
	}

	private void _includeView(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		LayoutSet layoutSet = _layoutSetLocalService.getLayoutSet(
			themeDisplay.getScopeGroupId(), false);

		themeDisplay.setLayoutSet(layoutSet);
		themeDisplay.setLookAndFeel(
			layoutSet.getTheme(), layoutSet.getColorScheme());

		httpServletRequest.setAttribute(WebKeys.THEME_DISPLAY, themeDisplay);

		RequestDispatcher requestDispatcher =
			_servletContext.getRequestDispatcher("/update_email_address.jsp");

		UnsyncStringWriter unsyncStringWriter = new UnsyncStringWriter();

		PipingServletResponse pipingServletResponse = new PipingServletResponse(
			httpServletResponse, unsyncStringWriter);

		requestDispatcher.include(httpServletRequest, pipingServletResponse);

		Document document = Jsoup.parse(
			ThemeUtil.include(
				httpServletRequest.getServletContext(), httpServletRequest,
				httpServletResponse, "portal_normal.ftl", layoutSet.getTheme(),
				false));

		Element contentElement = document.getElementById("content");

		contentElement.html(unsyncStringWriter.toString());

		ServletResponseUtil.write(httpServletResponse, document.html());
	}

	private void _updateEmailAddress(HttpServletRequest httpServletRequest)
		throws Exception {

		AuthTokenUtil.checkCSRFToken(
			httpServletRequest, UpdateEmailAddressStrutsAction.class.getName());

		long userId = _portal.getUserId(httpServletRequest);

		String password = AdminUtil.getUpdateUserPassword(
			httpServletRequest, userId);

		String emailAddress1 = ParamUtil.getString(
			httpServletRequest, "emailAddress1");
		String emailAddress2 = ParamUtil.getString(
			httpServletRequest, "emailAddress2");

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			httpServletRequest);

		_userService.updateEmailAddress(
			userId, password, emailAddress1, emailAddress2, serviceContext);
	}

	@Reference
	private LayoutSetLocalService _layoutSetLocalService;

	@Reference
	private Portal _portal;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.layout.utility.page.email.address)"
	)
	private ServletContext _servletContext;

	@Reference
	private UserService _userService;

}