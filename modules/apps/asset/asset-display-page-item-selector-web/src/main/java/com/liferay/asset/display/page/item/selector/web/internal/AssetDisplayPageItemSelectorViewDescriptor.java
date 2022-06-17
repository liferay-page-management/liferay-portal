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

package com.liferay.asset.display.page.item.selector.web.internal;

import com.liferay.asset.display.page.item.selector.AssetDisplayPageItemSelectorReturnType;
import com.liferay.asset.display.page.item.selector.criterion.AssetDisplayPageSelectorCriterion;
import com.liferay.item.selector.ItemSelectorReturnType;
import com.liferay.item.selector.ItemSelectorViewDescriptor;
import com.liferay.layout.page.template.constants.LayoutPageTemplateEntryTypeConstants;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryServiceUtil;
import com.liferay.layout.page.template.util.comparator.LayoutPageTemplateEntryCreateDateComparator;
import com.liferay.layout.page.template.util.comparator.LayoutPageTemplateEntryNameComparator;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Diego Hu
 */
public class AssetDisplayPageItemSelectorViewDescriptor
	implements ItemSelectorViewDescriptor<LayoutPageTemplateEntry> {

	public AssetDisplayPageItemSelectorViewDescriptor(
		AssetDisplayPageSelectorCriterion assetDisplayPageSelectorCriterion,
		HttpServletRequest httpServletRequest, PortletURL portletURL) {

		_assetDisplayPageSelectorCriterion = assetDisplayPageSelectorCriterion;
		_httpServletRequest = httpServletRequest;
		_portletURL = portletURL;

		_themeDisplay = (ThemeDisplay)_httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	@Override
	public String getDefaultDisplayStyle() {
		return "icon";
	}

	@Override
	public ItemDescriptor getItemDescriptor(
		LayoutPageTemplateEntry layoutPageTemplateEntry) {

		return new AssetDisplayPageItemDescriptor(
			_httpServletRequest, layoutPageTemplateEntry);
	}

	@Override
	public ItemSelectorReturnType getItemSelectorReturnType() {
		return new AssetDisplayPageItemSelectorReturnType();
	}

	@Override
	public String[] getOrderByKeys() {
		return new String[] {"name", "create-date"};
	}

	@Override
	public SearchContainer<LayoutPageTemplateEntry> getSearchContainer()
		throws PortalException {

		SearchContainer<LayoutPageTemplateEntry>
			assetDisplayPageSearchContainer = new SearchContainer<>(
				(PortletRequest)_httpServletRequest.getAttribute(
					JavaConstants.JAVAX_PORTLET_REQUEST),
				_portletURL, null, "there-are-no-display-page-templates");

		String orderByCol = ParamUtil.getString(
			_httpServletRequest, "orderByCol", "created-date");

		assetDisplayPageSearchContainer.setOrderByCol(orderByCol);

		String orderByType = ParamUtil.getString(
			_httpServletRequest, "orderByType", "asc");

		assetDisplayPageSearchContainer.setOrderByComparator(
			_getLayoutPageTemplateEntryOrderByComparator(
				orderByCol, orderByType));
		assetDisplayPageSearchContainer.setOrderByType(orderByType);

		String keywords = ParamUtil.getString(_httpServletRequest, "keywords");

		if (Validator.isNotNull(keywords)) {
			assetDisplayPageSearchContainer.setResultsAndTotal(
				() ->
					LayoutPageTemplateEntryServiceUtil.
						getLayoutPageTemplateEntries(
							_themeDisplay.getScopeGroupId(),
							_assetDisplayPageSelectorCriterion.getClassNameId(),
							_assetDisplayPageSelectorCriterion.getClassTypeId(),
							keywords,
							LayoutPageTemplateEntryTypeConstants.
								TYPE_DISPLAY_PAGE,
							WorkflowConstants.STATUS_APPROVED,
							assetDisplayPageSearchContainer.getStart(),
							assetDisplayPageSearchContainer.getEnd(),
							assetDisplayPageSearchContainer.
								getOrderByComparator()),
				LayoutPageTemplateEntryServiceUtil.
					getLayoutPageTemplateEntriesCount(
						_themeDisplay.getScopeGroupId(),
						_assetDisplayPageSelectorCriterion.getClassNameId(),
						_assetDisplayPageSelectorCriterion.getClassTypeId(),
						keywords,
						LayoutPageTemplateEntryTypeConstants.TYPE_DISPLAY_PAGE,
						WorkflowConstants.STATUS_APPROVED));
		}
		else {
			assetDisplayPageSearchContainer.setResultsAndTotal(
				() ->
					LayoutPageTemplateEntryServiceUtil.
						getLayoutPageTemplateEntries(
							_themeDisplay.getScopeGroupId(),
							_assetDisplayPageSelectorCriterion.getClassNameId(),
							_assetDisplayPageSelectorCriterion.getClassTypeId(),
							LayoutPageTemplateEntryTypeConstants.
								TYPE_DISPLAY_PAGE,
							WorkflowConstants.STATUS_APPROVED,
							assetDisplayPageSearchContainer.getStart(),
							assetDisplayPageSearchContainer.getEnd(),
							assetDisplayPageSearchContainer.
								getOrderByComparator()),
				LayoutPageTemplateEntryServiceUtil.
					getLayoutPageTemplateEntriesCount(
						_themeDisplay.getScopeGroupId(),
						_assetDisplayPageSelectorCriterion.getClassNameId(),
						_assetDisplayPageSelectorCriterion.getClassTypeId(),
						LayoutPageTemplateEntryTypeConstants.TYPE_DISPLAY_PAGE,
						WorkflowConstants.STATUS_APPROVED));
		}

		return assetDisplayPageSearchContainer;
	}

	@Override
	public boolean isShowBreadcrumb() {
		return false;
	}

	@Override
	public boolean isShowSearch() {
		return true;
	}

	private OrderByComparator<LayoutPageTemplateEntry>
		_getLayoutPageTemplateEntryOrderByComparator(
			String orderByCol, String orderByType) {

		boolean orderByAsc = false;

		if (orderByType.equals("asc")) {
			orderByAsc = true;
		}

		OrderByComparator<LayoutPageTemplateEntry> orderByComparator = null;

		if (orderByCol.equals("create-date")) {
			orderByComparator = new LayoutPageTemplateEntryCreateDateComparator(
				orderByAsc);
		}
		else if (orderByCol.equals("name")) {
			orderByComparator = new LayoutPageTemplateEntryNameComparator(
				orderByAsc);
		}

		return orderByComparator;
	}

	private final AssetDisplayPageSelectorCriterion
		_assetDisplayPageSelectorCriterion;
	private final HttpServletRequest _httpServletRequest;
	private final PortletURL _portletURL;
	private final ThemeDisplay _themeDisplay;

}