/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.client.dto.v1_0;

import com.liferay.headless.admin.site.client.serdes.v1_0.WidgetPageTemplateNavigationSettingsSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Rubén Pulido
 * @generated
 */
@Generated("")
public class WidgetPageTemplateNavigationSettings
	extends NavigationSettings implements Cloneable, Serializable {

	public static WidgetPageTemplateNavigationSettings toDTO(String json) {
		return WidgetPageTemplateNavigationSettingsSerDes.toDTO(json);
	}

	@Override
	public WidgetPageTemplateNavigationSettings clone()
		throws CloneNotSupportedException {

		return (WidgetPageTemplateNavigationSettings)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof WidgetPageTemplateNavigationSettings)) {
			return false;
		}

		WidgetPageTemplateNavigationSettings
			widgetPageTemplateNavigationSettings =
				(WidgetPageTemplateNavigationSettings)object;

		return Objects.equals(
			toString(), widgetPageTemplateNavigationSettings.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return WidgetPageTemplateNavigationSettingsSerDes.toJSON(this);
	}

}