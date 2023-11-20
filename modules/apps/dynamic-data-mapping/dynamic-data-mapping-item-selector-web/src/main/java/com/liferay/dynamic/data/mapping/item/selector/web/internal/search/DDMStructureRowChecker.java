/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.dynamic.data.mapping.item.selector.web.internal.search;

import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.portal.kernel.dao.search.EmptyOnClickRowChecker;
import com.liferay.portal.kernel.util.ArrayUtil;

import javax.portlet.RenderResponse;

/**
 * @author Jürgen Kappler
 */
public class DDMStructureRowChecker extends EmptyOnClickRowChecker {

	public DDMStructureRowChecker(
		RenderResponse renderResponse, long[] checkedDDMStructureIds) {

		super(renderResponse);

		_checkedDDMStructureIds = checkedDDMStructureIds;
	}

	@Override
	public boolean isChecked(Object object) {
		DDMStructure ddmStructure = (DDMStructure)object;

		return ArrayUtil.contains(
			_checkedDDMStructureIds, ddmStructure.getStructureId());
	}

	private final long[] _checkedDDMStructureIds;

}