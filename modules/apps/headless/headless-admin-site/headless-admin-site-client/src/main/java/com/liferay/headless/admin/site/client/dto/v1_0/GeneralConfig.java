/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.client.dto.v1_0;

import com.liferay.headless.admin.site.client.function.UnsafeSupplier;
import com.liferay.headless.admin.site.client.serdes.v1_0.GeneralConfigSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Map;
import java.util.Objects;

/**
 * @author Rubén Pulido
 * @generated
 */
@Generated("")
public class GeneralConfig implements Cloneable, Serializable {

	public static GeneralConfig toDTO(String json) {
		return GeneralConfigSerDes.toDTO(json);
	}

	public String getApplicationDecorator() {
		return applicationDecorator;
	}

	public void setApplicationDecorator(String applicationDecorator) {
		this.applicationDecorator = applicationDecorator;
	}

	public void setApplicationDecorator(
		UnsafeSupplier<String, Exception> applicationDecoratorUnsafeSupplier) {

		try {
			applicationDecorator = applicationDecoratorUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String applicationDecorator;

	public Map<String, String> getCustomTitle_i18n() {
		return customTitle_i18n;
	}

	public void setCustomTitle_i18n(Map<String, String> customTitle_i18n) {
		this.customTitle_i18n = customTitle_i18n;
	}

	public void setCustomTitle_i18n(
		UnsafeSupplier<Map<String, String>, Exception>
			customTitle_i18nUnsafeSupplier) {

		try {
			customTitle_i18n = customTitle_i18nUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Map<String, String> customTitle_i18n;

	public Boolean getUseCustomTitle() {
		return useCustomTitle;
	}

	public void setUseCustomTitle(Boolean useCustomTitle) {
		this.useCustomTitle = useCustomTitle;
	}

	public void setUseCustomTitle(
		UnsafeSupplier<Boolean, Exception> useCustomTitleUnsafeSupplier) {

		try {
			useCustomTitle = useCustomTitleUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Boolean useCustomTitle;

	@Override
	public GeneralConfig clone() throws CloneNotSupportedException {
		return (GeneralConfig)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof GeneralConfig)) {
			return false;
		}

		GeneralConfig generalConfig = (GeneralConfig)object;

		return Objects.equals(toString(), generalConfig.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return GeneralConfigSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:244313465