/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.internal.upgrade.v5_6_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.page.template.constants.LayoutPageTemplateCollectionTypeConstants;
import com.liferay.layout.page.template.constants.LayoutPageTemplateConstants;
import com.liferay.layout.page.template.model.LayoutPageTemplateCollection;
import com.liferay.layout.page.template.service.LayoutPageTemplateCollectionLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.ExternalReferenceCodeModel;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.BaseExternalReferenceCodeUpgradeProcessTestCase;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;

import org.junit.runner.RunWith;

/**
 * @author Rubén Pulido
 */
@RunWith(Arquillian.class)
public class LayoutPageTemplateCollectionExternalReferenceCodeUpgradeProcessTest
	extends BaseExternalReferenceCodeUpgradeProcessTestCase {

	@Override
	protected ExternalReferenceCodeModel addExternalReferenceCodeModel(
			String tableName)
		throws PortalException {

		return _layoutPageTemplateCollectionLocalService.
			addLayoutPageTemplateCollection(
				null, TestPropsValues.getUserId(), group.getGroupId(),
				LayoutPageTemplateConstants.
					PARENT_LAYOUT_PAGE_TEMPLATE_COLLECTION_ID_DEFAULT,
				RandomTestUtil.randomString(), StringPool.BLANK,
				LayoutPageTemplateCollectionTypeConstants.BASIC,
				serviceContext);
	}

	@Override
	protected ExternalReferenceCodeModel fetchExternalReferenceCodeModel(
		ExternalReferenceCodeModel externalReferenceCodeModel,
		String tableName) {

		LayoutPageTemplateCollection layoutPageTemplateCollection =
			(LayoutPageTemplateCollection)externalReferenceCodeModel;

		return _layoutPageTemplateCollectionLocalService.
			fetchLayoutPageTemplateCollection(
				layoutPageTemplateCollection.
					getLayoutPageTemplateCollectionId());
	}

	@Override
	protected String getExternalReferenceCode(
		ExternalReferenceCodeModel externalReferenceCodeModel,
		String tableName) {

		LayoutPageTemplateCollection layoutPageTemplateCollection =
			(LayoutPageTemplateCollection)externalReferenceCodeModel;

		return layoutPageTemplateCollection.getUuid();
	}

	@Override
	protected String[] getTableNames() {
		return new String[] {"LayoutPageTemplateCollection"};
	}

	@Override
	protected UpgradeProcess getUpgradeProcess() {
		return UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator, _CLASS_NAME);
	}

	private static final String _CLASS_NAME =
		"com.liferay.layout.page.template.internal.upgrade.v5_6_0." +
			"LayoutPageTemplateCollectionExternalReferenceCodeUpgradeProcess";

	@Inject(
		filter = "(&(component.name=com.liferay.layout.page.template.internal.upgrade.registry.LayoutPageTemplateServiceUpgradeStepRegistrator))"
	)
	private static UpgradeStepRegistrator _upgradeStepRegistrator;

	@Inject
	private LayoutPageTemplateCollectionLocalService
		_layoutPageTemplateCollectionLocalService;

}