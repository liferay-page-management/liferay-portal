/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Rule} from '../../../types/Rule';
import {State} from '../../../types/State';
import updateRules from '../../thunks/updateRules';

function undoAction({action}: {action: {rules: Rule[]}}) {
	const {rules} = action;

	return updateRules(rules);
}

function getDerivedStateForUndo({state}: {state: State}) {
	return {rules: state.layoutData.pageRules};
}

export {getDerivedStateForUndo, undoAction};
