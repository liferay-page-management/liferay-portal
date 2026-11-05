/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.segments.internal.upgrade.v4_2_2;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.PortalUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Georgel Pop
 */
public class SegmentsExperienceUpgradeProcess extends UpgradeProcess {

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
				_getSelectSegmentsExperienceSQL(
					fragmentEntryLinkColumnName,
					hasFragmentEntryLinkSegmentsExperienceId,
					layoutPageTemplateStructureColumnName));
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					_getUpdateFragmentEntryLinkSQL(
						fragmentEntryLinkColumnName,
						hasFragmentEntryLinkCTCollectionId, true));
			PreparedStatement preparedStatement3 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					_getUpdateFragmentEntryLinkSQL(
						fragmentEntryLinkColumnName,
						hasFragmentEntryLinkCTCollectionId, false));
			PreparedStatement preparedStatement4 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					_getUpdateLayoutPageTemplateStructureRelSQL(
						layoutPageTemplateStructureColumnName,
						hasLayoutPageTemplateStructureRelCTCollectionId, true));
			PreparedStatement preparedStatement5 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					_getUpdateLayoutPageTemplateStructureRelSQL(
						layoutPageTemplateStructureColumnName,
						hasLayoutPageTemplateStructureRelCTCollectionId,
						false))) {

			preparedStatement1.setLong(
				1, PortalUtil.getClassNameId(Layout.class));

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				while (resultSet.next()) {
					long ctCollectionId = resultSet.getLong("ctCollectionId");
					long draftSegmentsExperienceId = resultSet.getLong(
						"draftSegmentsExperienceId");
					long groupId = resultSet.getLong("groupId");
					long plid = resultSet.getLong("plid");
					long publishedSegmentsExperienceId = resultSet.getLong(
						"publishedSegmentsExperienceId");

					if (hasFragmentEntryLinkSegmentsExperienceId) {
						PreparedStatement preparedStatement =
							preparedStatement2;

						if (ctCollectionId != 0) {
							preparedStatement = preparedStatement3;
						}

						int index = 1;

						preparedStatement.setLong(
							index++, draftSegmentsExperienceId);
						preparedStatement.setLong(index++, groupId);

						if (hasFragmentEntryLinkCTCollectionId &&
							(ctCollectionId != 0)) {

							preparedStatement.setLong(index++, ctCollectionId);
						}

						preparedStatement.setLong(
							index++, publishedSegmentsExperienceId);
						preparedStatement.setLong(index, plid);

						preparedStatement.addBatch();
					}

					PreparedStatement preparedStatement = preparedStatement4;

					if (ctCollectionId != 0) {
						preparedStatement = preparedStatement5;
					}

					int index = 1;

					preparedStatement.setLong(
						index++, draftSegmentsExperienceId);

					if (hasLayoutPageTemplateStructureRelCTCollectionId &&
						(ctCollectionId != 0)) {

						preparedStatement.setLong(index++, ctCollectionId);
					}

					preparedStatement.setLong(
						index++, publishedSegmentsExperienceId);
					preparedStatement.setLong(index++, groupId);
					preparedStatement.setLong(index, plid);

					preparedStatement.addBatch();
				}
			}

			preparedStatement2.executeBatch();

			preparedStatement3.executeBatch();

			preparedStatement4.executeBatch();

			preparedStatement5.executeBatch();
		}
	}

	private String _getSelectSegmentsExperienceSQL(
		String fragmentEntryLinkColumnName,
		boolean hasFragmentEntryLinkSegmentsExperienceId,
		String layoutPageTemplateStructureColumnName) {

		StringBundler sb = new StringBundler(38);

		sb.append("select draftSegmentsExperience.ctCollectionId, ");
		sb.append("draftSegmentsExperience.segmentsExperienceId as ");
		sb.append("draftSegmentsExperienceId, ");
		sb.append("draftSegmentsExperience.groupId, ");
		sb.append("draftSegmentsExperience.plid, ");
		sb.append("publishedSegmentsExperience.segmentsExperienceId as ");
		sb.append("publishedSegmentsExperienceId from SegmentsExperience ");
		sb.append("draftSegmentsExperience inner join Layout on ");
		sb.append("Layout.ctCollectionId = 0 and Layout.plid = ");
		sb.append("draftSegmentsExperience.plid inner join ");
		sb.append("SegmentsExperience publishedSegmentsExperience on ");
		sb.append("publishedSegmentsExperience.ctCollectionId = ");
		sb.append("draftSegmentsExperience.ctCollectionId and ");
		sb.append("publishedSegmentsExperience.groupId = ");
		sb.append("draftSegmentsExperience.groupId and ");
		sb.append("publishedSegmentsExperience.segmentsExperienceKey = ");
		sb.append("draftSegmentsExperience.segmentsExperienceKey and ");
		sb.append("publishedSegmentsExperience.plid = Layout.classPK where ");
		sb.append("Layout.classNameId = ? and (");

		if (hasFragmentEntryLinkSegmentsExperienceId) {
			sb.append("exists (select 1 from FragmentEntryLink where ");
			sb.append("FragmentEntryLink.groupId = ");
			sb.append("draftSegmentsExperience.groupId and FragmentEntryLink.");
			sb.append(fragmentEntryLinkColumnName);
			sb.append(" = draftSegmentsExperience.plid and ");
			sb.append("FragmentEntryLink.segmentsExperienceId = ");
			sb.append("publishedSegmentsExperience.segmentsExperienceId) or ");
		}

		sb.append("exists (select 1 from LayoutPageTemplateStructureRel ");
		sb.append("inner join LayoutPageTemplateStructure on ");
		sb.append("LayoutPageTemplateStructure.layoutPageTemplateStructureId ");
		sb.append("= LayoutPageTemplateStructureRel.");
		sb.append("layoutPageTemplateStructureId where ");
		sb.append("LayoutPageTemplateStructure.groupId = ");
		sb.append("draftSegmentsExperience.groupId and ");
		sb.append("LayoutPageTemplateStructure.");
		sb.append(layoutPageTemplateStructureColumnName);
		sb.append(" = draftSegmentsExperience.plid and ");
		sb.append("LayoutPageTemplateStructureRel.segmentsExperienceId = ");
		sb.append("publishedSegmentsExperience.segmentsExperienceId))");

		return sb.toString();
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

}