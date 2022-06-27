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
import {act, render, screen} from '@testing-library/react';
import {openSelectionModal} from 'frontend-js-web';
import * as React from 'react';

import GlobalCSSCETsConfiguration from '../../../../src/main/resources/META-INF/resources/js/layout/look_and_feel/GlobalCSSCETsConfiguration';

jest.mock('frontend-js-web', () => ({
	openSelectionModal: jest.fn(),
}));

describe('GlobalCSSCETsConfiguration', () => {
	afterEach(() => {
		openSelectionModal.mockReset();
	});

	it('shows "no extensions loaded" if there are no extensions', async () => {
		render(
			<GlobalCSSCETsConfiguration
				globalCSSCETSelectorURL=""
				globalCSSCETs={[]}
				portletNamespace=""
				selectGlobalCSSCETsEventName=""
			/>
		);

		await screen.findByText('no-css-extensions-were-loaded');
	});

	it('renders the given list of global extensions', async () => {
		render(
			<GlobalCSSCETsConfiguration
				globalCSSCETSelectorURL=""
				globalCSSCETs={[
					{
						cetExternalReferenceCode: 'niceId',
						name: 'Nice Global CSS',
					},
				]}
				portletNamespace=""
				selectGlobalCSSCETsEventName=""
			/>
		);

		await screen.findByText('Nice Global CSS');
	});

	it('renders a hidden input with the list of selected extensions', async () => {
		render(
			<GlobalCSSCETsConfiguration
				globalCSSCETSelectorURL=""
				globalCSSCETs={[
					{
						cetExternalReferenceCode: 'niceId',
						name: 'Nice Global CSS',
					},
					{
						cetExternalReferenceCode: 'anotherNiceId',
						name: 'Nice Global CSS v2',
					},
				]}
				portletNamespace=""
				selectGlobalCSSCETsEventName=""
			/>
		);

		await screen.findByDisplayValue('niceId,anotherNiceId');
	});

	it('opens a selection modal when "add" button is pressed', async () => {
		render(
			<GlobalCSSCETsConfiguration
				globalCSSCETSelectorURL=""
				globalCSSCETs={[
					{
						cetExternalReferenceCode: 'niceId',
						name: 'Nice Global CSS',
					},
				]}
				portletNamespace=""
				selectGlobalCSSCETsEventName=""
			/>
		);

		const addButton = await screen.findByText('add-css-extensions');

		expect(addButton).toBeInstanceOf(HTMLButtonElement);
		(addButton as HTMLButtonElement).click();

		expect(openSelectionModal).toHaveBeenCalled();
	});

	it('removes duplicated extensions if any', async () => {
		openSelectionModal.mockImplementation(() => {});

		render(
			<GlobalCSSCETsConfiguration
				globalCSSCETSelectorURL=""
				globalCSSCETs={[
					{
						cetExternalReferenceCode: 'niceId',
						name: 'Nice Global CSS',
					},
				]}
				portletNamespace=""
				selectGlobalCSSCETsEventName=""
			/>
		);

		const addButton = await screen.findByText('add-css-extensions');

		expect(addButton).toBeInstanceOf(HTMLButtonElement);
		(addButton as HTMLButtonElement).click();

		expect(openSelectionModal).toHaveBeenCalledTimes(1);

		expect(openSelectionModal).toHaveBeenCalledWith(
			expect.objectContaining({
				onSelect: expect.any(Function),
			})
		);

		const [[{onSelect}]] = openSelectionModal.mock.calls;

		act(() => {
			onSelect({
				value: [
					JSON.stringify({
						cetExternalReferenceCode: 'niceId',
						name: 'Nice Global CSS',
					}),
					JSON.stringify({
						cetExternalReferenceCode: 'someNiceId',
						name: 'Some Nice Global CSS',
					}),
				],
			});
		});

		await screen.findByDisplayValue('niceId,someNiceId');
	});

	it('allows removing extensions by pressing dropdown "remove" button', async () => {
		render(
			<GlobalCSSCETsConfiguration
				globalCSSCETSelectorURL=""
				globalCSSCETs={[
					{
						cetExternalReferenceCode: 'niceId',
						name: 'Nice Global CSS',
					},
				]}
				portletNamespace=""
				selectGlobalCSSCETsEventName=""
			/>
		);

		const optionsButton = await screen.findByLabelText('show-options');

		expect(optionsButton).toBeInstanceOf(HTMLButtonElement);
		(optionsButton as HTMLButtonElement).click();

		await screen.findByRole('menu');

		await act(async () => {
			const deleteButton = await screen.findByText('delete');

			expect(deleteButton).toBeInstanceOf(HTMLButtonElement);
			(deleteButton as HTMLButtonElement).click();
		});

		await screen.findByText('no-css-extensions-were-loaded');
	});
});
