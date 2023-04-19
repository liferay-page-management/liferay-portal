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

package com.liferay.analytics.reports.web.internal.portlet.action;

import com.liferay.analytics.reports.web.internal.data.provider.AnalyticsReportsDataProvider;
import com.liferay.analytics.settings.rest.manager.AnalyticsSettingsManager;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.struts.StrutsAction;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.WebKeys;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author David Arques
 */
@Component(
	property = "path=/portal/get_total_reads", service = StrutsAction.class
)
public class GetTotalReadsStrutsAction implements StrutsAction {

	@Override
	public String execute(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception {

		AnalyticsReportsDataProvider analyticsReportsDataProvider =
			new AnalyticsReportsDataProvider(_analyticsSettingsManager, _http);

		String canonicalURL = ParamUtil.getString(
			httpServletRequest, "canonicalURL");

		try {
			JSONObject jsonObject = JSONUtil.put(
				"analyticsReportsTotalReads",
				analyticsReportsDataProvider.getTotalReads(
					_portal.getCompanyId(httpServletRequest), canonicalURL));

			ServletResponseUtil.write(
				httpServletResponse, jsonObject.toString());
		}
		catch (Exception exception) {
			if (_log.isInfoEnabled()) {
				_log.info(exception);
			}

			ServletResponseUtil.write(
				httpServletResponse,
				JSONUtil.put(
					"error",
					() -> {
						ThemeDisplay themeDisplay =
							(ThemeDisplay)httpServletRequest.getAttribute(
								WebKeys.THEME_DISPLAY);

						return ResourceBundleUtil.getString(
							ResourceBundleUtil.getBundle(
								themeDisplay.getLocale(), getClass()),
							"an-unexpected-error-occurred");
					}
				).toString());
		}
		return null;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		GetTotalReadsStrutsAction.class);

	@Reference
	private AnalyticsSettingsManager _analyticsSettingsManager;

	@Reference
	private Http _http;

	@Reference
	private Portal _portal;

}