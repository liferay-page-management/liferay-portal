/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayButtonWithIcon} from '@clayui/button';
import ClayForm, {ClayInput} from '@clayui/form';
import {useModal} from '@clayui/modal';
import {IFrontendDataSetProps} from '@liferay/frontend-data-set-web';
import {ItemSelectorModal} from '@liferay/frontend-js-item-selector-web';
import {openSelectionModal} from 'frontend-js-components-web';
import React, {useState} from 'react';

type StyleBook = {
	designLibraryName: string | null;
	externalReferenceCode: string;
	name: string;
};

const STYLE_BOOK_VIEWS: IFrontendDataSetProps['views'] = [
	{
		contentRenderer: 'cards',
		default: true,
		label: Liferay.Language.get('cards'),
		name: 'cards',
		schema: {
			description: 'designLibraryName',
			symbol: '',
			title: 'name',
		},
		setItemComponentProps: ({props}: {item: StyleBook; props: object}) => ({
			...props,
			className: 'style-book-selector-card',
			symbol: 'book',
		}),
		thumbnail: 'cards2',
	},
	{
		contentRenderer: 'table',
		label: Liferay.Language.get('table'),
		name: 'table',
		schema: {
			fields: [
				{
					fieldName: 'name',
					label: Liferay.Language.get('name'),
					sortable: false,
				},
				{
					fieldName: 'designLibraryName',
					label: Liferay.Language.get('design-library'),
					sortable: false,
				},
			],
		},
		thumbnail: 'table',
	},
];

export default function StyleBookConfiguration({
	changeStyleBookURL,
	isDesignLibraryEnabled,
	isReadOnly,
	portletNamespace,
	styleBookEntryERC: initialStyleBookEntryERC,
	styleBookEntryName: initialStyleBookEntryName,
	styleBooksApiURL,
}: {
	changeStyleBookURL: string;
	isDesignLibraryEnabled: boolean;
	isReadOnly: boolean;
	portletNamespace: string;
	styleBookEntryERC: string;
	styleBookEntryName: string;
	styleBooksApiURL: string;
}) {
	const [styleBookEntry, setStyleBookEntry] = useState({
		name: initialStyleBookEntryName,
		styleBookEntryERC: initialStyleBookEntryERC,
	});

	const [selectedItems, setSelectedItems] = useState<StyleBook[]>([]);

	const {observer, onOpenChange, open} = useModal();

	const handleChangeStyleBook = () => {
		if (isReadOnly) {
			return;
		}

		if (isDesignLibraryEnabled) {
			onOpenChange(true);
		}
		else {
			openSelectionModal({
				iframeBodyCssClass: '',
				onSelect(selectedItem: {value: string}) {
					if (selectedItem) {
						const itemValue = JSON.parse(selectedItem.value);

						setStyleBookEntry({
							name: itemValue.name,
							styleBookEntryERC: itemValue.externalReferenceCode,
						});
					}
				},
				selectEventName: `${portletNamespace}selectStyleBook`,
				title: Liferay.Language.get('select-style-book'),
				url: changeStyleBookURL,
			});
		}
	};

	return (
		<>
			<input
				name={`${portletNamespace}styleBookEntryERC`}
				type="hidden"
				value={styleBookEntry.styleBookEntryERC}
			/>

			<label htmlFor={`${portletNamespace}styleBookEntry`}>
				{Liferay.Language.get('style-book')}
			</label>

			<div className="d-flex">
				<ClayForm.Group className="c-mb-0 flex-grow-1">
					<ClayInput
						id={`${portletNamespace}styleBookEntry`}
						onClick={handleChangeStyleBook}
						readOnly
						value={styleBookEntry.name}
					/>
				</ClayForm.Group>

				<ClayButtonWithIcon
					aria-label={Liferay.Language.get('change-style-book')}
					className="c-ml-2"
					disabled={isReadOnly}
					displayType="secondary"
					onClick={handleChangeStyleBook}
					symbol="change"
				/>
			</div>

			{open && isDesignLibraryEnabled && (
				<ItemSelectorModal<StyleBook>
					apiURL={styleBooksApiURL}
					fdsProps={{
						id: `${portletNamespace}styleBookSelector`,
						pagination: {
							deltas: [{label: 20}],
							initialDelta: 20,
						},
						views: STYLE_BOOK_VIEWS,
					}}
					items={selectedItems}
					locator={{
						id: 'externalReferenceCode',
						label: 'name',
						value: 'externalReferenceCode',
					}}
					observer={observer}
					onItemsChange={(items) => {
						if (items[0]) {
							setStyleBookEntry({
								name: items[0].name,
								styleBookEntryERC:
									items[0].externalReferenceCode,
							});
						}

						setSelectedItems([]);
					}}
					onOpenChange={onOpenChange}
					open={open}
					size="lg"
					title={Liferay.Language.get('select-style-book')}
				/>
			)}
		</>
	);
}
