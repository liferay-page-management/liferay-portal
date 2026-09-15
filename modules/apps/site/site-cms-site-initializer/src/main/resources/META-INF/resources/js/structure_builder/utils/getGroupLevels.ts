/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {StructureChild} from '../types/Structure';
import isGroup from './isGroup';

export default function getGroupLevels(item: StructureChild): number {
	if (!isGroup(item)) {
		return 0;
	}

	return (
		1 +
		Math.max(0, ...Array.from(item.children.values()).map(getGroupLevels))
	);
}
