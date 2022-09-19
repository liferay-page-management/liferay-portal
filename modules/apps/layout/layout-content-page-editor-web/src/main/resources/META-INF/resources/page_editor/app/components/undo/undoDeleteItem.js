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

import addItem from '../../actions/addItem';
import updatePageContents from '../../actions/updatePageContents';
import {LAYOUT_DATA_ITEM_TYPES} from '../../config/constants/layoutDataItemTypes';
import InfoItemService from '../../services/InfoItemService';
import LayoutService from '../../services/LayoutService';
import getFragmentEntryLinkIdsFromItemId from '../../utils/getFragmentEntryLinkIdsFromItemId';

function undoAction({action, store}) {
	const {itemId, portletIds} = action;

	const fragmentEntryLinkIds = findFragmentEntryLinkIds(
		itemId,
		store.layoutData
	);

	return (dispatch) => {
		return LayoutService.unmarkItemForDeletion({
			fragmentEntryLinkIds,
			itemId,
			onNetworkStatus: dispatch,
			segmentsExperienceId: store.segmentsExperienceId,
		})
			.then(({layoutData}) => {
				const fragmentEntryLinkIds = getFragmentEntryLinkIdsFromItemId({
					itemId,
					layoutData,
				});

				dispatch(
					addItem({
						fragmentEntryLinkIds,
						itemId,
						layoutData,
						portletIds,
					})
				);
			})
			.then(() => {
				InfoItemService.getPageContents({
					onNetworkStatus: dispatch,
					segmentsExperienceId: store.segmentsExperienceId,
				}).then((pageContents) => {
					dispatch(
						updatePageContents({
							pageContents,
						})
					);
				});
			});
	};
}

function findFragmentEntryLinkIds(itemId, layoutData) {
	const item = layoutData.items[itemId];

	const {config = {}, children = []} = item;

	const deletedFragments = [];

	if (
		item.type === LAYOUT_DATA_ITEM_TYPES.fragment &&
		config.fragmentEntryLinkId
	) {
		deletedFragments.push(config.fragmentEntryLinkId);
	}

	children.forEach((itemId) => {
		deletedFragments.push(...findFragmentEntryLinkIds(itemId, layoutData));
	});

	return deletedFragments;
}

function getDerivedStateForUndo({action}) {
	return {
		itemId: action.itemId,
		portletIds: action.portletIds,
	};
}

export {undoAction, getDerivedStateForUndo};
