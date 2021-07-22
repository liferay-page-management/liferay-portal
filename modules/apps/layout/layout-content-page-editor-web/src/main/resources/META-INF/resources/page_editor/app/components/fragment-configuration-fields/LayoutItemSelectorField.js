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
import ClayDropDown from '@clayui/drop-down';
import ClayForm, {ClayCheckbox} from '@clayui/form';
import PropTypes from 'prop-types';
import React, {useState} from 'react';

import {ConfigurationFieldPropTypes} from '../../../prop-types/index';
import {LAYOUT_DATA_ITEM_TYPES} from '../../config/constants/layoutDataItemTypes';
import {useHoverItem} from '../../contexts/ControlsContext';
import {useSelectorCallback} from '../../contexts/StoreContext';
import getLayoutDataItemLabel from '../../utils/getLayoutDataItemLabel';
import {useId} from '../../utils/useId';

function getLayoutDataItemLabelWithCollectionName(item, fragmentEntryLinks) {
	if (
		item.type === LAYOUT_DATA_ITEM_TYPES.collection &&
		item.config?.collection?.title
	) {
		return item.config.collection.title;
	}

	return getLayoutDataItemLabel(item, fragmentEntryLinks);
}

export const LayoutItemSelectorField = ({field, onValueSelect, value}) => {
	const [active, setActive] = useState(false);
	const inputId = useId();
	const [nextValue, setNextValue] = useState(value || []);
	const hoverItem = useHoverItem();

	const inputValue = useSelectorCallback(
		(state) => {
			if (nextValue.length === 0) {
				return '';
			}
			else if (nextValue.length === 1) {
				return getLayoutDataItemLabelWithCollectionName(
					state.layoutData.items[nextValue[0]],
					state.fragmentEntryLinks
				);
			}

			return Liferay.Language.get('multiple');
		},
		[nextValue]
	);

	const handleChange = (layoutItemId, checked) => {
		const included = nextValue.includes(layoutItemId);
		let selectedItems = nextValue;

		if (checked && !included) {
			selectedItems = [...nextValue, layoutItemId];

			setNextValue(selectedItems);
			onValueSelect(field.name, selectedItems);
		}
		else if (included) {
			selectedItems = nextValue.filter(
				(itemId) => itemId !== layoutItemId
			);

			setNextValue(selectedItems);
			onValueSelect(field.name, selectedItems);
		}
	};

	const items = useSelectorCallback(
		(state) =>
			Object.values(state.layoutData.items)
				.filter((item) => {
					if (
						item.type === LAYOUT_DATA_ITEM_TYPES.collection &&
						!item.config?.collection?.key
					) {
						return false;
					}

					return (
						!field.typeOptions.layoutItemType ||
						field.typeOptions.layoutItemType === item.type
					);
				})
				.map((item) => ({
					checked: nextValue.includes(item.itemId),
					label: getLayoutDataItemLabelWithCollectionName(
						item,
						state.fragmentEntryLinks
					),
					onChange: (checked) => handleChange(item.itemId, checked),
					type: 'checkbox',
					value: item.itemId,
				})),
		[field, nextValue]
	);

	return (
		<ClayForm.Group className="mt-1">
			<label htmlFor={inputId}>{field.label}</label>

			<ClayDropDown
				active={active}
				id={inputId}
				onActiveChange={setActive}
				trigger={
					<ClayButton
						aria-label={Liferay.Language.get('select')}
						className="bg-light font-weight-normal form-control-select text-left w-100"
						displayType="secondary"
						small
					>
						{inputValue ? (
							<span className="text-dark">{inputValue}</span>
						) : (
							Liferay.Language.get('select')
						)}
					</ClayButton>
				}
			>
				{items.map((item) => (
					<label
						className="d-flex dropdown-item"
						key={item.value}
						onMouseLeave={() => hoverItem(null)}
						onMouseOver={() => hoverItem(item.value)}
					>
						<ClayCheckbox
							checked={item.checked}
							onChange={item.onChange}
						/>
						<span className="font-weight-normal ml-2">
							{item.label}
						</span>
					</label>
				))}
			</ClayDropDown>
		</ClayForm.Group>
	);
};

LayoutItemSelectorField.propTypes = {
	field: PropTypes.shape(ConfigurationFieldPropTypes).isRequired,
	onValueSelect: PropTypes.func.isRequired,
	value: PropTypes.arrayOf(PropTypes.string),
};
