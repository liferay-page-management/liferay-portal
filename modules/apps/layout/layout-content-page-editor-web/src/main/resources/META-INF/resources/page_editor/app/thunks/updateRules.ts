/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Rule} from '../../types/Rule';
import {State} from '../../types/State';
import updateNetwork from '../actions/updateNetwork';
import updateRulesAction from '../actions/updateRules';
import RulesService from '../services/RulesService';

export default function updateRules(rules: Rule[]) {
	return async (
		dispatch: (
			action: ReturnType<typeof updateNetwork | typeof updateRulesAction>
		) => void,
		getState: () => State
	) => {
		const {segmentsExperienceId} = getState();

		const {layoutData} = await RulesService.updateRules({
			onNetworkStatus: dispatch,
			rules,
			segmentsExperienceId,
		});

		dispatch(updateRulesAction({layoutData}));
	};
}
