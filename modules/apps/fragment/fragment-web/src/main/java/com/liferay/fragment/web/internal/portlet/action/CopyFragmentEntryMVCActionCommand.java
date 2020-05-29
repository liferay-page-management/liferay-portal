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

package com.liferay.fragment.web.internal.portlet.action;

import com.liferay.fragment.constants.FragmentPortletKeys;
import com.liferay.fragment.service.FragmentEntryService;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletURL;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Pavel Savinov
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + FragmentPortletKeys.FRAGMENT,
		"mvc.command.name=/fragment/copy_fragment_entry"
	},
	service = {AopService.class, MVCActionCommand.class}
)
public class CopyFragmentEntryMVCActionCommand
	extends BaseMVCActionCommand implements AopService {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortalException {

		long fragmentCollectionId = ParamUtil.getLong(
			actionRequest, "fragmentCollectionId");

		long[] fragmentEntryIds = StringUtil.split(
			ParamUtil.getString(actionRequest, "fragmentEntryIds"), 0L);

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			actionRequest);

		for (long fragmentEntryId : fragmentEntryIds) {
			_copyFragmentEntry(
				themeDisplay.getScopeGroupId(), fragmentCollectionId,
				fragmentEntryId, serviceContext);
		}

		LiferayPortletResponse liferayPortletResponse =
			_portal.getLiferayPortletResponse(actionResponse);

		PortletURL redirectURL = liferayPortletResponse.createRenderURL();

		redirectURL.setParameter(
			"fragmentCollectionId", String.valueOf(fragmentCollectionId));

		sendRedirect(actionRequest, actionResponse, redirectURL.toString());
	}

	@Transactional(rollbackFor = PortalException.class)
	private void _copyFragmentEntry(
			long groupId, long fragmentCollectionId, long fragmentEntryId,
			ServiceContext serviceContext)
		throws PortalException {

		_fragmentEntryService.copyFragmentEntry(
			groupId, fragmentEntryId, fragmentCollectionId, serviceContext);
	}

	@Reference
	private FragmentEntryService _fragmentEntryService;

	@Reference
	private Portal _portal;

}