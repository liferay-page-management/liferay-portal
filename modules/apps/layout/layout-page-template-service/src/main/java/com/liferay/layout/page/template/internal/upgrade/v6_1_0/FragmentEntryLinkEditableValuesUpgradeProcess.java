/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.internal.upgrade.v6_1_0;

import com.liferay.fragment.entry.processor.constants.FragmentEntryProcessorConstants;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.Validator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marco Leo
 */
public class FragmentEntryLinkEditableValuesUpgradeProcess
	extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				"select ctCollectionId, fragmentEntryLinkId, editableValues " +
					"from FragmentEntryLink where editableValues like " +
						"'%classPK%' or editableValues like '%fileEntryId%'");
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					"update FragmentEntryLink set editableValues = ? where " +
						"ctCollectionId = ? and fragmentEntryLinkId = ?");
			ResultSet resultSet = preparedStatement1.executeQuery()) {

			while (resultSet.next()) {
				String editableValues = resultSet.getString("editableValues");

				JSONObject editableValuesJSONObject =
					JSONFactoryUtil.createJSONObject(editableValues);

				boolean modified = false;

				modified |= _processEditableFragmentEntryProcessor(
					editableValuesJSONObject);
				modified |= _processBackgroundImageFragmentEntryProcessor(
					editableValuesJSONObject);

				if (!modified) {
					continue;
				}

				preparedStatement2.setString(
					1, editableValuesJSONObject.toString());
				preparedStatement2.setLong(
					2, resultSet.getLong("ctCollectionId"));
				preparedStatement2.setLong(
					3, resultSet.getLong("fragmentEntryLinkId"));

				preparedStatement2.addBatch();
			}

			preparedStatement2.executeBatch();
		}
	}

	private DLFileEntryInfo _fetchDLFileEntryInfo(long fileEntryId)
		throws SQLException {

		if (_dlFileEntryInfoCache.containsKey(fileEntryId)) {
			return _dlFileEntryInfoCache.get(fileEntryId);
		}

		DLFileEntryInfo dlFileEntryInfo = null;

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select externalReferenceCode, groupId from DLFileEntry " +
					"where fileEntryId = ?")) {

			preparedStatement.setLong(1, fileEntryId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					dlFileEntryInfo = new DLFileEntryInfo(
						resultSet.getString("externalReferenceCode"),
						resultSet.getLong("groupId"));
				}
			}
		}

		_dlFileEntryInfoCache.put(fileEntryId, dlFileEntryInfo);

		return dlFileEntryInfo;
	}

	private String _fetchGroupExternalReferenceCode(long groupId)
		throws SQLException {

		if (_groupERCCache.containsKey(groupId)) {
			return _groupERCCache.get(groupId);
		}

		String externalReferenceCode = null;

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select externalReferenceCode from Group_ where groupId = ?")) {

			preparedStatement.setLong(1, groupId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					externalReferenceCode = resultSet.getString(
						"externalReferenceCode");
				}
			}
		}

		_groupERCCache.put(groupId, externalReferenceCode);

		return externalReferenceCode;
	}

	private boolean _fixImageJSONObject(JSONObject imageJSONObject)
		throws SQLException {

		long classPK = imageJSONObject.getLong("classPK");
		long fileEntryId = imageJSONObject.getLong("fileEntryId");

		if ((classPK <= 0) && (fileEntryId <= 0)) {
			return false;
		}

		long resolvedFileEntryId = 0;

		DLFileEntryInfo dlFileEntryInfo = null;

		if (classPK > 0) {
			dlFileEntryInfo = _fetchDLFileEntryInfo(classPK);
		}

		if (dlFileEntryInfo != null) {
			resolvedFileEntryId = classPK;
		}
		else if (fileEntryId > 0) {
			dlFileEntryInfo = _fetchDLFileEntryInfo(fileEntryId);

			if (dlFileEntryInfo != null) {
				resolvedFileEntryId = fileEntryId;
			}
		}

		if (dlFileEntryInfo == null) {
			return false;
		}

		boolean modified = false;

		if (classPK != resolvedFileEntryId) {
			imageJSONObject.put("classPK", resolvedFileEntryId);

			modified = true;
		}

		if (fileEntryId != resolvedFileEntryId) {
			imageJSONObject.put("fileEntryId", resolvedFileEntryId);

			modified = true;
		}

		String externalReferenceCode = dlFileEntryInfo._externalReferenceCode;

		if (Validator.isNotNull(externalReferenceCode) &&
			!externalReferenceCode.equals(
				imageJSONObject.getString("externalReferenceCode"))) {

			imageJSONObject.put("externalReferenceCode", externalReferenceCode);

			modified = true;
		}

		String groupERC = _fetchGroupExternalReferenceCode(
			dlFileEntryInfo._groupId);

		if (Validator.isNotNull(groupERC) &&
			!groupERC.equals(
				imageJSONObject.getString("scopeExternalReferenceCode"))) {

			imageJSONObject.put("scopeExternalReferenceCode", groupERC);

			modified = true;
		}

		return modified;
	}

	private boolean _isMappedEditable(JSONObject editableJSONObject) {
		long classNameId = editableJSONObject.getLong("classNameId");
		String fieldId = editableJSONObject.getString("fieldId");

		if ((classNameId > 0) && Validator.isNotNull(fieldId)) {
			return true;
		}

		return false;
	}

	private boolean _processBackgroundImageFragmentEntryProcessor(
			JSONObject editableValuesJSONObject)
		throws SQLException {

		JSONObject backgroundImageJSONObject =
			editableValuesJSONObject.getJSONObject(
				FragmentEntryProcessorConstants.
					KEY_BACKGROUND_IMAGE_FRAGMENT_ENTRY_PROCESSOR);

		if (backgroundImageJSONObject == null) {
			return false;
		}

		boolean modified = false;

		for (String elementKey : backgroundImageJSONObject.keySet()) {
			JSONObject elementJSONObject =
				backgroundImageJSONObject.getJSONObject(elementKey);

			if ((elementJSONObject == null) ||
				_isMappedEditable(elementJSONObject)) {

				continue;
			}

			modified |= _processImageWithLocales(elementJSONObject);
		}

		return modified;
	}

	private boolean _processEditableFragmentEntryProcessor(
			JSONObject editableValuesJSONObject)
		throws SQLException {

		JSONObject editableJSONObject = editableValuesJSONObject.getJSONObject(
			FragmentEntryProcessorConstants.
				KEY_EDITABLE_FRAGMENT_ENTRY_PROCESSOR);

		if (editableJSONObject == null) {
			return false;
		}

		boolean modified = false;

		for (String editableKey : editableJSONObject.keySet()) {
			JSONObject editableValueJSONObject =
				editableJSONObject.getJSONObject(editableKey);

			if ((editableValueJSONObject == null) ||
				_isMappedEditable(editableValueJSONObject)) {

				continue;
			}

			modified |= _processImageWithLocales(editableValueJSONObject);
		}

		return modified;
	}

	private boolean _processImageWithLocales(JSONObject jsonObject)
		throws SQLException {

		boolean modified = false;

		if (jsonObject.has("classPK") || jsonObject.has("fileEntryId")) {
			modified |= _fixImageJSONObject(jsonObject);
		}

		JSONObject configJSONObject = jsonObject.getJSONObject("config");

		if ((configJSONObject != null) &&
			(configJSONObject.has("classPK") ||
			 configJSONObject.has("fileEntryId"))) {

			modified |= _fixImageJSONObject(configJSONObject);
		}

		for (String key : jsonObject.keySet()) {
			if (key.equals("config")) {
				continue;
			}

			JSONObject localeJSONObject = jsonObject.getJSONObject(key);

			if (localeJSONObject == null) {
				continue;
			}

			if (localeJSONObject.has("classPK") ||
				localeJSONObject.has("fileEntryId")) {

				modified |= _fixImageJSONObject(localeJSONObject);
			}

			JSONObject localeConfigJSONObject = localeJSONObject.getJSONObject(
				"config");

			if ((localeConfigJSONObject != null) &&
				(localeConfigJSONObject.has("classPK") ||
				 localeConfigJSONObject.has("fileEntryId"))) {

				modified |= _fixImageJSONObject(localeConfigJSONObject);
			}
		}

		return modified;
	}

	private final Map<Long, DLFileEntryInfo> _dlFileEntryInfoCache =
		new HashMap<>();
	private final Map<Long, String> _groupERCCache = new HashMap<>();

	private static class DLFileEntryInfo {

		private DLFileEntryInfo(String externalReferenceCode, long groupId) {
			_externalReferenceCode = externalReferenceCode;
			_groupId = groupId;
		}

		private final String _externalReferenceCode;
		private final long _groupId;

	}

}