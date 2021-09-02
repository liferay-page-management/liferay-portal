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

import restoreCollectionDisplay from '../../actions/restoreCollectionDisplay';
import LayoutService from '../../services/LayoutService';

const undoAction = ({action, store}) => (dispatch) =>
	LayoutService.unmarkCollectionForDeletion({
		itemId: action.itemId,
		onNetworkStatus: dispatch,
		segmentsExperienceId: store.segmentsExperienceId,
	}).then(() =>
		dispatch(
			restoreCollectionDisplay({
				deletedFragmentEntryLinksIds:
					action.deletedFragmentEntryLinksIds,
				layoutData: action.layoutData,
				pageContents: action.pageContents,
				updatedFragmentEntryLinks: action.updatedFragmentEntryLinks,
			})
		)
	);

const getDerivedStateForUndo = ({action, state}) => ({
	deletedFragmentEntryLinksIds: action.deletedFragmentEntryLinksIds,
	itemId: action.itemId,
	layoutData: state.layoutData,
	pageContents: state.pageContents,
	segmentsExperienceId: state.segmentsExperienceId,
	updatedFragmentEntryLinks: action.updatedFragmentEntryLinks.map(
		({fragmentEntryLinkId}) => {
			const fragmentEntryLink =
				state.fragmentEntryLinks[fragmentEntryLinkId];

			return {
				content: fragmentEntryLink.content,
				editableValues: fragmentEntryLink.editableValues,
				fragmentEntryLinkId,
			};
		}
	),
});

export {undoAction, getDerivedStateForUndo};
