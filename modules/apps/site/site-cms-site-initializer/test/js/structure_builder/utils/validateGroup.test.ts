/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	Group,
	StructureChild,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import {Field} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import {validateGroup} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/validation';

const ROOT_UUID = getUuid();

function field(parent: Uuid): Field {
	return {
		erc: 'sku-erc',
		indexableConfig: {indexed: false},
		label: {en_US: 'SKU'},
		localized: false,
		locked: false,
		name: 'sku',
		parent,
		required: false,
		settings: {},
		type: 'text',
		uuid: getUuid(),
	};
}

function group(children: StructureChild[] = []): Group {
	const uuid = getUuid();

	return {
		children: new Map(children.map((child) => [child.uuid, child])),
		isRepeatable: false,
		label: {en_US: 'Nuovo'},
		parent: ROOT_UUID,
		type: 'group',
		uuid,
	} as Group;
}

describe('validateGroup', () => {
	it('reports an empty group only when publishing, so a draft can be saved while it is still being filled', () => {
		const empty = group();

		expect(validateGroup({data: empty}).get('global')).toBeUndefined();
		expect(
			validateGroup({data: empty, isPublishing: true}).get('global')
		).toBe('group-without-fields');
	});

	it('accepts a group that holds a field', () => {
		const uuid = getUuid();

		const target = group();

		target.children.set(uuid, {...field(target.uuid), uuid});

		expect(
			validateGroup({data: target, isPublishing: true}).get('global')
		).toBeUndefined();
	});

	it('accepts a group whose field sits in a nested group', () => {
		const outer = group();
		const inner = group();

		inner.children.set('x' as Uuid, field(inner.uuid));
		outer.children.set(inner.uuid, inner);

		expect(
			validateGroup({data: outer, isPublishing: true}).get('global')
		).toBeUndefined();
	});

	it('does not count a referenced structure or related content as a field', () => {
		const target = group([
			{
				erc: 'related-erc',
				label: {en_US: 'Related'},
				multiselection: false,
				name: 'related',
				parent: ROOT_UUID,
				relatedStructureERC: '',
				type: 'related-content',
				uuid: getUuid(),
			} as StructureChild,
		]);

		expect(
			validateGroup({data: target, isPublishing: true}).get('global')
		).toBe('group-without-fields');
	});

	it('drops a stale empty-group error once the group holds a field, so re-publishing recovers', () => {
		const target = group();

		target.children.set('x' as Uuid, field(target.uuid));

		const currentErrors = new Map([
			['global', 'group-without-fields'],
		]) as Parameters<typeof validateGroup>[0]['currentErrors'];

		const errors = validateGroup({
			currentErrors,
			data: target,
			isPublishing: true,
		});

		expect(errors.get('global')).toBeUndefined();
	});
});
