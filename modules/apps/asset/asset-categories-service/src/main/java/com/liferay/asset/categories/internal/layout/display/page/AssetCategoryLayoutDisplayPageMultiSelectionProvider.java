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
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.InfoItemHierarchicalReference;
import com.liferay.layout.display.page.LayoutDisplayPageMultiSelectionProvider;
import com.liferay.portal.kernel.language.LanguageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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

		Map<Long, List<T>> itemsByVocabularyIdMap = stream.filter(
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
				Collectors.toList())
		);

		List<T> itemsHierarchy = new ArrayList<>();

		for (List<T> vocabularyItems : itemsByVocabularyIdMap.values()) {
			itemsHierarchy.addAll(vocabularyItems);
		}

		return itemsHierarchy;
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

	@Reference
	private AssetCategoryLocalService _assetCategoryLocalService;

}