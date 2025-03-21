/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export default function isDeepEqual(a: any, b: any): boolean {
	if (a === b) {
		return true;
	}

	if (Array.isArray(a) && Array.isArray(b)) {
		if (a.length !== b.length) {
			return false;
		}

		return a.every((value, index) => {
			return isDeepEqual(value, b[index]);
		});
	}

	if (a && b && typeof a === 'object' && typeof b === 'object') {
		if (a instanceof Set && b instanceof Set) {
			if (a.size !== b.size) {
				return false;
			}

			return [...a].every((item) => b.has(item));
		}
		else {
			const keys = Object.keys(a);

			if (keys.length !== Object.keys(b).length) {
				return false;
			}

			return keys.every((key) => {
				return isDeepEqual(a[key], b[key]);
			});
		}
	}

	return false;
}
