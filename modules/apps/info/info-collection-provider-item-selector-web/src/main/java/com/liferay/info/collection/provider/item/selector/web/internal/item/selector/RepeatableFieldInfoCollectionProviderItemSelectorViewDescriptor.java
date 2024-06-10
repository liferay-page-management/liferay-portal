/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.info.collection.provider.item.selector.web.internal.item.selector;

import com.liferay.info.field.InfoFieldSetEntry;
import com.liferay.info.list.provider.item.selector.criterion.InfoListProviderItemSelectorReturnType;
import com.liferay.item.selector.ItemSelectorReturnType;
import com.liferay.item.selector.ItemSelectorViewDescriptor;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Víctor Galán
 */
public class RepeatableFieldInfoCollectionProviderItemSelectorViewDescriptor
	implements ItemSelectorViewDescriptor<InfoFieldSetEntry> {

	public RepeatableFieldInfoCollectionProviderItemSelectorViewDescriptor(
		List<InfoFieldSetEntry> infoFieldSetEntries, String itemType,
		String itemSubtype, HttpServletRequest httpServletRequest,
		PortletURL portletURL) {

		_infoFieldSetEntries = infoFieldSetEntries;
		_itemType = itemType;
		_itemSubtype = itemSubtype;
		_httpServletRequest = httpServletRequest;
		_portletURL = portletURL;
	}

	@Override
	public String[] getDisplayViews() {
		return new String[] {"icon"};
	}

	@Override
	public ItemDescriptor getItemDescriptor(
		InfoFieldSetEntry infoFieldSetEntry) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)_httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return new RepeatableFieldInfoCollectionProviderItemDescriptor(
			infoFieldSetEntry, _itemType, _itemSubtype,
			themeDisplay.getLocale());
	}

	@Override
	public ItemSelectorReturnType getItemSelectorReturnType() {
		return new InfoListProviderItemSelectorReturnType();
	}

	@Override
	public SearchContainer<InfoFieldSetEntry> getSearchContainer() {
		PortletRequest portletRequest =
			(PortletRequest)_httpServletRequest.getAttribute(
				JavaConstants.JAVAX_PORTLET_REQUEST);

		SearchContainer<InfoFieldSetEntry> searchContainer =
			new SearchContainer<>(
				portletRequest, _portletURL, null,
				"there-are-no-repeatable-field-collection-providers");

		searchContainer.setResultsAndTotal(_infoFieldSetEntries);

		return searchContainer;
	}

	private final HttpServletRequest _httpServletRequest;
	private final List<InfoFieldSetEntry> _infoFieldSetEntries;
	private final String _itemSubtype;
	private final String _itemType;
	private final PortletURL _portletURL;

}