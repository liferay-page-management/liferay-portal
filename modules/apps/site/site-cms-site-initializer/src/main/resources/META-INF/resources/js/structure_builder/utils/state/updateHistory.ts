/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {State} from '../../contexts/StateContext';
import {Structure} from '../../types/Structure';
import {Uuid} from '../../types/Uuid';
import findChild from '../findChild';
import getRelationshipStructureERC from '../getRelationshipStructureERC';
import {isRepeatableGroup} from '../isGroup';

export default function updateHistory({
	deletedChildrenUuids,
	initialHistory,
	savedChildren,
	structure,
}: {
	deletedChildrenUuids: Set<Uuid>;
	initialHistory: State['history'];
	savedChildren: State['savedChildren'];
	structure: Structure;
}) {
	let nextHistory = {...initialHistory};

	for (const deletedChildUuid of deletedChildrenUuids) {
		const child = findChild({
			root: structure,
			uuid: deletedChildUuid,
		});

		if (!child) {
			continue;
		}

		if (savedChildren.has(deletedChildUuid)) {
			nextHistory = {
				...nextHistory,
				deletedChildren: [...nextHistory.deletedChildren, child],
			};

			if (
				isRepeatableGroup(child) ||
				child.type === 'related-content' ||
				child.type === 'referenced-structure'
			) {
				let structureERC = getRelationshipStructureERC({
					structure,
					uuid: child.parent,
				});

				if (child.type === 'related-content' && !child.multiselection) {
					structureERC = child.relatedStructureERC;
				}

				nextHistory = {
					...nextHistory,
					deletedRelationships: [
						...nextHistory.deletedRelationships,
						{
							relationshipERC:
								child.type === 'related-content'
									? child.erc
									: child.relationshipERC!,
							structureERC,
						},
					],
				};
			}

			if (isRepeatableGroup(child)) {
				nextHistory = {
					...nextHistory,
					deletedGroupERCs: [
						...nextHistory.deletedGroupERCs,
						child.erc,
					],
				};
			}
		}
	}

	return nextHistory;
}
