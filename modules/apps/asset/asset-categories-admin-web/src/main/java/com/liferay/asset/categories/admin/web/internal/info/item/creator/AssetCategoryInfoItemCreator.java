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

package com.liferay.asset.categories.admin.web.internal.info.item.creator;

import com.liferay.asset.categories.admin.web.internal.info.item.AssetCategoryInfoItemFields;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetCategoryConstants;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.info.exception.InfoFormException;
import com.liferay.info.field.InfoFieldValue;
import com.liferay.info.item.InfoItemFieldValues;
import com.liferay.info.item.creator.InfoItemCreator;
import com.liferay.info.localized.InfoLocalizedValue;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portlet.asset.service.permission.AssetCategoryPermission;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rubén Pulido
 */
@Component(enabled = false, immediate = true, service = InfoItemCreator.class)
public class AssetCategoryInfoItemCreator
	implements InfoItemCreator<AssetCategory> {

	@Override
	public AssetCategory createFromInfoItemFieldValues(
			InfoItemFieldValues infoItemFieldValues)
		throws InfoFormException {

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		ThemeDisplay themeDisplay = serviceContext.getThemeDisplay();

		if (!_hasPermission(
				themeDisplay.getPermissionChecker(),
				themeDisplay.getScopeGroupId())) {

			return null;
		}

		InfoFieldValue<Object> vocabularyInfoFieldValue =
			infoItemFieldValues.getInfoFieldValue(
				AssetCategoryInfoItemFields.vocabularyInfoField.getName());

		if (vocabularyInfoFieldValue == null) {
			return null;
		}

		InfoLocalizedValue<String> vocabularyInfoLocalizedValue =
			(InfoLocalizedValue)vocabularyInfoFieldValue.getValue();

		AssetVocabulary assetVocabulary =
			_assetVocabularyLocalService.fetchGroupVocabulary(
				themeDisplay.getScopeGroupId(),
				vocabularyInfoLocalizedValue.getValue());

		if (assetVocabulary == null) {
			return null;
		}

		InfoFieldValue<Object> nameInfoFieldValue =
			infoItemFieldValues.getInfoFieldValue(
				AssetCategoryInfoItemFields.nameInfoField.getName());

		if (nameInfoFieldValue == null) {
			return null;
		}

		InfoLocalizedValue<String> nameInfoLocalizedValue =
			(InfoLocalizedValue)nameInfoFieldValue.getValue();

		try {
			return _assetCategoryLocalService.addCategory(
				themeDisplay.getUserId(), themeDisplay.getScopeGroupId(),
				nameInfoLocalizedValue.getValue(),
				assetVocabulary.getVocabularyId(), new ServiceContext());
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}

			throw new InfoFormException(portalException);
		}
	}

	private boolean _hasPermission(
		PermissionChecker permissionChecker, long groupId) {

		boolean hasPermission = false;

		try {
			hasPermission = AssetCategoryPermission.contains(
				permissionChecker, groupId,
				AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID,
				ActionKeys.ADD_CATEGORY);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}

		return hasPermission;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AssetCategoryInfoItemCreator.class);

	@Reference
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Reference
	private AssetVocabularyLocalService _assetVocabularyLocalService;

}