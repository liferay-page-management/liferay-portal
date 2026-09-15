/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Group, Structure} from '../../types/Structure';
import {Uuid} from '../../types/Uuid';
import findChild from '../findChild';
import {ErrorMap, validateGroup} from '../validation';
import updateChild from './updateChild';

export default function updateGroup({
	invalids,
	label,
	structure,
	uuid,
}: {
	invalids: Map<Uuid, ErrorMap>;
	label: Liferay.Language.LocalizedValue<string>;
	structure: Structure;
	uuid: Uuid;
}):
	| {children: Structure['children']; invalids: Map<Uuid, ErrorMap>}
	| undefined {
	const group = findChild({root: structure, uuid}) as Group;

	if (!group) {
		return undefined;
	}

	const children = updateChild({
		child: {...group, label},
		root: structure,
	});

	const nextInvalids = new Map(invalids);

	const errors = validateGroup({
		currentErrors: invalids.get(uuid),
		data: {label},
	});

	if (errors.size) {
		nextInvalids.set(uuid, errors);
	}
	else {
		nextInvalids.delete(uuid);
	}

	return {children, invalids: nextInvalids};
}
