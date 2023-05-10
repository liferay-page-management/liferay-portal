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

import com.liferay.analytics.settings.configuration.AnalyticsConfiguration;
import com.liferay.analytics.settings.rest.manager.AnalyticsSettingsManager;
import com.liferay.item.selector.ItemSelector;
import com.liferay.item.selector.criteria.UUIDItemSelectorReturnType;
import com.liferay.portal.kernel.dao.search.EmptyOnClickRowChecker;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.SearchDisplayStyleUtil;
import com.liferay.portal.kernel.portlet.SearchOrderByUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.roles.admin.role.type.contributor.RoleTypeContributor;
import com.liferay.roles.admin.role.type.contributor.provider.RoleTypeContributorProvider;
import com.liferay.roles.item.selector.RoleItemSelectorCriterion;
import com.liferay.segments.configuration.provider.SegmentsConfigurationProvider;
import com.liferay.segments.constants.SegmentsPortletKeys;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.service.SegmentsEntryService;
import com.liferay.segments.web.internal.security.permission.resource.SegmentsEntryPermission;
import com.liferay.segments.web.internal.util.comparator.SegmentsEntryModifiedDateComparator;
import com.liferay.segments.web.internal.util.comparator.SegmentsEntryNameComparator;

import java.util.Map;
import java.util.Objects;

import javax.portlet.ActionRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Eduardo García
 */
public class SegmentsDisplayContext {

	public SegmentsDisplayContext(
		AnalyticsSettingsManager analyticsSettingsManager,
		GroupLocalService groupLocalService, ItemSelector itemSelector,
		Language language, Portal portal, RenderRequest renderRequest,
		RenderResponse renderResponse,
		RoleTypeContributorProvider roleTypeContributorProvider,
		SegmentsConfigurationProvider segmentsConfigurationProvider,
		SegmentsEntryService segmentsEntryService) {

		_analyticsSettingsManager = analyticsSettingsManager;
		_groupLocalService = groupLocalService;
		_itemSelector = itemSelector;
		_language = language;
		_portal = portal;
		_renderRequest = renderRequest;
		_renderResponse = renderResponse;
		_roleTypeContributorProvider = roleTypeContributorProvider;
		_segmentsConfigurationProvider = segmentsConfigurationProvider;
		_segmentsEntryService = segmentsEntryService;

		_httpServletRequest = portal.getHttpServletRequest(renderRequest);

		_themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		_permissionChecker = _themeDisplay.getPermissionChecker();
	}

	public AnalyticsSettingsManager getAnalyticsSettingsManager() {
		return _analyticsSettingsManager;
	}

	public Map<String, Object> getAssignUserRolesDataMap(
		SegmentsEntry segmentsEntry) {

		return HashMapBuilder.<String, Object>put(
			"itemSelectorURL",
			() -> {
				RoleItemSelectorCriterion roleItemSelectorCriterion =
					new RoleItemSelectorCriterion(RoleConstants.TYPE_SITE);

				roleItemSelectorCriterion.setCheckedRoleIds(
					segmentsEntry.getRoleIds());
				roleItemSelectorCriterion.setDesiredItemSelectorReturnTypes(
					new UUIDItemSelectorReturnType());
				roleItemSelectorCriterion.setExcludedRoleNames(
					_getExcludedRoleNames());

				PortletURL portletURL = _itemSelector.getItemSelectorURL(
					RequestBackedPortletURLFactoryUtil.create(_renderRequest),
					(String)_httpServletRequest.getAttribute(
						"view.jsp-eventName"),
					roleItemSelectorCriterion);

				return portletURL.toString();
			}
		).put(
			"segmentsEntryId", segmentsEntry.getSegmentsEntryId()
		).build();
	}

	public String getDeleteURL(SegmentsEntry segmentsEntry) {
		return PortletURLBuilder.createActionURL(
			_renderResponse
		).setActionName(
			"/segments/delete_segments_entry"
		).setRedirect(
			_portal.getCurrentURL(_renderRequest)
		).setParameter(
			"segmentsEntryId", segmentsEntry.getSegmentsEntryId()
		).buildString();
	}

	public String getDisplayStyle() {
		if (_displayStyle != null) {
			return _displayStyle;
		}

		_displayStyle = SearchDisplayStyleUtil.getDisplayStyle(
			_renderRequest, SegmentsPortletKeys.SEGMENTS, "list");

		return _displayStyle;
	}

	public String getEditURL(SegmentsEntry segmentsEntry) {
		return PortletURLBuilder.createRenderURL(
			_renderResponse
		).setMVCRenderCommandName(
			"/segments/edit_segments_entry"
		).setRedirect(
			_portal.getCurrentURL(_renderRequest)
		).setParameter(
			"segmentsEntryId", segmentsEntry.getSegmentsEntryId()
		).buildString();
	}

	public String getKeywords() {
		return _keywords;
	}

