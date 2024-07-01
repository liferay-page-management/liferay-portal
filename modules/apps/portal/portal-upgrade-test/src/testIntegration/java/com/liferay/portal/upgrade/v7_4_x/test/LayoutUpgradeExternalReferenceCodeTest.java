/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.v7_4_x.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.ExternalReferenceCodeModel;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.upgrade.test.util.BaseUpgradeExternalReferenceCodeTestCase;
import com.liferay.portal.upgrade.v7_4_x.UpgradeLayoutExternalReferenceCode;

import org.junit.runner.RunWith;

/**
 * @author Rubén Pulido
 */
@RunWith(Arquillian.class)
public class LayoutUpgradeExternalReferenceCodeTest
	extends BaseUpgradeExternalReferenceCodeTestCase {

	@Override
	protected ExternalReferenceCodeModel addExternalReferenceCodeModel(
			String tableName)
		throws PortalException {

		return _layoutLocalService.addLayout(
			null, TestPropsValues.getUserId(), group.getGroupId(), true,
			LayoutConstants.DEFAULT_PARENT_LAYOUT_ID,
			RandomTestUtil.randomString(), null, RandomTestUtil.randomString(),
			LayoutConstants.TYPE_CONTENT, false, false, null, serviceContext);
	}

	protected ExternalReferenceCodeModel fetchDraftExternalReferenceCodeModel(
		ExternalReferenceCodeModel externalReferenceCodeModel,
		String tableName) {

		Layout layout = (Layout)externalReferenceCodeModel;

		return layout.fetchDraftLayout();
	}

	@Override
	protected ExternalReferenceCodeModel fetchExternalReferenceCodeModel(
		ExternalReferenceCodeModel externalReferenceCodeModel,
		String tableName) {

		Layout layout = (Layout)externalReferenceCodeModel;

		return _layoutLocalService.fetchLayout(layout.getPlid());
	}

	@Override
	protected String getExternalReferenceCode(
		ExternalReferenceCodeModel externalReferenceCodeModel,
		String tableName) {

		Layout layout = (Layout)externalReferenceCodeModel;

		return layout.getUuid();
	}

	@Override
	protected String[] getTableNames() {
		return new String[] {"Layout"};
	}

	protected UpgradeLayoutExternalReferenceCode getUpgradeProcess() {
		return new UpgradeLayoutExternalReferenceCode();
	}

	@Inject
	private LayoutLocalService _layoutLocalService;

}