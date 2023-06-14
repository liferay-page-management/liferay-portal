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

package com.liferay.fragment.entry.processor.editable.internal.mapper;

import com.liferay.fragment.entry.processor.editable.mapper.EditableElementMapper;
import com.liferay.fragment.processor.FragmentEntryProcessorContext;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.InfoItemIdentifier;
import com.liferay.info.item.InfoItemReference;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;

import java.util.Locale;

import org.jsoup.nodes.Element;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rubén Pulido
 */
@Component(property = "type=action", service = EditableElementMapper.class)
public class ActionEditableElementMapper implements EditableElementMapper {

	@Override
	public void map(
			Element element, JSONObject configJSONObject,
			FragmentEntryProcessorContext fragmentEntryProcessorContext)
		throws PortalException {

		JSONObject mappedActionJSONObject = configJSONObject.getJSONObject(
			"mappedAction");

		if (mappedActionJSONObject == null) {
			return;
		}

		String fieldId = mappedActionJSONObject.getString("fieldId");

		if (Validator.isNull(fieldId)) {
			fieldId = mappedActionJSONObject.getString("collectionFieldId");
		}

		if (Validator.isNull(fieldId)) {
			fieldId = mappedActionJSONObject.getString("mappedField");
		}

		if (Validator.isNull(fieldId)) {
			return;
		}

		String classNameId = mappedActionJSONObject.getString("classNameId");
		String classPK = mappedActionJSONObject.getString("classPK");

		if (Validator.isNull(classNameId) || Validator.isNull(classPK)) {
			InfoItemReference infoItemReference =
				fragmentEntryProcessorContext.getContextInfoItemReference();

			if (infoItemReference == null) {
				return;
			}

			classNameId = String.valueOf(
				_portal.getClassNameId(infoItemReference.getClassName()));

			InfoItemIdentifier infoItemIdentifier =
				infoItemReference.getInfoItemIdentifier();

			if (infoItemIdentifier instanceof ClassPKInfoItemIdentifier) {
				ClassPKInfoItemIdentifier classPKInfoItemIdentifier =
					(ClassPKInfoItemIdentifier)infoItemIdentifier;

				classPK = String.valueOf(
					classPKInfoItemIdentifier.getClassPK());
			}

			if (Validator.isNull(classNameId) || Validator.isNull(classPK)) {
				return;
			}
		}

		element.attr("data-lfr-class-name-id", classNameId);
		element.attr("data-lfr-class-pk", classPK);
		element.attr("data-lfr-field-id", fieldId);

		_mapOnError(element, configJSONObject);
		_mapOnSuccess(element, configJSONObject);
	}

	private void _mapOnError(Element element, JSONObject jsonObject)
		throws PortalException {

		String onError = jsonObject.getString("onError");

		if (Validator.isNull(onError)) {
			onError = _ON_RESULT_ACTION_NONE;
		}

		element.attr("data-lfr-on-error", onError);

		ThemeDisplay themeDisplay = null;

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		if (serviceContext != null) {
			themeDisplay = serviceContext.getThemeDisplay();
		}

		if (onError.equals(_ON_RESULT_ACTION_NOTIFICATION)) {
			JSONObject textJSONObject = jsonObject.getJSONObject("errorText");

			if ((textJSONObject != null) && (themeDisplay != null)) {
				String text = textJSONObject.getString(
					themeDisplay.getLanguageId());

				if (Validator.isNotNull(text)) {
					element.attr("data-lfr-error-text", text);
				}
			}
		}
		else if (onError.equals(_ON_RESULT_ACTION_PAGE)) {
			JSONObject pageJSONObject = jsonObject.getJSONObject("errorPage");

			if (pageJSONObject != null) {
				Layout layout = _layoutLocalService.fetchLayout(
					GetterUtil.getLong(pageJSONObject.getString("groupId")),
					GetterUtil.getBoolean(
						pageJSONObject.getString("privateLayout")),
					GetterUtil.getLong(pageJSONObject.getString("layoutId")));

				if ((layout != null) && (themeDisplay != null)) {
					element.attr(
						"data-lfr-error-page-url",
						_portal.getLayoutURL(layout, themeDisplay));
				}
			}
		}
		else if (onError.equals(_ON_RESULT_ACTION_URL)) {
			JSONObject urlJSONObject = jsonObject.getJSONObject("errorURL");

			if ((urlJSONObject != null) && (themeDisplay != null)) {
				String url = urlJSONObject.getString(
					themeDisplay.getLanguageId());

				if (Validator.isNull(url)) {
					Locale locale = LocaleUtil.getSiteDefault();

					url = urlJSONObject.getString(locale.getLanguage());
				}

				if (Validator.isNotNull(url)) {
					element.attr("data-lfr-error-page-url", url);
				}
			}
		}

		if ((onError.equals(_ON_RESULT_ACTION_NONE) ||
			 onError.equals(_ON_RESULT_ACTION_NOTIFICATION)) &&
			jsonObject.getBoolean("errorReload")) {

			element.attr("data-lfr-error-reload", StringPool.TRUE);
		}
	}

