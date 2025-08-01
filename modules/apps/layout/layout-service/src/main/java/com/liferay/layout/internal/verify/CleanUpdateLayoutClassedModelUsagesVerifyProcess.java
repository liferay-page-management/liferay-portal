/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.internal.verify;

import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.layout.manager.ContentManager;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructure;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructureRel;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalService;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureRelLocalService;
import com.liferay.layout.service.LayoutClassedModelUsageLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.dao.orm.common.SQLTransformer;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Release;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.verify.VerifyProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Georgel Pop
 */
@Component(service = VerifyProcess.class)
public class CleanUpdateLayoutClassedModelUsagesVerifyProcess
	extends VerifyProcess {

	@Override
	protected void doVerify() throws Exception {
		_cleanUpdateLayoutClassedModelUsage();
	}

	private void _cleanUpdateLayoutClassedModelUsage() throws Exception {
		long containerTypeFragmentEntryLink =
			_classNameLocalService.getClassNameId(
				FragmentEntryLink.class.getName());
		long containerTypeLayoutPageTemplateStructure =
			_classNameLocalService.getClassNameId(
				LayoutPageTemplateStructure.class.getName());
		Map<Long, Set<Long>> fragmentEntryLinkPlid = new HashMap<>();
		Map<Long, Set<Long>> layoutPageTemplateStructurePlid = new HashMap<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				SQLTransformer.transform(
					StringBundler.concat(
						"SELECT containerKey, containerType, groupId, ",
						"layoutClassedModelUsageId, plid FROM ",
						"LayoutClassedModelUsage WHERE (containerType = ? AND ",
						"(plid <> (SELECT plid FROM FragmentEntryLink WHERE ",
						"fragmentEntryLinkId = CAST_LONG(containerKey)) OR ",
						"((SELECT count(*) FROM FragmentEntryLink WHERE ",
						"fragmentEntryLinkId = CAST_LONG(containerKey)) = 0)) ",
						") OR (containerType = ? AND (plid <> (SELECT plid ",
						"FROM LayoutPageTemplateStructure WHERE ",
						"layoutPageTemplateStructureId = CAST_LONG( ",
						"containerKey)) OR ((SELECT count(*) FROM ",
						"LayoutPageTemplateStructure WHERE ",
						"layoutPageTemplateStructureId = CAST_LONG( ",
						"containerKey)) = 0)))")))) {

			preparedStatement.setLong(1, containerTypeFragmentEntryLink);
			preparedStatement.setLong(
				2, containerTypeLayoutPageTemplateStructure);

			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				long containerType = resultSet.getLong("containerType");
				long groupId = resultSet.getLong("groupId");
				long layoutClassedModelUsageId = resultSet.getLong(
					"layoutClassedModelUsageId");
				long plid = resultSet.getLong("plid");

				try {
					if (containerType == containerTypeFragmentEntryLink) {
						Set<Long> plids = fragmentEntryLinkPlid.computeIfAbsent(
							groupId, key -> new HashSet<>());

						plids.add(plid);
					}
					else {
						Set<Long> plids =
							layoutPageTemplateStructurePlid.computeIfAbsent(
								groupId, key -> new HashSet<>());

						plids.add(plid);
					}

					_layoutClassedModelUsageLocalService.
						deleteLayoutClassedModelUsage(
							layoutClassedModelUsageId);
				}
				catch (Exception exception) {
					if (_log.isWarnEnabled()) {
						_log.warn(
							StringBundler.concat(
								"Unable to delete orphaned layout classed ",
								"model usage with ID ",
								layoutClassedModelUsageId),
							exception);
					}
				}
			}
		}

		_processClassedModelUsage(
			fragmentEntryLinkPlid,
			this::_updateFragmentEntryLayoutClassedModelUsage);

		_processClassedModelUsage(
			layoutPageTemplateStructurePlid,
			this::_updateLayoutPageTemplateStructureClassedModelUsage);
	}

	private void _processClassedModelUsage(
		Map<Long, Set<Long>> plidMap, BiConsumer<Long, Long> action) {

		for (Map.Entry<Long, Set<Long>> entry : plidMap.entrySet()) {
			long groupId = entry.getKey();
			Set<Long> plids = entry.getValue();

			for (long plid : plids) {
				action.accept(groupId, plid);
			}
		}
	}

	private void _updateFragmentEntryLayoutClassedModelUsage(
		long groupId, long plid) {

		List<FragmentEntryLink> fragmentEntryLinks =
			_fragmentEntryLinkLocalService.getFragmentEntryLinksByPlid(
				groupId, plid);

		for (FragmentEntryLink fragmentEntryLink : fragmentEntryLinks) {
			try {
				if (fragmentEntryLink == null) {
					continue;
				}

				_contentManager.updateLayoutClassedModelUsage(
					fragmentEntryLink);
			}
			catch (Exception exception) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						StringBundler.concat(
							"Unable to update usages for fragment entry link ",
							"ID ", fragmentEntryLink.getFragmentEntryId()),
						exception);
				}
			}
		}
	}

	private void _updateLayoutPageTemplateStructureClassedModelUsage(
		long groupId, long plid) {

		LayoutPageTemplateStructure layoutPageTemplateStructure =
			_layoutPageTemplateStructureLocalService.
				fetchLayoutPageTemplateStructure(groupId, plid);

		if (layoutPageTemplateStructure == null) {
			return;
		}

		List<LayoutPageTemplateStructureRel> layoutPageTemplateStructureRels =
			_layoutPageTemplateStructureRelLocalService.
				getLayoutPageTemplateStructureRels(
					layoutPageTemplateStructure.
						getLayoutPageTemplateStructureId());

		_layoutClassedModelUsageLocalService.deleteLayoutClassedModelUsages(
			String.valueOf(
				layoutPageTemplateStructure.getLayoutPageTemplateStructureId()),
			_classNameLocalService.getClassNameId(
				LayoutPageTemplateStructure.class.getName()),
			layoutPageTemplateStructure.getPlid());

		for (LayoutPageTemplateStructureRel layoutPageTemplateStructureRel :
				layoutPageTemplateStructureRels) {

			if (layoutPageTemplateStructureRel == null) {
				continue;
			}

			_contentManager.updateLayoutClassedModelUsage(
				layoutPageTemplateStructureRel);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CleanUpdateLayoutClassedModelUsagesVerifyProcess.class);

	@Reference
	private ClassNameLocalService _classNameLocalService;

	@Reference
	private ContentManager _contentManager;

	@Reference
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@Reference
	private LayoutClassedModelUsageLocalService
		_layoutClassedModelUsageLocalService;

	@Reference
	private LayoutPageTemplateStructureLocalService
		_layoutPageTemplateStructureLocalService;

	@Reference
	private LayoutPageTemplateStructureRelLocalService
		_layoutPageTemplateStructureRelLocalService;

	@Reference(
		target = "(&(release.bundle.symbolic.name=com.liferay.layout.service)(release.schema.version>=2.0.0))"
	)
	private Release _release;

}