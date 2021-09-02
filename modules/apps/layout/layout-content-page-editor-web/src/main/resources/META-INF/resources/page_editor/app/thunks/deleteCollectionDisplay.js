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

import deleteCollectionDisplayAction from '../actions/deleteCollectionDisplay';
import LayoutService from '../services/LayoutService';
import getFragmentEntryLinkIdsFromItemId from '../utils/getFragmentEntryLinkIdsFromItemId';
import getNonInstantiablePortletIds from '../utils/getNonInstantiablePortletIds';

export default function deleteCollectionDisplay({
	itemId,
	selectItem = () => {},
}) {
	return (dispatch, getState) => {
		const {
			fragmentEntryLinks,
			languageId,
			layoutData,
			segmentsExperienceId,
		} = getState();

		const deletedPortletIds = getNonInstantiablePortletIds(
			itemId,
			layoutData,
			fragmentEntryLinks
		);

		return LayoutService.markCollectionDisplayForDeletion({
			itemId,
			languageId,
			onNetworkStatus: dispatch,
			portletIds: deletedPortletIds,
			segmentsExperienceId,
		}).then(
			({
				fragmentEntryLinks: updatedFragmentEntryLinks,
				layoutData: updatedLayoutData,
				pageContents,
			}) => {
				selectItem(null);

				const deletedFragmentEntryLinksIds = getFragmentEntryLinkIdsFromItemId(
					{itemId, layoutData}
				);

				dispatch(
					deleteCollectionDisplayAction({
						deletedFragmentEntryLinksIds,
						deletedPortletIds,
						layoutData: updatedLayoutData,
						pageContents,
						updatedFragmentEntryLinks: updatedFragmentEntryLinks.filter(
							({fragmentEntryLinkId}) =>
								!deletedFragmentEntryLinksIds.includes(
									fragmentEntryLinkId
								)
						),
					})
				);
			}
		);
	};
}
