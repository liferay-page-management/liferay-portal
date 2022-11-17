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

package com.liferay.application.list.taglib.servlet.taglib;

import com.liferay.application.list.PanelApp;
import com.liferay.application.list.PanelAppRegistry;
import com.liferay.application.list.PanelCategory;
import com.liferay.application.list.PanelCategoryRegistry;
import com.liferay.application.list.constants.ApplicationListWebKeys;
import com.liferay.application.list.display.context.logic.PanelCategoryHelper;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Julio Camarero
 */
public class PanelCategoryBodyTag extends BasePanelTag {

	public List<PanelApp> getPanelApps() {
		return _panelApps;
	}

	public PanelCategory getPanelCategory() {
		return _panelCategory;
	}

	public void setPanelApps(List<PanelApp> panelApps) {
		_panelApps = panelApps;
	}

	public void setPanelCategory(PanelCategory panelCategory) {
		_panelCategory = panelCategory;
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();

		_applicationListProps = null;
		_panelApps = null;
		_panelCategory = null;
	}

	protected Map<String, Object> getApplicationListProps() {
		HttpServletRequest httpServletRequest = getRequest();

		PanelAppRegistry panelAppRegistry =
			(PanelAppRegistry)httpServletRequest.getAttribute(
				ApplicationListWebKeys.PANEL_APP_REGISTRY);

		PanelCategoryRegistry panelCategoryRegistry =
			(PanelCategoryRegistry)httpServletRequest.getAttribute(
				ApplicationListWebKeys.PANEL_CATEGORY_REGISTRY);

		PanelCategoryHelper panelCategoryHelper = new PanelCategoryHelper(
			panelAppRegistry, panelCategoryRegistry);

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return HashMapBuilder.<String, Object>put(
			"category",
			_getPanelCategoryObject(
				httpServletRequest, panelAppRegistry, _panelCategory,
				panelCategoryHelper, panelCategoryRegistry, themeDisplay)
		).build();
	}

	@Override
	protected String getPage() {
		return "/panel_category_body/page.jsp";
	}

	@Override
	protected void setAttributes(HttpServletRequest httpServletRequest) {
		if (_applicationListProps == null) {
			_applicationListProps = getApplicationListProps();
		}

		httpServletRequest.setAttribute(
			"liferay-application-list:panel-category-body:applicationListProps",
			_applicationListProps);
	}

	private Map<String, Object> _getPanelAppObject(
			HttpServletRequest httpServletRequest, PanelApp panelApp,
			ThemeDisplay themeDisplay)
		throws PortalException {

		return HashMapBuilder.<String, Object>put(
			"active",
			() -> {
				HttpServletRequest originalHttpServletRequest =
					PortalUtil.getOriginalServletRequest(httpServletRequest);

				String parameterName =
					PortalUtil.getPortletNamespace(themeDisplay.getPpid()) +
						"portletResource";

				String portletResource = ParamUtil.getString(
					originalHttpServletRequest, parameterName);

				boolean active = Objects.equals(
					portletResource, panelApp.getPortletId());

				if (Validator.isNull(portletResource)) {
					active = Objects.equals(
						themeDisplay.getPpid(), panelApp.getPortletId());
				}

				return active;
			}
		).put(
			"href", String.valueOf(panelApp.getPortletURL(httpServletRequest))
		).put(
			"id", panelApp.getKey()
		).put(
			"label", panelApp.getLabel(themeDisplay.getLocale())
		).build();
	}

	private Map<String, Object> _getPanelCategoryObject(
		HttpServletRequest httpServletRequest,
		PanelAppRegistry panelAppRegistry, PanelCategory panelCategory,
		PanelCategoryHelper panelCategoryHelper,
		PanelCategoryRegistry panelCategoryRegistry,
		ThemeDisplay themeDisplay) {

		return HashMapBuilder.<String, Object>put(
			"id", panelCategory.getKey()
		).put(
			"initialExpanded",
			panelCategory.isActive(
				httpServletRequest, panelCategoryHelper, getGroup())
		).put(
			"items",
			() -> {
				List<Map<String, Object>> items = new ArrayList<>();

				List<PanelCategory> panelCategories =
					panelCategoryRegistry.getChildPanelCategories(
						panelCategory, themeDisplay.getPermissionChecker(),
						getGroup());

				for (PanelCategory childPanelCategory : panelCategories) {
					items.add(
						_getPanelCategoryObject(
							httpServletRequest, panelAppRegistry,
							childPanelCategory, panelCategoryHelper,
							panelCategoryRegistry, themeDisplay));
				}

				List<PanelApp> panelApps = panelAppRegistry.getPanelApps(
					panelCategory, themeDisplay.getPermissionChecker(),
					getGroup());

				for (PanelApp panelApp : panelApps) {
					items.add(
						_getPanelAppObject(
							httpServletRequest, panelApp, themeDisplay));
				}

				return items;
			}
		).put(
			"label", panelCategory.getLabel(themeDisplay.getLocale())
		).build();
	}

	private Map<String, Object> _applicationListProps;
	private List<PanelApp> _panelApps;
	private PanelCategory _panelCategory;

}