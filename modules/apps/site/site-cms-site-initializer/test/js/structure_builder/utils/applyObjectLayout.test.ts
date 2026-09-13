/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	ObjectDefinition,
	ObjectLayout,
} from '../../../../src/main/resources/META-INF/resources/js/common/types/ObjectDefinition';
import {
	Group,
	RelatedContent,
	RepeatableGroup,
	Structure,
	StructureChild,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import applyObjectLayout from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/applyObjectLayout';
import buildObjectLayout from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/buildObjectLayout';
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

function toArray(children: Map<Uuid, StructureChild>): StructureChild[] {
	return Array.from(children.values());
}

function toMap(children: StructureChild[]): Map<Uuid, StructureChild> {
	return new Map(children.map((child) => [child.uuid, child]));
}

function toObjectDefinition(objectLayout: ObjectLayout): ObjectDefinition {
	return {
		objectLayouts: [{...objectLayout, defaultObjectLayout: true}],
	} as ObjectDefinition;
}

describe('applyObjectLayout', () => {
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

	it('unwraps the general tab into loose children and rebuilds every other tab as a root group', () => {
		const title = field('title');
		const sku = field('sku');

		const objectLayout = buildObjectLayout(
			structure([title, group('Details', [sku])])
		)!;

		const children = toArray(
			applyObjectLayout({
				children: toMap([title, sku]),
				objectDefinition: toObjectDefinition(objectLayout),
				parent: ROOT_UUID,
			})
		);

		const looseField = children.find((child) => child.type === 'text');
		const detailsGroup = children.find(
			(child) => child.type === 'group'
		) as Group;

		expect(looseField).toMatchObject({name: 'title', parent: ROOT_UUID});
		expect(detailsGroup.label).toEqual({en_US: 'Details'});

		const groupChildren = toArray(detailsGroup.children);

		expect(groupChildren).toHaveLength(1);
		expect(groupChildren[0]).toMatchObject({
			name: 'sku',
			parent: detailsGroup.uuid,
		});
	});

	it('rebuilds a collapsable box as a group nested inside its root group', () => {
		const title = field('title');
		const width = field('width');

		const objectLayout = buildObjectLayout(
			structure([title, group('Specs', [group('Dimensions', [width])])])
		)!;

		const children = toArray(
			applyObjectLayout({
				children: toMap([title, width]),
				objectDefinition: toObjectDefinition(objectLayout),
				parent: ROOT_UUID,
			})
		);

		const specsGroup = children.find(
			(child) => child.type === 'group'
		) as Group;
		const nestedGroup = toArray(specsGroup.children).find(
			(child) => child.type === 'group'
		) as Group;

		expect(nestedGroup.label).toEqual({en_US: 'Dimensions'});
		expect(nestedGroup.parent).toBe(specsGroup.uuid);

		const nestedChildren = toArray(nestedGroup.children);

		expect(nestedChildren).toHaveLength(1);
		expect(nestedChildren[0]).toMatchObject({
			name: 'width',
			parent: nestedGroup.uuid,
		});
	});

	it('resolves a repeatable-group marker box back to its repeatable group node', () => {
		const title = field('title');
		const variants = repeatableGroup('skuVariants');

		const objectLayout = buildObjectLayout(
			structure([title, group('Variants', [variants])])
		)!;

		const children = toArray(
			applyObjectLayout({
				children: toMap([title, variants]),
				objectDefinition: toObjectDefinition(objectLayout),
				parent: ROOT_UUID,
			})
		);

		const variantsGroup = children.find(
			(child) => child.type === 'group'
		) as Group;

		const rebuiltGroup = toArray(
			variantsGroup.children
		)[0] as RepeatableGroup;

		expect(rebuiltGroup.isRepeatable).toBe(true);
		expect(rebuiltGroup.relationshipName).toBe('skuVariants');
		expect(rebuiltGroup.parent).toBe(variantsGroup.uuid);
	});

	it('resolves a related content column back to its related content node', () => {
		const title = field('title');
		const image = relatedContent('image', 'imageId');

		const objectLayout = buildObjectLayout(
			structure([title, group('Media', [image])])
		)!;

		const children = toArray(
			applyObjectLayout({
				children: toMap([title, image]),
				objectDefinition: toObjectDefinition(objectLayout),
				parent: ROOT_UUID,
			})
		);

		const mediaGroup = children.find(
			(child) => child.type === 'group'
		) as Group;

		const rebuiltRelatedContent = toArray(mediaGroup.children)[0];

		expect(rebuiltRelatedContent.type).toBe('related-content');
		expect(rebuiltRelatedContent.parent).toBe(mediaGroup.uuid);
	});

	it('keeps children the layout does not reference as loose fields', () => {
		const title = field('title');
		const sku = field('sku');
		const orphan = field('orphan');

		const objectLayout = buildObjectLayout(
			structure([title, group('Details', [sku])])
		)!;

		const children = toArray(
			applyObjectLayout({
				children: toMap([title, sku, orphan]),
				objectDefinition: toObjectDefinition(objectLayout),
				parent: ROOT_UUID,
			})
		);

		const orphanField = children.find(
			(child) =>
				child.type === 'text' && (child as Field).name === 'orphan'
		);

		expect(orphanField).toMatchObject({name: 'orphan', parent: ROOT_UUID});
	});

	it('round-trips a grouped structure (build then apply then build is stable)', () => {
		const title = field('title');
		const sku = field('sku');
		const width = field('width');
		const variants = repeatableGroup('variants');

		const authored = structure([
			title,
			group('Specs', [group('Dimensions', [width]), sku]),
			group('Variants', [variants]),
		]);

		const objectLayout = buildObjectLayout(authored)!;

		const children = applyObjectLayout({
			children: toMap([title, sku, width, variants]),
			objectDefinition: toObjectDefinition(objectLayout),
			parent: ROOT_UUID,
		});

		const reserialized = buildObjectLayout({...authored, children});

		expect(reserialized).toEqual(objectLayout);
	});
});
