/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Uuid} from '../contexts/StateContext';
import {Field} from './field';

export default function hasInvalidInput(field: Field, inputId: Uuid) {
	if (!inputId) {
		return false;
	}

	if ('picklistInputUuid' in field) {
		return field.picklistInputUuid === inputId && !field.picklistId;
	}

	return false;
}
