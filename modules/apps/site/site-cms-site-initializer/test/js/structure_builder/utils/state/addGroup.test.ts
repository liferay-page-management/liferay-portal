/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {setDefaultLanguageLabels} from '../../../../../src/main/resources/META-INF/resources/js/common/utils/defaultLanguageLabels';
import {
	Group,
	Structure,
} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Field} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getUuid from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import addGroup from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/state/addGroup';

const GROUP_UUID = getUuid();
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

function root(...fields: Field[]): Structure {
	return {
		children: new Map(fields.map((child) => [child.uuid, child])),
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

function toParents(group: Group): string[] {
	return Array.from(group.children.values()).map((child) => child.parent);
}

describe('addGroup', () => {
	beforeEach(() => {
		jest.spyOn(
			Liferay.ThemeDisplay,
			'getDefaultLanguageId'
		).mockReturnValue('en_US');
		jest.spyOn(Liferay.ThemeDisplay, 'getLanguageId').mockReturnValue(
			'es_ES'
		);
	});

	afterEach(() => {
		jest.restoreAllMocks();
		setDefaultLanguageLabels({labels: {}, locale: 'en_US'});
	});

	it('creates a group node labeled under both default and current language IDs', () => {
		const children = addGroup({
			groupChildren: [],
			groupParent: ROOT_UUID,
			groupUuid: GROUP_UUID,
			root: root(),
		});

		const group = children.get(GROUP_UUID) as Group;

		expect(group.type).toBe('group');
		expect(group.label).toEqual({en_US: 'group', es_ES: 'group'});
	});

	it('does not give the group an erc or relationship (presentation only)', () => {
		const children = addGroup({
			groupChildren: [],
			groupParent: ROOT_UUID,
			groupUuid: GROUP_UUID,
			root: root(),
		});

		const group = children.get(GROUP_UUID) as Group;

		expect(group).not.toHaveProperty('erc');
		expect(group).not.toHaveProperty('relationshipERC');
		expect(group).not.toHaveProperty('relationshipName');
	});

	it('moves the selected fields into the group and reparents them', () => {
		const width = field('width');
		const height = field('height');

		const children = addGroup({
			groupChildren: [width, height],
			groupParent: ROOT_UUID,
			groupUuid: GROUP_UUID,
			root: root(width, height),
		});

		const group = children.get(GROUP_UUID) as Group;

		expect(children.has(width.uuid)).toBe(false);
		expect(children.has(height.uuid)).toBe(false);

		expect(toParents(group)).toEqual([GROUP_UUID, GROUP_UUID]);
	});

	it('leaves the fields that were not selected where they are', () => {
		const title = field('title');
		const sku = field('sku');

		const children = addGroup({
			groupChildren: [sku],
			groupParent: ROOT_UUID,
			groupUuid: GROUP_UUID,
			root: root(title, sku),
		});

		const group = children.get(GROUP_UUID) as Group;

		expect(children.has(title.uuid)).toBe(true);
		expect(children.has(sku.uuid)).toBe(false);

		const movedField = group.children.get(sku.uuid) as Field;

		expect(movedField.name).toBe('sku');
		expect(movedField.parent).toBe(GROUP_UUID);
	});
});
