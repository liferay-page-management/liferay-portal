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

package com.liferay.journal.web.internal.portlet.action;

import com.liferay.asset.display.page.item.selector.criterion.AssetDisplayPageSelectorCriterion;
import com.liferay.item.selector.ItemSelector;
import com.liferay.item.selector.criteria.UUIDItemSelectorReturnType;
import com.liferay.journal.constants.JournalPortletKeys;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.constants.MVCRenderConstants;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;

import java.io.IOException;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + JournalPortletKeys.JOURNAL,
		"mvc.command.name=/journal/select_display_page"
	},
	service = MVCRenderCommand.class
)
public class SelectDisplayPageMVCRenderCommand implements MVCRenderCommand {

	@Override
	public String render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		HttpServletResponse httpServletResponse =
			_portal.getHttpServletResponse(renderResponse);

		try {
			httpServletResponse.sendRedirect(
				_getAssetDisplayPageItemSelectorURL(renderRequest));
		}
		catch (IOException ioException) {
			_log.error(ioException);

			return "/error.jsp";
		}

		return MVCRenderConstants.MVC_PATH_VALUE_SKIP_DISPATCH;
	}

	private String _getAssetDisplayPageItemSelectorURL(
		RenderRequest renderRequest) {

		AssetDisplayPageSelectorCriterion assetDisplayPageSelectorCriterion =
			new AssetDisplayPageSelectorCriterion();

		assetDisplayPageSelectorCriterion.setClassNameId(
			_portal.getClassNameId(JournalArticle.class));
		assetDisplayPageSelectorCriterion.setClassTypeId(
			ParamUtil.getLong(renderRequest, "classTypeId"));
		assetDisplayPageSelectorCriterion.setGroupId(
			ParamUtil.getLong(renderRequest, "groupId"));
		assetDisplayPageSelectorCriterion.setDesiredItemSelectorReturnTypes(
			new UUIDItemSelectorReturnType());

		return PortletURLBuilder.create(
			_itemSelector.getItemSelectorURL(
				RequestBackedPortletURLFactoryUtil.create(
					_portal.getHttpServletRequest(renderRequest)),
				ParamUtil.getString(renderRequest, "eventName"),
				assetDisplayPageSelectorCriterion)
		).buildString();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SelectDisplayPageMVCRenderCommand.class);

	@Reference
	private ItemSelector _itemSelector;

	@Reference
	private Portal _portal;

}