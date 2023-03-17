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

package com.liferay.frontend.taglib.internal.constants;

import java.util.Objects;

/**
 * @author Jürgen Kappler
 */
public enum FormNavigatorDisplayStyleType {

	TYPE_FIELDSET("field-set"), TYPE_SHEET("sheet");

	public static FormNavigatorDisplayStyleType parse(String value) {
		if (Objects.equals(TYPE_FIELDSET.getValue(), value)) {
			return TYPE_FIELDSET;
		}
		else if (Objects.equals(TYPE_SHEET.getValue(), value)) {
			return TYPE_SHEET;
		}

		throw new IllegalArgumentException("Invalid value " + value);
	}

	public String getValue() {
		return _value;
	}

	@Override
	public String toString() {
		return _value;
	}

	private FormNavigatorDisplayStyleType(String value) {
		_value = value;
	}

	private final String _value;

}