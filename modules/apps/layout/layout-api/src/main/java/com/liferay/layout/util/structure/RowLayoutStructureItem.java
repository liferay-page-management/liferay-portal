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

package com.liferay.layout.util.structure;

import com.liferay.layout.util.constants.LayoutDataItemTypeConstants;
import com.liferay.petra.lang.HashUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;

import java.util.Objects;

/**
 * @author Eudaldo Alonso
 */
public class RowLayoutStructureItem extends LayoutStructureItem {

	public RowLayoutStructureItem(String parentItemId) {
		super(parentItemId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof RowLayoutStructureItem)) {
			return false;
		}

		RowLayoutStructureItem rowLayoutStructureItem =
			(RowLayoutStructureItem)obj;

		if (!Objects.equals(_gutters, rowLayoutStructureItem._gutters) ||
			!Objects.equals(
				_modulesPerRow, rowLayoutStructureItem._numberOfColumns) ||
			!Objects.equals(
				_numberOfColumns, rowLayoutStructureItem._numberOfColumns) ||
			!Objects.equals(
				_reverseOrder, rowLayoutStructureItem._reverseOrder) ||
			!Objects.equals(
				_verticalAlignment,
				rowLayoutStructureItem._verticalAlignment)) {

			return false;
		}

		return super.equals(obj);
	}

	@Override
	public JSONObject getItemConfigJSONObject() {
		return JSONUtil.put(
			"gutters", _gutters
		).put(
			"modulesPerRow", _modulesPerRow
		).put(
			"numberOfColumns", _numberOfColumns
		).put(
			"reverseOrder", _reverseOrder
		).put(
			"verticalAlignment", _verticalAlignment
		);
	}

	@Override
	public String getItemType() {
		return LayoutDataItemTypeConstants.TYPE_ROW;
	}

	public int getModulesPerRow() {
		return _modulesPerRow;
	}

	public int getNumberOfColumns() {
		return _numberOfColumns;
	}

	public String getVerticalAlignment() {
		return _verticalAlignment;
	}

	@Override
	public int hashCode() {
		return HashUtil.hash(0, getItemId());
	}

	public boolean isGutters() {
		return _gutters;
	}

	public boolean isReverseOrder() {
		return _reverseOrder;
	}

	public void setGutters(boolean gutters) {
		_gutters = gutters;
	}

	public void setModulesPerRow(int modulesPerRow) {
		_modulesPerRow = modulesPerRow;
	}

	public void setNumberOfColumns(int numberOfColumns) {
		_numberOfColumns = numberOfColumns;
	}

	public void setReverseOrder(boolean reverseOrder) {
		_reverseOrder = reverseOrder;
	}

	public void setVerticalAlignment(String verticalAlignment) {
		_verticalAlignment = verticalAlignment;
	}

	@Override
	public void updateItemConfig(JSONObject itemConfigJSONObject) {
		if (itemConfigJSONObject.has("gutters")) {
			setGutters(itemConfigJSONObject.getBoolean("gutters"));
		}

		if (itemConfigJSONObject.has("modulesPerRow")) {
			setModulesPerRow(itemConfigJSONObject.getInt("modulesPerRow"));
		}

		if (itemConfigJSONObject.has("numberOfColumns")) {
			setNumberOfColumns(itemConfigJSONObject.getInt("numberOfColumns"));
		}

		if (itemConfigJSONObject.has("reverseOrder")) {
			setReverseOrder(itemConfigJSONObject.getBoolean("reverseOrder"));
		}

		if (itemConfigJSONObject.has("verticalAlignment")) {
			setVerticalAlignment(
				itemConfigJSONObject.getString("verticalAlignment"));
		}
	}

	private boolean _gutters = true;
	private int _modulesPerRow = 3;
	private int _numberOfColumns;
	private boolean _reverseOrder = true;
	private String _verticalAlignment = "top";

}