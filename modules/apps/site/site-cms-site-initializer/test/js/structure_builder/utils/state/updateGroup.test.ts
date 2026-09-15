/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	Group,
	Structure,
} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import {Field} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getUuid from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import updateGroup from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/state/updateGroup';

const GROUP_UUID = getUuid();
const ROOT_UUID = getUuid();

function field(name: string, parent: Uuid): Field {
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

function structureWithGroup(
	label: Liferay.Language.LocalizedValue<string>
): Structure {
	const sku = field('sku', GROUP_UUID);

	const group: Group = {
		children: new Map([[sku.uuid, sku]]),
		isRepeatable: false,
		label,
		parent: ROOT_UUID,
		type: 'group',
		uuid: GROUP_UUID,
	};

	return {
		children: new Map([[GROUP_UUID, group]]),
		erc: 'root-erc',
		label: {},
		name: 'Root',
		path: '',
		slug: '',
		spaces: 'all',
		status: 'draft',
		system: false,
		type: 'L_CMS_CONTENT_STRUCTURES',
		uuid: ROOT_UUID,
		workflows: {},
	};
}

describe('updateGroup', () => {
	it('renames the group while preserving its fields', () => {
		const result = updateGroup({
			invalids: new Map(),
			label: {en_US: 'Specifications'},
			structure: structureWithGroup({en_US: 'Group'}),
			uuid: GROUP_UUID,
		});

		const group = result!.children.get(GROUP_UUID) as Group;

		expect(group.label).toEqual({en_US: 'Specifications'});
		expect(group.children.size).toBe(1);
	});

	it('flags an empty label and clears the error once it is valid again', () => {
		const structure = structureWithGroup({en_US: 'Group'});

		const flagged = updateGroup({
			invalids: new Map(),
			label: {en_US: ''},
			structure,
			uuid: GROUP_UUID,
		})!;

		expect(flagged.invalids.get(GROUP_UUID)?.get('label')).toBe('empty');

		const cleared = updateGroup({
			invalids: flagged.invalids,
			label: {en_US: 'Specs'},
			structure,
			uuid: GROUP_UUID,
		})!;

		expect(cleared.invalids.has(GROUP_UUID)).toBe(false);
	});

	it('returns undefined when the group no longer exists', () => {
		const result = updateGroup({
			invalids: new Map(),
			label: {en_US: 'X'},
			structure: structureWithGroup({en_US: 'Group'}),
			uuid: getUuid(),
		});

		expect(result).toBeUndefined();
	});

	it('does not mutate the passed-in validation map', () => {
		const invalids = new Map();

		updateGroup({
			invalids,
			label: {en_US: ''},
			structure: structureWithGroup({en_US: 'Group'}),
			uuid: GROUP_UUID,
		});

		expect(invalids.size).toBe(0);
	});
});
