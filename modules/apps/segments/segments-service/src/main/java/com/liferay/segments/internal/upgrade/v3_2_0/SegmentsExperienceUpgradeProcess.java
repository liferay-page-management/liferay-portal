/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.segments.internal.upgrade.v3_2_0;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eudaldo Alonso
 */
public class SegmentsExperienceUpgradeProcess extends UpgradeProcess {

	public SegmentsExperienceUpgradeProcess(
		LayoutLocalService layoutLocalService) {

		_layoutLocalService = layoutLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		String fragmentEntryLinkColumnName = "plid";

		if (!hasColumn("FragmentEntryLink", "plid")) {
			fragmentEntryLinkColumnName = "classPK";
		}

		boolean hasFragmentEntryLinkCTCollectionId = hasColumn(
			"FragmentEntryLink", "ctCollectionId");
		boolean hasFragmentEntryLinkSegmentsExperienceId = hasColumn(
			"FragmentEntryLink", "segmentsExperienceId");
		boolean hasLayoutPageTemplateStructureRelCTCollectionId = hasColumn(
			"LayoutPageTemplateStructureRel", "ctCollectionId");

		String layoutPageTemplateStructureColumnName = "plid";

		if (!hasColumn("LayoutPageTemplateStructure", "plid")) {
			layoutPageTemplateStructureColumnName = "classPK";
		}

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				"select * from SegmentsExperience order by ctCollectionId");
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					StringBundler.concat(
						"insert into SegmentsExperience (mvccVersion, ",
						"ctCollectionId, uuid_, externalReferenceCode, ",
						"segmentsExperienceId, groupId, companyId, userId, ",
						"userName, createDate, modifiedDate, segmentsEntryId, ",
						"segmentsExperienceKey, plid, name, priority, ",
						"active_, typeSettings, lastPublishDate) values (?, ",
						"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ",
						"?)"));
			PreparedStatement preparedStatement3 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					_getUpdateFragmentEntryLinkSQL(
						fragmentEntryLinkColumnName,
						hasFragmentEntryLinkCTCollectionId, true));
			PreparedStatement preparedStatement4 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					_getUpdateFragmentEntryLinkSQL(
						fragmentEntryLinkColumnName,
						hasFragmentEntryLinkCTCollectionId, false));
			PreparedStatement preparedStatement5 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					_getUpdateLayoutPageTemplateStructureRelSQL(
						layoutPageTemplateStructureColumnName,
						hasLayoutPageTemplateStructureRelCTCollectionId, true));
			PreparedStatement preparedStatement6 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					_getUpdateLayoutPageTemplateStructureRelSQL(
						layoutPageTemplateStructureColumnName,
						hasLayoutPageTemplateStructureRelCTCollectionId,
						false));
			ResultSet resultSet = preparedStatement1.executeQuery()) {

			Map<Long, Long> draftLayoutSegmentsExperienceIds = new HashMap<>();

			while (resultSet.next()) {
				Layout layout = _layoutLocalService.fetchLayout(
					resultSet.getLong("plid"));

				if (layout == null) {
					continue;
				}

				Layout draftLayout = layout.fetchDraftLayout();

				if (draftLayout == null) {
					continue;
				}

				long ctCollectionId = resultSet.getLong("ctCollectionId");
				long groupId = resultSet.getLong("groupId");
				long segmentsExperienceId = resultSet.getLong(
					"segmentsExperienceId");
				String segmentsExperienceKey = resultSet.getString(
					"segmentsExperienceKey");

				long draftLayoutSegmentsExperienceId =
					_getDraftLayoutSegmentsExperienceId(
						ctCollectionId, groupId, segmentsExperienceKey,
						draftLayout.getPlid());

				if (draftLayoutSegmentsExperienceId == 0) {
					draftLayoutSegmentsExperienceId =
						draftLayoutSegmentsExperienceIds.getOrDefault(
							segmentsExperienceId, 0L);
				}

				if (draftLayoutSegmentsExperienceId == 0) {
					preparedStatement2.setLong(1, 0);
					preparedStatement2.setLong(2, ctCollectionId);

					String uuid = PortalUUIDUtil.generate();

					preparedStatement2.setString(3, uuid);
					preparedStatement2.setString(4, uuid);

					draftLayoutSegmentsExperienceId = increment();

					preparedStatement2.setLong(
						5, draftLayoutSegmentsExperienceId);

					preparedStatement2.setLong(6, groupId);
					preparedStatement2.setLong(
						7, resultSet.getLong("companyId"));
					preparedStatement2.setLong(8, resultSet.getLong("userId"));
					preparedStatement2.setString(
						9, resultSet.getString("userName"));
					preparedStatement2.setDate(
						10, resultSet.getDate("createDate"));
					preparedStatement2.setDate(
						11, resultSet.getDate("modifiedDate"));
					preparedStatement2.setLong(
						12, resultSet.getLong("segmentsEntryId"));
					preparedStatement2.setString(13, segmentsExperienceKey);
					preparedStatement2.setLong(14, draftLayout.getPlid());
					preparedStatement2.setString(
						15, resultSet.getString("name"));
					preparedStatement2.setLong(
						16, resultSet.getInt("priority"));
					preparedStatement2.setBoolean(
						17, resultSet.getBoolean("active_"));
					preparedStatement2.setString(
						18, resultSet.getString("typeSettings"));
					preparedStatement2.setDate(
						19, resultSet.getDate("lastPublishDate"));

					preparedStatement2.addBatch();
				}

				draftLayoutSegmentsExperienceIds.put(
					segmentsExperienceId, draftLayoutSegmentsExperienceId);

				if (hasFragmentEntryLinkSegmentsExperienceId) {
					PreparedStatement preparedStatement = preparedStatement3;

					if (ctCollectionId != 0) {
						preparedStatement = preparedStatement4;
					}

					int index = 1;

					preparedStatement.setLong(
						index++, draftLayoutSegmentsExperienceId);
					preparedStatement.setLong(index++, groupId);

					if (hasFragmentEntryLinkCTCollectionId &&
						(ctCollectionId != 0)) {

						preparedStatement.setLong(index++, ctCollectionId);
					}

					preparedStatement.setLong(index++, segmentsExperienceId);
					preparedStatement.setLong(index, draftLayout.getPlid());

					preparedStatement.addBatch();
				}

				PreparedStatement preparedStatement = preparedStatement5;

				if (ctCollectionId != 0) {
					preparedStatement = preparedStatement6;
				}

				int index = 1;

				preparedStatement.setLong(
					index++, draftLayoutSegmentsExperienceId);

				if (hasLayoutPageTemplateStructureRelCTCollectionId &&
					(ctCollectionId != 0)) {

					preparedStatement.setLong(index++, ctCollectionId);
				}

				preparedStatement.setLong(index++, segmentsExperienceId);
				preparedStatement.setLong(index++, groupId);
				preparedStatement.setLong(index, draftLayout.getPlid());

				preparedStatement.addBatch();
			}

			preparedStatement2.executeBatch();

			preparedStatement3.executeBatch();

			preparedStatement4.executeBatch();

			preparedStatement5.executeBatch();

			preparedStatement6.executeBatch();
		}
	}

	private long _getDraftLayoutSegmentsExperienceId(
			long ctCollectionId, long groupId, String segmentsExperienceKey,
			long plid)
		throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select segmentsExperienceId from SegmentsExperience ",
					"where ctCollectionId = ? and groupId = ? and ",
					"segmentsExperienceKey = ? and plid = ?"))) {

			preparedStatement.setLong(1, ctCollectionId);
			preparedStatement.setLong(2, groupId);
			preparedStatement.setString(3, segmentsExperienceKey);
			preparedStatement.setLong(4, plid);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getLong("segmentsExperienceId");
				}

				return 0;
			}
		}
	}

	private String _getUpdateFragmentEntryLinkSQL(
		String columnName, boolean hasCTCollectionId, boolean productionMode) {

		StringBundler sb = new StringBundler(11);

		sb.append("update FragmentEntryLink set segmentsExperienceId = ? ");
		sb.append("where groupId = ? and ");

		if (hasCTCollectionId) {
			if (productionMode) {
				sb.append("(ctCollectionId = 0 or not exists (select 1 from ");
				sb.append("SegmentsExperience where ");
				sb.append("SegmentsExperience.ctCollectionId = ");
				sb.append("FragmentEntryLink.ctCollectionId and ");
				sb.append("SegmentsExperience.segmentsExperienceId = ");
				sb.append("FragmentEntryLink.segmentsExperienceId)) and ");
			}
			else {
				sb.append("ctCollectionId = ? and ");
			}
		}

		sb.append("segmentsExperienceId = ? and ");
		sb.append(columnName);
		sb.append(" = ?");

		return sb.toString();
	}

	private String _getUpdateLayoutPageTemplateStructureRelSQL(
		String columnName, boolean hasCTCollectionId, boolean productionMode) {

		StringBundler sb = new StringBundler(15);

		sb.append("update LayoutPageTemplateStructureRel set ");
		sb.append("segmentsExperienceId = ? where ");

		if (hasCTCollectionId) {
			if (productionMode) {
				sb.append("(ctCollectionId = 0 or not exists (select 1 from ");
				sb.append("SegmentsExperience where ");
				sb.append("SegmentsExperience.ctCollectionId = ");
				sb.append("LayoutPageTemplateStructureRel.ctCollectionId and ");
				sb.append("SegmentsExperience.segmentsExperienceId = ");
				sb.append("LayoutPageTemplateStructureRel.");
				sb.append("segmentsExperienceId)) and ");
			}
			else {
				sb.append("ctCollectionId = ? and ");
			}
		}

		sb.append("segmentsExperienceId = ? and ");
		sb.append("layoutPageTemplateStructureId in (select ");
		sb.append("layoutPageTemplateStructureId from ");
		sb.append("LayoutPageTemplateStructure where groupId = ? and ");
		sb.append(columnName);
		sb.append(" = ?)");

		return sb.toString();
	}

	private final LayoutLocalService _layoutLocalService;

}