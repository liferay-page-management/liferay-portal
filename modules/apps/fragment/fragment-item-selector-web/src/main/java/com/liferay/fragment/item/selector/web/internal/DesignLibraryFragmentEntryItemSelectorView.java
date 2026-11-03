/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.item.selector.web.internal;

import com.liferay.design.library.util.DesignLibraryUtil;
import com.liferay.fragment.item.selector.FragmentEntryItemSelectorCriterion;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;

import jakarta.servlet.ServletContext;

import java.util.Locale;

/**
 * @author Lourdes Fernández Besada
 */
public class DesignLibraryFragmentEntryItemSelectorView
	extends BaseFragmentEntryItemSelectorView {

	public DesignLibraryFragmentEntryItemSelectorView(
		long groupId, GroupLocalService groupLocalService, Portal portal,
		ServletContext servletContext) {

		super(portal, servletContext);

		_groupId = groupId;
		_groupLocalService = groupLocalService;
	}

	@Override
	public String getTitle(Locale locale) {
		Group group = _groupLocalService.fetchGroup(_groupId);

		try {
			return group.getDescriptiveName(locale);
		}
		catch (PortalException portalException) {
			if (_log.isWarnEnabled()) {
				_log.warn(portalException);
			}
		}

		return group.getName();
	}

	@Override
	public boolean isVisible(
		FragmentEntryItemSelectorCriterion fragmentEntryItemSelectorCriterion,
		ThemeDisplay themeDisplay) {

		return DesignLibraryUtil.isConnectedDesignLibraryGroupId(
			_groupId, themeDisplay.getScopeGroupId());
	}

	@Override
	protected Group getGroup(ThemeDisplay themeDisplay) {
		return _groupLocalService.fetchGroup(_groupId);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DesignLibraryFragmentEntryItemSelectorView.class);

	private final long _groupId;
	private final GroupLocalService _groupLocalService;

}