/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.analytics.reports.web.internal.display.context;

import com.liferay.analytics.reports.info.item.ClassNameClassPKInfoItemIdentifier;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.InfoItemReference;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.Collections;
import java.util.Map;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author David Arques
 * @author Sarai Díaz
 */
public class AnalyticsReportsDisplayContext<T> {

	public AnalyticsReportsDisplayContext(
		InfoItemReference infoItemReference, Portal portal,
		RenderRequest renderRequest, RenderResponse renderResponse) {

		_infoItemReference = infoItemReference;
		_portal = portal;
		_renderRequest = renderRequest;
		_renderResponse = renderResponse;

		_themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public Map<String, Object> getData() throws PortalException {
		if (_data != null) {
			return _data;
		}

		_data = Collections.singletonMap(
			"context",
			Collections.singletonMap(
				"analyticsReportsDataURL",
				String.valueOf(
					_getResourceURL("/analytics_reports/get_data"))));

		return _data;
	}

	private String _getResourceURL(String resourceID) throws PortalException {
		String getDataURL = _themeDisplay.getPathMain() + "/portal/get_data";

		getDataURL = HttpComponentsUtil.addParameter(
			getDataURL, ParamUtil.getString(_renderRequest, "redirect"),
			"redirect");

		getDataURL = HttpComponentsUtil.addParameter(
			getDataURL, "className", _infoItemReference.getClassName());

		if (_infoItemReference.getInfoItemIdentifier() instanceof
				ClassNameClassPKInfoItemIdentifier) {

			ClassNameClassPKInfoItemIdentifier
				classNameClassPKInfoItemIdentifier =
					(ClassNameClassPKInfoItemIdentifier)
						_infoItemReference.getInfoItemIdentifier();

			getDataURL = HttpComponentsUtil.addParameter(
				getDataURL, "classPK",
				String.valueOf(
					classNameClassPKInfoItemIdentifier.getClassPK()));

			getDataURL = HttpComponentsUtil.addParameter(
				getDataURL, "classTypeName",
				classNameClassPKInfoItemIdentifier.getClassName());
		}
		else if (_infoItemReference.getInfoItemIdentifier() instanceof
					ClassPKInfoItemIdentifier) {

			ClassPKInfoItemIdentifier classPKInfoItemIdentifier =
				(ClassPKInfoItemIdentifier)
					_infoItemReference.getInfoItemIdentifier();

			getDataURL = HttpComponentsUtil.addParameter(
				getDataURL, "classPK",
				String.valueOf(classPKInfoItemIdentifier.getClassPK()));
		}

		return getDataURL;
	}

	private Map<String, Object> _data;
	private final InfoItemReference _infoItemReference;
	private final Portal _portal;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final ThemeDisplay _themeDisplay;

}