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

package com.liferay.asset.list.web.internal.servlet.taglib.util;

import com.liferay.asset.display.page.portlet.AssetDisplayPageFriendlyURLProvider;
import com.liferay.asset.display.page.util.AssetDisplayPageUtil;
import com.liferay.asset.info.display.url.provider.AssetInfoEditURLProvider;
import com.liferay.asset.list.web.internal.field.ListItemField;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemListBuilder;
import com.liferay.info.display.contributor.InfoDisplayContributor;
import com.liferay.info.display.contributor.InfoDisplayContributorTracker;
import com.liferay.info.display.contributor.InfoDisplayObjectProvider;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.service.permission.LayoutPermissionUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Jürgen Kappler
 */
public class ListItemsActionDropdownItems {

	public ListItemsActionDropdownItems(
		AssetDisplayPageFriendlyURLProvider assetDisplayPageFriendlyURLProvider,
		AssetInfoEditURLProvider assetInfoEditURLProvider,
		InfoDisplayContributorTracker infoDisplayContributorTracker,
		HttpServletRequest httpServletRequest) {

		_assetDisplayPageFriendlyURLProvider =
			assetDisplayPageFriendlyURLProvider;
		_assetInfoEditURLProvider = assetInfoEditURLProvider;
		_infoDisplayContributorTracker = infoDisplayContributorTracker;

		_httpServletRequest = httpServletRequest;

		_themeDisplay = (ThemeDisplay)_httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public List<DropdownItem> getActionDropdownItems(
			ListItemField listItemField)
		throws Exception {

		return DropdownItemListBuilder.add(
			_getViewDisplayPageActionUnsafeConsumer(listItemField)
		).add(
			_getEditContentActionUnsafeConsumer(listItemField)
		).add(
			_getEditDisplayPageActionUnsafeConsumer(listItemField)
		).build();
	}

	private UnsafeConsumer<DropdownItem, Exception>
		_getEditContentActionUnsafeConsumer(ListItemField listItemField) {

		String editContentURL = _getEditContentURL(listItemField);

		return dropdownItem -> {
			dropdownItem.putData("action", "editContent");
			dropdownItem.putData("editContentURL", editContentURL);
			dropdownItem.setDisabled(Validator.isNull(editContentURL));
			dropdownItem.setLabel(
				LanguageUtil.get(_httpServletRequest, "edit-content"));
		};
	}

	private String _getEditContentURL(ListItemField listItemField) {
		String editContentURL = _assetInfoEditURLProvider.getURL(
			listItemField.getClassName(), listItemField.getClassPK(),
			_httpServletRequest);

		return HttpUtil.setParameter(
			editContentURL, "redirect", _getRedirect());
	}

	private UnsafeConsumer<DropdownItem, Exception>
			_getEditDisplayPageActionUnsafeConsumer(ListItemField listItemField)
		throws Exception {

		String editDisplayPageTemplateURL = _getEditDisplayPageTemplateURL(
			listItemField);

		return dropdownItem -> {
			dropdownItem.putData("action", "editDisplayPageTemplate");
			dropdownItem.putData(
				"editDisplayPageTemplateURL", editDisplayPageTemplateURL);
			dropdownItem.setDisabled(
				Validator.isNull(editDisplayPageTemplateURL));
			dropdownItem.setLabel(
				LanguageUtil.get(
					_httpServletRequest, "edit-display-page-template"));
		};
	}

	private String _getEditDisplayPageTemplateURL(ListItemField listItemField)
		throws Exception {

		InfoDisplayContributor<?> infoDisplayContributor =
			_infoDisplayContributorTracker.getInfoDisplayContributor(
				listItemField.getClassName());

		if (infoDisplayContributor == null) {
			return null;
		}

		InfoDisplayObjectProvider<?> infoDisplayObjectProvider =
			infoDisplayContributor.getInfoDisplayObjectProvider(
				listItemField.getClassPK());

		if (infoDisplayObjectProvider == null) {
			return null;
		}

		LayoutPageTemplateEntry assetDisplayPageLayoutPageTemplateEntry =
			AssetDisplayPageUtil.getAssetDisplayPageLayoutPageTemplateEntry(
				_themeDisplay.getScopeGroupId(),
				PortalUtil.getClassNameId(listItemField.getClassName()),
				listItemField.getClassPK(),
				infoDisplayObjectProvider.getClassTypeId());

		if (assetDisplayPageLayoutPageTemplateEntry == null) {
			return null;
		}

		Layout layout = LayoutLocalServiceUtil.fetchLayout(
			assetDisplayPageLayoutPageTemplateEntry.getPlid());

		if (layout == null) {
			return null;
		}

		Layout draftLayout = layout.fetchDraftLayout();

		if ((draftLayout == null) ||
			!LayoutPermissionUtil.contains(
				_themeDisplay.getPermissionChecker(), draftLayout,
				ActionKeys.UPDATE)) {

			return null;
		}

		String editDisplayPageTemplateURL = PortalUtil.getLayoutFullURL(
			draftLayout, _themeDisplay);

		editDisplayPageTemplateURL = HttpUtil.setParameter(
			editDisplayPageTemplateURL, "p_l_back_url", _getRedirect());

		editDisplayPageTemplateURL = HttpUtil.setParameter(
			editDisplayPageTemplateURL, "p_l_mode", Constants.EDIT);

		return editDisplayPageTemplateURL;
	}

	private String _getRedirect() {
		if (Validator.isNotNull(_redirect)) {
			return _redirect;
		}

		_redirect = ParamUtil.getString(_httpServletRequest, "redirect");

		return _redirect;
	}

	private UnsafeConsumer<DropdownItem, Exception>
			_getViewDisplayPageActionUnsafeConsumer(ListItemField listItemField)
		throws Exception {

		String viewDisplayPageURL = _getViewDisplayPageURL(listItemField);

		return dropdownItem -> {
			dropdownItem.putData("action", "viewDisplayPage");
			dropdownItem.putData("viewDisplayPageURL", viewDisplayPageURL);
			dropdownItem.setDisabled(Validator.isNull(viewDisplayPageURL));
			dropdownItem.setLabel(
				LanguageUtil.get(_httpServletRequest, "view-display-page"));
		};
	}

	private String _getViewDisplayPageURL(ListItemField listItemField)
		throws Exception {

		if (_assetDisplayPageFriendlyURLProvider == null) {
			return null;
		}

		String viewDisplayPageURL =
			_assetDisplayPageFriendlyURLProvider.getFriendlyURL(
				listItemField.getClassName(), listItemField.getClassPK(),
				_themeDisplay);

		return HttpUtil.setParameter(
			viewDisplayPageURL, "p_l_back_url", _getRedirect());
	}

	private final AssetDisplayPageFriendlyURLProvider
		_assetDisplayPageFriendlyURLProvider;
	private final AssetInfoEditURLProvider _assetInfoEditURLProvider;
	private final HttpServletRequest _httpServletRequest;
	private final InfoDisplayContributorTracker _infoDisplayContributorTracker;
	private String _redirect;
	private final ThemeDisplay _themeDisplay;

}