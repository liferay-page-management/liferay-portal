/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openConfirmModal} from '@liferay/layout-js-components-web';
import {openToast} from 'frontend-js-components-web';
import {Dispatch} from 'react';

import {Action, State} from '../contexts/StateContext';
import {Group, Structure} from '../types/Structure';
import {getChildrenUuids} from './getChildrenUuids';
import getGroupRepeatableBlocker from './getGroupRepeatableBlocker';

export default async function handleSetGroupRepeatable({
	dispatch,
	group,
	isRepeatable,
	publishedChildren,
	structure,
}: {
	dispatch: Dispatch<Action>;
	group: Group;
	isRepeatable: boolean;
	publishedChildren: State['publishedChildren'];
	structure: Structure;
}) {
	if (group.isRepeatable === isRepeatable) {
		return;
	}

	const blockerMessage = getGroupRepeatableBlocker({
		group,
		isRepeatable,
		publishedChildren,
		structure,
	});

	if (blockerMessage) {
		openToast({
			message: blockerMessage,
			type: 'danger',
		});

		return;
	}

	// Moving published fields into a new object definition drops their data.

	if (isRepeatable) {
		const uuids = getChildrenUuids({root: group});

		if (Array.from(uuids).some((uuid) => publishedChildren.has(uuid))) {
			const confirmed = await openConfirmModal({
				buttonLabel: Liferay.Language.get('create-repeatable-group'),
				center: true,
				optOutConfig: {
					sessionKey: 'disableRepeatableGroupCreationModal',
				},
				status: 'warning',
				text: Liferay.Language.get(
					'creating-a-repeatable-group-with-published-fields-will-permanently-delete-existing-field-data-after-publishing-the-structure'
				),
				title: Liferay.Language.get('create-repeatable-group'),
			});

			if (!confirmed) {
				return;
			}
		}
	}

	dispatch({isRepeatable, type: 'set-group-repeatable', uuid: group.uuid});
}
