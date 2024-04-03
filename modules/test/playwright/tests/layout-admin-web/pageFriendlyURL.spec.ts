/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {featureFlagsTest} from '../../fixtures/featureFlagsTest';
import {loginTest} from '../../fixtures/loginTest';
import getRandomString from '../../utils/getRandomString';
import getFragmentDefinition from '../layout-content-page-editor-web/utils/getFragmentDefinition';
import getPageDefinition from '../layout-content-page-editor-web/utils/getPageDefinition';

export const test = mergeTests(
	apiHelpersTest,
	featureFlagsTest({
		'LPS-178052': true,
	}),
	loginTest()
);

test('This is a test for LPD-21554. Some page names result in 404 friendly URLs.', async ({
	apiHelpers,
	page,
}) => {
	const company = await apiHelpers.jsonWebServicesCompany.getCompanyByWebId(
		'liferay.com'
	);

	const group = await apiHelpers.jsonWebServicesGroup.getGroup(
		company.companyId,
		'Guest'
	);

	// Create a page in Guest site with name matching a supported locale which
	// is not an available locale for the site

	const pageName = 'th';

	const sitePage = await apiHelpers.headlessDelivery.createSitePage({
		pageDefinition: getPageDefinition([
			getFragmentDefinition(getRandomString(), 'BASIC_COMPONENT-heading'),
		]),
		siteId: group.groupId,
		title: pageName,
	});

	// Check that page friendlyURL does not contain the page name (which
	// should have been replaced by layoutId)

	await expect.soft(sitePage.friendlyUrlPath).not.toContain(`${pageName}`);

	await page.goto(sitePage.friendlyUrlPath);

	await expect.soft(page.getByText('Heading Example')).toBeVisible();

	await apiHelpers.jsonWebServicesLayout.deleteLayout(sitePage.id);
});
