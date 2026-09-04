/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.internal.upgrade.v6_3_0;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.change.tracking.CTCollectionThreadLocal;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.LocaleThreadLocal;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.segments.constants.SegmentsExperienceConstants;
import com.liferay.segments.model.SegmentsExperience;
import com.liferay.segments.service.SegmentsExperienceLocalService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author Balázs Sáfrány-Kovalik
 */
public class DefaultSegmentsExperienceUpgradeProcess extends UpgradeProcess {

	public DefaultSegmentsExperienceUpgradeProcess(
		Portal portal,
		SegmentsExperienceLocalService segmentsExperienceLocalService,
		UserLocalService userLocalService) {

		_portal = portal;
		_segmentsExperienceLocalService = segmentsExperienceLocalService;
		_userLocalService = userLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select Layout.companyId, Layout.ctCollectionId, ",
					"Layout.externalReferenceCode, Layout.groupId, ",
					"Layout.plid, Layout.userId from Layout where ",
					"Layout.type_ in (?, ?, ?) and (not exists (select 1 from ",
					"SegmentsExperience where SegmentsExperience.groupId = ",
					"Layout.groupId and SegmentsExperience.plid = Layout.plid ",
					"and SegmentsExperience.segmentsExperienceKey = ? and ",
					"SegmentsExperience.ctCollectionId in (0, ",
					"Layout.ctCollectionId)) or exists (select 1 from ",
					"FragmentEntryLink where FragmentEntryLink.groupId = ",
					"Layout.groupId and FragmentEntryLink.plid = Layout.plid ",
					"and FragmentEntryLink.ctCollectionId = ",
					"Layout.ctCollectionId and ",
					"FragmentEntryLink.segmentsExperienceId > 0 and not ",
					"exists (select 1 from SegmentsExperience where ",
					"SegmentsExperience.segmentsExperienceId = ",
					"FragmentEntryLink.segmentsExperienceId and ",
					"SegmentsExperience.plid = FragmentEntryLink.plid and ",
					"SegmentsExperience.ctCollectionId in (0, ",
					"Layout.ctCollectionId))) or exists (select 1 from ",
					"LayoutPageTemplateStructureRel inner join ",
					"LayoutPageTemplateStructure on ",
					"LayoutPageTemplateStructure.",
					"layoutPageTemplateStructureId = ",
					"LayoutPageTemplateStructureRel.",
					"layoutPageTemplateStructureId where ",
					"LayoutPageTemplateStructure.plid = Layout.plid and ",
					"LayoutPageTemplateStructure.ctCollectionId in (0, ",
					"Layout.ctCollectionId) and ",
					"LayoutPageTemplateStructureRel.ctCollectionId = ",
					"Layout.ctCollectionId and ",
					"LayoutPageTemplateStructureRel.segmentsExperienceId > 0 ",
					"and not exists (select 1 from SegmentsExperience where ",
					"SegmentsExperience.segmentsExperienceId = ",
					"LayoutPageTemplateStructureRel.segmentsExperienceId and ",
					"SegmentsExperience.plid = Layout.plid and ",
					"SegmentsExperience.ctCollectionId in (0, ",
					"Layout.ctCollectionId)))) order by ",
					"Layout.ctCollectionId"))) {

			preparedStatement.setString(1, LayoutConstants.TYPE_CONTENT);
			preparedStatement.setString(2, LayoutConstants.TYPE_ASSET_DISPLAY);
			preparedStatement.setString(3, LayoutConstants.TYPE_UTILITY);
			preparedStatement.setString(
				4, SegmentsExperienceConstants.KEY_DEFAULT);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					long ctCollectionId = resultSet.getLong("ctCollectionId");

					try (SafeCloseable safeCloseable =
							CTCollectionThreadLocal.
								setCTCollectionIdWithSafeCloseable(
									ctCollectionId)) {

						_updateOrphanedSegmentsExperienceIds(
							resultSet.getLong("companyId"), ctCollectionId,
							resultSet.getString("externalReferenceCode"),
							resultSet.getLong("groupId"),
							resultSet.getLong("plid"),
							resultSet.getLong("userId"));
					}
				}
			}
		}
	}

	private long _addDefaultSegmentsExperience(
			long companyId, String externalReferenceCode, long groupId,
			long plid, long userId)
		throws Exception {

		Locale siteDefaultLocale = LocaleThreadLocal.getSiteDefaultLocale();

		try {
			LocaleThreadLocal.setSiteDefaultLocale(
				_portal.getSiteDefaultLocale(groupId));

			SegmentsExperience segmentsExperience =
				_segmentsExperienceLocalService.addDefaultSegmentsExperience(
					externalReferenceCode +
						LayoutConstants.EXTERNAL_REFERENCE_CODE_SUFFIX_DEFAULT,
					_getUserId(companyId, userId), plid, new ServiceContext());

			return segmentsExperience.getSegmentsExperienceId();
		}
		finally {
			LocaleThreadLocal.setSiteDefaultLocale(siteDefaultLocale);
		}
	}

	private void _deleteLayoutPageTemplateStructureRels(
			long ctCollectionId, long layoutPageTemplateStructureId,
			long orphanedSegmentsExperienceId)
		throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"delete from LayoutPageTemplateStructureRel where ",
					"ctCollectionId = ? and layoutPageTemplateStructureId = ? ",
					"and segmentsExperienceId = ?"))) {

			preparedStatement.setLong(1, ctCollectionId);
			preparedStatement.setLong(2, layoutPageTemplateStructureId);
			preparedStatement.setLong(3, orphanedSegmentsExperienceId);

			preparedStatement.executeUpdate();
		}
	}

	private long _getDefaultSegmentsExperienceId(
			long companyId, String externalReferenceCode, long groupId,
			long plid, long userId)
		throws Exception {

		long defaultSegmentsExperienceId =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperienceId(
				plid);

		if (defaultSegmentsExperienceId !=
				SegmentsExperienceConstants.ID_DEFAULT) {

			return defaultSegmentsExperienceId;
		}

		return _addDefaultSegmentsExperience(
			companyId, externalReferenceCode, groupId, plid, userId);
	}

	private Set<Long> _getFragmentEntryLinkSegmentsExperienceIds(
			long ctCollectionId, long groupId, long plid)
		throws Exception {

		Set<Long> segmentsExperienceIds = new HashSet<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select distinct FragmentEntryLink.segmentsExperienceId ",
					"from FragmentEntryLink where FragmentEntryLink.groupId = ",
					"? and FragmentEntryLink.plid = ? and ",
					"FragmentEntryLink.ctCollectionId = ? and ",
					"FragmentEntryLink.segmentsExperienceId > 0 and not ",
					"exists (select 1 from SegmentsExperience where ",
					"SegmentsExperience.segmentsExperienceId = ",
					"FragmentEntryLink.segmentsExperienceId and ",
					"SegmentsExperience.plid = FragmentEntryLink.plid and ",
					"SegmentsExperience.ctCollectionId in (0, ?))"))) {

			preparedStatement.setLong(1, groupId);
			preparedStatement.setLong(2, plid);
			preparedStatement.setLong(3, ctCollectionId);
			preparedStatement.setLong(4, ctCollectionId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					segmentsExperienceIds.add(
						resultSet.getLong("segmentsExperienceId"));
				}
			}
		}

		return segmentsExperienceIds;
	}

	private long _getLayoutPageTemplateStructureId(
			long ctCollectionId, long plid)
		throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select distinct layoutPageTemplateStructureId from ",
					"LayoutPageTemplateStructure where ctCollectionId in (0, ",
					"?) and plid = ?"))) {

			preparedStatement.setLong(1, ctCollectionId);
			preparedStatement.setLong(2, plid);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getLong("layoutPageTemplateStructureId");
				}
			}
		}

		return 0;
	}

	private Set<Long> _getLayoutPageTemplateStructureRelSegmentsExperienceIds(
			long ctCollectionId, long layoutPageTemplateStructureId, long plid)
		throws Exception {

		Set<Long> segmentsExperienceIds = new HashSet<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select distinct ",
					"LayoutPageTemplateStructureRel.segmentsExperienceId from ",
					"LayoutPageTemplateStructureRel where ",
					"LayoutPageTemplateStructureRel.ctCollectionId = ? and ",
					"LayoutPageTemplateStructureRel.",
					"layoutPageTemplateStructureId = ? and ",
					"LayoutPageTemplateStructureRel.segmentsExperienceId > 0 ",
					"and not exists (select 1 from SegmentsExperience where ",
					"SegmentsExperience.segmentsExperienceId = ",
					"LayoutPageTemplateStructureRel.segmentsExperienceId and ",
					"SegmentsExperience.plid = ? and ",
					"SegmentsExperience.ctCollectionId in (0, ?))"))) {

			preparedStatement.setLong(1, ctCollectionId);
			preparedStatement.setLong(2, layoutPageTemplateStructureId);
			preparedStatement.setLong(3, plid);
			preparedStatement.setLong(4, ctCollectionId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					segmentsExperienceIds.add(
						resultSet.getLong("segmentsExperienceId"));
				}
			}
		}

		return segmentsExperienceIds;
	}

	private long _getUserId(long companyId, long userId) throws Exception {
		User user = _userLocalService.fetchUser(userId);

		if (user == null) {
			return _userLocalService.getGuestUserId(companyId);
		}

		return userId;
	}

	private boolean _hasLayoutPageTemplateStructureRel(
			long ctCollectionId, long layoutPageTemplateStructureId,
			long segmentsExperienceId)
		throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select 1 from LayoutPageTemplateStructureRel where ",
					"ctCollectionId = ? and layoutPageTemplateStructureId = ? ",
					"and segmentsExperienceId = ?"))) {

			preparedStatement.setLong(1, ctCollectionId);
			preparedStatement.setLong(2, layoutPageTemplateStructureId);
			preparedStatement.setLong(3, segmentsExperienceId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				return resultSet.next();
			}
		}
	}

	private boolean _hasMultipleNondefaultSegmentsExperiences(
			long ctCollectionId, Set<Long> segmentsExperienceIds)
		throws Exception {

		int count = 0;

		for (long segmentsExperienceId : segmentsExperienceIds) {
			if (_isDefaultSegmentsExperience(
					ctCollectionId, segmentsExperienceId)) {

				continue;
			}

			count++;

			if (count > 1) {
				return true;
			}
		}

		return false;
	}

	private boolean _isDefaultSegmentsExperience(
			long ctCollectionId, long segmentsExperienceId)
		throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select 1 from SegmentsExperience where ",
					"segmentsExperienceId = ? and segmentsExperienceKey = ? ",
					"and ctCollectionId in (0, ?)"))) {

			preparedStatement.setLong(1, segmentsExperienceId);
			preparedStatement.setString(
				2, SegmentsExperienceConstants.KEY_DEFAULT);
			preparedStatement.setLong(3, ctCollectionId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				return resultSet.next();
			}
		}
	}

	private void _updateFragmentEntryLinks(
			long ctCollectionId, long defaultSegmentsExperienceId, long groupId,
			long orphanedSegmentsExperienceId, long plid)
		throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"update FragmentEntryLink set segmentsExperienceId = ? ",
					"where groupId = ? and plid = ? and ctCollectionId = ? ",
					"and segmentsExperienceId = ?"))) {

			preparedStatement.setLong(1, defaultSegmentsExperienceId);
			preparedStatement.setLong(2, groupId);
			preparedStatement.setLong(3, plid);
			preparedStatement.setLong(4, ctCollectionId);
			preparedStatement.setLong(5, orphanedSegmentsExperienceId);

			preparedStatement.executeUpdate();
		}
	}

	private void _updateLayoutPageTemplateStructureRels(
			long ctCollectionId, long defaultSegmentsExperienceId,
			long layoutPageTemplateStructureId,
			long orphanedSegmentsExperienceId)
		throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"update LayoutPageTemplateStructureRel set ",
					"segmentsExperienceId = ? where ctCollectionId = ? and ",
					"layoutPageTemplateStructureId = ? and ",
					"segmentsExperienceId = ?"))) {

			preparedStatement.setLong(1, defaultSegmentsExperienceId);
			preparedStatement.setLong(2, ctCollectionId);
			preparedStatement.setLong(3, layoutPageTemplateStructureId);
			preparedStatement.setLong(4, orphanedSegmentsExperienceId);

			preparedStatement.executeUpdate();
		}
	}

	private void _updateOrphanedSegmentsExperienceIds(
			long companyId, long ctCollectionId, String externalReferenceCode,
			long groupId, long plid, long userId)
		throws Exception {

		long defaultSegmentsExperienceId = _getDefaultSegmentsExperienceId(
			companyId, externalReferenceCode, groupId, plid, userId);

		long layoutPageTemplateStructureId = _getLayoutPageTemplateStructureId(
			ctCollectionId, plid);

		Set<Long> orphanedSegmentsExperienceIds =
			_getFragmentEntryLinkSegmentsExperienceIds(
				ctCollectionId, groupId, plid);

		if (layoutPageTemplateStructureId > 0) {
			orphanedSegmentsExperienceIds.addAll(
				_getLayoutPageTemplateStructureRelSegmentsExperienceIds(
					ctCollectionId, layoutPageTemplateStructureId, plid));
		}

		if (orphanedSegmentsExperienceIds.isEmpty()) {
			return;
		}

		if (_hasMultipleNondefaultSegmentsExperiences(
				ctCollectionId, orphanedSegmentsExperienceIds)) {

			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Unable to repoint layout ", plid,
						" because it references the orphaned segments ",
						"experiences ",
						StringUtil.merge(orphanedSegmentsExperienceIds, ", "),
						" and the correct mapping is ambiguous"));
			}

			return;
		}

		for (long orphanedSegmentsExperienceId :
				orphanedSegmentsExperienceIds) {

			_updateFragmentEntryLinks(
				ctCollectionId, defaultSegmentsExperienceId, groupId,
				orphanedSegmentsExperienceId, plid);

			if (layoutPageTemplateStructureId > 0) {
				if (_hasLayoutPageTemplateStructureRel(
						ctCollectionId, layoutPageTemplateStructureId,
						defaultSegmentsExperienceId)) {

					_deleteLayoutPageTemplateStructureRels(
						ctCollectionId, layoutPageTemplateStructureId,
						orphanedSegmentsExperienceId);
				}
				else {
					_updateLayoutPageTemplateStructureRels(
						ctCollectionId, defaultSegmentsExperienceId,
						layoutPageTemplateStructureId,
						orphanedSegmentsExperienceId);
				}
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DefaultSegmentsExperienceUpgradeProcess.class);

	private final Portal _portal;
	private final SegmentsExperienceLocalService
		_segmentsExperienceLocalService;
	private final UserLocalService _userLocalService;

}