/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openToast} from 'frontend-js-components-web';
import {Dispatch} from 'react';

import {Action, State} from '../contexts/StateContext';
import {Group} from '../types/Structure';

export default function handleUngroup({
	dispatch,
	group,
	publishedChildren,
}: {
	dispatch: Dispatch<Action>;
	group: Group;
	publishedChildren: State['publishedChildren'];
}) {

	// Ungrouping a repeatable group drops the object definition its children
	// live on, so a published one keeps it. A group that is not repeatable only
	// describes the layout, so there is nothing to lose.

	if (group.isRepeatable && publishedChildren.has(group.uuid)) {
		openToast({
			message: Liferay.Language.get(
				'the-ungroup-action-cannot-be-done-because-this-repeatable-group-is-already-published'
			),
			type: 'danger',
		});

		return;
	}

	dispatch({type: 'ungroup', uuid: group.uuid});
}
