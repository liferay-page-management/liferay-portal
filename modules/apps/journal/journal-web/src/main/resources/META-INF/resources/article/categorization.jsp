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
JournalArticle article = journalDisplayContext.getArticle();

JournalEditArticleDisplayContext journalEditArticleDisplayContext = new JournalEditArticleDisplayContext(request, liferayPortletResponse, article);
%>

<liferay-ui:error-marker
	key="<%= WebKeys.ERROR_SECTION %>"
	value="categorization"
/>

<aui:model-context bean="<%= article %>" model="<%= JournalArticle.class %>" />

<liferay-asset:asset-categories-error />

<liferay-asset:asset-tags-error />

<%
long classPK = 0;
double priority = 0;

if (article != null) {
	classPK = article.getResourcePrimKey();

	if (!article.isApproved() && (article.getVersion() != JournalArticleConstants.VERSION_DEFAULT)) {
		AssetEntry assetEntry = AssetEntryLocalServiceUtil.fetchEntry(JournalArticle.class.getName(), article.getPrimaryKey());

		if (assetEntry != null) {
			classPK = article.getPrimaryKey();
			priority = assetEntry.getPriority();
		}
	}
	else {
		AssetEntry assetEntry = AssetEntryLocalServiceUtil.fetchEntry(JournalArticle.class.getName(), article.getResourcePrimKey());

		if (assetEntry != null) {
			priority = assetEntry.getPriority();
		}
	}
}

DDMStructure ddmStructure = journalEditArticleDisplayContext.getDDMStructure();
%>

<liferay-asset:asset-categories-selector
	className="<%= JournalArticle.class.getName() %>"
	classPK="<%= classPK %>"
	classTypePK="<%= ddmStructure.getStructureId() %>"
	ignoreRequestValue="<%= journalEditArticleDisplayContext.isChangeStructure() %>"
	visibilityTypes="<%= AssetVocabularyConstants.VISIBILITY_TYPES %>"
/>

<div class="border-0 mb-0 sheet-subtitle text-uppercase">
	<liferay-ui:message key="other-metadata" />
</div>

<liferay-asset:asset-tags-selector
	className="<%= JournalArticle.class.getName() %>"
	classPK="<%= classPK %>"
	ignoreRequestValue="<%= journalEditArticleDisplayContext.isChangeStructure() %>"
/>

<script>
	var categorationRoot = document.querySelectorAll(
		'#_com_liferay_journal_web_portlet_JournalPortlet_mvfr___assetCategoriesSelector .form-group'
	);
	var vocabularyTitle = categorationRoot[0].textContent.split('Required')[0];
	var categoriesNotification = document.createElement('div');
	categoriesNotification.classList.add(
		'alert',
		'alert-dismissible',
		'alert-danger'
	);
	categoriesNotification.setAttribute('role', 'alert');
	categoriesNotification.innerHTML = `
			<button aria-label="<%= LanguageUtil.get(request, "close") %>" class="close" data-dismiss="liferay-alert" type="button">
				<aui:icon image="times" markupView="lexicon" />

				<span class="sr-only"><%= LanguageUtil.get(request, "close") %></span>
			</button>

			<span class="alert-indicator">
				<svg aria-hidden="true" class="lexicon-icon lexicon-icon-times">
					<use xlink:href="<%= themeDisplay.getPathThemeImages() %>/clay/icons.svg#exclamation-full"></use>
				</svg>
			</span>

			<strong class="lead">Error:</strong>
	`;
	categoriesNotification.innerHTML +=
		'<liferay-ui:message arguments="' + vocabularyTitle + '" key="please-select-at-least-one-category-for-x" translateArguments="<%= false %>" />';

	document.getElementsByClassName('btn-primary')[0].onclick = function () {
		if (categorationRoot[0].innerText.includes('Required')) {
			if (
				!document.getElementsByClassName('alert-danger').length &&
				!categorationRoot[0].querySelectorAll(
					'.input-group .input-group-item .input-group .input-group-item .form-control-inset'
				)[0].value
			) {
				document
					.querySelector('#categorizationContent')
					.before(categoriesNotification);
			}
		}
	};
</script>

<c:if test='<%= GetterUtil.getBoolean(PropsUtil.get("feature.flag.LPS-150762")) && (article != null) %>'>

	<%
	AssetAutoTaggerConfiguration assetAutoTaggerConfiguration = (AssetAutoTaggerConfiguration)request.getAttribute(AssetAutoTaggerConfiguration.class.getName());
	%>

	<clay:checkbox
		checked="<%= assetAutoTaggerConfiguration.isUpdateAutoTags() %>"
		id='<%= liferayPortletResponse.getNamespace() + "updateAutoTags" %>'
		label='<%= LanguageUtil.get(request, "update-auto-tags") %>'
		name='<%= liferayPortletResponse.getNamespace() + "updateAutoTags" %>'
	/>

	<div class="ml-4">
		<small class="text-secondary">
			<liferay-ui:message key="update-auto-tags-help" />
		</small>
	</div>
</c:if>

<aui:input cssClass="form-control-sm" label="priority" name="assetPriority" type="text" value="<%= priority %>" wrapperCssClass="mb-3">
	<aui:validator name="number" />

	<aui:validator name="min">[0]</aui:validator>
</aui:input>

<c:if test="<%= CustomAttributesUtil.hasCustomAttributes(company.getCompanyId(), JournalArticle.class.getName(), classPK, null) %>">
	<liferay-expando:custom-attribute-list
		className="<%= JournalArticle.class.getName() %>"
		classPK="<%= (article != null) ? article.getPrimaryKey() : 0 %>"
		editable="<%= true %>"
		label="<%= true %>"
	/>
</c:if>