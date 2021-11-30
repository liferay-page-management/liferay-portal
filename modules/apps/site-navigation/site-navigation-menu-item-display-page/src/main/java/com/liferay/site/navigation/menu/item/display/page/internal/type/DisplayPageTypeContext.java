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

package com.liferay.site.navigation.menu.item.display.page.internal.type;

import com.liferay.info.item.InfoItemClassDetails;
import com.liferay.info.item.InfoItemReference;
import com.liferay.info.item.provider.InfoItemFormVariationsProvider;
import com.liferay.layout.display.page.LayoutDisplayPageMultiSelectionProvider;
import com.liferay.layout.display.page.LayoutDisplayPageObjectProvider;
import com.liferay.layout.display.page.LayoutDisplayPageProvider;

import java.util.Locale;
import java.util.Optional;

/**
 * @author Lourdes Fernández Besada
 */
public class DisplayPageTypeContext {

	public DisplayPageTypeContext(
		InfoItemClassDetails infoItemClassDetails,
		InfoItemFormVariationsProvider<?> infoItemFormVariationsProvider,
		LayoutDisplayPageProvider layoutDisplayPageProvider,
		LayoutDisplayPageMultiSelectionProvider
			layoutDisplayPageMultiSelectionProvider) {

		_infoItemClassDetails = infoItemClassDetails;
		_infoItemFormVariationsProvider = infoItemFormVariationsProvider;
		_layoutDisplayPageProvider = layoutDisplayPageProvider;
		_layoutDisplayPageMultiSelectionProvider =
			layoutDisplayPageMultiSelectionProvider;
	}

	public String getClassName() {
		return _infoItemClassDetails.getClassName();
	}

	public InfoItemClassDetails getInfoItemClassDetails() {
		return _infoItemClassDetails;
	}

	public InfoItemFormVariationsProvider<?>
		getInfoItemFormVariationsProvider() {

		return _infoItemFormVariationsProvider;
	}

	public String getLabel(Locale locale) {
		return _infoItemClassDetails.getLabel(locale);
	}

	public Optional<LayoutDisplayPageMultiSelectionProvider<?>>
		getLayoutDisplayPageMultiSelectionProviderOptional() {

		return Optional.ofNullable(_layoutDisplayPageMultiSelectionProvider);
	}

	public LayoutDisplayPageObjectProvider<?>
		getLayoutDisplayPageObjectProvider(long classPK) {

		return _layoutDisplayPageProvider.getLayoutDisplayPageObjectProvider(
			new InfoItemReference(
				_infoItemClassDetails.getClassName(), classPK));
	}

	public LayoutDisplayPageProvider<?> getLayoutDisplayPageProvider() {
		return _layoutDisplayPageProvider;
	}

	public boolean isMultiSelection() {
		if (_layoutDisplayPageMultiSelectionProvider != null) {
			return true;
		}

		return false;
	}

	private final InfoItemClassDetails _infoItemClassDetails;
	private final InfoItemFormVariationsProvider<?>
		_infoItemFormVariationsProvider;
	private final LayoutDisplayPageMultiSelectionProvider<?>
		_layoutDisplayPageMultiSelectionProvider;
	private final LayoutDisplayPageProvider<?> _layoutDisplayPageProvider;

}