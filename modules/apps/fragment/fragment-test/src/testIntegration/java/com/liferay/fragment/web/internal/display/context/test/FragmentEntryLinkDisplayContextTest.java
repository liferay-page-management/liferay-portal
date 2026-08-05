/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.web.internal.display.context.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.test.util.DisplayContextTestUtil;
import com.liferay.fragment.test.util.FragmentEntryTestUtil;
import com.liferay.fragment.test.util.FragmentTestUtil;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.lang.reflect.Constructor;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Georgel Pop
 */
@RunWith(Arquillian.class)
public class FragmentEntryLinkDisplayContextTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		_constructor = DisplayContextTestUtil.getConstructor(
			"com.liferay.fragment.web.internal.display.context." +
				"FragmentEntryLinkDisplayContext");
	}

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@Test
	@TestInfo("LPD-99652")
	public void testGetFragmentEntryLinkName() throws Exception {
		FragmentEntry fragmentEntry = _addFragmentEntry();

		FragmentEntryLink fragmentEntryLink =
			FragmentTestUtil.addFragmentEntryLink(
				fragmentEntry, RandomTestUtil.randomLong());

		Assert.assertEquals(
			StringPool.BLANK,
			ReflectionTestUtil.invoke(
				_getFragmentEntryLinkDisplayContext(fragmentEntry),
				"getFragmentEntryLinkName",
				new Class<?>[] {FragmentEntryLink.class}, fragmentEntryLink));
	}

	@Test
	@TestInfo("LPD-99652")
	public void testGetFragmentEntryLinkTypeLabel() throws Exception {
		FragmentEntry fragmentEntry = _addFragmentEntry();

		FragmentEntryLink fragmentEntryLink =
			FragmentTestUtil.addFragmentEntryLink(
				fragmentEntry, RandomTestUtil.randomLong());

		Assert.assertEquals(
			StringPool.BLANK,
			ReflectionTestUtil.invoke(
				_getFragmentEntryLinkDisplayContext(fragmentEntry),
				"getFragmentEntryLinkTypeLabel",
				new Class<?>[] {FragmentEntryLink.class}, fragmentEntryLink));
	}

	@Test
	@TestInfo("LPD-99652")
	public void testGetSearchContainer() throws Exception {
		FragmentEntry fragmentEntry = _addFragmentEntry();

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		FragmentEntryLink fragmentEntryLink =
			FragmentTestUtil.addFragmentEntryLink(
				fragmentEntry, layout.getPlid());

		FragmentTestUtil.addFragmentEntryLink(
			fragmentEntry, RandomTestUtil.randomLong());

		SearchContainer<FragmentEntryLink> searchContainer =
			ReflectionTestUtil.invoke(
				_getFragmentEntryLinkDisplayContext(fragmentEntry),
				"getSearchContainer", new Class<?>[0]);

		List<FragmentEntryLink> fragmentEntryLinks =
			searchContainer.getResults();

		Assert.assertEquals(
			fragmentEntryLinks.toString(), 1, fragmentEntryLinks.size());
		Assert.assertEquals(fragmentEntryLink, fragmentEntryLinks.get(0));

		Assert.assertEquals(1, searchContainer.getTotal());
	}

	private FragmentEntry _addFragmentEntry() throws Exception {
		FragmentCollection fragmentCollection =
			FragmentTestUtil.addFragmentCollection(_group.getGroupId());

		return FragmentEntryTestUtil.addFragmentEntry(
			fragmentCollection.getFragmentCollectionId());
	}

	private Object _getFragmentEntryLinkDisplayContext(
			FragmentEntry fragmentEntry)
		throws Exception {

		return DisplayContextTestUtil.createDisplayContext(
			_constructor, _group,
			HashMapBuilder.put(
				"fragmentEntryId",
				String.valueOf(fragmentEntry.getFragmentEntryId())
			).build());
	}

	private static Constructor<?> _constructor;

	@DeleteAfterTestRun
	private Group _group;

}