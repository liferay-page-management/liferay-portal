/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openToast} from 'frontend-js-components-web';

import {Group} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import handleUngroup from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/handleUngroup';

jest.mock('frontend-js-components-web', () => ({openToast: jest.fn()}));

const ROOT_UUID = getUuid();

function group(isRepeatable: boolean): Group {
	return {
		children: new Map(),
		erc: 'group-erc',
		isRepeatable,
		label: {en_US: 'Group'},
		name: 'group',
		parent: ROOT_UUID,
		relationshipERC: 'group-rel-erc',
		relationshipName: 'group',
		type: 'group',
		uuid: getUuid(),
	} as Group;
}

describe('handleUngroup', () => {
	afterEach(() => {
		(openToast as jest.Mock).mockClear();
	});

	it('ungroups a group that is not repeatable, which only describes the layout', () => {
		const dispatch = jest.fn();
		const target = group(false);

		handleUngroup({
			dispatch,
			group: target,
			publishedChildren: new Set<Uuid>(),
		});

		expect(dispatch).toHaveBeenCalledWith({
			type: 'ungroup',
			uuid: target.uuid,
		});
	});

	it('ungroups a published group that is not repeatable, since no object definition is lost', () => {
		const dispatch = jest.fn();
		const target = group(false);

		handleUngroup({
			dispatch,
			group: target,
			publishedChildren: new Set<Uuid>([target.uuid]),
		});

		expect(dispatch).toHaveBeenCalled();
		expect(openToast).not.toHaveBeenCalled();
	});

	it('ungroups an unpublished repeatable group', () => {
		const dispatch = jest.fn();
		const target = group(true);

		handleUngroup({
			dispatch,
			group: target,
			publishedChildren: new Set<Uuid>(),
		});

		expect(dispatch).toHaveBeenCalledWith({
			type: 'ungroup',
			uuid: target.uuid,
		});
	});

	it('refuses a published repeatable group, which would drop the object definition its children live on', () => {
		const dispatch = jest.fn();
		const target = group(true);

		handleUngroup({
			dispatch,
			group: target,
			publishedChildren: new Set<Uuid>([target.uuid]),
		});

		expect(openToast).toHaveBeenCalledWith({
			message:
				'the-ungroup-action-cannot-be-done-because-this-repeatable-group-is-already-published',
			type: 'danger',
		});
		expect(dispatch).not.toHaveBeenCalled();
	});
});
