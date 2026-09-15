/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {fireEvent, render} from '@testing-library/react';
import React from 'react';

import StructureTree, {
	flatItemIds,
	getItemActions,
	getRangeItems,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/components/StructureTree';
import {config} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/config';
import {useStateDispatch} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/contexts/StateContext';
import {
	Group,
	Structure,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import {Field} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import {MockCacheProvider} from '../mocks/MockCacheProvider';
import {DEFAULT_STRUCTURE, MockStateProvider} from '../mocks/MockStateProvider';

jest.mock(
	'../../../../src/main/resources/META-INF/resources/js/structure_builder/config',
	() => ({config: {isNonRepeatableGroupsEnabled: true}})
);

jest.mock(
	'../../../../src/main/resources/META-INF/resources/js/structure_builder/contexts/StateContext',
	() => {
		const actual = jest.requireActual(
			'../../../../src/main/resources/META-INF/resources/js/structure_builder/contexts/StateContext'
		);

		return {
			...actual,
			useStateDispatch: jest.fn(),
		};
	}
);

const uuid = (id: string) => id as Uuid;

const ROOT = uuid('root');
const A = uuid('a');
const B = uuid('b');

const items = [
	{
		children: [
			{id: uuid('a')},
			{
				children: [{id: uuid('b1')}, {id: uuid('b2')}],
				id: uuid('b'),
			},
			{id: uuid('c')},
		],
		id: ROOT,
	},
];

const mockDispatch = jest.fn();

const renderTree = (selection: Uuid[] = []) =>
	render(
		<MockCacheProvider objectDefinitions={{}} spaces={[]}>
			<MockStateProvider state={{selection}}>
				<StructureTree search="" />
			</MockStateProvider>
		</MockCacheProvider>
	);

const treeItem = (id: Uuid) => {
	const element = document.createElement('div');

	element.setAttribute('data-id', `string,${id}`);
	element.setAttribute('tabindex', '0');

	document.body.appendChild(element);

	return element;
};

beforeEach(() => {
	(globalThis as any).Liferay = {
		...(globalThis as any).Liferay,
		Browser: {isMac: () => false},
	};

	jest.clearAllMocks();

	(useStateDispatch as jest.Mock).mockReturnValue(mockDispatch);
});

it('flatItemIds flattens ids in depth-first order including nested children', () => {
	expect(flatItemIds(items)).toEqual([
		ROOT,
		uuid('a'),
		uuid('b'),
		uuid('b1'),
		uuid('b2'),
		uuid('c'),
	]);
});

it('getRangeItems returns items between the anchor and the target going downwards', () => {
	expect(
		getRangeItems({
			items,
			rootId: ROOT,
			selection: [uuid('a')],
			targetId: uuid('b2'),
		})
	).toEqual([uuid('a'), uuid('b'), uuid('b1'), uuid('b2')]);
});

it('getRangeItems returns items between the anchor and the target when the target is above the anchor', () => {
	expect(
		getRangeItems({
			items,
			rootId: ROOT,
			selection: [uuid('b2')],
			targetId: uuid('a'),
		})
	).toEqual([uuid('a'), uuid('b'), uuid('b1'), uuid('b2')]);
});

it('getRangeItems uses the last selected item as the anchor', () => {
	expect(
		getRangeItems({
			items,
			rootId: ROOT,
			selection: [uuid('c'), uuid('a')],
			targetId: uuid('b'),
		})
	).toEqual([uuid('a'), uuid('b')]);
});

it('getRangeItems returns only the target when anchor and target are the same', () => {
	expect(
		getRangeItems({
			items,
			rootId: ROOT,
			selection: [uuid('b1')],
			targetId: uuid('b1'),
		})
	).toEqual([uuid('b1')]);
});

it('extends selection with Shift+ArrowDown when destination is not selected', () => {
	renderTree([A]);

	const fromEl = treeItem(A);
	const toEl = treeItem(B);

	toEl.focus();

	fireEvent.keyDown(fromEl, {key: 'ArrowDown', shiftKey: true});

	expect(mockDispatch).toHaveBeenCalledWith({
		selection: [A, B],
		type: 'set-selection',
	});
});

it('shrinks selection with Shift+ArrowUp when destination is already selected', () => {
	renderTree([A, B]);

	const fromEl = treeItem(B);
	const toEl = treeItem(A);

	toEl.focus();

	fireEvent.keyDown(fromEl, {key: 'ArrowUp', shiftKey: true});

	expect(mockDispatch).toHaveBeenCalledWith({
		selection: [A],
		type: 'set-selection',
	});
});

it('does not change selection when destination is the structure root', () => {
	renderTree([A]);

	const fromEl = treeItem(A);
	const toEl = treeItem(DEFAULT_STRUCTURE.uuid);

	toEl.focus();

	fireEvent.keyDown(fromEl, {key: 'ArrowUp', shiftKey: true});

	expect(mockDispatch).not.toHaveBeenCalled();
});

it('clears selection on Escape', () => {
	renderTree([A, B]);

	fireEvent.keyDown(window, {key: 'Escape'});

	expect(mockDispatch).toHaveBeenCalledWith({
		selection: [],
		type: 'set-selection',
	});
});

describe('getItemActions group actions', () => {
	const buildField = (parent: Uuid): Field =>
		({
			label: {en_US: `Field-${parent}`},
			locked: false,
			name: `field-${parent}`,
			parent,
			type: 'text',
			uuid: getUuid(),
		}) as unknown as Field;

	const labelsOf = (structure: Structure, item: Field | Group) =>
		getItemActions({
			clipboard: null,
			dispatch: jest.fn(),
			item,
			publishedChildren: new Map() as never,
			structure,
		})
			.map((action) => ('label' in action ? action.label : undefined))
			.filter(Boolean);

	it('offers one group action for a field at the root', () => {
		const field = buildField(ROOT);

		const structure = {
			children: new Map([[field.uuid, field]]),
			type: 'L_CMS_CONTENT_STRUCTURES',
			uuid: ROOT,
		} as unknown as Structure;

		const labels = labelsOf(structure, field);

		expect(labels).toContain('create-group');
		expect(labels).not.toContain('create-repeatable-group');
	});

	it('offers the same single group action for a field inside a group', () => {
		const nestedField = buildField(A);

		const container: Group = {
			children: new Map([[nestedField.uuid, nestedField]]),
			isRepeatable: false,
			label: {en_US: 'Group'},
			parent: ROOT,
			type: 'group',
			uuid: A,
		};

		const structure = {
			children: new Map([[A, container]]),
			type: 'L_CMS_CONTENT_STRUCTURES',
			uuid: ROOT,
		} as unknown as Structure;

		const labels = labelsOf(structure, nestedField);

		expect(labels).toContain('create-group');
		expect(labels).not.toContain('create-repeatable-group');
	});

	it('offers ungroup for a group that is not repeatable, not only a repeatable one', () => {
		const nestedField = buildField(A);

		const container: Group = {
			children: new Map([[nestedField.uuid, nestedField]]),
			isRepeatable: false,
			label: {en_US: 'Group'},
			parent: ROOT,
			type: 'group',
			uuid: A,
		};

		const structure = {
			children: new Map([[A, container]]),
			type: 'L_CMS_CONTENT_STRUCTURES',
			uuid: ROOT,
		} as unknown as Structure;

		expect(labelsOf(structure, container)).toContain('ungroup');
	});

	it('offers no group action for a locked field while the feature flag is off, since it cannot start a repeatable group', () => {
		config.isNonRepeatableGroupsEnabled = false;

		const locked = {...buildField(A), locked: true};

		const structure = {
			children: new Map([[locked.uuid, locked]]),
			type: 'L_CMS_CONTENT_STRUCTURES',
			uuid: ROOT,
		} as unknown as Structure;

		expect(labelsOf(structure, locked)).not.toContain(
			'create-repeatable-group'
		);

		config.isNonRepeatableGroupsEnabled = true;
	});
});
