/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {featureFlagsTest} from '../../fixtures/featureFlagsTest';
import {isolatedSiteTest} from '../../fixtures/isolatedSiteTest';
import {loginTest} from '../../fixtures/loginTest';
import {pagesAdminPagesTest} from '../../fixtures/pagesAdminPagesTest';
import {clickAndExpectToBeVisible} from '../../utils/clickAndExpectToBeVisible';
import getRandomString from '../../utils/getRandomString';

const test = mergeTests(
	apiHelpersTest,
	featureFlagsTest({
		'LPS-178052': true,
	}),
	isolatedSiteTest,
	loginTest(),
	pagesAdminPagesTest
);

test('changes the permissions of a group of pages', async ({
	apiHelpers,
	page,
	pagesAdminPage,
	site,
}) => {

	// Create two random pages

	const firstName = getRandomString();
	const secondName = getRandomString();

	for (const pageName of [firstName, secondName]) {
		await apiHelpers.headlessDelivery.createSitePage({
			siteId: site.id,
			title: pageName,
		});
	}

	// Go to admin page

	await pagesAdminPage.goto(site.friendlyUrlPath);

	// Select the first page and change the Guest-View permission

	await pagesAdminPage.selectPageAndChangePermissions(
		[firstName],
		['guest_ACTION_VIEW']
	);

	// Select the second page (keeping the first page checked) and open the modal of permissions

	await page
		.getByLabel(`Select ${secondName}`, {
			exact: true,
		})
		.check();

	await page.getByRole('button', {name: 'Permissions'}).click();

	const permissionsFrame = page.frameLocator('iframe[title="Permissions"]');

	await permissionsFrame
		.getByRole('cell', {exact: true, name: 'Role'})
		.waitFor();

	// Check that the Guest-View permission value for both pages is indeterminate

	const permission = await permissionsFrame.locator('#guest_ACTION_VIEW');

	await expect(permission).toHaveValue('indeterminate');

	await page.getByLabel('close', {exact: true}).click();

	// Change the Guest-View permission for both pages

	await pagesAdminPage.selectPageAndChangePermissions(
		[firstName, secondName],
		['guest_ACTION_VIEW']
	);

	// Refresh the admin page

	await pagesAdminPage.goto(site.friendlyUrlPath);

	// Check if the pages are retricted pages

	for (const pageName of [firstName, secondName]) {
		await expect(
			page.getByLabel(`${pageName}. Restricted Page`)
		).toBeVisible();
	}
});

test('checks the correct label for restricted pages in Miller Columns', async ({
	apiHelpers,
	page,
	pagesAdminPage,
	site,
}) => {

	// Create a page with only one permission

	const pageName = getRandomString();

	await apiHelpers.headlessDelivery.createSitePage({
		pagePermissions: [
			{
				actionKeys: ['VIEW'],
				roleKey: 'Owner',
			},
		],
		siteId: site.id,
		title: pageName,
	});

	// Go to admin page and check if the Restricted Page label is in the Miller Columns item

	await pagesAdminPage.goto(site.friendlyUrlPath);

	await expect(
		page
			.locator('.miller-columns-item')
			.getByLabel(`${pageName}. Restricted Page`)
	).toBeVisible();
});

test('Can add and delete a child page.', async ({
	apiHelpers,
	page,
	pagesAdminPage,
	site,
}) => {
	const title = getRandomString();

	await apiHelpers.jsonWebServicesLayout.addLayout({
		groupId: site.id,
		title,
	});

	// Can add a child page

	await pagesAdminPage.goto(site.friendlyUrlPath);

	await clickAndExpectToBeVisible({
		autoClick: true,
		target: page.getByRole('menuitem', {name: 'Add Page'}),
		trigger: page.getByTitle('Add Child Page'),
	});

	await pagesAdminPage.createSelectTemplate('Child Page', 'Blank');

	await pagesAdminPage.goto(site.friendlyUrlPath);

	await page.getByRole('button', {name: title}).click();

	await expect(page.getByRole('link', {name: 'Child Page'})).toBeVisible();

	// Can view the draft label of a page in pages admin

	await expect(
		page.locator('.miller-columns-item').getByText('Draft')
	).toBeVisible();

	// Can view preview draft action

	await clickAndExpectToBeVisible({
		target: page.getByRole('menuitem', {
			exact: true,
			name: 'Child Page',
		}),
		trigger: page
			.locator('li', {has: page.getByText('Preview Draft')})
			.getByRole('button', {name: 'Open Page Options Menu'}),
	});

	// View alert message when delete a page

	await pagesAdminPage.clickOnAction('Delete', 'Child Page');

	await expect(
		page.getByText(
			'Are you sure you want to delete the page "Child Page"? It will be removed immediately.'
		)
	).toBeVisible();

	// Delete page

	await page.getByRole('button', {name: 'Delete'}).click();

	await expect(
		page.getByRole('link', {name: 'Child Page'})
	).not.toBeVisible();
});

