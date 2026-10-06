/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.helper;

import com.liferay.layout.util.structure.DropZoneLayoutStructureItem;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Lourdes Fernández Besada
 */
@ProviderType
public interface DropZoneAllowedFragmentEntriesHelper {

	public List<String> getFragmentEntryKeys(
		long companyId, DropZoneLayoutStructureItem dropZoneLayoutStructureItem,
		long scopeGroupId);

	public boolean isAllowedFragmentEntryKey(
		long companyId, DropZoneLayoutStructureItem dropZoneLayoutStructureItem,
		String fragmentEntryKey, long scopeGroupId);

}