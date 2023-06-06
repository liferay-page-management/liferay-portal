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
AICreatorOpenAIDisplayContext aiCreatorOpenAIDisplayContext = (AICreatorOpenAIDisplayContext)request.getAttribute(AICreatorOpenAIDisplayContext.class.getName());
%>

<clay:content-row>
	<clay:content-col
		expand="<%= true %>"
	>
		<aui:input label="description" name="content" type="text" />
	</clay:content-col>
</clay:content-row>

<clay:content-row>
	<clay:content-col>
		<aui:input label="tone" name="tone" type="text" />
	</clay:content-col>

	<clay:content-col>
		<aui:input label="words" name="words" type="number" />
	</clay:content-col>
</clay:content-row>

<clay:content-row>
	<clay:content-col
		expand="<%= true %>"
	>
		<aui:input label="create" name="create" type="button" />
	</clay:content-col>
</clay:content-row>

<aui:script>
	var contentInput = document.getElementById('<portlet:namespace />content');
	var createButton = document.getElementById('<portlet:namespace />create');
	var toneInput = document.getElementById('<portlet:namespace />tone');
	var wordsInput = document.getElementById('<portlet:namespace />words');

	var inputs = [
		document.getElementById('<portlet:namespace />content'),
		document.getElementById('<portlet:namespace />tone'),
		document.getElementById('<portlet:namespace />words'),
	];

	var fields = JSON.stringify({
		content: contentInput.value,
		words: toneInput.value,
		tone: wordsInput.value,
	});

	var completionURL =
		'<%= aiCreatorOpenAIDisplayContext.getGetCompletionURL() %>';

	createButton.addEventListener('click', (event) => {
		var fields = {
			<portlet:namespace />content: document.getElementById(
				'<portlet:namespace />content'
			).value,
			<portlet:namespace />words: document.getElementById(
				'<portlet:namespace />words'
			).value,
			<portlet:namespace />tone: document.getElementById(
				'<portlet:namespace />tone'
			).value,
		};

		Liferay.Util.fetch(completionURL, {
			body: Liferay.Util.objectToFormData(fields),
			method: 'POST',
		})
			.then((response) => {
				return response.ok ? response.text() : Promise.reject();
			})
			.then((data) => {
				var responseData = {};

				try {
					responseData = JSON.parse(data);
				}
				catch (e) {}

				if (responseData.error) {
					alert('Error!' + responseData.error.message);
				}
				else if (responseData.completion) {
					alert('Text created!' + responseData.completion.content);
				}
			});
	});
</aui:script>