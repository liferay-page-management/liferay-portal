/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ConfigurationCustomComponentProps} from '@liferay/layout-js-components-web';
import React, {useEffect, useState} from 'react';

type SimpleInputFieldValues = {
	description: string;
	order: string;
	title: string;
};

function SimpleInputField({
	onValueSelect,
	values,
}: ConfigurationCustomComponentProps<SimpleInputFieldValues>) {
	const [localValues, setLocalValues] = useState<SimpleInputFieldValues>({
		description: values['description'] || '',
		order: values['order'] || '',
		title: values['title'] || '',
	});

	useEffect(() => {
		setLocalValues({
			description: values['description'] || '',
			order: values['order'] || '',
			title: values['title'] || '',
		});
	}, [values]);

	const handleChange = (field: keyof SimpleInputFieldValues, value: string) => {
		setLocalValues((previous) => ({...previous, [field]: value}));
		onValueSelect(field, value);
	};

	return (
		<div>
			<label className="control-label" htmlFor="simpleInputTitle">
				{'Title'}
			</label>

			<input
				className="form-control"
				id="simpleInputTitle"
				onChange={(event) => {
					handleChange('title', event.target.value);
				}}
				type="text"
				value={localValues.title}
			/>

			<label
				className="control-label mt-3"
				htmlFor="simpleInputDescription"
			>
				{'Description'}
			</label>

			<input
				className="form-control"
				id="simpleInputDescription"
				onChange={(event) => {
					handleChange('description', event.target.value);
				}}
				type="text"
				value={localValues.description}
			/>

			<label className="control-label mt-3" htmlFor="simpleInputOrder">
				{'Order'}
			</label>

			<input
				className="form-control"
				id="simpleInputOrder"
				onChange={(event) => {
					handleChange('order', event.target.value);
				}}
				type="number"
				value={localValues.order}
			/>
		</div>
	);
}

export default SimpleInputField;
