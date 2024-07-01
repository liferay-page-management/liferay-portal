/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.navigation.internal.upgrade.v2_5_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.ExternalReferenceCodeModel;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.BaseUpgradeExternalReferenceCodeTestCase;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;
import com.liferay.site.navigation.model.SiteNavigationMenu;
import com.liferay.site.navigation.service.SiteNavigationMenuService;
import com.liferay.site.navigation.test.util.SiteNavigationMenuTestUtil;

import org.junit.runner.RunWith;

/**
 * @author Rubén Pulido
 */
@RunWith(Arquillian.class)
public class SiteNavigationMenuUpgradeExternalReferenceCodeTest
	extends BaseUpgradeExternalReferenceCodeTestCase {

	@Override
	protected ExternalReferenceCodeModel addExternalReferenceCodeModel(
			String tableName)
		throws PortalException {

		return SiteNavigationMenuTestUtil.addSiteNavigationMenu(group);
	}

	@Override
	protected ExternalReferenceCodeModel fetchExternalReferenceCodeModel(
			ExternalReferenceCodeModel externalReferenceCodeModel,
			String tableName)
		throws PortalException {

		SiteNavigationMenu siteNavigationMenu =
			(SiteNavigationMenu)externalReferenceCodeModel;

		return _siteNavigationMenuService.fetchSiteNavigationMenu(
			siteNavigationMenu.getSiteNavigationMenuId());
	}

	@Override
	protected String getExternalReferenceCode(
		ExternalReferenceCodeModel externalReferenceCodeModel,
		String tableName) {

		SiteNavigationMenu siteNavigationMenu =
			(SiteNavigationMenu)externalReferenceCodeModel;

		return siteNavigationMenu.getUuid();
	}

	@Override
	protected String[] getTableNames() {
		return new String[] {"SiteNavigationMenu"};
	}

	@Override
	protected UpgradeProcess getUpgradeProcess() {
		return UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator, _CLASS_NAME);
	}

	private static final String _CLASS_NAME =
		"com.liferay.site.navigation.internal.upgrade.v2_5_0." +
			"SiteNavigationMenuExternalReferenceCodeUpgradeProcess";

	@Inject(
		filter = "(&(component.name=com.liferay.site.navigation.internal.upgrade.registry.SiteNavigationServiceUpgradeStepRegistrator))"
	)
	private static UpgradeStepRegistrator _upgradeStepRegistrator;

	@Inject
	private SiteNavigationMenuService _siteNavigationMenuService;

}