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

import {useCallback} from 'react';

import {UPDATE_CONTROLS_ITEM} from '../actions/types';
import {fromControlsId} from '../components/layout-data-items/Collection';
import {ITEM_ACTIVATION_ORIGINS} from '../config/constants/itemActivationOrigins';
import {ITEM_TYPES} from '../config/constants/itemTypes';
import {useToControlsId} from './CollectionItemContext';
import {useDispatch, useSelector, useSelectorCallback} from './StoreContext';

export function useActivationOrigin() {
	return useSelector((state) => state.controls?.active?.activationOrigin);
}

export function useActiveItemId() {
	return useSelector((state) =>
		fromControlsId(state.controls?.active?.itemId)
	);
}

export function useActiveItemType() {
	return useSelector((state) => state.controls?.active?.itemType);
}

export function useIsActive(itemId) {
	const toControlsId = useToControlsId();

	return useSelectorCallback(
		(state) => state.controls?.active?.itemId === toControlsId(itemId),
		[itemId, toControlsId]
	);
}

export function useIsActiveCallback() {
	const activeItemId = useSelector((state) => state.controls?.active?.itemId);
	const toControlsId = useToControlsId();

	return useCallback((itemId) => activeItemId === toControlsId(itemId), [
		activeItemId,
		toControlsId,
	]);
}

export function useHoveringOrigin() {
	return useSelector((state) => state.controls?.hover?.activationOrigin);
}

export function useHoveredItemId() {
	return useSelector((state) =>
		fromControlsId(state.controls?.hover?.itemId)
	);
}

export function useHoveredItemType() {
	return useSelector((state) => state.controls?.hover?.itemType);
}

export function useIsHovered(itemId) {
	const toControlsId = useToControlsId();

	return useSelectorCallback(
		(state) => state.controls?.hover?.itemId === toControlsId(itemId),
		[itemId, toControlsId]
	);
}

export function useIsHoveredCallback() {
	const hoveredItemId = useSelector((state) => state.controls?.hover?.itemId);
	const toControlsId = useToControlsId();

	return useCallback((itemId) => hoveredItemId === toControlsId(itemId), [
		hoveredItemId,
		toControlsId,
	]);
}

export function useHoverItem() {
	const dispatch = useDispatch();
	const toControlsId = useToControlsId();

	return useCallback(
		(
			itemId,
			{
				itemType = ITEM_TYPES.layoutDataItem,
				origin = ITEM_ACTIVATION_ORIGINS.pageEditor,
			} = {
				itemType: ITEM_TYPES.layoutDataItem,
			}
		) =>
			dispatch({
				activationOrigin: origin,
				itemId: toControlsId(itemId),
				itemType,
				namespace: 'hover',
				type: UPDATE_CONTROLS_ITEM,
			}),
		[dispatch, toControlsId]
	);
}

export function useSelectItem() {
	const dispatch = useDispatch();
	const toControlsId = useToControlsId();

	return useCallback(
		(
			itemId,
			{
				itemType = ITEM_TYPES.layoutDataItem,
				origin = ITEM_ACTIVATION_ORIGINS.pageEditor,
			} = {
				itemType: ITEM_TYPES.layoutDataItem,
			}
		) =>
			dispatch({
				activationOrigin: origin,
				itemId: toControlsId(itemId),
				itemType,
				namespace: 'active',
				type: UPDATE_CONTROLS_ITEM,
			}),
		[dispatch, toControlsId]
	);
}
