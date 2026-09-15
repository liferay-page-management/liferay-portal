/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	Group,
	RepeatableGroup,
	Structure,
	StructureChild,
} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import {getDefaultField} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getUuid from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import deleteChildren from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/state/deleteChildren';

function createGroup({
	children = [],
	parent,
}: {
	children?: StructureChild[];
	parent: Uuid;
}): Group {
	const uuid = getUuid();

	const container: Group = {
		children: new Map(),
		isRepeatable: false,
		label: {en_US: 'Grouping Group'},
		parent,
		type: 'group',
		uuid,
	};

	children.forEach((child) => {
		child.parent = uuid;

		container.children.set(child.uuid, child);
	});

	return container;
}

function createRepeatableGroup({
	children = [],
	parent,
}: {
	children?: StructureChild[];
	parent: Uuid;
}): RepeatableGroup {
	const uuid = getUuid();

	const group: RepeatableGroup = {
		children: new Map(),
		erc: 'group-erc',
		isRepeatable: true,
		label: {en_US: 'Repeatable Group'},
		name: 'RepeatableGroup',
		parent,
		relationshipERC: 'relationship-erc',
		relationshipName: 'relationshipName',
		type: 'group',
		uuid,
	};

	children.forEach((child) => {
		child.parent = uuid;

		group.children.set(child.uuid, child);
	});

	return group;
}

function createRoot(children: StructureChild[]): Structure {
	const uuid = getUuid();

	const root: Structure = {
		children: new Map(),
		erc: 'root-erc',
		label: {},
		name: 'Root',
		path: '',
		slug: '',
		spaces: 'all',
		status: 'new',
		system: false,
		type: 'L_CMS_CONTENT_STRUCTURES',
		uuid,
		workflows: {},
	};

	children.forEach((child) => {
		child.parent = uuid;

		root.children.set(child.uuid, child);
	});

	return root;
}

describe('deleteChildren', () => {
	it('keeps an emptied group and only removes its child', () => {
		const field = getDefaultField({parent: getUuid(), type: 'text'});

		const container = createGroup({
			children: [field],
			parent: getUuid(),
		});

		const root = createRoot([container]);

		const {deletedChildrenUuids, updatedChildren} = deleteChildren({
			root,
			uuids: [field.uuid],
		});

		const updatedContainer = updatedChildren.get(container.uuid) as Group;

		expect(updatedContainer).toBeDefined();
		expect(updatedContainer.type).toBe('group');
		expect(updatedContainer.children.size).toBe(0);
		expect(deletedChildrenUuids.has(field.uuid)).toBe(true);
		expect(deletedChildrenUuids.has(container.uuid)).toBe(false);
	});

	it('removes an emptied repeatable-group along with its child', () => {
		const field = getDefaultField({parent: getUuid(), type: 'text'});

		const group = createRepeatableGroup({
			children: [field],
			parent: getUuid(),
		});

		const root = createRoot([group]);

		const {deletedChildrenUuids, updatedChildren} = deleteChildren({
			root,
			uuids: [field.uuid],
		});

		expect(updatedChildren.has(group.uuid)).toBe(false);
		expect(deletedChildrenUuids.has(field.uuid)).toBe(true);
		expect(deletedChildrenUuids.has(group.uuid)).toBe(true);
	});

	it('removes a group and all of its descendants when deleted by its own uuid', () => {
		const field = getDefaultField({parent: getUuid(), type: 'text'});

		const nestedField = getDefaultField({
			parent: getUuid(),
			type: 'integer',
		});

		const nestedContainer = createGroup({
			children: [nestedField],
			parent: getUuid(),
		});

		const container = createGroup({
			children: [field, nestedContainer],
			parent: getUuid(),
		});

		const root = createRoot([container]);

		const {deletedChildrenUuids, updatedChildren} = deleteChildren({
			root,
			uuids: [container.uuid],
		});

		expect(updatedChildren.has(container.uuid)).toBe(false);
		expect(deletedChildrenUuids.has(container.uuid)).toBe(true);
		expect(deletedChildrenUuids.has(field.uuid)).toBe(true);
		expect(deletedChildrenUuids.has(nestedContainer.uuid)).toBe(true);
		expect(deletedChildrenUuids.has(nestedField.uuid)).toBe(true);
	});
});
