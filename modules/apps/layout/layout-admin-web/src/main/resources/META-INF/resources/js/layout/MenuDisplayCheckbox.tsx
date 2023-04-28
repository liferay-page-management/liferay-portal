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

import ClayForm, {ClayCheckbox} from '@clayui/form';
import React, {useState} from 'react';

export default function CheckboxWithDescription({
	checked: initiallyChecked,
	portletNamespace,
}: IProps) {
	const [checked, setChecked] = useState(initiallyChecked);

	return (
		<>
			<ClayForm.Group className="mb-0">
				<ClayCheckbox
					aria-describedby={`${portletNamespace}hiddenDescription`}
					checked={checked}
					label={Liferay.Language.get('show-page-in-menu-display')}
					onChange={() => setChecked((val) => !val)}
				/>

				<p
					className="mb-0 text-3 text-secondary"
					id={`${portletNamespace}hiddenDescription`}
				>
					{Liferay.Language.get(
						'hidden-from-navigation-menu-widget-help-message'
					)}
				</p>
			</ClayForm.Group>

			<input
				name={`${portletNamespace}hidden`}
				type="hidden"
				value={`${!checked}`}
			/>
		</>
	);
}

interface IProps {
	checked: boolean;
	portletNamespace: string;
}
