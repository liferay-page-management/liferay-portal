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

import PropTypes from 'prop-types';
import React, {useCallback, useEffect, useMemo} from 'react';

import {BACKGROUND_IMAGE_FRAGMENT_ENTRY_PROCESSOR} from '../../config/constants/backgroundImageFragmentEntryProcessor';
import {EDITABLE_FRAGMENT_ENTRY_PROCESSOR} from '../../config/constants/editableFragmentEntryProcessor';
import {ITEM_ACTIVATION_ORIGINS} from '../../config/constants/itemActivationOrigins';
import {ITEM_TYPES} from '../../config/constants/itemTypes';
import {VIEWPORT_SIZES} from '../../config/constants/viewportSizes';
import {config} from '../../config/index';
import {useToControlsId} from '../../contexts/CollectionItemContext';
import {useHoverItem, useSelectItem} from '../../contexts/ControlsContext';
import {useSetEditableProcessorUniqueId} from '../../contexts/EditableProcessorContext';
import {useSelector, useSelectorCallback} from '../../contexts/StoreContext';
import selectCanUpdateEditables from '../../selectors/selectCanUpdateEditables';
import selectCanUpdatePageStructure from '../../selectors/selectCanUpdatePageStructure';
import selectLanguageId from '../../selectors/selectLanguageId';
import canActivateEditable from '../../utils/canActivateEditable';
import {deepEqual} from '../../utils/checkDeepEqual';
import isMapped from '../../utils/editable-value/isMapped';
import {getEditableElement} from './getEditableElement';

const EDITABLE_CLASS_NAMES = {
	active: 'page-editor__editable--active',
	contentHovered: 'page-editor__editable--content-hovered',
	hovered: 'page-editor__editable--hovered',
	mapped: 'page-editor__editable--mapped',
	translated: 'page-editor__editable--translated',
};

const isTranslated = (defaultLanguageId, languageId, editableValue) =>
	defaultLanguageId !== languageId && editableValue?.[languageId];

