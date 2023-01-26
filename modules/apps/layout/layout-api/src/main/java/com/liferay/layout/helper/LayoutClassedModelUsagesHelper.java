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

package com.liferay.layout.helper;

import com.liferay.layout.model.LayoutClassedModelUsage;

import java.util.Locale;

import javax.portlet.PortletRequest;

/**
 * @author Rubén Pulido
 */
public interface LayoutClassedModelUsagesHelper {

	public String getName(
		LayoutClassedModelUsage layoutClassedModelUsage, Locale locale);

	public String getPreviewURL(
			LayoutClassedModelUsage layoutClassedModelUsage,
			PortletRequest portletRequest)
		throws Exception;

	public String getTypeLabel(LayoutClassedModelUsage layoutClassedModelUsage);

	public boolean isShowPreview(
		LayoutClassedModelUsage layoutClassedModelUsage);

}