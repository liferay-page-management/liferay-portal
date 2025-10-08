/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.client.dto.v1_0;

import com.liferay.headless.admin.site.client.function.UnsafeSupplier;
import com.liferay.headless.admin.site.client.serdes.v1_0.SitePageNavigationSettingsSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Rubén Pulido
 * @generated
 */
@Generated("")
public class SitePageNavigationSettings
	extends NavigationSettings implements Cloneable, Serializable {

	public static SitePageNavigationSettings toDTO(String json) {
		return SitePageNavigationSettingsSerDes.toDTO(json);
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public void setQueryString(
		UnsafeSupplier<String, Exception> queryStringUnsafeSupplier) {

		try {
			queryString = queryStringUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String queryString;

	@Override
	public SitePageNavigationSettings clone()
		throws CloneNotSupportedException {

		return (SitePageNavigationSettings)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof SitePageNavigationSettings)) {
			return false;
		}

		SitePageNavigationSettings sitePageNavigationSettings =
			(SitePageNavigationSettings)object;

		return Objects.equals(
			toString(), sitePageNavigationSettings.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return SitePageNavigationSettingsSerDes.toJSON(this);
	}

}