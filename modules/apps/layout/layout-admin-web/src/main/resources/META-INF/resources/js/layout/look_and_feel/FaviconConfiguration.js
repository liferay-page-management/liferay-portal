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

import ClayButton from '@clayui/button';
import {openSelectionModal} from 'frontend-js-web';
import React, {useState} from 'react';

export default function FaviconConfiguration({
	faviconImage: initialFaviconImage,
	faviconTitle: initialFaviconTitle,
	fileEntryItemSelectorURL,
	isClearFaviconButtonEnabled,
	portletNamespace,
}) {
	const [favicon, setFavicon] = useState({
		fileEntryId: '',
		image: initialFaviconImage,
		title: initialFaviconTitle,
	});

	const handleChangeButtonClick = () =>
		openSelectionModal({
			onSelect(selectedItem) {
				if (selectedItem?.value) {
					const itemValue = JSON.parse(selectedItem.value);

					setFavicon({
						fileEntryId: itemValue.fileEntryId,
						image: itemValue.url || '',
						title: itemValue.title || '',
					});
				}
			},
			selectEventName: `${portletNamespace}selectImage`,
			title: Liferay.Language.get('select-favicon'),
			url: fileEntryItemSelectorURL.toString(),
		});

	const handleClearButtonClick = () => {
		setFavicon({
			fileEntryId: '0',
			image: '',
			title: '',
		});
	};

	return (
		<>
			<input
				name={`${portletNamespace}faviconFileEntryId`}
				type="hidden"
				value={favicon.fileEntryId}
			/>

			<h3 className="sheet-subtitle">
				{Liferay.Language.get('favicon')}
			</h3>

			{favicon.image ? (
				<img
					alt={favicon.title}
					className="mb-2"
					height="16"
					src={favicon.image}
					width="16"
				/>
			) : null}

			<p>
				<strong>{`${Liferay.Language.get('favicon-name')}: `}</strong>

				{favicon.title}
			</p>

			<ClayButton.Group spaced>
				<ClayButton
					displayType="secondary"
					onClick={handleChangeButtonClick}
					small
				>
					{Liferay.Language.get('change-favicon')}
				</ClayButton>

				<ClayButton
					disabled={!isClearFaviconButtonEnabled}
					displayType="secondary"
					onClick={handleClearButtonClick}
					small
				>
					{Liferay.Language.get('clear')}
				</ClayButton>
			</ClayButton.Group>
		</>
	);
}
