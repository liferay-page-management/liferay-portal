/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.analytics.asset;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.info.item.InfoItemServiceRegistryUtil;
import com.liferay.info.item.provider.InfoItemObjectVariationProvider;
import com.liferay.journal.model.JournalArticle;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Georgel Pop
 */
public class AssetAnalyticsAttributesProviderTest {

	@ClassRule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@After
	public void tearDown() {
		_featureFlagManagerUtilMockedStatic.close();
		_infoItemServiceRegistryUtilMockedStatic.close();
	}

	@Test
	@TestInfo("LPD-83537")
	public void testBuildAssetAnalyticsAttributes() {
		AssetEntry journalArticle = _mockAssetEntry(
			_CLASS_NAME_JOURNAL_ARTICLE, RandomTestUtil.randomLong(),
			RandomTestUtil.randomLong());

		AssetRenderer<?> assetRenderer = Mockito.mock(AssetRenderer.class);

		Mockito.when(
			assetRenderer.getTitle(LocaleUtil.US)
		).thenReturn(
			"Quoted \" and <tag>"
		);

		AssetAnalyticsAttributesProvider provider1 =
			new AssetAnalyticsAttributesProvider(
				journalArticle, assetRenderer, LocaleUtil.US, true);

		Assert.assertEquals(
			StringPool.BLANK,
			provider1.buildAttributes(
				AssetAnalyticsAttributesProvider.ACTION_IMPRESSION,
				AssetAnalyticsAttributesProvider.FIELD_TITLE));

		_featureFlagManagerUtilMockedStatic.when(
			() -> FeatureFlagManagerUtil.isEnabled(
				Mockito.anyLong(), Mockito.eq("LPD-81914"))
		).thenReturn(
			true
		);

		AssetAnalyticsAttributesProvider provider2 =
			new AssetAnalyticsAttributesProvider(null, null, null, true);

		Assert.assertEquals(
			StringPool.BLANK,
			provider2.buildAttributes(
				AssetAnalyticsAttributesProvider.ACTION_IMPRESSION,
				AssetAnalyticsAttributesProvider.FIELD_TITLE));

		AssetAnalyticsAttributesProvider provider3 =
			new AssetAnalyticsAttributesProvider(
				journalArticle, assetRenderer, LocaleUtil.US, false);

		Assert.assertEquals(
			StringPool.BLANK,
			provider3.buildAttributes(
				AssetAnalyticsAttributesProvider.ACTION_IMPRESSION,
				AssetAnalyticsAttributesProvider.FIELD_TITLE));

		String attributes = provider1.buildAttributes(
			AssetAnalyticsAttributesProvider.ACTION_IMPRESSION,
			AssetAnalyticsAttributesProvider.FIELD_TITLE);

		Assert.assertFalse(attributes.contains("\"Quoted \""));
		Assert.assertTrue(attributes.contains("data-analytics-asset-title=\""));
		Assert.assertTrue(
			attributes.contains(
				"data-analytics-asset-field=\"" +
					AssetAnalyticsAttributesProvider.FIELD_TITLE + "\""));
	}

