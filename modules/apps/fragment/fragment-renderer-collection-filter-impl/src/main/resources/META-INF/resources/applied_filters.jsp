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
List<Map<String, String>> appliedFilterList = (List)request.getAttribute(CollectionAppliedFiltersFragmentRendererWebKeys.APPLIED_FILTER_LIST);

for (Map<String, String> appliedFilter : appliedFilterList) {
%>

	<span class="label label-lg label-secondary">
		<span class="label-item label-item-expand">
			<%= appliedFilter.get("filterValue") %>
		</span>
		<span class="label-item label-item-after">
			<button aria-label="Remove filter" class="close remove-collection-applied-filter-button" data-filter-fragment-entry-link-id="<%= appliedFilter.get("filterFragmentEntryLinkId") %>" data-filter-type="<%= appliedFilter.get("filterType") %>" data-filter-value="<%= appliedFilter.get("filterValue") %>" type="button">
				<span class="c-inner">
					<clay:icon
						symbol="times"
					/>
				</span>
			</button>
		</span>
	</span>

<%
}
%>

<liferay-frontend:component
	context='<%= HashMapBuilder.<String, Object>put("collectionFilterParameterPrefix", CollectionAppliedFiltersFragmentRendererWebKeys.COLLECTION_FILTER_PARAMETER_PREFIX).put("removeButtonSelector", ".remove-collection-applied-filter-button").build() %>'
	module="js/CollectionAppliedFilters"
/>