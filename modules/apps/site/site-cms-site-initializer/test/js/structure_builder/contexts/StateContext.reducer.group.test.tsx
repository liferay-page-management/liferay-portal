/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {act, render} from '@testing-library/react';
import React, {Dispatch, useEffect} from 'react';

import {
	Action,
	State,
	StateContextProvider,
	useSelector,
	useStateDispatch,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/contexts/StateContext';
import {
	Group,
	Structure,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import {Field} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';

const SKU_UUID = getUuid();
const STRUCTURE_UUID = getUuid();
const TITLE_UUID = getUuid();

function field(name: string, uuid: Uuid): Field {
	return {
		erc: `${name}-erc`,
		indexableConfig: {indexed: false},
		label: {en_US: name},
		localized: false,
		locked: false,
		name,
		parent: STRUCTURE_UUID,
		required: false,
		settings: {},
		type: 'text',
		uuid,
	};
}

function buildInitialState(publishedChildren: Set<Uuid> = new Set()): State {
	const structure: Structure = {
		children: new Map([
			[TITLE_UUID, field('title', TITLE_UUID)],
			[SKU_UUID, field('sku', SKU_UUID)],
		]),
		erc: 'structure-erc',
		label: {en_US: 'Structure'},
		name: 'Structure',
		path: '',
		slug: '',
		spaces: 'all',
		status: 'draft',
		system: false,
		type: 'L_CUSTOM_STRUCTURES',
		uuid: STRUCTURE_UUID,
		workflows: {},
	};

	return {
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
		publishedChildren,
		renamingItemUuid: null,
		savedChildren: new Set(),
		selection: [],
		structure,
		unsavedChanges: false,
	};
}

type Refs = {
	dispatch?: Dispatch<Action>;
	state?: State;
};

function renderWithState(initialState: State) {
	const refs: Refs = {};

	function Harness() {
		const state = useSelector((s) => s);
		const dispatch = useStateDispatch();

		useEffect(() => {
			refs.dispatch = dispatch;
			refs.state = state;
		});

		return null;
	}

	render(
		<StateContextProvider initialState={initialState}>
			<Harness />
		</StateContextProvider>
	);

	return refs;
}

describe('StateContext reducer - groups', () => {
	beforeEach(() => {
		jest.spyOn(
			Liferay.ThemeDisplay,
			'getDefaultLanguageId'
		).mockReturnValue('en_US');
		jest.spyOn(Liferay.ThemeDisplay, 'getLanguageId').mockReturnValue(
			'en_US'
		);
	});

	afterEach(() => {
		jest.restoreAllMocks();
	});

	it('wraps the selection into a group and selects it', () => {
		const refs = renderWithState(buildInitialState());

		act(() => {
			refs.dispatch!({
				parent: STRUCTURE_UUID,
				type: 'add-group',
				uuids: [SKU_UUID],
			});
		});

		const groupUuid = refs.state!.selection[0];
		const group = refs.state!.structure.children.get(groupUuid) as Group;

		expect(group.type).toBe('group');
		expect(group.children.has(SKU_UUID)).toBe(true);
		expect(refs.state!.structure.children.has(SKU_UUID)).toBe(false);
		expect(refs.state!.structure.children.has(TITLE_UUID)).toBe(true);
	});

	it('renames a group', () => {
		const refs = renderWithState(buildInitialState());

		act(() => {
			refs.dispatch!({
				parent: STRUCTURE_UUID,
				type: 'add-group',
				uuids: [SKU_UUID],
			});
		});

		const groupUuid = refs.state!.selection[0];

		act(() => {
			refs.dispatch!({
				label: {en_US: 'Specifications'},
				type: 'update-group',
				uuid: groupUuid,
			});
		});

		const group = refs.state!.structure.children.get(groupUuid) as Group;

		expect(group.label).toEqual({en_US: 'Specifications'});
	});

	it('records no data-loss history when grouping published fields (fields stay on the definition)', () => {
		const refs = renderWithState(buildInitialState(new Set([SKU_UUID])));

		act(() => {
			refs.dispatch!({
				parent: STRUCTURE_UUID,
				type: 'add-group',
				uuids: [SKU_UUID],
			});
		});

		expect(refs.state!.history.deletedChildren).toHaveLength(0);
	});

	it('records the abandoned object definition when a saved group stops being repeatable, which is reachable while it is saved but not published', () => {
		const groupUuid = getUuid();

		const initialState = buildInitialState();

		const repeatable = {
			children: new Map(),
			erc: 'group-erc',
			isRepeatable: true,
			label: {en_US: 'Group'},
			name: 'group',
			parent: STRUCTURE_UUID,
			relationshipERC: 'group-rel-erc',
			relationshipName: 'group',
			type: 'group',
			uuid: groupUuid,
		} as Group;

		initialState.structure.children.set(groupUuid, repeatable);
		initialState.savedChildren = new Set([groupUuid]);

		const refs = renderWithState(initialState);

		act(() => {
			refs.dispatch!({
				isRepeatable: false,
				type: 'set-group-repeatable',
				uuid: groupUuid,
			});
		});

		expect(refs.state!.history.deletedGroupERCs).toEqual(['group-erc']);
		expect(refs.state!.history.deletedRelationships).toEqual([
			{relationshipERC: 'group-rel-erc', structureERC: 'structure-erc'},
		]);
	});

	it('records nothing for a group that was never saved, since it has no object definition yet', () => {
		const groupUuid = getUuid();

		const initialState = buildInitialState();

		initialState.structure.children.set(groupUuid, {
			children: new Map(),
			erc: 'group-erc',
			isRepeatable: true,
			label: {en_US: 'Group'},
			name: 'group',
			parent: STRUCTURE_UUID,
			relationshipERC: 'group-rel-erc',
			relationshipName: 'group',
			type: 'group',
			uuid: groupUuid,
		} as Group);

		const refs = renderWithState(initialState);

		act(() => {
			refs.dispatch!({
				isRepeatable: false,
				type: 'set-group-repeatable',
				uuid: groupUuid,
			});
		});

		expect(refs.state!.history.deletedGroupERCs).toEqual([]);
		expect(refs.state!.history.deletedRelationships).toEqual([]);
	});

	it('withdraws the queued object definition deletion when the group becomes repeatable again, so the save does not delete a definition the payload still references', () => {
		const groupUuid = getUuid();

		const initialState = buildInitialState();

		initialState.structure.children.set(groupUuid, {
			children: new Map(),
			erc: 'group-erc',
			isRepeatable: true,
			label: {en_US: 'Group'},
			name: 'group',
			parent: STRUCTURE_UUID,
			relationshipERC: 'group-rel-erc',
			relationshipName: 'group',
			type: 'group',
			uuid: groupUuid,
		} as Group);
		initialState.savedChildren = new Set([groupUuid]);

		const refs = renderWithState(initialState);

		act(() => {
			refs.dispatch!({
				isRepeatable: false,
				type: 'set-group-repeatable',
				uuid: groupUuid,
			});
		});

		expect(refs.state!.history.deletedGroupERCs).toEqual(['group-erc']);

		act(() => {
			refs.dispatch!({
				isRepeatable: true,
				type: 'set-group-repeatable',
				uuid: groupUuid,
			});
		});

		expect(refs.state!.history.deletedGroupERCs).toEqual([]);
		expect(refs.state!.history.deletedRelationships).toEqual([]);
	});

	it('records the abandoned object definition when a saved repeatable group is ungrouped', () => {
		const groupUuid = getUuid();

		const initialState = buildInitialState();

		initialState.structure.children.set(groupUuid, {
			children: new Map(),
			erc: 'group-erc',
			isRepeatable: true,
			label: {en_US: 'Group'},
			name: 'group',
			parent: STRUCTURE_UUID,
			relationshipERC: 'group-rel-erc',
			relationshipName: 'group',
			type: 'group',
			uuid: groupUuid,
		} as Group);
		initialState.savedChildren = new Set([groupUuid]);

		const refs = renderWithState(initialState);

		act(() => {
			refs.dispatch!({type: 'ungroup', uuid: groupUuid});
		});

		expect(refs.state!.history.deletedGroupERCs).toEqual(['group-erc']);
		expect(refs.state!.history.deletedRelationships).toEqual([
			{relationshipERC: 'group-rel-erc', structureERC: 'structure-erc'},
		]);
	});

	it('leaves the history untouched when a non-repeatable group is ungrouped, since it has no object definition', () => {
		const groupUuid = getUuid();

		const initialState = buildInitialState();

		initialState.structure.children.set(groupUuid, {
			children: new Map(),
			isRepeatable: false,
			label: {en_US: 'Group'},
			parent: STRUCTURE_UUID,
			type: 'group',
			uuid: groupUuid,
		} as Group);
		initialState.savedChildren = new Set([groupUuid]);

		const refs = renderWithState(initialState);

		act(() => {
			refs.dispatch!({type: 'ungroup', uuid: groupUuid});
		});

		expect(refs.state!.history.deletedChildren).toEqual([]);
		expect(refs.state!.history.deletedGroupERCs).toEqual([]);
		expect(refs.state!.history.deletedRelationships).toEqual([]);
	});

	it('clears the empty-group error as soon as the group gains a field, so a stale banner does not block publishing', () => {
		const groupUuid = getUuid();

		const initialState = buildInitialState();

		initialState.structure.children.set(groupUuid, {
			children: new Map(),
			isRepeatable: false,
			label: {en_US: 'Nuovo'},
			parent: STRUCTURE_UUID,
			type: 'group',
			uuid: groupUuid,
		} as Group);

		initialState.invalids = new Map([
			[groupUuid, new Map([['global', 'group-without-fields']])],
		]) as State['invalids'];

		const refs = renderWithState(initialState);

		act(() => {
			refs.dispatch!({
				field: {
					...field('fieldX', getUuid()),
					parent: groupUuid,
				},
				type: 'add-field',
			});
		});

		expect(refs.state!.invalids.has(groupUuid)).toBe(false);
	});

	it('keeps a group error that is not about being empty when a field is added', () => {
		const groupUuid = getUuid();

		const initialState = buildInitialState();

		initialState.structure.children.set(groupUuid, {
			children: new Map(),
			isRepeatable: false,
			label: {},
			parent: STRUCTURE_UUID,
			type: 'group',
			uuid: groupUuid,
		} as Group);

		initialState.invalids = new Map([
			[
				groupUuid,
				new Map([
					['global', 'group-without-fields'],
					['label', 'empty'],
				]),
			],
		]) as State['invalids'];

		const refs = renderWithState(initialState);

		act(() => {
			refs.dispatch!({
				field: {
					...field('fieldX', getUuid()),
					parent: groupUuid,
				},
				type: 'add-field',
			});
		});

		expect(refs.state!.invalids.get(groupUuid)?.get('label')).toBe('empty');
		expect(refs.state!.invalids.get(groupUuid)?.has('global')).toBe(false);
	});
});
