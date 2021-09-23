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

import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author Petteri Karttunen
 */
public class DateSXPParameter extends BaseSXPParameter {

	public DateSXPParameter(String name, boolean templateVariable, Date value) {
		super(name, templateVariable);

		_value = value;
	}

	@Override
	public boolean evaluateEquals(JSONObject jsonObject) {
		DateFormat dateFormat = new SimpleDateFormat(
			jsonObject.getString("date_format"));

		return Objects.equals(
			dateFormat.format(_value), jsonObject.getString("value"));
	}

	@Override
	public boolean evaluateGreaterThan(
		boolean closedRange, JSONObject jsonObject) {

		try {
			DateFormat dateFormat = new SimpleDateFormat(
				jsonObject.getString("date_format"));

			return _value.after(
				dateFormat.parse(jsonObject.getString("value")));
		}
		catch (ParseException parseException) {
			return ReflectionUtil.throwException(parseException);
		}
	}

	@Override
	public boolean evaluateInRange(JSONObject jsonObject) {
		try {
			DateFormat dateFormat = new SimpleDateFormat(
				jsonObject.getString("date_format"));

			JSONArray jsonArray = jsonObject.getJSONArray("value");

			Date lowerBoundDate = dateFormat.parse(jsonArray.getString(0));
			Date upperBoundDate = dateFormat.parse(jsonArray.getString(1));

			if (_value.after(lowerBoundDate) && _value.before(upperBoundDate)) {
				return true;
			}

			return false;
		}
		catch (ParseException parseException) {
			return ReflectionUtil.throwException(parseException);
		}
	}

	@Override
	public String evaluateTemplateVariable(Map<String, String> options) {
		if ((options == null) || (options.get("date_format") == null)) {
			return _value.toString();
		}

		Date date = _value;

		if (options.containsKey("modifier")) {
			date = _modify(date, options.get("modifier"));
		}

		String dateFormatString = options.get("date_format");

		if (dateFormatString.equals("timestamp")) {
			return String.valueOf(date.getTime());
		}

		DateFormat dateFormat = new SimpleDateFormat(dateFormatString);

		return dateFormat.format(date);
	}

	@Override
	public Date getValue() {
		return _value;
	}

	private Date _modify(Date date, String option) {
		if (Validator.isBlank(option) ||
			!option.matches("^[\\+|\\-][0-9]+[h|d|w|M|y]")) {

			return date;
		}

		char operator = option.charAt(0);

		char unit = option.charAt(option.length() - 1);

		option = option.replaceAll("\\D+", "");

		long amount = GetterUtil.getLong(option);

		if (operator == '-') {
			amount *= -1;
		}

		Instant instant = date.toInstant();

		ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());

		LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();

		if (unit == 'h') {
			localDateTime = localDateTime.plusHours(amount);
		}
		else if (unit == 'd') {
			localDateTime = localDateTime.plusDays(amount);
		}
		else if (unit == 'w') {
			localDateTime = localDateTime.plusWeeks(amount);
		}
		else if (unit == 'M') {
			localDateTime = localDateTime.plusMonths(amount);
		}
		else if (unit == 'y') {
			localDateTime = localDateTime.plusYears(amount);
		}

		zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());

		return Date.from(zonedDateTime.toInstant());
	}

	private final Date _value;

}