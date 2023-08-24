/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.content.page.editor.web.internal.portlet.action;

import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.fragment.constants.FragmentEntryLinkConstants;
import com.liferay.fragment.entry.processor.constants.FragmentEntryProcessorConstants;
import com.liferay.fragment.entry.processor.helper.FragmentEntryProcessorHelper;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.processor.DefaultFragmentEntryProcessorContext;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.info.type.WebImage;
import com.liferay.layout.content.page.editor.constants.ContentPageEditorPortletKeys;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.HashMap;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Eudaldo Alonso
 */
@Component(
	property = {
		"javax.portlet.name=" + ContentPageEditorPortletKeys.CONTENT_PAGE_EDITOR_PORTLET,
		"mvc.command.name=/layout_content_page_editor/get_mapped_field_warning_message"
	},
	service = MVCResourceCommand.class
)
public class GetMappedFieldWarningMessageMVCResourceCommand
	extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		long fragmentEntryLinkId = ParamUtil.getLong(
			resourceRequest, "fragmentEntryLinkId");

		FragmentEntryLink fragmentEntryLink =
			_fragmentEntryLinkLocalService.fetchFragmentEntryLink(
				fragmentEntryLinkId);

		if (fragmentEntryLink == null) {
			JSONPortletResponseUtil.writeJSON(
				resourceRequest, resourceResponse,
				_jsonFactory.createJSONObject());

			return;
		}

		JSONObject jsonObject = _jsonFactory.createJSONObject();

		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		try {
			jsonObject = JSONUtil.put(
				"warningMessage",
				_getWarningMessage(
					fragmentEntryLink, resourceRequest, resourceResponse,
					themeDisplay));
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			jsonObject.put(
				"error",
				_language.get(
					themeDisplay.getLocale(), "an-unexpected-error-occurred"));
		}

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse, jsonObject);
	}

	private String _getWarningMessage(
			FragmentEntryLink fragmentEntryLink,
			ResourceRequest resourceRequest, ResourceResponse resourceResponse,
			ThemeDisplay themeDisplay)
		throws Exception {

		String fieldId = ParamUtil.getString(resourceRequest, "fieldId");

		JSONObject jsonObject = _jsonFactory.createJSONObject(
			fragmentEntryLink.getEditableValues());

		JSONObject editableValuesJSONObject = jsonObject.getJSONObject(
			FragmentEntryProcessorConstants.
				KEY_EDITABLE_FRAGMENT_ENTRY_PROCESSOR);

		if ((editableValuesJSONObject == null) ||
			!editableValuesJSONObject.has(fieldId)) {

			return StringPool.BLANK;
		}

		JSONObject editableValueJSONObject =
			editableValuesJSONObject.getJSONObject(fieldId);

		if (!_fragmentEntryProcessorHelper.isMapped(editableValueJSONObject) &&
			!_fragmentEntryProcessorHelper.isMappedCollection(
				editableValueJSONObject) &&
			!_fragmentEntryProcessorHelper.isMappedDisplayPage(
				editableValueJSONObject)) {

			return StringPool.BLANK;
		}

		JSONObject configJSONObject = editableValueJSONObject.getJSONObject(
			"config");

		if (configJSONObject.getBoolean("lazyLoading")) {
			return StringPool.BLANK;
		}

		Object fieldValue = _fragmentEntryProcessorHelper.getFieldValue(
			editableValueJSONObject, new HashMap<>(),
			new DefaultFragmentEntryProcessorContext(
				_portal.getHttpServletRequest(resourceRequest),
				_portal.getHttpServletResponse(resourceResponse),
				FragmentEntryLinkConstants.EDIT, themeDisplay.getLocale()));

		if (fieldValue == null) {
			return StringPool.BLANK;
		}

		long fileEntryId = 0;

		if (fieldValue instanceof JSONObject) {
			JSONObject fieldValueJSONObject = (JSONObject)fieldValue;

			if (fieldValueJSONObject.has("className") &&
				fieldValueJSONObject.has("classPK")) {

				fileEntryId = _fragmentEntryProcessorHelper.getFileEntryId(
					fieldValueJSONObject.getString("className"),
					fieldValueJSONObject.getLong("classPK"));
			}
			else if (fieldValueJSONObject.has("fileEntryId")) {
				fileEntryId = fieldValueJSONObject.getLong("fileEntryId");
			}
		}
		else if (fieldValue instanceof WebImage) {
			WebImage webImage = (WebImage)fieldValue;

			fileEntryId = _fragmentEntryProcessorHelper.getFileEntryId(
				webImage);
		}

		FileEntry fileEntry = _dlAppLocalService.getFileEntry(fileEntryId);

		long size = fileEntry.getSize();

		if (size < _MAX_SIZE) {
			return StringPool.BLANK;
		}

		return _language.get(
			themeDisplay.getLocale(),
			"big-image-file-size-used-please-consider-configuring-adaptive-" +
				"media-lazy-loading-or-reducing-the-image-size");
	}

	private static final int _MAX_SIZE = 500 * 1024;

	private static final Log _log = LogFactoryUtil.getLog(
		GetMappedFieldWarningMessageMVCResourceCommand.class);

	@Reference
	private DLAppLocalService _dlAppLocalService;

	@Reference
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@Reference
	private FragmentEntryProcessorHelper _fragmentEntryProcessorHelper;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Language _language;

	@Reference
	private Portal _portal;

}