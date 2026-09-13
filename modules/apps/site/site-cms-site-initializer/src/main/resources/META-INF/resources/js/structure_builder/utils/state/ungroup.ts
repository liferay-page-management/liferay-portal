/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Group, Structure} from '../../types/Structure';
import {Uuid} from '../../types/Uuid';
import isGroup from '../isGroup';

export default function ungroup({
	root,
	uuid,
}: {
	root: Group | Structure;
	uuid: Uuid;
}): Group['children'] | Structure['children'] {
	const children = new Map();

	for (const child of root.children.values()) {

		// The group being ungrouped hands its children to its own parent.

		if (child.uuid === uuid && isGroup(child)) {
			for (const grandChild of child.children.values()) {
				const nextGrandChild = {
					...grandChild,
					parent: child.parent,
				};

				children.set(nextGrandChild.uuid, nextGrandChild);
			}
		}
		else if (isGroup(child)) {
			const group: Group = {
				...child,
				children: ungroup({
					root: child,
					uuid,
				}),
			};

			children.set(group.uuid, group);
		}
		else {
			children.set(child.uuid, child);
		}
	}

	return children;
}
