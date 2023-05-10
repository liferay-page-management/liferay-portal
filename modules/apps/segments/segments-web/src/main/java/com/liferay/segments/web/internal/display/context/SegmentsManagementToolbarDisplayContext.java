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

package com.liferay.segments.web.internal.display.context;

import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.SearchContainerManagementToolbarDisplayContext;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenuBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemListBuilder;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.PortletURLUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.segments.configuration.provider.SegmentsConfigurationProvider;
import com.liferay.segments.constants.SegmentsActionKeys;
import com.liferay.segments.constants.SegmentsEntryConstants;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.web.internal.security.permission.resource.SegmentsEntryPermission;
import com.liferay.segments.web.internal.security.permission.resource.SegmentsResourcePermission;

import java.util.List;
import java.util.Objects;

import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Yurena Cabrera
 */
public class SegmentsManagementToolbarDisplayContext
	extends SearchContainerManagementToolbarDisplayContext {

	public SegmentsManagementToolbarDisplayContext(
			HttpServletRequest httpServletRequest,
			LiferayPortletRequest liferayPortletRequest,
			LiferayPortletResponse liferayPortletResponse,
			RenderResponse renderResponse,
			SegmentsDisplayContext segmentsDisplayContext)
		throws PortalException {

		super(
			httpServletRequest, liferayPortletRequest, liferayPortletResponse,
			segmentsDisplayContext.getSearchContainer());

		_currentURLObj = PortletURLUtil.getCurrent(
			liferayPortletRequest, liferayPortletResponse);

		_httpServletRequest = httpServletRequest;
		_renderResponse = renderResponse;
		_segmentsDisplayContext = segmentsDisplayContext;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	@Override
	public List<DropdownItem> getActionDropdownItems() {
		return DropdownItemListBuilder.add(
			dropdownItem -> {
				dropdownItem.putData("action", "deleteSegmentsEntries");
				dropdownItem.setIcon("times-circle");
				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "delete"));
				dropdownItem.setQuickAction(true);
			}
		).build();
	}

	public String getAvailableActions(SegmentsEntry segmentsEntry)
		throws PortalException {

		if (SegmentsEntryPermission.contains(
				_themeDisplay.getPermissionChecker(), segmentsEntry,
				ActionKeys.DELETE)) {

			return "deleteSegmentsEntries";
		}

		return StringPool.BLANK;
	}

	public String getClearResultsURL() {
		return PortletURLBuilder.create(
			_getPortletURL()
		).setKeywords(
			StringPool.BLANK
		).buildString();
	}

	public CreationMenu getCreationMenu() {
		return CreationMenuBuilder.addPrimaryDropdownItem(
			dropdownItem -> {
				dropdownItem.setHref(
					_renderResponse.createRenderURL(), "mvcRenderCommandName",
					"/segments/edit_segments_entry", "type",
					User.class.getName());
				dropdownItem.setLabel(
					LanguageUtil.get(
						_httpServletRequest, "add-new-user-segment"));
			}
		).build();
	}

	public List<DropdownItem> getFilterItemsDropdownItems() {
		return DropdownItemListBuilder.addGroup(
			dropdownGroupItem -> {
				dropdownGroupItem.setDropdownItems(
					_getFilterNavigationDropdownItems());
				dropdownGroupItem.setLabel(
					LanguageUtil.get(
						_httpServletRequest, "filter-by-navigation"));
			}
		).addGroup(
			dropdownGroupItem -> {
				dropdownGroupItem.setDropdownItems(_getOrderByDropdownItems());
				dropdownGroupItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "order-by"));
			}
		).build();
	}

	public String getOrderByType() {
		return _segmentsDisplayContext.getOrderByType();
	}

	public String getScopeName(SegmentsEntry segmentsEntry) {
		if (_themeDisplay.getCompanyGroupId() == segmentsEntry.getGroupId()) {
			return LanguageUtil.get(_themeDisplay.getLocale(), "global");
		}

		if (segmentsEntry.getGroupId() == _themeDisplay.getScopeGroupId()) {
			return LanguageUtil.get(_themeDisplay.getLocale(), "current-site");
		}

		return LanguageUtil.get(_themeDisplay.getLocale(), "parent-site");
	}

	public String getSearchActionURL() {
		return String.valueOf(_getPortletURL());
	}

	public SearchContainer<SegmentsEntry> getSearchContainer()
		throws PortalException {

		return _segmentsDisplayContext.getSearchContainer();
	}

	public String getSegmentsCompanyConfigurationURL(
		HttpServletRequest httpServletRequest) {

		SegmentsConfigurationProvider segmentsConfigurationProvider =
			_segmentsDisplayContext.getSegmentsConfigurationProvider();

		try {
			return segmentsConfigurationProvider.getCompanyConfigurationURL(
				httpServletRequest);
		}
		catch (PortalException portalException) {
			_log.error(portalException);
		}

		return StringPool.BLANK;
	}

	public SegmentsDisplayContext getSegmentsDisplayContext() {
		return _segmentsDisplayContext;
	}

	public String getSegmentsEntryURL(SegmentsEntry segmentsEntry)
		throws ConfigurationException {

		if (segmentsEntry == null) {
			return StringPool.BLANK;
		}

		if (Objects.equals(
				segmentsEntry.getSource(),
				SegmentsEntryConstants.SOURCE_ASAH_FARO_BACKEND) &&
			Validator.isNull(segmentsEntry.getCriteria())) {

			String liferayAnalyticsURL =
				_segmentsDisplayContext.getLiferayAnalyticsURL(segmentsEntry);

			if (Validator.isNull(liferayAnalyticsURL)) {
				return StringPool.BLANK;
			}

			return liferayAnalyticsURL + "/contacts/segments/" +
				segmentsEntry.getSegmentsEntryKey();
		}

		return PortletURLBuilder.createRenderURL(
			_renderResponse
		).setMVCRenderCommandName(
			"/segments/edit_segments_entry"
		).setRedirect(
			_currentURLObj
		).setParameter(
			"segmentsEntryId", segmentsEntry.getSegmentsEntryId()
		).setParameter(
			"showInEditMode", false
		).buildString();
	}

	public String getSegmentsEntryURLTarget(SegmentsEntry segmentsEntry) {
		if (Objects.equals(
				segmentsEntry.getSource(),
				SegmentsEntryConstants.SOURCE_ASAH_FARO_BACKEND) &&
			Validator.isNull(segmentsEntry.getCriteria())) {

			return "_blank";
		}

		return "_self";
	}

	public String getSortingURL() {
		return PortletURLBuilder.create(
			_getPortletURL()
		).setParameter(
			"orderByType",
			Objects.equals(getOrderByType(), "asc") ? "desc" : "asc"
		).buildString();
	}

	public int getTotalItems() throws PortalException {
		return _segmentsDisplayContext.getTotalItems();
	}

	public boolean isDisabledManagementBar() throws PortalException {
		return _segmentsDisplayContext.isDisabledManagementBar();
	}

	public boolean isSegmentationEnabled(long companyId) {
		return _segmentsDisplayContext.isSegmentationEnabled(companyId);
	}

	@Override
	public Boolean isShowCreationMenu() {
		if (SegmentsResourcePermission.contains(
				_themeDisplay.getPermissionChecker(),
				_themeDisplay.getScopeGroupId(),
				SegmentsActionKeys.MANAGE_SEGMENTS_ENTRIES)) {

			return true;
		}

		return false;
	}

	private List<DropdownItem> _getFilterNavigationDropdownItems() {
		return DropdownItemListBuilder.add(
			dropdownItem -> {
				dropdownItem.setActive(true);
				dropdownItem.setHref(_renderResponse.createRenderURL());
				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "all"));
			}
		).build();
	}

	private List<DropdownItem> _getOrderByDropdownItems() {
		return DropdownItemListBuilder.add(
			dropdownItem -> {
				dropdownItem.setActive(
					Objects.equals(
						_segmentsDisplayContext.getOrderByCol(),
						"modified-date"));
				dropdownItem.setHref(
					_getPortletURL(), "orderByCol", "modified-date");
				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "modified-date"));
			}
		).add(
			dropdownItem -> {
				dropdownItem.setActive(
					Objects.equals(
						_segmentsDisplayContext.getOrderByCol(), "name"));
				dropdownItem.setHref(_getPortletURL(), "orderByCol", "name");
				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "name"));
			}
		).build();
	}

	private PortletURL _getPortletURL() {
		return PortletURLBuilder.createRenderURL(
			_renderResponse
		).setMVCPath(
			"/view.jsp"
		).setKeywords(
			() -> {
				String keywords = _segmentsDisplayContext.getKeywords();

				if (Validator.isNotNull(keywords)) {
					return keywords;
				}

				return null;
			}
		).setParameter(
			"displayStyle", getDisplayStyle()
		).setParameter(
			"orderByCol", _segmentsDisplayContext.getOrderByCol()
		).setParameter(
			"orderByType", getOrderByType()
		).buildPortletURL();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SegmentsManagementToolbarDisplayContext.class);

	private final PortletURL _currentURLObj;
	private final HttpServletRequest _httpServletRequest;
	private final RenderResponse _renderResponse;
	private final SegmentsDisplayContext _segmentsDisplayContext;
	private final ThemeDisplay _themeDisplay;

}