/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.internal.upgrade.v5_7_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.page.template.constants.LayoutPageTemplateEntryTypeConstants;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.ExternalReferenceCodeModel;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.BaseUpgradeExternalReferenceCodeTestCase;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;

import org.junit.runner.RunWith;

/**
 * @author Rubén Pulido
 */
@RunWith(Arquillian.class)
public class LayoutPageTemplateEntryUpgradeExternalReferenceCodeTest
	extends BaseUpgradeExternalReferenceCodeTestCase {

	@Override
	protected ExternalReferenceCodeModel addExternalReferenceCodeModel(
			String tableName)
		throws PortalException {

		return _layoutPageTemplateEntryLocalService.addLayoutPageTemplateEntry(
			null, TestPropsValues.getUserId(), group.getGroupId(), 0,
			RandomTestUtil.randomString(),
			LayoutPageTemplateEntryTypeConstants.MASTER_LAYOUT, 0,
			WorkflowConstants.STATUS_APPROVED,
			ServiceContextTestUtil.getServiceContext(group.getGroupId()));
	}

	@Override
	protected ExternalReferenceCodeModel fetchExternalReferenceCodeModel(
		ExternalReferenceCodeModel externalReferenceCodeModel,
		String tableName) {

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			(LayoutPageTemplateEntry)externalReferenceCodeModel;

		return _layoutPageTemplateEntryLocalService.
			fetchLayoutPageTemplateEntry(
				layoutPageTemplateEntry.getLayoutPageTemplateEntryId());
	}

	@Override
	protected String getExternalReferenceCode(
		ExternalReferenceCodeModel externalReferenceCodeModel,
		String tableName) {

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			(LayoutPageTemplateEntry)externalReferenceCodeModel;

		return layoutPageTemplateEntry.getUuid();
	}

	@Override
	protected String[] getTableNames() {
		return new String[] {"LayoutPageTemplateEntry"};
	}

	@Override
	protected UpgradeProcess getUpgradeProcess() {
		return UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator, _CLASS_NAME);
	}

	private static final String _CLASS_NAME =
		"com.liferay.layout.page.template.internal.upgrade.v5_7_0." +
			"LayoutPageTemplateEntryExternalReferenceCodeUpgradeProcess";

	@Inject(
		filter = "(&(component.name=com.liferay.layout.page.template.internal.upgrade.registry.LayoutPageTemplateServiceUpgradeStepRegistrator))"
	)
	private static UpgradeStepRegistrator _upgradeStepRegistrator;

	@Inject
	private LayoutPageTemplateEntryLocalService
		_layoutPageTemplateEntryLocalService;

}