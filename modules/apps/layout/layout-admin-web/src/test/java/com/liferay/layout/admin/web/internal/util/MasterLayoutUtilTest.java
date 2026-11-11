/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.admin.web.internal.util;

import com.liferay.design.library.util.DesignLibraryUtil;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Georgel Pop
 */
public class MasterLayoutUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		Group group = Mockito.mock(Group.class);

		Mockito.when(
			group.getDescriptiveName(LocaleUtil.US)
		).thenReturn(
			_DESCRIPTIVE_NAME
		);

		Mockito.when(
			group.getExternalReferenceCode()
		).thenReturn(
			_EXTERNAL_REFERENCE_CODE
		);

		_groupLocalServiceUtilMockedStatic.when(
			() -> GroupLocalServiceUtil.fetchGroup(_GROUP_ID_DESIGN_LIBRARY)
		).thenReturn(
			group
		);

		_designLibraryUtilMockedStatic.when(
			() -> DesignLibraryUtil.isDesignLibraryScope(group)
		).thenReturn(
			true
		);
	}

	@After
	public void tearDown() {
		_designLibraryUtilMockedStatic.close();
		_groupLocalServiceUtilMockedStatic.close();
		_layoutPageTemplateEntryLocalServiceUtilMockedStatic.close();
	}

	@Test
	@TestInfo("LPD-108827")
	public void testGetMasterLayoutPageTemplateEntryScopeERC() {
		Assert.assertEquals(
			StringPool.BLANK,
			MasterLayoutUtil.getMasterLayoutPageTemplateEntryScopeERC(
				_GROUP_ID,
				_mockLayout(
					_mockLayoutPageTemplateEntry(_GROUP_ID),
					RandomTestUtil.randomLong())));
		Assert.assertEquals(
			_EXTERNAL_REFERENCE_CODE,
			MasterLayoutUtil.getMasterLayoutPageTemplateEntryScopeERC(
				_GROUP_ID,
				_mockLayout(
					_mockLayoutPageTemplateEntry(_GROUP_ID_DESIGN_LIBRARY),
					RandomTestUtil.randomLong())));
		Assert.assertEquals(
			StringPool.BLANK,
			MasterLayoutUtil.getMasterLayoutPageTemplateEntryScopeERC(
				_GROUP_ID, _mockLayout(null, 0)));
		Assert.assertEquals(
			StringPool.BLANK,
			MasterLayoutUtil.getMasterLayoutPageTemplateEntryScopeERC(
				_GROUP_ID, _mockLayout(null, RandomTestUtil.randomLong())));
	}

	@Test
	@TestInfo("LPD-108827")
	public void testGetMasterLayoutPageTemplateEntryScopeLabel() {
		Assert.assertEquals(
			StringPool.BLANK,
			MasterLayoutUtil.getMasterLayoutPageTemplateEntryScopeLabel(
				_GROUP_ID, LocaleUtil.US, _mockLayoutPageTemplateEntry(0)));
		Assert.assertEquals(
			StringPool.BLANK,
			MasterLayoutUtil.getMasterLayoutPageTemplateEntryScopeLabel(
				_GROUP_ID, LocaleUtil.US,
				_mockLayoutPageTemplateEntry(RandomTestUtil.randomLong())));
		Assert.assertEquals(
			_DESCRIPTIVE_NAME,
			MasterLayoutUtil.getMasterLayoutPageTemplateEntryScopeLabel(
				_GROUP_ID, LocaleUtil.US,
				_mockLayoutPageTemplateEntry(_GROUP_ID_DESIGN_LIBRARY)));
		Assert.assertEquals(
			_DESCRIPTIVE_NAME,
			MasterLayoutUtil.getMasterLayoutPageTemplateEntryScopeLabel(
				_GROUP_ID,
				_mockLayout(
					_mockLayoutPageTemplateEntry(_GROUP_ID_DESIGN_LIBRARY),
					RandomTestUtil.randomLong()),
				LocaleUtil.US));
	}

	private Layout _mockLayout(
		LayoutPageTemplateEntry masterLayoutPageTemplateEntry,
		long masterLayoutPlid) {

		Layout layout = Mockito.mock(Layout.class);

		Mockito.when(
			layout.getMasterLayoutPlid()
		).thenReturn(
			masterLayoutPlid
		);

		_layoutPageTemplateEntryLocalServiceUtilMockedStatic.when(
			() ->
				LayoutPageTemplateEntryLocalServiceUtil.
					fetchLayoutPageTemplateEntryByPlid(masterLayoutPlid)
		).thenReturn(
			masterLayoutPageTemplateEntry
		);

		return layout;
	}

	private LayoutPageTemplateEntry _mockLayoutPageTemplateEntry(long groupId) {
		LayoutPageTemplateEntry layoutPageTemplateEntry = Mockito.mock(
			LayoutPageTemplateEntry.class);

		Mockito.when(
			layoutPageTemplateEntry.getGroupId()
		).thenReturn(
			groupId
		);

		return layoutPageTemplateEntry;
	}

	private static final String _DESCRIPTIVE_NAME =
		RandomTestUtil.randomString();

	private static final String _EXTERNAL_REFERENCE_CODE =
		RandomTestUtil.randomString();

	private static final long _GROUP_ID = RandomTestUtil.randomLong();

	private static final long _GROUP_ID_DESIGN_LIBRARY =
		RandomTestUtil.randomLong();

	private final MockedStatic<DesignLibraryUtil>
		_designLibraryUtilMockedStatic = Mockito.mockStatic(
			DesignLibraryUtil.class);
	private final MockedStatic<GroupLocalServiceUtil>
		_groupLocalServiceUtilMockedStatic = Mockito.mockStatic(
			GroupLocalServiceUtil.class);
	private final MockedStatic<LayoutPageTemplateEntryLocalServiceUtil>
		_layoutPageTemplateEntryLocalServiceUtilMockedStatic =
			Mockito.mockStatic(LayoutPageTemplateEntryLocalServiceUtil.class);

}