	public String getLiferayAnalyticsURL(SegmentsEntry segmentsEntry)
		throws ConfigurationException {

		AnalyticsConfiguration analyticsConfiguration =
			_analyticsSettingsManager.getAnalyticsConfiguration(
				segmentsEntry.getCompanyId());

		return analyticsConfiguration.liferayAnalyticsURL();
	}

	public String getOrderByCol() {
		if (_orderByCol != null) {
			return _orderByCol;
		}

		_orderByCol = SearchOrderByUtil.getOrderByCol(
			_renderRequest, SegmentsPortletKeys.SEGMENTS, "modified-date");

		return _orderByCol;
	}

	public String getOrderByType() {
		if (_orderByType != null) {
			return _orderByType;
		}

		_orderByType = SearchOrderByUtil.getOrderByType(
			_renderRequest, SegmentsPortletKeys.SEGMENTS, "asc");

		return _orderByType;
	}

	public String getPermissionURL(SegmentsEntry segmentsEntry) {
		return PortletURLBuilder.create(
			_portal.getControlPanelPortletURL(
				_httpServletRequest,
				"com_liferay_portlet_configuration_web_portlet_" +
					"PortletConfigurationPortlet",
				ActionRequest.RENDER_PHASE)
		).setMVCPath(
			"/edit_permissions.jsp"
		).setRedirect(
			_portal.getCurrentURL(_renderRequest)
		).setParameter(
			"modelResource", SegmentsEntry.class.getName()
		).setParameter(
			"modelResourceDescription",
			segmentsEntry.getName(_themeDisplay.getLocale())
		).setParameter(
			"resourcePrimKey", segmentsEntry.getSegmentsEntryId()
		).setWindowState(
			LiferayWindowState.POP_UP
		).buildString();
	}

	public String getPreviewMembersURL(SegmentsEntry segmentsEntry) {
		return PortletURLBuilder.createRenderURL(
			_renderResponse
		).setMVCRenderCommandName(
			"/segments/preview_segments_entry_users"
		).setRedirect(
			_portal.getCurrentURL(_renderRequest)
		).setParameter(
			"clearSessionCriteria", true
		).setParameter(
			"segmentsEntryId", segmentsEntry.getSegmentsEntryId()
		).setWindowState(
			LiferayWindowState.POP_UP
		).buildString();
	}

	public SearchContainer<SegmentsEntry> getSearchContainer()
		throws PortalException {

		if (_searchContainer != null) {
			return _searchContainer;
		}

		SearchContainer<SegmentsEntry> searchContainer = new SearchContainer<>(
			_renderRequest, _getPortletURL(), null, "there-are-no-segments");

		searchContainer.setId("segmentsEntries");
		searchContainer.setOrderByCol(getOrderByCol());
		searchContainer.setOrderByComparator(_getOrderByComparator());
		searchContainer.setOrderByType(getOrderByType());

		if (_isSearch()) {
			searchContainer.setResultsAndTotal(
				_segmentsEntryService.searchSegmentsEntries(
					_themeDisplay.getCompanyId(),
					_themeDisplay.getScopeGroupId(), _getKeywords(), true,
					searchContainer.getStart(), searchContainer.getEnd(),
					_getSort()));
		}
		else {
			searchContainer.setResultsAndTotal(
				() -> _segmentsEntryService.getSegmentsEntries(
					_themeDisplay.getScopeGroupId(), true,
					searchContainer.getStart(), searchContainer.getEnd(),
					searchContainer.getOrderByComparator()),
				_segmentsEntryService.getSegmentsEntriesCount(
					_themeDisplay.getScopeGroupId(), true));
		}

		searchContainer.setRowChecker(
			new EmptyOnClickRowChecker(_renderResponse));

		_searchContainer = searchContainer;

		return _searchContainer;
	}

	public SegmentsConfigurationProvider getSegmentsConfigurationProvider() {
		return _segmentsConfigurationProvider;
	}

	public int getTotalItems() throws PortalException {
		SearchContainer<?> searchContainer = getSearchContainer();

		return searchContainer.getTotal();
	}

	public boolean isAsahEnabled(long companyId) throws Exception {
		AnalyticsSettingsManager analyticsSettingsManager =
			getAnalyticsSettingsManager();

		return analyticsSettingsManager.isAnalyticsEnabled(companyId);
	}

	public boolean isDisabledManagementBar() throws PortalException {
		if (_hasResults() || _isSearch()) {
			return false;
		}

		return true;
	}

	public boolean isRoleSegmentationEnabled(long companyId) {
		try {
			return _segmentsConfigurationProvider.isRoleSegmentationEnabled(
				companyId);
		}
		catch (ConfigurationException configurationException) {
			_log.error(configurationException);
		}

		return false;
	}

	public boolean isSegmentationEnabled(long companyId) {
		try {
			return _segmentsConfigurationProvider.isSegmentationEnabled(
				companyId);
		}
		catch (ConfigurationException configurationException) {
			_log.error(configurationException);
		}

		return false;
	}

