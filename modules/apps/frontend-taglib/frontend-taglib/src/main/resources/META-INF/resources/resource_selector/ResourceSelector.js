/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAlert from '@clayui/alert';
import ClayButton from '@clayui/button';
import ClayForm, {ClayInput} from '@clayui/form';
import {openSelectionModal} from 'frontend-js-web';
import React, {useState} from 'react';

export default function ResourceSelector({
	inputLabel,
	inputName,
	modalTitle,
	portletNamespace,
	resourceName: initialResourceName,
	resourceValue: initialResourceValue,
	selectEventName,
	selectResourceURL,
	showRemoveButton,
	warningMessage,
}) {
	const [resourceName, setResourceName] = useState(initialResourceName);
	const [resourceValue, setResourceValue] = useState(initialResourceValue);
	const [showWarning, setShowWarning] = useState(!!warningMessage);

	const onResourceSelect = () =>
		openSelectionModal({
			iframeBodyCssClass: '',
			onSelect: (selectedItem) => {
				if (selectedItem) {
					setResourceValue(selectedItem.resourceId);
					setResourceName(selectedItem.resourceName);
					setShowWarning(false);
				}
			},
			selectEventName: `${portletNamespace}${selectEventName}`,
			title: modalTitle,
			url: selectResourceURL,
		});

	return (
		<ClayForm.Group>
			<ClayForm.Group>
				<ClayInput
					name={`${portletNamespace}${inputName}`}
					type="hidden"
					value={resourceValue}
				/>

				<label htmlFor={`${portletNamespace}resourceName`}>
					{inputLabel}
				</label>

				<ClayInput
					disabled
					id={`${portletNamespace}resourceName`}
					type="text"
					value={resourceName}
				/>
			</ClayForm.Group>

			{showWarning ? (
				<ClayAlert displayType="warning">{warningMessage}</ClayAlert>
			) : null}

			<ClayButton.Group spaced>
				<ClayButton displayType="secondary" onClick={onResourceSelect}>
					{Liferay.Language.get('select')}
				</ClayButton>

				{showRemoveButton ? (
					<ClayButton
						disabled={resourceValue === '0'}
						displayType="secondary"
						onClick={() => {
							setResourceValue('0');
							setResourceName('');
							setShowWarning(false);
						}}
					>
						{Liferay.Language.get('remove')}
					</ClayButton>
				) : null}
			</ClayButton.Group>
		</ClayForm.Group>
	);
}
