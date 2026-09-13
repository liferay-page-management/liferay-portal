/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {fireEvent, render, screen} from '@testing-library/react';
import React from 'react';

import GroupSettings from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/components/settings/GroupSettings';
import {config} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/config';
import {Group} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import getUuid from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import {MockState, MockStateProvider} from '../../mocks/MockStateProvider';

jest.mock(
	'../../../../../src/main/resources/META-INF/resources/js/structure_builder/config',
	() => ({config: {isNonRepeatableGroupsEnabled: true}})
);

const GROUP_UUID = getUuid();
const ROOT_UUID = getUuid();

const GROUP: Group = {
	children: new Map(),
	isRepeatable: false,
	label: {en_US: 'Specifications'},
	parent: ROOT_UUID,
	type: 'group',
	uuid: GROUP_UUID,
};

const FIELD_UUID = getUuid();

const FIELD = {
	erc: 'sku-erc',
	indexableConfig: {indexed: false},
	label: {en_US: 'SKU'},
	localized: false,
	locked: false,
	name: 'sku',
	parent: GROUP_UUID,
	required: false,
	settings: {},
	type: 'text',
	uuid: FIELD_UUID,
};

const GROUP_WITH_FIELD: Group = {
	...GROUP,
	children: new Map([[FIELD_UUID, FIELD]]),
} as Group;

const STATE_WITH_FIELD = {
	invalids: new Map(),
	structure: {
		children: new Map([[GROUP_UUID, GROUP_WITH_FIELD]]),
		erc: 'structure-erc',
		label: {en_US: 'Product'},
		name: 'Product',
		type: 'L_CMS_CONTENT_STRUCTURES',
		uuid: ROOT_UUID,
	},
} as MockState;

const DEFAULT_STATE: MockState = {
	invalids: new Map(),
	structure: {
		children: new Map([[GROUP_UUID, GROUP]]),
		erc: 'structure-erc',
		label: {en_US: 'Product'},
		name: 'Product',
		type: 'L_CMS_CONTENT_STRUCTURES',
		uuid: ROOT_UUID,
	},
};

const MOCK_DISPATCH = jest.fn();

const renderComponent = ({
	disabled = false,
	dispatch = MOCK_DISPATCH,
	group = GROUP,
	state = DEFAULT_STATE,
}: {
	disabled?: boolean;
	dispatch?: jest.Mock;
	group?: Group;
	state?: MockState;
} = {}) => {
	return render(
		<MockStateProvider dispatch={dispatch} state={state}>
			<GroupSettings disabled={disabled} group={group} />
		</MockStateProvider>
	);
};

describe('GroupSettings', () => {
	beforeEach(() => {
		jest.clearAllMocks();
	});

	afterEach(() => {
		config.isNonRepeatableGroupsEnabled = true;
	});

	it('labels the field type as a group', () => {
		renderComponent();

		expect(screen.getByText('group')).toHaveClass('label-item');
	});

	it('shows the group in the breadcrumb, under its structure', () => {
		renderComponent();

		expect(screen.getByText('Product')).toBeInTheDocument();
		expect(screen.getByText('Specifications')).toBeInTheDocument();
	});

	it('dispatches update-group when the label input is saved', () => {
		renderComponent();

		fireEvent.blur(screen.getAllByRole('textbox')[0]);

		expect(MOCK_DISPATCH).toHaveBeenCalledWith({
			label: {en_US: 'Specifications'},
			type: 'update-group',
			uuid: GROUP_UUID,
		});
	});

	it('disables the label input when the group cannot be edited', () => {
		renderComponent({disabled: true});

		expect(screen.getAllByRole('textbox')[0]).toBeDisabled();
	});

	it('offers the repeatable flag, reflecting the group', () => {
		renderComponent();

		expect(screen.getByLabelText('repeatable')).not.toBeChecked();
	});

	it('hides the repeatable flag while the feature flag is off, where a group is always repeatable', () => {
		config.isNonRepeatableGroupsEnabled = false;

		renderComponent();

		expect(screen.queryByLabelText('repeatable')).not.toBeInTheDocument();
	});

	it('labels the field type as a repeatable group while the feature flag is off', () => {
		config.isNonRepeatableGroupsEnabled = false;

		renderComponent();

		expect(screen.getByText('repeatable-group')).toHaveClass('label-item');
	});

	it('locks the repeatable flag once the group is published, since it keeps its object definition', () => {
		const published: Group = {...GROUP, isRepeatable: true} as Group;

		renderComponent({
			group: published,
			state: {
				...DEFAULT_STATE,
				publishedChildren: new Set([GROUP_UUID]),
				structure: {
					...DEFAULT_STATE.structure,
					children: new Map([[GROUP_UUID, published]]),
				},
			} as MockState,
		});

		expect(screen.getByLabelText('repeatable')).toBeDisabled();
	});

	it('leaves the repeatable flag editable for an unpublished group that holds a field', () => {
		renderComponent({
			group: GROUP_WITH_FIELD,
			state: STATE_WITH_FIELD,
		});

		expect(screen.getByLabelText('repeatable')).not.toBeDisabled();
	});

	it('leaves the repeatable flag editable for an empty group, which can be filled later', () => {
		renderComponent();

		expect(screen.getByLabelText('repeatable')).not.toBeDisabled();
	});

	it('disables the repeatable flag for a group that holds another group', () => {
		const nestedUuid = getUuid();

		const outer = {
			...GROUP,
			children: new Map([
				[
					nestedUuid,
					{
						children: new Map(),
						isRepeatable: false,
						label: {en_US: 'Inner'},
						parent: GROUP_UUID,
						type: 'group',
						uuid: nestedUuid,
					},
				],
			]),
		} as Group;

		renderComponent({
			group: outer,
			state: {
				invalids: new Map(),
				structure: {
					...DEFAULT_STATE.structure,
					children: new Map([[GROUP_UUID, outer]]),
				},
			} as MockState,
		});

		expect(screen.getByLabelText('repeatable')).toBeDisabled();
	});
});
