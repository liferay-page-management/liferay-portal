/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.site.cms.site.initializer.contributor.CMSStructureObjectFolderContributor;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Adolfo Pérez
 */
public class ViewStructuresDisplayContextTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetAPIURL() {
		String apiURL = _viewStructuresDisplayContext.getAPIURL();

		int start = apiURL.indexOf("filter=");

		String filterString = apiURL.substring(start + 7);

		Assert.assertTrue(filterString.startsWith(StringPool.OPEN_PARENTHESIS));
		Assert.assertTrue(filterString.endsWith(StringPool.CLOSE_PARENTHESIS));
	}

	@Test
	public void testGetCreationMenu() {
		LanguageUtil languageUtil = new LanguageUtil();

		languageUtil.setLanguage(Mockito.mock(Language.class));

		ViewStructuresDisplayContext viewStructuresDisplayContext1 =
			_getViewStructuresDisplayContext(null);

		CreationMenu creationMenu1 =
			viewStructuresDisplayContext1.getCreationMenu();

		List<DropdownItem> dropdownItems1 = _getPrimaryItems(creationMenu1);

		ViewStructuresDisplayContext viewStructuresDisplayContext2 =
			_getViewStructuresDisplayContext(StringUtil.randomString());

		CreationMenu creationMenu2 =
			viewStructuresDisplayContext2.getCreationMenu();

		List<DropdownItem> dropdownItems2 = _getPrimaryItems(creationMenu2);

		Assert.assertEquals(
			dropdownItems2.toString(), dropdownItems1.size() + 1,
			dropdownItems2.size());
	}

	private List<DropdownItem> _getPrimaryItems(CreationMenu creationMenu) {
		return (List<DropdownItem>)creationMenu.get("primaryItems");
	}

	private ViewStructuresDisplayContext _getViewStructuresDisplayContext(
		String objectFolderExternalReferenceCode) {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY,
			new ThemeDisplay() {
				{
					setPathFriendlyURLPublic(StringUtil.randomString());
				}
			});

		CMSStructureObjectFolderContributor
			cmsStructureObjectFolderContributor = Mockito.mock(
				CMSStructureObjectFolderContributor.class);

		Mockito.when(
			cmsStructureObjectFolderContributor.
				getObjectFolderExternalReferenceCode()
		).thenReturn(
			objectFolderExternalReferenceCode
		);

		Mockito.lenient(
		).when(
			cmsStructureObjectFolderContributor.getLabel()
		).thenReturn(
			StringUtil.randomString()
		);

		return new ViewStructuresDisplayContext(
			List.of(cmsStructureObjectFolderContributor),
			mockHttpServletRequest);
	}

	private final ViewStructuresDisplayContext _viewStructuresDisplayContext =
		new ViewStructuresDisplayContext(
			Collections.emptyList(), new MockHttpServletRequest());

}