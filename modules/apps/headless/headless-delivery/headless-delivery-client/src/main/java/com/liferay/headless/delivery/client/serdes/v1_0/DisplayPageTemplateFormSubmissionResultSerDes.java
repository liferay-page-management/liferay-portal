/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.client.serdes.v1_0;

import com.liferay.headless.delivery.client.dto.v1_0.DisplayPageTemplateFormSubmissionResult;
import com.liferay.headless.delivery.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class DisplayPageTemplateFormSubmissionResultSerDes {

	public static DisplayPageTemplateFormSubmissionResult toDTO(String json) {
		DisplayPageTemplateFormSubmissionResultJSONParser
			displayPageTemplateFormSubmissionResultJSONParser =
				new DisplayPageTemplateFormSubmissionResultJSONParser();

		return displayPageTemplateFormSubmissionResultJSONParser.parseToDTO(
			json);
	}

	public static DisplayPageTemplateFormSubmissionResult[] toDTOs(
		String json) {

		DisplayPageTemplateFormSubmissionResultJSONParser
			displayPageTemplateFormSubmissionResultJSONParser =
				new DisplayPageTemplateFormSubmissionResultJSONParser();

		return displayPageTemplateFormSubmissionResultJSONParser.parseToDTOs(
			json);
	}

	public static String toJSON(
		DisplayPageTemplateFormSubmissionResult
			displayPageTemplateFormSubmissionResult) {

		if (displayPageTemplateFormSubmissionResult == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (displayPageTemplateFormSubmissionResult.getMapping() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"mapping\": ");

			sb.append(
				String.valueOf(
					displayPageTemplateFormSubmissionResult.getMapping()));
		}

		if (displayPageTemplateFormSubmissionResult.getNotificationText() !=
				null) {

			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"notificationText\": ");

			sb.append(
				String.valueOf(
					displayPageTemplateFormSubmissionResult.
						getNotificationText()));
		}

		if (displayPageTemplateFormSubmissionResult.getShowNotification() !=
				null) {

			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"showNotification\": ");

			sb.append(
				displayPageTemplateFormSubmissionResult.getShowNotification());
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		DisplayPageTemplateFormSubmissionResultJSONParser
			displayPageTemplateFormSubmissionResultJSONParser =
				new DisplayPageTemplateFormSubmissionResultJSONParser();

		return displayPageTemplateFormSubmissionResultJSONParser.parseToMap(
			json);
	}

	public static Map<String, String> toMap(
		DisplayPageTemplateFormSubmissionResult
			displayPageTemplateFormSubmissionResult) {

		if (displayPageTemplateFormSubmissionResult == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (displayPageTemplateFormSubmissionResult.getMapping() == null) {
			map.put("mapping", null);
		}
		else {
			map.put(
				"mapping",
				String.valueOf(
					displayPageTemplateFormSubmissionResult.getMapping()));
		}

		if (displayPageTemplateFormSubmissionResult.getNotificationText() ==
				null) {

			map.put("notificationText", null);
		}
		else {
			map.put(
				"notificationText",
				String.valueOf(
					displayPageTemplateFormSubmissionResult.
						getNotificationText()));
		}

		if (displayPageTemplateFormSubmissionResult.getShowNotification() ==
				null) {

			map.put("showNotification", null);
		}
		else {
			map.put(
				"showNotification",
				String.valueOf(
					displayPageTemplateFormSubmissionResult.
						getShowNotification()));
		}

		return map;
	}

	public static class DisplayPageTemplateFormSubmissionResultJSONParser
		extends BaseJSONParser<DisplayPageTemplateFormSubmissionResult> {

		@Override
		protected DisplayPageTemplateFormSubmissionResult createDTO() {
			return new DisplayPageTemplateFormSubmissionResult();
		}

		@Override
		protected DisplayPageTemplateFormSubmissionResult[] createDTOArray(
			int size) {

			return new DisplayPageTemplateFormSubmissionResult[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "mapping")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "notificationText")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "showNotification")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			DisplayPageTemplateFormSubmissionResult
				displayPageTemplateFormSubmissionResult,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "mapping")) {
				if (jsonParserFieldValue != null) {
					displayPageTemplateFormSubmissionResult.setMapping(
						MappingSerDes.toDTO((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "notificationText")) {
				if (jsonParserFieldValue != null) {
					displayPageTemplateFormSubmissionResult.setNotificationText(
						FragmentInlineValueSerDes.toDTO(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "showNotification")) {
				if (jsonParserFieldValue != null) {
					displayPageTemplateFormSubmissionResult.setShowNotification(
						(Boolean)jsonParserFieldValue);
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