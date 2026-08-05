/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.web.internal.display.context.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.test.util.DisplayContextTestUtil;
import com.liferay.fragment.test.util.FragmentEntryTestUtil;
import com.liferay.fragment.test.util.FragmentTestUtil;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
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
public class FragmentDisplayContextTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		_constructor = DisplayContextTestUtil.getConstructor(_CLASS_NAME);
	}

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_fragmentCollection = FragmentTestUtil.addFragmentCollection(
			_group.getGroupId());
	}

	@Test
	@TestInfo("LPD-99652")
	public void testLogMissingLayoutPlids() throws Exception {
		FragmentEntry fragmentEntry1 = FragmentEntryTestUtil.addFragmentEntry(
			_fragmentCollection.getFragmentCollectionId());

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		FragmentTestUtil.addFragmentEntryLink(fragmentEntry1, layout.getPlid());

		FragmentEntry fragmentEntry2 = FragmentEntryTestUtil.addFragmentEntry(
			_fragmentCollection.getFragmentCollectionId());

		long missingLayoutPlid1 = RandomTestUtil.randomLong();

		long missingLayoutPlid2 = missingLayoutPlid1 + 1;

		FragmentTestUtil.addFragmentEntryLink(
			fragmentEntry2, missingLayoutPlid2);

		FragmentTestUtil.addFragmentEntryLink(
			fragmentEntry2, missingLayoutPlid1);

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				_CLASS_NAME, LoggerTestUtil.DEBUG)) {

			ReflectionTestUtil.invoke(
				_getFragmentDisplayContext(),
				"getFragmentEntriesSearchContainer", new Class<?>[0]);

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 1, logEntries.size());

			LogEntry logEntry = logEntries.get(0);

			Assert.assertEquals(
				StringBundler.concat(
					"Fragment entry ",
					fragmentEntry2.getExternalReferenceCode(),
					" references missing layouts ", missingLayoutPlid1, ", ",
					missingLayoutPlid2),
				logEntry.getMessage());
		}
	}

	private Object _getFragmentDisplayContext() throws Exception {
		return DisplayContextTestUtil.createDisplayContext(
			_constructor, _group,
			HashMapBuilder.put(
				"fragmentCollectionId",
				String.valueOf(_fragmentCollection.getFragmentCollectionId())
			).build());
	}

	private static final String _CLASS_NAME =
		"com.liferay.fragment.web.internal.display.context." +
			"FragmentDisplayContext";

	private static Constructor<?> _constructor;

	@DeleteAfterTestRun
	private FragmentCollection _fragmentCollection;

	@DeleteAfterTestRun
	private Group _group;

}