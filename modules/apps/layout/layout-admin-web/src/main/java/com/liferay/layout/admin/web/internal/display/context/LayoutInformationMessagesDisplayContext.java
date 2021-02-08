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

package com.liferay.layout.admin.web.internal.display.context;

import com.liferay.layout.admin.constants.LayoutAdminPortletKeys;
import com.liferay.layout.admin.web.internal.product.navigation.control.menu.InformationMessagesProductNavigationControlMenuEntry;
import com.liferay.layout.set.prototype.constants.LayoutSetPrototypePortletKeys;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutSetPrototype;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.resource.bundle.ResourceBundleLoader;
import com.liferay.portal.kernel.resource.bundle.ResourceBundleLoaderUtil;
import com.liferay.portal.kernel.service.LayoutSetPrototypeLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.AggregateResourceBundle;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.sites.kernel.util.SitesUtil;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Víctor Galán
 */
public class LayoutInformationMessagesDisplayContext {

	public LayoutInformationMessagesDisplayContext(
		HttpServletRequest httpServletRequest) {

		_httpServletRequest = httpServletRequest;
	}

	public Map<String, Object> getData() {
		ThemeDisplay themeDisplay =
			(ThemeDisplay)_httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		boolean showLayoutSetPrototypeLayoutMessage = GetterUtil.getBoolean(
			_httpServletRequest.getAttribute(
				InformationMessagesProductNavigationControlMenuEntry.
					INFORMATION_MESSAGES_LAYOUT_SET_PROTOTYPE_LAYOUT));

		boolean showLinkedLayoutMessage = GetterUtil.getBoolean(
			_httpServletRequest.getAttribute(
				InformationMessagesProductNavigationControlMenuEntry.
					INFORMATION_MESSAGES_LINKED_LAYOUT));

		return HashMapBuilder.<String, Object>put(
			"enableDisableLayoutSetPrototypePropagationURL",
			() -> {
				if (!showLayoutSetPrototypeLayoutMessage) {
					return null;
				}

				Layout layout = themeDisplay.getLayout();

				boolean readyForPropagation =
					_isLayoutSetPrototypeReadyForPropagation(layout);

				Group group = layout.getGroup();

				LayoutSetPrototype layoutSetPrototype =
					LayoutSetPrototypeLocalServiceUtil.getLayoutSetPrototype(
						group.getClassPK());

				PortletURL enableDisableLayoutSetPrototypePropagationURL =
					PortletURLFactoryUtil.create(
						_httpServletRequest,
						LayoutSetPrototypePortletKeys.LAYOUT_SET_PROTOTYPE,
						PortletRequest.ACTION_PHASE);

				enableDisableLayoutSetPrototypePropagationURL.setParameter(
					ActionRequest.ACTION_NAME,
					"updateLayoutSetPrototypeAction");
				enableDisableLayoutSetPrototypePropagationURL.setParameter(
					"readyForPropagation",
					String.valueOf(!readyForPropagation));
				enableDisableLayoutSetPrototypePropagationURL.setParameter(
					"redirect", PortalUtil.getLayoutURL(themeDisplay));
				enableDisableLayoutSetPrototypePropagationURL.setParameter(
					"layoutSetPrototypeId",
					String.valueOf(
						layoutSetPrototype.getLayoutSetPrototypeId()));

				return enableDisableLayoutSetPrototypePropagationURL.toString();
			}
		).put(
			"layoutSetPrototypeLayoutButtonLabel",
			() -> {
				if (!showLayoutSetPrototypeLayoutMessage) {
					return null;
				}

				boolean readyForPropagation =
					_isLayoutSetPrototypeReadyForPropagation(
						themeDisplay.getLayout());

				String message = "ready-for-propagation";

				if (readyForPropagation) {
					message = "disable-propagation";
				}

				return LanguageUtil.get(
					_getResourceBundle(themeDisplay.getLocale()), message);
			}
		).put(
			"layoutSetPrototypeLayoutMessage",
			() -> {
				if (!showLayoutSetPrototypeLayoutMessage) {
					return null;
				}

				return LanguageUtil.get(
					_getResourceBundle(themeDisplay.getLocale()),
					"each-page-modification-can-trigger-a-propagation-from-" +
						"the-site-template-to-the-connected-sites");
			}
		).put(
			"layoutSetPrototypeLayoutToastMessage",
			() -> {
				if (!showLayoutSetPrototypeLayoutMessage) {
					return null;
				}

				String message =
					"propagation-is-disabled-connected-sites-might-not-have-" +
						"been-updated-yet-propagation-is-only-triggered-when-" +
							"a-site-created-from-the-template-is-visited";

				if (!_isLayoutSetPrototypeReadyForPropagation(
						themeDisplay.getLayout())) {

					message =
						"propagation-is-enabled-connected-sites-will-be-" +
							"updated-once-a-site-page-is-visited";
				}

				return LanguageUtil.get(
					_getResourceBundle(themeDisplay.getLocale()), message);
			}
		).put(
			"linkedLayoutMessage",
			() -> {
				if (!showLinkedLayoutMessage) {
					return null;
				}

				String message =
					"this-page-is-linked-to-a-site-template-which-does-not-" +
						"allow-modifications-to-it";

				Layout layout = themeDisplay.getLayout();

				Group group = themeDisplay.getScopeGroup();

				if (layout.isLayoutPrototypeLinkActive() &&
					!group.hasStagingGroup()) {

					message = "this-page-is-linked-to-a-page-template";
				}
				else if (SitesUtil.isUserGroupLayout(layout)) {
					message = "this-page-belongs-to-a-user-group";
				}

				return LanguageUtil.get(
					_getResourceBundle(themeDisplay.getLocale()), message);
			}
		).put(
			"portletNamespace",
			PortalUtil.getPortletNamespace(LayoutAdminPortletKeys.GROUP_PAGES)
		).put(
			"resetPrototypeURL",
			() -> {
				PortletURL resetPrototypeURL = PortletURLFactoryUtil.create(
					_httpServletRequest, LayoutAdminPortletKeys.GROUP_PAGES,
					PortletRequest.ACTION_PHASE);

				resetPrototypeURL.setParameter(
					ActionRequest.ACTION_NAME, "/layout_admin/reset_prototype");
				resetPrototypeURL.setParameter(
					"redirect", PortalUtil.getLayoutURL(themeDisplay));
				resetPrototypeURL.setParameter(
					"groupId", String.valueOf(themeDisplay.getSiteGroupId()));

				return resetPrototypeURL.toString();
			}
		).put(
			"showLayoutSetPrototypeLayoutMessage",
			showLayoutSetPrototypeLayoutMessage
		).put(
			"showLinkedLayoutMessage", showLinkedLayoutMessage
		).put(
			"showModifiedLayoutMessage",
			GetterUtil.getBoolean(
				_httpServletRequest.getAttribute(
					InformationMessagesProductNavigationControlMenuEntry.
						INFORMATION_MESSAGES_MODIFIED_LAYOUT))
		).build();
	}

