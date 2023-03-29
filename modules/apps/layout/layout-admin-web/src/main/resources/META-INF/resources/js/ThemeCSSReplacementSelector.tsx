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

import {ClayButtonWithIcon} from '@clayui/button';
import ClayForm, {ClayInput} from '@clayui/form';
import {openSelectionModal} from 'frontend-js-web';
import React, {useEffect, useRef, useState} from 'react';

export default function ThemeCSSReplacementSelector({
	disabled: initialDisabled,
	placeholder,
	portletNamespace,
	selectThemeCSSClientExtensionEventName,
	selectThemeCSSClientExtensionURL,
	themeCSSCETExternalReferenceCode,
	themeCSSExtensionName,
}: IProps) {
	const [extensionName, setExtensionName] = useState(themeCSSExtensionName);
	const [cetExternalReferenceCode, setCETExternalReferenceCode] = useState(
		themeCSSCETExternalReferenceCode
	);
	const [disabled, setDisabled] = useState(initialDisabled);

	const inputRef = useRef<HTMLInputElement>(null);

	const onClick = () => {
		if (inputRef.current?.classList.contains('disabled')) {
			return;
		}

		openSelectionModal<{value: string}>({
			onSelect: (selectedItem) => {
				const item = JSON.parse(selectedItem.value);

				setCETExternalReferenceCode(item.cetExternalReferenceCode);
				setExtensionName(item.name);
			},
			selectEventName: selectThemeCSSClientExtensionEventName,
			title: Liferay.Language.get('select-theme-css-client-extension'),
			url: selectThemeCSSClientExtensionURL,
		});
	};

	const onThemeChange = ({value}: {value: string}) => {
		setDisabled(value === 'inherit' ? true : false);
	};

	useEffect(() => {
		Liferay.on('themeChange', onThemeChange);

		return () => {
			Liferay.detach('themeChange');
		};
	}, []);

	return (
		<>
			<ClayInput
				name={`${portletNamespace}themeCSSCETExternalReferenceCode`}
				type="hidden"
				value={cetExternalReferenceCode}
			/>
			<ClayForm.Group>
				<label
					className={disabled ? 'disabled' : ''}
					htmlFor={`${portletNamespace}themeCSSReplacementExtension`}
				>
					{Liferay.Language.get('theme-css')}
				</label>

				<ClayInput.Group className="w-50" small>
					<ClayInput.GroupItem>
						<ClayInput
							disabled={disabled}
							id={`${portletNamespace}themeCSSReplacementExtension`}
							onClick={onClick}
							placeholder={placeholder}
							readOnly
							ref={inputRef}
							type="text"
							value={extensionName}
						/>
					</ClayInput.GroupItem>

					<ClayInput.GroupItem shrink>
						{extensionName ? (
							<>
								<ClayButtonWithIcon
									aria-label={Liferay.Language.get('replace')}
									className="mr-2"
									disabled={disabled}
									displayType="secondary"
									id={`${portletNamespace}themeCSSSelectButton`}
									onClick={onClick}
									small
									symbol="change"
								/>

								<ClayButtonWithIcon
									aria-label={Liferay.Language.get('delete')}
									disabled={disabled}
									displayType="secondary"
									id={`${portletNamespace}themeCSSDeleteButton`}
									onClick={() => {
										setExtensionName('');
										setCETExternalReferenceCode('');
									}}
									small
									symbol="trash"
								/>
							</>
						) : (
							<ClayButtonWithIcon
								aria-label={Liferay.Language.get('select')}
								disabled={disabled}
								displayType="secondary"
								id={`${portletNamespace}themeCSSSelectButton`}
								onClick={onClick}
								small
								symbol="plus"
							/>
						)}
					</ClayInput.GroupItem>
				</ClayInput.Group>
			</ClayForm.Group>
		</>
	);
}

interface IProps {
	disabled: boolean;
	placeholder: string;
	portletNamespace: string;
	selectThemeCSSClientExtensionEventName: string;
	selectThemeCSSClientExtensionURL: string;
	themeCSSCETExternalReferenceCode: string;
	themeCSSExtensionName: string;
}
