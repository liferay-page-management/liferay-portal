/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.content.page.editor.web.internal.display.context;

import com.liferay.exportimport.kernel.staging.Staging;
import com.liferay.frontend.token.definition.FrontendTokenDefinition;
import com.liferay.frontend.token.definition.FrontendTokenDefinitionRegistry;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.style.book.model.StyleBookEntry;
import com.liferay.style.book.service.StyleBookEntryLocalService;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Anderson Luiz
 */
public class ContentPageEditorDisplayContextTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		LiferayUnitTestRule.INSTANCE;

	@FeatureFlags("LPD-30204")
	@Test
	public void testGetStyleBooks() {
		String themeId = RandomTestUtil.randomString();

		FrontendTokenDefinition frontendTokenDefinition = Mockito.mock(
			FrontendTokenDefinition.class);

		Mockito.when(
			frontendTokenDefinition.getThemeId()
		).thenReturn(
			themeId
		);

		FrontendTokenDefinitionRegistry frontendTokenDefinitionRegistry =
			Mockito.mock(FrontendTokenDefinitionRegistry.class);

		Mockito.when(
			frontendTokenDefinitionRegistry.getFrontendTokenDefinition(
				Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString())
		).thenReturn(
			frontendTokenDefinition
		);

		StyleBookEntry styleBookEntry1 = Mockito.mock(StyleBookEntry.class);

		Mockito.when(
			styleBookEntry1.getThemeId()
		).thenReturn(
			RandomTestUtil.randomString()
		);

		ThemeDisplay themeDisplay = Mockito.mock(ThemeDisplay.class);

		Mockito.when(
			themeDisplay.getCompanyId()
		).thenReturn(
			RandomTestUtil.randomLong()
		);

		Mockito.when(
			themeDisplay.getScopeGroupId()
		).thenReturn(
			RandomTestUtil.randomLong()
		);

		Mockito.when(
			themeDisplay.getThemeId()
		).thenReturn(
			RandomTestUtil.randomString()
		);

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		String name = RandomTestUtil.randomString();

		StyleBookEntry styleBookEntry2 = Mockito.mock(StyleBookEntry.class);

		Mockito.when(
			styleBookEntry2.getThemeId()
		).thenReturn(
			themeId
		);

		Mockito.when(
			styleBookEntry2.getName()
		).thenReturn(
			name
		);

		Staging staging = Mockito.mock(Staging.class);

		Mockito.when(
			staging.getLiveGroupId(Mockito.anyLong())
		).thenReturn(
			RandomTestUtil.randomLong()
		);

		StyleBookEntryLocalService styleBookEntryLocalService = Mockito.mock(
			StyleBookEntryLocalService.class);

		Mockito.when(
			styleBookEntryLocalService.getStyleBookEntries(
				Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.any())
		).thenReturn(
			ListUtil.fromArray(styleBookEntry1, styleBookEntry2)
		);

		ContentPageEditorDisplayContext contentPageEditorDisplayContext =
			new ContentPageEditorDisplayContext(
				null, null, null, null, null, null,
				frontendTokenDefinitionRegistry, mockHttpServletRequest, null,
				null, null, null, null, null, null, null, null, null, null,
				null, null, null, null, null, null, null, null, null, null,
				staging, null, styleBookEntryLocalService, null, null);

		List<Map<String, Object>> result = ReflectionTestUtil.invoke(
			contentPageEditorDisplayContext, "_getStyleBooks", new Class<?>[0]);

		Assert.assertEquals(result.toString(), 1, result.size());

		Map<String, Object> firstEntry = result.get(0);

		Assert.assertEquals(name, firstEntry.get("name"));
	}

}