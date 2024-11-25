/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {displayPageTemplatesPagesTest} from '../../fixtures/displayPageTemplatesPagesTest';
import {featureFlagsTest} from '../../fixtures/featureFlagsTest';
import {loginTest} from '../../fixtures/loginTest';
import {objectPagesTest} from '../../fixtures/objectPagesTest';
import {pageEditorPagesTest} from '../../fixtures/pageEditorPagesTest';
import {pageManagementSiteTest} from '../../fixtures/pageManagementSiteTest';
import {clickAndExpectToBeHidden} from '../../utils/clickAndExpectToBeHidden';
import fillAndClickOutside from '../../utils/fillAndClickOutside';
import getRandomString from '../../utils/getRandomString';
import {waitForAlert} from '../../utils/waitForAlert';
import {goToObjectEntity} from '../setup/page-management-site/utils/goToObjectEntity';
import getFormContainerDefinition from './utils/getFormContainerDefinition';
import getFragmentDefinition from './utils/getFragmentDefinition';
import getPageDefinition from './utils/getPageDefinition';

const test = mergeTests(
	apiHelpersTest,
	displayPageTemplatesPagesTest,
	featureFlagsTest({
		'LPD-10727': true,
		'LPS-178052': true,
	}),
	loginTest(),
	objectPagesTest,
	pageEditorPagesTest,
	pageManagementSiteTest
);

