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

package com.liferay.layout.element.internal;

import com.liferay.layout.element.LayoutElement;
import com.liferay.portal.kernel.language.Language;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Lourdes Fernández Besada
 */
@Component(
	immediate = true, property = "layout.element.order:Integer=100",
	service = LayoutElement.class
)
public class RowLayoutElement implements LayoutElement {

	@Override
	public String getCollectionKey() {
		return "layout-elements";
	}

	@Override
	public String getIcon() {
		return "table";
	}

	@Override
	public String getKey() {
		return "row";
	}

	@Override
	public String getLabel(Locale locale) {
		return _language.get(locale, "grid");
	}

	@Override
	public String getType() {
		return "row";
	}

	@Reference
	private Language _language;

}