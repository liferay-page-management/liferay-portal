/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayButtonWithIcon} from '@clayui/button';
import ClayForm, {ClayInput} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayLink from '@clayui/link';
import {DesignLibraryNameLabel} from '@liferay/layout-js-components-web';
import {openSelectionModal} from 'frontend-js-components-web';
import {sub} from 'frontend-js-web';
import React, {useState} from 'react';

function updateLookAndFeelVisibility(
	masterLayoutPageTemplateEntryERC: string,
	portletNamespace: string
) {
	const customCSS = document.getElementById(`${portletNamespace}customCSS`);

	if (customCSS) {
		if (!masterLayoutPageTemplateEntryERC) {
			customCSS.classList.remove('hide');
		}
		else {
			customCSS.classList.add('hide');
		}
	}

	const themeContainer = document.getElementById(
		`${portletNamespace}themeContainer`
	);

	const sheet = themeContainer?.closest('.sheet');

	if (sheet) {
		if (!masterLayoutPageTemplateEntryERC) {
			sheet.classList.remove('hide');

			sheet.removeAttribute('aria-hidden');
		}
		else {
			sheet.classList.add('hide');

			sheet.setAttribute('aria-hidden', 'true');
		}
	}
}

interface IProps {
	changeMasterLayoutURL: string;
	editMasterLayoutURL: string;
	isReadOnly: boolean;
	masterLayoutName: string;
	masterLayoutPageTemplateEntryERC: string;
	masterLayoutPageTemplateEntryScopeERC: string;
	masterLayoutPageTemplateEntryScopeLabel: string;
	portletNamespace: string;
}

export default function MasterLayoutConfiguration({
	changeMasterLayoutURL,
	editMasterLayoutURL,
	isReadOnly,
	masterLayoutName,
	masterLayoutPageTemplateEntryERC,
	masterLayoutPageTemplateEntryScopeERC,
	masterLayoutPageTemplateEntryScopeLabel,
	portletNamespace,
}: IProps) {
	const [masterLayout, setMasterLayout] = useState({
		erc: masterLayoutPageTemplateEntryERC,
		name: masterLayoutName,
		scopeERC: masterLayoutPageTemplateEntryScopeERC,
		scopeLabel: masterLayoutPageTemplateEntryScopeLabel,
	});

	const handleChangeMasterButtonClick = () => {
		if (isReadOnly) {
			return;
		}

		openSelectionModal({
			iframeBodyCssClass: '',
			onSelect(selectedItem: {value: string}) {
				if (selectedItem) {
					const itemValue = JSON.parse(selectedItem.value);

					setMasterLayout({
						erc: itemValue.masterLayoutPageTemplateEntryERC,
						name: itemValue.name,
						scopeERC:
							itemValue.masterLayoutPageTemplateEntryScopeERC,
						scopeLabel:
							itemValue.masterLayoutPageTemplateEntryScopeLabel,
					});

					updateLookAndFeelVisibility(
						itemValue.masterLayoutPageTemplateEntryERC,
						portletNamespace
					);
				}
			},
			selectEventName: `${portletNamespace}selectMasterLayout`,
			title: Liferay.Language.get('select-master'),
			url: changeMasterLayoutURL,
		});
	};

	return (
		<>
			<input
				name={`${portletNamespace}masterLayoutPageTemplateEntryERC`}
				type="hidden"
				value={masterLayout.erc}
			/>

			<input
				name={`${portletNamespace}masterLayoutPageTemplateEntryScopeERC`}
				type="hidden"
				value={masterLayout.scopeERC}
			/>

			<label htmlFor={`${portletNamespace}masterLayout`}>
				{Liferay.Language.get('master')}
			</label>

			<div className="d-flex">
				<ClayForm.Group className="c-mb-0 flex-grow-1">
					<ClayInput
						id={`${portletNamespace}masterLayout`}
						onClick={handleChangeMasterButtonClick}
						onKeyDown={(event) => {
							if (event.key === 'Enter') {
								event.preventDefault();

								handleChangeMasterButtonClick();
							}
						}}
						readOnly
						value={masterLayout.name}
					/>
				</ClayForm.Group>

				{editMasterLayoutURL && masterLayout.erc && (
					<ClayLink
						aria-label={Liferay.Language.get('edit-master')}
						button={{monospaced: true}}
						className="c-ml-2"
						displayType="secondary"
						href={editMasterLayoutURL}
					>
						<ClayIcon symbol="pencil" />
					</ClayLink>
				)}

				<ClayButtonWithIcon
					aria-label={Liferay.Language.get('change-master')}
					className="c-ml-2"
					disabled={isReadOnly}
					displayType="secondary"
					onClick={handleChangeMasterButtonClick}
					symbol="change"
				/>
			</div>

			{masterLayout.scopeLabel && (
				<div className="c-mt-2">
					<DesignLibraryNameLabel
						ariaLabel={sub(
							Liferay.Language.get(
								'master-from-x-design-library'
							),
							masterLayout.scopeLabel
						)}
						value={masterLayout.scopeLabel}
					/>
				</div>
			)}
		</>
	);
}