test.describe('Submit button', () => {
	test(
		"Cannot save a value as draft in the object when 'Allow Users to Save Entries as Draft' option is not enabled",
		{tag: '@LPS-191474'},
		async ({apiHelpers, page, pageEditorPage, pageManagementSite}) => {

			// Create a Content page with a form

			const formId = getRandomString();

			const formDefinition = getFormContainerDefinition({
				id: formId,
			});

			const layout = await apiHelpers.headlessDelivery.createSitePage({
				pageDefinition: getPageDefinition([formDefinition]),
				siteId: pageManagementSite.id,
				title: getRandomString(),
			});

			// Go to edit mode and map the form to Lemon Weight field

			await pageEditorPage.goto(
				layout,
				pageManagementSite.friendlyUrlPath
			);

			await pageEditorPage.mapFormFragment(formId, 'Lemon', [
				'Lemon Weight',
			]);

			// Change the "Submitted Entry Status" configuration to Draft

			await pageEditorPage.changeFragmentConfiguration({
				fieldLabel: 'Submitted Entry Status',
				fragmentId: await pageEditorPage.getFragmentId('Form Button'),
				tab: 'General',
				value: 'Draft',
			});

			// Publish with a draft submit button

			await page.getByLabel('Publish', {exact: true}).click();

			await expect(
				page.getByText(
					'form does not allow creating entries as draft. Review the button configuration and set it to approved to generate valid entries.'
				)
			).toBeVisible();

			await page
				.locator('.modal')
				.getByText('Publish', {exact: true})
				.click();

			await waitForAlert(
				page,
				'Success:The page was published successfully.'
			);

			// Go to view mode and check that the value cannot be saved

			await page.goto(
				`/web${pageManagementSite.friendlyUrlPath}${layout.friendlyUrlPath}`
			);

			await page.getByLabel('Lemon Weight').fill('200');

			await page.getByText('Submit', {exact: true}).click();

			await expect(
				page.getByText(
					'An error occurred while sending the form information.'
				)
			).toBeVisible();
		}
	);

	test.skip(
		'It is not possible to change an object from approved status to draft status',
		{tag: '@LPS-191474'},
		async ({
			apiHelpers,
			displayPageTemplatesPage,
			objectDetailsPage,
			page,
			pageEditorPage,
			pageManagementSite,
		}) => {
			const checkObjectEntryStatus = async (
				value: string,
				status: string
			) => {

				// Go to entity

				await goToObjectEntity({
					entityName: 'Lemon',
					page,
				});

				// Check the status of the object entry

				const row = page.locator('.dnd-tr').filter({hasText: value});

				await expect(row).toContainText(status);
			};

			await test.step('Set the "Allow Users to Save Entries as Draft" configuration of the Lemon object to true', async () => {
				await objectDetailsPage.goto('Lemon');

				await objectDetailsPage.updateConfiguration({
					fieldLabel: 'Allow Users to Save Entries as Draft',
					value: true,
				});
			});

			const displayPageTemplateName = getRandomString();

			await test.step('Create a Display Page Template with a Form container mapped to Lemon object and two buttons, one to save as Draft and other to save as Approved', async () => {

				// Create a Display page for the Lemon object

				await displayPageTemplatesPage.goto(
					pageManagementSite.friendlyUrlPath
				);

				await displayPageTemplatesPage.createTemplate({
					contentType: 'Lemon',
					name: displayPageTemplateName,
				});

				await displayPageTemplatesPage.editTemplate(
					displayPageTemplateName
				);

				// Add a Form Container and map it to Lemon Weight field

				await pageEditorPage.addFragment(
					'Form Components',
					'Form Container'
				);

				const fragmentId =
					await pageEditorPage.getFragmentId('Form Container');

				await pageEditorPage.mapFormFragment(
					fragmentId,
					'Lemon (Default)',
					['Lemon Weight']
				);

				// Add another submit button with the "Submitted Entry Status" configuration as Draft

				const dptSubmitButtonId =
					await pageEditorPage.getFragmentId('Form Button');

				await pageEditorPage.clickFragmentOption(
					dptSubmitButtonId,
					'Duplicate'
				);

				await pageEditorPage.editTextEditable(
					dptSubmitButtonId,
					'submit-button-text',
					'Submit as draft'
				);

				await pageEditorPage.changeFragmentConfiguration({
					fieldLabel: 'Submitted Entry Status',
					fragmentId: dptSubmitButtonId,
					tab: 'General',
					value: 'Draft',
				});

				await displayPageTemplatesPage.publishTemplate();
			});

			const headingId = getRandomString();
			const formId = getRandomString();
			let layout = null;

			await test.step('Create a Content page with a Form fragment mapped to Lemon object with a draft Submit Button and a Heading fragment', async () => {

				// Create a Content page

				const formDefinition = getFormContainerDefinition({
					id: formId,
				});

				const headingDefinition = getFragmentDefinition({
					id: headingId,
					key: 'BASIC_COMPONENT-heading',
				});

				layout = await apiHelpers.headlessDelivery.createSitePage({
					pageDefinition: getPageDefinition([
						formDefinition,
						headingDefinition,
					]),
					siteId: pageManagementSite.id,
					title: getRandomString(),
				});

				// Go to edit mode

				await pageEditorPage.goto(
					layout,
					pageManagementSite.friendlyUrlPath
				);

				// Map the form to Lemon Weight field

				await pageEditorPage.mapFormFragment(formId, 'Lemon', [
					'Lemon Weight',
				]);

				// Change the "Submitted Entry Status" configuration to Draft

				const submitButtonId =
					await pageEditorPage.getFragmentId('Form Button');

				await pageEditorPage.editTextEditable(
					submitButtonId,
					'submit-button-text',
					'Submit as draft'
				);

				await pageEditorPage.changeFragmentConfiguration({
					fieldLabel: 'Submitted Entry Status',
					fragmentId: submitButtonId,
					tab: 'General',
					value: 'Draft',
				});

				await pageEditorPage.publishPage();
			});

			const input = page.getByLabel('Lemon Weight');
			const submitDraftButton = page.getByText('Submit as draft', {
				exact: true,
			});

			await test.step('Go to view mode and save the Lemon Weight field value as draft', async () => {
				await page.goto(
					`/web${pageManagementSite.friendlyUrlPath}${layout.friendlyUrlPath}`
				);

				await fillAndClickOutside(page, input, '100');

				await submitDraftButton.click();

				await page
					.getByText(
						'Thank you. Your information was successfully received.'
					)
					.waitFor();

				// Check the saved value

				await checkObjectEntryStatus('100', 'Draft');
			});

			await test.step('Go to edit mode and map the Heading to the draft entry number and select the DPT created before as Field', async () => {
				await pageEditorPage.goto(
					layout,
					pageManagementSite.friendlyUrlPath
				);

				await pageEditorPage.changeEditableConfiguration({
					editableId: 'element-text',
					fieldLabel: 'Link',
					fragmentId: headingId,
					tab: 'Link',
					value: 'Mapped URL',
				});

				await pageEditorPage.openMappingSelector();

				const iframe = page.frameLocator('iframe[title="Select"]');

				await iframe.getByPlaceholder('Search').waitFor();

				await iframe.getByText('Lemons', {exact: true}).click();

				await clickAndExpectToBeHidden({
					target: iframe.locator('.lfr-item-viewer'),
					trigger: iframe
						.locator('.item-selector-list-row .entry')
						.first(),
				});

				await pageEditorPage.changeConfiguration({
					fieldLabel: 'Field',
					tab: 'Link',
					value: displayPageTemplateName,
				});

				await pageEditorPage.publishPage();
			});

			const headingFragment = page.getByText('Heading Example', {
				exact: true,
			});

			await test.step('Go to view mode, click in the Heading and save the field value as Draft', async () => {
				await page.goto(
					`/web${pageManagementSite.friendlyUrlPath}${layout.friendlyUrlPath}`
				);

				await headingFragment.click();

				await expect(headingFragment).not.toBeAttached();

				// Set new value and submit as draft

				await fillAndClickOutside(page, input, '200');

				await submitDraftButton.click();

				await page
					.getByText(
						'Thank you. Your information was successfully received.'
					)
					.waitFor();

				// Check the saved value

				await checkObjectEntryStatus('200', 'Draft');
			});

			await test.step('Go to view mode, click in the Heading and save the field value as Approved', async () => {
				await page.goto(
					`/web${pageManagementSite.friendlyUrlPath}${layout.friendlyUrlPath}`
				);

				await headingFragment.click();

				await expect(headingFragment).not.toBeAttached();

				// Set new value and submit as approved

				await fillAndClickOutside(page, input, '300');

				await page.getByText('Submit', {exact: true}).click();

				await page
					.getByText(
						'Thank you. Your information was successfully received.'
					)
					.waitFor();

				// Check the saved value

				await checkObjectEntryStatus('300', 'Approved');
			});

			await test.step('Go to view mode, click in the Heading and try to save the field value as Draft again', async () => {
				await page.goto(
					`/web${pageManagementSite.friendlyUrlPath}${layout.friendlyUrlPath}`
				);

				await headingFragment.click();

				await expect(headingFragment).not.toBeAttached();

				// Set new value and submit as draft

				await fillAndClickOutside(page, input, '400');

				await submitDraftButton.click();

				await expect(
					page.getByText(
						'An error occurred while sending the form information.'
					)
				).toBeVisible();

				// Check the saved value

				await checkObjectEntryStatus('300', 'Approved');
			});
		}
	);
});
