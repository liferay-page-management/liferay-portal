/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {config} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/config';
import {
	Group,
	RepeatableGroup,
	Structure,
	StructureChild,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import {Field} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getGroupItemActions from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getGroupItemActions';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';

jest.mock('frontend-js-components-web', () => ({openToast: jest.fn()}));

jest.mock(
	'../../../../src/main/resources/META-INF/resources/js/structure_builder/config',
	() => ({config: {isNonRepeatableGroupsEnabled: true}})
);

const ROOT_UUID = getUuid();

function field(name: string): Field {
	return {
		erc: `${name}-erc`,
		indexableConfig: {indexed: false},
		label: {en_US: name},
		localized: false,
		locked: false,
		name,
		parent: ROOT_UUID,
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

function repeatableGroup(): RepeatableGroup {
	return {
		children: new Map(),
		erc: 'group-erc',
		isRepeatable: true,
		label: {en_US: 'Group'},
		name: 'group',
		parent: ROOT_UUID,
		relationshipERC: '',
		relationshipName: '',
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

describe('getGroupItemActions', () => {
	afterEach(() => {
		config.isNonRepeatableGroupsEnabled = true;
	});

	it('offers no create-group action inside a repeatable group, which has nowhere to render one', () => {
		const repeatable = repeatableGroup();

		const sku = {...field('sku'), parent: repeatable.uuid};

		const structure = root({
			...repeatable,
			children: new Map([[sku.uuid, sku]]),
		});

		expect(
			getGroupItemActions({
				dispatch: jest.fn(),
				items: [sku],
				publishedChildren: new Set<Uuid>(),
				structure,
			})
		).toEqual([]);
	});

	it('offers a single create-group action for a field', () => {
		const sku = field('sku');

		const actions = getGroupItemActions({
			dispatch: jest.fn(),
			items: [sku],
			publishedChildren: new Set<Uuid>(),
			structure: root(sku),
		});

		expect(actions).toHaveLength(1);
		expect(actions[0].label).toBe('create-group');
		expect(actions[0].symbolLeft).toBe('fieldset');
	});

	it('offers create-group for a group at the root, to push it down a level', () => {
		const rootGroup = group();

		const actions = getGroupItemActions({
			dispatch: jest.fn(),
			items: [rootGroup],
			publishedChildren: new Set<Uuid>(),
			structure: root(rootGroup),
		});

		expect(actions).toHaveLength(1);
		expect(actions[0].label).toBe('create-group');
	});

	it('offers create-group when the selection mixes a field and a root group', () => {
		const sku = field('sku');
		const rootGroup = group();

		const actions = getGroupItemActions({
			dispatch: jest.fn(),
			items: [sku, rootGroup],
			publishedChildren: new Set<Uuid>(),
			structure: root(sku, rootGroup),
		});

		expect(actions).toHaveLength(1);
	});

	it('offers no action for a group that is already nested', () => {
		const rootGroup = group();
		const nestedGroup = group(rootGroup.uuid);

		rootGroup.children.set(nestedGroup.uuid, nestedGroup);

		const actions = getGroupItemActions({
			dispatch: jest.fn(),
			items: [nestedGroup],
			publishedChildren: new Set<Uuid>(),
			structure: root(rootGroup),
		});

		expect(actions).toHaveLength(0);
	});

	it('offers no action for a field inside a nested group, since the new group would sit deeper than the object layout allows', () => {
		const rootGroup = group();
		const nestedGroup = group(rootGroup.uuid);
		const weight = {...field('weight'), parent: nestedGroup.uuid};

		nestedGroup.children.set(weight.uuid, weight);
		rootGroup.children.set(nestedGroup.uuid, nestedGroup);

		expect(
			getGroupItemActions({
				dispatch: jest.fn(),
				items: [weight],
				publishedChildren: new Set<Uuid>(),
				structure: root(rootGroup),
			})
		).toHaveLength(0);
	});

	it('offers create-group for a field inside a root group, which still fits', () => {
		const rootGroup = group();
		const weight = {...field('weight'), parent: rootGroup.uuid};

		rootGroup.children.set(weight.uuid, weight);

		const actions = getGroupItemActions({
			dispatch: jest.fn(),
			items: [weight],
			publishedChildren: new Set<Uuid>(),
			structure: root(rootGroup),
		});

		expect(actions).toHaveLength(1);
		expect(actions[0].label).toBe('create-group');
	});

	it('wraps a root repeatable group in a group, which the object layout renders as a repeatable box inside a tab', () => {
		const variants = repeatableGroup();

		const actions = getGroupItemActions({
			dispatch: jest.fn(),
			items: [variants],
			publishedChildren: new Set<Uuid>(),
			structure: root(variants),
		});

		expect(actions).toHaveLength(1);
		expect(actions[0].label).toBe('create-group');
	});

	it('offers no action for an empty selection', () => {
		expect(
			getGroupItemActions({
				dispatch: jest.fn(),
				items: [],
				publishedChildren: new Set<Uuid>(),
				structure: root(),
			})
		).toHaveLength(0);
	});

	it('offers the repeatable-group action while the feature flag is off, exactly as before', () => {
		config.isNonRepeatableGroupsEnabled = false;

		const sku = field('sku');

		const actions = getGroupItemActions({
			dispatch: jest.fn(),
			items: [sku],
			publishedChildren: new Set<Uuid>(),
			structure: root(sku),
		});

		expect(actions).toHaveLength(1);
		expect(actions[0].label).toBe('create-repeatable-group');
	});

	it('keeps offering the action inside a repeatable group while the feature flag is off, so the repeatable flow can report why', () => {
		config.isNonRepeatableGroupsEnabled = false;

		const repeatable = repeatableGroup();

		const sku = {...field('sku'), parent: repeatable.uuid};

		const structure = root({
			...repeatable,
			children: new Map([[sku.uuid, sku]]),
		});

		expect(
			getGroupItemActions({
				dispatch: jest.fn(),
				items: [sku],
				publishedChildren: new Set<Uuid>(),
				structure,
			})
		).toHaveLength(1);
	});

	it('offers the group action with the feature flag on', () => {
		const sku = field('sku');

		const actions = getGroupItemActions({
			dispatch: jest.fn(),
			items: [sku],
			publishedChildren: new Set<Uuid>(),
			structure: root(sku),
		});

		expect(actions[0].label).toBe('create-group');
	});

	it('creates a repeatable group when create-group is clicked, which is the default for a new group', async () => {
		const dispatch = jest.fn();
		const sku = field('sku');

		const [createGroup] = getGroupItemActions({
			dispatch,
			items: [sku],
			publishedChildren: new Set<Uuid>(),
			structure: root(sku),
		});

		await createGroup.onClick();

		expect(dispatch).toHaveBeenCalledWith({
			type: 'add-repeatable-group',
			uuids: [sku.uuid],
		});
	});
});
