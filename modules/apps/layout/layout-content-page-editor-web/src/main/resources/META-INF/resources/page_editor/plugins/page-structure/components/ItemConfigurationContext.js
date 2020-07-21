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

import React, {useCallback, useContext, useRef} from 'react';

import {deepEqual} from '../../../app/utils/checkDeepEqual';

const ItemConfigurationDispatchContext = React.createContext(() => {});
const ItemConfigurationStateContext = React.createContext(new Map());

export const ITEM_CONFIGURATION_PANELS = {};

export function ItemConfigurationContextProvider({children}) {
	const panels = useRef(new Map());

	const setPanels = useCallback((item, itemPanels) => {
		const prevItemPanels = panels.current.get(item.type);

		if (!deepEqual(prevItemPanels, itemPanels)) {
			panels.current.set(item.type, itemPanels);
		}
	}, []);

	return (
		<ItemConfigurationDispatchContext.Provider value={setPanels}>
			<ItemConfigurationStateContext.Provider value={panels.current}>
				{children}
			</ItemConfigurationStateContext.Provider>
		</ItemConfigurationDispatchContext.Provider>
	);
}

export function useSetItemConfigurationPanels(item, panels) {
	const setPanels = useContext(ItemConfigurationDispatchContext);

	setPanels(item, panels);
}

const EMPTY_OBJECT = {};

export function useItemConfigurationPanels(item = null) {
	const panels = useContext(ItemConfigurationStateContext);

	return panels.get(item?.type ?? '') || EMPTY_OBJECT;
}
