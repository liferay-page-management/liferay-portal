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

package com.liferay.layout.page.template.item.selector.web.internal;

import com.liferay.item.selector.ItemSelectorReturnType;
import com.liferay.item.selector.ItemSelectorView;
import com.liferay.item.selector.ItemSelectorViewDescriptor;
import com.liferay.item.selector.ItemSelectorViewDescriptorRenderer;
import com.liferay.item.selector.criteria.UUIDItemSelectorReturnType;
import com.liferay.layout.page.template.item.selector.criterion.LayoutPageTemplateCollectionItemSelectorCriterion;
import com.liferay.layout.page.template.model.LayoutPageTemplateCollection;
import com.liferay.layout.page.template.service.LayoutPageTemplateCollectionLocalService;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.IOException;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
@Component(immediate = true, service = ItemSelectorView.class)
public class LayoutPageTemplateCollectionsItemSelectorView
	implements ItemSelectorView
		<LayoutPageTemplateCollectionItemSelectorCriterion> {

	@Override
	public Class<LayoutPageTemplateCollectionItemSelectorCriterion>
		getItemSelectorCriterionClass() {

		return LayoutPageTemplateCollectionItemSelectorCriterion.class;
	}

	@Override
	public List<ItemSelectorReturnType> getSupportedItemSelectorReturnTypes() {
		return _supportedItemSelectorReturnTypes;
	}

	@Override
	public String getTitle(Locale locale) {
		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			locale, LayoutPageTemplateCollectionsItemSelectorView.class);

		return ResourceBundleUtil.getString(
			resourceBundle, "page-template-collections");
	}

	@Override
	public void renderHTML(
			ServletRequest servletRequest, ServletResponse servletResponse,
			LayoutPageTemplateCollectionItemSelectorCriterion
				layoutPageTemplateCollectionItemSelectorCriterion,
			PortletURL portletURL, String itemSelectedEventName, boolean search)
		throws IOException, ServletException {

		_itemSelectorViewDescriptorRenderer.renderHTML(
			servletRequest, servletResponse,
			layoutPageTemplateCollectionItemSelectorCriterion, portletURL,
			itemSelectedEventName, search,
			new LayoutPageTemplateCollectionItemSelectorViewDescriptor(
				(HttpServletRequest)servletRequest, portletURL));
	}

	private static final List<ItemSelectorReturnType>
		_supportedItemSelectorReturnTypes = Collections.singletonList(
			new UUIDItemSelectorReturnType());

	@Reference
	private ItemSelectorViewDescriptorRenderer
		<LayoutPageTemplateCollectionItemSelectorCriterion>
			_itemSelectorViewDescriptorRenderer;

	@Reference
	private LayoutPageTemplateCollectionLocalService
		_layoutPageTemplateCollectionLocalService;

	@Reference
	private Portal _portal;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.layout.page.template.item.selector.web)"
	)
	private ServletContext _servletContext;

	private class LayoutPageTemplateCollectionItemDescriptor
		implements ItemSelectorViewDescriptor.ItemDescriptor {

		public LayoutPageTemplateCollectionItemDescriptor(
			HttpServletRequest httpServletRequest,
			LayoutPageTemplateCollection layoutPageTemplateCollection) {

			_httpServletRequest = httpServletRequest;
			_layoutPageTemplateCollection = layoutPageTemplateCollection;

			_themeDisplay = (ThemeDisplay)_httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);
		}

		@Override
		public String getIcon() {
			return "box-container";
		}

		@Override
		public String getImageURL() {
			return null;
		}

		@Override
		public String getPayload() {
			return JSONUtil.put(
				"layoutPageTemplateCollectionId",
				_layoutPageTemplateCollection.
					getLayoutPageTemplateCollectionId()
			).toString();
		}

		@Override
		public String getSubtitle(Locale locale) {
			return null;
		}

		@Override
		public String getTitle(Locale locale) {
			return _layoutPageTemplateCollection.getName();
		}

		@Override
		public boolean isCompact() {
			return true;
		}

		private HttpServletRequest _httpServletRequest;
		private final LayoutPageTemplateCollection
			_layoutPageTemplateCollection;
		private final ThemeDisplay _themeDisplay;

	}

	private class LayoutPageTemplateCollectionItemSelectorViewDescriptor
		implements ItemSelectorViewDescriptor<LayoutPageTemplateCollection> {

		public LayoutPageTemplateCollectionItemSelectorViewDescriptor(
			HttpServletRequest httpServletRequest, PortletURL portletURL) {

			_httpServletRequest = httpServletRequest;
			_portletURL = portletURL;

			_portletRequest = (PortletRequest)_httpServletRequest.getAttribute(
				JavaConstants.JAVAX_PORTLET_REQUEST);

			_themeDisplay = (ThemeDisplay)_httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);
		}

		@Override
		public ItemDescriptor getItemDescriptor(
			LayoutPageTemplateCollection layoutPageTemplateCollection) {

			return new LayoutPageTemplateCollectionItemDescriptor(
				_httpServletRequest, layoutPageTemplateCollection);
		}

		@Override
		public ItemSelectorReturnType getItemSelectorReturnType() {
			return new UUIDItemSelectorReturnType();
		}

		@Override
		public SearchContainer<LayoutPageTemplateCollection>
			getSearchContainer() {

			SearchContainer<LayoutPageTemplateCollection> searchContainer =
				new SearchContainer<>(
					_portletRequest, _portletURL, null,
					"no-entries-were-found");

			List<LayoutPageTemplateCollection> layoutPageTemplateCollections =
				_layoutPageTemplateCollectionLocalService.
					getLayoutPageTemplateCollections(
						_themeDisplay.getScopeGroupId(),
						searchContainer.getStart(), searchContainer.getEnd());

			int total =
				_layoutPageTemplateCollectionLocalService.
					getLayoutPageTemplateCollectionsCount(
						_themeDisplay.getScopeGroupId());

			searchContainer.setResults(layoutPageTemplateCollections);
			searchContainer.setTotal(total);

			return searchContainer;
		}

		@Override
		public boolean isShowBreadcrumb() {
			return false;
		}

		@Override
		public boolean isShowManagementToolbar() {
			return false;
		}

		private HttpServletRequest _httpServletRequest;
		private final PortletRequest _portletRequest;
		private final PortletURL _portletURL;
		private final ThemeDisplay _themeDisplay;

	}

}