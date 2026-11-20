/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.segments.internal.upgrade.v4_2_2;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.segments.model.SegmentsExperience;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

		_updateSegmentsExperiences();

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
					long groupId = resultSet.getLong("groupId");
					long oldSegmentsExperienceId = resultSet.getLong(
						"oldSegmentsExperienceId");
					long newSegmentsExperienceId = resultSet.getLong(
						"newSegmentsExperienceId");
					long plid = resultSet.getLong("plid");

					if (hasFragmentEntryLinkSegmentsExperienceId) {
						PreparedStatement preparedStatement =
							preparedStatement2;

						if (ctCollectionId != 0) {
							preparedStatement = preparedStatement3;
						}

						int index = 1;

						preparedStatement.setLong(
							index++, newSegmentsExperienceId);
						preparedStatement.setLong(index++, groupId);

						if (hasFragmentEntryLinkCTCollectionId &&
							(ctCollectionId != 0)) {

							preparedStatement.setLong(index++, ctCollectionId);
						}

						preparedStatement.setLong(
							index++, oldSegmentsExperienceId);
						preparedStatement.setLong(index, plid);

						preparedStatement.addBatch();
					}

					PreparedStatement preparedStatement = preparedStatement4;

					if (ctCollectionId != 0) {
						preparedStatement = preparedStatement5;
					}

					int index = 1;

					preparedStatement.setLong(index++, newSegmentsExperienceId);

					if (hasLayoutPageTemplateStructureRelCTCollectionId &&
						(ctCollectionId != 0)) {

						preparedStatement.setLong(index++, ctCollectionId);
					}

					preparedStatement.setLong(index++, oldSegmentsExperienceId);
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

		_updateCTCollectionSegmentsExperiences(
			fragmentEntryLinkColumnName,
			hasFragmentEntryLinkSegmentsExperienceId);
	}

	private Set<Integer> _getPriorities(
			long groupId, long plid, PreparedStatement preparedStatement)
		throws Exception {

		Set<Integer> priorities = new HashSet<>();

		preparedStatement.setLong(1, groupId);
		preparedStatement.setLong(2, plid);

		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			while (resultSet.next()) {
				priorities.add(resultSet.getInt("priority"));
			}
		}

		return priorities;
	}

	private String _getSelectSegmentsExperienceSQL(
		String fragmentEntryLinkColumnName,
		boolean hasFragmentEntryLinkSegmentsExperienceId,
		String layoutPageTemplateStructureColumnName) {

		StringBundler sb = new StringBundler(37);

		sb.append("select draftSegmentsExperience.ctCollectionId, ");
		sb.append("draftSegmentsExperience.segmentsExperienceId as ");
		sb.append("newSegmentsExperienceId, draftSegmentsExperience.groupId, ");
		sb.append("draftSegmentsExperience.plid, ");
		sb.append("publishedSegmentsExperience.segmentsExperienceId as ");
		sb.append("oldSegmentsExperienceId from SegmentsExperience ");
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

	private void _updateCTCollectionSegmentsExperiences(
			String fragmentEntryLinkColumnName,
			boolean hasFragmentEntryLinkSegmentsExperienceId)
		throws Exception {

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				StringBundler.concat(
					"select SegmentsExperience.segmentsExperienceId as ",
					"oldSegmentsExperienceId, SegmentsExperience.groupId, ",
					"SegmentsExperience.plid, ",
					"productionSegmentsExperience.segmentsExperienceId as ",
					"newSegmentsExperienceId from SegmentsExperience inner ",
					"join CTCollection on CTCollection.ctCollectionId = ",
					"SegmentsExperience.ctCollectionId inner join Layout on ",
					"Layout.ctCollectionId = 0 and Layout.plid = ",
					"SegmentsExperience.plid inner join SegmentsExperience ",
					"productionSegmentsExperience on ",
					"productionSegmentsExperience.ctCollectionId = 0 and ",
					"productionSegmentsExperience.groupId = ",
					"SegmentsExperience.groupId and ",
					"productionSegmentsExperience.segmentsExperienceKey = ",
					"SegmentsExperience.segmentsExperienceKey and ",
					"productionSegmentsExperience.plid = ",
					"SegmentsExperience.plid where CTCollection.status = ? ",
					"and Layout.classNameId = ? and not exists (select 1 from ",
					"CTEntry where CTEntry.ctCollectionId = ",
					"SegmentsExperience.ctCollectionId and ",
					"CTEntry.modelClassNameId = ? and CTEntry.modelClassPK = ",
					"SegmentsExperience.segmentsExperienceId)"));
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					StringBundler.concat(
						"update FragmentEntryLink set segmentsExperienceId = ",
						"? where groupId = ? and segmentsExperienceId = ? and ",
						fragmentEntryLinkColumnName, " = ?"));
			PreparedStatement preparedStatement3 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					"update LayoutPageTemplateStructureRel set " +
						"segmentsExperienceId = ? where segmentsExperienceId " +
							"= ?")) {

			preparedStatement1.setInt(1, WorkflowConstants.STATUS_APPROVED);
			preparedStatement1.setLong(
				2, PortalUtil.getClassNameId(Layout.class));
			preparedStatement1.setLong(
				3, PortalUtil.getClassNameId(SegmentsExperience.class));

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				while (resultSet.next()) {
					long oldSegmentsExperienceId = resultSet.getLong(
						"oldSegmentsExperienceId");
					long newSegmentsExperienceId = resultSet.getLong(
						"newSegmentsExperienceId");

					if (hasFragmentEntryLinkSegmentsExperienceId) {
						preparedStatement2.setLong(1, newSegmentsExperienceId);
						preparedStatement2.setLong(
							2, resultSet.getLong("groupId"));
						preparedStatement2.setLong(3, oldSegmentsExperienceId);
						preparedStatement2.setLong(
							4, resultSet.getLong("plid"));

						preparedStatement2.addBatch();
					}

					preparedStatement3.setLong(1, newSegmentsExperienceId);
					preparedStatement3.setLong(2, oldSegmentsExperienceId);

					preparedStatement3.addBatch();
				}
			}

			preparedStatement2.executeBatch();

			preparedStatement3.executeBatch();
		}
	}

	private void _updateSegmentsExperiences() throws Exception {
		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				StringBundler.concat(
					"select SegmentsExperience.ctCollectionId, ",
					"SegmentsExperience.segmentsExperienceId, ",
					"SegmentsExperience.groupId, ",
					"SegmentsExperience.segmentsExperienceKey, ",
					"SegmentsExperience.plid, SegmentsExperience.priority ",
					"from SegmentsExperience inner join CTCollection on ",
					"CTCollection.ctCollectionId = ",
					"SegmentsExperience.ctCollectionId inner join Layout on ",
					"Layout.ctCollectionId = 0 and Layout.plid = ",
					"SegmentsExperience.plid inner join SegmentsExperience ",
					"publishedSegmentsExperience on ",
					"publishedSegmentsExperience.ctCollectionId = 0 and ",
					"publishedSegmentsExperience.groupId = ",
					"SegmentsExperience.groupId and ",
					"publishedSegmentsExperience.plid = Layout.classPK and ",
					"publishedSegmentsExperience.segmentsExperienceKey = ",
					"SegmentsExperience.segmentsExperienceKey where ",
					"CTCollection.status = ? and Layout.classNameId = ? and ",
					"not exists (select 1 from CTEntry where ",
					"CTEntry.ctCollectionId = ",
					"SegmentsExperience.ctCollectionId and ",
					"CTEntry.modelClassNameId = ? and CTEntry.modelClassPK = ",
					"SegmentsExperience.segmentsExperienceId) and not exists ",
					"(select 1 from SegmentsExperience ",
					"productionSegmentsExperience where ",
					"productionSegmentsExperience.ctCollectionId = 0 and ",
					"productionSegmentsExperience.groupId = ",
					"SegmentsExperience.groupId and ",
					"productionSegmentsExperience.plid = ",
					"SegmentsExperience.plid and ",
					"productionSegmentsExperience.segmentsExperienceKey = ",
					"SegmentsExperience.segmentsExperienceKey)"));
			PreparedStatement preparedStatement2 = connection.prepareStatement(
				"select priority from SegmentsExperience where " +
					"ctCollectionId = 0 and groupId = ? and plid = ?");
			PreparedStatement preparedStatement3 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					"update SegmentsExperience set ctCollectionId = 0, " +
						"priority = ? where ctCollectionId = ? and " +
							"segmentsExperienceId = ?")) {

			preparedStatement1.setInt(1, WorkflowConstants.STATUS_APPROVED);
			preparedStatement1.setLong(
				2, PortalUtil.getClassNameId(Layout.class));
			preparedStatement1.setLong(
				3, PortalUtil.getClassNameId(SegmentsExperience.class));

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				Map<Long, Set<Integer>> plidPriorities = new HashMap<>();
				Set<String> plidSegmentsExperienceKeys = new HashSet<>();

				while (resultSet.next()) {
					long plid = resultSet.getLong("plid");

					String plidSegmentsExperienceKey = StringBundler.concat(
						plid, StringPool.POUND,
						resultSet.getString("segmentsExperienceKey"));

					if (!plidSegmentsExperienceKeys.add(
							plidSegmentsExperienceKey)) {

						continue;
					}

					Set<Integer> priorities = plidPriorities.get(plid);

					if (priorities == null) {
						priorities = _getPriorities(
							resultSet.getLong("groupId"), plid,
							preparedStatement2);

						plidPriorities.put(plid, priorities);
					}

					int priority = resultSet.getInt("priority");

					if (priorities.contains(priority)) {
						priority =
							(priority > 0) ? Collections.max(priorities) + 1 :
								Collections.min(priorities) - 1;
					}

					priorities.add(priority);

					preparedStatement3.setInt(1, priority);
					preparedStatement3.setLong(
						2, resultSet.getLong("ctCollectionId"));
					preparedStatement3.setLong(
						3, resultSet.getLong("segmentsExperienceId"));

					preparedStatement3.addBatch();
				}
			}

			preparedStatement3.executeBatch();
		}
	}

}