/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ObjectDefinitionApi} from '@liferay/object-admin-rest-client-js';
import {expect, mergeTests} from '@playwright/test';
import {resolve} from 'path';

import {backendPageTest} from '../../../../fixtures/backendPageTest';
import {ApiHelpers} from '../../../../helpers/ApiHelpers';
import {deleteObjectEntries} from '../../../../utils/deleteObjectEntries';
import {performLoginViaApi} from '../../../../utils/performLogin';
import {DEFAULT_ENTRIES_ERCS, OBJECT_ENTITIES} from '../constants/objects';
import {PAGE_MANAGEMENT_SITE_ERC} from '../constants/site';

const base = mergeTests(backendPageTest);

async function createSite(apiHelpers: ApiHelpers): Promise<Site> {
	const site = await apiHelpers.headlessSite.createSiteFromZip(
		{
			externalReferenceCode: PAGE_MANAGEMENT_SITE_ERC,
			name: 'Page Management Site',
		},
		resolve(__dirname, '../site-initializer')
	);

	expect(site).toHaveProperty(
		'externalReferenceCode',
		PAGE_MANAGEMENT_SITE_ERC
	);

	return site;
}

async function deleteSite(apiHelpers: ApiHelpers) {
	const {id: siteId} = await apiHelpers.headlessSite.getSiteByERC(
		PAGE_MANAGEMENT_SITE_ERC
	);

	// Return if site is already deleted

	if (!siteId) {
		return;
	}

	// Delete object definitions

	const ERCs = Object.values(OBJECT_ENTITIES).map((entity) => entity.ERC);

	for (const ERC of ERCs) {
		const objectDefinitionApiClient =
			await apiHelpers.buildRestClient(ObjectDefinitionApi);

		const {id: objectDefinitionId} = (
			await objectDefinitionApiClient.getObjectDefinitionByExternalReferenceCode(
				ERC
			)
		).body;

		if (objectDefinitionId) {
			await objectDefinitionApiClient.deleteObjectDefinition(
				objectDefinitionId
			);
		}
	}

	// Delete site

	await expect(
		await apiHelpers.headlessSite.deleteSiteByERC(PAGE_MANAGEMENT_SITE_ERC)
	).toBeOK();
}

export const setup = base.extend<{}, {pageManagementSiteSetup: {site: Site}}>({
	pageManagementSiteSetup: [
		async ({browser}, use) => {
			const page = await browser.newPage();

			await performLoginViaApi(page, 'test');

			const apiHelpers = new ApiHelpers(page);

			try {
				const site = await createSite(apiHelpers);

				await use({site});
			}
			catch {
				throw new Error('Page Management site could not be created');
			}
			finally {
				await deleteSite(apiHelpers);
			}
		},
		{scope: 'worker'},
	],
});

const pageManagementSiteTest = setup.extend<{
	pageManagementSite: Site;
}>({
	pageManagementSite: [
		async ({backendPage, pageManagementSiteSetup: {site}}, use) => {
			await backendPage.goto('/');

			const apiHelpers = new ApiHelpers(backendPage);

			try {
				await use(site);
			}
			catch {
				throw new Error(
					'An error occurred while using Page Management site'
				);
			}
			finally {

				// Delete all pages after each test

				const {items} = await apiHelpers.headlessDelivery.getSitePages(
					site.id
				);

				if (items) {
					for (const page of items) {
						await apiHelpers.jsonWebServicesLayout.deleteLayout(
							page.id
						);
					}
				}

				// Delete also all existing object entries

				const names = Object.values(OBJECT_ENTITIES).map(
					(entity) => entity.name
				);

				for (const entityName of names) {
					await deleteObjectEntries({
						apiHelpers,
						entityName,
						excludeERC: DEFAULT_ENTRIES_ERCS,
						scopeKey: site.key,
					});
				}
			}
		},
		{auto: true},
	],
});

export {pageManagementSiteTest};
