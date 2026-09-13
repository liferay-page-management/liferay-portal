/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Structure, StructureChild} from '../types/Structure';
import getGroupDepth, {MAXIMUM_GROUP_DEPTH} from './getGroupDepth';
import getGroupLevels from './getGroupLevels';
import isInsideRepeatableGroup from './isInsideRepeatableGroup';

export default function getAddGroupBlocker({
	items,
	structure,
}: {
	items: StructureChild[];
	structure: Structure;
}): string | null {
	if (!items.length) {
		return null;
	}

	if (
		items.some((item) =>
			isInsideRepeatableGroup({structure, uuid: item.parent})
		)
	) {
		return Liferay.Language.get(
			'a-group-cannot-be-created-inside-a-repeatable-group'
		);
	}

	const depth = Math.max(
		...items.map(
			(item) =>
				getGroupDepth({structure, uuid: item.parent}) +
				1 +
				getGroupLevels(item)
		)
	);

	if (depth > MAXIMUM_GROUP_DEPTH) {
		return Liferay.Language.get(
			'a-group-cannot-be-created-inside-a-nested-group'
		);
	}

	return null;
}