	@Test
	@TestInfo("LPD-83537")
	public void testGetAssetAnalyticsAttributes() {
		AssetAnalyticsAttributesProvider provider1 =
			new AssetAnalyticsAttributesProvider(null, null, null, false);

		Assert.assertTrue(
			provider1.getAttributes(
				AssetAnalyticsAttributesProvider.ACTION_IMPRESSION,
				AssetAnalyticsAttributesProvider.FIELD_TITLE
			).isEmpty());

		long journalArticleClassPK = RandomTestUtil.randomLong();

		AssetEntry journalArticle = _mockAssetEntry(
			_CLASS_NAME_JOURNAL_ARTICLE, journalArticleClassPK,
			RandomTestUtil.randomLong());

		AssetRenderer<?> assetRenderer = Mockito.mock(AssetRenderer.class);

		String title = RandomTestUtil.randomString();

		Mockito.when(
			assetRenderer.getTitle(LocaleUtil.US)
		).thenReturn(
			title
		);

		String structureId = RandomTestUtil.randomString();

		InfoItemObjectVariationProvider<Object>
			infoItemObjectVariationProvider = Mockito.mock(
				InfoItemObjectVariationProvider.class);

		Mockito.when(
			infoItemObjectVariationProvider.getInfoItemFormVariationKey(
				Mockito.any())
		).thenReturn(
			structureId
		);

		_infoItemServiceRegistryUtilMockedStatic.when(
			() -> InfoItemServiceRegistryUtil.getFirstInfoItemService(
				InfoItemObjectVariationProvider.class,
				_CLASS_NAME_JOURNAL_ARTICLE)
		).thenReturn(
			infoItemObjectVariationProvider
		);

		AssetAnalyticsAttributesProvider provider2 =
			new AssetAnalyticsAttributesProvider(
				journalArticle, assetRenderer, LocaleUtil.US, false);

		Map<String, String> analyticsAttributes1 = provider2.getAttributes(
			AssetAnalyticsAttributesProvider.ACTION_VIEW,
			AssetAnalyticsAttributesProvider.FIELD_CONTENT);

		Assert.assertEquals(
			AssetAnalyticsAttributesProvider.ACTION_VIEW,
			analyticsAttributes1.get("data-analytics-asset-action"));
		Assert.assertEquals(
			"1.0", analyticsAttributes1.get("data-analytics-asset-cmsversion"));
		Assert.assertEquals(
			AssetAnalyticsAttributesProvider.FIELD_CONTENT,
			analyticsAttributes1.get("data-analytics-asset-field"));
		Assert.assertEquals(
			String.valueOf(journalArticleClassPK),
			analyticsAttributes1.get("data-analytics-asset-id"));
		Assert.assertEquals(
			structureId,
			analyticsAttributes1.get("data-analytics-asset-subtype"));
		Assert.assertEquals(
			title, analyticsAttributes1.get("data-analytics-asset-title"));
		Assert.assertEquals(
			"web-content",
			analyticsAttributes1.get("data-analytics-asset-type"));

		AssetAnalyticsAttributesProvider provider3 =
			new AssetAnalyticsAttributesProvider(
				_mockAssetEntry(
					"com.liferay.blogs.model.BlogsEntry",
					RandomTestUtil.randomLong(), RandomTestUtil.randomLong()),
				null, null, false);

		Map<String, String> analyticsAttributes2 = provider3.getAttributes(
			AssetAnalyticsAttributesProvider.ACTION_IMPRESSION,
			AssetAnalyticsAttributesProvider.FIELD_TITLE);

		Assert.assertNull(
			analyticsAttributes2.get("data-analytics-asset-subtype"));
		Assert.assertEquals(
			"blog", analyticsAttributes2.get("data-analytics-asset-type"));

		AssetAnalyticsAttributesProvider provider4 =
			new AssetAnalyticsAttributesProvider(
				_mockAssetEntry(
					"com.liferay.object.model.ObjectDefinition#99",
					RandomTestUtil.randomLong(), RandomTestUtil.randomLong()),
				null, null, false);

		Map<String, String> analyticsAttributes3 = provider4.getAttributes(
			AssetAnalyticsAttributesProvider.ACTION_IMPRESSION,
			AssetAnalyticsAttributesProvider.FIELD_TITLE);

		Assert.assertEquals(
			"object-entry",
			analyticsAttributes3.get("data-analytics-asset-type"));

		AssetAnalyticsAttributesProvider provider5 =
			new AssetAnalyticsAttributesProvider(
				journalArticle, null, null, false);

		Map<String, String> analyticsAttributes4 = provider5.getAttributes(
			AssetAnalyticsAttributesProvider.ACTION_IMPRESSION,
			AssetAnalyticsAttributesProvider.FIELD_TITLE);

		Assert.assertNull(
			analyticsAttributes4.get("data-analytics-asset-title"));

		AssetAnalyticsAttributesProvider provider6 =
			new AssetAnalyticsAttributesProvider(
				journalArticle, assetRenderer, null, false);

		Map<String, String> analyticsAttributes5 = provider6.getAttributes(
			AssetAnalyticsAttributesProvider.ACTION_IMPRESSION,
			AssetAnalyticsAttributesProvider.FIELD_TITLE);

		Assert.assertNull(
			analyticsAttributes5.get("data-analytics-asset-title"));

		AssetAnalyticsAttributesProvider provider7 =
			new AssetAnalyticsAttributesProvider(
				journalArticle, assetRenderer, LocaleUtil.US, false);

		Map<String, String> analyticsAttributes6 = provider7.getAttributes(
			AssetAnalyticsAttributesProvider.ACTION_IMPRESSION, null);

		Assert.assertNull(
			analyticsAttributes6.get("data-analytics-asset-field"));

		AssetAnalyticsAttributesProvider provider8 =
			new AssetAnalyticsAttributesProvider(
				_mockAssetEntry(
					"com.liferay.document.library.kernel.model.DLFileEntry",
					RandomTestUtil.randomLong(), RandomTestUtil.randomLong()),
				null, null, false);

		Map<String, String> analyticsAttributes7 = provider8.getAttributes(
			AssetAnalyticsAttributesProvider.ACTION_IMPRESSION,
			AssetAnalyticsAttributesProvider.FIELD_TITLE);

		Assert.assertEquals(
			"document", analyticsAttributes7.get("data-analytics-asset-type"));

		String ddmStructureKey = "MY-STRUCTURE-KEY";

		AssetRenderer<?> journalArticleAssetRenderer = Mockito.mock(
			AssetRenderer.class);

		JournalArticle journalArticleObject = Mockito.mock(
			JournalArticle.class);

		Mockito.when(
			journalArticleAssetRenderer.getAssetObject()
		).thenReturn(
			journalArticleObject
		);

		Mockito.when(
			journalArticleObject.getDDMStructureKey()
		).thenReturn(
			ddmStructureKey
		);

		AssetAnalyticsAttributesProvider provider9 =
			new AssetAnalyticsAttributesProvider(
				journalArticle, journalArticleAssetRenderer, LocaleUtil.US,
				false);

		Map<String, String> analyticsAttributes8 = provider9.getAttributes(
			AssetAnalyticsAttributesProvider.ACTION_IMPRESSION,
			AssetAnalyticsAttributesProvider.FIELD_TITLE);

		Assert.assertEquals(
			StringUtil.toLowerCase(ddmStructureKey),
			analyticsAttributes8.get("data-analytics-asset-subtype"));

		Mockito.when(
			journalArticleObject.getDDMStructureKey()
		).thenReturn(
			null
		);

		Map<String, String> analyticsAttributes9 = provider9.getAttributes(
			AssetAnalyticsAttributesProvider.ACTION_IMPRESSION,
			AssetAnalyticsAttributesProvider.FIELD_TITLE);

		Assert.assertEquals(
			structureId,
			analyticsAttributes9.get("data-analytics-asset-subtype"));
	}