	private void _mapOnSuccess(Element element, JSONObject jsonObject)
		throws PortalException {

		String onSuccess = jsonObject.getString("onSuccess");

		if (Validator.isNull(onSuccess)) {
			onSuccess = _ON_RESULT_ACTION_NONE;
		}

		element.attr("data-lfr-on-success", onSuccess);

		ThemeDisplay themeDisplay = null;

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		if (serviceContext != null) {
			themeDisplay = serviceContext.getThemeDisplay();
		}

		if (onSuccess.equals(_ON_RESULT_ACTION_NOTIFICATION)) {
			JSONObject textJSONObject = jsonObject.getJSONObject("successText");

			if ((textJSONObject != null) && (themeDisplay != null)) {
				String text = textJSONObject.getString(
					themeDisplay.getLanguageId());

				if (Validator.isNotNull(text)) {
					element.attr("data-lfr-success-text", text);
				}
			}
		}
		else if (onSuccess.equals(_ON_RESULT_ACTION_PAGE)) {
			JSONObject pageJSONObject = jsonObject.getJSONObject("successPage");

			if (pageJSONObject != null) {
				Layout layout = _layoutLocalService.fetchLayout(
					GetterUtil.getLong(pageJSONObject.getString("groupId")),
					GetterUtil.getBoolean(
						pageJSONObject.getString("privateLayout")),
					GetterUtil.getLong(pageJSONObject.getString("layoutId")));

				if ((layout != null) && (themeDisplay != null)) {
					element.attr(
						"data-lfr-success-page-url",
						_portal.getLayoutURL(layout, themeDisplay));
				}
			}
		}
		else if (onSuccess.equals(_ON_RESULT_ACTION_URL)) {
			JSONObject urlJSONObject = jsonObject.getJSONObject("successURL");

			if ((urlJSONObject != null) && (themeDisplay != null)) {
				String url = urlJSONObject.getString(
					themeDisplay.getLanguageId());

				if (Validator.isNull(url)) {
					Locale locale = LocaleUtil.getSiteDefault();

					url = urlJSONObject.getString(locale.getLanguage());
				}

				if (Validator.isNotNull(url)) {
					element.attr("data-lfr-success-page-url", url);
				}
			}
		}

		if ((onSuccess.equals(_ON_RESULT_ACTION_NONE) ||
			 onSuccess.equals(_ON_RESULT_ACTION_NOTIFICATION)) &&
			jsonObject.getBoolean("successReload")) {

			element.attr("data-lfr-success-reload", StringPool.TRUE);
		}
	}

	private static final String _ON_RESULT_ACTION_NONE = "none";

	private static final String _ON_RESULT_ACTION_NOTIFICATION = "notification";

	private static final String _ON_RESULT_ACTION_PAGE = "page";

	private static final String _ON_RESULT_ACTION_URL = "url";

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private Portal _portal;

}