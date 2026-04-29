/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import StructureService from '../../../../src/main/resources/META-INF/resources/js/common/services/StructureService';
import {State} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/contexts/StateContext';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import handleSaveStructure from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/handleSaveStructure';

jest.mock('frontend-js-components-web', () => ({
	openToast: jest.fn(),
}));

jest.mock(
	'../../../../src/main/resources/META-INF/resources/js/common/services/StructureService',
	() => ({
		__esModule: true,
		default: {
			createStructure: jest.fn(),
			updateStructure: jest.fn(),
		},
	})
);

(globalThis as any).Liferay.Util.sub = (template: string) => template;

function buildState(status: State['structure']['status']): State {
	return {
		history: {
			deletedChildren: [],
			deletedGroupERCs: [],
			deletedRelationships: [],
			modifiedNames: new Set(),
		},
		invalids: new Map(),
		publishedChildren: new Set(),
		renamingItemUuid: null,
		selection: [],
		structure: {
			children: new Map(),
			erc: 'main-erc',
			id: status === 'new' ? undefined : 1234,
			label: {en_US: 'Main Structure'},
			name: 'mainStructure',
			path: '',
			spaces: 'all',
			status,
			system: false,
			type: 'L_CMS_CONTENT_STRUCTURES',
			uuid: getUuid(),
			workflows: {},
		},
		unsavedChanges: true,
	};
}

describe('handleSaveStructure', () => {
	beforeEach(() => {
		jest.clearAllMocks();
	});

	it('resets the structure status after a successful update so the Save button becomes interactable again', async () => {
		(StructureService.updateStructure as jest.Mock).mockResolvedValue({
			error: null,
		});

		const dispatch = jest.fn();

		await handleSaveStructure({
			dispatch,
			state: buildState('draft'),
			validate: () => true,
		});

		expect(dispatch).toHaveBeenNthCalledWith(1, {
			status: 'saving',
			type: 'set-structure-status',
		});

		expect(dispatch).toHaveBeenNthCalledWith(2, {
			status: 'draft',
			type: 'set-structure-status',
		});

		expect(dispatch).toHaveBeenNthCalledWith(3, {type: 'clear-errors'});
	});

	it('dispatches create-structure on a successful first save', async () => {
		(StructureService.createStructure as jest.Mock).mockResolvedValue({
			data: {id: 9999},
			error: null,
		});

		const dispatch = jest.fn();

		await handleSaveStructure({
			dispatch,
			state: buildState('new'),
			validate: () => true,
		});

		expect(dispatch).toHaveBeenNthCalledWith(1, {
			status: 'saving',
			type: 'set-structure-status',
		});

		expect(dispatch).toHaveBeenNthCalledWith(2, {
			id: 9999,
			type: 'create-structure',
		});
	});

	it('does not dispatch any save actions when validation fails', async () => {
		const dispatch = jest.fn();

		await handleSaveStructure({
			dispatch,
			state: buildState('draft'),
			validate: () => false,
		});

		expect(dispatch).not.toHaveBeenCalled();
		expect(StructureService.updateStructure).not.toHaveBeenCalled();
	});
});
