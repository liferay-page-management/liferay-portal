/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {addParams, navigate, openSelectionModal} from 'frontend-js-web';

type Tag = {
	qaId: string;
	selectable: boolean;
	value: string;
};

export default function openTagSelectionModal({
	portletNamespace,
	redirectURL,
	selectTagURL,
	title,
}: {
	portletNamespace: string;
	redirectURL: string;
	selectTagURL: string;
	title?: string;
}) {
	openSelectionModal({
		buttonAddLabel: Liferay.Language.get('select'),
		height: '70vh',
		iframeBodyCssClass: '',
		multiple: true,
		onSelect: (selectedItems: Tag[]) => {
			if (!selectedItems.length) {
				return;
			}

			let url = redirectURL;

			const assetTags = selectedItems.map((tag) => tag.value);

			assetTags.forEach((assetTag: string) => {
				const selectedValue = JSON.parse(assetTag);

				url = addParams(
					`${portletNamespace}assetTagName=${selectedValue.tagName}`,
					url
				);
			});

			navigate(url);
		},
		selectEventName: `${portletNamespace}selectedAssetTag`,
		size: 'lg',
		title: title || Liferay.Language.get('filter-by-tags'),
		url: selectTagURL,
	});
}
