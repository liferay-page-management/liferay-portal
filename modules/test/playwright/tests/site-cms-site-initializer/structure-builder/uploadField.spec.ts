/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {dataApiHelpersTest} from '../../../fixtures/dataApiHelpersTest';
import {loginTest} from '../../../fixtures/loginTest';
import {getRandomInt} from '../../../utils/getRandomInt';
import getRandomString from '../../../utils/getRandomString';
import chooseFileFromCMSLibrary from '../../layout-content-page-editor-web/main/utils/chooseFileFromCMSLibrary';
import {cmsPagesTest} from '../main/fixtures/cmsPagesTest';
import {structureBuilderPagesTest} from './fixtures/structureBuilderPagesTest';

const test = mergeTests(
	cmsPagesTest,
	dataApiHelpersTest,
	loginTest(),
	structureBuilderPagesTest
);

test(
	'Every repeat of an upload field opens the same CMS item selector',
	{tag: '@LPD-96714'},
	async ({apiHelpers, contentsPage, page, structureBuilderPage}) => {
		const fileName = `file_${getRandomString()}.png`;
		const structureLabel = `StructureName${getRandomInt()}`;

		// Seed a file in the Default space so it can be found in the selector

		const objectEntry = await apiHelpers.objectEntry.postObjectEntry(
			{
				file: {
					fileBase64: 'R0lGODlhAQABAAAAACw=',
					name: fileName,
				},
				objectEntryFolderExternalReferenceCode: 'L_FILES',
				title: fileName,
			},
			'cms/basic-documents',
			'Default'
		);

		apiHelpers.data.push({
			applicationName: 'cms/basic-documents',
			id: objectEntry.id,
			type: 'objectEntry',
		});

		// Create a structure whose upload field selects from the item selector
		// and is a repeatable group

		await structureBuilderPage.createStructureFromData({
			label: structureLabel,
			page: structureBuilderPage,
			publish: false,
		});

		await structureBuilderPage.addField('Upload');

		await structureBuilderPage.changeFieldSettings({
			label: 'Upload from DM',
			name: 'uploadFromDM',
			requestFile: 'document-library',
		});

		await structureBuilderPage.createRepeatableGroup({
			fields: [{label: 'Upload from DM'}],
			label: 'Repeatable Group',
		});

		await structureBuilderPage.publishStructure();

		// Create a content for the structure in the Default space

		await contentsPage.goto();

		await contentsPage.createContent(structureLabel);

		const uploadFragments = page.locator('.file-upload');

		// The first repeat selects the seeded file through the CMS item selector

		await chooseFileFromCMSLibrary({
			fileName,
			page,
			trigger: uploadFragments.nth(0).getByText('Select File', {
				exact: true,
			}),
		});

		// The second repeat opens the same CMS item selector and finds the same
		// file instead of a different, empty selector

		await page.getByText('Add New', {exact: true}).first().click();

		await chooseFileFromCMSLibrary({
			fileName,
			page,
			trigger: uploadFragments.nth(1).getByText('Select File', {
				exact: true,
			}),
		});

		// Deleting the auto-tracked structure also removes this draft content

	}
);

test(
	'An upload field with a file size error blocks publishing but not saving as draft',
	{tag: '@LPD-101986'},
	async ({contentsPage, page, structureBuilderPage}) => {
		const maximumFileSize = 1;
		const structureLabel = `StructureName${getRandomInt()}`;

		await structureBuilderPage.createStructureFromData({
			label: structureLabel,
			page: structureBuilderPage,
			publish: false,
		});

		await structureBuilderPage.addField('Upload');

		await structureBuilderPage.changeFieldSettings({
			label: 'Attachment',
			maximumFileSize,
			name: 'attachment',
			requestFile: 'computer',
		});

		await structureBuilderPage.publishStructure();

		await contentsPage.goto();

		await contentsPage.createContent(structureLabel);

		await page
			.getByRole('textbox', {exact: true, name: 'Title'})
			.fill(`Title ${getRandomString()}`);

		const uploadFragment = page.locator('.file-upload');

		const fileInput = uploadFragment.locator('.file-upload-input');
		const formGroup = uploadFragment.locator('.form-group');
		const fileSizeError = uploadFragment.getByText(
			`Please enter a file with a valid file size no larger than ${maximumFileSize} MB.`
		);
		const largeFileName = `large_${getRandomString()}.png`;
		const selectFileButton = uploadFragment.getByRole('button', {
			exact: true,
			name: 'Select File',
		});

		const setLargeFile = async () => {
			await fileInput.setInputFiles({
				buffer: Buffer.alloc((maximumFileSize + 1) * 1024 * 1024),
				mimeType: 'image/png',
				name: largeFileName,
			});

			await expect(fileSizeError).toBeVisible();
			await expect(uploadFragment.getByText(largeFileName)).toBeVisible();
		};

		await test.step('Publish is blocked and the upload field is focused', async () => {
			await setLargeFile();

			await contentsPage.publishButton.click();

			await expect(selectFileButton).toBeFocused();
			await expect(fileSizeError).toBeVisible();
		});

		await test.step('Schedule publication is blocked and the upload field is focused', async () => {
			await contentsPage.openSchedulePublication();

			await page
				.getByRole('textbox', {name: 'Date and Time'})
				.fill(`10/31/${new Date().getFullYear() + 1} 01:00 PM`);

			await page.keyboard.press('Tab');

			await page.getByRole('button', {name: 'Schedule'}).click();

			await expect(selectFileButton).toBeFocused();
		});

		await test.step('Removing the rejected file clears the error', async () => {
			await uploadFragment.getByTitle('Remove Item').click();

			await expect(formGroup).not.toHaveClass(/has-error/);
			await expect(uploadFragment.getByText(largeFileName)).toBeHidden();
		});

		await test.step('Saving as draft is allowed', async () => {
			await setLargeFile();

			await contentsPage.saveContentAsDraft();

			await expect(uploadFragment.getByText(largeFileName)).toBeHidden();
		});

		await test.step('Uploading a valid file clears the error and publishing is allowed', async () => {
			await setLargeFile();

			const validFileName = `valid_${getRandomString()}.png`;

			await fileInput.setInputFiles({
				buffer: Buffer.alloc(1024),
				mimeType: 'image/png',
				name: validFileName,
			});

			await expect(uploadFragment.getByText(validFileName)).toBeVisible();

			await contentsPage.saveContent();
		});
	}
);
