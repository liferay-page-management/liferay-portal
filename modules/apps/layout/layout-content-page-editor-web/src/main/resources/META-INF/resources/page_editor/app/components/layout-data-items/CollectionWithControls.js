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

import classNames from 'classnames';
import React from 'react';

import useSetRef from '../../../core/hooks/useSetRef';
import {getLayoutDataItemPropTypes} from '../../../prop-types/index';
import {LAYOUT_DATA_ITEM_TYPES} from '../../config/constants/layoutDataItemTypes';
import {config} from '../../config/index';
import {useSelector, useSelectorCallback} from '../../contexts/StoreContext';
import getLayoutDataItemTopperUniqueClassName from '../../utils/getLayoutDataItemTopperUniqueClassName';
import {getResponsiveConfig} from '../../utils/getResponsiveConfig';
import Topper from '../topper/Topper';
import Collection from './Collection';
import isHovered from './isHovered';

const CollectionWithControls = React.forwardRef(({children, item}, ref) => {
	const hovered = useSelectorCallback(
		(state) => {
			const isMapped =
				item.type === LAYOUT_DATA_ITEM_TYPES.collection &&
				'collection' in item.config;

			return isHovered({
				editableValue: isMapped ? item.config.collection : {},
				hoveredItemId: state.controls?.hover?.itemId,
				hoveredItemType: state.controls?.hover?.itemType,
			});
		},
		[item.type, item.config?.collection]
	);

	const [setRef, itemElement] = useSetRef(ref);

	const selectedViewportSize = useSelector(
		(state) => state.selectedViewportSize
	);

	const responsiveConfig = getResponsiveConfig(
		item.config,
		selectedViewportSize
	);

	const {display} = responsiveConfig.styles;

	return (
		<Topper
			className={classNames({
				[getLayoutDataItemTopperUniqueClassName(
					item.itemId
				)]: config.featureFlagLps132571,
				'page-editor__topper--hovered': hovered,
			})}
			item={item}
			itemElement={itemElement}
			style={{display}}
		>
			<Collection item={item} ref={setRef}>
				{children}
			</Collection>
		</Topper>
	);
});

CollectionWithControls.propTypes = {
	item: getLayoutDataItemPropTypes().isRequired,
};

export default CollectionWithControls;
