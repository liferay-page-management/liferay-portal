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
Group group = layoutsAdminDisplayContext.getGroup();

Layout selLayout = layoutsAdminDisplayContext.getSelLayout();

LayoutSet selLayoutSet = layoutsAdminDisplayContext.getSelLayoutSet();

LayoutType selLayoutType = selLayout.getLayoutType();

String friendlyURLBase = StringPool.BLANK;

if (!group.isLayoutPrototype() && selLayoutType.isURLFriendliable() && !layoutsAdminDisplayContext.isDraft() && !selLayout.isSystem()) {
	friendlyURLBase = layoutsAdminDisplayContext.getFriendlyURLBase();
}
%>

<liferay-ui:error-marker
	key="<%= WebKeys.ERROR_SECTION %>"
	value="url"
/>

<aui:model-context bean="<%= selLayout %>" model="<%= Layout.class %>" />

<c:choose>
	<c:when test="<%= !group.isLayoutPrototype() %>">
		<c:choose>
			<c:when test="<%= selLayoutType.isURLFriendliable() && !layoutsAdminDisplayContext.isDraft() && !selLayout.isSystem() %>">
				<liferay-friendly-url:input
					className="<%= Layout.class.getName() %>"
					classPK="<%= selLayout.getPlid() %>"
					inputAddon="<%= friendlyURLBase %>"
					name="friendlyURL"
				/>

				<aui:input name="originalFriendlyURL" type="hidden" value="<%= (selLayout != null) ? HttpComponentsUtil.decodeURL(selLayout.getFriendlyURL()) : StringPool.BLANK %>" />

				<c:if test="<%= group.isLayoutSetPrototype() %>">
					<c:if test='<%= FeatureFlagManagerUtil.isEnabled("LPS-174431") && SessionMessages.contains(renderRequest, "friendlyURLConflictWithSiteLayouts") %>'>
						<aui:script>
							Liferay.Util.openToast({
								autoClose: 10000,
								message:
									'<liferay-ui:message key="the-site-template-page-was-saved-with-a-conflicting-friendly-url" />',
								type: 'warning',
							});
						</aui:script>
					</c:if>

					<c:if test='<%= FeatureFlagManagerUtil.isEnabled("LPS-174431") && layoutsAdminDisplayContext.isShowLayoutSetPrototypeFriendlyURLConflictSitesLayouts() %>'>
						<div class="alert alert-warning">
							<liferay-ui:message key="layout-config-layout-set-prototype-friendly-url-collision" />

							<ul>

								<%
								for (Layout conflictLayout : layoutsAdminDisplayContext.getLayoutSetPrototypeFriendlyURLConflictSitesLayouts()) {
									Group conflictGroup = conflictLayout.getGroup();
								%>

									<c:choose>
										<c:when test="<%= layoutsAdminDisplayContext.isShowConfigureAction(conflictLayout) %>">
											<liferay-util:buffer
												var="layoutLink"
											>
												<em><clay:link cssClass="alert-link" href="<%= layoutsAdminDisplayContext.getConfigureConflictLayoutURL(conflictLayout) %>" label="<%= HtmlUtil.escape(conflictLayout.getName(locale)) %>" /></em>
											</liferay-util:buffer>

											<li>
												<liferay-ui:message arguments="<%= new Object[] {layoutLink.trim(), conflictGroup.getName(locale)} %>" key="page-x-of-x" translateArguments="<%= false %>" />
											</li>
										</c:when>
										<c:otherwise>
											<li>
												<liferay-ui:message arguments="<%= new Object[] {conflictLayout.getName(locale), conflictGroup.getName(locale)} %>" key="page-x-of-x" translateArguments="<%= false %>" />
												(<liferay-ui:message key="please-contact-the-administrator-to-resolve-this-friendly-url-conflict" />)
											</li>
										</c:otherwise>
									</c:choose>

								<%
								}
								%>

							</ul>
						</div>
					</c:if>
				</c:if>

				<c:if test="<%= !group.isLayoutSetPrototype() && selLayoutSet.isLayoutSetPrototypeLinkEnabled() %>">
					<c:if test='<%= FeatureFlagManagerUtil.isEnabled("LPS-174434") && SessionMessages.contains(renderRequest, "friendlyURLConflictWithSiteLayoutSetPrototypeLayout") %>'>
						<aui:script>
							Liferay.Util.openToast({
								autoClose: 10000,
								message:
									'<liferay-ui:message key="the-page-was-saved-with-a-conflicting-friendly-url" />',
								type: 'warning',
							});
						</aui:script>
					</c:if>

					<c:if test='<%= FeatureFlagManagerUtil.isEnabled("LPS-174434") && layoutsAdminDisplayContext.isShowLayoutSetPrototypeFriendlyURLConflictLayout() %>'>
						<div class="alert alert-warning">
							<liferay-ui:message key="the-friendly-url-of-this-page-is-conflicting-with-a-friendly-url-of-a-page-in-the-site-template-from-which-this-site-was-created" />

							<ul>

								<%
								Layout conflictLayout = layoutsAdminDisplayContext.getLayoutSetPrototypeFriendlyURLConflictLayout();

								Group conflictGroup = conflictLayout.getGroup();

								LayoutSetPrototype layoutSetPrototype = LayoutSetPrototypeLocalServiceUtil.getLayoutSetPrototype(conflictGroup.getClassPK());
								%>

								<c:choose>
									<c:when test="<%= layoutsAdminDisplayContext.isShowConfigureAction(conflictLayout) %>">
										<liferay-util:buffer
											var="layoutLink"
										>
											<em><clay:link cssClass="alert-link" href="<%= layoutsAdminDisplayContext.getConfigureConflictLayoutURL(conflictLayout) %>" label="<%= HtmlUtil.escape(conflictLayout.getName(locale)) %>" /></em>
										</liferay-util:buffer>

										<li>
											<liferay-ui:message arguments="<%= new Object[] {layoutLink.trim(), layoutSetPrototype.getName(locale)} %>" key="page-x-of-x" translateArguments="<%= false %>" />
										</li>
									</c:when>
									<c:otherwise>
										<li>
											<liferay-ui:message arguments="<%= new Object[] {conflictLayout.getName(locale), layoutSetPrototype.getName(locale)} %>" key="page-x-of-x" translateArguments="<%= false %>" />
											(<liferay-ui:message key="please-contact-the-administrator-to-resolve-this-friendly-url-conflict" />)
										</li>
									</c:otherwise>
								</c:choose>
							</ul>
						</div>
					</c:if>
				</c:if>
			</c:when>
			<c:otherwise>
				<aui:input name="friendlyURL" type="hidden" value="<%= (selLayout != null) ? HttpComponentsUtil.decodeURL(selLayout.getFriendlyURL()) : StringPool.BLANK %>" />
			</c:otherwise>
		</c:choose>
	</c:when>
	<c:otherwise>
		<aui:input name="friendlyURL" type="hidden" value="<%= (selLayout != null) ? HttpComponentsUtil.decodeURL(selLayout.getFriendlyURL()) : StringPool.BLANK %>" />
	</c:otherwise>
</c:choose>