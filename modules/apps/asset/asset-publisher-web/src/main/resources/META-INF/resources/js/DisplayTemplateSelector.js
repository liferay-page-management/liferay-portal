/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Option, Picker} from '@clayui/core';
import DropDown from '@clayui/drop-down';
import Form, {ClayInput} from '@clayui/form';
import React, {useState} from 'react';

export default function DisplayTemplateSelector({
	ddmTemplates,
	displayStyleGroupId,
	displayStyles,
	namespace,
	selectedDisplayStyle,
}) {
	const [groupId, setGroupId] = useState(displayStyleGroupId);
	const [selectedOption, setSelectedOption] = useState(selectedDisplayStyle);

	const items = [
		{
			items: [...displayStyles],
			label: Liferay.Language.get('default'),
		},
		{
			items: [...ddmTemplates],
		},
	];

	const setDisplayStyleGroupId = (option) => {
		if (!displayStyles.find(({value}) => value === option)) {
			const groupId = ddmTemplates.find(({value}) => value === option)
				.groupId;

			if (groupId) {
				setGroupId(groupId);
			}
		}
	};

	const onSelectionChangeHandlder = (option) => {
		setSelectedOption(option);

		showHiddenFields(option);

		setDisplayStyleGroupId(option);
	};

	const showHiddenFields = (option) => {
		const hiddenFields = document.querySelectorAll('.hidden-field');

		Array.from(hiddenFields).forEach((field) => {
			const fieldContainer = field.closest('.form-group');

			if (fieldContainer) {
				const fieldClassList = field.classList;
				const fieldContainerClassList = fieldContainer.classList;

				if (
					option === 'full-content' &&
					(fieldClassList.contains('show-asset-title') ||
						fieldClassList.contains('show-context-link') ||
						fieldClassList.contains('show-extra-info'))
				) {
					fieldContainerClassList.remove('hide');
				}
				else if (
					option === 'abstracts' &&
					fieldClassList.contains('abstract-length')
				) {
					fieldContainerClassList.remove('hide');
				}
				else {
					fieldContainerClassList.add('hide');
				}
			}
		});
	};

	return (
		<>
			<ClayInput
				name={`${namespace}preferences--displayStyle--`}
				type="hidden"
				value={selectedOption}
			/>

			<ClayInput
				id={`${namespace}displayStyleGroupId`}
				name={`${namespace}preferences--displayStyleGroupId--`}
				type="hidden"
				value={groupId}
			/>

			<Form.Group>
				<label htmlFor={`${namespace}displayStyle`}>
					{Liferay.Language.get('display-template')}
				</label>

				<Picker
					UNSAFE_menuClassName="cadmin"
					className="display-template-selector"
					defaultSelectedKey="abstracts"
					id={`${namespace}displayStyle`}
					items={items}
					onSelectionChange={(key) => onSelectionChangeHandlder(key)}
					selectedKey={selectedOption}
				>
					{(group) => (
						<DropDown.Group
							header={group.label}
							items={group.items}
						>
							{(item) => (
								<Option
									displaystylegroupid={item.groupId}
									key={item.value}
								>
									{item.label}
								</Option>
							)}
						</DropDown.Group>
					)}
				</Picker>
			</Form.Group>
		</>
	);
}
