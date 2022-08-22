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
	immediate = true, property = "layout.element.order:Integer=300",
	service = LayoutElement.class
)
public class FormLayoutElement implements LayoutElement {

	@Override
	public String getCollectionKey() {
		return "INPUTS";
	}

	@Override
	public String getIcon() {
		return "container";
	}

	@Override
	public String getKey() {
		return "form";
	}

	@Override
	public String getLabel(Locale locale) {
		return _language.get(locale, "form-container");
	}

	@Override
	public String getType() {
		return "form";
	}

	@Reference
	private Language _language;

}