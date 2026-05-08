/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.fragment.client.serdes.v1_0;

import com.liferay.headless.admin.fragment.client.dto.v1_0.Thumbnail;
import com.liferay.headless.admin.fragment.client.json.BaseJSONParser;

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
public class ThumbnailSerDes {

	public static Thumbnail toDTO(String json) {
		ThumbnailJSONParser thumbnailJSONParser = new ThumbnailJSONParser();

		return thumbnailJSONParser.parseToDTO(json);
	}

	public static Thumbnail[] toDTOs(String json) {
		ThumbnailJSONParser thumbnailJSONParser = new ThumbnailJSONParser();

		return thumbnailJSONParser.parseToDTOs(json);
	}

	public static String toJSON(Thumbnail thumbnail) {
		if (thumbnail == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (thumbnail.getExternalReferenceCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"externalReferenceCode\": ");

			sb.append("\"");

			sb.append(_escape(thumbnail.getExternalReferenceCode()));

			sb.append("\"");
		}

		if (thumbnail.getFileBase64() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"fileBase64\": ");

			sb.append("\"");

			sb.append(_escape(thumbnail.getFileBase64()));

			sb.append("\"");
		}

		if (thumbnail.getUrl() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"url\": ");

			sb.append("\"");

			sb.append(_escape(thumbnail.getUrl()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		ThumbnailJSONParser thumbnailJSONParser = new ThumbnailJSONParser();

		return thumbnailJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(Thumbnail thumbnail) {
		if (thumbnail == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (thumbnail.getExternalReferenceCode() == null) {
			map.put("externalReferenceCode", null);
		}
		else {
			map.put(
				"externalReferenceCode",
				String.valueOf(thumbnail.getExternalReferenceCode()));
		}

		if (thumbnail.getFileBase64() == null) {
			map.put("fileBase64", null);
		}
		else {
			map.put("fileBase64", String.valueOf(thumbnail.getFileBase64()));
		}

		if (thumbnail.getUrl() == null) {
			map.put("url", null);
		}
		else {
			map.put("url", String.valueOf(thumbnail.getUrl()));
		}

		return map;
	}

	public static class ThumbnailJSONParser extends BaseJSONParser<Thumbnail> {

		@Override
		protected Thumbnail createDTO() {
			return new Thumbnail();
		}

		@Override
		protected Thumbnail[] createDTOArray(int size) {
			return new Thumbnail[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "externalReferenceCode")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "fileBase64")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "url")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			Thumbnail thumbnail, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "externalReferenceCode")) {
				if (jsonParserFieldValue != null) {
					thumbnail.setExternalReferenceCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "fileBase64")) {
				if (jsonParserFieldValue != null) {
					thumbnail.setFileBase64((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "url")) {
				if (jsonParserFieldValue != null) {
					thumbnail.setUrl((String)jsonParserFieldValue);
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
// LIFERAY-REST-BUILDER-HASH:1920303204