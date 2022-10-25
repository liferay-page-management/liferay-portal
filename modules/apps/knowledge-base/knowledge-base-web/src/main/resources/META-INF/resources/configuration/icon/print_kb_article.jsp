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
KBArticle kbArticle = (KBArticle)liferayPortletRequest.getAttribute(KBWebKeys.KNOWLEDGE_BASE_KB_ARTICLE);

if (kbArticle == null) {
	kbArticle = (KBArticle)liferayPortletRequest.getAttribute(KBWebKeys.KNOWLEDGE_BASE_PARENT_KB_ARTICLE);
}
%>

<portlet:renderURL var="printURL" windowState="<%= LiferayWindowState.POP_UP.toString() %>">
	<portlet:param name="mvcPath" value="/admin/common/print_kb_article.jsp" />
	<portlet:param name="resourceClassNameId" value="<%= String.valueOf(kbArticle.getClassNameId()) %>" />
	<portlet:param name="resourcePrimKey" value="<%= String.valueOf(kbArticle.getResourcePrimKey()) %>" />
	<portlet:param name="viewMode" value="<%= Constants.PRINT %>" />
</portlet:renderURL>

<liferay-util:buffer
	var="onClickFn"
>
	window.open('<%= printURL %>', '', 'directories=no,height=640,location=no, menubar=no,resizable=yes,scrollbars=yes,status=0, toolbar=0,width=680');
</liferay-util:buffer>

<liferay-ui:icon
	message="print"
	onClick="<%= onClickFn %>"
	url="javascript:void(0);"
/>