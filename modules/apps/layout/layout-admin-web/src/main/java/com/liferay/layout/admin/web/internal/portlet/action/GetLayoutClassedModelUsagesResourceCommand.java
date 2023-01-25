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

import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.layout.admin.constants.LayoutAdminPortletKeys;
import com.liferay.layout.model.LayoutClassedModelUsage;
import com.liferay.layout.page.template.constants.LayoutPageTemplateEntryTypeConstants;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.layout.service.LayoutClassedModelUsageLocalService;
import com.liferay.layout.util.comparator.LayoutClassedModelUsageModifiedDateComparator;
import com.liferay.layout.util.constants.LayoutClassedModelUsageConstants;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.List;
import java.util.Locale;

import javax.portlet.PortletRequest;
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
					_getLayoutClassedModelUsageName(
						layoutClassedModelUsage, themeDisplay.getLocale())
				).put(
					"type",
					_language.get(
						themeDisplay.getLocale(),
						_getLayoutClassedModelUsageTypeLabel(
							layoutClassedModelUsage))
				).put(
					"url",
					() -> {
						if (!_isShowPreview(layoutClassedModelUsage)) {
							return null;
						}

						return _getLayoutClassedModelUsagePreviewURL(
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

	private String _getLayoutClassedModelUsageName(
		LayoutClassedModelUsage layoutClassedModelUsage, Locale locale) {

		if (layoutClassedModelUsage.getType() ==
				LayoutClassedModelUsageConstants.TYPE_LAYOUT) {

			Layout layout = _layoutLocalService.fetchLayout(
				layoutClassedModelUsage.getPlid());

			if (layout == null) {
				return StringPool.BLANK;
			}

			if (!layout.isDraftLayout()) {
				return layout.getName(locale);
			}

			return StringBundler.concat(
				layout.getName(locale), " (", _language.get(locale, "draft"),
				")");
		}

		long plid = layoutClassedModelUsage.getPlid();

		Layout layout = _layoutLocalService.fetchLayout(plid);

		if (layout.isDraftLayout()) {
			plid = layout.getClassPK();
		}

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			_layoutPageTemplateEntryLocalService.
				fetchLayoutPageTemplateEntryByPlid(plid);

		if (layoutPageTemplateEntry == null) {
			return StringPool.BLANK;
		}

		if (!layout.isDraftLayout()) {
			return layoutPageTemplateEntry.getName();
		}

		return StringBundler.concat(
			layoutPageTemplateEntry.getName(), " (",
			_language.get(locale, "draft"), ")");
	}

	private String _getLayoutClassedModelUsagePreviewURL(
			LayoutClassedModelUsage layoutClassedModelUsage,
			ResourceRequest resourceRequest)
		throws Exception {

		String layoutURL = null;

		if (layoutClassedModelUsage.getContainerType() ==
				_portal.getClassNameId(FragmentEntryLink.class)) {

			ThemeDisplay themeDisplay =
				(ThemeDisplay)resourceRequest.getAttribute(
					WebKeys.THEME_DISPLAY);

			layoutURL = _portal.getLayoutFriendlyURL(
				_layoutLocalService.fetchLayout(
					layoutClassedModelUsage.getPlid()),
				themeDisplay);

			layoutURL = HttpComponentsUtil.setParameter(
				layoutURL, "previewClassNameId",
				String.valueOf(layoutClassedModelUsage.getClassNameId()));
			layoutURL = HttpComponentsUtil.setParameter(
				layoutURL, "previewClassPK",
				String.valueOf(layoutClassedModelUsage.getClassPK()));
			layoutURL = HttpComponentsUtil.setParameter(
				layoutURL, "previewType",
				String.valueOf(AssetRendererFactory.TYPE_LATEST));
		}
		else {
			layoutURL = PortletURLBuilder.create(
				PortletURLFactoryUtil.create(
					resourceRequest, layoutClassedModelUsage.getContainerKey(),
					layoutClassedModelUsage.getPlid(),
					PortletRequest.RENDER_PHASE)
			).setParameter(
				"previewClassNameId", layoutClassedModelUsage.getClassNameId()
			).setParameter(
				"previewClassPK", layoutClassedModelUsage.getClassPK()
			).setParameter(
				"previewType", AssetRendererFactory.TYPE_LATEST
			).buildString();
		}

		String portletURLString = HttpComponentsUtil.addParameter(
			layoutURL, "p_l_mode", Constants.PREVIEW);

		return portletURLString + "#portlet_" +
			layoutClassedModelUsage.getContainerKey();
	}

	private String _getLayoutClassedModelUsageTypeLabel(
		LayoutClassedModelUsage layoutClassedModelUsage) {

		if (layoutClassedModelUsage.getType() ==
				LayoutClassedModelUsageConstants.TYPE_DISPLAY_PAGE_TEMPLATE) {

			return "display-page-template";
		}

		if (layoutClassedModelUsage.getType() ==
				LayoutClassedModelUsageConstants.TYPE_LAYOUT) {

			return "page";
		}

		return "page-template";
	}

	private boolean _isShowPreview(
		LayoutClassedModelUsage layoutClassedModelUsage) {

		if (layoutClassedModelUsage.getType() ==
				LayoutClassedModelUsageConstants.TYPE_LAYOUT) {

			return true;
		}

		if ((layoutClassedModelUsage.getType() ==
				LayoutClassedModelUsageConstants.TYPE_DISPLAY_PAGE_TEMPLATE) ||
			(layoutClassedModelUsage.getType() !=
				LayoutClassedModelUsageConstants.TYPE_PAGE_TEMPLATE)) {

			return false;
		}

		long plid = layoutClassedModelUsage.getPlid();

		Layout layout = _layoutLocalService.fetchLayout(plid);

		if (layout.isDraftLayout()) {
			plid = layout.getClassPK();
		}

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			_layoutPageTemplateEntryLocalService.
				fetchLayoutPageTemplateEntryByPlid(plid);

		if ((layoutPageTemplateEntry == null) ||
			(layoutPageTemplateEntry.getType() ==
				LayoutPageTemplateEntryTypeConstants.TYPE_WIDGET_PAGE)) {

			return false;
		}

		return true;
	}

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Language _language;

	@Reference
	private LayoutClassedModelUsageLocalService
		_layoutClassedModelUsageLocalService;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private LayoutPageTemplateEntryLocalService
		_layoutPageTemplateEntryLocalService;

	@Reference
	private Portal _portal;

}