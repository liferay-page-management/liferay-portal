/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	openConfirmModal,
	openOptionsModal,
} from '@liferay/layout-js-components-web';
import {openToast} from 'frontend-js-components-web';
import {sub} from 'frontend-js-web';
import {Dispatch} from 'react';

import getLocalizedValue from '../../common/utils/getLocalizedValue';
import {Action, State} from '../contexts/StateContext';
import {Group, Structure, StructureChild} from '../types/Structure';
import {Uuid} from '../types/Uuid';
import findAvailableFieldName from './findAvailableFieldName';
import findChild from './findChild';
import getGroupDepth, {MAXIMUM_GROUP_DEPTH} from './getGroupDepth';
import getGroupLevels from './getGroupLevels';
import getUndeletableChildren, {
	UndeletableReason,
} from './getUndeletableChildren';
import hasName from './hasName';
import isGroup, {isRepeatableGroup} from './isGroup';
import isInsideRepeatableGroup from './isInsideRepeatableGroup';
import isReferenced from './isReferenced';

export default async function handleMoveChildren({
	deletedChildren,
	dispatch,
	publishedChildren,
	structure,
	targetUuid,
	uuids,
}: {
	deletedChildren: State['history']['deletedChildren'];
	dispatch: Dispatch<Action>;
	publishedChildren: State['publishedChildren'];
	structure: Structure;
	targetUuid: Uuid;
	uuids: Uuid[];
}) {
	const movingPublished = uuids.some(
		(uuid) =>
			!isReferenced({root: structure, uuid}) &&
			publishedChildren.has(uuid)
	);

	if (movingPublished) {
		const confirm = await openConfirmModal({
			buttonLabel: Liferay.Language.get('move'),
			center: true,
			optOutConfig: {
				sessionKey: 'disableChildrenMoveModal',
			},
			status: 'warning',
			text: Liferay.Language.get(
				'moving-fields-may-impact-existing-stored-data-after-publishing-the-structure.-are-you-sure-you-want-to-continue'
			),
			title: Liferay.Language.get('move-field'),
		});

		if (!confirm) {
			return;
		}
	}

	const undeletables = getUndeletableChildren(uuids, structure);

	const items = uuids.map((uuid) => findChild({root: structure, uuid})!);

	const targetChild =
		targetUuid === structure.uuid
			? undefined
			: (findChild({
					root: structure,
					uuid: targetUuid,
				}) as Group);

	const isLayoutContainer = !targetChild || !isRepeatableGroup(targetChild);
	const target: Group | Structure = targetChild ?? structure;

	const targetDepth = getGroupDepth({structure, uuid: targetUuid});
	const targetInsideRepeatableGroup = isInsideRepeatableGroup({
		structure,
		uuid: targetUuid,
	});

	let blockedByNesting = false;

	let movableItems = items
		.filter(({parent, uuid}) => {
			if (parent === targetUuid) {
				return false;
			}

			const item = findChild({root: structure, uuid})!;

			if (
				isGroup(item) &&
				(targetInsideRepeatableGroup ||
					targetDepth + getGroupLevels(item) > MAXIMUM_GROUP_DEPTH)
			) {
				blockedByNesting = true;

				return false;
			}

			if (isLayoutContainer) {
				const reason = undeletables.get(uuid);

				return (
					reason !== 'is-referenced' &&
					reason !== 'causes-invalid-group'
				);
			}

			return !undeletables.has(uuid);
		})
		.map((item) => ({...item, parent: targetUuid}));

	const movedUuids = new Set(movableItems.map(({uuid}) => uuid));

	const blockedReasons: UndeletableReason[] = [];

	for (const [uuid, reason] of undeletables) {
		if (!movedUuids.has(uuid)) {
			blockedReasons.push(reason);
		}
	}

	if (blockedByNesting) {
		openToast({
			message: Liferay.Language.get(
				'some-items-could-not-be-moved-because-a-group-cannot-be-nested-there'
			),
			type: 'danger',
		});
	}

	if (!movableItems.length) {
		showWarnings(blockedReasons);

		return;
	}

	if (hasNameConflict(movableItems, target)) {
		const onNameConflict = await openOptionsModal({
			defaultValue: 'rename',
			options: [
				{
					label: Liferay.Language.get('do-not-move'),
					value: 'do-not-move',
				},
				{
					label: Liferay.Language.get('keep-both-and-rename'),
					value: 'rename',
				},
			],
			text: sub(
				Liferay.Language.get(
					'one-or-more-fields-have-field-names-that-already-exist-in-the-location-x.-what-action-do-you-want-to-take'
				),
				getLocalizedValue(target.label)
			),
			title: Liferay.Language.get('move-options'),
		});

		if (!onNameConflict) {
			showWarnings(blockedReasons);

			return;
		}

		if (onNameConflict === 'rename') {
			movableItems = movableItems.map((item) =>
				hasName(item)
					? {
							...item,
							name: findAvailableFieldName(
								target.children,
								deletedChildren,
								item.name
							),
						}
					: item
			);
		}
		else if (onNameConflict === 'do-not-move') {
			movableItems = movableItems.filter(
				(item) =>
					!hasName(item) ||
					!Array.from(target.children.values()).some(
						(child) => hasName(child) && child.name === item.name
					)
			);
		}

		if (!movableItems.length) {
			showWarnings(blockedReasons);

			return;
		}
	}

	showWarnings(blockedReasons);

	dispatch({
		items: movableItems,
		targetUuid,
		type: 'move-children',
	});
}

function hasNameConflict(
	movableItems: StructureChild[],
	target: Group | Structure
): boolean {
	return movableItems.some(
		(item) =>
			hasName(item) &&
			Array.from(target.children.values()).some(
				(child) => hasName(child) && child.name === item.name
			)
	);
}

function showWarnings(reasons: UndeletableReason[]) {
	if (reasons.includes('causes-invalid-group')) {
		openToast({
			message: Liferay.Language.get(
				'some-fields-could-not-be-moved-because-at-least-one-field-is-required-in-a-repeatable-group'
			),
			type: 'danger',
		});
	}
	else if (
		reasons.includes('is-locked') ||
		reasons.includes('is-referenced')
	) {
		openToast({
			message: Liferay.Language.get(
				'some-items-could-not-be-moved-because-they-are-system-fields-or-they-belong-to-a-referenced-content-structure'
			),
			type: 'danger',
		});
	}
}
