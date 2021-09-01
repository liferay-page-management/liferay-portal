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

import com.liferay.fragment.constants.FragmentEntryLinkConstants;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.renderer.DefaultFragmentRendererContext;
import com.liferay.fragment.renderer.FragmentRendererController;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;

import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

/**
 * @author Pablo Molina
 */
public class CollectionDisplayUtil {

	public static JSONArray unlinkCollectionDisplayRelatedFilters(
			ActionRequest actionRequest, ActionResponse actionResponse,
			String collectionDisplayItemId,
			FragmentEntryLinkLocalService fragmentEntryLinkLocalService,
			FragmentRendererController fragmentRendererController,
			String languageId, Portal portal, long segmentsExperienceId,
			ThemeDisplay themeDisplay)
		throws Exception {

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		List<FragmentEntryLink> fragmentEntryLinks =
			fragmentEntryLinkLocalService.
				getFragmentEntryLinksBySegmentsExperienceId(
					themeDisplay.getScopeGroupId(), segmentsExperienceId,
					themeDisplay.getPlid(), _KEY_FILTER_FRAGMENT_RENDERER);

		for (FragmentEntryLink fragmentEntryLink : fragmentEntryLinks) {
			JSONObject editableValuesJSONObject =
				JSONFactoryUtil.createJSONObject(
					fragmentEntryLink.getEditableValues());

			if (!JSONUtil.isValid(
					editableValuesJSONObject.getString(
						_KEY_FREEMARKER_FRAGMENT_ENTRY_PROCESSOR))) {

				continue;
			}

			JSONObject configurationJSONObject =
				editableValuesJSONObject.getJSONObject(
					_KEY_FREEMARKER_FRAGMENT_ENTRY_PROCESSOR);

			if (!configurationJSONObject.has("targetCollections")) {
				continue;
			}

			List<String> targetCollections = JSONUtil.toStringList(
				configurationJSONObject.getJSONArray("targetCollections"));

			if (!targetCollections.contains(collectionDisplayItemId)) {
				continue;
			}

			targetCollections.remove(collectionDisplayItemId);

			configurationJSONObject.put(
				"targetCollections",
				JSONUtil.toJSONArray(
					targetCollections,
					targetCollectionItemId -> targetCollectionItemId));

			if (targetCollections.isEmpty()) {
				configurationJSONObject.put("filterKey", StringPool.BLANK);
			}

			editableValuesJSONObject.put(
				_KEY_FREEMARKER_FRAGMENT_ENTRY_PROCESSOR,
				configurationJSONObject);

			long fragmentEntryLinkId =
				fragmentEntryLink.getFragmentEntryLinkId();

			fragmentEntryLink =
				fragmentEntryLinkLocalService.updateFragmentEntryLink(
					fragmentEntryLinkId, editableValuesJSONObject.toString());

			DefaultFragmentRendererContext defaultFragmentRendererContext =
				new DefaultFragmentRendererContext(fragmentEntryLink);

			defaultFragmentRendererContext.setLocale(
				LocaleUtil.fromLanguageId(languageId));

			defaultFragmentRendererContext.setMode(
				FragmentEntryLinkConstants.EDIT);

			jsonArray.put(
				JSONUtil.put(
					"content",
					fragmentRendererController.render(
						defaultFragmentRendererContext,
						portal.getHttpServletRequest(actionRequest),
						portal.getHttpServletResponse(actionResponse))
				).put(
					"editableValues", editableValuesJSONObject
				).put(
					"fragmentEntryLinkId", String.valueOf(fragmentEntryLinkId)
				));
		}

		return jsonArray;
	}

	private static final String _KEY_FILTER_FRAGMENT_RENDERER =
		"com.liferay.fragment.renderer.collection.filter.internal." +
			"CollectionFilterFragmentRenderer";

	private static final String _KEY_FREEMARKER_FRAGMENT_ENTRY_PROCESSOR =
		"com.liferay.fragment.entry.processor.freemarker." +
			"FreeMarkerFragmentEntryProcessor";

}