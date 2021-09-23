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

package com.liferay.search.experiences.blueprint.parameter;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public class IntegerArraySXPParameter extends BaseSXPParameter {

	public IntegerArraySXPParameter(
		String name, boolean templateVariable, Integer[] value) {

		super(name, templateVariable);

		_value = value;
	}

	@Override
	public boolean evaluateContains(JSONObject jsonObject) {
		Object value = jsonObject.get("value");

		if (value instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray)value;

			for (int i = 0; i < jsonArray.length(); i++) {
				if (ArrayUtil.contains(_value, jsonArray.getInt(i))) {
					return true;
				}
			}

			return false;
		}

		return ArrayUtil.contains(_value, GetterUtil.getInteger(value));
	}

	@Override
	public String evaluateTemplateVariable(Map<String, String> options) {
		return Arrays.toString(_value);
	}

	@Override
	public Integer[] getValue() {
		return _value;
	}

	private final Integer[] _value;

}