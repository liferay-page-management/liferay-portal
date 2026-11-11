/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.admin.web.internal.frontend.taglib.clay.servlet.taglib;

import com.liferay.frontend.taglib.clay.servlet.taglib.VerticalCard;
import com.liferay.layout.admin.web.internal.util.MasterLayoutUtil;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.portlet.RenderRequest;

import java.util.Map;
import java.util.Objects;

/**
 * @author Eudaldo Alonso
 */
public class SelectMasterLayoutVerticalCard implements VerticalCard {

	public SelectMasterLayoutVerticalCard(
		boolean defaultTemplate, long groupId,
		LayoutPageTemplateEntry layoutPageTemplateEntry,
		RenderRequest renderRequest) {

		_defaultTemplate = defaultTemplate;
		_layoutPageTemplateEntry = layoutPageTemplateEntry;
		_renderRequest = renderRequest;

		_themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		_designLibraryMasterLayout = Validator.isNotNull(
			MasterLayoutUtil.getMasterLayoutPageTemplateEntryScopeERC(
				groupId, layoutPageTemplateEntry));
	}

	@Override
	public String getCssClass() {
		String cssClass =
			"select-master-layout-option card-interactive " +
				"card-interactive-secondary";

		long masterLayoutPlid = ParamUtil.getLong(
			_renderRequest, "masterLayoutPlid");

		if (Objects.equals(
				_layoutPageTemplateEntry.getPlid(), masterLayoutPlid)) {

			cssClass += " active";
		}

		if (_designLibraryMasterLayout) {
			cssClass += " design-library-master-layout";
		}

		return cssClass;
	}

	@Override
	public Map<String, String> getDynamicAttributes() {
		return HashMapBuilder.put(
			"role", "button"
		).put(
			"tabIndex", "0"
		).build();
	}

	@Override
	public String getIcon() {
		return "page";
	}

	@Override
	public String getImageAlt() {
		return getTitle();
	}

	@Override
	public String getImageSrc() {
		return _layoutPageTemplateEntry.getImagePreviewURL(_themeDisplay);
	}

	@Override
	public String getStickerCssClass() {
		return "sticker-primary";
	}

	@Override
	public String getStickerIcon() {
		if (_defaultTemplate) {
			return "check-circle";
		}

		return null;
	}

	@Override
	public String getTitle() {
		return _layoutPageTemplateEntry.getName();
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	private final boolean _defaultTemplate;
	private final boolean _designLibraryMasterLayout;
	private final LayoutPageTemplateEntry _layoutPageTemplateEntry;
	private final RenderRequest _renderRequest;
	private final ThemeDisplay _themeDisplay;

}