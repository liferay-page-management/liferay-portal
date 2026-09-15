/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openConfirmModal} from '@liferay/layout-js-components-web';
import {openToast} from 'frontend-js-components-web';

import {
	Group,
	Structure,
	StructureChild,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import {Field} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import handleSetGroupRepeatable from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/handleSetGroupRepeatable';

jest.mock('frontend-js-components-web', () => ({openToast: jest.fn()}));

jest.mock('@liferay/layout-js-components-web', () => ({
	openConfirmModal: jest.fn(() => Promise.resolve(true)),
}));

const ROOT_UUID = getUuid();

function field(name: string, parent: Uuid, locked = false): Field {
	return {
		erc: `${name}-erc`,
		indexableConfig: {indexed: false},
		label: {en_US: name},
		localized: false,
		locked,
		name,
		parent,
		required: false,
		settings: {},
		type: 'text',
		uuid: getUuid(),
	};
}

function group({
	children = [],
	isRepeatable = false,
	parent = ROOT_UUID,
}: {
	children?: StructureChild[];
	isRepeatable?: boolean;
	parent?: Uuid;
} = {}): Group {
	const uuid = getUuid();

	return {
		children: new Map(children.map((child) => [child.uuid, child])),
		erc: 'group-erc',
		isRepeatable,
		label: {en_US: 'Group'},
		name: 'group',
		parent,
		relationshipERC: 'group-rel-erc',
		relationshipName: 'group',
		type: 'group',
		uuid,
	} as Group;
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
	} as Structure;
}

describe('handleSetGroupRepeatable', () => {
	afterEach(() => {
		(openToast as jest.Mock).mockClear();
		(openConfirmModal as jest.Mock).mockClear();
	});

	it('turns the flag on for a group that can be repeatable', async () => {
		const dispatch = jest.fn();
		const target = group();
		const sku = field('sku', target.uuid);

		target.children.set(sku.uuid, sku);

		await handleSetGroupRepeatable({
			dispatch,
			group: target,
			isRepeatable: true,
			publishedChildren: new Set<Uuid>(),
			structure: root(target),
		});

		expect(dispatch).toHaveBeenCalledWith({
			isRepeatable: true,
			type: 'set-group-repeatable',
			uuid: target.uuid,
		});
		expect(openToast).not.toHaveBeenCalled();
	});

	it('refuses a group that holds another group, which a repeatable group cannot', async () => {
		const dispatch = jest.fn();
		const target = group();
		const nested = group({parent: target.uuid});

		target.children.set(nested.uuid, nested);

		await handleSetGroupRepeatable({
			dispatch,
			group: target,
			isRepeatable: true,
			publishedChildren: new Set<Uuid>(),
			structure: root(target),
		});

		expect(openToast).toHaveBeenCalledWith({
			message: 'a-group-that-contains-another-group-cannot-be-repeatable',
			type: 'danger',
		});
		expect(dispatch).not.toHaveBeenCalled();
	});

	it('refuses a group that holds system fields', async () => {
		const dispatch = jest.fn();
		const target = group();
		const title = field('title', target.uuid, true);

		target.children.set(title.uuid, title);

		await handleSetGroupRepeatable({
			dispatch,
			group: target,
			isRepeatable: true,
			publishedChildren: new Set<Uuid>(),
			structure: root(target),
		});

		expect(openToast).toHaveBeenCalledWith({
			message: 'a-group-that-contains-system-fields-cannot-be-repeatable',
			type: 'danger',
		});
		expect(dispatch).not.toHaveBeenCalled();
	});

	it('turns the flag on for an empty group, since being empty is only reported when publishing', async () => {
		const dispatch = jest.fn();
		const target = group();

		await handleSetGroupRepeatable({
			dispatch,
			group: target,
			isRepeatable: true,
			publishedChildren: new Set<Uuid>(),
			structure: root(target),
		});

		expect(dispatch).toHaveBeenCalledWith({
			isRepeatable: true,
			type: 'set-group-repeatable',
			uuid: target.uuid,
		});
		expect(openToast).not.toHaveBeenCalled();
	});

	it('warns before moving published fields into a new object definition', async () => {
		const dispatch = jest.fn();
		const target = group();
		const sku = field('sku', target.uuid);

		target.children.set(sku.uuid, sku);

		await handleSetGroupRepeatable({
			dispatch,
			group: target,
			isRepeatable: true,
			publishedChildren: new Set<Uuid>([sku.uuid]),
			structure: root(target),
		});

		expect(openConfirmModal).toHaveBeenCalled();
		expect(dispatch).toHaveBeenCalled();
	});

	it('refuses to turn the flag off once the group is published, matching the ungroup rule', async () => {
		const dispatch = jest.fn();
		const target = group({isRepeatable: true});

		await handleSetGroupRepeatable({
			dispatch,
			group: target,
			isRepeatable: false,
			publishedChildren: new Set<Uuid>([target.uuid]),
			structure: root(target),
		});

		expect(openToast).toHaveBeenCalledWith({
			message:
				'the-ungroup-action-cannot-be-done-because-this-repeatable-group-is-already-published',
			type: 'danger',
		});
		expect(dispatch).not.toHaveBeenCalled();
	});

	it('turns the flag off for an unpublished group', async () => {
		const dispatch = jest.fn();
		const target = group({isRepeatable: true});

		await handleSetGroupRepeatable({
			dispatch,
			group: target,
			isRepeatable: false,
			publishedChildren: new Set<Uuid>(),
			structure: root(target),
		});

		expect(dispatch).toHaveBeenCalledWith({
			isRepeatable: false,
			type: 'set-group-repeatable',
			uuid: target.uuid,
		});
	});
});
