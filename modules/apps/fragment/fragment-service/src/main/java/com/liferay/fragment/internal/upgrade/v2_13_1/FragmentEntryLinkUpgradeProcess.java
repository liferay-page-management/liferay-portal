/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.internal.upgrade.v2_13_1;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.fragment.entry.processor.constants.FragmentEntryProcessorConstants;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.Portal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Iterator;
import java.util.Objects;

/**
 * @author Rubén Pulido
 */
public class FragmentEntryLinkUpgradeProcess extends UpgradeProcess {

	public FragmentEntryLinkUpgradeProcess(Portal portal) {
		_portal = portal;
	}

	@Override
	protected void doUpgrade() throws Exception {
		long fileEntryClassNameId = _portal.getClassNameId(
			FileEntry.class.getName());

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				StringBundler.concat(
					"select ctCollectionId, fragmentEntryLinkId, ",
					"editableValues from FragmentEntryLink where plid in ",
					"(with plids1 as (select distinct plid from ",
					"LayoutClassedModelUsage where classNameId = ",
					fileEntryClassNameId,
					"), plids2 as (select distinct plid from Layout where ",
					"classPK in (select plid from plids1)) select plid from ",
					"plids1 union select plid from plids2)"));
			ResultSet resultSet = preparedStatement1.executeQuery();
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection,
					"update FragmentEntryLink set editableValues = ? where " +
						"ctCollectionId = ? and fragmentEntryLinkId = ?")) {

			long dlFileEntryClassNameId = _portal.getClassNameId(
				DLFileEntry.class.getName());

			while (resultSet.next()) {
				long ctCollectionId = resultSet.getLong("ctCollectionId");
				long fragmentEntryLinkId = resultSet.getLong(
					"fragmentEntryLinkId");

				String editableValues = resultSet.getString("editableValues");

				preparedStatement2.setString(
					1,
					_replaceDLFileEntryClassNameIdWithFileEntryClassNameId(
						editableValues, dlFileEntryClassNameId,
						fileEntryClassNameId));

				preparedStatement2.setLong(2, ctCollectionId);
				preparedStatement2.setLong(3, fragmentEntryLinkId);

				preparedStatement2.addBatch();
			}

			preparedStatement2.executeBatch();
		}
	}

	private String _replaceDLFileEntryClassNameIdWithFileEntryClassNameId(
		String editableValues, long dlFileEntryClassNameId,
		long fileEntryClassNameId) {

		try {
			JSONObject editableValuesJSONObject =
				JSONFactoryUtil.createJSONObject(editableValues);

			JSONObject editableFragmentEntryProcessorJSONObject =
				editableValuesJSONObject.getJSONObject(
					FragmentEntryProcessorConstants.
						KEY_EDITABLE_FRAGMENT_ENTRY_PROCESSOR);

			if (editableFragmentEntryProcessorJSONObject == null) {
				return editableValues;
			}

			Iterator<String> iterator =
				editableFragmentEntryProcessorJSONObject.keys();

			while (iterator.hasNext()) {
				String editableElementId = iterator.next();

				JSONObject editableElementJSONObject =
					editableFragmentEntryProcessorJSONObject.getJSONObject(
						editableElementId);

				if (editableElementJSONObject == null) {
					continue;
				}

				JSONObject configJSONObject =
					editableElementJSONObject.getJSONObject("config");

				if (configJSONObject == null) {
					continue;
				}

				String classNameId = configJSONObject.getString("classNameId");

				if (Objects.equals(
						classNameId, String.valueOf(dlFileEntryClassNameId))) {

					configJSONObject.put(
						"classNameId", String.valueOf(fileEntryClassNameId));
				}
			}

			return editableValuesJSONObject.toString();
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}

		return editableValues;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FragmentEntryLinkUpgradeProcess.class);

	private final Portal _portal;

}