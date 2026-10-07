/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';

import {getTranslationInput} from '../src/main/resources/META-INF/resources/js/api/getTranslationInput';

const TRANSLATION_INPUT_OPTIONS = {
	inputId: 'file-upload',
	inputName: 'ObjectField_upload',
	languageId: 'en_US',
	namespace: 'fragment-1',
};

function setUpLocalizationInputsContainer() {
	document.body.innerHTML = '<div id="container"></div>';

	return document.getElementById('container')!;
}

describe('getTranslationInput', () => {
	it('hides a new file input', () => {
		const translationInput = getTranslationInput({
			...TRANSLATION_INPUT_OPTIONS,
			localizationInputsContainer: setUpLocalizationInputsContainer(),
			type: 'file',
		});

		expect(translationInput).toHaveAttribute('type', 'file');
		expect(translationInput).toHaveClass('d-none');
	});

	it('hides a hidden input that becomes a file input', () => {
		const localizationInputsContainer = setUpLocalizationInputsContainer();

		const hiddenTranslationInput = getTranslationInput({
			...TRANSLATION_INPUT_OPTIONS,
			localizationInputsContainer,
		});

		const translationInput = getTranslationInput({
			...TRANSLATION_INPUT_OPTIONS,
			localizationInputsContainer,
			type: 'file',
		});

		expect(translationInput).toBe(hiddenTranslationInput);
		expect(translationInput).toHaveAttribute('type', 'file');
		expect(translationInput).toHaveClass('d-none');
	});
});
