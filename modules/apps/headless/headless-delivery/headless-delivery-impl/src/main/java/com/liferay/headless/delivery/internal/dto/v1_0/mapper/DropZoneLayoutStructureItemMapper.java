/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.internal.dto.v1_0.mapper;

import com.liferay.headless.delivery.dto.v1_0.Fragment;
import com.liferay.headless.delivery.dto.v1_0.PageDropZoneDefinition;
import com.liferay.headless.delivery.dto.v1_0.PageElement;
import com.liferay.layout.helper.DropZoneAllowedFragmentEntriesHelper;
import com.liferay.layout.util.structure.DropZoneLayoutStructureItem;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.HashMapBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jürgen Kappler
 */
public class DropZoneLayoutStructureItemMapper
	implements LayoutStructureItemMapper {

	public DropZoneLayoutStructureItemMapper(
		DropZoneAllowedFragmentEntriesHelper
			dropZoneAllowedFragmentEntriesHelper,
		GroupLocalService groupLocalService) {

		_dropZoneAllowedFragmentEntriesHelper =
			dropZoneAllowedFragmentEntriesHelper;
		_groupLocalService = groupLocalService;
	}

	@Override
	public PageElement getPageElement(
		long groupId, LayoutStructureItem layoutStructureItem,
		boolean saveInlineContent, boolean saveMappingConfiguration) {

		return new PageElement() {
			{
				setDefinition(
					() -> new PageDropZoneDefinition() {
						{
							setFragmentSettings(
								() -> _toFragmentSettingsMap(
									(DropZoneLayoutStructureItem)
										layoutStructureItem,
									groupId));
						}
					});
				setId(layoutStructureItem::getItemId);
				setType(() -> Type.DROP_ZONE);
			}
		};
	}

	private Map<String, Fragment[]> _toFragmentSettingsMap(
		DropZoneLayoutStructureItem dropZoneLayoutStructureItem, long groupId) {

		Group group = _groupLocalService.fetchGroup(groupId);

		long companyId = (group == null) ? 0 : group.getCompanyId();

		List<String> fragmentEntryKeys =
			_dropZoneAllowedFragmentEntriesHelper.getFragmentEntryKeys(
				companyId, dropZoneLayoutStructureItem, groupId);

		if (dropZoneLayoutStructureItem.isAllowNewFragmentEntries()) {
			return HashMapBuilder.put(
				"unallowedFragments", _toFragments(fragmentEntryKeys)
			).build();
		}

		return HashMapBuilder.put(
			"allowedFragments", _toFragments(fragmentEntryKeys)
		).build();
	}

	private Fragment[] _toFragments(List<String> fragmentEntryKeys) {
		List<Fragment> fragments = new ArrayList<>();

		if (fragmentEntryKeys == null) {
			return fragments.toArray(new Fragment[0]);
		}

		for (String fragmentEntryKey : fragmentEntryKeys) {
			fragments.add(
				new Fragment() {
					{
						setKey(() -> fragmentEntryKey);
					}
				});
		}

		return fragments.toArray(new Fragment[0]);
	}

	private final DropZoneAllowedFragmentEntriesHelper
		_dropZoneAllowedFragmentEntriesHelper;
	private final GroupLocalService _groupLocalService;

}