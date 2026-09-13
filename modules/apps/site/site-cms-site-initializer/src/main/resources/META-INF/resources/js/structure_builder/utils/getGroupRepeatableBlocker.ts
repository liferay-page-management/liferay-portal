/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {State} from '../contexts/StateContext';
import {Group, Structure} from '../types/Structure';
import getGroupDepth, {MAXIMUM_GROUP_DEPTH} from './getGroupDepth';
import isGroup from './isGroup';
import isLocked from './isLocked';

export default function getGroupRepeatableBlocker({
	group,
	isRepeatable,
	publishedChildren,
	structure,
}: {
	group: Group;
	isRepeatable: boolean;
	publishedChildren: State['publishedChildren'];
	structure: Structure;
}): string | null {
	if (group.isRepeatable === isRepeatable) {
		return null;
	}

	if (!isRepeatable) {

		// A published repeatable group keeps the object definition its
		// children live on.

		if (publishedChildren.has(group.uuid)) {
			return Liferay.Language.get(
				'the-ungroup-action-cannot-be-done-because-this-repeatable-group-is-already-published'
			);
		}

		return null;
	}

	const children = Array.from(group.children.values());

	if (children.some(isGroup)) {
		return Liferay.Language.get(
			'a-group-that-contains-another-group-cannot-be-repeatable'
		);
	}

	// A repeatable group is a box in the object layout, and a box cannot nest
	// in another box.

	if (
		getGroupDepth({structure, uuid: group.uuid}) >
		MAXIMUM_GROUP_DEPTH - 1
	) {
		return Liferay.Language.get('a-nested-group-cannot-be-repeatable');
	}

	if (children.some((child) => child.type === 'referenced-structure')) {
		return Liferay.Language.get(
			'a-group-that-contains-a-referenced-structure-cannot-be-repeatable'
		);
	}

	if (
		children.some((child) => isLocked({root: structure, uuid: child.uuid}))
	) {
		return Liferay.Language.get(
			'a-group-that-contains-system-fields-cannot-be-repeatable'
		);
	}

	return null;
}
