/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom/extend-expect';
import {fireEvent, render} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {navigate} from 'frontend-js-web';
import React from 'react';

import {ImportFragments} from '../../../src/main/resources/META-INF/resources/js';

jest.mock('frontend-js-web', () => ({
	...jest.requireActual('frontend-js-web'),
	fetch: () => Promise.resolve({json: () => ({valid: false})}),
	navigate: jest.fn(),
}));

function renderComponent({
	backURL = 'backURL',
	helpLink,
	importURL = 'importURL',
	portletNamespace = 'namespace',
}) {
	return render(
		<ImportFragments
			backURL={backURL}
			helpLink={helpLink}
			importURL={importURL}
			portletNamespace={portletNamespace}
		/>
	);
}

describe('ImportFragments', () => {
	beforeAll(() => {
		jest.useFakeTimers();
	});

	afterEach(() => {
		jest.clearAllMocks();
	});

	it('renders text informing the user should upload a ZIP file', async () => {
		const {findByText} = renderComponent({});

		expect(
			await findByText(
				'select-a-zip-file-containing-one-or-multiple-entries'
			)
		).toBeInTheDocument();
	});

	it('renders file input', async () => {
		const {findByLabelText} = renderComponent({});

		expect(await findByLabelText('file-upload')).toBeInTheDocument();
	});

	it('renders submit button disabled until file input has a valid value', async () => {
		const {findByLabelText, findByRole} = renderComponent({});

		const button = await findByRole('button', {name: /import/i});
		expect(button.disabled).toBeTruthy();

		const file = new File(['(⌐□_□)'], 'example.zip', {
			type: 'application/zip',
		});

		fireEvent.change(await findByLabelText('file-upload'), {
			target: {files: [file]},
		});

		expect(button.disabled).toBeFalsy();
	});

	it('renders cancel button enabled', async () => {
		const {findByRole} = renderComponent({backURL: 'http://test.com'});

		const button = await findByRole('button', {name: /cancel/i});
		expect(button.disabled).toBeFalsy();

		await userEvent.click(button, {
			advanceTimers: jest.advanceTimersByTime,
		});

		expect(navigate).toHaveBeenCalled();
	});

	it('shows required validation when a file with an invalid extension is introduced', async () => {
		const {findByLabelText, findByRole, findByText} = renderComponent({});

		const button = await findByRole('button', {name: /import/i});

		const file = new File(['(⌐□_□)'], 'example.png', {
			type: 'image/png',
		});

		fireEvent.change(await findByLabelText('file-upload'), {
			target: {files: [file]},
		});

		expect(button.disabled).toBeTruthy();
		expect(
			await findByText('only-zip-files-are-allowed')
		).toBeInTheDocument();
	});

	it('renders help link', async () => {
		const {findByText} = renderComponent({
			helpLink: {href: 'http://example.com', message: 'Learn more'},
		});

		expect(await findByText('Learn more')).toBeInTheDocument();
	});

	it.skip('renders Import Options modal', async () => {
		const {findByLabelText, findByRole, findByText} = renderComponent({});

		const button = await findByRole('button', {name: /import/i});

		const file = new File(['(⌐□_□)'], 'example.zip', {
			type: 'image/png',
		});

		fireEvent.change(await findByLabelText('file-upload'), {
			target: {files: [file]},
		});

		expect(button.disabled).toBeFalsy();

		expect(await findByText('import-options')).toBeInTheDocument();
	});
});
