/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import AddChildDropdown from '../../../../src/main/resources/META-INF/resources/js/structure_builder/components/AddChildDropdown';
import {config} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/config';
import {
	Group,
	RepeatableGroup,
	Structure,
	StructureChild,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import {MockCacheProvider} from '../mocks/MockCacheProvider';
import {MockStateProvider} from '../mocks/MockStateProvider';

jest.mock(
	'../../../../src/main/resources/META-INF/resources/js/structure_builder/config',
	() => ({config: {isNonRepeatableGroupsEnabled: true}})
);

const NESTED_GROUP_UUID = getUuid();
const REPEATABLE_GROUP_UUID = getUuid();
const ROOT_GROUP_UUID = getUuid();
const ROOT_UUID = getUuid();

const nestedGroup: Group = {
	children: new Map(),
	isRepeatable: false,
	label: {en_US: 'Nested Group'},
	parent: ROOT_GROUP_UUID,
	type: 'group',
	uuid: NESTED_GROUP_UUID,
};

const rootGroup: Group = {
	children: new Map([[NESTED_GROUP_UUID, nestedGroup]]),
	isRepeatable: false,
	label: {en_US: 'Root Group'},
	parent: ROOT_UUID,
	type: 'group',
	uuid: ROOT_GROUP_UUID,
};

const repeatableGroup: RepeatableGroup = {
	children: new Map(),
	erc: 'repeatable-erc',
	isRepeatable: true,
	label: {en_US: 'Repeatable Group'},
	name: 'repeatable',
	parent: ROOT_UUID,
	relationshipERC: '',
	relationshipName: '',
	type: 'group',
	uuid: REPEATABLE_GROUP_UUID,
};

const structure: Partial<Structure> = {
	children: new Map<Uuid, StructureChild>([
		[REPEATABLE_GROUP_UUID, repeatableGroup],
		[ROOT_GROUP_UUID, rootGroup],
	]),
	uuid: ROOT_UUID,
};

function renderDropdown(parentUuid?: Uuid) {
	return render(
		<MockCacheProvider objectDefinitions={{}} spaces={[]}>
			<MockStateProvider state={{structure}}>
				<AddChildDropdown parentUuid={parentUuid} />
			</MockStateProvider>
		</MockCacheProvider>
	);
}

describe('AddChildDropdown', () => {
	it('offers relationship options at the structure root', async () => {
		renderDropdown();

		await userEvent.click(screen.getByLabelText('add-field'));

		expect(screen.getByText('select-related-content')).toBeInTheDocument();
		expect(
			screen.getByText('referenced-content-structure')
		).toBeInTheDocument();
	});

	it('offers both relationship options inside a root group', async () => {
		renderDropdown(ROOT_GROUP_UUID);

		await userEvent.click(screen.getByLabelText('add-field'));

		expect(screen.getByText('select-related-content')).toBeInTheDocument();
		expect(
			screen.getByText('referenced-content-structure')
		).toBeInTheDocument();
	});

	it('keeps related content but hides a referenced structure inside a nested group, nested one level deep', async () => {
		renderDropdown(NESTED_GROUP_UUID);

		await userEvent.click(screen.getByLabelText('add-field'));

		expect(screen.getByText('select-related-content')).toBeInTheDocument();
		expect(
			screen.queryByText('referenced-content-structure')
		).not.toBeInTheDocument();
	});

	it('offers an empty group wherever the resulting depth still renders', async () => {
		config.isNonRepeatableGroupsEnabled = true;

		renderDropdown();

		await userEvent.click(screen.getByLabelText('add-field'));

		expect(screen.getByText('create-group')).toBeInTheDocument();
	});

	it('offers an empty group inside a root group, nested one level deep', async () => {
		config.isNonRepeatableGroupsEnabled = true;

		renderDropdown(ROOT_GROUP_UUID);

		await userEvent.click(screen.getByLabelText('add-field'));

		expect(screen.getByText('create-group')).toBeInTheDocument();
	});

	it('hides the empty group inside a nested group, which would exceed the depth cap', async () => {
		config.isNonRepeatableGroupsEnabled = true;

		renderDropdown(NESTED_GROUP_UUID);

		await userEvent.click(screen.getByLabelText('add-field'));

		expect(screen.queryByText('create-group')).not.toBeInTheDocument();
	});

	it('hides the empty group inside a repeatable group, which has nowhere to render one', async () => {
		config.isNonRepeatableGroupsEnabled = true;

		renderDropdown(REPEATABLE_GROUP_UUID);

		await userEvent.click(screen.getByLabelText('add-field'));

		expect(screen.queryByText('create-group')).not.toBeInTheDocument();
	});

	it('hides the empty group when the feature flag is off', async () => {
		config.isNonRepeatableGroupsEnabled = false;

		renderDropdown();

		await userEvent.click(screen.getByLabelText('add-field'));

		expect(screen.queryByText('create-group')).not.toBeInTheDocument();

		config.isNonRepeatableGroupsEnabled = true;
	});
});
