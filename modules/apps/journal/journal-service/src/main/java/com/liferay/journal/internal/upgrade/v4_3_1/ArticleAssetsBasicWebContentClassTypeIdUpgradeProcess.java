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
import com.liferay.asset.kernel.model.AssetEntryTable;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
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
import java.sql.SQLException;

/**
 * @author Georgel Pop
 */
public class ArticleAssetsBasicWebContentClassTypeIdUpgradeProcess
	extends UpgradeProcess {

	public ArticleAssetsBasicWebContentClassTypeIdUpgradeProcess(
		AssetEntryLocalService assetEntryLocalService,
		CompanyLocalService companyLocalService,
		DDMStructureLocalService ddmStructureLocalService,
		GroupLocalService groupLocalService) {

		_assetEntryLocalService = assetEntryLocalService;
		_companyLocalService = companyLocalService;
		_ddmStructureLocalService = ddmStructureLocalService;
		_groupLocalService = groupLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_updateDefaultDraftArticleAssets();
	}

	private boolean _isUpgradeNeeded(long classNameId) throws Exception {
		ActionableDynamicQuery actionableDynamicQuery =
			_assetEntryLocalService.getActionableDynamicQuery();

		actionableDynamicQuery.setAddCriteriaMethod(
			dynamicQuery -> {
				Property classNameIdProperty = PropertyFactoryUtil.forName(
					"classNameId");

				dynamicQuery.add(classNameIdProperty.eq(classNameId));

				Property classTypeIdProperty = PropertyFactoryUtil.forName(
					"classTypeId");

				dynamicQuery.add(classTypeIdProperty.eq(0L));
			});

		long count = actionableDynamicQuery.performCount();

		if (count > 0) {
			return true;
		}

		return false;
	}

	private void _updateDefaultDraftArticleAssets() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			long classNameId = PortalUtil.getClassNameId(
				JournalArticle.class.getName());

			if (!_isUpgradeNeeded(classNameId)) {
				if (_log.isInfoEnabled()) {
					_log.info(
						StringBundler.concat(
							"No need to upgrade asset entries with ",
							"classNameId = ", classNameId,
							" and classTypeId = 0"));
				}

				return;
			}

			_companyLocalService.forEachCompanyId(
				companyId -> _updateDefaultDraftArticleAssets(
					companyId, classNameId));
		}
	}

	private void _updateDefaultDraftArticleAssets(
			long companyId, long classNameId)
		throws Exception {

		Group companyGroup = _groupLocalService.getCompanyGroup(companyId);
		String structureKey = "BASIC-WEB-CONTENT";

		DDMStructure ddmStructure = _ddmStructureLocalService.fetchStructure(
			companyGroup.getGroupId(),
			PortalUtil.getClassNameId(JournalArticle.class.getName()),
			structureKey);

		if (ddmStructure == null) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"No DDMStructure with structure key ", structureKey,
						" found in Global site", companyGroup.getGroupId(),
						" for companyId ", companyId));
			}

			return;
		}

		long basicWebContentStructureId = ddmStructure.getStructureId();

		if (hasColumnType(
				AssetEntryTable.INSTANCE.getName(), "companyId", "LONG null") &&
			hasColumnType(
				AssetEntryTable.INSTANCE.getName(), "classNameId",
				"LONG null") &&
			hasColumnType(
				AssetEntryTable.INSTANCE.getName(), "classTypeId",
				"LONG null")) {

			try (PreparedStatement preparedStatement =
					connection.prepareStatement(
						StringBundler.concat(
							"update AssetEntry set classTypeId = ? where ",
							"companyId = ? and classNameId = ? and ",
							"classTypeId = ?"))) {

				preparedStatement.setLong(1, basicWebContentStructureId);
				preparedStatement.setLong(2, companyId);
				preparedStatement.setLong(3, classNameId);
				preparedStatement.setLong(4, 0);

				preparedStatement.executeUpdate();
			}
			catch (SQLException sqlException) {
				if (_log.isWarnEnabled()) {
					_log.warn(sqlException);
				}
			}
		}
		else {
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
									"key ", resourcePrimKey, " does not have ",
									"associated asset entry"));
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
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ArticleAssetsBasicWebContentClassTypeIdUpgradeProcess.class);

	private final AssetEntryLocalService _assetEntryLocalService;
	private final CompanyLocalService _companyLocalService;
	private final DDMStructureLocalService _ddmStructureLocalService;
	private final GroupLocalService _groupLocalService;

}