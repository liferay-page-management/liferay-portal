/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.seo.internal.upgrade.v3_0_0;

import com.liferay.change.tracking.constants.CTConstants;
import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.service.CTCollectionLocalService;
import com.liferay.change.tracking.service.CTEntryLocalService;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.dynamic.data.mapping.service.DDMStorageLinkLocalService;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.dynamic.data.mapping.storage.DDMStorageEngineManager;
import com.liferay.layout.seo.internal.upgrade.v3_0_0.util.LayoutSEOEntryCustomMetaTagTable;
import com.liferay.layout.seo.model.LayoutSEOEntry;
import com.liferay.layout.seo.model.LayoutSEOEntryCustomMetaTag;
import com.liferay.layout.seo.model.impl.LayoutSEOEntryCustomMetaTagImpl;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.change.tracking.CTCollectionThreadLocal;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.upgrade.UpgradeProcessFactory;
import com.liferay.portal.kernel.upgrade.UpgradeStep;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Eudaldo Alonso
 */
public class LayoutSEOEntryCustomMetaTagUpgradeProcess extends UpgradeProcess {

	public LayoutSEOEntryCustomMetaTagUpgradeProcess(
		ClassNameLocalService classNameLocalService,
		CompanyLocalService companyLocalService,
		CTCollectionLocalService ctCollectionLocalService,
		CTEntryLocalService ctEntryLocalService,
		DDMStorageEngineManager ddmStorageEngineManager,
		DDMStorageLinkLocalService ddmStorageLinkLocalService,
		DDMStructureLocalService ddmStructureLocalService,
		GroupLocalService groupLocalService) {

		_classNameLocalService = classNameLocalService;
		_companyLocalService = companyLocalService;
		_ctCollectionLocalService = ctCollectionLocalService;
		_ctEntryLocalService = ctEntryLocalService;
		_ddmStorageEngineManager = ddmStorageEngineManager;
		_ddmStorageLinkLocalService = ddmStorageLinkLocalService;
		_ddmStructureLocalService = ddmStructureLocalService;
		_groupLocalService = groupLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				"select ctCollectionId, layoutSEOEntryId, groupId, " +
					"companyId, DDMStorageId from LayoutSEOEntry where " +
						"DDMStorageId > 0 order by ctCollectionId");
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					StringBundler.concat(
						"insert into LayoutSEOEntryCustomMetaTag (",
						"mvccVersion, ctCollectionId, groupId, companyId, ",
						"layoutSEOEntryCustomMetaTagId, layoutSEOEntryId, ",
						"property, content) values (?, ?, ?, ?, ?, ?, ?, ?)"));
			ResultSet resultSet = preparedStatement1.executeQuery()) {

			Map<Long, List<Long>> addedLayoutSEOEntryCustomMetaTagIdsMap =
				new HashMap<>();
			Map<Long, Set<Long>> ddmStorageIdsMap = new TreeMap<>();
			Map<Long, List<Long>> deletedLayoutSEOEntryCustomMetaTagIdsMap =
				new HashMap<>();
			Map<Long, List<Long>> productionLayoutSEOEntryCustomMetaTagIdsMap =
				new HashMap<>();
			Map<Long, List<Map.Entry<String, String>>> productionPropertiesMap =
				new HashMap<>();

			while (resultSet.next()) {
				long ctCollectionId = resultSet.getLong("ctCollectionId");
				long ddmStorageId = resultSet.getLong("DDMStorageId");
				long layoutSEOEntryId = resultSet.getLong("layoutSEOEntryId");

				long writableCTCollectionId = _getWritableCTCollectionId(
					ctCollectionId);

				Set<Long> ddmStorageIds = ddmStorageIdsMap.computeIfAbsent(
					writableCTCollectionId, key -> new HashSet<>());

				ddmStorageIds.add(ddmStorageId);

				List<Map.Entry<String, String>> properties = _getProperties(
					writableCTCollectionId, ddmStorageId);

				if (writableCTCollectionId ==
						CTConstants.CT_COLLECTION_ID_PRODUCTION) {

					List<Long> layoutSEOEntryCustomMetaTagIds =
						_addLayoutSEOEntryCustomMetaTags(
							resultSet.getLong("companyId"), ctCollectionId,
							resultSet.getLong("groupId"), layoutSEOEntryId,
							preparedStatement2, properties);

					if (ctCollectionId ==
							CTConstants.CT_COLLECTION_ID_PRODUCTION) {

						productionLayoutSEOEntryCustomMetaTagIdsMap.put(
							layoutSEOEntryId, layoutSEOEntryCustomMetaTagIds);
						productionPropertiesMap.put(
							layoutSEOEntryId, properties);
					}

					continue;
				}

				if (properties.equals(
						productionPropertiesMap.get(layoutSEOEntryId))) {

					continue;
				}

				List<Long> addedLayoutSEOEntryCustomMetaTagIds =
					addedLayoutSEOEntryCustomMetaTagIdsMap.computeIfAbsent(
						writableCTCollectionId, key -> new ArrayList<>());

				addedLayoutSEOEntryCustomMetaTagIds.addAll(
					_addLayoutSEOEntryCustomMetaTags(
						resultSet.getLong("companyId"), ctCollectionId,
						resultSet.getLong("groupId"), layoutSEOEntryId,
						preparedStatement2, properties));

				List<Long> deletedLayoutSEOEntryCustomMetaTagIds =
					deletedLayoutSEOEntryCustomMetaTagIdsMap.computeIfAbsent(
						writableCTCollectionId, key -> new ArrayList<>());

				deletedLayoutSEOEntryCustomMetaTagIds.addAll(
					productionLayoutSEOEntryCustomMetaTagIdsMap.getOrDefault(
						layoutSEOEntryId, Collections.emptyList()));
			}

			_addDeletedLayoutSEOEntryCustomMetaTagIds(
				deletedLayoutSEOEntryCustomMetaTagIdsMap,
				productionLayoutSEOEntryCustomMetaTagIdsMap);

			preparedStatement2.executeBatch();

			_addCTEntries(
				addedLayoutSEOEntryCustomMetaTagIdsMap,
				CTConstants.CT_CHANGE_TYPE_ADDITION);
			_addCTEntries(
				deletedLayoutSEOEntryCustomMetaTagIdsMap,
				CTConstants.CT_CHANGE_TYPE_DELETION);

			for (Map.Entry<Long, Set<Long>> entry :
					ddmStorageIdsMap.entrySet()) {

				try (SafeCloseable safeCloseable =
						CTCollectionThreadLocal.
							setCTCollectionIdWithSafeCloseable(
								entry.getKey())) {

					for (long ddmStorageId : entry.getValue()) {
						_ddmStorageEngineManager.deleteByClass(ddmStorageId);
					}
				}
			}
		}

		_companyLocalService.forEachCompanyId(
			companyId -> {
				Group companyGroup = _groupLocalService.getCompanyGroup(
					companyId);

				DDMStructure ddmStructure =
					_ddmStructureLocalService.fetchStructure(
						companyGroup.getGroupId(),
						_classNameLocalService.getClassNameId(
							LayoutSEOEntry.class.getName()),
						"custom-meta-tags");

				if (ddmStructure == null) {
					return;
				}

				_ddmStorageLinkLocalService.deleteStructureStorageLinks(
					ddmStructure.getStructureId());

				_ddmStructureLocalService.deleteStructure(ddmStructure);
			});
	}

	@Override
	protected UpgradeStep[] getPostUpgradeSteps() {
		return new UpgradeStep[] {
			UpgradeProcessFactory.dropColumns("LayoutSEOEntry", "DDMStorageId")
		};
	}

	@Override
	protected UpgradeStep[] getPreUpgradeSteps() {
		return new UpgradeStep[] {LayoutSEOEntryCustomMetaTagTable.create()};
	}

	private void _addCTEntries(
			Map<Long, List<Long>> layoutSEOEntryCustomMetaTagIdsMap,
			int changeType)
		throws Exception {

		long classNameId = _classNameLocalService.getClassNameId(
			LayoutSEOEntryCustomMetaTag.class);

		for (Map.Entry<Long, List<Long>> entry :
				layoutSEOEntryCustomMetaTagIdsMap.entrySet()) {

			CTCollection ctCollection =
				_ctCollectionLocalService.getCTCollection(entry.getKey());

			for (long layoutSEOEntryCustomMetaTagId : entry.getValue()) {
				LayoutSEOEntryCustomMetaTag layoutSEOEntryCustomMetaTag =
					new LayoutSEOEntryCustomMetaTagImpl();

				layoutSEOEntryCustomMetaTag.setLayoutSEOEntryCustomMetaTagId(
					layoutSEOEntryCustomMetaTagId);

				_ctEntryLocalService.addCTEntry(
					null, ctCollection.getCtCollectionId(), classNameId,
					layoutSEOEntryCustomMetaTag, ctCollection.getUserId(),
					changeType);
			}
		}
	}

	private void _addDeletedLayoutSEOEntryCustomMetaTagIds(
			Map<Long, List<Long>> deletedLayoutSEOEntryCustomMetaTagIdsMap,
			Map<Long, List<Long>> productionLayoutSEOEntryCustomMetaTagIdsMap)
		throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select ctCollectionId, modelClassPK from CTEntry where " +
					"modelClassNameId = ? and changeType = ?")) {

			preparedStatement.setLong(
				1, _classNameLocalService.getClassNameId(LayoutSEOEntry.class));
			preparedStatement.setInt(2, CTConstants.CT_CHANGE_TYPE_DELETION);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					long ctCollectionId = resultSet.getLong("ctCollectionId");

					List<Long> layoutSEOEntryCustomMetaTagIds =
						productionLayoutSEOEntryCustomMetaTagIdsMap.get(
							resultSet.getLong("modelClassPK"));

					if ((layoutSEOEntryCustomMetaTagIds == null) ||
						(_getWritableCTCollectionId(ctCollectionId) !=
							ctCollectionId)) {

						continue;
					}

					List<Long> deletedLayoutSEOEntryCustomMetaTagIds =
						deletedLayoutSEOEntryCustomMetaTagIdsMap.
							computeIfAbsent(
								ctCollectionId, key -> new ArrayList<>());

					deletedLayoutSEOEntryCustomMetaTagIds.addAll(
						layoutSEOEntryCustomMetaTagIds);
				}
			}
		}
	}

	private List<Long> _addLayoutSEOEntryCustomMetaTags(
			long companyId, long ctCollectionId, long groupId,
			long layoutSEOEntryId, PreparedStatement preparedStatement,
			List<Map.Entry<String, String>> properties)
		throws Exception {

		List<Long> layoutSEOEntryCustomMetaTagIds = new ArrayList<>();

		for (Map.Entry<String, String> entry : properties) {
			long layoutSEOEntryCustomMetaTagId = increment();

			preparedStatement.setLong(1, 0);
			preparedStatement.setLong(2, ctCollectionId);
			preparedStatement.setLong(3, groupId);
			preparedStatement.setLong(4, companyId);
			preparedStatement.setLong(5, layoutSEOEntryCustomMetaTagId);
			preparedStatement.setLong(6, layoutSEOEntryId);
			preparedStatement.setString(7, entry.getKey());
			preparedStatement.setString(8, entry.getValue());

			preparedStatement.addBatch();

			layoutSEOEntryCustomMetaTagIds.add(layoutSEOEntryCustomMetaTagId);
		}

		return layoutSEOEntryCustomMetaTagIds;
	}

	private List<Map.Entry<String, String>> _getProperties(
			long ctCollectionId, long ddmStorageId)
		throws Exception {

		List<Map.Entry<String, String>> properties = new ArrayList<>();

		DDMFormValues ddmFormValues = null;

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollectionId)) {

			ddmFormValues = _ddmStorageEngineManager.getDDMFormValues(
				ddmStorageId);
		}

		for (DDMFormFieldValue nameDDMFormFieldValue :
				ddmFormValues.getDDMFormFieldValues()) {

			if (_isLegacyDDMFormFieldValue(nameDDMFormFieldValue)) {
				List<DDMFormFieldValue> nestedDDMFormFieldValues =
					nameDDMFormFieldValue.getNestedDDMFormFieldValues();

				nameDDMFormFieldValue = nestedDDMFormFieldValues.get(0);
			}

			Value nameValue = nameDDMFormFieldValue.getValue();

			List<DDMFormFieldValue> nestedDDMFormFieldValues =
				nameDDMFormFieldValue.getNestedDDMFormFieldValues();

			DDMFormFieldValue valueDDMFormFieldValue =
				nestedDDMFormFieldValues.get(0);

			Value valueValue = valueDDMFormFieldValue.getValue();

			Map<String, String> localizedMap = new HashMap<>();

			Map<Locale, String> contentMap = valueValue.getValues();

			contentMap.forEach(
				(locale, value) -> localizedMap.put(
					LocaleUtil.toLanguageId(locale), value));

			properties.add(
				new AbstractMap.SimpleImmutableEntry<>(
					nameValue.getString(nameValue.getDefaultLocale()),
					LocalizationUtil.getXml(
						localizedMap,
						LocaleUtil.toLanguageId(valueValue.getDefaultLocale()),
						"Label")));
		}

		return properties;
	}

	private long _getWritableCTCollectionId(long ctCollectionId) {
		if (ctCollectionId == CTConstants.CT_COLLECTION_ID_PRODUCTION) {
			return ctCollectionId;
		}

		CTCollection ctCollection = _ctCollectionLocalService.fetchCTCollection(
			ctCollectionId);

		if ((ctCollection == null) || ctCollection.isReadOnly()) {
			return CTConstants.CT_COLLECTION_ID_PRODUCTION;
		}

		return ctCollectionId;
	}

	private boolean _isLegacyDDMFormFieldValue(
		DDMFormFieldValue ddmFormFieldValue) {

		List<DDMFormFieldValue> nestedDDMFormFieldValues =
			ddmFormFieldValue.getNestedDDMFormFieldValues();

		DDMFormFieldValue childDDMFormFieldValue = nestedDDMFormFieldValues.get(
			0);

		return Objects.equals(childDDMFormFieldValue.getName(), "property");
	}

	private final ClassNameLocalService _classNameLocalService;
	private final CompanyLocalService _companyLocalService;
	private final CTCollectionLocalService _ctCollectionLocalService;
	private final CTEntryLocalService _ctEntryLocalService;
	private final DDMStorageEngineManager _ddmStorageEngineManager;
	private final DDMStorageLinkLocalService _ddmStorageLinkLocalService;
	private final DDMStructureLocalService _ddmStructureLocalService;
	private final GroupLocalService _groupLocalService;

}