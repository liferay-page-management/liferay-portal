/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.segments.internal.upgrade.v4_2_2;

import com.liferay.change.tracking.constants.CTConstants;
import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.model.CTEntry;
import com.liferay.change.tracking.service.CTCollectionLocalService;
import com.liferay.change.tracking.service.CTEntryLocalService;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructureRel;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.change.tracking.CTCollectionThreadLocal;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.ResourcePermission;
import com.liferay.portal.kernel.service.ResourceLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.segments.model.SegmentsExperience;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Georgel Pop
 */
public class SegmentsExperienceUpgradeProcess extends UpgradeProcess {

	public SegmentsExperienceUpgradeProcess(
		CTCollectionLocalService ctCollectionLocalService,
		CTEntryLocalService ctEntryLocalService,
		ResourceLocalService resourceLocalService) {

		_ctCollectionLocalService = ctCollectionLocalService;
		_ctEntryLocalService = ctEntryLocalService;
		_resourceLocalService = resourceLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		String fragmentEntryLinkColumnName = "plid";

		if (!hasColumn("FragmentEntryLink", "plid")) {
			fragmentEntryLinkColumnName = "classPK";
		}

		boolean hasFragmentEntryLinkSegmentsExperienceId = hasColumn(
			"FragmentEntryLink", "segmentsExperienceId");

		_updateCTCollectionSegmentsExperiences(
			false, fragmentEntryLinkColumnName,
			hasFragmentEntryLinkSegmentsExperienceId);

		_updateSegmentsExperiences();

		_updateDraftSegmentsExperiences();

		String layoutPageTemplateStructureColumnName = "plid";

		if (!hasColumn("LayoutPageTemplateStructure", "plid")) {
			layoutPageTemplateStructureColumnName = "classPK";
		}

		_addDraftLayoutSegmentsExperiences(
			fragmentEntryLinkColumnName,
			hasFragmentEntryLinkSegmentsExperienceId,
			layoutPageTemplateStructureColumnName);

		_addSegmentsExperienceModificationCTEntries();

		boolean hasFragmentEntryLinkCTCollectionId = hasColumn(
			"FragmentEntryLink", "ctCollectionId");
		boolean hasLayoutPageTemplateStructureRelCTCollectionId = hasColumn(
			"LayoutPageTemplateStructureRel", "ctCollectionId");

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
			true, fragmentEntryLinkColumnName,
			hasFragmentEntryLinkSegmentsExperienceId);

		_addSegmentsExperienceResources();

