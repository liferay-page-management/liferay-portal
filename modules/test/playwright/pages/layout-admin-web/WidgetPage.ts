/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page} from '@playwright/test';

import {ControlMenuPage} from '../product-navigation-control-menu/ControlMenuPage';

export class WidgetPage {
	readonly page: Page;
	readonly controlMenuPage: ControlMenuPage;

	constructor(page: Page) {
		this.page = page;
		this.controlMenuPage = new ControlMenuPage(page);
	}

	async goToSitePage(site: Site, layoutFriendlyURL: string) {
		await this.page.goto(`/web${site.friendlyUrlPath}${layoutFriendlyURL}`);
	}
}
