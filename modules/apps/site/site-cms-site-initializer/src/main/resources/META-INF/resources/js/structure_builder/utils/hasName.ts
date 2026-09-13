/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Group, Structure, StructureChild} from '../types/Structure';

export default function hasName(
	child: Structure | StructureChild
): child is Exclude<Structure | StructureChild, Group> {
	return 'name' in child;
}
