/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {dataApiHelpersTest} from '../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {isolatedSiteTest} from '../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../fixtures/loginTest';
import {getRandomInt} from '../../../utils/getRandomInt';
import getRandomString from '../../../utils/getRandomString';
import {
	GEOCODED_ADDRESS,
	GEOCODED_COORDINATES,
	interceptMapRequests,
} from '../../object-web/utils/location';
import getFragmentDefinition from '../main/utils/getFragmentDefinition';
import getPageDefinition from '../main/utils/getPageDefinition';

const test = mergeTests(
	dataApiHelpersTest,
	featureFlagsTest({
		'LPD-70668': {enabled: true},
	}),
	isolatedSiteTest,
	loginTest()
);

test(
	'Shows the map and the address of the selected entry',
	{tag: '@LPD-78829'},
	async ({apiHelpers, page, site}) => {
		await interceptMapRequests(page);

		// Create an object with a location field and an entry

		const objectDefinition =
			await apiHelpers.objectAdmin.postRandomObjectDefinition({
				objectDefinitionExternalReferenceCode: `Office${getRandomInt()}`,
				objectFields: [
					{
						DBType: 'Clob',
						businessType: 'Location',
						externalReferenceCode: getRandomString(),
						label: {en_US: 'Office Location'},
						name: 'location',
					},
				],
				status: {code: 0},
			});

		apiHelpers.data.push({
			id: objectDefinition.id,
			type: 'objectDefinition',
		});

		const objectEntry = await apiHelpers.objectEntry.postObjectEntry(
			{
				location: {
					address: GEOCODED_ADDRESS,
					coordinates: GEOCODED_COORDINATES,
				},
			},
			objectDefinition.restContextPath.replace(/^\/o\//, '')
		);

		// Create a page with a location map fragment showing the entry

		const layout = await apiHelpers.headlessDelivery.createSitePage({
			pageDefinition: getPageDefinition([
				getFragmentDefinition({
					fragmentConfig: {
						item: {
							className: objectDefinition.className,
							classPK: objectEntry.id,
						},
					},
					id: getRandomString(),
					key: 'BASIC_COMPONENT-location-map',
				}),
			]),
			siteId: site.id,
			title: getRandomString(),
		});

		// Check the map and the address are shown in view mode

		await page.goto(`/web${site.friendlyUrlPath}${layout.friendlyUrlPath}`);

		await expect(
			page.locator('.location-map-container .object-field__location-map')
		).toBeVisible();

		await expect(
			page.getByText(objectEntry.location.address)
		).toBeVisible();
	}
);
