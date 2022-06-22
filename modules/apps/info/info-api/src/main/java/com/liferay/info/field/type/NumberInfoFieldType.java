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

package com.liferay.info.field.type;

/**
 * @author Alejandro Tardín
 */
public class NumberInfoFieldType implements InfoFieldType {

	public static final Attribute<NumberInfoFieldType, Boolean> DECIMAL =
		new Attribute<>();

	public static final NumberInfoFieldType INSTANCE = new Builder().build();

	public static Builder builder() {
		return new Builder();
	}

	public Integer getDecimalPartMaxLength() {
		return _builder._decimalPartMaxLength;
	}

	public Long getIntegerPartMaxValue() {
		return _builder._integerPartMaxValue;
	}

	public Long getIntegerPartMinValue() {
		return _builder._integerPartMinValue;
	}

	@Override
	public String getName() {
		return "number";
	}

	public static class Builder {

		public NumberInfoFieldType build() {
			return new NumberInfoFieldType(this);
		}

		public Builder decimalPartMaxLength(int decimalPartMaxLength) {
			_decimalPartMaxLength = decimalPartMaxLength;

			return this;
		}

		public Builder integerPartMaxValue(long integerPartMaxValue) {
			_integerPartMaxValue = integerPartMaxValue;

			return this;
		}

		public Builder integerPartMinValue(long integerPartMinValue) {
			_integerPartMinValue = integerPartMinValue;

			return this;
		}

		private Integer _decimalPartMaxLength;
		private Long _integerPartMaxValue;
		private Long _integerPartMinValue;

	}

	private NumberInfoFieldType(Builder builder) {
		_builder = builder;
	}

	private final Builder _builder;

}