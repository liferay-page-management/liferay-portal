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

package com.liferay.asset.categories.internal.layout.display.page;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.model.AssetVocabularyConstants;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.InfoItemHierarchicalReference;
import com.liferay.layout.display.page.LayoutDisplayPageMultiSelectionProvider;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portlet.asset.util.comparator.AssetVocabularyGroupLocalizedTitleComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Lourdes Fernández Besada
 */
@Component(
	immediate = true, service = LayoutDisplayPageMultiSelectionProvider.class
)
public class AssetCategoryLayoutDisplayPageMultiSelectionProvider
	implements LayoutDisplayPageMultiSelectionProvider<AssetCategory> {

	@Override
	public String getClassName() {
		return AssetCategory.class.getName();
	}

	public String getPluralLabel(Locale locale) {
		return LanguageUtil.get(locale, "categories");
	}

	@Override
	public <T extends InfoItemHierarchicalReference> List<T> getSortedList(
		List<T> toSort) {

		Stream<T> stream = toSort.stream();

		Map<Long, Map<Long, T>> itemsByVocabularyIdMap = stream.filter(
			infoItemHierarchicalReference ->
				Objects.equals(
					getClassName(),
					infoItemHierarchicalReference.getClassName()) &&
				(_getClassPK(infoItemHierarchicalReference) > 0)
		).collect(
			Collectors.groupingBy(
				infoItemHierarchicalReference -> {
					AssetCategory assetCategory =
						_assetCategoryLocalService.fetchAssetCategory(
							_getClassPK(infoItemHierarchicalReference));

					return assetCategory.getVocabularyId();
				},
				Collectors.toMap(
					infoItemHierarchicalReference -> _getClassPK(
						infoItemHierarchicalReference),
					Function.identity()))
		);

		List<T> itemsHierarchy = new ArrayList<>();

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		ThemeDisplay themeDisplay = serviceContext.getThemeDisplay();

		for (long vocabularyId : _getOrderedVocabularyIds(themeDisplay)) {
			Map<Long, T> itemsByCategoryId = itemsByVocabularyIdMap.get(
				vocabularyId);

			if (MapUtil.isEmpty(itemsByCategoryId)) {
				continue;
			}

			Set<Long> categoryIds = itemsByCategoryId.keySet();

			Map<Long, List<T>> itemsByParentCategoryIdMap = new HashMap<>();

			for (T infoItemHierarchicalReference : itemsByCategoryId.values()) {
				AssetCategory assetCategory =
					_assetCategoryLocalService.fetchAssetCategory(
						_getClassPK(infoItemHierarchicalReference));

				long parentCategoryId = _getClosestParentCategoryId(
					assetCategory, categoryIds);

				List<T> children = itemsByParentCategoryIdMap.get(
					parentCategoryId);

				if (children == null) {
					children = new ArrayList<>();

					itemsByParentCategoryIdMap.put(parentCategoryId, children);
				}

				children.add(infoItemHierarchicalReference);
			}

			itemsHierarchy.addAll(_getChildren(itemsByParentCategoryIdMap, 0L));
		}

		return itemsHierarchy;
	}

	private <T extends InfoItemHierarchicalReference> List<T> _getChildren(
		Map<Long, List<T>> itemsByParentCategoryIdMap, long parentCategoryId) {

		if (!itemsByParentCategoryIdMap.containsKey(parentCategoryId)) {
			return Collections.emptyList();
		}

		List<T> children = ListUtil.sort(
			itemsByParentCategoryIdMap.get(parentCategoryId),
			Comparator.comparing(
				infoItemHierarchicalReference -> {
					AssetCategory assetCategory =
						_assetCategoryLocalService.fetchAssetCategory(
							_getClassPK(infoItemHierarchicalReference));

					return assetCategory.getName();
				}));

		for (T item : children) {
			item.setChildren(
				(List<InfoItemHierarchicalReference>)_getChildren(
					itemsByParentCategoryIdMap, _getClassPK(item)));
		}

		return children;
	}

	private long _getClassPK(
		InfoItemHierarchicalReference infoItemHierarchicalReference) {

		if (infoItemHierarchicalReference.getInfoItemIdentifier() instanceof
				ClassPKInfoItemIdentifier) {

			ClassPKInfoItemIdentifier classPKInfoItemIdentifier =
				(ClassPKInfoItemIdentifier)
					infoItemHierarchicalReference.getInfoItemIdentifier();

			return classPKInfoItemIdentifier.getClassPK();
		}

		return 0;
	}

	private long _getClosestParentCategoryId(
		AssetCategory assetCategory, Set<Long> availableCategoryIds) {

		String treePath = assetCategory.getTreePath();

		Stream<String> stream = Arrays.stream(treePath.split("/"));

		return stream.filter(
			s -> Validator.isNotNull(s)
		).mapToLong(
			Long::valueOf
		).filter(
			categoryId -> !Objects.equals(
				categoryId, assetCategory.getCategoryId())
		).boxed(
		).sorted(
			Collections.reverseOrder()
		).filter(
			parentCategoryId -> availableCategoryIds.contains(parentCategoryId)
		).findFirst(
		).orElse(
			0L
		);
	}

	private List<Long> _getOrderedVocabularyIds(ThemeDisplay themeDisplay) {
		List<AssetVocabulary> assetVocabularies =
			_assetVocabularyLocalService.getGroupVocabularies(
				new long[] {
					themeDisplay.getCompanyGroupId(),
					themeDisplay.getScopeGroupId()
				},
				new int[] {AssetVocabularyConstants.VISIBILITY_TYPE_PUBLIC});

		if (assetVocabularies.isEmpty()) {
			return Collections.emptyList();
		}

		ListUtil.sort(
			assetVocabularies,
			new AssetVocabularyGroupLocalizedTitleComparator(
				themeDisplay.getScopeGroupId(), themeDisplay.getLocale(),
				true));

		return ListUtil.toList(
			assetVocabularies, AssetVocabulary.VOCABULARY_ID_ACCESSOR);
	}

	@Reference
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Reference
	private AssetVocabularyLocalService _assetVocabularyLocalService;

}