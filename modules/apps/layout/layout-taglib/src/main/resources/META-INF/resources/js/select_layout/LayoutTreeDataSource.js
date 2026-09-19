/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {fetch} from 'frontend-js-web';

export default class LayoutTreeDataSource {
	constructor({
		checkDisplayPage,
		findLayoutsURL,
		groupId,
		itemSelectorReturnType,
		loadMoreItemsURL,
		maxPageSize,
		nodes,
		privateLayout,
	}) {
		this._checkDisplayPage = checkDisplayPage;
		this._findLayoutsURL = findLayoutsURL;
		this._groupId = groupId;
		this._itemSelectorReturnType = itemSelectorReturnType;
		this._loadMoreItemsURL = loadMoreItemsURL;
		this._maxPageSize = maxPageSize;
		this._nodes = nodes;
		this._preloadedChildrenById = new Map();
		this._privateLayout = privateLayout;

		const registerPreloadedChildren = (node) => {
			if (node.children) {
				this._preloadedChildrenById.set(node.id, node.children);

				node.children.forEach(registerPreloadedChildren);
			}
		};

		nodes.forEach(registerPreloadedChildren);
	}

	async getChildren(parentPageTreePickerItem, page) {
		if (!parentPageTreePickerItem) {
			return {
				items: this._nodes.map((node) => toItem(node)),
				totalCount: this._nodes.length,
			};
		}

		const preloadedChildren = this._preloadedChildrenById.get(
			parentPageTreePickerItem.id
		);

		if (page === 1 && preloadedChildren) {
			return {
				items: preloadedChildren.map((node) => toItem(node)),
				totalCount: parentPageTreePickerItem.page.paginated
					? preloadedChildren.length + 1
					: preloadedChildren.length,
			};
		}

		const {hasMoreElements, items} = await this._fetch(
			this._loadMoreItemsURL,
			{
				layoutUuid: parentPageTreePickerItem.id,
				parentLayoutId: parentPageTreePickerItem.page.layoutId,
				privateLayout: this._privateLayout,
				redirect: window.location.pathname + window.location.search,
				start: (page - 1) * this._maxPageSize,
			}
		);

		return {
			items: items.map((node) => toItem(node)),
			totalCount:
				(page - 1) * this._maxPageSize +
				items.length +
				(hasMoreElements ? 1 : 0),
		};
	}

	async search(query, page) {
		const {hasMoreElements, layouts} = await this._fetch(
			this._findLayoutsURL,
			{
				keywords: query,
				searchOnlyByTitle: true,
				start: (page - 1) * this._maxPageSize,
			}
		);

		return {
			items: layouts.map((node) => toItem(node)),
			totalCount:
				(page - 1) * this._maxPageSize +
				layouts.length +
				(hasMoreElements ? 1 : 0),
		};
	}

	async _fetch(url, parameters) {
		const response = await fetch(url, {
			body: new URLSearchParams({
				checkDisplayPage: this._checkDisplayPage,
				groupId: this._groupId,
				itemSelectorReturnType: this._itemSelectorReturnType,
				...parameters,
			}),
			method: 'post',
		});

		if (!response.ok) {
			throw new Error(
				`Request to ${url} failed with status ${response.status}`
			);
		}

		return response.json();
	}
}

function toItem(node) {
	return {
		badge:
			node.hasGuestViewPermission === false
				? {
						label: Liferay.Language.get('restricted-page'),
						symbol: 'password-policies',
					}
				: undefined,
		disabled: Boolean(node.disabled),
		hasChildren: Boolean(node.hasChildren),
		icon: node.icon,
		id: node.id,
		label: node.name,
		page: node,
		path: node.path,
		title: node.url,
	};
}
