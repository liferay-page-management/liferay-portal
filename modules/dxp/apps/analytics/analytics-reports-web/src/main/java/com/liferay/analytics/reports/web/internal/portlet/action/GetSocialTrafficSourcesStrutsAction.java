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
import com.liferay.analytics.reports.web.internal.model.ReferringSocialMedia;
import com.liferay.analytics.reports.web.internal.model.TimeRange;
import com.liferay.analytics.reports.web.internal.model.TimeSpan;
import com.liferay.analytics.settings.rest.manager.AnalyticsSettingsManager;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.struts.StrutsAction;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Cristina González
 */
@Component(
	property = {
		"path=/portal/get_social_traffic_sources"
	},
	service = StrutsAction.class
)
public class GetSocialTrafficSourcesStrutsAction
	implements StrutsAction {

	@Override
	public String execute(
		HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			themeDisplay.getLocale(), getClass());

		try {
			AnalyticsReportsDataProvider analyticsReportsDataProvider =
				new AnalyticsReportsDataProvider(
					_analyticsSettingsManager, _http);

			String canonicalURL = ParamUtil.getString(
				httpServletRequest, "canonicalURL");

			String timeSpanKey = ParamUtil.getString(
				httpServletRequest, "timeSpanKey", TimeSpan.defaultTimeSpanKey());

			TimeSpan timeSpan = TimeSpan.of(timeSpanKey);

			int timeSpanOffset = ParamUtil.getInteger(
				httpServletRequest, "timeSpanOffset");

			JSONObject jsonObject = JSONUtil.put(
				"referringSocialMedia",
				_getReferringSocialMediaJSONArray(
					_getReferringSocialMediaList(
						analyticsReportsDataProvider, canonicalURL,
						themeDisplay.getCompanyId(),
						timeSpan.toTimeRange(timeSpanOffset)),
					resourceBundle));

			ServletResponseUtil.write(
				httpServletResponse, jsonObject.toString());
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			ServletResponseUtil.write(
				httpServletResponse,
				JSONUtil.put(
					"error",
					ResourceBundleUtil.getString(
						resourceBundle, "an-unexpected-error-occurred")).toString());
		}
		return null;
	}

	private JSONArray _getReferringSocialMediaJSONArray(
		List<ReferringSocialMedia> referringSocialMediaList,
		ResourceBundle resourceBundle) {

		if (ListUtil.isEmpty(referringSocialMediaList)) {
			return _jsonFactory.createJSONArray();
		}

		Comparator<ReferringSocialMedia> comparator = Comparator.comparingInt(
			ReferringSocialMedia::getTrafficAmount);

		referringSocialMediaList = ListUtil.filter(
			referringSocialMediaList,
			referringSocialMedia ->
				referringSocialMedia.getTrafficAmount() > 0);

		referringSocialMediaList.sort(comparator.reversed());

		return JSONUtil.toJSONArray(
			referringSocialMediaList,
			referringSocialMedia -> referringSocialMedia.toJSONObject(
				resourceBundle),
			_log);
	}

	private List<ReferringSocialMedia> _getReferringSocialMediaList(
			AnalyticsReportsDataProvider analyticsReportsDataProvider,
			String canonicalURL, long companyId, TimeRange timeRange)
		throws Exception {

		if (!analyticsReportsDataProvider.isValidAnalyticsConnection(
				companyId)) {

			throw new PortalException("Unable to get social media ");
		}

		return analyticsReportsDataProvider.getReferringSocialMediaList(
			companyId, timeRange, canonicalURL);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		GetSocialTrafficSourcesStrutsAction.class);

	@Reference
	private AnalyticsSettingsManager _analyticsSettingsManager;

	@Reference
	private Http _http;

	@Reference
	private JSONFactory _jsonFactory;

}