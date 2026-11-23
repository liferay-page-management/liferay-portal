/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayDropDown from '@clayui/drop-down';
import ClayIcon from '@clayui/icon';
import React, {useState} from 'react';

import './LaunchDraftSelector.scss';

interface DraftLayout {
	active: boolean;
	disabled: boolean;
	href?: string;
	label: string;
	launchSetId: number;
}

interface Props {
	draftLayouts: DraftLayout[];
}

function iconSymbol(draftLayout: DraftLayout) {
	return draftLayout.launchSetId > 0 ? 'arrow-split' : 'simple-circle';
}

function iconVariant(draftLayout: DraftLayout) {
	return draftLayout.launchSetId > 0 ? 'launch' : 'main';
}

export default function LaunchDraftSelector({draftLayouts}: Props) {
	const [active, setActive] = useState(false);

	const current =
		draftLayouts.find((draftLayout) => draftLayout.active) ??
		draftLayouts[0];

	if (!current) {
		return null;
	}

	return (
		<ClayDropDown
			active={active}
			className="launch-draft-selector"
			menuElementAttrs={{className: 'launch-draft-selector__menu'}}
			onActiveChange={setActive}
			trigger={
				<ClayButton
					aria-label={Liferay.Language.get('select-draft')}
					className="launch-draft-selector__trigger"
					displayType="unstyled"
				>
					<ClayIcon
						className={`launch-draft-selector__trigger-icon launch-draft-selector__trigger-icon--${iconVariant(
							current
						)}`}
						symbol={iconSymbol(current)}
					/>

					<span className="launch-draft-selector__trigger-label">
						{current.label}
					</span>

					<ClayIcon
						className="launch-draft-selector__trigger-caret"
						symbol="caret-double"
					/>
				</ClayButton>
			}
		>
			<ClayDropDown.ItemList>
				{draftLayouts.map((draftLayout, index) => {
					const className = [
						'launch-draft-selector__item',
						draftLayout.active &&
							'launch-draft-selector__item--active',
						draftLayout.disabled &&
							'launch-draft-selector__item--disabled',
					]
						.filter(Boolean)
						.join(' ');

					const content = (
						<>
							<ClayIcon
								className={`launch-draft-selector__item-icon launch-draft-selector__item-icon--${iconVariant(
									draftLayout
								)}`}
								symbol={iconSymbol(draftLayout)}
							/>

							<span className="launch-draft-selector__item-label">
								{draftLayout.label}
							</span>
						</>
					);

					return (
						<React.Fragment key={draftLayout.launchSetId}>
							{draftLayout.active || draftLayout.disabled ? (
								<ClayDropDown.Item
									aria-current={
										draftLayout.active ? 'page' : undefined
									}
									aria-disabled={
										draftLayout.disabled ? true : undefined
									}
									className={className}
									onClick={(event) => event.preventDefault()}
									roleItem="menuitem"
								>
									{content}
								</ClayDropDown.Item>
							) : (
								<ClayDropDown.Item
									className={className}
									href={draftLayout.href}
									roleItem="menuitem"
								>
									{content}
								</ClayDropDown.Item>
							)}

							{index === 0 && draftLayouts.length > 1 && (
								<ClayDropDown.Divider />
							)}
						</React.Fragment>
					);
				})}
			</ClayDropDown.ItemList>
		</ClayDropDown>
	);
}
