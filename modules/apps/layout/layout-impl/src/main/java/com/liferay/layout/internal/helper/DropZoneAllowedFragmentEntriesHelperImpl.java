/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.internal.helper;

import com.liferay.fragment.contributor.FragmentCollectionContributorRegistry;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.renderer.FragmentRenderer;
import com.liferay.fragment.renderer.FragmentRendererRegistry;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.layout.helper.DropZoneAllowedFragmentEntriesHelper;
import com.liferay.layout.util.structure.DropZoneLayoutStructureItem;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ScopeUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		long companyId, DropZoneLayoutStructureItem dropZoneLayoutStructureItem,
		long scopeGroupId) {

		if (dropZoneLayoutStructureItem == null) {
			return Collections.emptyList();
		}

		List<String> fragmentEntryKeys =
			dropZoneLayoutStructureItem.getFragmentEntryKeys();

		if (ListUtil.isNotEmpty(fragmentEntryKeys)) {
			return fragmentEntryKeys;
		}

		JSONArray fragmentEntriesJSONArray =
			dropZoneLayoutStructureItem.getFragmentEntriesJSONArray();

		if ((fragmentEntriesJSONArray == null) ||
			(fragmentEntriesJSONArray.length() == 0)) {

			return Collections.emptyList();
		}

		fragmentEntryKeys = new ArrayList<>();

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

			if (fragmentEntry == null) {
				continue;
			}

			fragmentEntryKeys.add(fragmentEntry.getFragmentEntryKey());
		}

		return fragmentEntryKeys;
	}

	@Override
	public boolean isAllowedFragmentEntryKey(
		long companyId, DropZoneLayoutStructureItem dropZoneLayoutStructureItem,
		String fragmentEntryKey, long scopeGroupId) {

		if (dropZoneLayoutStructureItem == null) {
			return true;
		}

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

	@Override
	public JSONObject mergeFragmentEntrySettings(
			long companyId,
			DropZoneLayoutStructureItem dropZoneLayoutStructureItem,
			JSONObject itemConfigJSONObject, long scopeGroupId)
		throws PortalException {

		if ((dropZoneLayoutStructureItem == null) ||
			!itemConfigJSONObject.has("fragmentEntryKeys")) {

			return itemConfigJSONObject;
		}

		List<String> fragmentEntryKeys = JSONUtil.toStringList(
			itemConfigJSONObject.getJSONArray("fragmentEntryKeys"));

		JSONArray mergedFragmentEntriesJSONArray = JSONUtil.putAll();
		Set<String> promotedFragmentEntryKeys = new HashSet<>();

		JSONArray fragmentEntriesJSONArray =
			dropZoneLayoutStructureItem.getFragmentEntriesJSONArray();

		if (fragmentEntriesJSONArray != null) {
			for (int i = 0; i < fragmentEntriesJSONArray.length(); i++) {
				JSONObject fragmentEntryJSONObject =
					fragmentEntriesJSONArray.getJSONObject(i);

				String fragmentEntryRendererKey =
					fragmentEntryJSONObject.getString(
						"fragmentEntryRendererKey");

				if (Validator.isNotNull(fragmentEntryRendererKey)) {
					if (fragmentEntryKeys.contains(fragmentEntryRendererKey)) {
						mergedFragmentEntriesJSONArray.put(
							fragmentEntryJSONObject);
						promotedFragmentEntryKeys.add(fragmentEntryRendererKey);
					}

					continue;
				}

				String fragmentEntryERC = fragmentEntryJSONObject.getString(
					"fragmentEntryERC");

				if (Validator.isNull(fragmentEntryERC)) {
					continue;
				}

				FragmentEntry fragmentEntry = _fetchFragmentEntry(
					companyId, fragmentEntryERC,
					fragmentEntryJSONObject.getString("fragmentEntryScopeERC"),
					scopeGroupId);

				if (fragmentEntry == null) {
					mergedFragmentEntriesJSONArray.put(fragmentEntryJSONObject);

					continue;
				}

				if (!fragmentEntryKeys.contains(
						fragmentEntry.getFragmentEntryKey())) {

					continue;
				}

				mergedFragmentEntriesJSONArray.put(fragmentEntryJSONObject);
				promotedFragmentEntryKeys.add(
					fragmentEntry.getFragmentEntryKey());
			}
		}

		long[] groupIds = _getGroupIds(companyId, scopeGroupId);

		for (String fragmentEntryKey : fragmentEntryKeys) {
			if (promotedFragmentEntryKeys.contains(fragmentEntryKey)) {
				continue;
			}

			FragmentEntry defaultFragmentEntry =
				_fragmentCollectionContributorRegistry.getFragmentEntry(
					fragmentEntryKey);
			FragmentRenderer fragmentRenderer =
				_fragmentRendererRegistry.getFragmentRenderer(fragmentEntryKey);

			if ((defaultFragmentEntry != null) || (fragmentRenderer != null)) {
				mergedFragmentEntriesJSONArray.put(
					JSONUtil.put("fragmentEntryRendererKey", fragmentEntryKey));

				continue;
			}

			FragmentEntry fragmentEntry = _fetchFragmentEntry(
				fragmentEntryKey, groupIds);

			if (fragmentEntry == null) {
				continue;
			}

			mergedFragmentEntriesJSONArray.put(
				JSONUtil.put(
					"fragmentEntryERC", fragmentEntry.getExternalReferenceCode()
				).put(
					"fragmentEntryScopeERC",
					ScopeUtil.getItemScopeExternalReferenceCode(
						fragmentEntry.getGroupId(), scopeGroupId)
				));
		}

		return itemConfigJSONObject.put(
			"fragmentEntries", mergedFragmentEntriesJSONArray
		).put(
			"fragmentEntryKeys", JSONUtil.putAll()
		);
	}

	private FragmentEntry _fetchFragmentEntry(
		long companyId, String fragmentEntryERC, String fragmentEntryScopeERC,
		long scopeGroupId) {

		Long fragmentEntryScopeGroupId = ScopeUtil.getItemGroupId(
			companyId, fragmentEntryScopeERC, scopeGroupId);

		if (fragmentEntryScopeGroupId == null) {
			return null;
		}

		return _fragmentEntryLocalService.
			fetchFragmentEntryByExternalReferenceCode(
				fragmentEntryERC, fragmentEntryScopeGroupId);
	}

	private FragmentEntry _fetchFragmentEntry(
		String fragmentEntryKey, long[] groupIds) {

		for (long groupId : groupIds) {
			FragmentEntry fragmentEntry =
				_fragmentEntryLocalService.fetchFragmentEntry(
					groupId, fragmentEntryKey);

			if (fragmentEntry == null) {
				continue;
			}

			return fragmentEntry;
		}

		return null;
	}

	private long[] _getGroupIds(long companyId, long scopeGroupId)
		throws PortalException {

		Group companyGroup = _groupLocalService.getCompanyGroup(companyId);

		return new long[] {scopeGroupId, companyGroup.getGroupId()};
	}

	@Reference
	private FragmentCollectionContributorRegistry
		_fragmentCollectionContributorRegistry;

	@Reference
	private FragmentEntryLocalService _fragmentEntryLocalService;

	@Reference
	private FragmentRendererRegistry _fragmentRendererRegistry;

	@Reference
	private GroupLocalService _groupLocalService;

}