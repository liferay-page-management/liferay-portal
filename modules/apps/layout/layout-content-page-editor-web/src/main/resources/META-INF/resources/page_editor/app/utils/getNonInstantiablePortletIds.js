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

import {LAYOUT_DATA_ITEM_TYPES} from '../config/constants/layoutDataItemTypes';

/**
 * @param itemId
 * @param layoutData
 * @param fragmentEntryLinks
 * @return {string[]} List of all non instantiable portletIds contained inside
 *  given layout data item.
 */
export default function getNonInstantiablePortletIds(
	itemId,
	layoutData,
	fragmentEntryLinks
) {
	const item = layoutData.items[itemId];

	const {config = {}, children = []} = item;

	if (
		item.type === LAYOUT_DATA_ITEM_TYPES.fragment &&
		config.fragmentEntryLinkId
	) {
		const {editableValues = {}} = fragmentEntryLinks[
			config.fragmentEntryLinkId
		];

		if (editableValues.portletId && !editableValues.instanceId) {
			return [editableValues.portletId];
		}
	}

	const portletIds = [];

	children.forEach((itemId) => {
		portletIds.push(
			...getNonInstantiablePortletIds(
				itemId,
				layoutData,
				fragmentEntryLinks
			)
		);
	});

	return portletIds;
}
