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

package com.liferay.site.navigation.menu.item.display.page.internal.portlet.action;

import com.liferay.info.item.InfoItemHierarchicalReference;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.navigation.admin.constants.SiteNavigationAdminPortletKeys;
import com.liferay.site.navigation.model.SiteNavigationMenuItem;
import com.liferay.site.navigation.service.SiteNavigationMenuItemService;

import java.util.List;
import java.util.Objects;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Lourdes Fernández Besada
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + SiteNavigationAdminPortletKeys.SITE_NAVIGATION_ADMIN,
		"mvc.command.name=/navigation_menu/add_multiple_display_page_type_site_navigation_menu_item"
	},
	service = MVCActionCommand.class
)
public class AddMultipleDisplayPageTypeSiteNavigationMenuItemMVCActionCommand
	extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		String siteNavigationMenuItemType = ParamUtil.getString(
			actionRequest, "siteNavigationMenuItemType");
		long siteNavigationMenuId = ParamUtil.getLong(
			actionRequest, "siteNavigationMenuId");

		if (Validator.isNotNull(siteNavigationMenuItemType) &&
			(siteNavigationMenuId > 0)) {

			ServiceContext serviceContext = ServiceContextFactory.getInstance(
				actionRequest);

			ThemeDisplay themeDisplay =
				(ThemeDisplay)actionRequest.getAttribute(WebKeys.THEME_DISPLAY);

			try {
				List<InfoItemItemSelectorReturnItem>
					infoItemItemSelectorReturnItems = JSONUtil.toList(
						JSONFactoryUtil.createJSONArray(
							ParamUtil.getString(actionRequest, "items")),
						itemJSONObject -> {
							if (!Objects.equals(
									itemJSONObject.getString("className"),
									siteNavigationMenuItemType)) {

								return null;
							}

							return new InfoItemItemSelectorReturnItem(
								itemJSONObject);
						});

				for (InfoItemItemSelectorReturnItem
						infoItemItemSelectorReturnItem :
							infoItemItemSelectorReturnItems) {

					_addSiteNavigationMenuItem(
						themeDisplay.getScopeGroupId(),
						infoItemItemSelectorReturnItem, serviceContext,
						siteNavigationMenuId, siteNavigationMenuItemType);
				}
			}
			catch (PortalException portalException) {
				if (_log.isDebugEnabled()) {
					_log.debug(portalException, portalException);
				}

				jsonObject.put(
					"errorMessage",
					LanguageUtil.get(
						_portal.getHttpServletRequest(actionRequest),
						"an-unexpected-error-occurred"));
			}
		}
		else {
			if (_log.isDebugEnabled()) {
				_log.debug(
					StringBundler.concat(
						"Unable to add multiple SiteNavigationMenuItem for ",
						"siteNavigationMenuId ", siteNavigationMenuId,
						" and type ", siteNavigationMenuItemType));
			}

			jsonObject.put(
				"errorMessage",
				LanguageUtil.get(
					_portal.getHttpServletRequest(actionRequest),
					"an-unexpected-error-occurred"));
		}

		JSONPortletResponseUtil.writeJSON(
			actionRequest, actionResponse, jsonObject);
	}

	protected class InfoItemItemSelectorReturnItem
		extends InfoItemHierarchicalReference {

		public InfoItemItemSelectorReturnItem(JSONObject jsonObject) {
			super(
				jsonObject.getString("className"),
				jsonObject.getLong("classPK"));

			_className = jsonObject.getString("className");
			_classNameId = jsonObject.getLong("classNameId");
			_classPK = jsonObject.getLong("classPK");
			_classTypeId = jsonObject.getLong("classTypeId");
			_subtype = jsonObject.getString("subtype");
			_title = jsonObject.getString("title");
			_type = jsonObject.getString("type");
		}

		public String getClassName() {
			return _className;
		}

		public long getClassNameId() {
			return _classNameId;
		}

		public long getClassPK() {
			return _classPK;
		}

		public long getClassTypeId() {
			return _classTypeId;
		}

		public String getSubtype() {
			return _subtype;
		}

		public String getTitle() {
			return _title;
		}

		public String getType() {
			return _type;
		}

		public void setClassName(String className) {
			_className = className;
		}

		public void setClassNameId(long classNameId) {
			_classNameId = classNameId;
		}

		public void setClassPK(long classPK) {
			_classPK = classPK;
		}

		public void setClassTypeId(long classTypeId) {
			_classTypeId = classTypeId;
		}

		public void setSubtype(String subtype) {
			_subtype = subtype;
		}

		public void setTitle(String title) {
			_title = title;
		}

		public void setType(String type) {
			_type = type;
		}

		private String _className;
		private long _classNameId;
		private long _classPK;
		private long _classTypeId;
		private String _subtype;
		private String _title;
		private String _type;

	}

	private void _addSiteNavigationMenuItem(
			long groupId,
			InfoItemItemSelectorReturnItem infoItemItemSelectorReturnItem,
			ServiceContext serviceContext, long siteNavigationMenuId,
			String siteNavigationMenuItemType)
		throws PortalException {

		UnicodeProperties typeSettingsUnicodeProperties = new UnicodeProperties(
			true);

		typeSettingsUnicodeProperties.setProperty(
			"className", infoItemItemSelectorReturnItem.getClassName());
		typeSettingsUnicodeProperties.setProperty(
			"classNameId",
			String.valueOf(infoItemItemSelectorReturnItem.getClassNameId()));
		typeSettingsUnicodeProperties.setProperty(
			"classPK",
			String.valueOf(infoItemItemSelectorReturnItem.getClassPK()));
		typeSettingsUnicodeProperties.setProperty(
			"classTypeId",
			String.valueOf(infoItemItemSelectorReturnItem.getClassTypeId()));
		typeSettingsUnicodeProperties.setProperty(
			"type", infoItemItemSelectorReturnItem.getType());
		typeSettingsUnicodeProperties.setProperty(
			"title", infoItemItemSelectorReturnItem.getTitle());

		_siteNavigationMenuItemService.addSiteNavigationMenuItem(
			groupId, siteNavigationMenuId, 0L, siteNavigationMenuItemType,
			typeSettingsUnicodeProperties.toString(), serviceContext);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AddMultipleDisplayPageTypeSiteNavigationMenuItemMVCActionCommand.class);

	@Reference
	private Portal _portal;

	@Reference
	private SiteNavigationMenuItemService _siteNavigationMenuItemService;

}