	public boolean isShowAssignUserRolesAction(SegmentsEntry segmentsEntry) {
		try {
			Group group = _groupLocalService.getGroup(
				segmentsEntry.getGroupId());

			if (!group.isCompany() &&
				SegmentsEntryPermission.contains(
					_permissionChecker, segmentsEntry,
					ActionKeys.ASSIGN_USER_ROLES)) {

				return true;
			}

			return false;
		}
		catch (PortalException portalException) {
			_log.error(portalException);
		}

		return false;
	}

	public boolean isShowDeleteAction(SegmentsEntry segmentsEntry) {
		try {
			if ((segmentsEntry.getGroupId() ==
					_themeDisplay.getScopeGroupId()) &&
				SegmentsEntryPermission.contains(
					_permissionChecker, segmentsEntry, ActionKeys.DELETE)) {

				return true;
			}

			return false;
		}
		catch (PortalException portalException) {
			_log.error(portalException);
		}

		return false;
	}

	public boolean isShowPermissionAction(SegmentsEntry segmentsEntry) {
		try {
			return SegmentsEntryPermission.contains(
				_permissionChecker, segmentsEntry, ActionKeys.PERMISSIONS);
		}
		catch (PortalException portalException) {
			_log.error(portalException);
		}

		return false;
	}

	public boolean isShowUpdateAction(SegmentsEntry segmentsEntry) {
		try {
			return SegmentsEntryPermission.contains(
				_permissionChecker, segmentsEntry, ActionKeys.UPDATE);
		}
		catch (PortalException portalException) {
			_log.error(portalException);
		}

		return false;
	}

	public boolean isShowViewAction(SegmentsEntry segmentsEntry) {
		try {
			return SegmentsEntryPermission.contains(
				_permissionChecker, segmentsEntry, ActionKeys.VIEW);
		}
		catch (PortalException portalException) {
			_log.error(portalException);
		}

		return false;
	}

	private String[] _getExcludedRoleNames() {
		RoleTypeContributor roleTypeContributor =
			_roleTypeContributorProvider.getRoleTypeContributor(
				RoleConstants.TYPE_SITE);

		if (roleTypeContributor != null) {
			return roleTypeContributor.getExcludedRoleNames();
		}

		return new String[0];
	}

	private String _getKeywords() {
		if (_keywords != null) {
			return _keywords;
		}

		_keywords = ParamUtil.getString(_httpServletRequest, "keywords");

		return _keywords;
	}

	private OrderByComparator<SegmentsEntry> _getOrderByComparator() {
		boolean orderByAsc = false;

		if (Objects.equals(getOrderByType(), "asc")) {
			orderByAsc = true;
		}

		String orderByCol = getOrderByCol();

		OrderByComparator<SegmentsEntry> orderByComparator = null;

		if (orderByCol.equals("modified-date")) {
			orderByComparator = new SegmentsEntryModifiedDateComparator(
				orderByAsc);
		}
		else if (orderByCol.equals("name")) {
			orderByComparator = new SegmentsEntryNameComparator(orderByAsc);
		}

		return orderByComparator;
	}

	private PortletURL _getPortletURL() {
		return PortletURLBuilder.createRenderURL(
			_renderResponse
		).setMVCPath(
			"/view.jsp"
		).setKeywords(
			() -> {
				String keywords = _getKeywords();

				if (Validator.isNotNull(keywords)) {
					return keywords;
				}

				return null;
			}
		).setParameter(
			"displayStyle", getDisplayStyle()
		).setParameter(
			"orderByCol", getOrderByCol()
		).setParameter(
			"orderByType", getOrderByType()
		).buildPortletURL();
	}

	private Sort _getSort() {
		boolean orderByAsc = false;

		if (Objects.equals(getOrderByType(), "asc")) {
			orderByAsc = true;
		}

		Sort sort = null;

		if (Objects.equals(getOrderByCol(), "name")) {
			sort = new Sort(
				Field.getSortableFieldName(
					"localized_name_".concat(_themeDisplay.getLanguageId())),
				Sort.STRING_TYPE, !orderByAsc);
		}
		else {
			sort = new Sort(Field.MODIFIED_DATE, Sort.LONG_TYPE, !orderByAsc);
		}

		return sort;
	}

	private boolean _hasResults() throws PortalException {
		if (getTotalItems() > 0) {
			return true;
		}

		return false;
	}

	private boolean _isSearch() {
		if (Validator.isNotNull(_getKeywords())) {
			return true;
		}

		return false;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SegmentsDisplayContext.class);

	private final AnalyticsSettingsManager _analyticsSettingsManager;
	private String _displayStyle;
	private final GroupLocalService _groupLocalService;
	private final HttpServletRequest _httpServletRequest;
	private final ItemSelector _itemSelector;
	private String _keywords;
	private final Language _language;
	private String _orderByCol;
	private String _orderByType;
	private final PermissionChecker _permissionChecker;
	private final Portal _portal;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final RoleTypeContributorProvider _roleTypeContributorProvider;
	private SearchContainer<SegmentsEntry> _searchContainer;
	private final SegmentsConfigurationProvider _segmentsConfigurationProvider;
	private final SegmentsEntryService _segmentsEntryService;
	private final ThemeDisplay _themeDisplay;

}