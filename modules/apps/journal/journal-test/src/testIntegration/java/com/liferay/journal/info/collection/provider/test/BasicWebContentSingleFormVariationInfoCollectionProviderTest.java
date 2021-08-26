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

package com.liferay.journal.info.collection.provider.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.test.util.DDMStructureTestUtil;
import com.liferay.info.collection.provider.CollectionQuery;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.pagination.InfoPage;
import com.liferay.journal.constants.JournalArticleConstants;
import com.liferay.journal.constants.JournalFolderConstants;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.test.util.SearchTestRule;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Víctor Galán
 */
@RunWith(Arquillian.class)
public class BasicWebContentSingleFormVariationInfoCollectionProviderTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		Group group = GroupLocalServiceUtil.getCompanyGroup(
			TestPropsValues.getCompanyId());

		_ddmStructure = _ddmStructureLocalService.getStructure(
			group.getGroupId(), _portal.getClassNameId(JournalArticle.class),
			"BASIC-WEB-CONTENT");

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group, TestPropsValues.getUserId());

		ServiceContextThreadLocal.pushServiceContext(_serviceContext);
	}

	@Test
	public void testFilterTitle() throws Exception {
		String assetTag = "sample-tag";

		_addBasicJournalArticle(assetTag);

		_addBasicJournalArticle(RandomTestUtil.randomString());

		JournalTestUtil.addArticle(
			_group.getGroupId(),
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID);

		CollectionQuery collectionQuery = new CollectionQuery();

		collectionQuery.setConfiguration(
			HashMapBuilder.put(
				Field.ASSET_TAG_NAMES, new String[] {assetTag}
			).build());

		InfoPage<JournalArticle> collectionInfoPage =
			_infoCollectionProvider.getCollectionInfoPage(collectionQuery);

		Assert.assertEquals(1, collectionInfoPage.getTotalCount());
	}

	@Test
	public void testGetOnlyBasicJournalArticles() throws Exception {
		_addBasicJournalArticle(RandomTestUtil.randomString());
		_addBasicJournalArticle(RandomTestUtil.randomString());

		JournalTestUtil.addArticle(
			_group.getGroupId(),
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID);

		InfoPage<JournalArticle> collectionInfoPage =
			_infoCollectionProvider.getCollectionInfoPage(
				new CollectionQuery());

		Assert.assertEquals(2, collectionInfoPage.getTotalCount());
	}

	@Rule
	public SearchTestRule searchTestRule = new SearchTestRule();

	private void _addBasicJournalArticle(String assetTag) throws Exception {
		ServiceContext serviceContext = (ServiceContext)_serviceContext.clone();

		serviceContext.setAssetTagNames(new String[] {assetTag});

		JournalTestUtil.addArticleWithXMLContent(
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			JournalArticleConstants.CLASS_NAME_ID_DEFAULT, 0,
			DDMStructureTestUtil.getSampleStructuredContent(),
			_ddmStructure.getStructureKey(), null, LocaleUtil.getSiteDefault(),
			null, serviceContext);
	}

	private DDMStructure _ddmStructure;

	@Inject
	private DDMStructureLocalService _ddmStructureLocalService;

	@DeleteAfterTestRun
	private Group _group;

	@Inject(
		filter = "component.name=com.liferay.journal.web.internal.info.collection.provider.BasicWebContentSingleFormVariationInfoCollectionProvider"
	)
	private InfoCollectionProvider<JournalArticle> _infoCollectionProvider;

	@Inject
	private Portal _portal;

	private ServiceContext _serviceContext;

}