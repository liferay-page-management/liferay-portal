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

import com.liferay.analytics.reports.info.item.AnalyticsReportsInfoItem;
import com.liferay.analytics.reports.info.item.AnalyticsReportsInfoItemRegistry;
import com.liferay.analytics.reports.info.item.ClassNameClassPKInfoItemIdentifier;
import com.liferay.analytics.reports.info.item.provider.AnalyticsReportsInfoItemObjectProvider;
import com.liferay.analytics.reports.web.internal.constants.AnalyticsReportsPortletKeys;
import com.liferay.analytics.reports.web.internal.data.provider.AnalyticsReportsDataProvider;
import com.liferay.analytics.reports.web.internal.info.item.provider.AnalyticsReportsInfoItemObjectProviderRegistry;
import com.liferay.analytics.reports.web.internal.model.TimeRange;
import com.liferay.analytics.reports.web.internal.model.TimeSpan;
import com.liferay.analytics.reports.web.internal.util.AnalyticsReportsUtil;
import com.liferay.analytics.settings.rest.manager.AnalyticsSettingsManager;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.InfoItemReference;
import com.liferay.info.type.WebImage;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.NoSuchModelException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.struts.StrutsAction;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Cristina González
 */
@Component(property = "path=/portal/get_data", service = StrutsAction.class)
public class GetDataStrutsAction implements StrutsAction {

