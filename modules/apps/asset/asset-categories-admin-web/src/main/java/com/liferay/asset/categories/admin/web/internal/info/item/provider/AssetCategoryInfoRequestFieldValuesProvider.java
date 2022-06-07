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

package com.liferay.asset.categories.admin.web.internal.info.item.provider;

import com.liferay.asset.categories.admin.web.internal.info.item.AssetCategoryInfoItemFields;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.info.field.InfoFieldValue;
import com.liferay.info.item.InfoItemFieldValues;
import com.liferay.info.item.provider.InfoRequestFieldValuesProvider;
import com.liferay.info.localized.InfoLocalizedValue;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rubén Pulido
 */
@Component(
	enabled = false, immediate = true,
	service = InfoRequestFieldValuesProvider.class
)
public class AssetCategoryInfoRequestFieldValuesProvider
	implements InfoRequestFieldValuesProvider<AssetCategory> {

	@Override
	public InfoItemFieldValues create(HttpServletRequest httpServletRequest) {
		List<InfoFieldValue<Object>> assetCategoryInfoFieldValues =
			new ArrayList<>();

		HttpServletRequest originalHttpServletRequest =
			_portal.getOriginalServletRequest(httpServletRequest);

		String description = ParamUtil.getString(
			originalHttpServletRequest,
			AssetCategoryInfoItemFields.descriptionInfoField.getName());
		String name = ParamUtil.getString(
			originalHttpServletRequest,
			AssetCategoryInfoItemFields.nameInfoField.getName());
		String vocabularyName = ParamUtil.getString(
			originalHttpServletRequest,
			AssetCategoryInfoItemFields.vocabularyInfoField.getName());

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		ThemeDisplay themeDisplay = serviceContext.getThemeDisplay();

		Locale locale = themeDisplay.getLocale();

		assetCategoryInfoFieldValues.add(
			new InfoFieldValue<>(
				AssetCategoryInfoItemFields.descriptionInfoField,
				InfoLocalizedValue.<String>builder(
				).defaultLocale(
					locale
				).values(
					HashMapBuilder.put(
						locale, description
					).build()
				).build()));
		assetCategoryInfoFieldValues.add(
			new InfoFieldValue<>(
				AssetCategoryInfoItemFields.nameInfoField,
				InfoLocalizedValue.<String>builder(
				).defaultLocale(
					locale
				).values(
					HashMapBuilder.put(
						locale, name
					).build()
				).build()));

		AssetVocabulary assetVocabulary =
			_assetVocabularyLocalService.fetchGroupVocabulary(
				themeDisplay.getScopeGroupId(), vocabularyName);

		if (assetVocabulary != null) {
			assetCategoryInfoFieldValues.add(
				new InfoFieldValue<>(
					AssetCategoryInfoItemFields.vocabularyInfoField,
					InfoLocalizedValue.<String>builder(
					).defaultLocale(
						LocaleUtil.fromLanguageId(
							assetVocabulary.getDefaultLanguageId())
					).values(
						assetVocabulary.getTitleMap()
					).build()));
		}

		return InfoItemFieldValues.builder(
		).infoFieldValues(
			assetCategoryInfoFieldValues
		).build();
	}

	@Reference
	private AssetVocabularyLocalService _assetVocabularyLocalService;

	@Reference
	private Portal _portal;

}