/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayForm, {ClayInput} from '@clayui/form';
import {openSelectionModal, sub} from 'frontend-js-web';
import React, {useState} from 'react';

import {Item, LabelList} from './LabelList';

interface DDMStructure {
	ddmStructureId: string;
	name: string;
}

interface Props {
	ddmStructures?: DDMStructure[];
	itemSelectorNamespace: string;
	portletNamespace: string;
	selectDDMStructureURL: string;
}

export default function HighlightedDDMStructuresConfiguration({
	ddmStructures: initialDDMStructures,
	itemSelectorNamespace,
	portletNamespace,
	selectDDMStructureURL,
}: Props) {
	const [ddmStructures, setDDMStructures] = useState<DDMStructure[]>(
		initialDDMStructures || []
	);

	const addCheckedDDMStructureIdsToURL = () => {
		const url = new URL(selectDDMStructureURL);

		url.searchParams.set(
			`${itemSelectorNamespace}checkedDDMStructureIds`,
			ddmStructureIdsToString(ddmStructures)
		);

		return url.href;
	};

	const onSelectButtonClick = () =>
		openSelectionModal({
			multiple: true,
			onSelect: (selectedItems: Array<{value: string}>) =>
				setDDMStructures(
					selectedItems.map(itemSelectorValueToDDMStructure)
				),
			title: sub(
				Liferay.Language.get('select-x'),
				Liferay.Language.get('structures')
			),
			url: addCheckedDDMStructureIdsToURL(),
		});

	return (
		<div className="c-px-4">
			<p className="c-pb-4">
				{Liferay.Language.get(
					'select-the-structures-you-want-to-highlight-in-web-content-administration-to-quickly-access-and-manage-all-its-contents'
				)}
			</p>

			<input
				name={`${portletNamespace}preferences--highlightedDDMStructures--`}
				type="hidden"
				value={ddmStructureIdsToString(ddmStructures)}
			/>

			<ClayForm.Group>
				<p className="h5 text-weight-semi-bold">
					{Liferay.Language.get('highlighted-structures')}
				</p>

				<ClayInput.Group>
					<ClayInput.GroupItem>
						<LabelList
							items={ddmStructures.map(ddmStructureToItem)}
						/>
					</ClayInput.GroupItem>

					<ClayInput.GroupItem shrink>
						<ClayButton
							aria-label={Liferay.Language.get(
								'add-highlighted-structures'
							)}
							displayType="secondary"
							onClick={onSelectButtonClick}
							type="button"
						>
							{Liferay.Language.get('select')}
						</ClayButton>
					</ClayInput.GroupItem>
				</ClayInput.Group>
			</ClayForm.Group>
		</div>
	);
}

function ddmStructureIdsToString(ddmStructures: DDMStructure[]): string {
	return ddmStructures
		.map((ddmStructure) => ddmStructure.ddmStructureId)
		.join(',');
}

function ddmStructureToItem(ddmStructure: DDMStructure): Item {
	return {
		label: ddmStructure.name,
		value: ddmStructure.ddmStructureId,
	};
}

function itemSelectorValueToDDMStructure(item: {value: string}): DDMStructure {
	const parsedValue = JSON.parse(item.value) as {
		ddmstructureid: string;
		name: string;
	};

	return {
		ddmStructureId: parsedValue.ddmstructureid,
		name: parsedValue.name,
	};
}
