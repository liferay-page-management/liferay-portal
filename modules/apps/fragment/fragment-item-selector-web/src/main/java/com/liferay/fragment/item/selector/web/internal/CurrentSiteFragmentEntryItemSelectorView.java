/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.item.selector.web.internal;

import com.liferay.item.selector.ItemSelectorView;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.theme.ThemeDisplay;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán
 */
@Component(
	property = "item.selector.view.order:Integer=300",
	service = ItemSelectorView.class
)
public class CurrentSiteFragmentEntryItemSelectorView
	extends BaseFragmentEntryItemSelectorView {

	@Override
	public String getTitle(Locale locale) {
		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		try {
			Group group = serviceContext.getScopeGroup();

			return group.getDescriptiveName(locale);
		}
		catch (PortalException portalException) {
			_log.error(portalException);
		}

		return _language.get(locale, "current-site");
	}

	@Override
	protected Group getGroup(ThemeDisplay themeDisplay) {
		return themeDisplay.getScopeGroup();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CurrentSiteFragmentEntryItemSelectorView.class);

	@Reference
	private Language _language;

}