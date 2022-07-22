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
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.model.Ticket;
import com.liferay.portal.kernel.model.TicketConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.security.auth.AuthTokenUtil;
import com.liferay.portal.kernel.service.LayoutSetLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.TicketLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.servlet.PipingServletResponse;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.struts.StrutsAction;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.theme.ThemeUtil;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

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
	immediate = true, property = "path=/portal/verify_email_address",
	service = StrutsAction.class
)
public class VerifyEmailAddressStrutsAction implements StrutsAction {

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

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		if (themeDisplay.isSignedIn() && cmd.equals(Constants.SEND)) {
			_sendEmailAddressVerification(httpServletRequest, themeDisplay);

			_includeView(httpServletRequest, httpServletResponse);

			return null;
		}

		try {
			_verifyEmailAddress(httpServletRequest);

			if (!themeDisplay.isSignedIn()) {
				PortletURL portletURL = PortletURLFactoryUtil.create(
					httpServletRequest, PortletKeys.LOGIN,
					PortletRequest.RENDER_PHASE);

				httpServletResponse.sendRedirect(portletURL.toString());

				return null;
			}

			String referer = ParamUtil.getString(
				httpServletRequest, WebKeys.REFERER,
				_portal.getCurrentURL(httpServletRequest));

			httpServletResponse.sendRedirect(referer);
		}
		catch (Exception exception) {
			if (exception instanceof PortalException ||
				exception instanceof SystemException) {

				SessionErrors.add(httpServletRequest, exception.getClass());

				_includeView(httpServletRequest, httpServletResponse);

				return null;
			}

			_portal.sendError(
				exception, httpServletRequest, httpServletResponse);
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
			_servletContext.getRequestDispatcher("/verify_email_address.jsp");

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

	private void _sendEmailAddressVerification(
			HttpServletRequest httpServletRequest, ThemeDisplay themeDisplay)
		throws Exception {

		User user = themeDisplay.getUser();

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			httpServletRequest);

		List<Ticket> tickets = _ticketLocalService.getTickets(
			themeDisplay.getCompanyId(), User.class.getName(), user.getUserId(),
			TicketConstants.TYPE_EMAIL_ADDRESS);

		if (ListUtil.isEmpty(tickets)) {
			_userLocalService.sendEmailAddressVerification(
				user, user.getEmailAddress(), serviceContext);
		}
		else {
			Ticket ticket = tickets.get(0);

			_userLocalService.sendEmailAddressVerification(
				user, ticket.getExtraInfo(), serviceContext);
		}
	}

	private void _verifyEmailAddress(HttpServletRequest httpServletRequest)
		throws Exception {

		AuthTokenUtil.checkCSRFToken(
			httpServletRequest, VerifyEmailAddressStrutsAction.class.getName());

		String ticketKey = ParamUtil.getString(httpServletRequest, "ticketKey");

		_userLocalService.verifyEmailAddress(ticketKey);
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
	private TicketLocalService _ticketLocalService;

	@Reference
	private UserLocalService _userLocalService;

}