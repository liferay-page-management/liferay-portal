/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.jsoup.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Balázs Sáfrány-Kovalik
 */
@ExtendedObjectClassDefinition(category = "infrastructure")
@Meta.OCD(
	id = "com.liferay.portal.jsoup.configuration.JsoupConfiguration",
	localization = "content/Language", name = "jsoup-configuration-name"
)
public interface JsoupConfiguration {

	@Meta.AD(
		deflt = "false", description = "legacy-self-closing-tag-parsing-help",
		name = "legacy-self-closing-tag-parsing", required = false
	)
	public boolean legacySelfClosingTagParsing();

}