	@Override
	public String execute(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception {

		JSONObject jsonObject = _jsonFactory.createJSONObject();

		try {
			InfoItemReference infoItemReference = _getInfoItemReference(
				httpServletRequest);

			AnalyticsReportsInfoItemObjectProvider<?>
				analyticsReportsInfoItemObjectProvider =
					_analyticsReportsInfoItemObjectProviderRegistry.
						getAnalyticsReportsInfoItemObjectProvider(
							infoItemReference.getClassName());

			if (analyticsReportsInfoItemObjectProvider == null) {
				throw new NoSuchModelException(
					"No analytics reports info item object provider found " +
						"for " + infoItemReference);
			}

			Object analyticsReportsInfoItemObject =
				analyticsReportsInfoItemObjectProvider.
					getAnalyticsReportsInfoItemObject(infoItemReference);

			if (analyticsReportsInfoItemObject == null) {
				throw new NoSuchModelException(
					"No analytics reports info item object provider found " +
						"for " + infoItemReference);
			}

			AnalyticsReportsInfoItem<Object> analyticsReportsInfoItem =
				(AnalyticsReportsInfoItem<Object>)
					_analyticsReportsInfoItemRegistry.
						getAnalyticsReportsInfoItem(
							infoItemReference.getClassName());

			ThemeDisplay themeDisplay =
				(ThemeDisplay)httpServletRequest.getAttribute(
					WebKeys.THEME_DISPLAY);

			jsonObject.put(
				"context",
				_getJSONObject(
					analyticsReportsInfoItem, infoItemReference,
					themeDisplay.getLayout(),
					themeDisplay.getLayoutFriendlyURL(themeDisplay.getLayout()),
					themeDisplay.getLocale(),
					_getLocale(
						httpServletRequest, themeDisplay.getLanguageId()),
					analyticsReportsInfoItemObject, httpServletRequest,
					_getTimeRange(httpServletRequest), themeDisplay));

			ServletResponseUtil.write(
				httpServletResponse, jsonObject.toString());
		}
		catch (Exception exception) {
			_log.error(exception);

			ServletResponseUtil.write(
				httpServletResponse,
				jsonObject.put(
					"error",
					_language.get(
						httpServletRequest, "an-unexpected-error-occurred")
				).toString());
		}

		return null;
	}

	private JSONObject _getAnalyticsDataJSONObject(Layout layout) {
		return JSONUtil.put(
			"cloudTrialURL", AnalyticsReportsUtil.ANALYTICS_CLOUD_TRIAL_URL
		).put(
			"hasValidConnection",
			() -> {
				AnalyticsReportsDataProvider analyticsReportsDataProvider =
					new AnalyticsReportsDataProvider(
						_analyticsSettingsManager, _http);

				return analyticsReportsDataProvider.isValidAnalyticsConnection(
					layout.getCompanyId());
			}
		).put(
			"isSynced",
			() -> _analyticsSettingsManager.isSiteIdSynced(
				layout.getCompanyId(), layout.getGroupId())
		).put(
			"url",
			() -> PrefsPropsUtil.getString(
				layout.getCompanyId(), "liferayAnalyticsURL")
		);
	}

	private JSONObject _getAuthorJSONObject(
		AnalyticsReportsInfoItem<Object> analyticsReportsInfoItem,
		Locale locale, Object object) {

		WebImage webImage = analyticsReportsInfoItem.getAuthorWebImage(
			object, locale);

		if ((webImage == null) || Validator.isNull(webImage.getUrl())) {
			return null;
		}

		long portraitId = GetterUtil.getLong(
			HttpComponentsUtil.getParameter(
				HtmlUtil.escape(webImage.getUrl()), "img_id"));

		if (portraitId > 0) {
			return JSONUtil.put(
				"authorId", analyticsReportsInfoItem.getAuthorUserId(object)
			).put(
				"name", analyticsReportsInfoItem.getAuthorName(object)
			).put(
				"url", webImage.getUrl()
			);
		}

		return JSONUtil.put(
			"authorId", analyticsReportsInfoItem.getAuthorUserId(object)
		).put(
			"name", analyticsReportsInfoItem.getAuthorName(object)
		);
	}

	private String _getClassName(HttpServletRequest httpServletRequest) {
		String className = ParamUtil.getString(httpServletRequest, "className");

		if (Validator.isNull(className)) {
			return Layout.class.getName();
		}

		return className;
	}

	private long _getClassPK(HttpServletRequest httpServletRequest) {
		long classPK = ParamUtil.getLong(httpServletRequest, "classPK");

		if (classPK == 0) {
			ThemeDisplay themeDisplay =
				(ThemeDisplay)httpServletRequest.getAttribute(
					WebKeys.THEME_DISPLAY);

			return themeDisplay.getPlid();
		}

		return classPK;
	}

	private String _getClassTypeName(HttpServletRequest httpServletRequest) {
		return ParamUtil.getString(httpServletRequest, "classTypeName");
	}

	private JSONObject _getEndpointsJSONObject(
		AnalyticsReportsInfoItem<Object> analyticsReportsInfoItem,
		String canonicalURL, Locale locale, ThemeDisplay themeDisplay) {

		JSONObject jsonObject = _jsonFactory.createJSONObject();

		for (AnalyticsReportsInfoItem.Action action :
				analyticsReportsInfoItem.getActions()) {

			ObjectValuePair<String, String> objectValuePair =
				_objectValuePairs.get(action);

			if (objectValuePair == null) {
				continue;
			}

			jsonObject.put(
				objectValuePair.getKey(),
				_getEndpointsURL(
					canonicalURL, locale, objectValuePair.getValue(),
					themeDisplay));
		}

		return jsonObject;
	}

	private String _getEndpointsURL(
		String canonicalURL, Locale locale, String getEndpointsURL,
		ThemeDisplay themeDisplay) {

		getEndpointsURL =
			themeDisplay.getPortalURL() + themeDisplay.getPathMain() +
				getEndpointsURL;

		getEndpointsURL = HttpComponentsUtil.addParameter(
			getEndpointsURL, "canonicalURL", canonicalURL);

		return HttpComponentsUtil.addParameter(
			getEndpointsURL, "languageId", LocaleUtil.toLanguageId(locale));
	}

	private InfoItemReference _getInfoItemReference(
		HttpServletRequest httpServletRequest) {

		String classTypeName = _getClassTypeName(httpServletRequest);

		if (Validator.isNull(classTypeName)) {
			return new InfoItemReference(
				_getClassName(httpServletRequest),
				_getClassPK(httpServletRequest));
		}

		return new InfoItemReference(
			_getClassName(httpServletRequest),
			new ClassNameClassPKInfoItemIdentifier(
				classTypeName, _getClassPK(httpServletRequest)));
	}

	private JSONObject _getJSONObject(
		AnalyticsReportsInfoItem<Object> analyticsReportsInfoItem,
		InfoItemReference infoItemReference, Layout layout,
		String layoutFriendlyURL, Locale locale, Locale urlLocale,
		Object object, HttpServletRequest httpServletRequest,
		TimeRange timeRange, ThemeDisplay themeDisplay) {

		String canonicalURL = analyticsReportsInfoItem.getCanonicalURL(
			object, urlLocale);
		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			locale, getClass());

		return JSONUtil.put(
			"analyticsData", _getAnalyticsDataJSONObject(layout)
		).put(
			"author",
			_getAuthorJSONObject(analyticsReportsInfoItem, locale, object)
		).put(
			"canonicalURL", canonicalURL
		).put(
			"endpoints",
			_getEndpointsJSONObject(
				analyticsReportsInfoItem, canonicalURL, urlLocale, themeDisplay)
		).put(
			"languageTag", locale.toLanguageTag()
		).put(
			"namespace",
			_portal.getPortletNamespace(
				AnalyticsReportsPortletKeys.ANALYTICS_REPORTS)
		).put(
			"page", JSONUtil.put("plid", layout.getPlid())
		).put(
			"pathToAssets", _portal.getPathContext(httpServletRequest)
		).put(
			"publishDate",
			_toISODateFormat(
				_toLocaleDate(analyticsReportsInfoItem.getPublishDate(object)))
		).put(
			"timeRange",
			JSONUtil.put(
				"endDate", _toISODateFormat(timeRange.getEndLocalDate())
			).put(
				"startDate", _toISODateFormat(timeRange.getStartLocalDate())
			)
		).put(
			"timeSpanKey", _getTimeSpanKey(timeRange)
		).put(
			"timeSpans", _getTimeSpansJSONArray(resourceBundle)
		).put(
			"title", analyticsReportsInfoItem.getTitle(object, urlLocale)
		).put(
			"viewURLs",
			_getViewURLsJSONArray(
				analyticsReportsInfoItem, infoItemReference, locale, object,
				themeDisplay.getPortalURL() + themeDisplay.getPathMain() +
					"/portal/get_data",
				urlLocale)
		);
	}

