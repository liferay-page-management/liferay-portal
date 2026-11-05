<%--
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
FragmentDisplayContext portletFragmentDisplayContext = (FragmentDisplayContext)request.getAttribute(FragmentWebKeys.FRAGMENT_DISPLAY_CONTEXT);

String fragmentSetExternalReferenceCode = null;

if ((portletFragmentDisplayContext != null) && Objects.equals(portletFragmentDisplayContext.getFragmentType(), FragmentTypeConstants.BASIC_FRAGMENT_TYPE)) {
	FragmentCollection fragmentCollection = portletFragmentDisplayContext.getFragmentCollection();

	fragmentSetExternalReferenceCode = fragmentCollection.getExternalReferenceCode();
}

RequestBackedPortletURLFactory requestBackedPortletURLFactory = RequestBackedPortletURLFactoryUtil.create(request);
Group scopeGroup = themeDisplay.getScopeGroup();
%>

<li class="control-menu-nav-item">
	<react:component
		module="{FragmentAIAssistantTriggerButton} from fragment-web"
		props='<%=
			HashMapBuilder.<String, Object>put(
				"editFragmentURL",
				PortletURLBuilder.create(
					requestBackedPortletURLFactory.createRenderURL(FragmentPortletKeys.FRAGMENT)
				).setMVCRenderCommandName(
					"/fragment/edit_fragment_entry"
				).setRedirect(
					themeDisplay.getURLCurrent()
				).setParameter(
					"externalReferenceCode", "EXTERNAL_REFERENCE_CODE"
				).buildString()
			).put(
				"fragmentPortletNamespace", portletDisplay.getNamespace()
			).put(
				"fragmentSetExternalReferenceCode", fragmentSetExternalReferenceCode
			).put(
				"siteExternalReferenceCode", scopeGroup.getExternalReferenceCode()
			).build()
		%>'
	/>
</li>