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

package com.liferay.layout.internal.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Michael Bowerman
 */
@ExtendedObjectClassDefinition(
	category = "pages", scope = ExtendedObjectClassDefinition.Scope.COMPANY
)
@Meta.OCD(
	id = "com.liferay.layout.internal.configuration.LayoutCrawlerConfiguration",
	localization = "content/Language",
	name = "layout-crawler-configuration-name"
)
public interface LayoutCrawlerConfiguration {

	@Meta.AD(deflt = "", name = "hostname", required = false)
	public String hostname();

	@Meta.AD(deflt = "0", name = "port", required = false)
	public int port();

	@Meta.AD(
		deflt = "DEFAULT", name = "connection-protocol",
		optionValues = {"HTTP", "HTTPS", "DEFAULT"}, required = false
	)
	public String connectionProtocol();

}