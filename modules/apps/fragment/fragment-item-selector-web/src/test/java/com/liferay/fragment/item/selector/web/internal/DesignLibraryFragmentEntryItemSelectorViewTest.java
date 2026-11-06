/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.item.selector.web.internal;

import com.liferay.design.library.util.DesignLibraryUtil;
import com.liferay.fragment.item.selector.FragmentEntryItemSelectorCriterion;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.servlet.ServletContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Lourdes Fernández Besada
 */
public class DesignLibraryFragmentEntryItemSelectorViewTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		Mockito.when(
			_groupLocalService.fetchGroup(_GROUP_ID)
		).thenReturn(
			_group
		);

		_designLibraryFragmentEntryItemSelectorView =
			new DesignLibraryFragmentEntryItemSelectorView(
				_GROUP_ID, _groupLocalService, Mockito.mock(Portal.class),
				Mockito.mock(ServletContext.class));
	}

	@Test
	public void testGetGroup() {
		Assert.assertSame(
			_group,
			_designLibraryFragmentEntryItemSelectorView.getGroup(
				Mockito.mock(ThemeDisplay.class)));
	}

	@Test
	public void testGetTitle() throws PortalException {
		String descriptiveName = RandomTestUtil.randomString();

		Mockito.when(
			_group.getDescriptiveName(LocaleUtil.ENGLISH)
		).thenReturn(
			descriptiveName
		);

		Assert.assertEquals(
			descriptiveName,
			_designLibraryFragmentEntryItemSelectorView.getTitle(
				LocaleUtil.ENGLISH));

		String name = RandomTestUtil.randomString();

		Mockito.when(
			_group.getDescriptiveName(LocaleUtil.ENGLISH)
		).thenThrow(
			new PortalException()
		);

		Mockito.when(
			_group.getName()
		).thenReturn(
			name
		);

		Assert.assertEquals(
			name,
			_designLibraryFragmentEntryItemSelectorView.getTitle(
				LocaleUtil.ENGLISH));
	}

	@Test
	public void testIsVisible() {
		_testIsVisible(false);
		_testIsVisible(true);
	}

	private void _testIsVisible(boolean connected) {
		long scopeGroupId = RandomTestUtil.randomLong();

		ThemeDisplay themeDisplay = Mockito.mock(ThemeDisplay.class);

		Mockito.when(
			themeDisplay.getScopeGroupId()
		).thenReturn(
			scopeGroupId
		);

		try (MockedStatic<DesignLibraryUtil> designLibraryUtilMockedStatic =
				Mockito.mockStatic(DesignLibraryUtil.class)) {

			designLibraryUtilMockedStatic.when(
				() -> DesignLibraryUtil.isConnectedDesignLibraryGroupId(
					_GROUP_ID, scopeGroupId)
			).thenReturn(
				connected
			);

			Assert.assertEquals(
				connected,
				_designLibraryFragmentEntryItemSelectorView.isVisible(
					Mockito.mock(FragmentEntryItemSelectorCriterion.class),
					themeDisplay));
		}
	}

	private static final long _GROUP_ID = RandomTestUtil.randomLong();

	private DesignLibraryFragmentEntryItemSelectorView
		_designLibraryFragmentEntryItemSelectorView;
	private final Group _group = Mockito.mock(Group.class);
	private final GroupLocalService _groupLocalService = Mockito.mock(
		GroupLocalService.class);

}