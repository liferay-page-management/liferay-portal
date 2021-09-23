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
import com.liferay.portal.kernel.util.GetterUtil;

/**
 * @author Petteri Karttunen
 */
public class FloatSXPParameter extends BaseSXPParameter {

	public FloatSXPParameter(
		String name, boolean templateVariable, Float value) {

		super(name, templateVariable);

		_value = value;
	}

	@Override
	public boolean evaluateEquals(JSONObject jsonObject) {
		float value = GetterUtil.getFloat(jsonObject.get("value"));

		if (_value.floatValue() == value) {
			return true;
		}

		return false;
	}

	@Override
	public boolean evaluateGreaterThan(
		boolean closedRange, JSONObject jsonObject) {

		float value = GetterUtil.getFloat(jsonObject.get("value"));

		if (closedRange) {
			if (_value.compareTo(value) >= 0) {
				return true;
			}

			return false;
		}
		else if (_value.compareTo(value) > 0) {
			return true;
		}

		return false;
	}

	@Override
	public boolean evaluateIn(JSONObject jsonObject) {
		JSONArray jsonArray = jsonObject.getJSONArray("value");

		for (int i = 0; i < jsonArray.length(); i++) {
			if (_value.floatValue() == GetterUtil.getFloat(
					jsonArray.getString(i))) {

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean evaluateInRange(JSONObject jsonObject) {
		JSONArray jsonArray = jsonObject.getJSONArray("value");

		float lowerBound = GetterUtil.getFloat(jsonArray.getString(0));
		float upperBound = GetterUtil.getFloat(jsonArray.getString(1));

		if ((_value.compareTo(lowerBound) >= 0) &&
			(_value.compareTo(upperBound) <= 0)) {

			return true;
		}

		return false;
	}

	@Override
	public Float getValue() {
		return _value;
	}

	private final Float _value;

}