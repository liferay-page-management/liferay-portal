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
Layout selLayout = layoutsAdminDisplayContext.getSelLayout();
%>

<c:choose>
	<c:when test="<%= layoutsAdminDisplayContext.isShowGeneralSettings(user, selLayout) %>">
		<liferay-util:include page="/layout/details.jsp" servletContext="<%= application %>" />
	</c:when>
	<c:otherwise>
		<c:if test="<%= layoutsAdminDisplayContext.isShowAdvancedSettings(user, selLayout) %>">
			<clay:sheet-section
				cssClass="mb-5"
			>
				<h3 class="mb-4 text-uppercase"><liferay-ui:message key="url" /></h3>

				<liferay-util:include page="/layout/advanced.jsp" servletContext="<%= application %>" />
			</clay:sheet-section>
		</c:if>
	</c:otherwise>
</c:choose>

<c:if test="<%= layoutsAdminDisplayContext.isShowCustomization(user, selLayout) || layoutsAdminDisplayContext.isShowCustomFields(user, selLayout) %>">
	<clay:sheet-section
		cssClass="mb-5"
	>
		<h3 class="mb-4 text-uppercase"><liferay-ui:message key="advanced" /></h3>

		<c:if test="<%= layoutsAdminDisplayContext.isShowCustomization(user, selLayout) %>">
			<liferay-util:include page="/layout/customization_settings.jsp" servletContext="<%= application %>" />
		</c:if>

		<c:if test="<%= layoutsAdminDisplayContext.isShowCustomFields(user, selLayout) %>">
			<liferay-util:include page="/layout/custom_fields.jsp" servletContext="<%= application %>" />
		</c:if>
	</clay:sheet-section>
</c:if>