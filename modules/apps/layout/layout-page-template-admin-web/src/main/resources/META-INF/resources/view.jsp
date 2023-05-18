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
Group scopeGroup = themeDisplay.getScopeGroup();

ImportDisplayContext importDisplayContext = new ImportDisplayContext(request, renderRequest);

Map<LayoutsImporterResultEntry.Status, List<LayoutsImporterResultEntry>> layoutsImporterResultEntryMap = importDisplayContext.getLayoutsImporterResultEntryMap();
%>

<c:if test="<%= MapUtil.isNotEmpty(layoutsImporterResultEntryMap) %>">
	<liferay-util:buffer
		var="layoutsImporterResultEntryMessage"
	>
		<span><%= importDisplayContext.getDialogMessage() %></span>

		<ul>

			<%
			Map<Integer, List<LayoutsImporterResultEntry>> importedLayoutsImporterResultEntriesMap = importDisplayContext.getImportedLayoutsImporterResultEntriesMap();
			%>

			<c:if test="<%= MapUtil.isNotEmpty(importedLayoutsImporterResultEntriesMap) %>">

				<%
				for (Map.Entry<Integer, List<LayoutsImporterResultEntry>> entrySet : importedLayoutsImporterResultEntriesMap.entrySet()) {
				%>

					<li>
						<span class="font-italic"><%= HtmlUtil.escape(importDisplayContext.getSuccessMessage(entrySet)) %></span>
					</li>

				<%
				}
				%>

			</c:if>

			<%
			List<LayoutsImporterResultEntry> layoutsImporterResultEntriesWithWarnings = importDisplayContext.getLayoutsImporterResultEntriesWithWarnings();
			%>

			<c:if test="<%= ListUtil.isNotEmpty(layoutsImporterResultEntriesWithWarnings) %>">

				<%
				for (LayoutsImporterResultEntry layoutsImporterResultEntry : layoutsImporterResultEntriesWithWarnings) {
					String[] warningMessages = layoutsImporterResultEntry.getWarningMessages();
				%>

					<li>
						<span class="font-italic"><%= HtmlUtil.escape(importDisplayContext.getWarningMessage(layoutsImporterResultEntry.getName())) %></span>

						<ul>

							<%
							for (String warningMessage : warningMessages) {
							%>

								<li><span class="font-italic"><%= HtmlUtil.escape(warningMessage) %></span></li>

							<%
							}
							%>

						</ul>
					</li>

				<%
				}
				%>

			</c:if>

			<%
			int i = 0;

			List<LayoutsImporterResultEntry> notImportedLayoutsImporterResultEntries = importDisplayContext.getNotImportedLayoutsImporterResultEntries();
			%>

			<c:if test="<%= ListUtil.isNotEmpty(notImportedLayoutsImporterResultEntries) %>">

				<%
				for (; (i < notImportedLayoutsImporterResultEntries.size()) && (i < 10); i++) {
					LayoutsImporterResultEntry layoutsImporterResultEntry = notImportedLayoutsImporterResultEntries.get(i);
				%>

					<li>
						<span class="font-italic"><%= HtmlUtil.escape(layoutsImporterResultEntry.getErrorMessage()) %></span>
					</li>

				<%
				}
				%>

			</c:if>
		</ul>

		<c:if test="<%= notImportedLayoutsImporterResultEntries.size() > 10 %>">
			<span><%= LanguageUtil.format(request, "x-more-entries-could-also-not-be-imported", "<strong>" + (notImportedLayoutsImporterResultEntries.size() - i) + "</strong>", false) %></span>
		</c:if>
	</liferay-util:buffer>

	<aui:script>
		Liferay.Util.openToast({
			message: '<%= HtmlUtil.escapeJS(layoutsImporterResultEntryMessage) %>',
			title:
				'<liferay-ui:message key="<%= importDisplayContext.getDialogType() %>" />:',
			type: '<%= importDisplayContext.getDialogType() %>',
		});
	</aui:script>
</c:if>

<c:choose>
	<c:when test='<%= Objects.equals(layoutPageTemplatesAdminDisplayContext.getTabs1(), "display-page-templates") %>'>
		<liferay-util:include page="/view_display_pages.jsp" servletContext="<%= application %>" />
	</c:when>
	<c:when test='<%= Objects.equals(layoutPageTemplatesAdminDisplayContext.getTabs1(), "master-layouts") %>'>
		<liferay-util:include page="/view_master_layouts.jsp" servletContext="<%= application %>" />
	</c:when>
	<c:when test='<%= Objects.equals(layoutPageTemplatesAdminDisplayContext.getTabs1(), "page-templates") && scopeGroup.isCompany() %>'>
		<liferay-util:include page="/view_layout_prototypes.jsp" servletContext="<%= application %>" />
	</c:when>
	<c:when test='<%= Objects.equals(layoutPageTemplatesAdminDisplayContext.getTabs1(), "page-templates") && !scopeGroup.isCompany() %>'>
		<liferay-util:include page="/view_layout_page_template_collections.jsp" servletContext="<%= application %>" />
	</c:when>
</c:choose>