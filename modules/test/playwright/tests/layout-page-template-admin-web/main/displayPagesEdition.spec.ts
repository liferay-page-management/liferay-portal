/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	ObjectDefinitionAPI,
} from '@liferay/object-admin-rest-client-js';
import {expect, mergeTests} from '@playwright/test';

import {applicationsMenuPageTest} from '../../../fixtures/applicationsMenuPageTest';
import {dataApiHelpersTest} from '../../../fixtures/dataApiHelpersTest';
import {displayPageTemplatesPagesTest} from '../../../fixtures/displayPageTemplatesPagesTest';
import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {isolatedSiteTest} from '../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../fixtures/loginTest';
import {pageEditorPagesTest} from '../../../fixtures/pageEditorPagesTest';
import {pageManagementSiteTest} from '../../../fixtures/pageManagementSiteTest';
import getRandomString from '../../../utils/getRandomString';
import {pagesPagesTest} from '../../layout-admin-web/main/fixtures/pagesPagesTest';
import {getObjectERC} from '../../setup/page-management-site/main/utils/getObjectERC';
import {goToObjectEntity} from '../../setup/page-management-site/main/utils/goToObjectEntity';

const test = mergeTests(
	applicationsMenuPageTest,
	dataApiHelpersTest,
	displayPageTemplatesPagesTest,
	featureFlagsTest({
		'LPD-60546': {enabled: true},
	}),
	isolatedSiteTest,
	loginTest(),
	pageEditorPagesTest,
	pageManagementSiteTest,
	pagesPagesTest
);

test.describe('Object Display page', () => {
	test(
		'Can edit one field from an object in a display page',
		{
			tag: '@LPS-191389',
		},
		async ({
			apiHelpers,
			displayPageTemplatesPage,
			page,
			pageEditorPage,
			pageManagementSite,
		}) => {

			// Create a default display page for lemon object

			const objectDefinitionAPIClient =
				await apiHelpers.buildRestClient(ObjectDefinitionAPI);

			const {className: objectDefinitionClassName} = (
				await objectDefinitionAPIClient.getObjectDefinitionByExternalReferenceCode(
					getObjectERC('Lemon')
				)
			).body;

			const className =
				await apiHelpers.jsonWebServicesClassName.fetchClassName(
					objectDefinitionClassName
				);

			const displayPageTemplateName = getRandomString();

			await apiHelpers.jsonWebServicesLayoutPageTemplateEntry.addDisplayPageLayoutPageTemplateEntry(
				{
					classNameId: className.classNameId,
					groupId: pageManagementSite.id,
					name: displayPageTemplateName,
				}
			);

			// Edit display page template and add a form container when only one field

			displayPageTemplatesPage.goto(pageManagementSite.friendlyUrlPath);

			displayPageTemplatesPage.editTemplate(displayPageTemplateName);

			await pageEditorPage.addFragment(
				'Form Components',
				'Form Container'
			);

			await pageEditorPage.mapFormFragment(
				await pageEditorPage.getFragmentId('Form Container'),
				'Lemon (Default)',
				['Lemon Size']
			);

			await displayPageTemplatesPage.publishTemplate();

			// Create a lemon object entry

			const lemonObjectEntry =
				await apiHelpers.objectEntry.postObjectEntry(
					{
						lemonHistory: 'one',
						lemonSize: 'lemonSize',
						lemonWeight: 5,
					},
					'c/lemons',
					pageManagementSite.key
				);

			// Go to edit mode and edit only the lemon size field

			await expect(async () => {
				await page.goto(
					`/web${pageManagementSite.friendlyUrlPath}/e/${displayPageTemplateName}/${className.classNameId}/${lemonObjectEntry.id}`
				);

				await page
					.getByRole('textbox', {name: 'Lemon Size'})
					.waitFor({timeout: 2000});

				await page
					.getByRole('textbox', {name: 'Lemon Size'})
					.fill('lemonSize2', {timeout: 2000});
			}).toPass();

			await page.getByRole('button', {name: 'Submit'}).click();

			// Go to admin and check that only lemon size was updated

			goToObjectEntity({
				entityName: 'Lemon',
				page,
			});

			const row = page.locator('.fds tbody tr').first();

			await expect(row).toContainText('one');
			await expect(row).toContainText('lemonSize2');
			await expect(row).toContainText('5');
		}
	);
});