	private Locale _getLocale(
		HttpServletRequest httpServletRequest, String languageId) {

		return LocaleUtil.fromLanguageId(
			ParamUtil.getString(httpServletRequest, "languageId", languageId));
	}

	private String _getResourceURL(
		InfoItemReference infoItemReference, Locale locale, String getDataURL) {

		getDataURL = HttpComponentsUtil.addParameter(
			getDataURL, "className", infoItemReference.getClassName());

		getDataURL = HttpComponentsUtil.addParameter(
			getDataURL, "languageId", LocaleUtil.toLanguageId(locale));

		if (infoItemReference.getInfoItemIdentifier() instanceof
				ClassNameClassPKInfoItemIdentifier) {

			ClassNameClassPKInfoItemIdentifier
				classNameClassPKInfoItemIdentifier =
					(ClassNameClassPKInfoItemIdentifier)
						infoItemReference.getInfoItemIdentifier();

			getDataURL = HttpComponentsUtil.addParameter(
				getDataURL, "classPK",
				String.valueOf(
					classNameClassPKInfoItemIdentifier.getClassPK()));

			getDataURL = HttpComponentsUtil.addParameter(
				getDataURL, "classTypeName",
				classNameClassPKInfoItemIdentifier.getClassName());
		}
		else if (infoItemReference.getInfoItemIdentifier() instanceof
					ClassPKInfoItemIdentifier) {

			ClassPKInfoItemIdentifier classPKInfoItemIdentifier =
				(ClassPKInfoItemIdentifier)
					infoItemReference.getInfoItemIdentifier();

			getDataURL = HttpComponentsUtil.addParameter(
				getDataURL, "classPK",
				String.valueOf(classPKInfoItemIdentifier.getClassPK()));
		}

		return getDataURL;
	}

	private TimeRange _getTimeRange(HttpServletRequest httpServletRequest) {
		String timeSpanKey = ParamUtil.getString(
			httpServletRequest, "timeSpanKey", TimeSpan.defaultTimeSpanKey());

		if (Validator.isNull(timeSpanKey)) {
			TimeSpan defaultTimeSpan = TimeSpan.of(
				TimeSpan.defaultTimeSpanKey());

			return defaultTimeSpan.toTimeRange(0);
		}

		TimeSpan timeSpan = TimeSpan.of(timeSpanKey);

		int timeSpanOffset = ParamUtil.getInteger(
			httpServletRequest, "timeSpanOffset");

		return timeSpan.toTimeRange(timeSpanOffset);
	}

	private String _getTimeSpanKey(TimeRange timeRange) {
		TimeSpan timeSpan = timeRange.getTimeSpan();

		return timeSpan.getKey();
	}

