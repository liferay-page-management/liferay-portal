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

package com.liferay.content.dashboard.web.internal.display.context;

import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.KeyValuePair;
import com.liferay.portal.kernel.util.KeyValuePairComparator;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.portlet.ActionRequest;
import javax.portlet.ActionURL;
import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Eudaldo Alonso
 */
public class ContentDashboardAdminConfigurationDisplayContext {

	public ContentDashboardAdminConfigurationDisplayContext(
		AssetVocabularyLocalService assetVocabularyLocalService,
		String[] assetVocabularyNames, HttpServletRequest httpServletRequest,
		RenderResponse renderResponse) {

		_assetVocabularyLocalService = assetVocabularyLocalService;
		_assetVocabularyNames = assetVocabularyNames;
		_renderResponse = renderResponse;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public ActionURL getActionURL() {
		ActionURL actionURL = _renderResponse.createActionURL();

		actionURL.setParameter(
			ActionRequest.ACTION_NAME,
			"/update_content_dashboard_configuration");
		actionURL.setParameter("redirect", String.valueOf(getRedirect()));

		return actionURL;
	}

	public List<KeyValuePair> getAvailableVocabularyNames() {
		String[] assetVocabularyNames = ArrayUtil.clone(_assetVocabularyNames);

		Arrays.sort(assetVocabularyNames);

		Set<String> availableAssetVocabularyNamesSet = SetUtil.fromArray(
			_getAvailableAssetVocabularyNames());

		Stream<String> stream = availableAssetVocabularyNamesSet.stream();

		return stream.filter(
			assetVocabularyName ->
				Arrays.binarySearch(assetVocabularyNames, assetVocabularyName) <
					0
		).map(
			assetVocabularyName ->
				_assetVocabularyLocalService.fetchGroupVocabulary(
					_themeDisplay.getCompanyGroupId(), assetVocabularyName)
		).filter(
			Objects::nonNull
		).map(
			this::_toKeyValuePair
		).sorted(
			new KeyValuePairComparator(false, true)
		).collect(
			Collectors.toList()
		);
	}

	public List<KeyValuePair> getCurrentVocabularyNames() {
		return Stream.of(
			_assetVocabularyNames
		).map(
			assetVocabularyName ->
				_assetVocabularyLocalService.fetchGroupVocabulary(
					_themeDisplay.getCompanyGroupId(), assetVocabularyName)
		).filter(
			Objects::nonNull
		).map(
			this::_toKeyValuePair
		).collect(
			Collectors.toList()
		);
	}

	public PortletURL getRedirect() {
		PortletURL portletURL = _renderResponse.createRenderURL();

		portletURL.setParameter(
			"mvcRenderCommandName", "/edit_content_dashboard_configuration");

		return portletURL;
	}

	private List<AssetVocabulary> _getAssetVocabularies() {
		if (_assetVocabularies != null) {
			return _assetVocabularies;
		}

		_assetVocabularies = _assetVocabularyLocalService.getGroupVocabularies(
			new long[] {_themeDisplay.getCompanyGroupId()});

		return _assetVocabularies;
	}

	private String[] _getAvailableAssetVocabularyNames() {
		if (_availableAssetVocabularyNames != null) {
			return _availableAssetVocabularyNames;
		}

		List<AssetVocabulary> assetVocabularies = _getAssetVocabularies();

		_availableAssetVocabularyNames = new String[assetVocabularies.size()];

		for (int i = 0; i < assetVocabularies.size(); i++) {
			AssetVocabulary assetVocabulary = assetVocabularies.get(i);

			_availableAssetVocabularyNames[i] = assetVocabulary.getName();
		}

		return _availableAssetVocabularyNames;
	}

	private KeyValuePair _toKeyValuePair(AssetVocabulary assetVocabulary) {
		return new KeyValuePair(
			assetVocabulary.getName(),
			HtmlUtil.escape(
				assetVocabulary.getTitle(_themeDisplay.getLanguageId())));
	}

	private List<AssetVocabulary> _assetVocabularies;
	private final AssetVocabularyLocalService _assetVocabularyLocalService;
	private final String[] _assetVocabularyNames;
	private String[] _availableAssetVocabularyNames;
	private final RenderResponse _renderResponse;
	private final ThemeDisplay _themeDisplay;

}