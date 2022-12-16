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

import React from 'react';

import LayoutFinder from './LayoutFinder';
import NavigationMenuItemsTree from './NavigationMenuItemsTree';
import PageTypeSelector from './PageTypeSelector';
import PagesAdministrationLink from './PagesAdministrationLink';
import PagesTree from './PagesTree';

export default function ProductMenuTree({portletNamespace, ...props}) {
	const {
		config,
		hasAdministrationPortletPermission,
		isPrivateLayoutsTree,
		isSiteNavigationMenu,
		items,
		pageTypeOptions,
		pageTypeSelectedOption,
		pageTypeSelectedOptionLabel,
		selectedLayoutId,
		selectedLayoutPath,
		selectedSiteNavigationMenuItemId,
		showAddIcon,
		siteNavigationMenuItems,
	} = props.productMenuTreeData;

	const layoutFinderData = {
		administrationPortletNamespace: config.administrationPortletNamespace,
		administrationPortletURL: config.administrationPortletURL,
		findLayoutsURL: config.findLayoutsURL,
		namespace: portletNamespace,
		productMenuPortletURL: config.productMenuPortletURL,
		viewInPageAdministrationURL: config.viewInPageAdministrationURL,
	};

	const pagesTreeData = {
		config: {
			loadMoreItemsURL: config.loadMoreItemsURL,
			maxPageSize: config.maxPageSize,
			moveItemURL: config.moveItemURL,
			namespace: portletNamespace,
		},
		isPrivateLayoutsTree,
		items,
		selectedLayoutId,
		selectedLayoutPath,
	};

	const pagetypeSelectorData = {
		addCollectionLayoutURL: config.addCollectionLayoutURL,
		addLayoutURL: config.addLayoutURL,
		configureLayoutSetURL: config.configureLayoutSetURL,
		namespace: portletNamespace,
		pageTypeOptions,
		pageTypeSelectedOption,
		pageTypeSelectedOptionLabel,
		pagesTreeURL: config.pagesTreeURL,
		showAddIcon,
	};

	const siteNavigationMenuData = {
		portletNamespace,
		selectedSiteNavigationMenuItemId,
		siteNavigationMenuItems,
	};

	const PagesAdministrationData = {
		administrationPortletURL: config.administrationPortletURL,
		hasAdministrationPortletPermission,
	};

	return (
		<>
			<LayoutFinder {...layoutFinderData} />

			<PageTypeSelector {...pagetypeSelectorData} />

			{isSiteNavigationMenu ? (
				<NavigationMenuItemsTree {...siteNavigationMenuData} />
			) : (
				<PagesTree {...pagesTreeData} />
			)}

			<PagesAdministrationLink {...PagesAdministrationData} />
		</>
	);
}
