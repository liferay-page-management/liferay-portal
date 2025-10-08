/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.client.serdes.v1_0;

import com.liferay.headless.admin.site.client.dto.v1_0.WidgetPageTemplateNavigationSettings;
import com.liferay.headless.admin.site.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Rubén Pulido
 * @generated
 */
@Generated("")
public class WidgetPageTemplateNavigationSettingsSerDes {

	public static WidgetPageTemplateNavigationSettings toDTO(String json) {
		WidgetPageTemplateNavigationSettingsJSONParser
			widgetPageTemplateNavigationSettingsJSONParser =
				new WidgetPageTemplateNavigationSettingsJSONParser();

		return widgetPageTemplateNavigationSettingsJSONParser.parseToDTO(json);
	}

	public static WidgetPageTemplateNavigationSettings[] toDTOs(String json) {
		WidgetPageTemplateNavigationSettingsJSONParser
			widgetPageTemplateNavigationSettingsJSONParser =
				new WidgetPageTemplateNavigationSettingsJSONParser();

		return widgetPageTemplateNavigationSettingsJSONParser.parseToDTOs(json);
	}

	public static String toJSON(
		WidgetPageTemplateNavigationSettings
			widgetPageTemplateNavigationSettings) {

		if (widgetPageTemplateNavigationSettings == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (widgetPageTemplateNavigationSettings.getNavigationSettingsType() !=
				null) {

			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"navigationSettingsType\": ");

			sb.append("\"");

			sb.append(
				widgetPageTemplateNavigationSettings.
					getNavigationSettingsType());

			sb.append("\"");
		}

		if (widgetPageTemplateNavigationSettings.getTarget() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"target\": ");

			sb.append("\"");

			sb.append(
				_escape(widgetPageTemplateNavigationSettings.getTarget()));

			sb.append("\"");
		}

		if (widgetPageTemplateNavigationSettings.getTargetType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"targetType\": ");

			sb.append("\"");

			sb.append(widgetPageTemplateNavigationSettings.getTargetType());

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		WidgetPageTemplateNavigationSettingsJSONParser
			widgetPageTemplateNavigationSettingsJSONParser =
				new WidgetPageTemplateNavigationSettingsJSONParser();

		return widgetPageTemplateNavigationSettingsJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		WidgetPageTemplateNavigationSettings
			widgetPageTemplateNavigationSettings) {

		if (widgetPageTemplateNavigationSettings == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (widgetPageTemplateNavigationSettings.getNavigationSettingsType() ==
				null) {

			map.put("navigationSettingsType", null);
		}
		else {
			map.put(
				"navigationSettingsType",
				String.valueOf(
					widgetPageTemplateNavigationSettings.
						getNavigationSettingsType()));
		}

		if (widgetPageTemplateNavigationSettings.getTarget() == null) {
			map.put("target", null);
		}
		else {
			map.put(
				"target",
				String.valueOf(
					widgetPageTemplateNavigationSettings.getTarget()));
		}

		if (widgetPageTemplateNavigationSettings.getTargetType() == null) {
			map.put("targetType", null);
		}
		else {
			map.put(
				"targetType",
				String.valueOf(
					widgetPageTemplateNavigationSettings.getTargetType()));
		}

		return map;
	}

	public static class WidgetPageTemplateNavigationSettingsJSONParser
		extends BaseJSONParser<WidgetPageTemplateNavigationSettings> {

		@Override
		protected WidgetPageTemplateNavigationSettings createDTO() {
			return new WidgetPageTemplateNavigationSettings();
		}

		@Override
		protected WidgetPageTemplateNavigationSettings[] createDTOArray(
			int size) {

			return new WidgetPageTemplateNavigationSettings[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "navigationSettingsType")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "target")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "targetType")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			WidgetPageTemplateNavigationSettings
				widgetPageTemplateNavigationSettings,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "navigationSettingsType")) {
				if (jsonParserFieldValue != null) {
					widgetPageTemplateNavigationSettings.
						setNavigationSettingsType(
							WidgetPageTemplateNavigationSettings.
								NavigationSettingsType.create(
									(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "target")) {
				if (jsonParserFieldValue != null) {
					widgetPageTemplateNavigationSettings.setTarget(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "targetType")) {
				if (jsonParserFieldValue != null) {
					widgetPageTemplateNavigationSettings.setTargetType(
						WidgetPageTemplateNavigationSettings.TargetType.create(
							(String)jsonParserFieldValue));
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			sb.append(_toJSON(value));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static String _toJSON(Object value) {
		if (value == null) {
			return "null";
		}

		if (value instanceof Map) {
			return _toJSON((Map)value);
		}

		Class<?> clazz = value.getClass();

		if (clazz.isArray()) {
			StringBuilder sb = new StringBuilder("[");

			Object[] values = (Object[])value;

			for (int i = 0; i < values.length; i++) {
				sb.append(_toJSON(values[i]));

				if ((i + 1) < values.length) {
					sb.append(", ");
				}
			}

			sb.append("]");

			return sb.toString();
		}

		if (value instanceof String) {
			return "\"" + _escape(value) + "\"";
		}

		return String.valueOf(value);
	}

}