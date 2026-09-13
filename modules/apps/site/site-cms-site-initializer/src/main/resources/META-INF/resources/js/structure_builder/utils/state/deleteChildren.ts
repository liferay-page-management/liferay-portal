/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Group, Structure, StructureChild} from '../../types/Structure';
import {Uuid} from '../../types/Uuid';
import isGroup, {isRepeatableGroup} from '../isGroup';
import isLocked from '../isLocked';

export default function deleteChildren({
	root,
	uuids,
}: {
	root: Structure | Group;
	uuids: Uuid[];
}): {
	deletedChildrenUuids: Set<Uuid>;
	updatedChildren: Structure['children'] | Group['children'];
} {
	const deletedChildrenUuids = new Set<Uuid>();
	const children = new Map(root.children);

	// Iterate over existing children

	for (const child of root.children.values()) {

		// Delete child if it applies

		if (uuids.includes(child.uuid) && !isLocked({root, uuid: child.uuid})) {
			children.delete(child.uuid);

			getDeletedChildrenUuids({child}).forEach((uuid) => {
				deletedChildrenUuids.add(uuid);
			});
		}

		// If it's a container, do recursive call with its children

		else if (isGroup(child)) {
			const {
				deletedChildrenUuids: containerDeletedChildrenUuids,
				updatedChildren: containerChildren,
			} = deleteChildren({
				root: child,
				uuids,
			});

			containerDeletedChildrenUuids.forEach((uuid) => {
				deletedChildrenUuids.add(uuid);
			});

			// Delete an emptied repeatable group.

			if (isRepeatableGroup(child) && !containerChildren.size) {
				deletedChildrenUuids.add(child.uuid);
				children.delete(child.uuid);
			}

			// Otherwise update the container with updated children

			else {
				const container = {
					...child,
					children: containerChildren,
				};

				children.set(container.uuid, container);
			}
		}
	}

	return {
		deletedChildrenUuids,
		updatedChildren: children,
	};
}

function getDeletedChildrenUuids({child}: {child: StructureChild}): Set<Uuid> {
	const deletedChildrenUuids = new Set<Uuid>();

	deletedChildrenUuids.add(child.uuid);

	if (isGroup(child)) {
		for (const containerChild of child.children.values()) {
			const containerDeletedChildrenUuids = getDeletedChildrenUuids({
				child: containerChild,
			});

			containerDeletedChildrenUuids.forEach((uuid) => {
				deletedChildrenUuids.add(uuid);
			});
		}
	}

	return deletedChildrenUuids;
}
