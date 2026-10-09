/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.depot.service.DepotEntryLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Veronica Gonzalez
 */
public class SectionDisplayContextUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);

		ReflectionTestUtil.setFieldValue(
			RoleLocalServiceUtil.class, "_service", _roleLocalService);

		_mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, _themeDisplay);
	}

	@Test
	public void testAppendGroupIdsKeepsTheFilterStringOfAdministrators()
		throws Exception {

		_mockHasUserRole(true);

		Assert.assertEquals(
			_FILTER_STRING,
			SectionDisplayContextUtil.appendGroupIds(
				_FILTER_STRING, _mockHttpServletRequest));
	}

	@Test
	public void testAppendGroupIdsScopesNonadministrators() throws Exception {
		_mockHasUserRole(false);

		try (MockedStatic<DepotEntryLocalServiceUtil>
				depotEntryLocalServiceUtilMockedStatic = Mockito.mockStatic(
					DepotEntryLocalServiceUtil.class)) {

			depotEntryLocalServiceUtilMockedStatic.when(
				() -> DepotEntryLocalServiceUtil.getDepotEntryGroupIds(
					Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())
			).thenReturn(
				List.of(42L, 7L)
			);

			Assert.assertEquals(
				_FILTER_STRING + " and groupIds/any(g:g in (42,7))",
				SectionDisplayContextUtil.appendGroupIds(
					_FILTER_STRING, _mockHttpServletRequest));
		}
	}

	private void _mockHasUserRole(boolean hasUserRole) throws Exception {
		Mockito.when(
			_roleLocalService.hasUserRole(
				Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyBoolean())
		).thenReturn(
			hasUserRole
		);
	}

	private static final String _FILTER_STRING = "status eq 2";

	private final MockHttpServletRequest _mockHttpServletRequest =
		new MockHttpServletRequest();

	@Mock
	private RoleLocalService _roleLocalService;

	@Mock
	private ThemeDisplay _themeDisplay;

}