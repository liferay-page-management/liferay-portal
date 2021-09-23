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

/**
 * @author Petteri Karttunen
 */
public class LongSXPParameter extends BaseSXPParameter {

	public LongSXPParameter(String name, boolean templateVariable, Long value) {
		super(name, templateVariable);

		_value = value;
	}

	@Override
	public boolean evaluateEquals(JSONObject jsonObject) {
		long value = jsonObject.getLong("value");

		if (_value.longValue() == value) {
			return true;
		}

		return false;
	}

	@Override
	public boolean evaluateGreaterThan(
		boolean closedRange, JSONObject jsonObject) {

		long value = jsonObject.getLong("value");

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
			if (_value.longValue() == jsonArray.getLong(i)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean evaluateInRange(JSONObject jsonObject) {
		JSONArray jsonArray = jsonObject.getJSONArray("value");

		long lowerBound = jsonArray.getLong(0);
		long upperBound = jsonArray.getLong(1);

		if ((_value.compareTo(lowerBound) >= 0) &&
			(_value.compareTo(upperBound) <= 0)) {

			return true;
		}

		return false;
	}

	@Override
	public Long getValue() {
		return _value;
	}

	private final Long _value;

}