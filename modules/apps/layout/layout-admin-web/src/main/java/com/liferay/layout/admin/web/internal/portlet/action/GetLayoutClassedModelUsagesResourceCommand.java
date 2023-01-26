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

package com.liferay.layout.admin.web.internal.portlet.action;

import com.liferay.layout.admin.constants.LayoutAdminPortletKeys;
import com.liferay.layout.helper.LayoutClassedModelUsagesHelper;
import com.liferay.layout.model.LayoutClassedModelUsage;
import com.liferay.layout.service.LayoutClassedModelUsageLocalService;
import com.liferay.layout.util.comparator.LayoutClassedModelUsageModifiedDateComparator;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.List;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rubén Pulido
 */
@Component(
	property = {
		"javax.portlet.name=" + LayoutAdminPortletKeys.GROUP_PAGES,
		"mvc.command.name=/layout_admin/get_layout_classed_model_usages"
	},
	service = MVCResourceCommand.class
)
public class GetLayoutClassedModelUsagesResourceCommand
	extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		String className = ParamUtil.getString(resourceRequest, "className");
		long classPK = ParamUtil.getLong(resourceRequest, "classPK");

		long classNameId = _portal.getClassNameId(className);

		int usagesPageSize = GetterUtil.getInteger(
			PropsUtil.get(PropsKeys.SEARCH_CONTAINER_PAGE_DEFAULT_DELTA), 20);

		int layoutClassedModelUsagesCount =
			_layoutClassedModelUsageLocalService.
				getLayoutClassedModelUsagesCount(classNameId, classPK);

		int totalNumberOfPages = (int)Math.ceil(
			layoutClassedModelUsagesCount / (double)usagesPageSize);

		JSONArray usagesjsonArray = _jsonFactory.createJSONArray();

		if (layoutClassedModelUsagesCount == 0) {
			JSONPortletResponseUtil.writeJSON(
				resourceRequest, resourceResponse,
				JSONUtil.put(
					"totalNumberOfPages", totalNumberOfPages
				).put(
					"usages", usagesjsonArray
				));

			return;
		}

		int pageIndex = ParamUtil.getInteger(resourceRequest, "pageIndex", 1);

		if (pageIndex < 1) {
			pageIndex = 1;
		}

		if (pageIndex > totalNumberOfPages) {
			pageIndex = totalNumberOfPages;
		}

		List<LayoutClassedModelUsage> layoutClassedModelUsages =
			_layoutClassedModelUsageLocalService.getLayoutClassedModelUsages(
				classNameId, classPK, usagesPageSize * (pageIndex - 1),
				usagesPageSize * pageIndex,
				new LayoutClassedModelUsageModifiedDateComparator(false));

		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		for (LayoutClassedModelUsage layoutClassedModelUsage :
				layoutClassedModelUsages) {

			usagesjsonArray.put(
				JSONUtil.put(
					"id", layoutClassedModelUsage.getLayoutClassedModelUsageId()
				).put(
					"name",
					_layoutClassedModelUsagesHelper.getName(
						layoutClassedModelUsage, themeDisplay.getLocale())
				).put(
					"type",
					_language.get(
						themeDisplay.getLocale(),
						_layoutClassedModelUsagesHelper.getTypeLabel(
							layoutClassedModelUsage))
				).put(
					"url",
					() -> {
						if (!_layoutClassedModelUsagesHelper.isShowPreview(
								layoutClassedModelUsage)) {

							return null;
						}

						return _layoutClassedModelUsagesHelper.getPreviewURL(
							layoutClassedModelUsage, resourceRequest);
					}
				));
		}

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse,
			JSONUtil.put(
				"totalNumberOfPages", totalNumberOfPages
			).put(
				"usages", usagesjsonArray
			));
	}

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Language _language;

	@Reference
	private LayoutClassedModelUsageLocalService
		_layoutClassedModelUsageLocalService;

	@Reference
	private LayoutClassedModelUsagesHelper _layoutClassedModelUsagesHelper;

	@Reference
	private Portal _portal;

}