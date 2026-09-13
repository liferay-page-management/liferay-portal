/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import StructureService from '../../../../src/main/resources/META-INF/resources/js/common/services/StructureService';
import {State} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/contexts/StateContext';
import {
	Group,
	Structure,
	StructureChild,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import buildObjectDefinition from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/buildObjectDefinition';
import buildState from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/buildState';
import {Field} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import handleSaveStructure from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/handleSaveStructure';

jest.mock('frontend-js-components-web', () => ({
	openToast: jest.fn(),
}));

jest.mock(
	'../../../../src/main/resources/META-INF/resources/js/common/services/StructureService',
	() => ({
		__esModule: true,
		default: {updateStructure: jest.fn()},
	})
);

jest.mock(
	'../../../../src/main/resources/META-INF/resources/js/structure_builder/config',
	() => ({
		config: {
			isNonRepeatableGroupsEnabled: true,
			objectFolderExternalReferenceCode: 'L_CMS_CONTENT_STRUCTURES',
		},
	})
);

(globalThis as any).Liferay.Util.sub = (template: string) => template;

function buildDraftState() {
	return buildState({
		mainObjectDefinition: buildObjectDefinition({
			erc: 'erc',
			label: {en_US: 'Label'},
			name: 'name',
			spaces: 'all',
		}),
		objectDefinitions: {},
	})!;
}

it('ends the operation when the save fails so the Save button stops loading', async () => {
	(StructureService.updateStructure as jest.Mock).mockResolvedValue({
		error: 'in-use',
	});

	const dispatch = jest.fn();

	await handleSaveStructure({
		dispatch,
		state: buildDraftState(),
		validate: () => true,
	});

	expect(dispatch).toHaveBeenCalledWith({type: 'end-operation'});
});

it('resets the structure status to draft after a successful update save so the Save button becomes interactable again', async () => {
	(StructureService.updateStructure as jest.Mock).mockResolvedValue({
		error: null,
	});

	const dispatch = jest.fn();

	await handleSaveStructure({
		dispatch,
		state: buildDraftState(),
		validate: () => true,
	});

	expect(dispatch).toHaveBeenCalledWith({
		type: 'save-structure',
	});
});

it('saves a structure with groups in a single request', async () => {
	(StructureService.updateStructure as jest.Mock).mockResolvedValue({
		error: null,
	});

	jest.spyOn(Liferay.ThemeDisplay, 'getDefaultLanguageId').mockReturnValue(
		'en_US'
	);

	const structureUuid = getUuid();
	const rootGroupUuid = getUuid();

	const field = (name: string, parent: Uuid): Field => ({
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
	});

	const title = field('title', structureUuid);
	const sku = field('sku', rootGroupUuid);

	const rootGroup: Group = {
		children: new Map([[sku.uuid, sku]]),
		isRepeatable: false,
		label: {en_US: 'Details'},
		parent: structureUuid,
		type: 'group',
		uuid: rootGroupUuid,
	};

	const structure: Structure = {
		children: new Map<Uuid, StructureChild>([
			[title.uuid, title],
			[rootGroupUuid, rootGroup],
		]),
		erc: 'erc',
		label: {en_US: 'Structure'},
		name: 'Structure',
		path: '',
		slug: '',
		spaces: 'all',
		status: 'draft',
		system: false,
		type: 'L_CUSTOM_STRUCTURES',
		uuid: structureUuid,
		workflows: {},
	};

	const state: State = {
		clipboard: null,
		history: {
			deletedChildren: [],
			deletedGroupERCs: [],
			deletedRelationships: [],
			modifiedNames: new Set(),
			modifiedSlugs: new Set(),
		},
		invalids: new Map(),
		operation: null,
		publishedChildren: new Set(),
		renamingItemUuid: null,
		savedChildren: new Set(),
		selection: [],
		structure,
		unsavedChanges: false,
	};

	await handleSaveStructure({
		dispatch: jest.fn(),
		state,
		validate: () => true,
	});

	expect(StructureService.updateStructure).toHaveBeenCalledWith(
		expect.objectContaining({erc: 'erc'})
	);
});
