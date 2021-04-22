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

package com.liferay.asset.publisher.web.internal.portlet.layout.contents.contributor;

import com.liferay.asset.publisher.constants.AssetPublisherPortletKeys;
import com.liferay.asset.publisher.web.internal.constants.AssetPublisherSelectionStyleConstants;
import com.liferay.layout.contents.contributor.LayoutContentsContributor;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.model.PortletPreferences;
import com.liferay.portal.kernel.portlet.PortletIdCodec;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.service.PortletPreferenceValueLocalService;
import com.liferay.portal.kernel.service.PortletPreferencesLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán
 */
@Component(service = LayoutContentsContributor.class)
public class AssetPublisherLayoutContentsContributor
	implements LayoutContentsContributor {

	@Override
	public JSONArray getLayoutContentsJSONArray(
		HttpServletRequest httpServletRequest, long plid) {

		List<PortletPreferences> portletPreferences =
			_portletPreferencesLocalService.getPortletPreferences(
				PortletKeys.PREFS_OWNER_ID_DEFAULT,
				PortletKeys.PREFS_OWNER_TYPE_LAYOUT, plid);

		Stream<PortletPreferences> stream = portletPreferences.stream();

		return stream.filter(
			portletPreference -> Objects.equals(
				PortletIdCodec.decodePortletName(
					portletPreference.getPortletId()),
				AssetPublisherPortletKeys.ASSET_PUBLISHER)
		).map(
			portletPreference -> _getLayoutContentJSONObject(
				httpServletRequest, portletPreference)
		).filter(
			Objects::nonNull
		).collect(
			Collector.of(
				JSONFactoryUtil::createJSONArray, JSONArray::put,
				JSONArray::put)
		);
	}

	private String _getDynamicSubtype(
		HttpServletRequest httpServletRequest,
		javax.portlet.PortletPreferences jxPortletPreferences) {

		boolean anyAssetType = GetterUtil.getBoolean(
			jxPortletPreferences.getValue("anyAssetType", null));

		if (anyAssetType) {
			return LanguageUtil.get(httpServletRequest, "any");
		}

		String[] classNameIds = jxPortletPreferences.getValues(
			"classNameIds", null);

		if (ArrayUtil.isEmpty(classNameIds)) {
			return LanguageUtil.get(httpServletRequest, "dynamic");
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return Stream.of(
			classNameIds
		).map(
			classNameId -> ResourceActionsUtil.getModelResource(
				themeDisplay.getLocale(),
				_portal.getClassName(GetterUtil.getLong(classNameId)))
		).collect(
			Collectors.joining(StringPool.COMMA + StringPool.SPACE)
		);
	}

	private JSONObject _getLayoutContentJSONObject(
		HttpServletRequest httpServletRequest,
		PortletPreferences portletPreferences) {

		javax.portlet.PortletPreferences jxPortletPreferences =
			_portletPreferenceValueLocalService.getPreferences(
				portletPreferences);

		String selectionStyle = jxPortletPreferences.getValue(
			"selectionStyle", StringPool.BLANK);

		if (Validator.isNull(selectionStyle)) {
			return null;
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		JSONObject jsonObject = JSONUtil.put(
			"className", Portlet.class
		).put(
			"classNameId", _portal.getClassNameId(Portlet.class)
		).put(
			"classPK", portletPreferences.getPortletId()
		).put(
			"title",
			_portal.getPortletTitle(
				AssetPublisherPortletKeys.ASSET_PUBLISHER,
				themeDisplay.getLocale())
		).put(
			"type",
			_portal.getPortletTitle(
				AssetPublisherPortletKeys.ASSET_PUBLISHER,
				themeDisplay.getLocale())
		);

		if (Objects.equals(
				selectionStyle,
				AssetPublisherSelectionStyleConstants.TYPE_MANUAL)) {

			return jsonObject.put(
				"subtype", LanguageUtil.get(httpServletRequest, "asset"));
		}

		if (Objects.equals(
				selectionStyle,
				AssetPublisherSelectionStyleConstants.TYPE_DYNAMIC)) {

			return jsonObject.put(
				"subtype",
				_getDynamicSubtype(httpServletRequest, jxPortletPreferences));
		}

		return jsonObject;
	}

	@Reference
	private Portal _portal;

	@Reference
	private PortletPreferencesLocalService _portletPreferencesLocalService;

	@Reference
	private PortletPreferenceValueLocalService
		_portletPreferenceValueLocalService;

}