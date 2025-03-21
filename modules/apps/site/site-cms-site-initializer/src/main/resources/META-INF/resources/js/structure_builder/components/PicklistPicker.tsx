/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton, {ClayButtonWithIcon} from '@clayui/button';
import {Option, Picker} from '@clayui/core';
import {ClayDropDownWithItems} from '@clayui/drop-down';
import ClayForm, {ClayInput} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import classNames from 'classnames';
import {FieldFeedback, useId} from 'frontend-js-components-web';
import {sub} from 'frontend-js-web';
import React, {useEffect, useState} from 'react';

import {useSelector, useStateDispatch} from '../contexts/StateContext';
import selectInvalids from '../selectors/selectInvalids';
import selectPublishedFields from '../selectors/selectPublishedFields';
import PicklistService from '../services/PicklistService';
import {Field, MultiselectField, SingleSelectField} from '../utils/field';

type Picklist = {
	id: React.Key;
	name: string;
};

export default function PicklistPicker({field}: {field: Field}) {
	const selectField = field as SingleSelectField | MultiselectField;

	const [hasError, setHasError] = useState<boolean>(false);
	const [picklists, setPicklists] = useState<Picklist[]>([]);
	const [selectedKey, setSelectedKey] = useState<React.Key>(
		selectField.picklistId
	);

	const dispatch = useStateDispatch();

	const invalids = useSelector(selectInvalids);
	const publishedFields = useSelector(selectPublishedFields);

	const isPublished = publishedFields.has(field.uuid);

	const feedbackId = useId();
	const pickerId = selectField.picklistInputUuid;

	useEffect(() => {
		PicklistService.getPicklists().then((picklists) =>
			setPicklists(picklists)
		);
	}, []);

	useEffect(() => {
		setHasError(invalids.has(pickerId));
	}, [invalids, pickerId]);

	return (
		<ClayForm.Group className={classNames('mb-2', {'has-error': hasError})}>
			<ClayInput.Group className="align-items-end">
				<ClayInput.GroupItem>
					<label htmlFor={pickerId}>
						{Liferay.Language.get('picklist')}

						<ClayIcon
							className="ml-1 reference-mark"
							symbol="asterisk"
						/>
					</label>

					<Picker
						aria-describedby={feedbackId}
						aria-label={sub(
							Liferay.Language.get('select-x'),
							Liferay.Language.get('picklist')
						)}
						disabled={isPublished || !picklists.length}
						id={pickerId}
						items={picklists}
						onBlur={(
							event: React.FocusEvent<HTMLButtonElement>
						) => {
							const noOptionSelected = !picklists.some(
								(picklist) =>
									picklist.id.toString() ===
									event.relatedTarget?.id
							);

							if (!selectedKey && noOptionSelected) {
								dispatch({
									inputIdToValidate: pickerId,
									type: 'update-field',
									uuid: field.uuid,
								});
							}
						}}
						onSelectionChange={(selectedKey: React.Key) => {
							dispatch({
								inputIdToValidate: pickerId,
								picklistId: selectedKey as string,
								type: 'update-field',
								uuid: field.uuid,
							});

							setSelectedKey(selectedKey);
						}}
						selectedKey={selectedKey}
					>
						{(item) => <Option key={item.id}>{item.name}</Option>}
					</Picker>
				</ClayInput.GroupItem>

				<ClayInput.GroupItem shrink>
					{selectedKey ? (
						<ClayDropDownWithItems
							items={[
								{
									label: Liferay.Language.get('edit'),
									symbolLeft: 'pencil',
								},
								{type: 'divider'},
								{
									label: Liferay.Language.get('new-picklist'),
									symbolRight: 'shortcut',
								},
							]}
							trigger={
								<ClayButtonWithIcon
									aria-label={Liferay.Language.get(
										'more-actions'
									)}
									displayType="secondary"
									symbol="ellipsis-v"
									title={Liferay.Language.get('more-actions')}
								/>
							}
						/>
					) : (
						<ClayButton displayType="secondary">
							{Liferay.Language.get('new-picklist')}

							<ClayIcon className="ml-2" symbol="shortcut" />
						</ClayButton>
					)}
				</ClayInput.GroupItem>
			</ClayInput.Group>

			{hasError ? (
				<FieldFeedback
					errorMessage={Liferay.Language.get(
						'this-field-is-required'
					)}
					id={feedbackId}
				/>
			) : null}
		</ClayForm.Group>
	);
}
