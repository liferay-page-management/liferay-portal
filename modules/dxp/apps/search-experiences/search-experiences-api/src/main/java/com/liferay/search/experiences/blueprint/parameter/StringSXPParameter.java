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

import java.util.Objects;

/**
 * @author Petteri Karttunen
 */
public class StringSXPParameter extends BaseSXPParameter {

	public StringSXPParameter(
		String name, boolean templateVariable, String value) {

		super(name, templateVariable);

		_value = value;
	}

	@Override
	public boolean evaluateEquals(JSONObject jsonObject) {
		String value = jsonObject.getString("value");

		return Objects.equals(_value, value);
	}

	@Override
	public boolean evaluateIn(JSONObject jsonObject) {
		JSONArray jsonArray = jsonObject.getJSONArray("value");

		for (int i = 0; i < jsonArray.length(); i++) {
			if (Objects.equals(_value, jsonArray.getString(i))) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String getValue() {
		return _value;
	}

	private final String _value;

}