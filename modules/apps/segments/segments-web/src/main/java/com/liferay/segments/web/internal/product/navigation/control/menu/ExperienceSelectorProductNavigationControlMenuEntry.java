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

package com.liferay.segments.web.internal.product.navigation.control.menu;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.product.navigation.control.menu.BaseJSPProductNavigationControlMenuEntry;
import com.liferay.product.navigation.control.menu.ProductNavigationControlMenuEntry;
import com.liferay.product.navigation.control.menu.constants.ProductNavigationControlMenuCategoryKeys;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.model.SegmentsExperience;
import com.liferay.segments.service.SegmentsEntryLocalService;
import com.liferay.segments.service.SegmentsExperienceLocalService;
import com.liferay.segments.web.internal.constants.SegmentsWebKeys;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Pablo Molina
 */
@Component(
	immediate = true,
	property = {
		"product.navigation.control.menu.category.key=" + ProductNavigationControlMenuCategoryKeys.TOOLS,
		"product.navigation.control.menu.entry.order:Integer=110"
	},
	service = ProductNavigationControlMenuEntry.class
)
public class ExperienceSelectorProductNavigationControlMenuEntry
	extends BaseJSPProductNavigationControlMenuEntry
	implements ProductNavigationControlMenuEntry {

	@Override
	public String getIconJspPath() {
		return "/experience_selector.jsp";
	}

	@Override
	public boolean includeIcon(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException {

		try {
			ThemeDisplay themeDisplay =
				(ThemeDisplay)httpServletRequest.getAttribute(
					WebKeys.THEME_DISPLAY);

			List<SegmentsExperience> segmentsExperiences =
				_segmentsExperienceLocalService.getSegmentsExperiences(
					themeDisplay.getScopeGroupId(),
					_portal.getClassNameId(Layout.class.getName()),
					themeDisplay.getPlid(), true);

			if (segmentsExperiences.isEmpty()) {
				return false;
			}

			List<HashMap<String, Object>> segmentsExperiencesDropdownItems =
				new ArrayList<>();

			for (SegmentsExperience segmentsExperience : segmentsExperiences) {
				Locale locale = (Locale)httpServletRequest.getAttribute(
					WebKeys.LOCALE);

				segmentsExperiencesDropdownItems.add(
					HashMapBuilder.<String, Object>put(
						"active", segmentsExperience.isActive()
					).put(
						"segmentsEntryName",
						() -> {
							SegmentsEntry segmentsEntry =
								_segmentsEntryLocalService.fetchSegmentsEntry(
									segmentsExperience.getSegmentsEntryId());

							if (segmentsEntry != null) {
								return segmentsEntry.getName(locale);
							}

							return LanguageUtil.get(locale, "anyone");
						}
					).put(
						"segmentsExperienceName",
						segmentsExperience.getName(locale)
					).put(
						"url", ""
					).build());
			}

			httpServletRequest.setAttribute(
				SegmentsWebKeys.SEGMENTS_EXPERIENCES_DROPDOWN_ITEMS,
				segmentsExperiencesDropdownItems);
		}
		catch (PortalException portalException) {
			_log.error(portalException);

			return false;
		}

		httpServletRequest.setAttribute(
			SegmentsWebKeys.SEGMENTS_EXPERIENCE_NAME,
			"selected-segments-experience-name");

		return super.includeIcon(httpServletRequest, httpServletResponse);
	}

	@Override
	public boolean isShow(HttpServletRequest httpServletRequest) {
		return true;
	}

	@Override
	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.segments.web)",
		unbind = "-"
	)
	public void setServletContext(ServletContext servletContext) {
		super.setServletContext(servletContext);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ExperienceSelectorProductNavigationControlMenuEntry.class);

	@Reference
	private Portal _portal;

	@Reference
	private SegmentsEntryLocalService _segmentsEntryLocalService;

	@Reference
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

}