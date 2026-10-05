/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.admin.web.internal.servlet.taglib;

import com.liferay.design.library.util.DesignLibraryUtil;
import com.liferay.layout.page.template.admin.constants.LayoutPageTemplateAdminConstants;
import com.liferay.layout.page.template.admin.constants.LayoutPageTemplateAdminPortletKeys;
import com.liferay.layout.page.template.admin.web.internal.security.permission.resource.LayoutPageTemplateCollectionPermission;
import com.liferay.layout.page.template.admin.web.internal.util.LayoutPageTemplatePortletUtil;
import com.liferay.layout.page.template.constants.LayoutPageTemplateCollectionTypeConstants;
import com.liferay.layout.page.template.model.LayoutPageTemplateCollection;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.servlet.taglib.ui.BreadcrumbEntry;
import com.liferay.portal.kernel.servlet.taglib.ui.BreadcrumbEntryContributor;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.portlet.PortletRequest;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Georgel Pop
 */
@Component(
	property = "service.ranking:Integer=100",
	service = BreadcrumbEntryContributor.class
)
public class LayoutPageTemplateBreadcrumbEntryContributorImpl
	implements BreadcrumbEntryContributor {

	@Override
	public List<BreadcrumbEntry> getBreadcrumbEntries(
		List<BreadcrumbEntry> originalBreadcrumbEntries,
		HttpServletRequest httpServletRequest) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();

		if (!Objects.equals(
				LayoutPageTemplateAdminPortletKeys.LAYOUT_PAGE_TEMPLATES,
				portletDisplay.getPortletName()) ||
			!DesignLibraryUtil.isDesignLibraryScope(
				themeDisplay.getScopeGroup())) {

			return originalBreadcrumbEntries;
		}

		LayoutPageTemplateCollection layoutPageTemplateCollection =
			LayoutPageTemplatePortletUtil.fetchLayoutPageTemplateCollection(
				httpServletRequest, themeDisplay.getScopeGroupId());

		if (layoutPageTemplateCollection == null) {
			return originalBreadcrumbEntries;
		}

		List<LayoutPageTemplateCollection> layoutPageTemplateCollections =
			layoutPageTemplateCollection.getAncestors();

		Collections.reverse(layoutPageTemplateCollections);

		List<BreadcrumbEntry> breadcrumbEntries = TransformUtil.transform(
			layoutPageTemplateCollections,
			curLayoutPageTemplateCollection -> {
				if (!LayoutPageTemplateCollectionPermission.contains(
						themeDisplay.getPermissionChecker(),
						curLayoutPageTemplateCollection, ActionKeys.VIEW)) {

					return null;
				}

				return _createLayoutPageTemplateCollectionBreadcrumbEntry(
					httpServletRequest, curLayoutPageTemplateCollection,
					themeDisplay);
			});

		breadcrumbEntries.addAll(originalBreadcrumbEntries);

		return breadcrumbEntries;
	}

	private BreadcrumbEntry _createLayoutPageTemplateCollectionBreadcrumbEntry(
		HttpServletRequest httpServletRequest,
		LayoutPageTemplateCollection layoutPageTemplateCollection,
		ThemeDisplay themeDisplay) {

		BreadcrumbEntry breadcrumbEntry = new BreadcrumbEntry();

		breadcrumbEntry.setTitle(layoutPageTemplateCollection.getName());
		breadcrumbEntry.setURL(
			PortletURLBuilder.create(
				_portal.getControlPanelPortletURL(
					httpServletRequest, themeDisplay.getScopeGroup(),
					LayoutPageTemplateAdminPortletKeys.LAYOUT_PAGE_TEMPLATES, 0,
					0, PortletRequest.RENDER_PHASE)
			).setTabs1(
				_getTabs1(layoutPageTemplateCollection)
			).setParameter(
				"layoutPageTemplateCollectionId",
				layoutPageTemplateCollection.getLayoutPageTemplateCollectionId()
			).buildString());

		return breadcrumbEntry;
	}

	private String _getTabs1(
		LayoutPageTemplateCollection layoutPageTemplateCollection) {

		if (layoutPageTemplateCollection.getType() ==
				LayoutPageTemplateCollectionTypeConstants.DISPLAY_PAGE) {

			return LayoutPageTemplateAdminConstants.
				TABS1_DISPLAY_PAGE_TEMPLATES;
		}

		return LayoutPageTemplateAdminConstants.TABS1_PAGE_TEMPLATES;
	}

	@Reference
	private Portal _portal;

}