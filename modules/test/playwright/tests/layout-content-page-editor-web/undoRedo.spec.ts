/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {applicationsMenuPageTest} from '../../fixtures/applicationsMenuPageTest';
import {loginTest} from '../../fixtures/loginTest';
import {pageEditorPagesTest} from '../../fixtures/pageEditorPages';
import getRandomId from '../../utils/getRandomId';
import getFragmentDefinition from './utils/getFragmentDefinition';
import getPageDefinition from './utils/getPageDefinition';

export const test = mergeTests(
	apiHelpersTest,
	applicationsMenuPageTest,
	loginTest,
	pageEditorPagesTest
);

test('View Undo interaction state is cleared after refreshing the page', async ({
	apiHelpers,
	page,
	pageEditorPage,
}) => {
	await page.goto('/');

	// Create a site

	const site = await apiHelpers.headlessSite.createSite(getRandomId());

	// Create a page with a Heading fragment

	const headingId = getRandomId();

	const headingFragment = getFragmentDefinition(
		headingId,
		'BASIC_COMPONENT-heading'
	);

	const layout = await apiHelpers.headlessDelivery.createSitePage(
		site.id,
		getRandomId(),
		getPageDefinition([headingFragment])
	);

	// Go to edit mode of page

	await pageEditorPage.goToEditMode(site, layout);

	// assert undo button is visible

	expect(page.getByTitle('Undo')).toBeVisible();

	// select the fragment

	await pageEditorPage.selectFragment(headingId);

	// Go to Styles panel and set text to Align Center

	await pageEditorPage.goToConfigurationTab('Styles');
	await page.getByLabel('Align Center').click();

	// assert undo button is enabled

	await expect(page.getByTitle('Undo')).toBeEnabled();

	// refresh the page

	await page.reload({waitUntil: 'domcontentloaded'});

	// assert Undo button is disabled

	await expect(page.getByTitle('Undo')).toBeDisabled();

	// Delete the site

	await apiHelpers.headlessSite.deleteSite(site.id);
});

test('Undo and Redo buttons work as expected', async ({
	apiHelpers,
	page,
	pageEditorPage,
}) => {
	await page.goto('/');

	// Create a site

	const site = await apiHelpers.headlessSite.createSite(getRandomId());

	// Create a page with a Heading fragment

	const tabsId = getRandomId();

	const fragmentDefinition = getFragmentDefinition(
		tabsId,
		'BASIC_COMPONENT-tabs'
	);

	const layout = await apiHelpers.headlessDelivery.createSitePage(
		site.id,
		getRandomId(),
		getPageDefinition([fragmentDefinition])
	);

	// Go to edit mode of page

	await pageEditorPage.goToEditMode(site, layout);

	// assert undo button is visible

	const undoButton = page.getByTitle('Undo');
	const redoButton = page.getByTitle('Redo');
	const tabsFragment = page.locator(`.lfr-layout-structure-item-${tabsId}`);

	expect(undoButton).toBeVisible();
	expect(redoButton).toBeVisible();

	// select the fragment

	await tabsFragment.click();

	// Go to Styles panel and set text to Align Center

	await pageEditorPage.goToConfigurationTab('General');
	await page.getByLabel('Number of Tabs', {exact: true}).fill('5');
	await tabsFragment.click();

	await expect(tabsFragment.getByText('Tab 5')).toBeVisible();

	// delete tabs fragment

	await page.keyboard.press('Backspace');

	await expect(tabsFragment).not.toBeAttached();

	// assert undo button is enabled redo button is disabled

	await expect(undoButton).toBeEnabled();
	await expect(redoButton).toBeDisabled();

	await undoButton.click();

	// assert tabsfragment its present and configuration is not lost

	await expect(tabsFragment).toBeAttached();
	await expect(tabsFragment.getByText('Tab 5')).toBeVisible();

	// assert Undo button is disabled and Redo button is enabled

	await undoButton.click();

	await expect(undoButton).toBeDisabled();
	await expect(redoButton).toBeEnabled();

	// Delete the site

	await apiHelpers.headlessSite.deleteSite(site.id);
});
