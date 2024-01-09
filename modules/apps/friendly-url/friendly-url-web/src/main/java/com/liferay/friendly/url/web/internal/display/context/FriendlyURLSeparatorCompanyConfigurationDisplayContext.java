/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.friendly.url.web.internal.display.context;

import com.liferay.friendly.url.configuration.manager.FriendlyURLSeparatorConfigurationManager;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.FriendlyURLResolver;
import com.liferay.portal.kernel.portlet.FriendlyURLResolverRegistryUtil;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Mikel Lorza
 */
public class FriendlyURLSeparatorCompanyConfigurationDisplayContext {

	public FriendlyURLSeparatorCompanyConfigurationDisplayContext(
		FriendlyURLSeparatorConfigurationManager
			friendlyURLSeparatorConfigurationManager,
		JSONFactory jsonFactory, Language language, ThemeDisplay themeDisplay) {

		_friendlyURLSeparatorConfigurationManager =
			friendlyURLSeparatorConfigurationManager;
		_jsonFactory = jsonFactory;
		_language = language;
		_themeDisplay = themeDisplay;
	}

	public JSONArray getConfigurableFriendlyURLSeparatorsJSONArray() {
		JSONArray jsonArray = _jsonFactory.createJSONArray();

		List<FriendlyURLSeparator> friendlyURLSeparators = new ArrayList<>();

		JSONArray configuredURLSeparatorsJSONArray =
			_getConfiguredURLSeparatorsJSONArray(_themeDisplay.getCompanyId());

		for (FriendlyURLResolver friendlyURLResolver :
				FriendlyURLResolverRegistryUtil.
					getFriendlyURLResolversAsCollection()) {

			if (!friendlyURLResolver.isURLSeparatorConfigurable() ||
				StringPool.BLANK.equals(friendlyURLResolver.getType())) {

				continue;
			}

			friendlyURLSeparators.add(
				new FriendlyURLSeparator(
					_language.get(
						_themeDisplay.getLocale(),
						friendlyURLResolver.getType() + "-url-separator"),
					friendlyURLResolver.getType(),
					_getURLSeparator(
						configuredURLSeparatorsJSONArray,
						friendlyURLResolver.getDefaultURLSeparator(),
						friendlyURLResolver.getType())));
		}

		Collections.sort(
			friendlyURLSeparators,
			Comparator.comparing(FriendlyURLSeparator::getLabel));

		PortletDisplay portletDisplay = _themeDisplay.getPortletDisplay();

		for (FriendlyURLSeparator friendlyURLSeparator :
				friendlyURLSeparators) {

			jsonArray.put(
				JSONUtil.put(
					"label", friendlyURLSeparator.getLabel()
				).put(
					"name",
					portletDisplay.getNamespace() +
						friendlyURLSeparator.getType()
				).put(
					"value",
					() -> {
						String urlSeparator =
							friendlyURLSeparator.getUrlSeparator();

						if (urlSeparator.startsWith(StringPool.SLASH)) {
							urlSeparator = urlSeparator.substring(
								1, urlSeparator.length() - 1);
						}

						if (urlSeparator.endsWith(StringPool.SLASH)) {
							urlSeparator = urlSeparator.substring(
								0, urlSeparator.length() - 2);
						}

						return urlSeparator;
					}
				));
		}

		return jsonArray;
	}

	public Map<String, Object> getSeparatorFieldsProps() {
		return HashMapBuilder.<String, Object>put(
			"fields", getConfigurableFriendlyURLSeparatorsJSONArray()
		).put(
			"url", _themeDisplay.getPortalURL()
		).build();
	}

	private JSONArray _getConfiguredURLSeparatorsJSONArray(long companyId) {
		try {
			String urlSeparators =
				_friendlyURLSeparatorConfigurationManager.getURLSeparators(
					companyId);

			if (!Validator.isBlank(urlSeparators)) {
				return _jsonFactory.createJSONArray(urlSeparators);
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}

		return _jsonFactory.createJSONArray();
	}

	private String _getURLSeparator(
		JSONArray configuredURLSeparatorsJSONArray, String defaultURLSeparator,
		String type) {

		if (JSONUtil.isEmpty(configuredURLSeparatorsJSONArray)) {
			return defaultURLSeparator;
		}

		for (int i = 0; i < configuredURLSeparatorsJSONArray.length(); i++) {
			JSONObject jsonObject =
				configuredURLSeparatorsJSONArray.getJSONObject(i);

			if (Objects.equals(type, jsonObject.get("type"))) {
				return jsonObject.getString("urlSeparator");
			}
		}

		return StringPool.BLANK;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FriendlyURLSeparatorCompanyConfigurationDisplayContext.class.getName());

	private final FriendlyURLSeparatorConfigurationManager
		_friendlyURLSeparatorConfigurationManager;
	private final JSONFactory _jsonFactory;
	private final Language _language;
	private final ThemeDisplay _themeDisplay;

	private class FriendlyURLSeparator {

		public FriendlyURLSeparator(
			String label, String type, String urlSeparator) {

			_label = label;
			_type = type;
			_urlSeparator = urlSeparator;
		}

		public String getLabel() {
			return _label;
		}

		public String getType() {
			return _type;
		}

		public String getUrlSeparator() {
			return _urlSeparator;
		}

		private final String _label;
		private final String _type;
		private final String _urlSeparator;

	}

}