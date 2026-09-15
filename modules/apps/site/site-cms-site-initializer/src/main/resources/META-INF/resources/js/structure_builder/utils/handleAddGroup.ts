/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openToast} from 'frontend-js-components-web';
import {Dispatch} from 'react';

import {config} from '../config';
import {Action, State} from '../contexts/StateContext';
import {Structure} from '../types/Structure';
import {Uuid} from '../types/Uuid';
import findChild from './findChild';
import getAddGroupBlocker from './getAddGroupBlocker';
import getUndeletableChildren from './getUndeletableChildren';
import handleAddRepeatableGroup from './handleAddRepeatableGroup';

// The single entry point behind "Create Group". A new group is repeatable by
// default and only drops the flag when the selection cannot be repeatable. With
// the feature flag off there is nothing to drop to, so the repeatable flow
// answers on its own.

export default async function handleAddGroup({
	dispatch,
	publishedChildren,
	structure,
	uuids,
}: {
	dispatch: Dispatch<Action>;
	publishedChildren: State['publishedChildren'];
	structure: Structure;
	uuids: Uuid[];
}) {
	if (!config.isNonRepeatableGroupsEnabled) {
		return handleAddRepeatableGroup({
			dispatch,
			publishedChildren,
			structure,
			uuids,
		});
	}

	const items = uuids.map((uuid) => findChild({root: structure, uuid})!);

	if (new Set(items.map((item) => item.parent)).size > 1) {
		openToast({
			message: Liferay.Language.get(
				'selected-items-must-be-at-the-same-hierarchy-level'
			),
			type: 'danger',
		});

		return;
	}

	const parent = items[0].parent;

	const blockerMessage = getAddGroupBlocker({items, structure});

	if (blockerMessage) {
		openToast({message: blockerMessage, type: 'danger'});

		return;
	}

	// System fields and referenced structures cannot live in a repeatable group.

	const reasons = [...getUndeletableChildren(uuids, structure).values()];

	if (reasons.length) {
		dispatch({parent, type: 'add-group', uuids});

		return;
	}

	return handleAddRepeatableGroup({
		dispatch,
		publishedChildren,
		structure,
		uuids,
	});
}
