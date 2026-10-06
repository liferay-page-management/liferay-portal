/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.dto.v1_0.converter;

import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.fragment.contributor.util.FragmentCollectionContributorRegistryUtil;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.headless.admin.site.dto.v1_0.DefaultFragmentReference;
import com.liferay.headless.admin.site.dto.v1_0.DropZonePageElementDefinition;
import com.liferay.headless.admin.site.dto.v1_0.FragmentItemExternalReference;
import com.liferay.headless.admin.site.dto.v1_0.FragmentReference;
import com.liferay.headless.admin.site.dto.v1_0.PageElementDefinition;
import com.liferay.headless.admin.site.internal.dto.v1_0.util.ItemScopeUtil;
import com.liferay.layout.util.structure.DropZoneLayoutStructureItem;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.portal.vulcan.scope.Scope;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mikel Lorza
 */
@Component(
	property = {
		"default=true",
		"dto.class.name=com.liferay.layout.util.structure.DropZoneLayoutStructureItem"
	},
	service = DTOConverter.class
)
public class DropZonePageElementDefinitionDTOConverter
	implements DTOConverter
		<DropZoneLayoutStructureItem, DropZonePageElementDefinition> {

	@Override
	public String getContentType() {
		return DropZonePageElementDefinition.class.getSimpleName();
	}

	@Override
	public DropZonePageElementDefinition toDTO(
			DTOConverterContext dtoConverterContext,
			DropZoneLayoutStructureItem dropZoneLayoutStructureItem)
		throws Exception {

		Long companyId = (Long)dtoConverterContext.getAttribute("companyId");
		Long scopeGroupId = (Long)dtoConverterContext.getAttribute(
			"scopeGroupId");

		if ((companyId == null) || (scopeGroupId == null)) {
			throw new UnsupportedOperationException();
		}

		DropZonePageElementDefinition dropZonePageElementDefinition =
			new DropZonePageElementDefinition();

		dropZonePageElementDefinition.setAddNewFragmentEntries(
			dropZoneLayoutStructureItem::isAllowNewFragmentEntries);
		dropZonePageElementDefinition.setAllowedFragmentReferences(
			() -> _getFragmentReferences(
				companyId, dropZoneLayoutStructureItem, scopeGroupId));
		dropZonePageElementDefinition.setType(
			PageElementDefinition.Type.DROP_ZONE);

		return dropZonePageElementDefinition;
	}

	private FragmentReference[] _getFragmentReferences(
			long companyId,
			DropZoneLayoutStructureItem dropZoneLayoutStructureItem,
			long scopeGroupId)
		throws Exception {

		List<String> fragmentEntryKeys =
			dropZoneLayoutStructureItem.getFragmentEntryKeys();

		if (ListUtil.isNotEmpty(fragmentEntryKeys)) {
			return _getFragmentReferences(
				companyId, fragmentEntryKeys, scopeGroupId);
		}

		JSONArray fragmentEntriesJSONArray =
			dropZoneLayoutStructureItem.getFragmentEntriesJSONArray();

		if ((fragmentEntriesJSONArray != null) &&
			(fragmentEntriesJSONArray.length() > 0)) {

			return _getFragmentReferences(
				companyId, fragmentEntriesJSONArray, scopeGroupId);
		}

		return null;
	}

	private FragmentReference[] _getFragmentReferences(
		long companyId, JSONArray fragmentEntriesJSONArray, long scopeGroupId) {

		List<FragmentReference> fragmentReferences = new ArrayList<>();

		for (int i = 0; i < fragmentEntriesJSONArray.length(); i++) {
			JSONObject jsonObject = fragmentEntriesJSONArray.getJSONObject(i);

			if (jsonObject.has("fragmentEntryERC")) {
				fragmentReferences.add(
					_toFragmentItemExternalReference(
						jsonObject.getString("fragmentEntryERC"),
						ItemScopeUtil.getItemScope(
							companyId,
							jsonObject.getString("fragmentEntryScopeERC"),
							scopeGroupId)));
			}
			else if (jsonObject.has("fragmentEntryRendererKey")) {
				fragmentReferences.add(
					_toDefaultFragmentReference(
						jsonObject.getString("fragmentEntryRendererKey")));
			}
		}

		return fragmentReferences.toArray(new FragmentReference[0]);
	}

	private FragmentReference[] _getFragmentReferences(
			long companyId, List<String> fragmentEntryKeys, long scopeGroupId)
		throws Exception {

		long[] groupIds = _getGroupIds(companyId, scopeGroupId);

		List<FragmentReference> fragmentReferences = new ArrayList<>();

		for (String fragmentEntryKey : fragmentEntryKeys) {
			FragmentEntry fragmentEntry =
				FragmentCollectionContributorRegistryUtil.getFragmentEntry(
					fragmentEntryKey);

			if (fragmentEntry != null) {
				fragmentReferences.add(
					_toDefaultFragmentReference(fragmentEntryKey));

				continue;
			}

			for (long groupId : groupIds) {
				fragmentEntry = _fragmentEntryLocalService.fetchFragmentEntry(
					groupId, fragmentEntryKey);

				if (fragmentEntry == null) {
					continue;
				}

				fragmentReferences.add(
					_toFragmentItemExternalReference(
						fragmentEntry.getExternalReferenceCode(),
						ItemScopeUtil.getItemScope(
							fragmentEntry.getGroupId(), scopeGroupId)));
			}
		}

		return fragmentReferences.toArray(new FragmentReference[0]);
	}

	private long[] _getGroupIds(long companyId, long groupId) throws Exception {
		Group companyGroup = _groupLocalService.getCompanyGroup(companyId);

		long[] groupIds = {groupId, companyGroup.getGroupId()};

		if (!FeatureFlagManagerUtil.isEnabled(companyId, "LPD-57283")) {
			return groupIds;
		}

		return ArrayUtil.append(
			groupIds,
			TransformUtil.transformToLongArray(
				_depotEntryLocalService.getGroupConnectedDepotEntries(
					groupId, DepotConstants.TYPE_DESIGN_LIBRARY,
					QueryUtil.ALL_POS, QueryUtil.ALL_POS),
				DepotEntry::getGroupId));
	}

	private DefaultFragmentReference _toDefaultFragmentReference(
		String defaultFragmentKey) {

		DefaultFragmentReference defaultFragmentReference =
			new DefaultFragmentReference();

		defaultFragmentReference.setDefaultFragmentKey(
			() -> defaultFragmentKey);
		defaultFragmentReference.setFragmentReferenceType(
			() ->
				FragmentReference.FragmentReferenceType.
					DEFAULT_FRAGMENT_REFERENCE);

		return defaultFragmentReference;
	}

	private FragmentItemExternalReference _toFragmentItemExternalReference(
		String externalReferenceCode, Scope scope) {

		FragmentItemExternalReference fragmentItemExternalReference =
			new FragmentItemExternalReference();

		fragmentItemExternalReference.setExternalReferenceCode(
			() -> externalReferenceCode);
		fragmentItemExternalReference.setFragmentReferenceType(
			() ->
				FragmentReference.FragmentReferenceType.
					FRAGMENT_ITEM_EXTERNAL_REFERENCE);

		fragmentItemExternalReference.setScope(() -> scope);

		return fragmentItemExternalReference;
	}

	@Reference
	private DepotEntryLocalService _depotEntryLocalService;

	@Reference
	private FragmentEntryLocalService _fragmentEntryLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

}