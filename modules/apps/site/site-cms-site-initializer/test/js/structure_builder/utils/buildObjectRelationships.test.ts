/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	Group,
	RelatedContent,
	Structure,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import buildObjectRelationships from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/buildObjectRelationships';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';

const ROOT_UUID = getUuid();

function relatedContent(parent: Uuid): RelatedContent {
	return {
		erc: 'related-content-erc',
		label: {en_US: 'Related Content'},
		multiselection: false,
		name: 'relatedContent',
		parent,
		relatedStructureERC: 'target-structure-erc',
		type: 'related-content',
		uuid: getUuid(),
	};
}

function children(...items: Array<Group | RelatedContent>) {
	return new Map(
		items.map((item) => [item.uuid, item])
	) as Structure['children'];
}

describe('buildObjectRelationships', () => {
	it('Points the relationship of related content at the root to the structure', () => {
		const objectRelationships = buildObjectRelationships({
			children: children(relatedContent(ROOT_UUID)),
			structureERC: 'root-erc',
		});

		expect(objectRelationships).toEqual([
			{
				deletionType: 'disassociate',
				externalReferenceCode: 'related-content-erc',
				label: {en_US: 'Related Content'},
				name: 'relatedContent',
				objectDefinitionExternalReferenceCode1: 'target-structure-erc',
				objectDefinitionExternalReferenceCode2: 'root-erc',
				type: 'oneToMany',
			},
		]);
	});

	it('Points the relationship of related content inside a non-repeatable group to the structure, since the group has no object definition', () => {
		const groupUuid = getUuid();

		const group = {
			children: children(relatedContent(groupUuid)),
			isRepeatable: false,
			label: {en_US: 'Group'},
			parent: ROOT_UUID,
			type: 'group',
			uuid: groupUuid,
		} as Group;

		const objectRelationships = buildObjectRelationships({
			children: children(group),
			structureERC: 'root-erc',
		});

		expect(
			objectRelationships.map(
				({objectDefinitionExternalReferenceCode2}) =>
					objectDefinitionExternalReferenceCode2
			)
		).toEqual(['root-erc']);
	});

	it('Points the relationship of related content inside a repeatable group to the group, which owns an object definition', () => {
		const groupUuid = getUuid();

		const group = {
			children: children(relatedContent(groupUuid)),
			erc: 'group-erc',
			isRepeatable: true,
			label: {en_US: 'Group'},
			name: 'group',
			parent: ROOT_UUID,
			relationshipERC: 'group-rel-erc',
			relationshipName: 'group',
			type: 'group',
			uuid: groupUuid,
		} as Group;

		const objectRelationships = buildObjectRelationships({
			children: children(group),
			structureERC: 'root-erc',
		});

		expect(
			objectRelationships.map(
				({objectDefinitionExternalReferenceCode2}) =>
					objectDefinitionExternalReferenceCode2
			)
		).toEqual(['group-erc']);
	});
});
