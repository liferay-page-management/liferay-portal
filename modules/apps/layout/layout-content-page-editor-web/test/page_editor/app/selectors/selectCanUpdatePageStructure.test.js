/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {VIEWPORT_SIZES} from '../../../../src/main/resources/META-INF/resources/page_editor/app/config/constants/viewportSizes';
import selectCanUpdatePageStructure from '../../../../src/main/resources/META-INF/resources/page_editor/app/selectors/selectCanUpdatePageStructure';

describe('selectCanUpdatePageStructure', () => {
	it('returns true when the user has update permission in the desktop viewport', () => {
		const canUpdatePageStructure = selectCanUpdatePageStructure({
			permissions: {UPDATE: true},
			selectedViewportSize: VIEWPORT_SIZES.desktop,
		});

		expect(canUpdatePageStructure).toBe(true);
	});

	it('returns false when the page template is read only', () => {
		const canUpdatePageStructure = selectCanUpdatePageStructure({
			permissions: {READ_ONLY: true, UPDATE: true},
			selectedViewportSize: VIEWPORT_SIZES.desktop,
		});

		expect(canUpdatePageStructure).toBe(false);
	});

	it('returns false when the segments experiment is locked', () => {
		const canUpdatePageStructure = selectCanUpdatePageStructure({
			permissions: {LOCKED_SEGMENTS_EXPERIMENT: true, UPDATE: true},
			selectedViewportSize: VIEWPORT_SIZES.desktop,
		});

		expect(canUpdatePageStructure).toBe(false);
	});

	it('returns false when the user has no update permission', () => {
		const canUpdatePageStructure = selectCanUpdatePageStructure({
			permissions: {},
			selectedViewportSize: VIEWPORT_SIZES.desktop,
		});

		expect(canUpdatePageStructure).toBeFalsy();
	});

	it('returns false when the viewport is not desktop', () => {
		const canUpdatePageStructure = selectCanUpdatePageStructure({
			permissions: {UPDATE: true},
			selectedViewportSize: VIEWPORT_SIZES.landscapeMobile,
		});

		expect(canUpdatePageStructure).toBeFalsy();
	});
});