function FragmentContentInteractionsFilter({
	children,
	editables,
	fragmentEntryLinkId,
	itemId,
}) {
	const siblingIds = useMemo(
		() => [itemId, ...editables.map((editable) => editable.itemId)],
		[itemId, editables]
	);

	const canUpdateEditables = useSelector(selectCanUpdateEditables);
	const canUpdatePageStructure = useSelector(selectCanUpdatePageStructure);
	const hoverItem = useHoverItem();

	const languageId = useSelector(selectLanguageId);
	const selectItem = useSelectItem();
	const selectedViewportSize = useSelector(
		(state) => state.selectedViewportSize
	);
	const setEditableProcessorUniqueId = useSetEditableProcessorUniqueId();
	const toControlsId = useToControlsId();

	const activeItemId = useSelectorCallback(
		(state) =>
			siblingIds.includes(state.controls?.active?.itemId)
				? state.controls.active.itemId
				: null,
		[siblingIds]
	);

	const isActive = useCallback(
		(itemId) => toControlsId(itemId) === activeItemId,
		[activeItemId, toControlsId]
	);

	const hoveredItemId = useSelectorCallback(
		(state) =>
			siblingIds.includes(state.controls?.hover?.itemId)
				? state.controls.hover.itemId
				: null,
		[siblingIds]
	);

	const isHovered = useCallback(
		(itemId) => toControlsId(itemId) === hoveredItemId,
		[hoveredItemId, toControlsId]
	);

	const editableValues = useSelectorCallback(
		(state) => {
			const fragmentEntryLink =
				state.fragmentEntryLinks[fragmentEntryLinkId];

			return fragmentEntryLink
				? {
						...fragmentEntryLink.editableValues[
							EDITABLE_FRAGMENT_ENTRY_PROCESSOR
						],
						...fragmentEntryLink.editableValues[
							BACKGROUND_IMAGE_FRAGMENT_ENTRY_PROCESSOR
						],
				  }
				: {};
		},
		[fragmentEntryLinkId],
		deepEqual
	);

	useEffect(() => {
		editables.forEach((editable) => {
			if (editableValues) {
				const editableValue = editableValues[editable.editableId];
				const {element} = editable;

				if (isMapped(editableValue)) {
					element.classList.add(EDITABLE_CLASS_NAMES.mapped);
				}
				else if (
					isTranslated(
						config.defaultLanguageId,
						languageId,
						editableValue
					)
				) {
					element.classList.add(EDITABLE_CLASS_NAMES.translated);
				}
				else {
					element.classList.remove(EDITABLE_CLASS_NAMES.mapped);
					element.classList.remove(EDITABLE_CLASS_NAMES.translated);
				}
			}
		});
	}, [editables, editableValues, languageId]);

	useEffect(() => {
		editables.forEach((editable) => {
			if (isActive(editable.itemId)) {
				editable.element.classList.add(EDITABLE_CLASS_NAMES.active);
			}
			else {
				editable.element.classList.remove(EDITABLE_CLASS_NAMES.active);
			}
		});
	}, [editables, isActive]);

	useSelectorCallback(
		(state) => {
			const hoveredItemId = state.controls?.hover?.itemId;
			const hoveredItemType = state.controls?.hover?.itemType;
			const hoveringOrigin = state.controls?.hover?.activationOrigin;

			editables.forEach((editable) => {
				if (editableValues) {
					const editableValue =
						editableValues[editable.editableId] || {};

					const localizedEditableValue =
						editableValue[languageId] || {};

					const editableId =
						hoveredItemType === ITEM_TYPES.mappedContent
							? editableValue.classNameId
								? `${editableValue.classNameId}-${editableValue.classPK}`
								: `${localizedEditableValue.classNameId}-${localizedEditableValue.classPK}`
							: editable.itemId;

					const hovered =
						([
							ITEM_TYPES.mappedContent,
							ITEM_TYPES.inlineContent,
						].includes(hoveredItemType) &&
							hoveredItemId === editableId) ||
						((siblingIds.some(isActive) ||
							!canUpdatePageStructure) &&
							isHovered(editable.itemId));

					const hoveredClass =
						hoveringOrigin === ITEM_ACTIVATION_ORIGINS.contents
							? EDITABLE_CLASS_NAMES.contentHovered
							: EDITABLE_CLASS_NAMES.hovered;

					if (hovered) {
						editable.element.classList.add(hoveredClass);
					}
					else {
						editable.element.classList.remove(
							EDITABLE_CLASS_NAMES.hovered,
							EDITABLE_CLASS_NAMES.contentHovered
						);
					}
				}
			});

			return null;
		},
		[
			canUpdatePageStructure,
			editables,
			editableValues,
			fragmentEntryLinkId,
			isActive,
			isHovered,
			itemId,
			languageId,
			siblingIds,
		]
	);

	const enableProcessor = useCallback(
		(event) => {
			const editableElement = getEditableElement(event.target);

			const editable = editables.find(
				(editable) => editable.element === editableElement
			);

			if (editable) {
				const editableValue = editableValues[editable.editableId] || {};

				if (isMapped(editableValue)) {
					return;
				}

				const editableClickPosition = {
					clientX: event.clientX,
					clientY: event.clientY,
				};

				if (isActive(editable.itemId)) {
					setEditableProcessorUniqueId(
						toControlsId(editable.itemId),
						editableClickPosition
					);
				}
			}
		},
		[
			editables,
			editableValues,
			isActive,
			setEditableProcessorUniqueId,
			toControlsId,
		]
	);

	const activeEditable = useSelectorCallback(
		(state) => {
			const activationOrigin = state.controls?.active?.activationOrigin;
			const activeItemType = state.controls?.active?.itemType;

			let activeEditable;

			if (activeItemType === ITEM_TYPES.editable) {
				activeEditable = editables.find((editable) =>
					isActive(editable.itemId)
				);

				if (activeEditable) {
					if (
						canUpdateEditables &&
						selectedViewportSize === VIEWPORT_SIZES.desktop
					) {
						requestAnimationFrame(() => {
							activeEditable.element.addEventListener(
								'dblclick',
								enableProcessor
							);
						});
					}

					if (activationOrigin === ITEM_ACTIVATION_ORIGINS.sidebar) {
						activeEditable.element.scrollIntoView({
							behavior: 'smooth',
							block: 'center',
							inline: 'nearest',
						});
					}
				}
			}

			return activeEditable;
		},
		[
			canUpdateEditables,
			editables,
			fragmentEntryLinkId,
			isActive,
			itemId,
			selectedViewportSize,
		]
	);

	useEffect(() => {
		if (activeEditable) {
			return () => {
				activeEditable.element.removeEventListener(
					'dblclick',
					enableProcessor
				);
			};
		}
	}, [activeEditable, enableProcessor]);

	const hoverEditable = (event) => {
		const editableElement = getEditableElement(event.target);

		const editable = editables.find(
			(editable) => editable.element === editableElement
		);

		if (editable) {
			event.stopPropagation();

			hoverItem(editable.itemId, {itemType: ITEM_TYPES.editable});
		}
	};

	const selectEditable = (event) => {
		const editableElement = getEditableElement(event.target);

		const editable = editables.find(
			(editable) => editable.element === editableElement
		);

		if (
			editable &&
			canUpdateEditables &&
			canActivateEditable(selectedViewportSize, editable.type)
		) {
			event.stopPropagation();

			if (isActive(editable.itemId)) {
				event.stopPropagation();
			}
			else {
				selectItem(editable.itemId, {
					itemType: ITEM_TYPES.editable,
				});
			}
		}
	};

	const props = {};

	if (siblingIds.some(isActive) || !canUpdatePageStructure) {
		props.onClickCapture = selectEditable;
		props.onMouseLeave = () => hoverItem(null);
		props.onMouseOverCapture = hoverEditable;
	}

	return <div {...props}>{children}</div>;
}

FragmentContentInteractionsFilter.propTypes = {
	element: PropTypes.object,
	fragmentEntryLinkId: PropTypes.string.isRequired,
	itemId: PropTypes.string.isRequired,
};

export default React.memo(FragmentContentInteractionsFilter);
