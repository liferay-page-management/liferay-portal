/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Group, Structure, StructureChild} from '../../types/Structure';
import isGroup from '../isGroup';
import sortChildren from './sortChildren';

export default function addChild({
	child,
	root,
}: {
	child: StructureChild;
	root: Structure | Group;
}): Structure['children'] | Group['children'] {
	const children = new Map();

	// Iterate over children

	for (const rootChild of root.children.values()) {

		// Insert the child. If it's a container, build it with recursive call

		if (isGroup(rootChild)) {
			const container = {
				...rootChild,
				children: addChild({
					child,
					root: rootChild,
				}),
			};

			children.set(container.uuid, container);
		}
		else {
			children.set(rootChild.uuid, rootChild);
		}
	}

	// Insert child if this is the correct parent

	if (root.uuid === child.parent) {
		children.set(child.uuid, child);
	}

	return sortChildren(children);
}
