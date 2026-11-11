/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.admin.web.internal.util;

import com.liferay.design.library.util.DesignLibraryUtil;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;

import java.util.Locale;

/**
 * @author Georgel Pop
 */
public class MasterLayoutUtil {

	public static String getMasterLayoutPageTemplateEntryScopeERC(
		long groupId, Layout layout) {

		LayoutPageTemplateEntry masterLayoutPageTemplateEntry =
			_fetchMasterLayoutPageTemplateEntry(layout);

		if (masterLayoutPageTemplateEntry == null) {
			return StringPool.BLANK;
		}

		return getMasterLayoutPageTemplateEntryScopeERC(
			groupId, masterLayoutPageTemplateEntry);
	}

	public static String getMasterLayoutPageTemplateEntryScopeERC(
		long groupId, LayoutPageTemplateEntry masterLayoutPageTemplateEntry) {

		Group designLibraryGroup = _fetchDesignLibraryGroup(
			groupId, masterLayoutPageTemplateEntry);

		if (designLibraryGroup == null) {
			return StringPool.BLANK;
		}

		return designLibraryGroup.getExternalReferenceCode();
	}

	public static String getMasterLayoutPageTemplateEntryScopeLabel(
		long groupId, Layout layout, Locale locale) {

		LayoutPageTemplateEntry masterLayoutPageTemplateEntry =
			_fetchMasterLayoutPageTemplateEntry(layout);

		if (masterLayoutPageTemplateEntry == null) {
			return StringPool.BLANK;
		}

		return getMasterLayoutPageTemplateEntryScopeLabel(
			groupId, locale, masterLayoutPageTemplateEntry);
	}

	public static String getMasterLayoutPageTemplateEntryScopeLabel(
		long groupId, Locale locale,
		LayoutPageTemplateEntry masterLayoutPageTemplateEntry) {

		Group designLibraryGroup = _fetchDesignLibraryGroup(
			groupId, masterLayoutPageTemplateEntry);

		if (designLibraryGroup == null) {
			return StringPool.BLANK;
		}

		try {
			return designLibraryGroup.getDescriptiveName(locale);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}

			return StringPool.BLANK;
		}
	}

	private static Group _fetchDesignLibraryGroup(
		long groupId, LayoutPageTemplateEntry masterLayoutPageTemplateEntry) {

		if ((masterLayoutPageTemplateEntry.getGroupId() == 0) ||
			(masterLayoutPageTemplateEntry.getGroupId() == groupId)) {

			return null;
		}

		Group group = GroupLocalServiceUtil.fetchGroup(
			masterLayoutPageTemplateEntry.getGroupId());

		if (!DesignLibraryUtil.isDesignLibraryScope(group)) {
			return null;
		}

		return group;
	}

	private static LayoutPageTemplateEntry _fetchMasterLayoutPageTemplateEntry(
		Layout layout) {

		long masterLayoutPlid = layout.getMasterLayoutPlid();

		if (masterLayoutPlid <= 0) {
			return null;
		}

		return LayoutPageTemplateEntryLocalServiceUtil.
			fetchLayoutPageTemplateEntryByPlid(masterLayoutPlid);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MasterLayoutUtil.class);

}