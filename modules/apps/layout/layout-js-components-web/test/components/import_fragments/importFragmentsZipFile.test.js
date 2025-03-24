/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openToast} from 'frontend-js-components-web';
import {fetch, sub} from 'frontend-js-web';

import importFragmentZipFile from '../../../src/main/resources/META-INF/resources/js/components/import_fragments/importFragmentsZipFile';

jest.mock('frontend-js-components-web');
jest.mock('frontend-js-web');

const mockProps = {
	file: new File(['test'], 'test.zip', {type: 'application/zip'}),
	handleResponse: jest.fn(),
	importURL: '/import',
	portletNamespace: 'portletNamespace',
};

describe('importFragmentZipFile', () => {
	let consoleErrorSpy;

	beforeEach(() => {
		jest.clearAllMocks();
		consoleErrorSpy = jest
			.spyOn(console, 'error')
			.mockImplementation(() => {});
	});

	afterEach(() => {
		consoleErrorSpy.mockRestore();
	});

	it('handle successful import', async () => {
		const mockResponse = {
			json: () =>
				Promise.resolve({importResults: {test: 'result'}, valid: true}),
			ok: true,
		};
		fetch.mockResolvedValue(mockResponse);

		await importFragmentZipFile(mockProps);

		expect(fetch).toHaveBeenCalledWith(
			mockProps.importURL,
			expect.any(Object)
		);
		expect(mockProps.handleResponse).toHaveBeenCalledWith(
			{importResults: {test: 'result'}, valid: true},
			mockProps.file
		);
		expect(openToast).not.toHaveBeenCalled();
	});

	it('handle failed HTTP response', async () => {
		const mockResponse = {
			ok: false,
			status: 404,
		};
		fetch.mockResolvedValue(mockResponse);

		await importFragmentZipFile(mockProps);

		expect(fetch).toHaveBeenCalledWith(
			mockProps.importURL,
			expect.any(Object)
		);
		expect(mockProps.handleResponse).not.toHaveBeenCalled();
		expect(openToast).toHaveBeenCalledWith({
			message: sub(
				Liferay.Language.get(
					'something-went-wrong-and-the-x-could-not-be-imported'
				),
				mockProps.file.name
			),
			type: 'danger',
		});
	});

	it('handle fetch error', async () => {
		fetch.mockRejectedValue(new Error('Network error'));

		await importFragmentZipFile(mockProps);

		expect(fetch).toHaveBeenCalledWith(
			mockProps.importURL,
			expect.any(Object)
		);
		expect(mockProps.handleResponse).not.toHaveBeenCalled();
		expect(openToast).toHaveBeenCalledWith({
			message: sub(
				Liferay.Language.get(
					'something-went-wrong-and-the-x-could-not-be-imported'
				),
				mockProps.file.name
			),
			type: 'danger',
		});
	});

	it('handle null file', async () => {
		await importFragmentZipFile({
			...mockProps,
			file: null,
		});

		expect(fetch).not.toHaveBeenCalled();
		expect(mockProps.handleResponse).not.toHaveBeenCalled();
		expect(openToast).not.toHaveBeenCalled();
		expect(consoleErrorSpy).toHaveBeenCalledWith(
			'importFragmentZipFile: No file provided for import.'
		);
	});

	it('pass overwrite strategy if provided', async () => {
		const mockResponse = {
			json: () =>
				Promise.resolve({importResults: {test: 'result'}, valid: true}),
			ok: true,
		};
		fetch.mockResolvedValue(mockResponse);

		await importFragmentZipFile({
			...mockProps,
			overwriteStrategy: 'overwrite',
		});

		expect(fetch).toHaveBeenCalledWith(
			mockProps.importURL,
			expect.objectContaining({
				body: expect.any(FormData),
			})
		);

		const formData = fetch.mock.calls[0][1].body;
		expect(formData.get(`${mockProps.portletNamespace}importType`)).toBe(
			'overwrite'
		);
	});

	it('handle undefined file name', async () => {
		const nullNameFile = new File(['test'], undefined, {
			type: 'application/zip',
		});
		const mockResponse = {
			ok: false,
			status: 404,
		};
		fetch.mockResolvedValue(mockResponse);

		await importFragmentZipFile({...mockProps, file: nullNameFile});

		expect(openToast).toHaveBeenCalledWith({
			message: sub(
				Liferay.Language.get(
					'something-went-wrong-and-the-x-could-not-be-imported'
				),
				''
			),
			type: 'danger',
		});
	});
});
