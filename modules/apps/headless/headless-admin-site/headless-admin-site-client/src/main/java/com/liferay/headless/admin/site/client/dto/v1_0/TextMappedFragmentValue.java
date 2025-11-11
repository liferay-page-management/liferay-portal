/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.client.dto.v1_0;

import com.liferay.headless.admin.site.client.function.UnsafeSupplier;
import com.liferay.headless.admin.site.client.serdes.v1_0.TextMappedFragmentValueSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Rubén Pulido
 * @generated
 */
@Generated("")
public class TextMappedFragmentValue
	extends TextFragmentValue implements Cloneable, Serializable {

	public static TextMappedFragmentValue toDTO(String json) {
		return TextMappedFragmentValueSerDes.toDTO(json);
	}

	public FragmentMappedValue getMappedValue() {
		return mappedValue;
	}

	public void setMappedValue(FragmentMappedValue mappedValue) {
		this.mappedValue = mappedValue;
	}

	public void setMappedValue(
		UnsafeSupplier<FragmentMappedValue, Exception>
			mappedValueUnsafeSupplier) {

		try {
			mappedValue = mappedValueUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected FragmentMappedValue mappedValue;

	@Override
	public TextMappedFragmentValue clone() throws CloneNotSupportedException {
		return (TextMappedFragmentValue)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof TextMappedFragmentValue)) {
			return false;
		}

		TextMappedFragmentValue textMappedFragmentValue =
			(TextMappedFragmentValue)object;

		return Objects.equals(toString(), textMappedFragmentValue.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return TextMappedFragmentValueSerDes.toJSON(this);
	}

}