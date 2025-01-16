/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {State} from '../../types/State';
import {FormLayoutDataItem} from '../../types/layout_data/FormLayoutDataItem';
import {LayoutData, LayoutDataItem} from '../../types/layout_data/LayoutData';
import {updateNetwork} from '../actions';
import removeFormStepAction from '../actions/removeFormStep';
import FormService from '../services/FormService';
import {getFormParent} from '../utils/getFormParent';
import {getStepperChild} from '../utils/getStepperChild';
import {isMultistepForm} from '../utils/isMultistepForm';

export default function removeFormStep({
	index,
	itemId,
	selectItem,
}: {
	index: number;
	itemId: LayoutDataItem['itemId'];
	selectItem: (id: string) => void;
}) {
	return (
		dispatch: (
			action: ReturnType<
				typeof updateNetwork | typeof removeFormStepAction
			>
		) => void,
		getState: () => State
	) => {
		const {fragmentEntryLinks, layoutData, segmentsExperienceId} =
			getState();

		const step = layoutData.items[itemId];
		const form = getFormParent(step, layoutData);

		if (!form) {
			return;
		}

		const stepper = getStepperChild(form, layoutData, fragmentEntryLinks);

		return FormService.removeFormStep({
			itemId,
			onNetworkStatus: dispatch,
			segmentsExperienceId,
			stepperFragmentEntryLinkId: stepper?.config.fragmentEntryLinkId,
		}).then(
			({
				fragmentEntryLinks: nextFragmentEntryLinks,
				layoutData: nextLayoutData,
				removedItemIds,
			}) => {
				const nextForm = nextLayoutData.items[
					form.itemId
				] as FormLayoutDataItem;

				dispatch(
					removeFormStepAction({
						fragmentEntryLinks: nextFragmentEntryLinks,
						itemId,
						layoutData: nextLayoutData,
						movedItemIds: getMovedItems(nextForm, layoutData),
						removedItemIds,
					})
				);

				if (isMultistepForm(nextForm)) {
					const previousStepId =
						layoutData.items[step.parentId].children[index - 1];

					selectItem(previousStepId);
				}
			}
		);
	};
}

function getMovedItems(
	form: FormLayoutDataItem,
	previousLayoutData: LayoutData
) {
	if (isMultistepForm(form)) {
		return [];
	}

	const movedItems = [];

	for (const id of form.children) {
		const item = previousLayoutData.items[id];

		movedItems.push({itemId: item.itemId, parentId: item.parentId});
	}

	return movedItems;
}
