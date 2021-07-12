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

package com.liferay.portlet.asset.util;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.model.AssetVocabularyConstants;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.kernel.service.AssetCategoryLocalServiceUtil;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.asset.kernel.service.AssetVocabularyLocalServiceUtil;
import com.liferay.asset.kernel.service.persistence.AssetEntryQuery;
import com.liferay.portal.kernel.search.BooleanClause;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.search.filter.TermsFilter;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.registry.BasicRegistryImpl;
import com.liferay.registry.RegistryUtil;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Lourdes Fernández Besada
 */
public class AssetSearcherTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		RegistryUtil.setRegistry(new BasicRegistryImpl());

		_mockAssetCategoryLocalServiceUtil();
		_mockAssetVocabularyLocalServiceUtil();
	}

	@Test
	public void testSearchAllAssetCategoryIdsIncludingInternalCategories()
		throws Exception {

		BooleanFilter booleanFilter = new BooleanFilter();

		AssetSearcher assetSearcher = new AssetSearcher();

		AssetEntryQuery assetEntryQuery = new AssetEntryQuery();

		assetEntryQuery.setAllCategoryIds(
			_PUBLIC_AND_INTERNAL_CATEGORY_IDS_ARRAY);

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		SearchContext searchContext = new SearchContext();

		searchContext.setIncludeInternalAssetCategories(true);

		assetSearcher.addSearchAssetCategoryIds(booleanFilter, searchContext);

		_assertBooleanClausesListSize(booleanFilter, 2, 0, 0);

		List<BooleanClause<Filter>> mustBooleanClauses =
			booleanFilter.getMustBooleanClauses();

		BooleanFilter publicCategoriesBooleanFilter = _getFilter(
			BooleanFilter.class, mustBooleanClauses.get(0));

		_assertBooleanClausesListSize(publicCategoriesBooleanFilter, 2, 0, 0);

		List<BooleanClause<Filter>> publicCategoriesMustBooleanClauses =
			publicCategoriesBooleanFilter.getMustBooleanClauses();

		_verifyTermFilter(
			publicCategoriesMustBooleanClauses.get(0), Field.ASSET_CATEGORY_IDS,
			_PUBLIC_ASSET_CATEGORY_ID_1);
		_verifyTermFilter(
			publicCategoriesMustBooleanClauses.get(1), Field.ASSET_CATEGORY_IDS,
			_PUBLIC_ASSET_CATEGORY_ID_2);

		BooleanFilter internalCategoriesBooleanFilter = _getFilter(
			BooleanFilter.class, mustBooleanClauses.get(1));

		_assertBooleanClausesListSize(internalCategoriesBooleanFilter, 1, 0, 0);

		List<BooleanClause<Filter>> internalCategoriesMustBooleanClauses =
			internalCategoriesBooleanFilter.getMustBooleanClauses();

		_verifyTermFilter(
			internalCategoriesMustBooleanClauses.get(0),
			Field.ASSET_INTERNAL_CATEGORY_IDS, _INTERNAL_ASSET_CATEGORY_ID);
	}

	@Test
	public void testSearchAllAssetCategoryIdsOnlyPublicCategories()
		throws Exception {

		BooleanFilter booleanFilter = new BooleanFilter();

		AssetSearcher assetSearcher = new AssetSearcher();

		AssetEntryQuery assetEntryQuery = new AssetEntryQuery();

		assetEntryQuery.setAllCategoryIds(_PUBLIC_CATEGORY_IDS_ARRAY);

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		SearchContext searchContext = new SearchContext();

		searchContext.setIncludeInternalAssetCategories(false);

		assetSearcher.addSearchAssetCategoryIds(booleanFilter, searchContext);

		_assertBooleanClausesListSize(booleanFilter, 1, 0, 0);

		List<BooleanClause<Filter>> mustBooleanClauses =
			booleanFilter.getMustBooleanClauses();

		BooleanFilter publicCategoriesBooleanFilter = _getFilter(
			BooleanFilter.class, mustBooleanClauses.get(0));

		_assertBooleanClausesListSize(publicCategoriesBooleanFilter, 2, 0, 0);

		List<BooleanClause<Filter>> publicCategoriesMustBooleanClauses =
			publicCategoriesBooleanFilter.getMustBooleanClauses();

		_verifyTermFilter(
			publicCategoriesMustBooleanClauses.get(0), Field.ASSET_CATEGORY_IDS,
			_PUBLIC_ASSET_CATEGORY_ID_1);
		_verifyTermFilter(
			publicCategoriesMustBooleanClauses.get(1), Field.ASSET_CATEGORY_IDS,
			_PUBLIC_ASSET_CATEGORY_ID_2);
	}

	@Test
	public void testSearchAnyAssetCategoryIdsIncludingInternalCategories()
		throws Exception {

		BooleanFilter booleanFilter = new BooleanFilter();

		AssetSearcher assetSearcher = new AssetSearcher();

		AssetEntryQuery assetEntryQuery = new AssetEntryQuery();

		assetEntryQuery.setAnyCategoryIds(
			_PUBLIC_AND_INTERNAL_CATEGORY_IDS_ARRAY);

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		SearchContext searchContext = new SearchContext();

		searchContext.setIncludeInternalAssetCategories(true);

		assetSearcher.addSearchAssetCategoryIds(booleanFilter, searchContext);

		_assertBooleanClausesListSize(booleanFilter, 1, 0, 0);

		List<BooleanClause<Filter>> mustBooleanClauses =
			booleanFilter.getMustBooleanClauses();

		BooleanFilter categoryIdsQueryBooleanFilter = _getFilter(
			BooleanFilter.class, mustBooleanClauses.get(0));

		_assertBooleanClausesListSize(categoryIdsQueryBooleanFilter, 0, 0, 2);

		List<BooleanClause<Filter>> categoryIdsShouldBooleanClauses =
			categoryIdsQueryBooleanFilter.getShouldBooleanClauses();

		_verifyTermFilter(
			categoryIdsShouldBooleanClauses.get(0), Field.ASSET_CATEGORY_IDS,
			_PUBLIC_ASSET_CATEGORY_ID_1, _PUBLIC_ASSET_CATEGORY_ID_2);
		_verifyTermFilter(
			categoryIdsShouldBooleanClauses.get(1),
			Field.ASSET_INTERNAL_CATEGORY_IDS, _INTERNAL_ASSET_CATEGORY_ID);
	}

	@Test
	public void testSearchAnyAssetCategoryIdsOnlyPublicCategories()
		throws Exception {

		BooleanFilter booleanFilter = new BooleanFilter();

		AssetSearcher assetSearcher = new AssetSearcher();

		AssetEntryQuery assetEntryQuery = new AssetEntryQuery();

		assetEntryQuery.setAnyCategoryIds(_PUBLIC_CATEGORY_IDS_ARRAY);

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		SearchContext searchContext = new SearchContext();

		searchContext.setIncludeInternalAssetCategories(false);

		assetSearcher.addSearchAssetCategoryIds(booleanFilter, searchContext);

		_assertBooleanClausesListSize(booleanFilter, 1, 0, 0);

		List<BooleanClause<Filter>> mustBooleanClauses =
			booleanFilter.getMustBooleanClauses();

		BooleanFilter categoryIdsQueryBooleanFilter = _getFilter(
			BooleanFilter.class, mustBooleanClauses.get(0));

		_assertBooleanClausesListSize(categoryIdsQueryBooleanFilter, 0, 0, 1);

		List<BooleanClause<Filter>> categoryIdsShouldBooleanClauses =
			categoryIdsQueryBooleanFilter.getShouldBooleanClauses();

		_verifyTermFilter(
			categoryIdsShouldBooleanClauses.get(0), Field.ASSET_CATEGORY_IDS,
			_PUBLIC_ASSET_CATEGORY_ID_1, _PUBLIC_ASSET_CATEGORY_ID_2);
	}

	@Test
	public void testSearchNotAllAssetCategoryIdsIncludingInternalCategories()
		throws Exception {

		BooleanFilter booleanFilter = new BooleanFilter();

		AssetSearcher assetSearcher = new AssetSearcher();

		AssetEntryQuery assetEntryQuery = new AssetEntryQuery();

		assetEntryQuery.setNotAllCategoryIds(
			_PUBLIC_AND_INTERNAL_CATEGORY_IDS_ARRAY);

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		SearchContext searchContext = new SearchContext();

		searchContext.setIncludeInternalAssetCategories(true);

		assetSearcher.addSearchAssetCategoryIds(booleanFilter, searchContext);

		_assertBooleanClausesListSize(booleanFilter, 0, 1, 0);

		List<BooleanClause<Filter>> mustNotBooleanClauses =
			booleanFilter.getMustNotBooleanClauses();

		BooleanFilter categoryIdsQueryBooleanFilter = _getFilter(
			BooleanFilter.class, mustNotBooleanClauses.get(0));

		_assertBooleanClausesListSize(categoryIdsQueryBooleanFilter, 2, 0, 0);

		List<BooleanClause<Filter>> categoryIdsMustBooleanClauses =
			categoryIdsQueryBooleanFilter.getMustBooleanClauses();

		BooleanFilter publicCategoryIdsBooleanFilter = _getFilter(
			BooleanFilter.class, categoryIdsMustBooleanClauses.get(0));

		_assertBooleanClausesListSize(publicCategoryIdsBooleanFilter, 2, 0, 0);

		List<BooleanClause<Filter>> publicCategoryIdsMustBooleanClauses =
			publicCategoryIdsBooleanFilter.getMustBooleanClauses();

		_verifyTermFilter(
			publicCategoryIdsMustBooleanClauses.get(0),
			Field.ASSET_CATEGORY_IDS, _PUBLIC_ASSET_CATEGORY_ID_1);
		_verifyTermFilter(
			publicCategoryIdsMustBooleanClauses.get(1),
			Field.ASSET_CATEGORY_IDS, _PUBLIC_ASSET_CATEGORY_ID_2);

		BooleanFilter internalCategoryIdsBooleanFilter = _getFilter(
			BooleanFilter.class, categoryIdsMustBooleanClauses.get(1));

		_assertBooleanClausesListSize(
			internalCategoryIdsBooleanFilter, 1, 0, 0);

		List<BooleanClause<Filter>> internalCategoryIdsMustBooleanClauses =
			internalCategoryIdsBooleanFilter.getMustBooleanClauses();

		_verifyTermFilter(
			internalCategoryIdsMustBooleanClauses.get(0),
			Field.ASSET_INTERNAL_CATEGORY_IDS, _INTERNAL_ASSET_CATEGORY_ID);
	}

	@Test
	public void testSearchNotAllAssetCategoryIdsOnlyPublicCategories()
		throws Exception {

		BooleanFilter booleanFilter = new BooleanFilter();

		AssetSearcher assetSearcher = new AssetSearcher();

		AssetEntryQuery assetEntryQuery = new AssetEntryQuery();

		assetEntryQuery.setNotAllCategoryIds(_PUBLIC_CATEGORY_IDS_ARRAY);

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		SearchContext searchContext = new SearchContext();

		searchContext.setIncludeInternalAssetCategories(false);

		assetSearcher.addSearchAssetCategoryIds(booleanFilter, searchContext);

		_assertBooleanClausesListSize(booleanFilter, 0, 1, 0);

		List<BooleanClause<Filter>> mustNotBooleanClauses =
			booleanFilter.getMustNotBooleanClauses();

		BooleanFilter categoryIdsQueryBooleanFilter = _getFilter(
			BooleanFilter.class, mustNotBooleanClauses.get(0));

		_assertBooleanClausesListSize(categoryIdsQueryBooleanFilter, 1, 0, 0);

		List<BooleanClause<Filter>> categoryIdsMustBooleanClauses =
			categoryIdsQueryBooleanFilter.getMustBooleanClauses();

		BooleanFilter publicCategoryIdsBooleanFilter = _getFilter(
			BooleanFilter.class, categoryIdsMustBooleanClauses.get(0));

		_assertBooleanClausesListSize(publicCategoryIdsBooleanFilter, 2, 0, 0);

		List<BooleanClause<Filter>> publicCategoryIdsMustBooleanClauses =
			publicCategoryIdsBooleanFilter.getMustBooleanClauses();

		_verifyTermFilter(
			publicCategoryIdsMustBooleanClauses.get(0),
			Field.ASSET_CATEGORY_IDS, _PUBLIC_ASSET_CATEGORY_ID_1);
		_verifyTermFilter(
			publicCategoryIdsMustBooleanClauses.get(1),
			Field.ASSET_CATEGORY_IDS, _PUBLIC_ASSET_CATEGORY_ID_2);
	}

	@Test
	public void testSearchNotAnyAssetCategoryIdsIncludingInternalCategories()
		throws Exception {

		BooleanFilter booleanFilter = new BooleanFilter();

		AssetSearcher assetSearcher = new AssetSearcher();

		AssetEntryQuery assetEntryQuery = new AssetEntryQuery();

		assetEntryQuery.setNotAnyCategoryIds(
			_PUBLIC_AND_INTERNAL_CATEGORY_IDS_ARRAY);

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		SearchContext searchContext = new SearchContext();

		searchContext.setIncludeInternalAssetCategories(true);

		assetSearcher.addSearchAssetCategoryIds(booleanFilter, searchContext);

		_assertBooleanClausesListSize(booleanFilter, 0, 2, 0);

		List<BooleanClause<Filter>> mustNotBooleanClauses =
			booleanFilter.getMustNotBooleanClauses();

		_verifyTermFilter(
			mustNotBooleanClauses.get(0), Field.ASSET_CATEGORY_IDS,
			_PUBLIC_ASSET_CATEGORY_ID_1, _PUBLIC_ASSET_CATEGORY_ID_2);
		_verifyTermFilter(
			mustNotBooleanClauses.get(1), Field.ASSET_INTERNAL_CATEGORY_IDS,
			_INTERNAL_ASSET_CATEGORY_ID);
	}

	@Test
	public void testSearchNotAnyAssetCategoryIdsOnlyPublicCategories()
		throws Exception {

		BooleanFilter booleanFilter = new BooleanFilter();

		AssetSearcher assetSearcher = new AssetSearcher();

		AssetEntryQuery assetEntryQuery = new AssetEntryQuery();

		assetEntryQuery.setNotAnyCategoryIds(_PUBLIC_CATEGORY_IDS_ARRAY);

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		SearchContext searchContext = new SearchContext();

		searchContext.setIncludeInternalAssetCategories(false);

		assetSearcher.addSearchAssetCategoryIds(booleanFilter, searchContext);

		_assertBooleanClausesListSize(booleanFilter, 0, 1, 0);

		List<BooleanClause<Filter>> mustNotBooleanClauses =
			booleanFilter.getMustNotBooleanClauses();

		_verifyTermFilter(
			mustNotBooleanClauses.get(0), Field.ASSET_CATEGORY_IDS,
			_PUBLIC_ASSET_CATEGORY_ID_1, _PUBLIC_ASSET_CATEGORY_ID_2);
	}

	private void _assertBooleanClausesListSize(
		BooleanFilter booleanFilter, int expectedMustBooleanClausesListSize,
		int expectedMustNotBooleanClausesListSize,
		int expectedShouldBooleanClausesListSize) {

		List<BooleanClause<Filter>> mustBooleanClausesList =
			booleanFilter.getMustBooleanClauses();

		Assert.assertEquals(
			mustBooleanClausesList.toString(),
			expectedMustBooleanClausesListSize, mustBooleanClausesList.size());

		List<BooleanClause<Filter>> mustNotBooleanClausesList =
			booleanFilter.getMustNotBooleanClauses();

		Assert.assertEquals(
			mustNotBooleanClausesList.toString(),
			expectedMustNotBooleanClausesListSize,
			mustNotBooleanClausesList.size());

		List<BooleanClause<Filter>> shouldBooleanClausesList =
			booleanFilter.getShouldBooleanClauses();

		Assert.assertEquals(
			shouldBooleanClausesList.toString(),
			expectedShouldBooleanClausesListSize,
			shouldBooleanClausesList.size());
	}

	private <T extends Filter> T _getFilter(
		Class<T> clazz, BooleanClause<Filter> booleanClause) {

		Filter clause = booleanClause.getClause();

		Class<?> clauseClazz = clause.getClass();

		Assert.assertTrue(clauseClazz.isAssignableFrom(clazz));

		return (T)booleanClause.getClause();
	}

	private void _mockAssetCategoryLocalServiceUtil() {
		AssetCategory internalAssetCategory = Mockito.mock(AssetCategory.class);

		Mockito.when(
			internalAssetCategory.getVocabularyId()
		).thenReturn(
			_INTERNAL_ASSET_VOCABULARY_ID
		);

		AssetCategory publicAssetCategory1 = Mockito.mock(AssetCategory.class);

		Mockito.when(
			publicAssetCategory1.getVocabularyId()
		).thenReturn(
			_PUBLIC_ASSET_VOCABULARY_ID
		);

		AssetCategory publicAssetCategory2 = Mockito.mock(AssetCategory.class);

		Mockito.when(
			publicAssetCategory2.getVocabularyId()
		).thenReturn(
			_PUBLIC_ASSET_VOCABULARY_ID
		);

		AssetCategoryLocalService assetCategoryLocalService = Mockito.mock(
			AssetCategoryLocalService.class);

		Mockito.when(
			assetCategoryLocalService.fetchAssetCategory(
				_INTERNAL_ASSET_CATEGORY_ID)
		).thenReturn(
			internalAssetCategory
		);
		Mockito.when(
			assetCategoryLocalService.fetchAssetCategory(
				_PUBLIC_ASSET_CATEGORY_ID_1)
		).thenReturn(
			publicAssetCategory1
		);
		Mockito.when(
			assetCategoryLocalService.fetchAssetCategory(
				_PUBLIC_ASSET_CATEGORY_ID_2)
		).thenReturn(
			publicAssetCategory2
		);

		ReflectionTestUtil.setFieldValue(
			AssetCategoryLocalServiceUtil.class, "_service",
			assetCategoryLocalService);
	}

	private void _mockAssetVocabularyLocalServiceUtil() {
		AssetVocabularyLocalService assetVocabularyLocalService = Mockito.mock(
			AssetVocabularyLocalService.class);

		AssetVocabulary internalAssetVocabulary = Mockito.mock(
			AssetVocabulary.class);

		Mockito.when(
			internalAssetVocabulary.getVisibilityType()
		).thenReturn(
			AssetVocabularyConstants.VISIBILITY_TYPE_INTERNAL
		);

		Mockito.when(
			assetVocabularyLocalService.fetchAssetVocabulary(
				_INTERNAL_ASSET_VOCABULARY_ID)
		).thenReturn(
			internalAssetVocabulary
		);

		AssetVocabulary publicAssetVocabulary = Mockito.mock(
			AssetVocabulary.class);

		Mockito.when(
			publicAssetVocabulary.getVisibilityType()
		).thenReturn(
			AssetVocabularyConstants.VISIBILITY_TYPE_PUBLIC
		);

		Mockito.when(
			assetVocabularyLocalService.fetchAssetVocabulary(
				_PUBLIC_ASSET_VOCABULARY_ID)
		).thenReturn(
			publicAssetVocabulary
		);

		ReflectionTestUtil.setFieldValue(
			AssetVocabularyLocalServiceUtil.class, "_service",
			assetVocabularyLocalService);
	}

	private void _verifyTermFilter(
		BooleanClause<Filter> booleanClause, String expectedFieldName,
		long... expectedValuesArray) {

		TermsFilter termsFilter = _getFilter(TermsFilter.class, booleanClause);

		Assert.assertEquals(expectedFieldName, termsFilter.getField());
		Assert.assertArrayEquals(
			ArrayUtil.toStringArray(expectedValuesArray),
			termsFilter.getValues());
	}

	private static final long _INTERNAL_ASSET_CATEGORY_ID = 42;

	private static final long _INTERNAL_ASSET_VOCABULARY_ID = 43;

	private static final long[] _PUBLIC_AND_INTERNAL_CATEGORY_IDS_ARRAY = {
		44, 45, _INTERNAL_ASSET_CATEGORY_ID
	};

	private static final long _PUBLIC_ASSET_CATEGORY_ID_1 = 44;

	private static final long _PUBLIC_ASSET_CATEGORY_ID_2 = 45;

	private static final long _PUBLIC_ASSET_VOCABULARY_ID = 46;

	private static final long[] _PUBLIC_CATEGORY_IDS_ARRAY = {
		_PUBLIC_ASSET_CATEGORY_ID_1, _PUBLIC_ASSET_CATEGORY_ID_2
	};

}