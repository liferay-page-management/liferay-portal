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

package com.liferay.journal.internal.upgrade.v4_3_1;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.LoggingTimer;
import com.liferay.portal.kernel.util.PortalUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Georgel Pop
 */
public class ArticleAssetsBasicWebContentClassTypeIdUpgradeProcess
	extends UpgradeProcess {

	public ArticleAssetsBasicWebContentClassTypeIdUpgradeProcess(
		AssetEntryLocalService assetEntryLocalService,
		CompanyLocalService companyLocalService,
		GroupLocalService groupLocalService,
		DDMStructureLocalService ddmStructureLocalService) {

		_assetEntryLocalService = assetEntryLocalService;
		_companyLocalService = companyLocalService;
		_groupLocalService = groupLocalService;
		_ddmStructureLocalService = ddmStructureLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_updateDefaultDraftArticleAssets();
	}

	private boolean _isUpgradeNeeded(long classNameId) throws Exception {
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select count(*) from JournalArticle inner join ",
					"AssetEntry on JournalArticle.resourcePrimKey = ",
					"AssetEntry.classPK where AssetEntry.classTypeId = 0 and ",
					"AssetEntry.classNameId = ", classNameId));
			ResultSet resultSet = preparedStatement.executeQuery()) {

			if (resultSet.next()) {
				int count = resultSet.getInt(1);

				if (count > 0) {
					return true;
				}
			}

			return false;
		}
	}

	private void _updateDefaultDraftArticleAssets() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			long classNameId = PortalUtil.getClassNameId(
				JournalArticle.class.getName());

			if (_isUpgradeNeeded(classNameId)) {
				_companyLocalService.forEachCompanyId(
					companyId -> _updateDefaultDraftArticleAssets(companyId));
			}
			else {
				if (_log.isInfoEnabled()) {
					_log.info(
						StringBundler.concat(
							"No need to upgrade asset entries with ",
							"classNameId = ", classNameId,
							" and classTypeId = 0"));
				}
			}
		}
	}

	private void _updateDefaultDraftArticleAssets(long companyId)
		throws Exception {

		Group companyGroup = _groupLocalService.getCompanyGroup(companyId);
		String structureKey = "BASIC-WEB-CONTENT";

		DDMStructure ddmStructure = _ddmStructureLocalService.getStructure(
			companyGroup.getGroupId(),
			PortalUtil.getClassNameId(JournalArticle.class.getName()),
			structureKey);

		long basicWebContentStructureId;

		if (ddmStructure != null) {
			basicWebContentStructureId = ddmStructure.getStructureId();

			try (PreparedStatement preparedStatement =
					connection.prepareStatement(
						StringBundler.concat(
							"select resourcePrimKey, indexable from ",
							"JournalArticle where companyId = ", companyId,
							" and ddmtemplatekey = 'BASIC-WEB-CONTENT' "));
				ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {
					long resourcePrimKey = resultSet.getLong("resourcePrimKey");

					AssetEntry assetEntry = _assetEntryLocalService.fetchEntry(
						JournalArticle.class.getName(), resourcePrimKey);

					if (assetEntry == null) {
						if (_log.isWarnEnabled()) {
							_log.warn(
								StringBundler.concat(
									"Journal article with resource primary ",
									"key ", resourcePrimKey,
									" does not have associated asset entry"));
						}

						continue;
					}

					long classTypeId = assetEntry.getClassTypeId();

					if (classTypeId != basicWebContentStructureId) {
						assetEntry.setClassTypeId(basicWebContentStructureId);

						_assetEntryLocalService.updateAssetEntry(assetEntry);
					}
				}
			}
		}
		else {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"No DDMStructure with structure key ", structureKey,
						" found in Global site", companyGroup.getGroupId(),
						" for companyId ", companyId));
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ArticleAssetsBasicWebContentClassTypeIdUpgradeProcess.class);

	private final AssetEntryLocalService _assetEntryLocalService;
	private final CompanyLocalService _companyLocalService;
	private final DDMStructureLocalService _ddmStructureLocalService;
	private final GroupLocalService _groupLocalService;

}