	@Test
	@TestInfo("LPD-83537")
	public void testGetAssetAnalyticsCMSVersion() {
		long companyId = RandomTestUtil.randomLong();

		AssetEntry journalArticle = _mockAssetEntry(
			_CLASS_NAME_JOURNAL_ARTICLE, RandomTestUtil.randomLong(),
			RandomTestUtil.randomLong(), companyId);

		AssetAnalyticsAttributesProvider provider1 =
			new AssetAnalyticsAttributesProvider(
				journalArticle, null, null, false);

		Map<String, String> analyticsAttributes1 = provider1.getAttributes(
			AssetAnalyticsAttributesProvider.ACTION_VIEW,
			AssetAnalyticsAttributesProvider.FIELD_CONTENT);

		Assert.assertEquals(
			"1.0", analyticsAttributes1.get("data-analytics-asset-cmsversion"));

		String objectDefinitionClassName =
			"com.liferay.object.model.ObjectDefinition#42";

		AssetEntry objectEntry = _mockAssetEntry(
			objectDefinitionClassName, RandomTestUtil.randomLong(),
			RandomTestUtil.randomLong(), companyId);

		AssetAnalyticsAttributesProvider provider2 =
			new AssetAnalyticsAttributesProvider(
				objectEntry, null, null, false);

		Snapshot<ObjectDefinitionLocalService> originalSnapshot =
			(Snapshot<ObjectDefinitionLocalService>)
				ReflectionTestUtil.getFieldValue(
					AssetAnalyticsAttributesProvider.class,
					"_objectDefinitionLocalServiceSnapshot");

		try {
			@SuppressWarnings("unchecked")
			Snapshot<ObjectDefinitionLocalService> mockSnapshot = Mockito.mock(
				Snapshot.class);

			ReflectionTestUtil.setFieldValue(
				AssetAnalyticsAttributesProvider.class,
				"_objectDefinitionLocalServiceSnapshot", mockSnapshot);

			Mockito.when(
				mockSnapshot.get()
			).thenReturn(
				null
			);

			Map<String, String> analyticsAttributes2 = provider2.getAttributes(
				AssetAnalyticsAttributesProvider.ACTION_VIEW,
				AssetAnalyticsAttributesProvider.FIELD_CONTENT);

			Assert.assertEquals(
				"1.0",
				analyticsAttributes2.get("data-analytics-asset-cmsversion"));

			ObjectDefinitionLocalService objectDefinitionLocalService =
				Mockito.mock(ObjectDefinitionLocalService.class);

			Mockito.when(
				mockSnapshot.get()
			).thenReturn(
				objectDefinitionLocalService
			);

			Mockito.when(
				objectDefinitionLocalService.fetchObjectDefinitionByClassName(
					companyId, objectDefinitionClassName)
			).thenReturn(
				null
			);

			Map<String, String> analyticsAttributes3 = provider2.getAttributes(
				AssetAnalyticsAttributesProvider.ACTION_VIEW,
				AssetAnalyticsAttributesProvider.FIELD_CONTENT);

			Assert.assertEquals(
				"1.0",
				analyticsAttributes3.get("data-analytics-asset-cmsversion"));

			ObjectDefinition objectDefinition = Mockito.mock(
				ObjectDefinition.class);

			Mockito.when(
				objectDefinitionLocalService.fetchObjectDefinitionByClassName(
					companyId, objectDefinitionClassName)
			).thenReturn(
				objectDefinition
			);

			Mockito.when(
				objectDefinition.isCMS()
			).thenReturn(
				false
			);

			Map<String, String> analyticsAttributes4 = provider2.getAttributes(
				AssetAnalyticsAttributesProvider.ACTION_VIEW,
				AssetAnalyticsAttributesProvider.FIELD_CONTENT);

			Assert.assertEquals(
				"1.0",
				analyticsAttributes4.get("data-analytics-asset-cmsversion"));

			Mockito.when(
				objectDefinition.isCMS()
			).thenReturn(
				true
			);

			Map<String, String> analyticsAttributes5 = provider2.getAttributes(
				AssetAnalyticsAttributesProvider.ACTION_VIEW,
				AssetAnalyticsAttributesProvider.FIELD_CONTENT);

			Assert.assertEquals(
				"2.0",
				analyticsAttributes5.get("data-analytics-asset-cmsversion"));
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				AssetAnalyticsAttributesProvider.class,
				"_objectDefinitionLocalServiceSnapshot", originalSnapshot);
		}
	}

	@Test
	@TestInfo("LPD-83537")
	public void testGetAssetAnalyticsTypeForObjectEntry() {
		long companyId = RandomTestUtil.randomLong();
		String objectDefinitionClassName =
			"com.liferay.object.model.ObjectDefinition#42";

		AssetEntry objectEntry = _mockAssetEntry(
			objectDefinitionClassName, RandomTestUtil.randomLong(),
			RandomTestUtil.randomLong(), companyId);

		AssetAnalyticsAttributesProvider provider =
			new AssetAnalyticsAttributesProvider(
				objectEntry, null, null, false);

		Snapshot<ObjectDefinitionLocalService> originalSnapshot =
			(Snapshot<ObjectDefinitionLocalService>)
				ReflectionTestUtil.getFieldValue(
					AssetAnalyticsAttributesProvider.class,
					"_objectDefinitionLocalServiceSnapshot");

		try {
			@SuppressWarnings("unchecked")
			Snapshot<ObjectDefinitionLocalService> mockSnapshot = Mockito.mock(
				Snapshot.class);

			ReflectionTestUtil.setFieldValue(
				AssetAnalyticsAttributesProvider.class,
				"_objectDefinitionLocalServiceSnapshot", mockSnapshot);

			ObjectDefinitionLocalService objectDefinitionLocalService =
				Mockito.mock(ObjectDefinitionLocalService.class);

			Mockito.when(
				mockSnapshot.get()
			).thenReturn(
				objectDefinitionLocalService
			);

			ObjectDefinition objectDefinition = Mockito.mock(
				ObjectDefinition.class);

			Mockito.when(
				objectDefinitionLocalService.fetchObjectDefinitionByClassName(
					companyId, objectDefinitionClassName)
			).thenReturn(
				objectDefinition
			);

			Mockito.when(
				objectDefinition.getName()
			).thenReturn(
				"MyCMSType"
			);

			Map<String, String> analyticsAttributes1 = provider.getAttributes(
				AssetAnalyticsAttributesProvider.ACTION_VIEW,
				AssetAnalyticsAttributesProvider.FIELD_CONTENT);

			Assert.assertEquals(
				"my-cms-type",
				analyticsAttributes1.get("data-analytics-asset-type"));

			Mockito.when(
				objectDefinition.getName()
			).thenReturn(
				null
			);

			Map<String, String> analyticsAttributes2 = provider.getAttributes(
				AssetAnalyticsAttributesProvider.ACTION_VIEW,
				AssetAnalyticsAttributesProvider.FIELD_CONTENT);

			Assert.assertEquals(
				"object-entry",
				analyticsAttributes2.get("data-analytics-asset-type"));
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				AssetAnalyticsAttributesProvider.class,
				"_objectDefinitionLocalServiceSnapshot", originalSnapshot);
		}
	}

	private AssetEntry _mockAssetEntry(
		String className, long classPK, long groupId) {

		AssetEntry assetEntry = Mockito.mock(AssetEntry.class);

		Mockito.when(
			assetEntry.getClassName()
		).thenReturn(
			className
		);

		Mockito.when(
			assetEntry.getClassPK()
		).thenReturn(
			classPK
		);

		Mockito.when(
			assetEntry.getGroupId()
		).thenReturn(
			groupId
		);

		return assetEntry;
	}

	private AssetEntry _mockAssetEntry(
		String className, long classPK, long groupId, long companyId) {

		AssetEntry assetEntry = _mockAssetEntry(className, classPK, groupId);

		Mockito.when(
			assetEntry.getCompanyId()
		).thenReturn(
			companyId
		);

		return assetEntry;
	}

	private static final String _CLASS_NAME_JOURNAL_ARTICLE =
		"com.liferay.journal.model.JournalArticle";

	private final MockedStatic<FeatureFlagManagerUtil>
		_featureFlagManagerUtilMockedStatic = Mockito.mockStatic(
			FeatureFlagManagerUtil.class);
	private final MockedStatic<InfoItemServiceRegistryUtil>
		_infoItemServiceRegistryUtilMockedStatic = Mockito.mockStatic(
			InfoItemServiceRegistryUtil.class);

}