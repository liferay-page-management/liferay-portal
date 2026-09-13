/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Structure} from '../types/Structure';
import {Uuid} from '../types/Uuid';
import findChild from './findChild';
import {isRepeatableGroup} from './isGroup';

export default function isInsideRepeatableGroup({
	structure,
	uuid,
}: {
	structure: Structure;
	uuid: Uuid;
}): boolean {
	let currentUuid = uuid;

	while (currentUuid && currentUuid !== structure.uuid) {
		const child = findChild({root: structure, uuid: currentUuid});

		if (!child) {
			break;
		}

		if (isRepeatableGroup(child)) {
			return true;
		}

		currentUuid = child.parent;
	}

	return false;
}
