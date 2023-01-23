/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import {ClayButtonWithIcon} from '@clayui/button';
import ClayList from '@clayui/list';
import ClayLoadingIndicator from '@clayui/loading-indicator';
import {ClayPaginationWithBasicItems} from '@clayui/pagination';
import {fetch, openModal, openToast} from 'frontend-js-web';
import React, {useCallback, useEffect, useState} from 'react';

import '../css/ViewUsages.scss';

export default function ViewUsages({getUsagesURL, portletNamespace}) {
	const [activePage, setActivePage] = useState(1);
	const [totalPages, setTotalPages] = useState(0);

	const [items, setItems] = useState([]);
	const [loading, setLoading] = useState(false);

	const onUsagesLoad = useCallback(
		(pageIndex) => {
			setLoading(true);

			const url = new URL(getUsagesURL);

			url.searchParams.set(`${portletNamespace}pageIndex`, pageIndex);

			fetch(url, {
				method: 'GET',
			})
				.then((response) => response.json())
				.then(({totalNumberOfPages, usages}) => {
					setItems(usages);
					setLoading(false);
					setTotalPages(totalNumberOfPages);
				})
				.catch(() => openErrorToast());
		},
		[getUsagesURL, portletNamespace]
	);

	useEffect(() => {
		onUsagesLoad(1);
	}, [onUsagesLoad]);

	if (!items.length) {
		return (
			<p className="text-secondary">
				{Liferay.Language.get('there-are-no-usages')}
			</p>
		);
	}

	return (
		<div className="cadmin">
			<ClayList className="usages-list">
				{loading ? (
					<ClayLoadingIndicator className="my-7" size="sm" />
				) : (
					items.map(({id, name, type, url}) => (
						<ClayList.Item flex key={id}>
							<ClayList.ItemField className="p-0" expand>
								<ClayList.ItemTitle className="mb-2 text-truncate">
									<span title={name}>{name}</span>
								</ClayList.ItemTitle>

								<ClayList.ItemText className="text-secondary">
									{type}
								</ClayList.ItemText>
							</ClayList.ItemField>

							{url && (
								<ClayList.ItemField className="p-0">
									<ClayButtonWithIcon
										aria-label={Liferay.Language.get(
											'view-usage'
										)}
										displayType="secondary"
										onClick={() =>
											openModal({
												title: Liferay.Language.get(
													'preview'
												),
												url,
											})
										}
										size="sm"
										symbol="view"
										title={Liferay.Language.get(
											'view-usage'
										)}
									/>
								</ClayList.ItemField>
							)}
						</ClayList.Item>
					))
				)}
			</ClayList>

			{totalPages > 1 && (
				<ClayPaginationWithBasicItems
					active={activePage}
					ellipsisBuffer={2}
					ellipsisProps={{
						'aria-label': Liferay.Language.get('more'),
						'title': Liferay.Language.get('more'),
					}}
					onActiveChange={(nextPageIndex) => {
						if (nextPageIndex > 0 && nextPageIndex <= totalPages) {
							setActivePage(nextPageIndex);
							onUsagesLoad(nextPageIndex);
						}
					}}
					totalPages={totalPages}
				/>
			)}
		</div>
	);
}

function openErrorToast() {
	openToast({
		message: Liferay.Language.get('an-unexpected-error-occurred'),
		title: Liferay.Language.get('error'),
		type: 'danger',
	});
}
