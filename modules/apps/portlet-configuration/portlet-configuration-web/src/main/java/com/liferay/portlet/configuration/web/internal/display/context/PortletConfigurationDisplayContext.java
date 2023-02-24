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

package com.liferay.portlet.configuration.web.internal.display.context;

import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.PortletConstants;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Eudaldo Alonso
 */
public class PortletConfigurationDisplayContext {

	public PortletConfigurationDisplayContext(
		HttpServletRequest httpServletRequest) {

		_httpServletRequest = httpServletRequest;
	}

	public boolean isControlPanelPortlet() {
		String resourcePrimKey = _getResourcePrimKey();

		int pos = resourcePrimKey.indexOf(PortletConstants.LAYOUT_SEPARATOR);

		if (pos > 0) {
			Layout resourceLayout = LayoutLocalServiceUtil.fetchLayout(
				GetterUtil.getLong(resourcePrimKey.substring(0, pos)));

			if ((resourceLayout != null) &&
				resourceLayout.isTypeControlPanel()) {

				return true;
			}
		}

		return false;
	}

	private String _getResourcePrimKey() {
		if (_resourcePrimKey != null) {
			return _resourcePrimKey;
		}

		_resourcePrimKey = ParamUtil.getString(
			_httpServletRequest, "resourcePrimKey");

		return _resourcePrimKey;
	}

	private final HttpServletRequest _httpServletRequest;
	private String _resourcePrimKey;

}