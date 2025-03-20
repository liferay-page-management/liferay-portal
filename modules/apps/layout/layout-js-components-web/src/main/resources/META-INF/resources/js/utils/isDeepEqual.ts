/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export default function deepEqual(a: any, b: any): boolean {
	if (a === b) {
		return true;
	}

	if (Array.isArray(a) && Array.isArray(b)) {
		if (a.length !== b.length) {
			return false;
		}

		return a.every((value, index) => {
			return deepEqual(value, b[index]);
		});
	}

	if (a && b && typeof a === 'object' && typeof b === 'object') {
		const keys = Object.keys(a);

		if (keys.length !== Object.keys(b).length) {
			return false;
		}

		return keys.every((key) => {
			return deepEqual(a[key], b[key]);
		});
	}

	return false;
}
