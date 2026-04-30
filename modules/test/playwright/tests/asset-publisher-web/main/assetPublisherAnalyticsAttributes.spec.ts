/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';

import {analyticsCloudConnectedTest} from '../../../fixtures/analyticsCloudConnectedTest';
import {apiHelpersTest} from '../../../fixtures/apiHelpersTest';
import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {isolatedSiteTest} from '../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../fixtures/loginTest';
import {pageEditorPagesTest} from '../../../fixtures/pageEditorPagesTest';
import {ApiHelpers} from '../../../helpers/ApiHelpers';
import {PageEditorPage} from '../../../pages/layout-content-page-editor-web/PageEditorPage';
import getRandomString from '../../../utils/getRandomString';
import getBasicWebContentStructureId from '../../../utils/structured-content/getBasicWebContentStructureId';
import {templatesPageTest} from '../../template-web/main/fixtures/templatesPageTest';
import {createAssetPublisherPage} from './utils/createAssetPublisherPage';

const testWithFeatureFlagOn = mergeTests(
	analyticsCloudConnectedTest,
	apiHelpersTest,
	featureFlagsTest({
		'LPD-81914': {enabled: true},
	}),
	isolatedSiteTest,
	loginTest(),
	pageEditorPagesTest
);

const testWithFeatureFlagOnAndTemplates = mergeTests(
	analyticsCloudConnectedTest,
	apiHelpersTest,
	featureFlagsTest({
		'LPD-81914': {enabled: true},
	}),
	isolatedSiteTest,
	loginTest(),
	pageEditorPagesTest,
	templatesPageTest
);

const testWithFeatureFlagOff = mergeTests(
	apiHelpersTest,
	featureFlagsTest({
		'LPD-81914': {enabled: false},
	}),
	isolatedSiteTest,
	loginTest(),
	pageEditorPagesTest
);

async function expectNoAttributesInEditMode(
	page: Page,
	pageEditorPage: PageEditorPage,
	layout: Layout,
	site: Site,
	title: string,
	scopeSelector: string
) {
	await pageEditorPage.goto(layout, site.friendlyUrlPath);

	await expect(page.getByText(title, {exact: true})).toBeVisible();

	await expect(
		page.locator(`${scopeSelector}[data-analytics-asset-title="${title}"]`)
	).toHaveCount(0);
}

async function setUpJournalArticle(
	apiHelpers: ApiHelpers,
	siteId: string,
	title: string
) {
	return apiHelpers.headlessDelivery.postStructuredContent({
		contentStructureId: await getBasicWebContentStructureId(apiHelpers),
		datePublished: '2024-01-01T00:00:00Z',
		siteId,
		title,
		viewableBy: 'Anyone',
	});
}

const TEMPLATE_CASES = [
	{
		displayStyleLabel: undefined,
		editModeScope: 'div.asset-abstract',
		hasViewAction: true,
		name: 'Abstracts',
		testClickThrough: true,
	},
	{
		displayStyleLabel: 'Table',
		editModeScope: 'td.table-title',
		hasViewAction: false,
		name: 'Table',
		testClickThrough: false,
	},
	{
		displayStyleLabel: 'Title List',
		editModeScope: 'p.list-group-title',
		hasViewAction: false,
		name: 'Title List',
		testClickThrough: false,
	},
	{
		displayStyleLabel: 'Rich Summary',
		editModeScope: 'h3.asset-title',
		hasViewAction: true,
		name: 'Rich Summary',
		testClickThrough: false,
	},
];

