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

package com.liferay.layout.content.page.editor.web.internal.util;

import com.liferay.layout.content.page.editor.web.internal.layout.contents.contributor.LayoutContentsContributorTracker;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán
 */
@Component(service = {})
public class LayoutContentContributorTrackerUtil {

	public static JSONArray getAllLayoutContentsJSONArray(
		HttpServletRequest httpServletRequest, long plid) {

		if (_layoutContentsContributorTracker != null) {
			return _layoutContentsContributorTracker.
				getAllLayoutContentsJSONArray(httpServletRequest, plid);
		}

		return JSONFactoryUtil.createJSONArray();
	}

	@Reference(unbind = "-")
	protected void setLayoutContentContributorTracker(
		LayoutContentsContributorTracker layoutContentsContributorTracker) {

		_layoutContentsContributorTracker = layoutContentsContributorTracker;
	}

	private static LayoutContentsContributorTracker
		_layoutContentsContributorTracker;

}