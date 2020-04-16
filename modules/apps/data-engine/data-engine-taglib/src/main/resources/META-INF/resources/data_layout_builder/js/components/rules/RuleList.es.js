/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import ClayButton, {ClayButtonWithIcon} from '@clayui/button';
import React, {useContext, useState} from 'react';

import AppContext from '../../AppContext.es';
import EmptyState from '../empty-state/EmptyState.es';
import {SearchInputWithForm} from '../search-input/SearchInput.es';
import RuleEditorModal from './RuleEditorModal.es';
import RuleItem from './RuleItem.es';

export default () => {
	const [rulesEditorState, setRulesEditorState] = useState({
		isVisible: false,
		rule: null,
	});

	const [searchText, setSearchText] = useState('');

	const [
		{
			dataLayout: {dataRules},
		},
	] = useContext(AppContext);

	const filtereDataRules = dataRules
		.map((rule, index) => ({
			...rule,
			name: `Rule ${index}`,
		}))
		.filter(({name}) => new RegExp(searchText, 'ig').test(name));

	const toggleRulesEditorVisibility = rule => {
		if (rule) {
			rule['logical-operator'] = rule['logicalOperator'];
			delete rule.logicalOperator;
		}

		setRulesEditorState(prevState => ({
			isVisible: !prevState.isVisible,
			rule,
		}));
	};

	return (
		<>
			<div className="autofit-row sidebar-section">
				<div className="autofit-col autofit-col-expand">
					<SearchInputWithForm
						onChange={searchText => setSearchText(searchText)}
					/>
				</div>

				<div className="autofit-col ml-2">
					<ClayButtonWithIcon
						displayType="primary"
						onClick={() => toggleRulesEditorVisibility()}
						symbol="plus"
					/>
				</div>
			</div>

			{filtereDataRules.length === 0 ? (
				<EmptyState
					emptyState={{
						button: () => (
							<ClayButton
								displayType="secondary"
								onClick={() => toggleRulesEditorVisibility()}
							>
								{Liferay.Language.get('add-rule')}
							</ClayButton>
						),
						description: Liferay.Language.get(
							'there-are-no-rules-description'
						),
						title: Liferay.Language.get('there-are-no-rules'),
					}}
					keywords={searchText}
					small
				/>
			) : (
				<div className="autofit-col mt-4 rule-list">
					<hr />
					{filtereDataRules.map((rule, index) => (
						<RuleItem
							key={index}
							rule={rule}
							toggleRulesEditorVisibility={
								toggleRulesEditorVisibility
							}
						/>
					))}
				</div>
			)}

			<RuleEditorModal
				isVisible={rulesEditorState.isVisible}
				onClose={() => toggleRulesEditorVisibility()}
				rule={rulesEditorState.rule}
			/>
		</>
	);
};
