<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/select_layout/init.jsp" %>

<div>
	<react:component
		module="{SelectLayout} from layout-taglib"
		props='<%= (Map<String, Object>)request.getAttribute("liferay-layout:select-layout:data") %>'
	/>
</div>