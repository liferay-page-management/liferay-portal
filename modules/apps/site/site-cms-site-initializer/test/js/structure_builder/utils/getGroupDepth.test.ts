/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	Group,
	Structure,
	StructureChild,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import {Field} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getGroupDepth from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getGroupDepth';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';

const PANEL_UUID = getUuid();
const ROOT_UUID = getUuid();
const SKU_UUID = getUuid();
const TAB_UUID = getUuid();
const TITLE_UUID = getUuid();
const WIDTH_UUID = getUuid();

function field(name: string, parent: Uuid, uuid: Uuid): Field {
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
		uuid,
	};
}

function group({
	children,
	label,
	parent,
	uuid,
}: {
	children: StructureChild[];
	label: string;
	parent: Uuid;
	uuid: Uuid;
}): Group {
	return {
		children: toMap(children),
		isRepeatable: false,
		label: {en_US: label},
		parent,
		type: 'group',
		uuid,
	};
}

function structure(children: StructureChild[]): Structure {
	return {
		children: toMap(children),
		erc: 'product-erc',
		label: {en_US: 'Product'},
		name: 'Product',
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

function toMap(children: StructureChild[]): Map<Uuid, StructureChild> {
	return new Map(children.map((child) => [child.uuid, child]));
}

// Depth is what decides how a group renders in the object layout, so it is
// counted from the structure root.

const PANEL = group({
	children: [field('width', PANEL_UUID, WIDTH_UUID)],
	label: 'Dimensions',
	parent: TAB_UUID,
	uuid: PANEL_UUID,
});

const TAB = group({
	children: [PANEL, field('sku', TAB_UUID, SKU_UUID)],
	label: 'Specs',
	parent: ROOT_UUID,
	uuid: TAB_UUID,
});

const STRUCTURE = structure([field('title', ROOT_UUID, TITLE_UUID), TAB]);

describe('getGroupDepth', () => {
	it('counts no enclosing group for the structure root', () => {
		expect(getGroupDepth({structure: STRUCTURE, uuid: ROOT_UUID})).toBe(0);
	});

	it('counts no enclosing group for a loose field', () => {
		expect(getGroupDepth({structure: STRUCTURE, uuid: TITLE_UUID})).toBe(0);
	});

	it('counts one for a group at the root and for the items inside it', () => {
		expect(getGroupDepth({structure: STRUCTURE, uuid: TAB_UUID})).toBe(1);
		expect(getGroupDepth({structure: STRUCTURE, uuid: SKU_UUID})).toBe(1);
	});

	it('counts two for a group nested inside a group and for the items inside it', () => {
		expect(getGroupDepth({structure: STRUCTURE, uuid: PANEL_UUID})).toBe(2);
		expect(getGroupDepth({structure: STRUCTURE, uuid: WIDTH_UUID})).toBe(2);
	});

	it('counts no enclosing group for a uuid that is not in the structure', () => {
		expect(getGroupDepth({structure: STRUCTURE, uuid: getUuid()})).toBe(0);
	});
});
