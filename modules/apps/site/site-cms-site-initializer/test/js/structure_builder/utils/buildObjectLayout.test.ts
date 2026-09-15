/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	Group,
	RelatedContent,
	RepeatableGroup,
	Structure,
	StructureChild,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import buildObjectLayout, {
	REPEATABLE_GROUP_NAME_PREFIX,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/buildObjectLayout';
import {Field} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';

const ROOT_UUID = getUuid();

function field(name: string): Field {
	return {
		erc: `${name}-erc`,
		indexableConfig: {indexed: false},
		label: {en_US: name},
		localized: false,
		locked: false,
		name,
		parent: ROOT_UUID,
		required: false,
		settings: {},
		type: 'text',
		uuid: getUuid(),
	};
}

// A group at the root serializes as a tab, a group nested inside a group as a
// collapsable box: the position decides, nothing is stored on the node.

function group(label: string, children: StructureChild[]): Group {
	return {
		children: toMap(children),
		isRepeatable: false,
		label: {en_US: label},
		parent: ROOT_UUID,
		type: 'group',
		uuid: getUuid(),
	};
}

function relatedContent(
	name: string,
	objectFieldName?: string
): RelatedContent {
	return {
		erc: `${name}-erc`,
		label: {en_US: name},
		multiselection: false,
		name,
		objectFieldName,
		parent: ROOT_UUID,
		relatedStructureERC: 'related-erc',
		type: 'related-content',
		uuid: getUuid(),
	};
}

function repeatableGroup(relationshipName: string): RepeatableGroup {
	return {
		children: new Map(),
		erc: `${relationshipName}-erc`,
		isRepeatable: true,
		label: {en_US: relationshipName},
		name: relationshipName,
		parent: ROOT_UUID,
		relationshipERC: `${relationshipName}-rel-erc`,
		relationshipName,
		type: 'group',
		uuid: getUuid(),
	};
}

function structure(children: StructureChild[]): Structure {
	return {
		children: toMap(children),
		erc: 'product-erc',
		label: {en_US: 'Product'},
		name: 'Product',
		path: '',
		slug: '',
		spaces: 'all',
		status: 'draft',
		system: false,
		type: 'L_CMS_CONTENT_STRUCTURES',
		uuid: ROOT_UUID,
		workflows: {},
	};
}

function toMap(children: StructureChild[]): Map<Uuid, StructureChild> {
	return new Map(children.map((child) => [child.uuid, child]));
}

describe('buildObjectLayout', () => {
	beforeEach(() => {
		jest.spyOn(
			Liferay.ThemeDisplay,
			'getDefaultLanguageId'
		).mockReturnValue('en_US');
		jest.spyOn(Liferay.ThemeDisplay, 'getLanguageId').mockReturnValue(
			'en_US'
		);
	});

	afterEach(() => {
		jest.restoreAllMocks();
	});

	it('returns null when the structure has no group', () => {
		expect(
			buildObjectLayout(structure([field('title'), field('sku')]))
		).toBeNull();
	});

	it('gathers loose items into an implicit first general tab and serializes a root group as a tab', () => {
		expect(
			buildObjectLayout(
				structure([field('title'), group('Details', [field('sku')])])
			)
		).toMatchSnapshot();
	});

	it('serializes a nested group as a collapsable box inside its tab', () => {
		expect(
			buildObjectLayout(
				structure([
					field('title'),
					group('Specs', [
						group('Dimensions', [field('width'), field('height')]),
						field('weight'),
					]),
				])
			)
		).toMatchSnapshot();
	});

	it('serializes a repeatable group as a repeatable-group marker box', () => {
		expect(
			buildObjectLayout(
				structure([
					field('title'),
					group('Variants', [repeatableGroup('skuVariants')]),
				])
			)
		).toMatchSnapshot();
	});

	it('emits related content as a regular column, keyed by its object field name', () => {
		const objectLayout = buildObjectLayout(
			structure([
				relatedContent('image', 'imageId'),
				group('Details', [field('sku')]),
			])
		)!;

		expect(objectLayout.objectLayoutTabs[0].objectLayoutBoxes).toEqual([
			{
				collapsable: false,
				objectLayoutRows: [
					{
						objectLayoutColumns: [
							{
								objectFieldName: 'imageId',
								priority: 0,
								size: 12,
							},
						],
						priority: 0,
					},
				],
				priority: 0,
				type: 'regular',
			},
		]);
	});

	it('leaves related content without an object field name out of the layout', () => {
		const objectLayout = buildObjectLayout(
			structure([
				relatedContent('image'),
				group('Details', [field('sku')]),
			])
		)!;

		expect(objectLayout.objectLayoutTabs[0].objectLayoutBoxes).toEqual([]);
	});

	it('exposes the marker prefix the form renderer reads', () => {
		expect(REPEATABLE_GROUP_NAME_PREFIX).toBe('repeatable-group-');
	});
});
