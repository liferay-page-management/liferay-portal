/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	Group,
	StructureChild,
} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import {
	Field,
	getDefaultField,
} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getUuid from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import cloneChild from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/state/cloneChild';

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
		label: {en_US: 'Group'},
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

describe('cloneChild', () => {
	it('deep-clones a group with new uuids while preserving label and type', () => {
		const PARENT_UUID = getUuid();

		const nestedField = getDefaultField({parent: getUuid(), type: 'text'});

		const nestedContainer = createGroup({
			children: [nestedField],
			parent: getUuid(),
		});

		const field = getDefaultField({parent: getUuid(), type: 'integer'});

		const container = createGroup({
			children: [field, nestedContainer],
			parent: getUuid(),
		});

		const clone = cloneChild({
			child: container,
			deletedChildren: [],
			parent: PARENT_UUID,
			siblings: new Map(),
		}) as Group;

		expect(clone.type).toBe('group');
		expect(clone.uuid).not.toBe(container.uuid);
		expect(clone.parent).toBe(PARENT_UUID);
		expect(clone.label).toEqual(container.label);
		expect(clone.children.size).toBe(2);

		const clonedChildren = [...clone.children.values()];

		clonedChildren.forEach((clonedChild) => {
			expect(clonedChild.parent).toBe(clone.uuid);
			expect(clone.children.has(clonedChild.uuid)).toBe(true);
		});

		const clonedField = clonedChildren.find(
			(clonedChild) => clonedChild.type === 'integer'
		) as Field;

		expect(clonedField).toBeDefined();
		expect(clonedField.uuid).not.toBe(field.uuid);

		const clonedNestedContainer = clonedChildren.find(
			(clonedChild) => clonedChild.type === 'group'
		) as Group;

		expect(clonedNestedContainer).toBeDefined();
		expect(clonedNestedContainer.uuid).not.toBe(nestedContainer.uuid);
		expect(clonedNestedContainer.children.size).toBe(1);

		const clonedNestedField = [
			...clonedNestedContainer.children.values(),
		][0] as Field;

		expect(clonedNestedField.uuid).not.toBe(nestedField.uuid);
		expect(clonedNestedField.parent).toBe(clonedNestedContainer.uuid);
	});
});
