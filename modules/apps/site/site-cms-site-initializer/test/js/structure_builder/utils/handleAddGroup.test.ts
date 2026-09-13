/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openToast} from 'frontend-js-components-web';

import {config} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/config';
import {
	Group,
	Structure,
	StructureChild,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import {Field} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import handleAddGroup from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/handleAddGroup';
import handleAddRepeatableGroup from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/handleAddRepeatableGroup';

jest.mock('frontend-js-components-web', () => ({openToast: jest.fn()}));

jest.mock(
	'../../../../src/main/resources/META-INF/resources/js/structure_builder/config',
	() => ({config: {isNonRepeatableGroupsEnabled: true}})
);

jest.mock(
	'../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/handleAddRepeatableGroup',
	() => ({__esModule: true, default: jest.fn()})
);

const ROOT_UUID = getUuid();

function field(name: string, parent: Uuid = ROOT_UUID): Field {
	return {
		erc: `${name}-erc`,
		indexableConfig: {indexed: false},
		label: {en_US: name},
		localized: false,
		locked: false,
		name,
		parent,
		required: false,
		settings: {},
		type: 'text',
		uuid: getUuid(),
	};
}

function group(parent: Uuid = ROOT_UUID): Group {
	return {
		children: new Map(),
		isRepeatable: false,
		label: {en_US: 'Group'},
		parent,
		type: 'group',
		uuid: getUuid(),
	};
}

function root(...children: StructureChild[]): Structure {
	return {
		children: new Map(children.map((child) => [child.uuid, child])),
		erc: 'root-erc',
		label: {},
		name: 'Root',
		path: '',
		slug: '',
		spaces: 'all',
		status: 'new',
		system: false,
		type: 'L_CMS_CONTENT_STRUCTURES',
		uuid: ROOT_UUID,
		workflows: {},
	};
}

describe('handleAddGroup', () => {
	afterEach(() => {
		(openToast as jest.Mock).mockClear();
		(handleAddRepeatableGroup as jest.Mock).mockClear();
		config.isNonRepeatableGroupsEnabled = true;
	});

	it('creates a repeatable group by default, which is what a new group is', async () => {
		const dispatch = jest.fn();
		const sku = field('sku');

		await handleAddGroup({
			dispatch,
			publishedChildren: new Set<Uuid>(),
			structure: root(sku),
			uuids: [sku.uuid],
		});

		expect(handleAddRepeatableGroup).toHaveBeenCalled();
		expect(dispatch).not.toHaveBeenCalled();
		expect(openToast).not.toHaveBeenCalled();
	});

	it('falls back to a layout group when the selection cannot be repeatable', async () => {
		const dispatch = jest.fn();
		const locked = {...field('title'), locked: true};

		await handleAddGroup({
			dispatch,
			publishedChildren: new Set<Uuid>(),
			structure: root(locked),
			uuids: [locked.uuid],
		});

		expect(handleAddRepeatableGroup).not.toHaveBeenCalled();
		expect(dispatch).toHaveBeenCalledWith({
			parent: ROOT_UUID,
			type: 'add-group',
			uuids: [locked.uuid],
		});
	});

	it('leaves the repeatable flow to answer on its own while the feature flag is off, since there are no layout groups to fall back to', async () => {
		config.isNonRepeatableGroupsEnabled = false;

		const dispatch = jest.fn();
		const locked = {...field('title'), locked: true};

		await handleAddGroup({
			dispatch,
			publishedChildren: new Set<Uuid>(),
			structure: root(locked),
			uuids: [locked.uuid],
		});

		expect(handleAddRepeatableGroup).toHaveBeenCalled();
		expect(dispatch).not.toHaveBeenCalled();
	});

	it('toasts and does not dispatch when the selection spans more than one parent', async () => {
		const dispatch = jest.fn();

		const parentGroup = group();
		const sku = field('sku');
		const nested = field('nested', parentGroup.uuid);

		parentGroup.children.set(nested.uuid, nested);

		await handleAddGroup({
			dispatch,
			publishedChildren: new Set<Uuid>(),
			structure: root(parentGroup, sku),
			uuids: [sku.uuid, nested.uuid],
		});

		expect(openToast).toHaveBeenCalledWith({
			message: 'selected-items-must-be-at-the-same-hierarchy-level',
			type: 'danger',
		});
		expect(dispatch).not.toHaveBeenCalled();
	});

	it('toasts and does not dispatch when the group would sit deeper than the object layout allows', async () => {
		const dispatch = jest.fn();

		const rootGroup = group();
		const nestedGroup = group(rootGroup.uuid);
		const sku = field('sku', nestedGroup.uuid);

		nestedGroup.children.set(sku.uuid, sku);
		rootGroup.children.set(nestedGroup.uuid, nestedGroup);

		await handleAddGroup({
			dispatch,
			publishedChildren: new Set<Uuid>(),
			structure: root(rootGroup),
			uuids: [sku.uuid],
		});

		expect(openToast).toHaveBeenCalledWith({
			message: 'a-group-cannot-be-created-inside-a-nested-group',
			type: 'danger',
		});
		expect(dispatch).not.toHaveBeenCalled();
		expect(handleAddRepeatableGroup).not.toHaveBeenCalled();
	});

	it('toasts and does not dispatch for a selection inside a repeatable group, which the keyboard shortcut reaches without the item actions', async () => {
		const dispatch = jest.fn();

		const repeatable = {...group(), erc: 'group-erc', isRepeatable: true};
		const sku = field('sku', repeatable.uuid);

		repeatable.children.set(sku.uuid, sku);

		await handleAddGroup({
			dispatch,
			publishedChildren: new Set<Uuid>(),
			structure: root(repeatable as Group),
			uuids: [sku.uuid],
		});

		expect(openToast).toHaveBeenCalledWith({
			message: 'a-group-cannot-be-created-inside-a-repeatable-group',
			type: 'danger',
		});
		expect(dispatch).not.toHaveBeenCalled();
		expect(handleAddRepeatableGroup).not.toHaveBeenCalled();
	});

	it('toasts when wrapping a root group that already holds a nested group, which would exceed the cap', async () => {
		const dispatch = jest.fn();

		const rootGroup = group();
		const nestedGroup = group(rootGroup.uuid);

		rootGroup.children.set(nestedGroup.uuid, nestedGroup);

		await handleAddGroup({
			dispatch,
			publishedChildren: new Set<Uuid>(),
			structure: root(rootGroup),
			uuids: [rootGroup.uuid],
		});

		expect(openToast).toHaveBeenCalledWith({
			message: 'a-group-cannot-be-created-inside-a-nested-group',
			type: 'danger',
		});
		expect(dispatch).not.toHaveBeenCalled();
	});
});
