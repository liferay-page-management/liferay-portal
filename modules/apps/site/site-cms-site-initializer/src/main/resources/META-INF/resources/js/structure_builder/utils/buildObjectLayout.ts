/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	ObjectLayout,
	ObjectLayoutBox,
	ObjectLayoutTab,
} from '../../common/types/ObjectDefinition';
import buildLocalizedValue from '../../common/utils/buildLocalizedValue';
import {Group, Structure, StructureChild} from '../types/Structure';
import isField from './isField';
import isGroup, {isRepeatableGroup} from './isGroup';

export const REPEATABLE_GROUP_NAME_PREFIX = 'repeatable-group-';

export default function buildObjectLayout(
	structure: Pick<Structure, 'children' | 'erc' | 'label'>
): ObjectLayout | null {
	if (!_hasGroup(structure.children)) {
		return null;
	}

	const objectLayoutTabs: ObjectLayoutTab[] = [];

	const looseItems = Array.from(structure.children.values()).filter(
		(child) => !isGroup(child) || child.isRepeatable
	);

	if (looseItems.length) {
		objectLayoutTabs.push(
			_buildTab(buildLocalizedValue('general'), looseItems)
		);
	}

	for (const child of structure.children.values()) {
		if (isGroup(child) && !child.isRepeatable) {
			objectLayoutTabs.push(
				_buildTab(child.label, Array.from(child.children.values()))
			);
		}
	}

	return {
		defaultObjectLayout: false,
		name: structure.label,
		objectDefinitionExternalReferenceCode: structure.erc,
		objectLayoutTabs: objectLayoutTabs.map((objectLayoutTab, priority) => ({
			...objectLayoutTab,
			priority,
		})),
	};
}

function _buildColumnsBox(
	objectFieldNames: string[],
	collapsable: boolean,
	name?: Liferay.Language.LocalizedValue<string>
): ObjectLayoutBox {
	return {
		collapsable,
		...(name && {name}),
		objectLayoutRows: objectFieldNames.map((objectFieldName, priority) => ({
			objectLayoutColumns: [{objectFieldName, priority: 0, size: 12}],
			priority,
		})),
		type: 'regular',
	};
}

function _getObjectFieldName(item: StructureChild): string | undefined {
	if (isField(item)) {
		return item.name;
	}

	if (item.type === 'related-content') {
		return item.objectFieldName;
	}

	return undefined;
}

function _buildMarkerBox(relationshipName: string): ObjectLayoutBox {
	const marker = `${REPEATABLE_GROUP_NAME_PREFIX}${relationshipName}`;

	return {
		collapsable: false,
		name: {
			[Liferay.ThemeDisplay.getDefaultLanguageId()]: marker,
			[Liferay.ThemeDisplay.getLanguageId()]: marker,
		},
		objectLayoutRows: [],
		type: 'regular',
	};
}

function _buildNestedGroupBox(group: Group): ObjectLayoutBox {
	return _buildColumnsBox(
		Array.from(group.children.values())
			.map(_getObjectFieldName)
			.filter(Boolean) as string[],
		true,
		group.label
	);
}

function _buildTab(
	name: Liferay.Language.LocalizedValue<string>,
	items: StructureChild[]
): ObjectLayoutTab {
	const objectLayoutBoxes: ObjectLayoutBox[] = [];

	let looseObjectFieldNames: string[] = [];

	const flushLooseColumns = () => {
		if (looseObjectFieldNames.length) {
			objectLayoutBoxes.push(
				_buildColumnsBox(looseObjectFieldNames, false)
			);

			looseObjectFieldNames = [];
		}
	};

	for (const item of items) {
		const objectFieldName = _getObjectFieldName(item);

		if (objectFieldName) {
			looseObjectFieldNames.push(objectFieldName);
		}
		else if (isGroup(item) && !item.isRepeatable) {
			flushLooseColumns();

			objectLayoutBoxes.push(_buildNestedGroupBox(item));

			for (const groupChild of item.children.values()) {
				objectLayoutBoxes.push(..._relationshipMarkerBoxes(groupChild));
			}
		}
		else {
			flushLooseColumns();

			objectLayoutBoxes.push(..._relationshipMarkerBoxes(item));
		}
	}

	flushLooseColumns();

	return {
		name,
		objectLayoutBoxes: objectLayoutBoxes.map(
			(objectLayoutBox, priority) => ({...objectLayoutBox, priority})
		),
	};
}

function _hasGroup(children: Structure['children']): boolean {
	return Array.from(children.values()).some(
		(child) => isGroup(child) && !child.isRepeatable
	);
}

function _relationshipMarkerBoxes(item: StructureChild): ObjectLayoutBox[] {
	if (isRepeatableGroup(item) || item.type === 'referenced-structure') {
		return [_buildMarkerBox(item.relationshipName)];
	}

	return [];
}
