/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

export class ControlMenuPage {
	readonly page: Page;
	readonly addButton: Locator;
	readonly addPanelContentTab: Locator;

	constructor(page: Page) {
		this.page = page;

		this.addButton = page
			.locator('.control-menu-nav-item')
			.getByRole('button', {
				exact: true,
				name: 'Add',
			});

		this.addPanelContentTab = page.getByText('Content', {
			exact: true,
		});
	}

	async clickAddButton() {
		await this.addButton.click();
	}

	async goToAddPanelContentTab() {
		await this.page.getByText('Content', {exact: true}).click();
	}
}
