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

import {UPDATE_CONTROLS_ITEM} from '../actions/types';

const INITIAL_STATE = {
	active: {
		activationOrigin: null,
		itemId: null,
		itemType: null,
	},
	hover: {
		activationOrigin: null,
		itemId: null,
		itemType: null,
	},
};

export default function controlsReducer(state = INITIAL_STATE, action) {
	switch (action.type) {
		case UPDATE_CONTROLS_ITEM: {
			return {
				...state,
				[action.namespace]: {
					activationOrigin: action.activationOrigin,
					itemId: action.itemId,
					itemType: action.itemType,
				},
			};
		}
		default:
			return state;
	}
}
