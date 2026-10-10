/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';

import {accessibilityMenuPagesTest} from '../../../fixtures/accessibilityMenuPagesTest';
import {instanceSettingsPagesTest} from '../../../fixtures/instanceSettingsPagesTest';
import {loginTest} from '../../../fixtures/loginTest';
import {doAndGoBack} from '../../../utils/doAndGoBack';
import {performLoginViaApi, performLogout} from '../../../utils/performLogin';

// Liferay.Util.openToast closes after 5000 ms by default.

const TOAST_AUTO_CLOSE_INTERVAL = 5000;

async function openToast(page: Page, message: string) {
	await page.evaluate(
		(message) => Liferay.Util.openToast({message}),
		message
	);

	await expect(page.getByText(message)).toBeVisible();
}

const test = mergeTests(
	accessibilityMenuPagesTest,
	instanceSettingsPagesTest,
	loginTest()
);

test.beforeEach(async ({accessibilityMenuPage, instanceSettingsPage, page}) => {
	await doAndGoBack(page, async () => {
		await instanceSettingsPage.goToInstanceSetting(
			'Accessibility',
			'Accessibility Menu'
		);

		await accessibilityMenuPage.enableAccessibilityMenu();
	});

	await performLogout(page);
});

test.afterEach(async ({instanceSettingsPage, page}) => {
	await performLoginViaApi({page, screenName: 'test'});

	await instanceSettingsPage.goToInstanceSetting(
		'Accessibility',
		'Accessibility Menu'
	);

	await instanceSettingsPage.resetInstanceSetting();
});

test(
	'Toast stays open when persistent notifications is on',
	{tag: '@LPD-66969'},
	async ({accessibilityMenuPage, page}) => {
		await page.clock.install();

		await accessibilityMenuPage.openAccessibilityMenu();

		await accessibilityMenuPage.togglePersistentNotifications(true);

		await openToast(page, 'Toast that stays open');

		await page.clock.fastForward(TOAST_AUTO_CLOSE_INTERVAL + 1000);

		await expect(page.getByText('Toast that stays open')).toBeVisible();
	}
);

test(
	'Toast closes when persistent notifications is off',
	{tag: '@LPD-66969'},
	async ({accessibilityMenuPage, page}) => {
		await page.clock.install();

		await accessibilityMenuPage.openAccessibilityMenu();

		await accessibilityMenuPage.togglePersistentNotifications(false);

		await openToast(page, 'Toast that auto closes');

		await page.clock.fastForward(TOAST_AUTO_CLOSE_INTERVAL + 1000);

		await expect(page.getByText('Toast that auto closes')).toBeHidden();
	}
);
