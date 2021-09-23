/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.portal.search.tuning.rankings.web.internal.index.creation.model.listener;

import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.tuning.rankings.web.internal.index.RankingIndexCreator;
import com.liferay.portal.search.tuning.rankings.web.internal.index.RankingIndexReader;
import com.liferay.portal.search.tuning.rankings.web.internal.index.name.RankingIndexNameBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Wade Cao
 */
public class RankingIndexCreationCompanyModelListenerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		_rankingIndexCreationCompanyModelListener =
			new RankingIndexCreationCompanyModelListener();

		ReflectionTestUtil.setFieldValue(
			_rankingIndexCreationCompanyModelListener, "_rankingIndexCreator",
			_rankingIndexCreator);
		ReflectionTestUtil.setFieldValue(
			_rankingIndexCreationCompanyModelListener,
			"_rankingIndexNameBuilder", _rankingIndexNameBuilder);
		ReflectionTestUtil.setFieldValue(
			_rankingIndexCreationCompanyModelListener, "_rankingIndexReader",
			_rankingIndexReader);
	}

	@Test
	public void testOnAfterCreateRankingIndexReaderFalse() {
		_setUpRankingIndexReader(false);

		_rankingIndexCreationCompanyModelListener.onAfterCreate(
			Mockito.mock(Company.class));

		Mockito.verify(
			_rankingIndexReader, Mockito.times(1)
		).isExists(
			Mockito.anyObject()
		);

		Mockito.verify(
			_rankingIndexCreator, Mockito.times(1)
		).create(
			Mockito.anyObject()
		);
	}

	@Test
	public void testOnAfterCreateRankingIndexReaderTrue() {
		_setUpRankingIndexReader(true);

		_rankingIndexCreationCompanyModelListener.onAfterCreate(
			Mockito.mock(Company.class));

		Mockito.verify(
			_rankingIndexReader, Mockito.times(1)
		).isExists(
			Mockito.anyObject()
		);

		Mockito.verify(
			_rankingIndexCreator, Mockito.times(0)
		).create(
			Mockito.anyObject()
		);
	}

	@Test
	public void testOnBeforeRemoveRankingIndexReaderExistsFalse() {
		_setUpRankingIndexReader(false);

		_rankingIndexCreationCompanyModelListener.onBeforeRemove(
			Mockito.mock(Company.class));

		Mockito.verify(
			_rankingIndexReader, Mockito.times(1)
		).isExists(
			Mockito.anyObject()
		);

		Mockito.verify(
			_rankingIndexCreator, Mockito.times(0)
		).delete(
			Mockito.anyObject()
		);
	}

	@Test
	public void testOnBeforeRemoveRankingIndexReaderExistsTrue() {
		_setUpRankingIndexReader(true);

		_rankingIndexCreationCompanyModelListener.onBeforeRemove(
			Mockito.mock(Company.class));

		Mockito.verify(
			_rankingIndexReader, Mockito.times(1)
		).isExists(
			Mockito.anyObject()
		);

		Mockito.verify(
			_rankingIndexCreator, Mockito.times(1)
		).delete(
			Mockito.anyObject()
		);
	}

	private void _setUpRankingIndexReader(boolean exist) {
		Mockito.doReturn(
			exist
		).when(
			_rankingIndexReader
		).isExists(
			Mockito.anyObject()
		);
	}

	private RankingIndexCreationCompanyModelListener
		_rankingIndexCreationCompanyModelListener;

	@Mock
	private RankingIndexCreator _rankingIndexCreator;

	@Mock
	private RankingIndexNameBuilder _rankingIndexNameBuilder;

	@Mock
	private RankingIndexReader _rankingIndexReader;

}