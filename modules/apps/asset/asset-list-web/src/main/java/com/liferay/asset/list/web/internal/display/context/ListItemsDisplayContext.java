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

package com.liferay.asset.list.web.internal.display.context;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.list.asset.entry.provider.AssetListAssetEntryProvider;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.asset.list.service.AssetListEntryLocalServiceUtil;
import com.liferay.asset.list.web.internal.constants.AssetListListTypeConstants;
import com.liferay.asset.list.web.internal.field.ListItemField;
import com.liferay.info.field.InfoFieldValue;
import com.liferay.info.item.InfoItemClassPKReference;
import com.liferay.info.item.InfoItemFieldValues;
import com.liferay.info.item.InfoItemServiceTracker;
import com.liferay.info.item.provider.InfoItemFieldValuesProvider;
import com.liferay.info.list.provider.DefaultInfoListProviderContext;
import com.liferay.info.list.provider.InfoListProvider;
import com.liferay.petra.reflect.GenericUtil;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.segments.constants.SegmentsEntryConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Jürgen Kappler
 */
public class ListItemsDisplayContext {

	public ListItemsDisplayContext(
		AssetListAssetEntryProvider assetListAssetEntryProvider,
		InfoItemServiceTracker infoItemServiceTracker,
		RenderRequest renderRequest, RenderResponse renderResponse) {

		_assetListAssetEntryProvider = assetListAssetEntryProvider;
		_infoItemServiceTracker = infoItemServiceTracker;
		_renderRequest = renderRequest;
		_renderResponse = renderResponse;

		_httpServletRequest = PortalUtil.getHttpServletRequest(renderRequest);

		_themeDisplay = (ThemeDisplay)_httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public AssetListEntry getAssetListEntry() {
		if (_assetListEntry != null) {
			return _assetListEntry;
		}

		_assetListEntry = AssetListEntryLocalServiceUtil.fetchAssetListEntry(
			getAssetListEntryId());

		return _assetListEntry;
	}

	public long getAssetListEntryId() {
		if (_assetListEntryId != null) {
			return _assetListEntryId;
		}

		_assetListEntryId = ParamUtil.getLong(
			_httpServletRequest, "assetListEntryId");

		return _assetListEntryId;
	}

	public String getListItemFieldType(ListItemField listItemField) {
		return ResourceActionsUtil.getModelResource(
			_themeDisplay.getLocale(), listItemField.getClassName());
	}

	public SearchContainer<ListItemField> getSearchContainer() {
		if (_searchContainer != null) {
			return _searchContainer;
		}

		SearchContainer<ListItemField> searchContainer = new SearchContainer(
			_renderRequest, _getListItemsURL(), null, "there-are-no-items");

		searchContainer.setResults(_getListItemFields(searchContainer));

		searchContainer.setTotal(_getListItemFieldsCount());

		_searchContainer = searchContainer;

		return _searchContainer;
	}

	public long getSegmentsEntryId() {
		if (_segmentsEntryId != null) {
			return _segmentsEntryId;
		}

		_segmentsEntryId = ParamUtil.getLong(
			_httpServletRequest, "segmentsEntryId",
			SegmentsEntryConstants.ID_DEFAULT);

		return _segmentsEntryId;
	}

	public boolean isShowActions() {
		if (_showActions != null) {
			return _showActions;
		}

		_showActions = ParamUtil.get(_renderRequest, "showActions", false);

		return _showActions;
	}

	private List<ListItemField> _getAssetListListItemFields(
		SearchContainer<ListItemField> searchContainer) {

		List<ListItemField> listItemFields = new ArrayList<>();

		List<AssetEntry> assetEntries =
			_assetListAssetEntryProvider.getAssetEntries(
				getAssetListEntry(), getSegmentsEntryId(),
				searchContainer.getStart(), searchContainer.getEnd());

		Locale locale = _themeDisplay.getLocale();

		for (AssetEntry assetEntry : assetEntries) {
			AssetRenderer<?> assetRenderer = assetEntry.getAssetRenderer();

			listItemFields.add(
				new ListItemField(
					assetRenderer.getTitle(locale),
					assetRenderer.getClassName(), assetEntry.getClassPK(),
					assetEntry.getUserName(), assetEntry.getModifiedDate(),
					assetEntry.getCreateDate()));
		}

		return listItemFields;
	}

	private String _getInfoListProviderKey() {
		if (Validator.isNotNull(_infoListProviderKey)) {
			return _infoListProviderKey;
		}

		_infoListProviderKey = ParamUtil.getString(
			_httpServletRequest, "infoListProviderKey");

		return _infoListProviderKey;
	}

	private List<ListItemField> _getInfoListProviderListItemFields(
		SearchContainer<ListItemField> searchContainer) {

		List<ListItemField> listItemFields = new ArrayList<>();

		InfoListProvider<?> infoListProvider =
			_infoItemServiceTracker.getInfoItemService(
				InfoListProvider.class, _getInfoListProviderKey());

		String className = GenericUtil.getGenericClassName(infoListProvider);

		InfoItemFieldValuesProvider<Object> infoItemFieldValuesProvider =
			_infoItemServiceTracker.getFirstInfoItemService(
				InfoItemFieldValuesProvider.class, className);

		DefaultInfoListProviderContext defaultInfoListProviderContext =
			new DefaultInfoListProviderContext(
				_themeDisplay.getScopeGroup(), _themeDisplay.getUser());

		List<Object> infoList = (List<Object>)infoListProvider.getInfoList(
			defaultInfoListProviderContext);

		List<Object> resultList = ListUtil.subList(
			infoList, searchContainer.getStart(), searchContainer.getEnd());

		for (Object result : resultList) {
			InfoItemFieldValues infoFormValues =
				infoItemFieldValuesProvider.getInfoItemFieldValues(result);

			InfoItemClassPKReference infoItemClassPKReference =
				infoFormValues.getInfoItemClassPKReference();

			InfoFieldValue<Object> title = infoFormValues.getInfoFieldValue(
				"title");
			InfoFieldValue<Object> userName = infoFormValues.getInfoFieldValue(
				"userName");

			long classPK = infoItemClassPKReference.getClassPK();
			String resultClassName = className;

			if (result instanceof AssetEntry) {
				AssetEntry assetEntry = (AssetEntry)result;

				classPK = assetEntry.getClassPK();
				resultClassName = assetEntry.getClassName();
			}

			listItemFields.add(
				new ListItemField(
					String.valueOf(title.getValue(_themeDisplay.getLocale())),
					resultClassName, classPK,
					String.valueOf(
						userName.getValue(_themeDisplay.getLocale())),
					null, null));
		}

		return listItemFields;
	}

	private List<ListItemField> _getListItemFields(
		SearchContainer<ListItemField> searchContainer) {

		if (_isListTypeAssetList()) {
			return _getAssetListListItemFields(searchContainer);
		}

		if (_isListTypeInfoListProvider()) {
			return _getInfoListProviderListItemFields(searchContainer);
		}

		return null;
	}

	private int _getListItemFieldsCount() {
		if (_isListTypeAssetList()) {
			return _assetListAssetEntryProvider.getAssetEntriesCount(
				getAssetListEntry(), getSegmentsEntryId());
		}

		if (_isListTypeInfoListProvider()) {
			InfoListProvider<?> infoListProvider =
				_infoItemServiceTracker.getInfoItemService(
					InfoListProvider.class, _getInfoListProviderKey());

			DefaultInfoListProviderContext defaultInfoListProviderContext =
				new DefaultInfoListProviderContext(
					_themeDisplay.getScopeGroup(), _themeDisplay.getUser());

			return infoListProvider.getInfoListCount(
				defaultInfoListProviderContext);
		}

		return 0;
	}

	private PortletURL _getListItemsURL() {
		PortletURL portletURL = _renderResponse.createRenderURL();

		portletURL.setParameter("mvcPath", "/view_list_items.jsp");
		portletURL.setParameter("redirect", _getRedirect());

		if (_isListTypeAssetList()) {
			portletURL.setParameter(
				"assetListEntryId", String.valueOf(getAssetListEntryId()));
			portletURL.setParameter(
				"segmentsEntryId", String.valueOf(getSegmentsEntryId()));
		}

		if (_isListTypeInfoListProvider()) {
			portletURL.setParameter(
				"infoListProviderKey", _getInfoListProviderKey());
		}

		portletURL.setParameter("listType", _getListType());

		return portletURL;
	}

	private String _getListType() {
		if (Validator.isNotNull(_listType)) {
			return _listType;
		}

		_listType = ParamUtil.get(
			_renderRequest, "listType",
			AssetListListTypeConstants.LIST_TYPE_ASSET_LIST);

		return _listType;
	}

	private String _getRedirect() {
		return ParamUtil.get(
			_renderRequest, "redirect", _themeDisplay.getURLCurrent());
	}

	private boolean _isListTypeAssetList() {
		if (Objects.equals(
				_getListType(),
				AssetListListTypeConstants.LIST_TYPE_ASSET_LIST)) {

			return true;
		}

		return false;
	}

	private boolean _isListTypeInfoListProvider() {
		if (Objects.equals(
				_getListType(),
				AssetListListTypeConstants.LIST_TYPE_INFO_LIST_PROVIDER)) {

			return true;
		}

		return false;
	}

	private final AssetListAssetEntryProvider _assetListAssetEntryProvider;
	private AssetListEntry _assetListEntry;
	private Long _assetListEntryId;
	private final HttpServletRequest _httpServletRequest;
	private final InfoItemServiceTracker _infoItemServiceTracker;
	private String _infoListProviderKey;
	private String _listType;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private SearchContainer<ListItemField> _searchContainer;
	private Long _segmentsEntryId;
	private Boolean _showActions;
	private final ThemeDisplay _themeDisplay;

}