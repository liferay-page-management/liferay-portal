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

import java.math.BigDecimal;

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

	public BigDecimal getMaxValue() {
		return _builder._maxValue;
	}

	public BigDecimal getMinValue() {
		return _builder._minValue;
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

		public Builder maxValue(BigDecimal maxValue) {
			_maxValue = maxValue;

			return this;
		}

		public Builder minValue(BigDecimal minValue) {
			_minValue = minValue;

			return this;
		}

		private Integer _decimalPartMaxLength;
		private BigDecimal _maxValue;
		private BigDecimal _minValue;

	}

	private NumberInfoFieldType(Builder builder) {
		_builder = builder;
	}

	private final Builder _builder;

}