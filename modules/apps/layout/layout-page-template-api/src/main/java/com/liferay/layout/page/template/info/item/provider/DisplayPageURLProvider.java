/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.info.item.provider;

import com.liferay.info.item.InfoItemReference;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.theme.ThemeDisplay;

import java.util.Locale;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Rubén Pulido
 */
@ProviderType
public interface DisplayPageURLProvider {

	public String getDefaultURL(
			InfoItemReference infoItemReference, ThemeDisplay themeDisplay)
		throws PortalException;

	public String getURL(
			InfoItemReference infoItemReference,
			LayoutPageTemplateEntry layoutPageTemplateEntry, Locale locale,
			ThemeDisplay themeDisplay)
		throws PortalException;

}