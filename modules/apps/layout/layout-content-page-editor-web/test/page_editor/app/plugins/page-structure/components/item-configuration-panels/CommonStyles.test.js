/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import '@testing-library/jest-dom/extend-expect';
import {cleanup, fireEvent, render} from '@testing-library/react';
import React from 'react';

import {config} from '../../../../../../../src/main/resources/META-INF/resources/page_editor/app/config/index';
import {StoreAPIContextProvider} from '../../../../../../../src/main/resources/META-INF/resources/page_editor/app/store/index';
import updateItemConfig from '../../../../../../../src/main/resources/META-INF/resources/page_editor/app/thunks/updateItemConfig';
import {CommonStyles} from '../../../../../../../src/main/resources/META-INF/resources/page_editor/plugins/page-structure/components/item-configuration-panels/CommonStyles';

const STATE = {
	segmentsExperienceId: '0',
	selectedViewportSize: 'desktop',
};

const item = {
	children: [],
	config: {
		gutters: true,
		modulesPerRow: 2,
		numberOfColumns: 2,
		styles: {},
		tablet: {styles: {}},
		verticalAlignment: 'top',
	},
	itemId: '0',
	parentId: '',
	type: '',
};

const renderComponent = ({state = STATE, dispatch = () => {}}) =>
	render(
		<StoreAPIContextProvider
			dispatch={dispatch}
			getState={() => ({...STATE, ...state})}
		>
			<CommonStyles commonStylesValues={{}} itemId={item.itemId} />
		</StoreAPIContextProvider>
	);

jest.mock(
	'../../../../../../../src/main/resources/META-INF/resources/page_editor/app/config',
	() => ({
		config: {
			availableViewportSizes: {
				desktop: {label: 'Desktop'},
				tablet: {label: 'tablet'},
			},
			commonStyles: [
				{
					label: 'margin',
					styles: [
						{
							dataType: 'string',
							defaultValue: '0',
							dependencies: [],
							displaySize: 'small',
							label: 'margin-top',
							name: 'marginTop',
							responsive: true,
							responsiveTemplate: 'mt{viewport}{value}',
							type: 'select',
							validValues: [
								{
									label: '0',
									value: '0',
								},
								{
									label: '1',
									value: '1',
								},
							],
						},
					],
				},
			],
			responsiveEnabled: true,
		},
	})
);

jest.mock(
	'../../../../../../../src/main/resources/META-INF/resources/page_editor/app/thunks/updateItemConfig',
	() => jest.fn()
);

describe('CommonStyles', () => {
	afterEach(() => {
		cleanup();
		config.responsiveEnabled = true;
		updateItemConfig.mockClear();
	});

	it('shows common styles panel', async () => {
		const {getByText} = renderComponent({});

		expect(getByText('common-styles')).toBeInTheDocument();
	});

	it('allows changing common styles for a given viewport', async () => {
		const {getByLabelText} = renderComponent({
			state: {selectedViewportSize: 'tablet'},
		});
		const input = getByLabelText('margin-top');

		await fireEvent.change(input, {
			target: {value: '1'},
		});

		expect(updateItemConfig).toHaveBeenCalledWith({
			itemConfig: {
				tablet: {
					styles: {
						marginTop: '1',
					},
				},
			},
			itemId: '0',
			segmentsExperienceId: '0',
		});
	});
});