		_addSegmentsExperienceDeletionCTEntries(
			fragmentEntryLinkColumnName, hasFragmentEntryLinkCTCollectionId,
			hasFragmentEntryLinkSegmentsExperienceId,
			hasLayoutPageTemplateStructureRelCTCollectionId);
	}

	private void _addCTEntry(
			long companyId, long userId, long ctCollectionId,
			long modelClassNameId, long modelClassPK, long modelMvccVersion,
			int changeType)
		throws Exception {

		_ctEntryLocalService.addCTEntry(
			_createCTEntry(
				companyId, userId, ctCollectionId, modelClassNameId,
				modelClassPK, modelMvccVersion, changeType));
	}

	private void _addDeletionCTEntries(
			long companyId, long userId, long ctCollectionId,
			String modelClassName, String tableName,
			String primaryKeyColumnName, String sql, long... parameters)
		throws Exception {

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				sql);
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection,
					StringBundler.concat(
						"delete from ", tableName,
						" where ctCollectionId = ? and ", primaryKeyColumnName,
						" = ?"))) {

			for (int i = 0; i < parameters.length; i++) {
				preparedStatement1.setLong(i + 1, parameters[i]);
			}

			List<CTEntry> ctEntries = new ArrayList<>();
			Set<Long> ctEntryIds = new HashSet<>();

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				long modelClassNameId = PortalUtil.getClassNameId(
					modelClassName);

				while (resultSet.next()) {
					long modelClassPK = resultSet.getLong(primaryKeyColumnName);

					CTEntry ctEntry = _ctEntryLocalService.fetchCTEntry(
						ctCollectionId, modelClassNameId, modelClassPK);

					if (ctEntry != null) {
						if (ctEntry.getChangeType() ==
								CTConstants.CT_CHANGE_TYPE_ADDITION) {

							preparedStatement2.setLong(1, ctCollectionId);
							preparedStatement2.setLong(2, modelClassPK);

							preparedStatement2.addBatch();

							ctEntries.add(ctEntry);
						}
						else if (ctEntry.getChangeType() ==
									CTConstants.CT_CHANGE_TYPE_MODIFICATION) {

							preparedStatement2.setLong(1, ctCollectionId);
							preparedStatement2.setLong(2, modelClassPK);

							preparedStatement2.addBatch();

							ctEntryIds.add(ctEntry.getCtEntryId());
						}

						continue;
					}

					_addCTEntry(
						companyId, userId, ctCollectionId, modelClassNameId,
						modelClassPK, resultSet.getLong("mvccVersion"),
						CTConstants.CT_CHANGE_TYPE_DELETION);
				}
			}

			preparedStatement2.executeBatch();

			for (CTEntry ctEntry : ctEntries) {
				_ctEntryLocalService.deleteCTEntry(ctEntry);
			}

			for (long ctEntryId : ctEntryIds) {
				_ctEntryLocalService.updateChangeType(
					ctEntryId, CTConstants.CT_CHANGE_TYPE_DELETION);
			}
		}
	}

	private void _addDraftLayoutSegmentsExperiences(
			String fragmentEntryLinkColumnName,
			boolean hasFragmentEntryLinkSegmentsExperienceId,
			String layoutPageTemplateStructureColumnName)
		throws Exception {

		StringBundler sb = new StringBundler(49);

		sb.append("select SegmentsExperience.ctCollectionId, ");
		sb.append("SegmentsExperience.groupId, SegmentsExperience.companyId, ");
		sb.append("SegmentsExperience.userId, SegmentsExperience.userName, ");
		sb.append("SegmentsExperience.createDate, ");
		sb.append("SegmentsExperience.modifiedDate, ");
		sb.append("SegmentsExperience.segmentsEntryERC, ");
		sb.append("SegmentsExperience.segmentsEntryScopeERC, ");
		sb.append("SegmentsExperience.segmentsExperienceKey, Layout.plid, ");
		sb.append("SegmentsExperience.name, SegmentsExperience.priority, ");
		sb.append("SegmentsExperience.active_, ");
		sb.append("SegmentsExperience.typeSettings, ");
		sb.append("SegmentsExperience.lastPublishDate from ");
		sb.append("SegmentsExperience inner join Layout on ");
		sb.append("(Layout.ctCollectionId = 0 or Layout.ctCollectionId = ");
		sb.append("SegmentsExperience.ctCollectionId) and Layout.classNameId ");
		sb.append("= ? and Layout.classPK = SegmentsExperience.plid left ");
		sb.append("join CTCollection on CTCollection.ctCollectionId = ");
		sb.append("SegmentsExperience.ctCollectionId where ");
		sb.append("(SegmentsExperience.ctCollectionId = 0 or ");
		sb.append("CTCollection.status != ?) and not exists (select 1 from ");
		sb.append("SegmentsExperience draftSegmentsExperience where ");
		sb.append("(draftSegmentsExperience.ctCollectionId = 0 or ");
		sb.append("draftSegmentsExperience.ctCollectionId = ");
		sb.append("SegmentsExperience.ctCollectionId) and ");
		sb.append("draftSegmentsExperience.groupId = ");
		sb.append("SegmentsExperience.groupId and ");
		sb.append("draftSegmentsExperience.segmentsExperienceKey = ");
		sb.append("SegmentsExperience.segmentsExperienceKey and ");
		sb.append("draftSegmentsExperience.plid = Layout.plid) and (");

		if (hasFragmentEntryLinkSegmentsExperienceId) {
			sb.append("exists (select 1 from FragmentEntryLink where ");
			sb.append("FragmentEntryLink.groupId = ");
			sb.append("SegmentsExperience.groupId and FragmentEntryLink.");
			sb.append(fragmentEntryLinkColumnName);
			sb.append(" = Layout.plid and ");
			sb.append("FragmentEntryLink.segmentsExperienceId = ");
			sb.append("SegmentsExperience.segmentsExperienceId) or ");
		}

		sb.append("exists (select 1 from LayoutPageTemplateStructureRel ");
		sb.append("inner join LayoutPageTemplateStructure on ");
		sb.append("LayoutPageTemplateStructure.layoutPageTemplateStructureId ");
		sb.append("= LayoutPageTemplateStructureRel.");
		sb.append("layoutPageTemplateStructureId where ");
		sb.append("LayoutPageTemplateStructure.groupId = ");
		sb.append("SegmentsExperience.groupId and ");
		sb.append("LayoutPageTemplateStructure.");
		sb.append(layoutPageTemplateStructureColumnName);
		sb.append(" = Layout.plid and ");
		sb.append("LayoutPageTemplateStructureRel.segmentsExperienceId = ");
		sb.append("SegmentsExperience.segmentsExperienceId)) order by ");
		sb.append("SegmentsExperience.ctCollectionId");

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				sb.toString());
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection, _getInsertSegmentsExperienceSQL())) {

			preparedStatement1.setLong(
				1, PortalUtil.getClassNameId(Layout.class));
			preparedStatement1.setInt(2, WorkflowConstants.STATUS_APPROVED);

			List<CTEntry> ctEntries = new ArrayList<>();

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				Set<String> plidSegmentsExperienceKeys = new HashSet<>();
				long segmentsExperienceClassNameId = PortalUtil.getClassNameId(
					SegmentsExperience.class);

				while (resultSet.next()) {
					long plid = resultSet.getLong("plid");
					String segmentsExperienceKey = resultSet.getString(
						"segmentsExperienceKey");

					String plidSegmentsExperienceKey = StringBundler.concat(
						plid, StringPool.POUND, segmentsExperienceKey);

					if (!plidSegmentsExperienceKeys.add(
							plidSegmentsExperienceKey)) {

						continue;
					}

					preparedStatement2.setLong(1, 0);

					long ctCollectionId = resultSet.getLong("ctCollectionId");

					preparedStatement2.setLong(2, ctCollectionId);

					String uuid = PortalUUIDUtil.generate();

					preparedStatement2.setString(3, uuid);
					preparedStatement2.setString(4, uuid);

					long segmentsExperienceId = increment();

					preparedStatement2.setLong(5, segmentsExperienceId);

					preparedStatement2.setLong(6, resultSet.getLong("groupId"));

					long companyId = resultSet.getLong("companyId");

					preparedStatement2.setLong(7, companyId);

					long userId = resultSet.getLong("userId");

					preparedStatement2.setLong(8, userId);

					preparedStatement2.setString(
						9, resultSet.getString("userName"));
					preparedStatement2.setTimestamp(
						10, resultSet.getTimestamp("createDate"));
					preparedStatement2.setTimestamp(
						11, resultSet.getTimestamp("modifiedDate"));
					preparedStatement2.setString(
						12, resultSet.getString("segmentsEntryERC"));
					preparedStatement2.setString(
						13, resultSet.getString("segmentsEntryScopeERC"));
					preparedStatement2.setString(14, segmentsExperienceKey);
					preparedStatement2.setLong(15, plid);
					preparedStatement2.setString(
						16, resultSet.getString("name"));
					preparedStatement2.setInt(17, resultSet.getInt("priority"));
					preparedStatement2.setBoolean(
						18, resultSet.getBoolean("active_"));
					preparedStatement2.setString(
						19, resultSet.getString("typeSettings"));
					preparedStatement2.setTimestamp(
						20, resultSet.getTimestamp("lastPublishDate"));

					preparedStatement2.addBatch();

					if (ctCollectionId == 0) {
						continue;
					}

					ctEntries.add(
						_createCTEntry(
							companyId, userId, ctCollectionId,
							segmentsExperienceClassNameId, segmentsExperienceId,
							0, CTConstants.CT_CHANGE_TYPE_ADDITION));
				}
			}

			preparedStatement2.executeBatch();

			for (CTEntry ctEntry : ctEntries) {
				_ctEntryLocalService.addCTEntry(ctEntry);
			}
		}
	}

	private void _addSegmentsExperienceDeletionCTEntries(
			String fragmentEntryLinkColumnName,
			boolean hasFragmentEntryLinkCTCollectionId,
			boolean hasFragmentEntryLinkSegmentsExperienceId,
			boolean hasLayoutPageTemplateStructureRelCTCollectionId)
		throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select draftSegmentsExperience.mvccVersion, ",
					"CTEntry.ctCollectionId, ",
					"draftSegmentsExperience.segmentsExperienceId, ",
					"draftSegmentsExperience.groupId, ",
					"draftSegmentsExperience.companyId, CTEntry.userId, ",
					"draftSegmentsExperience.plid from CTEntry inner join ",
					"CTCollection on CTCollection.ctCollectionId = ",
					"CTEntry.ctCollectionId inner join SegmentsExperience ",
					"publishedSegmentsExperience on ",
					"publishedSegmentsExperience.ctCollectionId = 0 and ",
					"publishedSegmentsExperience.segmentsExperienceId = ",
					"CTEntry.modelClassPK inner join Layout on ",
					"Layout.ctCollectionId = 0 and Layout.classNameId = ? and ",
					"Layout.classPK = publishedSegmentsExperience.plid inner ",
					"join SegmentsExperience draftSegmentsExperience on ",
					"draftSegmentsExperience.ctCollectionId = 0 and ",
					"draftSegmentsExperience.groupId = ",
					"publishedSegmentsExperience.groupId and ",
					"draftSegmentsExperience.segmentsExperienceKey = ",
					"publishedSegmentsExperience.segmentsExperienceKey and ",
					"draftSegmentsExperience.plid = Layout.plid where ",
					"CTCollection.status != ? and CTEntry.modelClassNameId = ",
					"? and CTEntry.changeType = ? and not exists (select 1 ",
					"from CTEntry draftCTEntry where ",
					"draftCTEntry.ctCollectionId = CTEntry.ctCollectionId and ",
					"draftCTEntry.modelClassNameId = ? and ",
					"draftCTEntry.modelClassPK = ",
					"draftSegmentsExperience.segmentsExperienceId)"))) {

			long segmentsExperienceClassNameId = PortalUtil.getClassNameId(
				SegmentsExperience.class);

			preparedStatement.setLong(
				1, PortalUtil.getClassNameId(Layout.class));
			preparedStatement.setInt(2, WorkflowConstants.STATUS_APPROVED);
			preparedStatement.setLong(3, segmentsExperienceClassNameId);
			preparedStatement.setInt(4, CTConstants.CT_CHANGE_TYPE_DELETION);
			preparedStatement.setLong(5, segmentsExperienceClassNameId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					long companyId = resultSet.getLong("companyId");
					long userId = resultSet.getLong("userId");
					long ctCollectionId = resultSet.getLong("ctCollectionId");
					long groupId = resultSet.getLong("groupId");
					long segmentsExperienceId = resultSet.getLong(
						"segmentsExperienceId");

					if (hasFragmentEntryLinkCTCollectionId &&
						hasFragmentEntryLinkSegmentsExperienceId) {

						_addDeletionCTEntries(
							companyId, userId, ctCollectionId,
							"com.liferay.fragment.model.FragmentEntryLink",
							"FragmentEntryLink", "fragmentEntryLinkId",
							StringBundler.concat(
								"select mvccVersion, fragmentEntryLinkId from ",
								"FragmentEntryLink where ctCollectionId in ",
								"(0, ?) and groupId = ? and ",
								"segmentsExperienceId = ? and ",
								fragmentEntryLinkColumnName, " = ?"),
							ctCollectionId, groupId, segmentsExperienceId,
							resultSet.getLong("plid"));
					}

					if (hasLayoutPageTemplateStructureRelCTCollectionId) {
						_addDeletionCTEntries(
							companyId, userId, ctCollectionId,
							LayoutPageTemplateStructureRel.class.getName(),
							"LayoutPageTemplateStructureRel",
							"lPageTemplateStructureRelId",
							StringBundler.concat(
								"select mvccVersion, ",
								"lPageTemplateStructureRelId from ",
								"LayoutPageTemplateStructureRel where ",
								"ctCollectionId = 0 and groupId = ? and ",
								"segmentsExperienceId = ?"),
							groupId, segmentsExperienceId);
					}

					_addDeletionCTEntries(
						companyId, userId, ctCollectionId,
						ResourcePermission.class.getName(),
						"ResourcePermission", "resourcePermissionId",
						StringBundler.concat(
							"select mvccVersion, resourcePermissionId from ",
							"ResourcePermission where ctCollectionId = 0 and ",
							"companyId = ? and name = '",
							SegmentsExperience.class.getName(),
							"' and scope = ",
							ResourceConstants.SCOPE_INDIVIDUAL,
							" and primKeyId = ?"),
						companyId, segmentsExperienceId);

					_addCTEntry(
						companyId, userId, ctCollectionId,
						segmentsExperienceClassNameId, segmentsExperienceId,
						resultSet.getLong("mvccVersion"),
						CTConstants.CT_CHANGE_TYPE_DELETION);
				}
			}
		}
	}

	private void _addSegmentsExperienceModificationCTEntries()
		throws Exception {

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				StringBundler.concat(
					"select CTEntry.ctCollectionId, CTEntry.userId, ",
					"draftSegmentsExperience.mvccVersion, ",
					"draftSegmentsExperience.uuid_, ",
					"draftSegmentsExperience.externalReferenceCode, ",
					"draftSegmentsExperience.segmentsExperienceId, ",
					"draftSegmentsExperience.groupId, ",
					"draftSegmentsExperience.companyId, ",
					"draftSegmentsExperience.userId as ",
					"segmentsExperienceUserId, ",
					"draftSegmentsExperience.userName, ",
					"draftSegmentsExperience.createDate, ",
					"draftSegmentsExperience.segmentsEntryERC as ",
					"draftSegmentsEntryERC, ",
					"draftSegmentsExperience.segmentsEntryScopeERC as ",
					"draftSegmentsEntryScopeERC, ",
					"draftSegmentsExperience.segmentsExperienceKey, ",
					"draftSegmentsExperience.plid, ",
					"draftSegmentsExperience.name as draftName, ",
					"draftSegmentsExperience.priority as draftPriority, ",
					"draftSegmentsExperience.active_ as draftActive, ",
					"draftSegmentsExperience.typeSettings as ",
					"draftTypeSettings, ",
					"draftSegmentsExperience.lastPublishDate, ",
					"publishedSegmentsExperience.modifiedDate, ",
					"publishedSegmentsExperience.segmentsEntryERC, ",
					"publishedSegmentsExperience.segmentsEntryScopeERC, ",
					"publishedSegmentsExperience.name, ",
					"publishedSegmentsExperience.priority, ",
					"publishedSegmentsExperience.active_, ",
					"publishedSegmentsExperience.typeSettings from CTEntry ",
					"inner join CTCollection on CTCollection.ctCollectionId = ",
					"CTEntry.ctCollectionId inner join SegmentsExperience ",
					"publishedSegmentsExperience on ",
					"publishedSegmentsExperience.ctCollectionId = ",
					"CTEntry.ctCollectionId and ",
					"publishedSegmentsExperience.segmentsExperienceId = ",
					"CTEntry.modelClassPK inner join Layout on ",
					"Layout.ctCollectionId = 0 and Layout.classNameId = ? and ",
					"Layout.classPK = publishedSegmentsExperience.plid inner ",
					"join SegmentsExperience draftSegmentsExperience on ",
					"draftSegmentsExperience.ctCollectionId = 0 and ",
					"draftSegmentsExperience.groupId = ",
					"publishedSegmentsExperience.groupId and ",
					"draftSegmentsExperience.segmentsExperienceKey = ",
					"publishedSegmentsExperience.segmentsExperienceKey and ",
					"draftSegmentsExperience.plid = Layout.plid where ",
					"CTCollection.status != ? and CTEntry.modelClassNameId = ",
					"? and CTEntry.changeType = ? and not exists (select 1 ",
					"from CTEntry draftCTEntry where ",
					"draftCTEntry.ctCollectionId = CTEntry.ctCollectionId and ",
					"draftCTEntry.modelClassNameId = ? and ",
					"draftCTEntry.modelClassPK = ",
					"draftSegmentsExperience.segmentsExperienceId) and not ",
					"exists (select 1 from SegmentsExperience where ",
					"SegmentsExperience.ctCollectionId = ",
					"CTEntry.ctCollectionId and ",
					"SegmentsExperience.segmentsExperienceId = ",
					"draftSegmentsExperience.segmentsExperienceId)"));
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection, _getInsertSegmentsExperienceSQL())) {

			long segmentsExperienceClassNameId = PortalUtil.getClassNameId(
				SegmentsExperience.class);

			preparedStatement1.setLong(
				1, PortalUtil.getClassNameId(Layout.class));
			preparedStatement1.setInt(2, WorkflowConstants.STATUS_APPROVED);
			preparedStatement1.setLong(3, segmentsExperienceClassNameId);
			preparedStatement1.setInt(
				4, CTConstants.CT_CHANGE_TYPE_MODIFICATION);
			preparedStatement1.setLong(5, segmentsExperienceClassNameId);

			List<CTEntry> ctEntries = new ArrayList<>();

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				while (resultSet.next()) {
					boolean active = resultSet.getBoolean("active_");
					String name = resultSet.getString("name");
					int priority = resultSet.getInt("priority");
					String segmentsEntryERC = resultSet.getString(
						"segmentsEntryERC");
					String segmentsEntryScopeERC = resultSet.getString(
						"segmentsEntryScopeERC");
					String typeSettings = resultSet.getString("typeSettings");

					if (!_isSegmentsExperienceChanged(
							active, name, priority, resultSet, segmentsEntryERC,
							segmentsEntryScopeERC, typeSettings)) {

						continue;
					}

					long companyId = resultSet.getLong("companyId");
					long ctCollectionId = resultSet.getLong("ctCollectionId");
					long mvccVersion = resultSet.getLong("mvccVersion");
					long segmentsExperienceId = resultSet.getLong(
						"segmentsExperienceId");

					preparedStatement2.setLong(1, mvccVersion);
					preparedStatement2.setLong(2, ctCollectionId);
					preparedStatement2.setString(
						3, resultSet.getString("uuid_"));
					preparedStatement2.setString(
						4, resultSet.getString("externalReferenceCode"));
					preparedStatement2.setLong(5, segmentsExperienceId);
					preparedStatement2.setLong(6, resultSet.getLong("groupId"));
					preparedStatement2.setLong(7, companyId);
					preparedStatement2.setLong(
						8, resultSet.getLong("segmentsExperienceUserId"));
					preparedStatement2.setString(
						9, resultSet.getString("userName"));
					preparedStatement2.setTimestamp(
						10, resultSet.getTimestamp("createDate"));
					preparedStatement2.setTimestamp(
						11, resultSet.getTimestamp("modifiedDate"));
					preparedStatement2.setString(12, segmentsEntryERC);
					preparedStatement2.setString(13, segmentsEntryScopeERC);
					preparedStatement2.setString(
						14, resultSet.getString("segmentsExperienceKey"));
					preparedStatement2.setLong(15, resultSet.getLong("plid"));
					preparedStatement2.setString(16, name);
					preparedStatement2.setInt(17, priority);
					preparedStatement2.setBoolean(18, active);
					preparedStatement2.setString(19, typeSettings);
					preparedStatement2.setTimestamp(
						20, resultSet.getTimestamp("lastPublishDate"));

					preparedStatement2.addBatch();

					ctEntries.add(
						_createCTEntry(
							companyId, resultSet.getLong("userId"),
							ctCollectionId, segmentsExperienceClassNameId,
							segmentsExperienceId, mvccVersion,
							CTConstants.CT_CHANGE_TYPE_MODIFICATION));
				}
			}

			preparedStatement2.executeBatch();

			for (CTEntry ctEntry : ctEntries) {
				_ctEntryLocalService.addCTEntry(ctEntry);
			}
		}
	}

	private void _addSegmentsExperienceResources() throws Exception {
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select SegmentsExperience.ctCollectionId, ",
					"SegmentsExperience.segmentsExperienceId, ",
					"SegmentsExperience.groupId, ",
					"SegmentsExperience.companyId, SegmentsExperience.userId ",
					"from SegmentsExperience left join CTCollection on ",
					"CTCollection.ctCollectionId = ",
					"SegmentsExperience.ctCollectionId where ",
					"(SegmentsExperience.ctCollectionId = 0 or ",
					"CTCollection.status != ?) and exists (select 1 from ",
					"Layout where (Layout.ctCollectionId = 0 or ",
					"Layout.ctCollectionId = ",
					"SegmentsExperience.ctCollectionId) and Layout.plid = ",
					"SegmentsExperience.plid and Layout.classNameId = ?) and ",
					"not exists (select 1 from ResourcePermission where ",
					"(ResourcePermission.ctCollectionId = 0 or ",
					"ResourcePermission.ctCollectionId = ",
					"SegmentsExperience.ctCollectionId) and ",
					"ResourcePermission.companyId = ",
					"SegmentsExperience.companyId and ResourcePermission.name ",
					"= ? and ResourcePermission.scope = ? and ",
					"ResourcePermission.primKeyId = ",
					"SegmentsExperience.segmentsExperienceId)"))) {

			preparedStatement.setInt(1, WorkflowConstants.STATUS_APPROVED);
			preparedStatement.setLong(
				2, PortalUtil.getClassNameId(Layout.class));
			preparedStatement.setString(3, SegmentsExperience.class.getName());
			preparedStatement.setInt(4, ResourceConstants.SCOPE_INDIVIDUAL);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					long ctCollectionId = resultSet.getLong("ctCollectionId");

					if (ctCollectionId != 0) {
						CTCollection ctCollection =
							_ctCollectionLocalService.getCTCollection(
								ctCollectionId);

						if (ctCollection.isReadOnly()) {
							ctCollectionId = 0;
						}
					}

					try (SafeCloseable safeCloseable =
							CTCollectionThreadLocal.
								setCTCollectionIdWithSafeCloseable(
									ctCollectionId)) {

						_resourceLocalService.addResources(
							resultSet.getLong("companyId"),
							resultSet.getLong("groupId"),
							resultSet.getLong("userId"),
							SegmentsExperience.class.getName(),
							resultSet.getLong("segmentsExperienceId"), false,
							true, true);
					}
				}
			}
		}
	}

	private CTEntry _createCTEntry(
			long companyId, long userId, long ctCollectionId,
			long modelClassNameId, long modelClassPK, long modelMvccVersion,
			int changeType)
		throws Exception {

		CTEntry ctEntry = _ctEntryLocalService.createCTEntry(
			increment(CTEntry.class.getName()));

		ctEntry.setCompanyId(companyId);
		ctEntry.setUserId(userId);
		ctEntry.setCtCollectionId(ctCollectionId);
		ctEntry.setModelClassNameId(modelClassNameId);
		ctEntry.setModelClassPK(modelClassPK);
		ctEntry.setModelMvccVersion(modelMvccVersion);
		ctEntry.setChangeType(changeType);

		return ctEntry;
	}

	private String _getInsertSegmentsExperienceSQL() {
		return StringBundler.concat(
			"insert into SegmentsExperience (mvccVersion, ctCollectionId, ",
			"uuid_, externalReferenceCode, segmentsExperienceId, groupId, ",
			"companyId, userId, userName, createDate, modifiedDate, ",
			"segmentsEntryERC, segmentsEntryScopeERC, segmentsExperienceKey, ",
			"plid, name, priority, active_, typeSettings, lastPublishDate) ",
			"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ",
			"?)");
	}

	private Set<Integer> _getPriorities(
			long groupId, long plid, Map<Long, Set<Integer>> plidPriorities,
			PreparedStatement preparedStatement)
		throws Exception {

		Set<Integer> priorities = plidPriorities.get(plid);

		if (priorities != null) {
			return priorities;
		}

		priorities = new HashSet<>();

		preparedStatement.setLong(1, groupId);
		preparedStatement.setLong(2, plid);

		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			while (resultSet.next()) {
				priorities.add(resultSet.getInt("priority"));
			}
		}

		plidPriorities.put(plid, priorities);

		return priorities;
	}

	private String _getSelectSegmentsExperienceSQL(
		String fragmentEntryLinkColumnName,
		boolean hasFragmentEntryLinkSegmentsExperienceId,
		String layoutPageTemplateStructureColumnName) {

		StringBundler sb = new StringBundler(49);

		sb.append("select publishedSegmentsExperience.ctCollectionId, ");
		sb.append("draftSegmentsExperience.segmentsExperienceId as ");
		sb.append("newSegmentsExperienceId, draftSegmentsExperience.groupId, ");
		sb.append("draftSegmentsExperience.plid, ");
		sb.append("publishedSegmentsExperience.segmentsExperienceId as ");
		sb.append("oldSegmentsExperienceId from SegmentsExperience ");
		sb.append("draftSegmentsExperience inner join Layout on ");
		sb.append("(Layout.ctCollectionId = 0 or Layout.ctCollectionId = ");
		sb.append("draftSegmentsExperience.ctCollectionId) and Layout.plid = ");
		sb.append("draftSegmentsExperience.plid inner join ");
		sb.append("SegmentsExperience publishedSegmentsExperience on ");
		sb.append("publishedSegmentsExperience.groupId = ");
		sb.append("draftSegmentsExperience.groupId and ");
		sb.append("publishedSegmentsExperience.segmentsExperienceKey = ");
		sb.append("draftSegmentsExperience.segmentsExperienceKey and ");
		sb.append("publishedSegmentsExperience.plid = Layout.classPK and ");
		sb.append("(publishedSegmentsExperience.ctCollectionId = ");
		sb.append("draftSegmentsExperience.ctCollectionId or ");
		sb.append("(draftSegmentsExperience.ctCollectionId = 0 and not ");
		sb.append("exists (select 1 from SegmentsExperience ");
		sb.append("ctCollectionSegmentsExperience where ");
		sb.append("ctCollectionSegmentsExperience.ctCollectionId = ");
		sb.append("publishedSegmentsExperience.ctCollectionId and ");
		sb.append("ctCollectionSegmentsExperience.groupId = ");
		sb.append("draftSegmentsExperience.groupId and ");
		sb.append("ctCollectionSegmentsExperience.segmentsExperienceKey = ");
		sb.append("draftSegmentsExperience.segmentsExperienceKey and ");
		sb.append("ctCollectionSegmentsExperience.plid = ");
		sb.append("draftSegmentsExperience.plid))) where Layout.classNameId ");
		sb.append("= ? and (");

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

	private boolean _isSegmentsExperienceChanged(
			boolean active, String name, int priority, ResultSet resultSet,
			String segmentsEntryERC, String segmentsEntryScopeERC,
			String typeSettings)
		throws Exception {

		if (Objects.equals(
				segmentsEntryERC,
				resultSet.getString("draftSegmentsEntryERC")) &&
			Objects.equals(
				segmentsEntryScopeERC,
				resultSet.getString("draftSegmentsEntryScopeERC")) &&
			Objects.equals(name, resultSet.getString("draftName")) &&
			(priority == resultSet.getInt("draftPriority")) &&
			(active == resultSet.getBoolean("draftActive")) &&
			Objects.equals(
				typeSettings, resultSet.getString("draftTypeSettings"))) {

			return false;
		}

		return true;
	}

	private void _updateCTCollectionSegmentsExperiences(
			boolean approved, String fragmentEntryLinkColumnName,
			boolean hasFragmentEntryLinkSegmentsExperienceId)
		throws Exception {

		String statusOperator = "!=";

		if (approved) {
			statusOperator = "=";
		}

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				StringBundler.concat(
					"select SegmentsExperience.mvccVersion, ",
					"SegmentsExperience.ctCollectionId, ",
					"SegmentsExperience.segmentsExperienceId as ",
					"oldSegmentsExperienceId, SegmentsExperience.groupId, ",
					"SegmentsExperience.companyId, SegmentsExperience.userId, ",
					"SegmentsExperience.plid, ",
					"productionSegmentsExperience.segmentsExperienceId as ",
					"newSegmentsExperienceId from SegmentsExperience inner ",
					"join CTCollection on CTCollection.ctCollectionId = ",
					"SegmentsExperience.ctCollectionId left join ",
					"SegmentsExperience productionSegmentsExperience on ",
					"productionSegmentsExperience.ctCollectionId = 0 and ",
					"productionSegmentsExperience.groupId = ",
					"SegmentsExperience.groupId and ",
					"productionSegmentsExperience.segmentsExperienceKey = ",
					"SegmentsExperience.segmentsExperienceKey and ",
					"productionSegmentsExperience.plid = ",
					"SegmentsExperience.plid where CTCollection.status ",
					statusOperator, " ? and exists (select 1 from Layout ",
					"where (Layout.ctCollectionId = 0 or ",
					"Layout.ctCollectionId = ",
					"SegmentsExperience.ctCollectionId) and Layout.plid = ",
					"SegmentsExperience.plid and Layout.classNameId = ?) and ",
					"not exists (select 1 from CTEntry where ",
					"CTEntry.ctCollectionId = ",
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
							"= ?");
			PreparedStatement preparedStatement4 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					"delete from SegmentsExperience where ctCollectionId = ? " +
						"and segmentsExperienceId = ?")) {

			long segmentsExperienceClassNameId = PortalUtil.getClassNameId(
				SegmentsExperience.class);

			preparedStatement1.setInt(1, WorkflowConstants.STATUS_APPROVED);
			preparedStatement1.setLong(
				2, PortalUtil.getClassNameId(Layout.class));
			preparedStatement1.setLong(3, segmentsExperienceClassNameId);

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				while (resultSet.next()) {
					long ctCollectionId = resultSet.getLong("ctCollectionId");
					long oldSegmentsExperienceId = resultSet.getLong(
						"oldSegmentsExperienceId");
					long newSegmentsExperienceId = resultSet.getLong(
						"newSegmentsExperienceId");

					if (newSegmentsExperienceId == 0) {
						if (!approved) {
							_addCTEntry(
								resultSet.getLong("companyId"),
								resultSet.getLong("userId"), ctCollectionId,
								segmentsExperienceClassNameId,
								oldSegmentsExperienceId,
								resultSet.getLong("mvccVersion"),
								CTConstants.CT_CHANGE_TYPE_ADDITION);
						}

						continue;
					}

					if (!approved) {
						preparedStatement4.setLong(1, ctCollectionId);
						preparedStatement4.setLong(2, oldSegmentsExperienceId);

						preparedStatement4.addBatch();
					}

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

			preparedStatement4.executeBatch();
		}
	}

	private void _updateDraftSegmentsExperiencePriorities(
			Map<Long, Long> plidGroupIds,
			Map<Long, Map<Long, Integer>> plidSegmentsExperiencePriorities)
		throws Exception {

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				"select segmentsExperienceId, priority from " +
					"SegmentsExperience where ctCollectionId = 0 and groupId " +
						"= ? and plid = ?");
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection,
					"update SegmentsExperience set priority = ? where " +
						"ctCollectionId = 0 and segmentsExperienceId = ?")) {

			for (Map.Entry<Long, Map<Long, Integer>> entry :
					plidSegmentsExperiencePriorities.entrySet()) {

				Map<Long, Integer> draftPriorities = new HashMap<>();
				int minimumPriority = 0;
				Set<Integer> priorities = new HashSet<>();
				Map<Long, Integer> segmentsExperiencePriorities =
					entry.getValue();

				preparedStatement1.setLong(1, plidGroupIds.get(entry.getKey()));
				preparedStatement1.setLong(2, entry.getKey());

				try (ResultSet resultSet = preparedStatement1.executeQuery()) {
					while (resultSet.next()) {
						int priority = resultSet.getInt("priority");
						long segmentsExperienceId = resultSet.getLong(
							"segmentsExperienceId");

						minimumPriority = Math.min(minimumPriority, priority);

						if (segmentsExperiencePriorities.containsKey(
								segmentsExperienceId)) {

							draftPriorities.put(segmentsExperienceId, priority);
						}
						else {
							priorities.add(priority);
						}
					}
				}

				boolean reverted = true;

				while (reverted) {
					reverted = false;

					Set<Integer> takenPriorities = new HashSet<>(priorities);

					Iterator<Map.Entry<Long, Integer>> iterator =
						segmentsExperiencePriorities.entrySet(
						).iterator();

					while (iterator.hasNext()) {
						Map.Entry<Long, Integer> segmentsExperiencePriority =
							iterator.next();

						if (takenPriorities.add(
								segmentsExperiencePriority.getValue())) {

							continue;
						}

						priorities.add(
							draftPriorities.get(
								segmentsExperiencePriority.getKey()));

						iterator.remove();

						reverted = true;
					}
				}

				for (int priority : segmentsExperiencePriorities.values()) {
					minimumPriority = Math.min(minimumPriority, priority);
				}

				for (long segmentsExperienceId :
						segmentsExperiencePriorities.keySet()) {

					preparedStatement2.setInt(1, --minimumPriority);
					preparedStatement2.setLong(2, segmentsExperienceId);

					preparedStatement2.addBatch();
				}

				for (Map.Entry<Long, Integer> segmentsExperiencePriority :
						segmentsExperiencePriorities.entrySet()) {

					preparedStatement2.setInt(
						1, segmentsExperiencePriority.getValue());
					preparedStatement2.setLong(
						2, segmentsExperiencePriority.getKey());

					preparedStatement2.addBatch();
				}
			}

			preparedStatement2.executeBatch();
		}
	}

	private void _updateDraftSegmentsExperiences() throws Exception {
		Map<Long, Long> plidGroupIds = new HashMap<>();
		Map<Long, Map<Long, Integer>> plidSegmentsExperiencePriorities =
			new HashMap<>();

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				StringBundler.concat(
					"select SegmentsExperience.groupId, ",
					"SegmentsExperience.modifiedDate, ",
					"SegmentsExperience.segmentsEntryERC, ",
					"SegmentsExperience.segmentsEntryScopeERC, ",
					"SegmentsExperience.plid, SegmentsExperience.name, ",
					"SegmentsExperience.priority, SegmentsExperience.active_, ",
					"SegmentsExperience.typeSettings, ",
					"draftSegmentsExperience.segmentsExperienceId, ",
					"draftSegmentsExperience.segmentsEntryERC as ",
					"draftSegmentsEntryERC, ",
					"draftSegmentsExperience.segmentsEntryScopeERC as ",
					"draftSegmentsEntryScopeERC, draftSegmentsExperience.name ",
					"as draftName, draftSegmentsExperience.priority as ",
					"draftPriority, draftSegmentsExperience.active_ as ",
					"draftActive, draftSegmentsExperience.typeSettings as ",
					"draftTypeSettings from SegmentsExperience inner join ",
					"CTCollection on CTCollection.ctCollectionId = ",
					"SegmentsExperience.ctCollectionId inner join Layout on ",
					"Layout.ctCollectionId = 0 and Layout.plid = ",
					"SegmentsExperience.plid inner join SegmentsExperience ",
					"draftSegmentsExperience on ",
					"draftSegmentsExperience.ctCollectionId = 0 and ",
					"draftSegmentsExperience.groupId = ",
					"SegmentsExperience.groupId and ",
					"draftSegmentsExperience.segmentsExperienceKey = ",
					"SegmentsExperience.segmentsExperienceKey and ",
					"draftSegmentsExperience.plid = SegmentsExperience.plid ",
					"where CTCollection.status = ? and Layout.classNameId = ? ",
					"and draftSegmentsExperience.modifiedDate <= ",
					"SegmentsExperience.modifiedDate and not exists (select 1 ",
					"from CTEntry where CTEntry.ctCollectionId = ",
					"SegmentsExperience.ctCollectionId and ",
					"CTEntry.modelClassNameId = ? and CTEntry.modelClassPK = ",
					"SegmentsExperience.segmentsExperienceId) order by ",
					"SegmentsExperience.modifiedDate desc"));
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					StringBundler.concat(
						"update SegmentsExperience set modifiedDate = ?, ",
						"segmentsEntryERC = ?, segmentsEntryScopeERC = ?, ",
						"name = ?, priority = ?, active_ = ?, typeSettings = ",
						"? where ctCollectionId = 0 and segmentsExperienceId ",
						"= ?"))) {

			preparedStatement1.setInt(1, WorkflowConstants.STATUS_APPROVED);
			preparedStatement1.setLong(
				2, PortalUtil.getClassNameId(Layout.class));
			preparedStatement1.setLong(
				3, PortalUtil.getClassNameId(SegmentsExperience.class));

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				Set<Long> segmentsExperienceIds = new HashSet<>();

				while (resultSet.next()) {
					long segmentsExperienceId = resultSet.getLong(
						"segmentsExperienceId");

					if (!segmentsExperienceIds.add(segmentsExperienceId)) {
						continue;
					}

					boolean active = resultSet.getBoolean("active_");
					String name = resultSet.getString("name");
					int priority = resultSet.getInt("priority");
					String segmentsEntryERC = resultSet.getString(
						"segmentsEntryERC");
					String segmentsEntryScopeERC = resultSet.getString(
						"segmentsEntryScopeERC");
					String typeSettings = resultSet.getString("typeSettings");

					if (!_isSegmentsExperienceChanged(
							active, name, priority, resultSet, segmentsEntryERC,
							segmentsEntryScopeERC, typeSettings)) {

						continue;
					}

					int draftPriority = resultSet.getInt("draftPriority");

					if (priority != draftPriority) {
						long plid = resultSet.getLong("plid");

						plidGroupIds.put(plid, resultSet.getLong("groupId"));

						Map<Long, Integer> segmentsExperiencePriorities =
							plidSegmentsExperiencePriorities.computeIfAbsent(
								plid, key -> new LinkedHashMap<>());

						segmentsExperiencePriorities.put(
							segmentsExperienceId, priority);

						priority = draftPriority;
					}

					preparedStatement2.setTimestamp(
						1, resultSet.getTimestamp("modifiedDate"));
					preparedStatement2.setString(2, segmentsEntryERC);
					preparedStatement2.setString(3, segmentsEntryScopeERC);
					preparedStatement2.setString(4, name);
					preparedStatement2.setInt(5, priority);
					preparedStatement2.setBoolean(6, active);
					preparedStatement2.setString(7, typeSettings);
					preparedStatement2.setLong(8, segmentsExperienceId);

					preparedStatement2.addBatch();
				}
			}

			preparedStatement2.executeBatch();
		}

		_updateDraftSegmentsExperiencePriorities(
			plidGroupIds, plidSegmentsExperiencePriorities);
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

					Set<Integer> priorities = _getPriorities(
						resultSet.getLong("groupId"), plid, plidPriorities,
						preparedStatement2);
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

	private final CTCollectionLocalService _ctCollectionLocalService;
	private final CTEntryLocalService _ctEntryLocalService;
	private final ResourceLocalService _resourceLocalService;

}