test('Can search a child page.', async ({
	apiHelpers,
	page,
	pagesAdminPage,
	site,
}) => {

	const layout = await apiHelpers.jsonWebServicesLayout.addLayout({
		groupId: site.id,
		title: getRandomString(),
	});

	const childLayout = await apiHelpers.jsonWebServicesLayout.addLayout({
		groupId: site.id,
		parentLayoutId: layout.layoutId,
		title: getRandomString(),
	});

	await pagesAdminPage.goto(site.friendlyUrlPath);



	/*
	test NavigateToPageItemViaRelativePathFromSearchResults {
		task ("When the page creator searchs pages in Page admin") {
			Search.searchCP(searchTerm = "Release");
		}

		task ("Then the page creator could navigate to the page item via relative path") {
			Click(
				key_breadcrumbName = "Product Details",
				locator1 = "Breadcrumb#BREADCRUMB_ENTRY_LINK");

			AssertVisible(
				key_itemName = "Product Details",
				locator1 = "PagesAdmin#LIST_GROUP_ITEM");

			Breadcrumb.viewActiveEntry(breadcrumbName = "Product Details");
		}
	}
	 */

	/*
	test SearchPages {
		task ("Search for page one and assert only that page appears") {
			Search.searchCP(searchTerm = "one");

			AssertElementPresent(
				index = 1,
				key_tableEntry = "Page One",
				locator1 = "LexiconTable#TABLE_ENTRY_TITLE_INDEXED");

			for (var pageName : list "Page Two,Page Three") {
				AssertElementNotPresent(
					index = 2,
					key_tableEntry = ${pageName},
					locator1 = "LexiconTable#TABLE_ENTRY_TITLE_INDEXED");
			}
		}

		task ("Search for child page and assert that page appears") {
			Search.searchCP(searchTerm = "three");

			AssertElementPresent(
				index = 1,
				key_tableEntry = "Page Three",
				locator1 = "LexiconTable#TABLE_ENTRY_TITLE_INDEXED");

			for (var pageName : list "Page One,Page Two") {
				AssertElementNotPresent(
					index = 2,
					key_tableEntry = ${pageName},
					locator1 = "LexiconTable#TABLE_ENTRY_TITLE_INDEXED");
			}
		}

		task ("Search for all three pages and order by Create Date") {
			Search.searchCP(searchTerm = "page");

			ManagementBar.setFilterAndOrder(orderBy = "Create Date");

			var i = 1;

			for (var pageName : list "Page One,Page Two,Page Three") {
				AssertElementPresent(
					index = ${i},
					key_tableEntry = ${pageName},
					locator1 = "LexiconTable#TABLE_ENTRY_TITLE_INDEXED");

				var i = ${i} + 1;
			}
		}

		task ("Order by Relevance") {
			ManagementBar.setFilterAndOrder(orderBy = "Relevance");
		}

		task ("View redirect to Pages admin after clear search results") {
			Search.clearSearchResult();

			for (var pageName : list "Page One,Page Two") {
				PagesAdmin.viewPageItemPresent(pageName = ${pageName});
			}
		}
	}
	 */

	/*
	@description = "This is a test for LPS-152949. View widget type pages' name and navigate to the page in search widget."
	@priority = 5
	test SearchWidgetTypePages {
		property portal.acceptance = "true";

		task ("Given a page creator has a widget page") {
			JSONLayout.addPublicLayout(
				groupName = ${randomSiteName},
				layoutName = "Test Widget Page Name");
		}

		task ("When the page creator searches it in Pages admin") {
			PagesAdmin.openPagesAdmin(siteURLKey = ${siteURLKey});

			Search.searchCP(searchTerm = "Widget");
		}

		task ("Then the page creator could see the wiget page in the results and be able to navigate to it") {
			LexiconTable.clickTableEntryTitle(tableEntry = "Test Widget Page Name");

			ControlMenu.viewHeaderTitle(headerTitle = "Test Widget Page Name");
		}

		task ("Given a page creator has a Full Page Application type page") {
			PagesAdmin.openPagesAdmin(siteURLKey = ${siteURLKey});

			PagesAdmin.addPage(
				pageName = "Test Full Page Application Page Name",
				pageTypeURL = "https://www.google.com",
				sitePageType = "Full Page Application");
		}

		task ("When the page creator searches it in Pages admin") {
			PagesAdmin.openPagesAdmin(siteURLKey = ${siteURLKey});

			Search.searchCP(searchTerm = "Full");
		}

		task ("Then the page creator could see the Full Page Application type page in the results and be able to navigate to it") {
			LexiconTable.clickTableEntryTitle(tableEntry = "Test Full Page Application Page Name");

			ControlMenu.viewHeaderTitle(headerTitle = "Test Full Page Application Page Name");
		}

		task ("Given a page creator has a Link to URL type page") {
			PagesAdmin.openPagesAdmin(siteURLKey = ${siteURLKey});

			PagesAdmin.addPage(
				pageName = "Test Link to URL Page Name",
				pageTypeURL = "https://www.google.com",
				sitePageType = "Link to URL");
		}

		task ("When the page creator searches it in Pages admin") {
			PagesAdmin.openPagesAdmin(siteURLKey = ${siteURLKey});

			Search.searchCP(searchTerm = "Link");
		}

		task ("Then the page creator could see the Link to URL type page in the results and be able to navigate to it") {
			LexiconTable.clickTableEntryTitle(tableEntry = "Test Link to URL Page Name");

			AssertLocation.assertPartialLocation(value1 = "https://www.google.com");
		}

		task ("Given a page creator has a Panel type page") {
			JSONLayout.addPublicLayout(
				groupName = ${randomSiteName},
				layoutName = "Test Panel Page Name",
				type = "panel");
		}

		task ("When the page creator searches it in Pages admin") {
			PagesAdmin.openPagesAdmin(siteURLKey = ${siteURLKey});

			Search.searchCP(searchTerm = "Panel");
		}

		task ("Then the page creator could see the Panel type page in the results and be able to navigate to it") {
			LexiconTable.clickTableEntryTitle(tableEntry = "Test Panel Page Name");

			ControlMenu.viewHeaderTitle(headerTitle = "Test Panel Page Name");
		}

		task ("Given a page creator has an embedded type page") {
			PagesAdmin.openPagesAdmin(siteURLKey = ${siteURLKey});

			PagesAdmin.addPage(
				pageName = "Test Embedded Page Name",
				pageTypeURL = "http://www.liferay.com",
				sitePageType = "Embedded");
		}

		task ("When the page creator searches it in Pages admin") {
			PagesAdmin.openPagesAdmin(siteURLKey = ${siteURLKey});

			Search.searchCP(searchTerm = "Embedded");
		}

		task ("Then the page creator could see the embedded type page in the results and be able to navigate to it") {
			LexiconTable.clickTableEntryTitle(tableEntry = "Test Embedded Page Name");

			ControlMenu.viewHeaderTitle(headerTitle = "Test Embedded Page Name");
		}
	}
	 */
});

test('LPS-178476 View the XSS is escaped when store it in widget page name.', async ({
	apiHelpers,
	page,
	pagesAdminPage,
	site,
}) => {

	// Add listener with expect so it fails when a browser dialog is shown

	page.on('dialog', async (dialog) => {
		dialog.accept();

		expect(dialog.message(), 'This alert should not be shown').toBeNull();
	});

	await apiHelpers.jsonWebServicesLayout.addLayout({
		groupId: site.id,
		title: '<script>alert(123);</script>',
	});

	await pagesAdminPage.goto(site.friendlyUrlPath);
});
