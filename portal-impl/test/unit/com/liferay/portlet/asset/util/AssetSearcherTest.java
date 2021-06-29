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
	}

	@Test
	public void testSearchAllAssetCategoryIdsIncludingInternalCategories()
		throws Exception {

		AssetEntryQuery assetEntryQuery = _getAssetEntryQuery(
			_PUBLIC_AND_INTERNAL_CATEGORY_IDS_ARRAY, _EMPTY_LONG_ARRAY,
			_EMPTY_LONG_ARRAY, _EMPTY_LONG_ARRAY);

		AssetSearcher assetSearcher = new AssetSearcher();

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		BooleanFilter booleanFilter = new BooleanFilter();

		SearchContext mockSearchContext = Mockito.mock(SearchContext.class);

		Mockito.when(
			mockSearchContext.isIncludeInternalAssetCategories()
		).thenReturn(
			true
		);

		_mockAssetCategoryLocalServiceUtil();
		_mockAssetVocabularyLocalServiceUtil();

		assetSearcher.addSearchAssetCategoryIds(
			booleanFilter, mockSearchContext);

		_assertBooleanClausesListSize(booleanFilter, 2, 0, 0);

		List<BooleanClause<Filter>> mustBooleanClauses =
			booleanFilter.getMustBooleanClauses();

		BooleanFilter publicCategoriesBooleanFilter = _getFilter(
			BooleanFilter.class, mustBooleanClauses.get(0));
		BooleanFilter internalCategoriesBooleanFilter = _getFilter(
			BooleanFilter.class, mustBooleanClauses.get(1));

		_assertBooleanClausesListSize(publicCategoriesBooleanFilter, 2, 0, 0);
		_assertBooleanClausesListSize(internalCategoriesBooleanFilter, 1, 0, 0);

		List<BooleanClause<Filter>> publicCategoriesMustBooleanClauses =
			publicCategoriesBooleanFilter.getMustBooleanClauses();
		List<BooleanClause<Filter>> internalCategoriesMustBooleanClauses =
			internalCategoriesBooleanFilter.getMustBooleanClauses();

		TermsFilter publicCategoryIdTermsFilter1 = _getFilter(
			TermsFilter.class, publicCategoriesMustBooleanClauses.get(0));
		TermsFilter publicCategoryIdTermsFilter2 = _getFilter(
			TermsFilter.class, publicCategoriesMustBooleanClauses.get(1));
		TermsFilter internalCategoryIdTermsFilter = _getFilter(
			TermsFilter.class, internalCategoriesMustBooleanClauses.get(0));

		_verifyTermFilter(
			publicCategoryIdTermsFilter1, Field.ASSET_CATEGORY_IDS,
			new String[] {String.valueOf(_PUBLIC_ASSET_CATEGORY_ID_1)});
		_verifyTermFilter(
			publicCategoryIdTermsFilter2, Field.ASSET_CATEGORY_IDS,
			new String[] {String.valueOf(_PUBLIC_ASSET_CATEGORY_ID_2)});
		_verifyTermFilter(
			internalCategoryIdTermsFilter, Field.ASSET_INTERNAL_CATEGORY_IDS,
			new String[] {String.valueOf(_INTERNAL_ASSET_CATEGORY_ID)});
	}

	@Test
	public void testSearchAllAssetCategoryIdsOnlyPublicCategories()
		throws Exception {

		AssetEntryQuery assetEntryQuery = _getAssetEntryQuery(
			_PUBLIC_CATEGORY_IDS_ARRAY, _EMPTY_LONG_ARRAY, _EMPTY_LONG_ARRAY,
			_EMPTY_LONG_ARRAY);

		AssetSearcher assetSearcher = new AssetSearcher();

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		BooleanFilter booleanFilter = new BooleanFilter();

		SearchContext mockSearchContext = Mockito.mock(SearchContext.class);

		Mockito.when(
			mockSearchContext.isIncludeInternalAssetCategories()
		).thenReturn(
			false
		);

		_mockAssetCategoryLocalServiceUtil();
		_mockAssetVocabularyLocalServiceUtil();

		assetSearcher.addSearchAssetCategoryIds(
			booleanFilter, mockSearchContext);

		_assertBooleanClausesListSize(booleanFilter, 1, 0, 0);

		List<BooleanClause<Filter>> mustBooleanClauses =
			booleanFilter.getMustBooleanClauses();

		BooleanFilter publicCategoriesBooleanFilter = _getFilter(
			BooleanFilter.class, mustBooleanClauses.get(0));

		_assertBooleanClausesListSize(publicCategoriesBooleanFilter, 2, 0, 0);

		List<BooleanClause<Filter>> publicCategoriesMustBooleanClauses =
			publicCategoriesBooleanFilter.getMustBooleanClauses();

		TermsFilter publicCategoryIdTermsFilter1 = _getFilter(
			TermsFilter.class, publicCategoriesMustBooleanClauses.get(0));
		TermsFilter publicCategoryIdTermsFilter2 = _getFilter(
			TermsFilter.class, publicCategoriesMustBooleanClauses.get(1));

		_verifyTermFilter(
			publicCategoryIdTermsFilter1, Field.ASSET_CATEGORY_IDS,
			new String[] {String.valueOf(_PUBLIC_ASSET_CATEGORY_ID_1)});
		_verifyTermFilter(
			publicCategoryIdTermsFilter2, Field.ASSET_CATEGORY_IDS,
			new String[] {String.valueOf(_PUBLIC_ASSET_CATEGORY_ID_2)});
	}

	@Test
	public void testSearchAnyAssetCategoryIdsIncludingInternalCategories()
		throws Exception {

		AssetEntryQuery assetEntryQuery = _getAssetEntryQuery(
			_EMPTY_LONG_ARRAY, _PUBLIC_AND_INTERNAL_CATEGORY_IDS_ARRAY,
			_EMPTY_LONG_ARRAY, _EMPTY_LONG_ARRAY);

		AssetSearcher assetSearcher = new AssetSearcher();

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		BooleanFilter booleanFilter = new BooleanFilter();

		SearchContext mockSearchContext = Mockito.mock(SearchContext.class);

		Mockito.when(
			mockSearchContext.isIncludeInternalAssetCategories()
		).thenReturn(
			true
		);

		_mockAssetCategoryLocalServiceUtil();
		_mockAssetVocabularyLocalServiceUtil();

		assetSearcher.addSearchAssetCategoryIds(
			booleanFilter, mockSearchContext);

		_assertBooleanClausesListSize(booleanFilter, 1, 0, 0);

		List<BooleanClause<Filter>> mustBooleanClauses =
			booleanFilter.getMustBooleanClauses();

		BooleanFilter categoryIdsQueryBooleanFilter = _getFilter(
			BooleanFilter.class, mustBooleanClauses.get(0));

		_assertBooleanClausesListSize(categoryIdsQueryBooleanFilter, 0, 0, 2);

		List<BooleanClause<Filter>> categoryIdsShouldBooleanClauses =
			categoryIdsQueryBooleanFilter.getShouldBooleanClauses();

		TermsFilter publicCategoryIdTermsFilter1 = _getFilter(
			TermsFilter.class, categoryIdsShouldBooleanClauses.get(0));
		TermsFilter internalCategoryIdTermsFilter = _getFilter(
			TermsFilter.class, categoryIdsShouldBooleanClauses.get(1));

		_verifyTermFilter(
			publicCategoryIdTermsFilter1, Field.ASSET_CATEGORY_IDS,
			new String[] {
				String.valueOf(_PUBLIC_ASSET_CATEGORY_ID_1),
				String.valueOf(_PUBLIC_ASSET_CATEGORY_ID_2)
			});
		_verifyTermFilter(
			internalCategoryIdTermsFilter, Field.ASSET_INTERNAL_CATEGORY_IDS,
			new String[] {String.valueOf(_INTERNAL_ASSET_CATEGORY_ID)});
	}

	@Test
	public void testSearchAnyAssetCategoryIdsOnlyPublicCategories()
		throws Exception {

		AssetEntryQuery assetEntryQuery = _getAssetEntryQuery(
			_EMPTY_LONG_ARRAY, _PUBLIC_CATEGORY_IDS_ARRAY, _EMPTY_LONG_ARRAY,
			_EMPTY_LONG_ARRAY);

		AssetSearcher assetSearcher = new AssetSearcher();

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		BooleanFilter booleanFilter = new BooleanFilter();

		SearchContext mockSearchContext = Mockito.mock(SearchContext.class);

		Mockito.when(
			mockSearchContext.isIncludeInternalAssetCategories()
		).thenReturn(
			false
		);

		_mockAssetCategoryLocalServiceUtil();
		_mockAssetVocabularyLocalServiceUtil();

		assetSearcher.addSearchAssetCategoryIds(
			booleanFilter, mockSearchContext);

		_assertBooleanClausesListSize(booleanFilter, 1, 0, 0);

		List<BooleanClause<Filter>> mustBooleanClauses =
			booleanFilter.getMustBooleanClauses();

		BooleanFilter categoryIdsQueryBooleanFilter = _getFilter(
			BooleanFilter.class, mustBooleanClauses.get(0));

		_assertBooleanClausesListSize(categoryIdsQueryBooleanFilter, 0, 0, 1);

		List<BooleanClause<Filter>> categoryIdsShouldBooleanClauses =
			categoryIdsQueryBooleanFilter.getShouldBooleanClauses();

		TermsFilter publicCategoryIdTermsFilter = _getFilter(
			TermsFilter.class, categoryIdsShouldBooleanClauses.get(0));

		_verifyTermFilter(
			publicCategoryIdTermsFilter, Field.ASSET_CATEGORY_IDS,
			new String[] {
				String.valueOf(_PUBLIC_ASSET_CATEGORY_ID_1),
				String.valueOf(_PUBLIC_ASSET_CATEGORY_ID_2)
			});
	}

	@Test
	public void testSearchNotAllAssetCategoryIdsIncludingInternalCategories()
		throws Exception {

		AssetEntryQuery assetEntryQuery = _getAssetEntryQuery(
			_EMPTY_LONG_ARRAY, _EMPTY_LONG_ARRAY,
			_PUBLIC_AND_INTERNAL_CATEGORY_IDS_ARRAY, _EMPTY_LONG_ARRAY);

		AssetSearcher assetSearcher = new AssetSearcher();

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		BooleanFilter booleanFilter = new BooleanFilter();

		SearchContext mockSearchContext = Mockito.mock(SearchContext.class);

		Mockito.when(
			mockSearchContext.isIncludeInternalAssetCategories()
		).thenReturn(
			true
		);

		_mockAssetCategoryLocalServiceUtil();
		_mockAssetVocabularyLocalServiceUtil();

		assetSearcher.addSearchAssetCategoryIds(
			booleanFilter, mockSearchContext);

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
		BooleanFilter internalCategoryIdsBooleanFilter = _getFilter(
			BooleanFilter.class, categoryIdsMustBooleanClauses.get(1));

		_assertBooleanClausesListSize(publicCategoryIdsBooleanFilter, 2, 0, 0);
		_assertBooleanClausesListSize(
			internalCategoryIdsBooleanFilter, 1, 0, 0);

		List<BooleanClause<Filter>> publicCategoryIdsMustBooleanClauses =
			publicCategoryIdsBooleanFilter.getMustBooleanClauses();
		List<BooleanClause<Filter>> internalCategoryIdsMustBooleanClauses =
			internalCategoryIdsBooleanFilter.getMustBooleanClauses();

		TermsFilter publicCategoryIdTermsFilter1 = _getFilter(
			TermsFilter.class, publicCategoryIdsMustBooleanClauses.get(0));
		TermsFilter publicCategoryIdTermsFilter2 = _getFilter(
			TermsFilter.class, publicCategoryIdsMustBooleanClauses.get(1));
		TermsFilter internalCategoryIdTermsFilter = _getFilter(
			TermsFilter.class, internalCategoryIdsMustBooleanClauses.get(0));

		_verifyTermFilter(
			publicCategoryIdTermsFilter1, Field.ASSET_CATEGORY_IDS,
			new String[] {String.valueOf(_PUBLIC_ASSET_CATEGORY_ID_1)});
		_verifyTermFilter(
			publicCategoryIdTermsFilter2, Field.ASSET_CATEGORY_IDS,
			new String[] {String.valueOf(_PUBLIC_ASSET_CATEGORY_ID_2)});
		_verifyTermFilter(
			internalCategoryIdTermsFilter, Field.ASSET_INTERNAL_CATEGORY_IDS,
			new String[] {String.valueOf(_INTERNAL_ASSET_CATEGORY_ID)});
	}

	@Test
	public void testSearchNotAllAssetCategoryIdsOnlyPublicCategories()
		throws Exception {

		AssetEntryQuery assetEntryQuery = _getAssetEntryQuery(
			_EMPTY_LONG_ARRAY, _EMPTY_LONG_ARRAY, _PUBLIC_CATEGORY_IDS_ARRAY,
			_EMPTY_LONG_ARRAY);

		AssetSearcher assetSearcher = new AssetSearcher();

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		BooleanFilter booleanFilter = new BooleanFilter();

		SearchContext mockSearchContext = Mockito.mock(SearchContext.class);

		Mockito.when(
			mockSearchContext.isIncludeInternalAssetCategories()
		).thenReturn(
			false
		);

		_mockAssetCategoryLocalServiceUtil();
		_mockAssetVocabularyLocalServiceUtil();

		assetSearcher.addSearchAssetCategoryIds(
			booleanFilter, mockSearchContext);

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

		TermsFilter publicCategoryIdTermsFilter1 = _getFilter(
			TermsFilter.class, publicCategoryIdsMustBooleanClauses.get(0));
		TermsFilter publicCategoryIdTermsFilter2 = _getFilter(
			TermsFilter.class, publicCategoryIdsMustBooleanClauses.get(1));

		_verifyTermFilter(
			publicCategoryIdTermsFilter1, Field.ASSET_CATEGORY_IDS,
			new String[] {String.valueOf(_PUBLIC_ASSET_CATEGORY_ID_1)});
		_verifyTermFilter(
			publicCategoryIdTermsFilter2, Field.ASSET_CATEGORY_IDS,
			new String[] {String.valueOf(_PUBLIC_ASSET_CATEGORY_ID_2)});
	}

	@Test
	public void testSearchNotAnyAssetCategoryIdsIncludingInternalCategories()
		throws Exception {

		AssetEntryQuery assetEntryQuery = _getAssetEntryQuery(
			_EMPTY_LONG_ARRAY, _EMPTY_LONG_ARRAY, _EMPTY_LONG_ARRAY,
			_PUBLIC_AND_INTERNAL_CATEGORY_IDS_ARRAY);

		AssetSearcher assetSearcher = new AssetSearcher();

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		BooleanFilter booleanFilter = new BooleanFilter();

		SearchContext mockSearchContext = Mockito.mock(SearchContext.class);

		Mockito.when(
			mockSearchContext.isIncludeInternalAssetCategories()
		).thenReturn(
			true
		);

		_mockAssetCategoryLocalServiceUtil();
		_mockAssetVocabularyLocalServiceUtil();

		assetSearcher.addSearchAssetCategoryIds(
			booleanFilter, mockSearchContext);

		_assertBooleanClausesListSize(booleanFilter, 0, 2, 0);

		List<BooleanClause<Filter>> mustNotBooleanClauses =
			booleanFilter.getMustNotBooleanClauses();

		TermsFilter publicCategoryIdTermsFilter = _getFilter(
			TermsFilter.class, mustNotBooleanClauses.get(0));
		TermsFilter internalCategoryIdTermsFilter = _getFilter(
			TermsFilter.class, mustNotBooleanClauses.get(1));

		_verifyTermFilter(
			publicCategoryIdTermsFilter, Field.ASSET_CATEGORY_IDS,
			new String[] {
				String.valueOf(_PUBLIC_ASSET_CATEGORY_ID_1),
				String.valueOf(_PUBLIC_ASSET_CATEGORY_ID_2)
			});
		_verifyTermFilter(
			internalCategoryIdTermsFilter, Field.ASSET_INTERNAL_CATEGORY_IDS,
			new String[] {String.valueOf(_INTERNAL_ASSET_CATEGORY_ID)});
	}

	@Test
	public void testSearchNotAnyAssetCategoryIdsOnlyPublicCategories()
		throws Exception {

		AssetEntryQuery assetEntryQuery = _getAssetEntryQuery(
			_EMPTY_LONG_ARRAY, _EMPTY_LONG_ARRAY, _EMPTY_LONG_ARRAY,
			_PUBLIC_CATEGORY_IDS_ARRAY);

		AssetSearcher assetSearcher = new AssetSearcher();

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		BooleanFilter booleanFilter = new BooleanFilter();

		SearchContext mockSearchContext = Mockito.mock(SearchContext.class);

		Mockito.when(
			mockSearchContext.isIncludeInternalAssetCategories()
		).thenReturn(
			false
		);

		_mockAssetCategoryLocalServiceUtil();
		_mockAssetVocabularyLocalServiceUtil();

		assetSearcher.addSearchAssetCategoryIds(
			booleanFilter, mockSearchContext);

		_assertBooleanClausesListSize(booleanFilter, 0, 1, 0);

		List<BooleanClause<Filter>> mustNotBooleanClauses =
			booleanFilter.getMustNotBooleanClauses();

		TermsFilter publicCategoryIdTermsFilter = _getFilter(
			TermsFilter.class, mustNotBooleanClauses.get(0));

		_verifyTermFilter(
			publicCategoryIdTermsFilter, Field.ASSET_CATEGORY_IDS,
			new String[] {
				String.valueOf(_PUBLIC_ASSET_CATEGORY_ID_1),
				String.valueOf(_PUBLIC_ASSET_CATEGORY_ID_2)
			});
	}

	private void _assertBooleanClausesListSize(
		BooleanFilter booleanFilter, int expectedMustBooleanClausesListSize,
		int expectedMustNotBooleanClausesListSize,
		int expectedShouldBooleanClausesListSize) {

		List<BooleanClause<Filter>> mustBooleanClausesList =
			booleanFilter.getMustBooleanClauses();
		List<BooleanClause<Filter>> mustNotBooleanClausesList =
			booleanFilter.getMustNotBooleanClauses();
		List<BooleanClause<Filter>> shouldBooleanClausesList =
			booleanFilter.getShouldBooleanClauses();

		Assert.assertEquals(
			mustBooleanClausesList.toString(),
			expectedMustBooleanClausesListSize, mustBooleanClausesList.size());
		Assert.assertEquals(
			mustNotBooleanClausesList.toString(),
			expectedMustNotBooleanClausesListSize,
			mustNotBooleanClausesList.size());
		Assert.assertEquals(
			shouldBooleanClausesList.toString(),
			expectedShouldBooleanClausesListSize,
			shouldBooleanClausesList.size());
	}

	private AssetEntryQuery _getAssetEntryQuery(
		long[] allCategoryIds, long[] anyCategoryIds, long[] notAllCategoryIds,
		long[] notAnyCategoryIds) {

		AssetEntryQuery assetEntryQuery = Mockito.mock(AssetEntryQuery.class);

		Mockito.when(
			assetEntryQuery.getAllCategoryIds()
		).thenReturn(
			allCategoryIds
		);
		Mockito.when(
			assetEntryQuery.getAnyCategoryIds()
		).thenReturn(
			anyCategoryIds
		);
		Mockito.when(
			assetEntryQuery.getNotAllCategoryIds()
		).thenReturn(
			notAllCategoryIds
		);
		Mockito.when(
			assetEntryQuery.getNotAnyCategoryIds()
		).thenReturn(
			notAnyCategoryIds
		);

		return assetEntryQuery;
	}

	private <T extends Filter> T _getFilter(
		Class<T> clazz, BooleanClause<Filter> booleanClause) {

		Filter clause = booleanClause.getClause();

		Class<?> clausClazz = clause.getClass();

		Assert.assertTrue(clausClazz.isAssignableFrom(clazz));

		return (T)booleanClause.getClause();
	}

	private void _mockAssetCategoryLocalServiceUtil() {
		_assetCategoryLocalService = Mockito.mock(
			AssetCategoryLocalService.class);

		_internalAssetCategory = Mockito.mock(AssetCategory.class);

		_publicAssetCategory1 = Mockito.mock(AssetCategory.class);

		_publicAssetCategory2 = Mockito.mock(AssetCategory.class);

		Mockito.when(
			_internalAssetCategory.getVocabularyId()
		).thenReturn(
			_INTERNAL_ASSET_VOCABULARY_ID
		);
		Mockito.when(
			_publicAssetCategory1.getVocabularyId()
		).thenReturn(
			_PUBLIC_ASSET_VOCABULARY_ID
		);
		Mockito.when(
			_publicAssetCategory2.getVocabularyId()
		).thenReturn(
			_PUBLIC_ASSET_VOCABULARY_ID
		);

		Mockito.when(
			_assetCategoryLocalService.fetchAssetCategory(
				_INTERNAL_ASSET_CATEGORY_ID)
		).thenReturn(
			_internalAssetCategory
		);
		Mockito.when(
			_assetCategoryLocalService.fetchAssetCategory(
				_PUBLIC_ASSET_CATEGORY_ID_1)
		).thenReturn(
			_publicAssetCategory1
		);
		Mockito.when(
			_assetCategoryLocalService.fetchAssetCategory(
				_PUBLIC_ASSET_CATEGORY_ID_2)
		).thenReturn(
			_publicAssetCategory2
		);

		ReflectionTestUtil.setFieldValue(
			AssetCategoryLocalServiceUtil.class, "_service",
			_assetCategoryLocalService);
	}

	private void _mockAssetVocabularyLocalServiceUtil() {
		_assetVocabularyLocalService = Mockito.mock(
			AssetVocabularyLocalService.class);

		_internalAssetVocabulary = Mockito.mock(AssetVocabulary.class);

		_publicAssetVocabulary = Mockito.mock(AssetVocabulary.class);

		Mockito.when(
			_internalAssetVocabulary.getVisibilityType()
		).thenReturn(
			AssetVocabularyConstants.VISIBILITY_TYPE_INTERNAL
		);
		Mockito.when(
			_publicAssetVocabulary.getVisibilityType()
		).thenReturn(
			AssetVocabularyConstants.VISIBILITY_TYPE_PUBLIC
		);

		Mockito.when(
			_assetVocabularyLocalService.fetchAssetVocabulary(
				_INTERNAL_ASSET_VOCABULARY_ID)
		).thenReturn(
			_internalAssetVocabulary
		);
		Mockito.when(
			_assetVocabularyLocalService.fetchAssetVocabulary(
				_PUBLIC_ASSET_VOCABULARY_ID)
		).thenReturn(
			_publicAssetVocabulary
		);

		ReflectionTestUtil.setFieldValue(
			AssetVocabularyLocalServiceUtil.class, "_service",
			_assetVocabularyLocalService);
	}

	private void _verifyTermFilter(
		TermsFilter termsFilter, String expectedFieldName,
		String[] expectedValueArray) {

		Assert.assertEquals(expectedFieldName, termsFilter.getField());
		Assert.assertArrayEquals(expectedValueArray, termsFilter.getValues());
	}

	private static final long[] _EMPTY_LONG_ARRAY = new long[0];

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

	private AssetCategoryLocalService _assetCategoryLocalService;
	private AssetVocabularyLocalService _assetVocabularyLocalService;
	private AssetCategory _internalAssetCategory;
	private AssetVocabulary _internalAssetVocabulary;
	private AssetCategory _publicAssetCategory1;
	private AssetCategory _publicAssetCategory2;
	private AssetVocabulary _publicAssetVocabulary;

}