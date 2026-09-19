/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// eslint-disable-next-line @liferay/portal/no-cross-module-deep-import
import {checkAccessibility} from '@liferay/layout-js-components-web/test/__lib__/index';
import {fireEvent, render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import fetch from 'jest-fetch-mock';
import React from 'react';

import '@testing-library/jest-dom';

import SelectLayout from '../../src/main/resources/META-INF/resources/js/select_layout/SelectLayout';

jest.mock('frontend-js-web', () => ({
	...jest.requireActual('frontend-js-web'),
	getOpener: jest.fn(() => ({Liferay: {fire: jest.fn()}})),
}));

const HOME = {
	externalReferenceCode: 'home-erc',
	groupId: 20127,
	hasChildren: false,
	hasGuestViewPermission: true,
	icon: 'page',
	id: 'home-uuid',
	layoutId: 1,
	name: 'Home',
	payload: '{"layoutId":1}',
	privateLayout: false,
	returnType: 'URL',
	url: '/home',
	value: 'Home',
};

const PRODUCTS = {
	...HOME,
	externalReferenceCode: 'products-erc',
	hasChildren: true,
	hasGuestViewPermission: false,
	id: 'products-uuid',
	layoutId: 2,
	name: 'Products',
	paginated: false,
	url: '/products',
	value: 'Products',
};

const PHONES = {
	...HOME,
	externalReferenceCode: 'phones-erc',
	id: 'phones-uuid',
	layoutId: 3,
	name: 'Phones',
	url: '/phones',
	value: 'Products > Phones',
};

const NODES = [
	{
		children: [HOME, PRODUCTS],
		disabled: true,
		expanded: true,
		hasChildren: true,
		icon: 'home',
		id: '0',
		name: 'Liferay',
		paginated: false,
	},
];

function renderSelectLayout(props = {}) {
	return render(
		<SelectLayout
			checkDisplayPage={false}
			config={{
				findLayoutsURL: '/portal/find_layouts',
				loadMoreItemsURL: '/portal/get_layouts',
				maxPageSize: 20,
			}}
			groupId={20127}
			itemSelectorReturnType="URL"
			itemSelectorSaveEvent="selectLayout"
			multiSelection={false}
			nodes={NODES}
			privateLayout={false}
			{...props}
		/>
	);
}

describe('SelectLayout', () => {
	beforeEach(() => {
		jest.clearAllMocks();

		fetch.mockResponse(async (request) => {
			if (request.url.includes('find_layouts')) {
				return {
					body: JSON.stringify({
						hasMoreElements: false,
						layouts: [{...PHONES, path: ['Liferay', 'Products']}],
					}),
				};
			}

			return {
				body: JSON.stringify({hasMoreElements: false, items: [PHONES]}),
			};
		});
	});

	it('renders the preloaded pages under the site', async () => {
		renderSelectLayout();

		expect(await screen.findByText('Home')).toBeInTheDocument();

		expect(screen.getByText('Products')).toBeInTheDocument();
		expect(screen.getByTitle('restricted-page')).toBeInTheDocument();

		expect(fetch).not.toHaveBeenCalled();
	});

	it('fires the item selector event with the clicked page', async () => {
		renderSelectLayout();

		await userEvent.click(await screen.findByText('Home'));

		expect(Liferay.fire).toHaveBeenCalledWith('selectLayout', {
			data: expect.objectContaining({
				id: 'home-uuid',
				layoutId: 1,
				name: 'Home',
				title: 'Home',
			}),
		});
	});

	it('loads the children of a page on demand', async () => {
		renderSelectLayout();

		const productsTreeItem = (await screen.findByText('Products')).closest(
			'[role="treeitem"]'
		);

		fireEvent.click(productsTreeItem.querySelector('.component-expander'));

		expect(await screen.findByText('Phones')).toBeInTheDocument();

		const requestBody = String(fetch.mock.calls[0][1].body);

		expect(requestBody).toContain('layoutUuid=products-uuid');
		expect(requestBody).toContain('parentLayoutId=2');
	});

	it('does not fire the item selector event for the preselected pages', async () => {
		renderSelectLayout({
			multiSelection: true,
			selectedLayoutIds: ['home-uuid'],
		});

		expect(
			await screen.findByRole('checkbox', {name: 'Home'})
		).toBeChecked();

		expect(Liferay.fire).not.toHaveBeenCalled();
	});

	it('fires every selected page in the multiple selection mode', async () => {
		renderSelectLayout({multiSelection: true});

		await userEvent.click(
			await screen.findByRole('checkbox', {name: 'Home'})
		);

		await waitFor(() =>
			expect(Liferay.fire).toHaveBeenCalledWith('selectLayout', {
				data: [expect.objectContaining({id: 'home-uuid'})],
			})
		);
	});

	it('reports the children loaded under a shift selected page', async () => {
		renderSelectLayout({multiSelection: true});

		fireEvent.click(
			await screen.findByRole('checkbox', {name: 'Products'}),
			{shiftKey: true}
		);

		await waitFor(() =>
			expect(Liferay.fire).toHaveBeenLastCalledWith('selectLayout', {
				data: [expect.objectContaining({id: 'products-uuid'})],
			})
		);

		const productsTreeItem = screen
			.getByText('Products')
			.closest('[role="treeitem"]');

		fireEvent.click(productsTreeItem.querySelector('.component-expander'));

		await screen.findByText('Phones');

		await waitFor(() =>
			expect(Liferay.fire).toHaveBeenLastCalledWith('selectLayout', {
				data: [
					expect.objectContaining({id: 'products-uuid'}),
					expect.objectContaining({id: 'phones-uuid'}),
				],
			})
		);
	});

	it('keeps a loaded child selected when it appears in the search results', async () => {
		renderSelectLayout({multiSelection: true});

		fireEvent.click(
			await screen.findByRole('checkbox', {name: 'Products'}),
			{shiftKey: true}
		);

		const productsTreeItem = screen
			.getByText('Products')
			.closest('[role="treeitem"]');

		fireEvent.click(productsTreeItem.querySelector('.component-expander'));

		await screen.findByText('Phones');

		await waitFor(() =>
			expect(Liferay.fire).toHaveBeenLastCalledWith('selectLayout', {
				data: [
					expect.objectContaining({id: 'products-uuid'}),
					expect.objectContaining({id: 'phones-uuid'}),
				],
			})
		);

		await userEvent.type(screen.getByRole('textbox'), 'Pho');

		await screen.findByRole('checkbox', {name: 'Phones'});

		await waitFor(() =>
			expect(Liferay.fire).toHaveBeenLastCalledWith('selectLayout', {
				data: [
					expect.objectContaining({id: 'products-uuid'}),
					expect.objectContaining({id: 'phones-uuid'}),
				],
			})
		);
	});

	it('offers the search results as buttons in the single selection mode', async () => {
		renderSelectLayout();

		await screen.findByText('Home');

		await userEvent.type(screen.getByRole('textbox'), 'Pho');

		await userEvent.click(
			await screen.findByRole('button', {name: /Phones/})
		);

		expect(Liferay.fire).toHaveBeenCalledWith('selectLayout', {
			data: expect.objectContaining({id: 'phones-uuid'}),
		});
	});

	it('has no accessibility violations', async () => {
		const {container} = renderSelectLayout();

		await screen.findByText('Home');

		await checkAccessibility({
			bestPractices: true,
			context: {
				exclude: ['.component-expander', '[aria-owns]'],
				include: [container],
			},
		});
	});
});