for (const templateCase of TEMPLATE_CASES) {
	testWithFeatureFlagOn(
		`Emits data-analytics-asset-* attributes for the ${templateCase.name} display template`,
		{
			tag: '@LPD-83537',
		},
		async ({apiHelpers, page, pageEditorPage, site}) => {
			const title = getRandomString();

			await setUpJournalArticle(apiHelpers, site.id, title);

			const layout = await createAssetPublisherPage({
				apiHelpers,
				displayStyleLabel: templateCase.displayStyleLabel,
				page,
				pageEditorPage,
				site,
			});

			await page.goto(
				`/web${site.friendlyUrlPath}${layout.friendlyUrlPath}`
			);

			const assetEntry = page.locator(
				`[data-analytics-asset-title="${title}"][data-analytics-asset-id]`
			);

			await expect(assetEntry.first()).toBeVisible();

			await expect(assetEntry.first()).toHaveAttribute(
				'data-analytics-asset-type',
				'web-content'
			);

			await expect(assetEntry.first()).toHaveAttribute(
				'data-analytics-asset-subtype',
				'basic-web-content'
			);

			await expect(assetEntry.first()).toHaveAttribute(
				'data-analytics-external-reference-code',
				/.+/
			);

			await expect(
				page.locator(
					`[data-analytics-asset-title="${title}"][data-analytics-asset-action="impression"]`
				)
			).not.toHaveCount(0);

			if (templateCase.hasViewAction) {
				await expect(
					page.locator(
						`[data-analytics-asset-title="${title}"][data-analytics-asset-action="view"]`
					)
				).not.toHaveCount(0);
			}

			if (templateCase.testClickThrough) {
				await page
					.getByRole('link', {exact: true, name: title})
					.click();

				await expect(
					page.getByText(title, {exact: true})
				).toBeVisible();

				await expect(
					page.locator(
						`[data-analytics-asset-title="${title}"][data-analytics-asset-action="impression"]`
					)
				).not.toHaveCount(0);

				await expect(
					page.locator(
						`[data-analytics-asset-title="${title}"][data-analytics-asset-action="view"]`
					)
				).not.toHaveCount(0);
			}

			await expectNoAttributesInEditMode(
				page,
				pageEditorPage,
				layout,
				site,
				title,
				templateCase.editModeScope
			);
		}
	);
}

testWithFeatureFlagOnAndTemplates(
	'Exposes assetAnalyticsAttributesHelper to the Asset Publisher widget template designer',
	{
		tag: '@LPD-83537',
	},
	async ({page, site, templatesPage}) => {
		const templateName = getRandomString();

		await templatesPage.gotoWidgetTemplates(site.friendlyUrlPath);

		await templatesPage.createWidgetTemplate(
			templateName,
			'Asset Publisher Template'
		);

		await templatesPage.editTemplate(templateName);

		const helperButton = page.getByRole('button', {
			exact: true,
			name: 'Asset Analytics Attributes Helper',
		});

		await expect(helperButton).toBeVisible();

		await helperButton.locator('.preview-icon').hover();

		await expect(
			page.getByText(
				'Builds the data-analytics-asset-* attributes for an asset entry.'
			)
		).toBeVisible();

		await helperButton.click();

		const codeMirror = page.locator('.CodeMirror-code');

		await expect(codeMirror).toContainText('entry?has_content');
		await expect(codeMirror).toContainText(
			'assetAnalyticsAttributesHelper.buildAttributes(entry, "impression", "title", locale)'
		);
		await expect(codeMirror).toContainText('entry.getTitle(locale)');
	}
);

testWithFeatureFlagOff(
	'Does not emit data-analytics-asset-* attributes when LPD-81914 is disabled',
	{
		tag: '@LPD-83537',
	},
	async ({apiHelpers, page, pageEditorPage, site}) => {
		const title = getRandomString();

		await setUpJournalArticle(apiHelpers, site.id, title);

		const layout = await createAssetPublisherPage({
			apiHelpers,
			page,
			pageEditorPage,
			site,
		});

		await page.goto(`/web${site.friendlyUrlPath}${layout.friendlyUrlPath}`);

		await expect(page.getByText(title, {exact: true})).toBeVisible();

		await expect(
			page.locator(
				`div.asset-abstract[data-analytics-asset-title="${title}"]`
			)
		).toHaveCount(0);
	}
);
