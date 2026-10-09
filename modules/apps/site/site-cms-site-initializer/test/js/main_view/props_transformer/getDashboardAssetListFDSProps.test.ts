/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import deleteItemAction from '../../../../src/main/resources/META-INF/resources/js/main_view/props_transformer/actions/deleteItemAction';
import getDashboardAssetListFDSProps from '../../../../src/main/resources/META-INF/resources/js/main_view/props_transformer/getDashboardAssetListFDSProps';

jest.mock('@liferay/frontend-data-set-web', () => ({
	EConfigInURLBehavior: {OFF: 'off'},
	replaceTokens: jest.fn(),
}));

jest.mock(
	'../../../../src/main/resources/META-INF/resources/js/main_view/props_transformer/actions/deleteItemAction',
	() => ({__esModule: true, default: jest.fn()})
);

describe('getDashboardAssetListFDSProps', () => {
	it('marks the delete item action with the danger class', () => {
		const [deleteAction, shareAction] = getDashboardAssetListFDSProps({
			additionalProps: {},
			itemsActions: [{data: {id: 'delete'}}, {data: {id: 'share'}}],
		} as any).itemsActions;

		expect(deleteAction.className).toBe('text-danger');
		expect(shareAction.className).toBeUndefined();
	});

	it('notifies the data change when an action reloads the list', async () => {
		const loadData = jest.fn();
		const onDataChange = jest.fn();

		await getDashboardAssetListFDSProps({
			additionalProps: {},
			onDataChange,
		} as any).onActionDropdownItemClick({
			action: {data: {id: 'delete'}},
			itemData: {title: 'Draft'},
			loadData,
		} as any);

		const [, , reloadList] = (deleteItemAction as jest.Mock).mock.calls[0];

		reloadList();

		expect(loadData).toHaveBeenCalledTimes(1);
		expect(onDataChange).toHaveBeenCalledTimes(1);
	});
});
