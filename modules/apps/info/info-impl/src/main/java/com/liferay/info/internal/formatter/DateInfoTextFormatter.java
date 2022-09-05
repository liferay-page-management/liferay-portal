/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.info.internal.formatter;

import com.liferay.info.formatter.InfoTextFormatter;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Jorge Ferrer
 */
@Component(service = InfoTextFormatter.class)
public class DateInfoTextFormatter implements InfoTextFormatter<Date> {

	@Override
	public String format(Date date, Locale locale) {
		Format dateFormatDateTime = FastDateFormatFactoryUtil.getDateTime(
			locale);

		return dateFormatDateTime.format(date);
	}

	@Override
	public String formatWithOptions(
		Date date, Map<String, Object> formatOptions, Locale locale) {

		Object dateFormat = formatOptions.get("dateFormat");

		DateFormat dateFormatPattern = new SimpleDateFormat(
			dateFormat.toString());

		return dateFormatPattern.format(date);
	}

}