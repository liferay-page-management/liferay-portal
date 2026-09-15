/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Dispatch} from 'react';

import {config} from '../config';
import {Action, State} from '../contexts/StateContext';
import {Structure, StructureChild} from '../types/Structure';
import getAddGroupBlocker from './getAddGroupBlocker';
import handleAddGroup from './handleAddGroup';
import isField from './isField';
import isGroup from './isGroup';

export type GroupItemAction = {
	label: string;
	onClick: () => void;
	symbolLeft?: string;
};

export default function getGroupItemActions({
	dispatch,
	items,
	publishedChildren,
	structure,
}: {
	dispatch: Dispatch<Action>;
	items: StructureChild[];
	publishedChildren: State['publishedChildren'];
	structure: Structure;
}): GroupItemAction[] {
	if (!items.length) {
		return [];
	}

	// Without the feature flag only fields can be grouped, and the repeatable
	// flow reports anything it cannot accept, so the action stays offered.

	const groupable = config.isNonRepeatableGroupsEnabled
		? items.every((item) => isField(item) || isGroup(item)) &&
			!getAddGroupBlocker({items, structure})
		: items.every(isField);

	if (!groupable) {
		return [];
	}

	return [
		{
			label: config.isNonRepeatableGroupsEnabled
				? Liferay.Language.get('create-group')
				: Liferay.Language.get('create-repeatable-group'),
			onClick: () =>
				handleAddGroup({
					dispatch,
					publishedChildren,
					structure,
					uuids: items.map((item) => item.uuid),
				}),
			symbolLeft: 'fieldset',
		},
	];
}
