/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.internal.dto.v1_0.mapper;

import com.liferay.headless.delivery.dto.v1_0.Fragment;
import com.liferay.headless.delivery.dto.v1_0.PageDropZoneDefinition;
import com.liferay.headless.delivery.dto.v1_0.PageElement;
import com.liferay.layout.helper.DropZoneAllowedFragmentEntriesHelper;
import com.liferay.layout.util.structure.DropZoneLayoutStructureItem;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Lourdes Fernández Besada
 */
public class DropZoneLayoutStructureItemMapperTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_dropZoneLayoutStructureItemMapper =
			new DropZoneLayoutStructureItemMapper(
				_dropZoneAllowedFragmentEntriesHelper, _groupLocalService);

		Mockito.reset(
			_dropZoneAllowedFragmentEntriesHelper, _groupLocalService);
	}

	@Test
	@TestInfo("LPP-65865")
	public void testGetPageElement() {
		String allowedFragmentEntryKey = RandomTestUtil.randomString();

		Group group = Mockito.mock(Group.class);

		Mockito.when(
			group.getCompanyId()
		).thenReturn(
			_COMPANY_ID
		);

		Mockito.when(
			_groupLocalService.fetchGroup(_GROUP_ID)
		).thenReturn(
			group
		);

		DropZoneLayoutStructureItem dropZoneLayoutStructureItem =
			_getDropZoneLayoutStructureItem(false);

		Mockito.when(
			_dropZoneAllowedFragmentEntriesHelper.getFragmentEntryKeys(
				_COMPANY_ID, dropZoneLayoutStructureItem, _GROUP_ID)
		).thenReturn(
			Arrays.asList(allowedFragmentEntryKey)
		);

		_testGetPageElement(
			dropZoneLayoutStructureItem, "allowedFragments",
			Arrays.asList(allowedFragmentEntryKey));

		String unallowedFragmentEntryKey = RandomTestUtil.randomString();

		dropZoneLayoutStructureItem = _getDropZoneLayoutStructureItem(true);

		Mockito.when(
			_dropZoneAllowedFragmentEntriesHelper.getFragmentEntryKeys(
				_COMPANY_ID, dropZoneLayoutStructureItem, _GROUP_ID)
		).thenReturn(
			Arrays.asList(unallowedFragmentEntryKey)
		);

		_testGetPageElement(
			dropZoneLayoutStructureItem, "unallowedFragments",
			Arrays.asList(unallowedFragmentEntryKey));

		dropZoneLayoutStructureItem = _getDropZoneLayoutStructureItem(false);

		Mockito.when(
			_dropZoneAllowedFragmentEntriesHelper.getFragmentEntryKeys(
				_COMPANY_ID, dropZoneLayoutStructureItem, _GROUP_ID)
		).thenReturn(
			null
		);

		_testGetPageElement(
			dropZoneLayoutStructureItem, "allowedFragments", Arrays.asList());

		Mockito.when(
			_groupLocalService.fetchGroup(_GROUP_ID)
		).thenReturn(
			null
		);

		dropZoneLayoutStructureItem = _getDropZoneLayoutStructureItem(false);

		Mockito.when(
			_dropZoneAllowedFragmentEntriesHelper.getFragmentEntryKeys(
				0, dropZoneLayoutStructureItem, _GROUP_ID)
		).thenReturn(
			Arrays.asList(allowedFragmentEntryKey)
		);

		_testGetPageElement(
			dropZoneLayoutStructureItem, "allowedFragments",
			Arrays.asList(allowedFragmentEntryKey));
	}

	private DropZoneLayoutStructureItem _getDropZoneLayoutStructureItem(
		boolean allowNewFragmentEntries) {

		DropZoneLayoutStructureItem dropZoneLayoutStructureItem =
			new DropZoneLayoutStructureItem(
				RandomTestUtil.randomString(), RandomTestUtil.randomString());

		dropZoneLayoutStructureItem.setAllowNewFragmentEntries(
			allowNewFragmentEntries);

		return dropZoneLayoutStructureItem;
	}

	@SuppressWarnings("unchecked")
	private void _testGetPageElement(
		DropZoneLayoutStructureItem dropZoneLayoutStructureItem,
		String expectedFragmentSettingsKey,
		List<String> expectedFragmentEntryKeys) {

		PageElement pageElement =
			_dropZoneLayoutStructureItemMapper.getPageElement(
				_GROUP_ID, dropZoneLayoutStructureItem, false, false);

		PageDropZoneDefinition pageDropZoneDefinition =
			(PageDropZoneDefinition)pageElement.getDefinition();

		Map<String, Fragment[]> fragmentSettings =
			(Map<String, Fragment[]>)
				pageDropZoneDefinition.getFragmentSettings();

		Assert.assertEquals(
			fragmentSettings.toString(), 1, fragmentSettings.size());
		Assert.assertTrue(
			fragmentSettings.containsKey(expectedFragmentSettingsKey));

		Fragment[] fragments = fragmentSettings.get(
			expectedFragmentSettingsKey);

		Assert.assertEquals(
			Arrays.toString(fragments), expectedFragmentEntryKeys.size(),
			fragments.length);

		for (int i = 0; i < fragments.length; i++) {
			Assert.assertEquals(
				expectedFragmentEntryKeys.get(i), fragments[i].getKey());
		}
	}

	private static final long _COMPANY_ID = RandomTestUtil.randomLong();

	private static final long _GROUP_ID = RandomTestUtil.randomLong();

	private final DropZoneAllowedFragmentEntriesHelper
		_dropZoneAllowedFragmentEntriesHelper = Mockito.mock(
			DropZoneAllowedFragmentEntriesHelper.class);
	private DropZoneLayoutStructureItemMapper
		_dropZoneLayoutStructureItemMapper;
	private final GroupLocalService _groupLocalService = Mockito.mock(
		GroupLocalService.class);

}