/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import useKeyboardDragAndDrop from './useKeyboardDragAndDrop';
import usePointerDragAndDrop from './usePointerDragAndDrop';

import './useDragAndDrop.scss';

export const DROP_POSITIONS = {
	bottom: 'bottom',
	top: 'top',
} as const;

export type DropPosition =
	| (typeof DROP_POSITIONS)[keyof typeof DROP_POSITIONS]
	| '';

interface Props<T> {
	dragItemRef: React.RefObject<HTMLElement>;
	dropItemRef: React.RefObject<HTMLElement>;
	item: T;
	itemIndex: number;
	items: T[];
	onDrop: (items: T[]) => void;
}

export default function useDragAndDrop<T extends {id: string; name: string}>({
	dragItemRef,
	dropItemRef,
	item,
	itemIndex,
	items,
	onDrop,
}: Props<T>) {
	const {
		isPointerDragging,
		isPointerDropBottomPosition,
		isPointerDropTopPosition,
	} = usePointerDragAndDrop<T>({
		dragItemRef,
		dropItemRef,
		items,
		onDrop,
		targetItem: item,
	});

	const {
		handleKeyboardDragAndDrop,
		isKeyboardDragging,
		isKeyboardDropBottomPosition,
		isKeyboardDropTopPosition,
	} = useKeyboardDragAndDrop<T>({
		draggedItem: item,
		draggedItemIndex: itemIndex,
		items,
		onDrop,
	});

	return {
		handleKeyboardDragAndDrop,
		isDragging: isPointerDragging || isKeyboardDragging,
		isDropBottomPosition:
			isPointerDropBottomPosition || isKeyboardDropBottomPosition,
		isDropTopPosition:
			isPointerDropTopPosition || isKeyboardDropTopPosition,
		isKeyboardDragging,
	};
}
