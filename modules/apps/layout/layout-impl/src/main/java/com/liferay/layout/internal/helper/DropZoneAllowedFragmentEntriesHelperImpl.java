/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.internal.helper;

import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.layout.helper.DropZoneAllowedFragmentEntriesHelper;
import com.liferay.layout.util.structure.DropZoneLayoutStructureItem;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ScopeUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Lourdes Fernández Besada
 */
@Component(service = DropZoneAllowedFragmentEntriesHelper.class)
public class DropZoneAllowedFragmentEntriesHelperImpl
	implements DropZoneAllowedFragmentEntriesHelper {

	@Override
	public List<String> getFragmentEntryKeys(
			long companyId,
			DropZoneLayoutStructureItem dropZoneLayoutStructureItem,
			long scopeGroupId)
		throws Exception {

		JSONArray fragmentEntriesJSONArray =
			dropZoneLayoutStructureItem.getFragmentEntriesJSONArray();

		if ((fragmentEntriesJSONArray == null) ||
			(fragmentEntriesJSONArray.length() == 0)) {

			return dropZoneLayoutStructureItem.getFragmentEntryKeys();
		}

		List<String> fragmentEntryKeys = new ArrayList<>();

		for (int i = 0; i < fragmentEntriesJSONArray.length(); i++) {
			JSONObject fragmentEntryJSONObject =
				fragmentEntriesJSONArray.getJSONObject(i);

			String fragmentEntryRendererKey = fragmentEntryJSONObject.getString(
				"fragmentEntryRendererKey");

			if (Validator.isNotNull(fragmentEntryRendererKey)) {
				fragmentEntryKeys.add(fragmentEntryRendererKey);

				continue;
			}

			String fragmentEntryERC = fragmentEntryJSONObject.getString(
				"fragmentEntryERC");

			if (Validator.isNull(fragmentEntryERC)) {
				continue;
			}

			Long fragmentEntryScopeGroupId = ScopeUtil.getItemGroupId(
				companyId,
				fragmentEntryJSONObject.getString("fragmentEntryScopeERC"),
				scopeGroupId);

			if (fragmentEntryScopeGroupId == null) {
				continue;
			}

			FragmentEntry fragmentEntry =
				_fragmentEntryLocalService.
					fetchFragmentEntryByExternalReferenceCode(
						fragmentEntryERC, fragmentEntryScopeGroupId);

			if (fragmentEntry != null) {
				fragmentEntryKeys.add(fragmentEntry.getFragmentEntryKey());
			}
		}

		return fragmentEntryKeys;
	}

	@Override
	public boolean isAllowedFragmentEntryKey(
			long companyId,
			DropZoneLayoutStructureItem dropZoneLayoutStructureItem,
			String fragmentEntryKey, long scopeGroupId)
		throws Exception {

		List<String> fragmentEntryKeys = getFragmentEntryKeys(
			companyId, dropZoneLayoutStructureItem, scopeGroupId);

		if (dropZoneLayoutStructureItem.isAllowNewFragmentEntries()) {
			if (ListUtil.isEmpty(fragmentEntryKeys) ||
				!fragmentEntryKeys.contains(fragmentEntryKey)) {

				return true;
			}

			return false;
		}

		if (ListUtil.isNotEmpty(fragmentEntryKeys) &&
			fragmentEntryKeys.contains(fragmentEntryKey)) {

			return true;
		}

		return false;
	}

	@Reference
	private FragmentEntryLocalService _fragmentEntryLocalService;

}