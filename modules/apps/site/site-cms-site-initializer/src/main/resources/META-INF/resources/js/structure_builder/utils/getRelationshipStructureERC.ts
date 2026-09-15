/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Structure} from '../types/Structure';
import {Uuid} from '../types/Uuid';
import findChild from './findChild';
import {isRepeatableGroup} from './isGroup';

/**
 * Returns the external reference code of the object definition that owns the
 * relationships declared by the given child, which is the nearest enclosing
 * repeatable group, or the structure itself when there is none. Non-repeatable
 * groups have no object definition, so they never own a relationship.
 */
export default function getRelationshipStructureERC({
	structure,
	uuid,
}: {
	structure: Structure;
	uuid: Uuid;
}): string {
	let currentUuid = uuid;

	while (currentUuid && currentUuid !== structure.uuid) {
		const child = findChild({root: structure, uuid: currentUuid});

		if (!child) {
			break;
		}

		if (isRepeatableGroup(child)) {
			return child.erc;
		}

		currentUuid = child.parent;
	}

	return structure.erc;
}
