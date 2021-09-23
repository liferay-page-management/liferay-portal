/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.search.experiences.internal.blueprint.parser;

import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.search.experiences.blueprint.parameter.DateSXPParameter;
import com.liferay.search.experiences.blueprint.parameter.SXPParameter;
import com.liferay.search.experiences.blueprint.parameter.SXPParameterData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Petteri Karttunen
 */
public class SXPTemplateVariableParser {

	public static JSONArray parse(
		JSONArray jsonArray, SXPParameterData sxpParameterData) {

		if (jsonArray == null) {
			return null;
		}

		Set<SXPParameter> sxpParameters = sxpParameterData.getSXPParameters();

		if (sxpParameters.isEmpty()) {
			return jsonArray;
		}

		String json = jsonArray.toString();

		if (!json.contains("${")) {
			return jsonArray;
		}

		json = _parse(json, sxpParameterData);

		if (!Validator.isBlank(json)) {
			try {
				return JSONFactoryUtil.createJSONArray(json);
			}
			catch (JSONException jsonException) {
				return ReflectionUtil.throwException(jsonException);
			}
		}

		return null;
	}

	public static JSONObject parse(
		JSONObject jsonObject, SXPParameterData sxpParameterData) {

		if (jsonObject == null) {
			return null;
		}

		Set<SXPParameter> sxpParameters = sxpParameterData.getSXPParameters();

		if (sxpParameters.isEmpty()) {
			return jsonObject;
		}

		String json = jsonObject.toString();

		if (!json.contains("${")) {
			return jsonObject;
		}

		json = _parse(json, sxpParameterData);

		if (!Validator.isBlank(json)) {
			try {
				return JSONFactoryUtil.createJSONObject(json);
			}
			catch (JSONException jsonException) {
				return ReflectionUtil.throwException(jsonException);
			}
		}

		return null;
	}

	public static Object parse(
		Object object, SXPParameterData sxpParameterData) {

		if (object instanceof JSONArray) {
			return parse((JSONArray)object, sxpParameterData);
		}
		else if (object instanceof JSONObject) {
			return parse((JSONObject)object, sxpParameterData);
		}

		return object;
	}

	private static Map<String, String> _getOptions(String optionsString) {
		Map<String, String> options = new HashMap<>();

		if (Validator.isBlank(optionsString)) {
			return options;
		}

		for (String string : optionsString.split(",")) {
			String[] array = string.split("=");

			if (array.length == 1) {
				options.put(array[0], null);
			}
			else {
				options.put(array[0], array[1]);
			}
		}

		return options;
	}

	private static String _parse(String json, SXPParameter sxpParameter) {
		String templateVariable = sxpParameter.getTemplateVariable();

		String parameterizedTemplateVariable =
			templateVariable.substring(0, templateVariable.length() - 1) + "|";

		if (json.contains(parameterizedTemplateVariable) &&
			(sxpParameter instanceof DateSXPParameter)) {

			int index = json.indexOf(parameterizedTemplateVariable);

			while (index >= 0) {
				String optionsString = json.substring(
					index + parameterizedTemplateVariable.length(),
					json.indexOf("}", index));

				String evaluatedTemplateVariable =
					sxpParameter.evaluateTemplateVariable(
						_getOptions(optionsString));

				json = _replace(
					json,
					StringBundler.concat(
						parameterizedTemplateVariable, optionsString, "}"),
					evaluatedTemplateVariable);

				index = json.indexOf(parameterizedTemplateVariable, index);
			}
		}

		if (json.contains(templateVariable)) {
			String evaluatedTemplateVariable =
				sxpParameter.evaluateTemplateVariable(null);

			json = _replace(json, templateVariable, evaluatedTemplateVariable);
		}

		return json;
	}

	private static String _parse(
		String json, SXPParameterData sxpParameterData) {

		for (SXPParameter sxpParameter : sxpParameterData.getSXPParameters()) {
			if (!sxpParameter.isTemplateVariable()) {
				continue;
			}

			json = _parse(json, sxpParameter);

			if (!json.contains("${")) {
				break;
			}
		}

		return json;
	}

	private static String _replace(
		String json, String templateVariable,
		String evaluatedTemplateVariable) {

		if (evaluatedTemplateVariable.startsWith("[")) {
			return StringUtil.replace(
				json, StringUtil.quote(templateVariable, CharPool.QUOTE),
				evaluatedTemplateVariable);
		}

		return StringUtil.replace(
			json, templateVariable, evaluatedTemplateVariable);
	}

}