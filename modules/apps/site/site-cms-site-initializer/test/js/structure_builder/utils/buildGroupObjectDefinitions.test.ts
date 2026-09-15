/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	Group,
	Structure,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import buildGroupObjectDefinitions from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/buildGroupObjectDefinitions';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';

const ROOT_UUID = getUuid();

function repeatableGroup(parent: string): Group {
	return {
		children: new Map(),
		erc: 'repeatable-erc',
		isRepeatable: true,
		label: {en_US: 'Repeatable'},
		name: 'repeatable',
		parent,
		relationshipERC: 'repeatable-rel-erc',
		relationshipName: 'repeatable',
		type: 'group',
		uuid: getUuid(),
	} as Group;
}

describe('buildGroupObjectDefinitions', () => {
	beforeEach(() => {
		jest.spyOn(
			Liferay.ThemeDisplay,
			'getDefaultLanguageId'
		).mockReturnValue('en_US');
	});

	afterEach(() => {
		jest.restoreAllMocks();
	});

	it('Builds the object definition of a repeatable group at the root', () => {
		const group = repeatableGroup(ROOT_UUID);

		const objectDefinitions = buildGroupObjectDefinitions({
			children: new Map([[group.uuid, group]]) as Structure['children'],
			publishedChildren: new Set(),
		});

		expect(
			objectDefinitions.map(
				({externalReferenceCode}) => externalReferenceCode
			)
		).toEqual(['repeatable-erc']);
	});

	it('Builds the object definition of a repeatable group nested in a non-repeatable group, which has no object definition of its own', () => {
		const groupUuid = getUuid();

		const repeatable = repeatableGroup(groupUuid);

		const group = {
			children: new Map([[repeatable.uuid, repeatable]]),
			isRepeatable: false,
			label: {en_US: 'Group'},
			parent: ROOT_UUID,
			type: 'group',
			uuid: groupUuid,
		} as Group;

		const objectDefinitions = buildGroupObjectDefinitions({
			children: new Map([[groupUuid, group]]) as Structure['children'],
			publishedChildren: new Set(),
		});

		expect(
			objectDefinitions.map(
				({externalReferenceCode}) => externalReferenceCode
			)
		).toEqual(['repeatable-erc']);
	});
});
