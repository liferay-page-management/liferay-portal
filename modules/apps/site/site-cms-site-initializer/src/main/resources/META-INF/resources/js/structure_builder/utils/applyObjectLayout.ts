/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	ObjectDefinition,
	ObjectLayoutBox,
	ObjectLayoutTab,
} from '../../common/types/ObjectDefinition';
import buildLocalizedValue from '../../common/utils/buildLocalizedValue';
import {Group, Structure, StructureChild} from '../types/Structure';
import {Uuid} from '../types/Uuid';
import {REPEATABLE_GROUP_NAME_PREFIX} from './buildObjectLayout';
import getUuid from './getUuid';
import isField from './isField';
import {isRepeatableGroup} from './isGroup';
import sortChildren from './state/sortChildren';

export default function applyObjectLayout({
	children,
	objectDefinition,
	parent,
}: {
	children: Structure['children'];
	objectDefinition: ObjectDefinition;
	parent: Uuid;
}): Structure['children'] {
	const [objectLayout] = objectDefinition.objectLayouts ?? [];

	if (!objectLayout) {
		return children;
	}

	const childrenByObjectFieldName = new Map<string, StructureChild>();
	const defaultLanguageId = Liferay.ThemeDisplay.getDefaultLanguageId();
	const generalName = Liferay.Language.get('general');
	const relationshipsByName = new Map<string, StructureChild>();

	for (const child of children.values()) {
		if (isField(child)) {
			childrenByObjectFieldName.set(child.name, child);
		}
		else if (child.type === 'related-content') {
			if (child.objectFieldName) {
				childrenByObjectFieldName.set(child.objectFieldName, child);
			}
		}
		else if (
			child.type === 'referenced-structure' ||
			isRepeatableGroup(child)
		) {
			relationshipsByName.set(child.relationshipName, child);
		}
	}

	const consumed = new Set<Uuid>();

	const consumeBoxItems = (
		objectLayoutBox: ObjectLayoutBox,
		containerUuid: Uuid
	): StructureChild[] => {
		const items: StructureChild[] = [];

		const name = objectLayoutBox.name?.[defaultLanguageId] ?? '';

		if (name.startsWith(REPEATABLE_GROUP_NAME_PREFIX)) {
			const relationshipName = name.substring(
				REPEATABLE_GROUP_NAME_PREFIX.length
			);

			const node = relationshipsByName.get(relationshipName);

			if (node && !consumed.has(node.uuid)) {
				consumed.add(node.uuid);

				items.push({...node, parent: containerUuid});
			}

			return items;
		}

		for (const objectLayoutRow of objectLayoutBox.objectLayoutRows ?? []) {
			for (const objectLayoutColumn of objectLayoutRow.objectLayoutColumns ??
				[]) {
				const child = childrenByObjectFieldName.get(
					objectLayoutColumn.objectFieldName
				);

				if (child && !consumed.has(child.uuid)) {
					consumed.add(child.uuid);

					items.push({...child, parent: containerUuid});
				}
			}
		}

		return items;
	};

	const buildTabChildren = (
		objectLayoutTab: ObjectLayoutTab,
		tabUuid: Uuid
	): StructureChild[] => {
		const tabChildren: StructureChild[] = [];

		for (const objectLayoutBox of objectLayoutTab.objectLayoutBoxes ?? []) {
			const name = objectLayoutBox.name?.[defaultLanguageId] ?? '';

			const isNestedGroup =
				objectLayoutBox.collapsable &&
				!name.startsWith(REPEATABLE_GROUP_NAME_PREFIX);

			if (isNestedGroup) {
				const groupUuid = getUuid();

				const groupChildren = consumeBoxItems(
					objectLayoutBox,
					groupUuid
				);

				const group: Group = {
					children: new Map(
						groupChildren.map((child) => [child.uuid, child])
					),
					isRepeatable: false,
					label: objectLayoutBox.name ?? buildLocalizedValue('group'),
					parent: tabUuid,
					type: 'group',
					uuid: groupUuid,
				};

				tabChildren.push(group);
			}
			else {
				tabChildren.push(...consumeBoxItems(objectLayoutBox, tabUuid));
			}
		}

		return tabChildren;
	};

	const result: StructureChild[] = [];

	const objectLayoutTabs = objectLayout.objectLayoutTabs ?? [];

	for (let i = 0; i < objectLayoutTabs.length; i++) {
		const objectLayoutTab = objectLayoutTabs[i];

		const name = objectLayoutTab.name?.[defaultLanguageId] ?? '';

		if (i === 0 && name === generalName) {
			result.push(...buildTabChildren(objectLayoutTab, parent));

			continue;
		}

		const groupUuid = getUuid();

		const groupChildren = buildTabChildren(objectLayoutTab, groupUuid);

		const group: Group = {
			children: new Map(
				groupChildren.map((child) => [child.uuid, child])
			),
			isRepeatable: false,
			label: objectLayoutTab.name,
			parent,
			type: 'group',
			uuid: groupUuid,
		};

		result.push(group);
	}

	// Keep anything the layout did not reference as a loose child.

	for (const child of children.values()) {
		if (!consumed.has(child.uuid)) {
			result.push({...child, parent});
		}
	}

	return sortChildren(new Map(result.map((child) => [child.uuid, child])));
}
