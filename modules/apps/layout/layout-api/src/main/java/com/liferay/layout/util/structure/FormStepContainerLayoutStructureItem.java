/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.util.structure;

import com.liferay.layout.util.constants.LayoutDataItemTypeConstants;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;

/**
 * @author Víctor Galán
 */
public class FormStepContainerLayoutStructureItem extends LayoutStructureItem {

	public FormStepContainerLayoutStructureItem(String parentItemId) {
		super(parentItemId);
	}

	public FormStepContainerLayoutStructureItem(
		String itemId, String parentId) {

		super(itemId, parentId);
	}

	@Override
	public JSONObject getItemConfigJSONObject() {
		return JSONFactoryUtil.createJSONObject();
	}

	@Override
	public String getItemType() {
		return LayoutDataItemTypeConstants.TYPE_FORM_STEP_CONTAINER;
	}

	@Override
	public void updateItemConfig(JSONObject itemConfigJSONObject) {
	}

}