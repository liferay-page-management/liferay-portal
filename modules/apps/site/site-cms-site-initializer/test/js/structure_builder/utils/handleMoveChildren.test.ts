/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openToast} from 'frontend-js-components-web';

import {
	Group,
	RepeatableGroup,
	Structure,
	StructureChild,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Field} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import handleMoveChildren from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/handleMoveChildren';

jest.mock('@liferay/layout-js-components-web', () => ({
	openConfirmModal: jest.fn(),
	openOptionsModal: jest.fn(),
}));

jest.mock('frontend-js-components-web', () => ({openToast: jest.fn()}));

const GROUP_UUID = getUuid();
const LOCKED_FIELD_UUID = getUuid();
const REPEATABLE_GROUP_UUID = getUuid();
const STRUCTURE_UUID = getUuid();

function group(): Group {
	return {
		children: new Map(),
		isRepeatable: false,
		label: {en_US: 'Details'},
		parent: STRUCTURE_UUID,
		type: 'group',
		uuid: GROUP_UUID,
	};
}

function lockedField(): Field {
	return {
		erc: 'title-erc',
		indexableConfig: {indexed: false},
		label: {en_US: 'Title'},
		localized: false,
		locked: true,
		name: 'title',
		parent: STRUCTURE_UUID,
		required: true,
		settings: {},
		type: 'text',
		uuid: LOCKED_FIELD_UUID,
	};
}

function repeatableGroup(): RepeatableGroup {
	return {
		children: new Map(),
		erc: 'group-erc',
		isRepeatable: true,
		label: {en_US: 'Group'},
		name: 'group',
		parent: STRUCTURE_UUID,
		relationshipERC: 'group-rel-erc',
		relationshipName: 'group',
		type: 'group',
		uuid: REPEATABLE_GROUP_UUID,
	};
}

function structure(children: StructureChild[]): Structure {
	return {
		children: new Map(children.map((child) => [child.uuid, child])),
		erc: 'structure-erc',
		label: {en_US: 'Structure'},
		name: 'Structure',
		path: '',
		slug: '',
		spaces: 'all',
		status: 'draft',
		system: false,
		type: 'L_CMS_CONTENT_STRUCTURES',
		uuid: STRUCTURE_UUID,
		workflows: {},
	};
}

describe('handleMoveChildren', () => {
	beforeEach(() => {
		(openToast as jest.Mock).mockClear();
	});

	it('moves a system (locked) field into a group (layout only) without warning', async () => {
		const dispatch = jest.fn();

		await handleMoveChildren({
			deletedChildren: [],
			dispatch,
			publishedChildren: new Set(),
			structure: structure([lockedField(), group()]),
			targetUuid: GROUP_UUID,
			uuids: [LOCKED_FIELD_UUID],
		});

		expect(dispatch).toHaveBeenCalledWith(
			expect.objectContaining({
				items: [
					expect.objectContaining({
						parent: GROUP_UUID,
						uuid: LOCKED_FIELD_UUID,
					}),
				],
				targetUuid: GROUP_UUID,
				type: 'move-children',
			})
		);
		expect(openToast).not.toHaveBeenCalled();
	});

	it('does not move a system (locked) field into a repeatable group', async () => {
		const dispatch = jest.fn();

		await handleMoveChildren({
			deletedChildren: [],
			dispatch,
			publishedChildren: new Set(),
			structure: structure([lockedField(), repeatableGroup()]),
			targetUuid: REPEATABLE_GROUP_UUID,
			uuids: [LOCKED_FIELD_UUID],
		});

		expect(dispatch).not.toHaveBeenCalledWith(
			expect.objectContaining({type: 'move-children'})
		);
		expect(openToast).toHaveBeenCalled();
	});
});
