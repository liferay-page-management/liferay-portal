/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrameLocator, Page, expect} from '@playwright/test';

import {clickAndExpectToBeHidden} from '../../../../utils/clickAndExpectToBeHidden';
import {clickAndExpectToBeVisible} from '../../../../utils/clickAndExpectToBeVisible';
import getRandomString from '../../../../utils/getRandomString';
import getPageDefinition from '../../../layout-content-page-editor-web/main/utils/getPageDefinition';
import getWidgetDefinition from '../../../layout-content-page-editor-web/main/utils/getWidgetDefinition';

import type {ApiHelpers} from '../../../../helpers/ApiHelpers';
import type {PageEditorPage} from '../../../../pages/layout-content-page-editor-web/PageEditorPage';

export async function createAssetPublisherPage({
	apiHelpers,
	beforeSave,
	displayStyleLabel,
	page,
	pageEditorPage,
	site,
}: {
	apiHelpers: ApiHelpers;
	beforeSave?: (configurationIframe: FrameLocator) => Promise<void>;
	displayStyleLabel?: string;
	page: Page;
	pageEditorPage: PageEditorPage;
	site: Site;
}): Promise<Layout> {
	const widgetId = getRandomString();

	const layout = await apiHelpers.headlessDelivery.createSitePage({
		pageDefinition: getPageDefinition([
			getWidgetDefinition({
				id: widgetId,
				widgetName:
					'com_liferay_asset_publisher_web_portlet_AssetPublisherPortlet',
			}),
		]),
		siteId: site.id,
		title: getRandomString(),
	});

	await pageEditorPage.goto(layout, site.friendlyUrlPath);

	await pageEditorPage.goToWidgetConfiguration(widgetId);

	const configurationIframe = page.frameLocator(
		'iframe[title*="Configuration"]'
	);

	const assetSelectionTab = configurationIframe.getByRole('tab', {
		name: 'Asset Selection',
	});

	await assetSelectionTab.waitFor({state: 'visible'});

	await assetSelectionTab.click();

	await clickAndExpectToBeVisible({
		autoClick: true,
		target: configurationIframe
			.frameLocator('iframe[title="Select Collection"]')
			.getByRole('link', {name: 'Collection Providers'}),
		timeout: 2000,
		trigger: configurationIframe.getByRole('button', {
			exact: true,
			name: 'Select Collection',
		}),
	});

	await clickAndExpectToBeHidden({
		target: configurationIframe.locator('.modal-dialog'),
		timeout: 2000,
		trigger: configurationIframe
			.frameLocator('iframe[title="Select Collection"]')
			.getByRole('button', {name: 'Select Recent Content'}),
	});

	if (displayStyleLabel) {
		await configurationIframe
			.getByRole('tab', {name: 'Display Settings'})
			.click();

		await configurationIframe.getByLabel('Display Template').click();

		await configurationIframe
			.getByRole('option', {name: displayStyleLabel})
			.click();
	}

	if (beforeSave) {
		await beforeSave(configurationIframe);
	}

	await configurationIframe.getByRole('button', {name: 'Save'}).click();

	await expect(
		configurationIframe.locator('.alert-success').first()
	).toBeVisible();

	await page
		.locator('.modal-header')
		.getByLabel('Close', {exact: true})
		.click();

	await page.getByLabel('Publish', {exact: true}).click();

	return layout;
}