	private ResourceBundle _getResourceBundle(Locale locale) {
		if (_resourceBundle != null) {
			return _resourceBundle;
		}

		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			"content.Language", locale, getClass());

		ResourceBundleLoader resourceBundleLoader =
			ResourceBundleLoaderUtil.
				getResourceBundleLoaderByBundleSymbolicName(
					"com.liferay.layout.set.prototype.web");

		if (resourceBundleLoader == null) {
			_resourceBundle = resourceBundle;
		}
		else {
			_resourceBundle = new AggregateResourceBundle(
				resourceBundle,
				resourceBundleLoader.loadResourceBundle(locale));
		}

		return _resourceBundle;
	}

	private boolean _isLayoutSetPrototypeReadyForPropagation(Layout layout)
		throws PortalException {

		if (_readyForPropagation != null) {
			return _readyForPropagation;
		}

		Group group = layout.getGroup();

		LayoutSetPrototype layoutSetPrototype =
			LayoutSetPrototypeLocalServiceUtil.getLayoutSetPrototype(
				group.getClassPK());

		UnicodeProperties settingsUnicodeProperties =
			layoutSetPrototype.getSettingsProperties();

		_readyForPropagation = GetterUtil.getBoolean(
			settingsUnicodeProperties.getProperty("readyForPropagation"));

		return _readyForPropagation;
	}

	private final HttpServletRequest _httpServletRequest;
	private Boolean _readyForPropagation;
	private ResourceBundle _resourceBundle;

}