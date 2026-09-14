/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import selectCanUpdateEditables from '../../../../src/main/resources/META-INF/resources/page_editor/app/selectors/selectCanUpdateEditables';

describe('selectCanUpdateEditables', () => {
	it('returns true when the user has update permission', () => {
		const canUpdateEditables = selectCanUpdateEditables({
			permissions: {UPDATE: true},
		});

		expect(canUpdateEditables).toBe(true);
	});

	it('returns false when the page template is read only', () => {
		const canUpdateEditables = selectCanUpdateEditables({
			permissions: {READ_ONLY: true, UPDATE: true},
		});

		expect(canUpdateEditables).toBe(false);
	});

	it('returns false when the page template is read only and the user has one of the layout update permissions', () => {
		const canUpdateEditables = selectCanUpdateEditables({
			permissions: {READ_ONLY: true, UPDATE_LAYOUT_CONTENT: true},
		});

		expect(canUpdateEditables).toBe(false);
	});

	it('returns false when the segments experiment is locked', () => {
		const canUpdateEditables = selectCanUpdateEditables({
			permissions: {LOCKED_SEGMENTS_EXPERIMENT: true, UPDATE: true},
		});

		expect(canUpdateEditables).toBe(false);
	});

	it('returns false when the user has no update permission', () => {
		const canUpdateEditables = selectCanUpdateEditables({
			permissions: {},
		});

		expect(canUpdateEditables).toBeFalsy();
	});
});
