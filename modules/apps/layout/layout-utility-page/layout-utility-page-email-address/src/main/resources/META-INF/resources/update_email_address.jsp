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
UpdateEmailAddressDisplayContext updateEmailAddressDisplayContext = new UpdateEmailAddressDisplayContext(request, themeDisplay);
%>

<div class="sheet sheet-lg">
	<div class="sheet-header">
		<div class="autofit-padded-no-gutters-x autofit-row">
			<div class="autofit-col autofit-col-expand">
				<h2 class="sheet-title">
					<liferay-ui:message key="change-email-address" />
				</h2>
			</div>

			<div class="autofit-col">
				<liferay-util:include page="/html/portal/select_language.jspf" />
			</div>
		</div>
	</div>

	<div class="sheet-text">
		<aui:form action='<%= themeDisplay.getPathMain() + "/portal/update_email_address" %>' method="post" name="fm">
			<aui:input name="p_auth" type="hidden" value="<%= AuthTokenUtil.getToken(request) %>" />
			<aui:input name="doAsUserId" type="hidden" value="<%= themeDisplay.getDoAsUserId() %>" />
			<aui:input name="<%= Constants.CMD %>" type="hidden" value="<%= Constants.UPDATE %>" />
			<aui:input name="<%= WebKeys.REFERER %>" type="hidden" value="<%= updateEmailAddressDisplayContext.getReferer() %>" />

			<c:if test="<%= !SessionErrors.isEmpty(request) %>">
				<div class="alert alert-danger">
					<liferay-ui:message key="<%= updateEmailAddressDisplayContext.getUserEmailAddressExceptionMessageKey() %>" />
				</div>
			</c:if>

			<aui:fieldset label="email-address">
				<aui:input autoFocus="<%= true %>" class="lfr-input-text-container" label="email-address" name="emailAddress1" type="text" value='<%= ParamUtil.getString(request, "emailAddress1") %>' />

				<aui:input class="lfr-input-text-container" label="enter-again" name="emailAddress2" type="text" value='<%= ParamUtil.getString(request, "emailAddress2") %>' />
			</aui:fieldset>

			<aui:button-row>
				<aui:button cssClass="mr-2" type="submit" />

				<aui:button href="<%= themeDisplay.getPathMain() %>" type="cancel" />
			</aui:button-row>
		</aui:form>
	</div>
</div>