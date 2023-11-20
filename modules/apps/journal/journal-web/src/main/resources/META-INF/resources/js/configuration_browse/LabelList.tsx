/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLabel from '@clayui/label';
import classNames from 'classnames';
import React from 'react';

export interface Item {
	label: string;
	value: string;
}

export function LabelList({items}: {items: Item[]}) {
	return (
		<ul
			className={classNames('bg-white form-control list-unstyled', {
				'form-control-tag-group': items.length,
			})}
		>
			{items.map((item) => (
				<li key={item.value}>
					<ClayLabel>{item.label}</ClayLabel>
				</li>
			))}
		</ul>
	);
}