	private JSONArray _getTimeSpansJSONArray(ResourceBundle resourceBundle) {
		List<TimeSpan> timeSpans = Arrays.asList(TimeSpan.values());

		timeSpans = ListUtil.filter(
			timeSpans, timeSpan -> timeSpan != TimeSpan.TODAY);

		timeSpans.sort(Comparator.comparingInt(TimeSpan::getDays));

		return JSONUtil.toJSONArray(
			timeSpans,
			timeSpan -> JSONUtil.put(
				"key", timeSpan.getKey()
			).put(
				"label",
				ResourceBundleUtil.getString(resourceBundle, timeSpan.getKey())
			),
			_log);
	}

	private JSONArray _getViewURLsJSONArray(
		AnalyticsReportsInfoItem<Object> analyticsReportsInfoItem,
		InfoItemReference infoItemReference, Locale locale, Object object,
		String getDataURL, Locale urlLocale) {

		List<Locale> locales = ListUtil.copy(
			analyticsReportsInfoItem.getAvailableLocales(object));

		locales.sort(
			(locale1, locale2) -> {
				if (Objects.equals(
						locale1,
						analyticsReportsInfoItem.getDefaultLocale(object))) {

					return -1;
				}

				if (Objects.equals(
						locale2,
						analyticsReportsInfoItem.getDefaultLocale(object))) {

					return 1;
				}

				String displayLanguage1 = locale1.getDisplayLanguage(locale);
				String displayLanguage2 = locale2.getDisplayLanguage(locale);

				return displayLanguage1.compareToIgnoreCase(displayLanguage2);
			});

		return JSONUtil.putAll(
			TransformUtil.transformToArray(
				locales,
				currentLocale -> JSONUtil.put(
					"default",
					Objects.equals(
						currentLocale,
						analyticsReportsInfoItem.getDefaultLocale(object))
				).put(
					"languageId", LocaleUtil.toW3cLanguageId(currentLocale)
				).put(
					"languageLabel",
					StringBundler.concat(
						currentLocale.getDisplayLanguage(locale),
						StringPool.SPACE, StringPool.OPEN_PARENTHESIS,
						currentLocale.getDisplayCountry(locale),
						StringPool.CLOSE_PARENTHESIS)
				).put(
					"selected", Objects.equals(currentLocale, urlLocale)
				).put(
					"viewURL",
					_getResourceURL(
						infoItemReference, currentLocale, getDataURL)
				),
				JSONObject.class));
	}

	private String _toISODateFormat(LocalDate localDate) {
		if (localDate == null) {
			return null;
		}

		return DateTimeFormatter.ISO_DATE.format(localDate);
	}

	private LocalDate _toLocaleDate(Date date) {
		if (date == null) {
			return null;
		}

		Instant instant = date.toInstant();

		ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());

		return zonedDateTime.toLocalDate();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		GetDataStrutsAction.class);

	private static final Map
		<AnalyticsReportsInfoItem.Action, ObjectValuePair<String, String>>
			_objectValuePairs =
				HashMapBuilder.
					<AnalyticsReportsInfoItem.Action,
					 ObjectValuePair<String, String>>put(
						AnalyticsReportsInfoItem.Action.HISTORICAL_READS,
						new ObjectValuePair<>(
							"analyticsReportsHistoricalReadsURL",
							"/portal/get_historical_reads")
					).put(
						AnalyticsReportsInfoItem.Action.HISTORICAL_VIEWS,
						new ObjectValuePair<>(
							"analyticsReportsHistoricalViewsURL",
							"/portal/get_historical_views")
					).put(
						AnalyticsReportsInfoItem.Action.TOTAL_READS,
						new ObjectValuePair<>(
							"analyticsReportsTotalReadsURL",
							"/portal/get_total_reads")
					).put(
						AnalyticsReportsInfoItem.Action.TOTAL_VIEWS,
						new ObjectValuePair<>(
							"analyticsReportsTotalViewsURL",
							"/portal/get_total_views")
					).put(
						AnalyticsReportsInfoItem.Action.TRAFFIC_CHANNELS,
						new ObjectValuePair<>(
							"analyticsReportsTrafficSourcesURL",
							"/analytics_reports/get_traffic_sources")
					).build();

	@Reference
	private AnalyticsReportsInfoItemObjectProvider
		_analyticsReportsInfoItemObjectProvider;

	@Reference
	private AnalyticsReportsInfoItemObjectProviderRegistry
		_analyticsReportsInfoItemObjectProviderRegistry;

	@Reference
	private AnalyticsReportsInfoItemRegistry _analyticsReportsInfoItemRegistry;

	@Reference
	private AnalyticsSettingsManager _analyticsSettingsManager;

	@Reference
	private Http _http;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Language _language;

	@Reference
	private Portal _portal;

}