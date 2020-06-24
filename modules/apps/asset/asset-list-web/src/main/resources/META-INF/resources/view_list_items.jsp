<%--
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
--%>

<%@ include file="/init.jsp" %>

<%
ListItemsActionDropdownItems listItemsActionDropdownItems = (ListItemsActionDropdownItems)request.getAttribute(AssetListWebKeys.LIST_ITEMS_ACTION_DROPDOWN_ITEMS);
ListItemsDisplayContext listItemsDisplayContext = (ListItemsDisplayContext)request.getAttribute(AssetListWebKeys.LIST_ITEMS_DISPLAY_CONTEXT);
%>

<clay:container-fluid
	cssClass="container-view"
>
	<liferay-ui:search-container
		id="listItemFieldEntries"
		searchContainer="<%= listItemsDisplayContext.getSearchContainer() %>"
	>
		<liferay-ui:search-container-row
			className="com.liferay.asset.list.web.internal.field.ListItemField"
			modelVar="listItemField"
		>
			<liferay-ui:search-container-column-text
				name="title"
				value="<%= HtmlUtil.escape(listItemField.getTitle()) %>"
			/>

			<liferay-ui:search-container-column-text
				name="type"
				value="<%= listItemsDisplayContext.getListItemFieldType(listItemField) %>"
			/>

			<liferay-ui:search-container-column-text
				name="author"
				value="<%= listItemField.getAuthor() %>"
			/>

			<liferay-ui:search-container-column-date
				name="modified-date"
				value="<%= listItemField.getModifiedDate() %>"
			/>

			<liferay-ui:search-container-column-date
				name="create-date"
				value="<%= listItemField.getCreateDate() %>"
			/>

			<c:if test="<%= listItemsDisplayContext.isShowActions() %>">
				<liferay-ui:search-container-column-text>
					<clay:dropdown-actions
						defaultEventHandler="<%= AssetListWebKeys.LIST_ITEMS_DROPDOWN_DEFAULT_EVENT_HANDLER %>"
						dropdownItems="<%= listItemsActionDropdownItems.getActionDropdownItems(listItemField) %>"
					/>
				</liferay-ui:search-container-column-text>
			</c:if>
		</liferay-ui:search-container-row>

		<liferay-ui:search-iterator
			markupView="lexicon"
		/>
	</liferay-ui:search-container>
</clay:container-fluid>

<liferay-frontend:component
	componentId="<%= AssetListWebKeys.LIST_ITEMS_DROPDOWN_DEFAULT_EVENT_HANDLER %>"
	module="js/ListItemsDropdownDefaultEventHandler.es"
/>