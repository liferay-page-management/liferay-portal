/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import selectCanPublish from '../../../../src/main/resources/META-INF/resources/page_editor/app/selectors/selectCanPublish';

describe('selectCanPublish', () => {
	it('returns true when the user has update permission', () => {
		const canPublish = selectCanPublish({
			permissions: {UPDATE: true},
		});

		expect(canPublish).toBe(true);
	});

	it('returns false when the page template is read only', () => {
		const canPublish = selectCanPublish({
			permissions: {READ_ONLY: true, UPDATE: true},
		});

		expect(canPublish).toBe(false);
	});

	it('returns false when the page template is read only and the user has one of the layout update permissions', () => {
		const canPublish = selectCanPublish({
			permissions: {READ_ONLY: true, UPDATE_LAYOUT_CONTENT: true},
		});

		expect(canPublish).toBe(false);
	});

	it('returns false when the user has no update permission', () => {
		const canPublish = selectCanPublish({
			permissions: {},
		});

		expect(canPublish).toBeFalsy();
	});
});
