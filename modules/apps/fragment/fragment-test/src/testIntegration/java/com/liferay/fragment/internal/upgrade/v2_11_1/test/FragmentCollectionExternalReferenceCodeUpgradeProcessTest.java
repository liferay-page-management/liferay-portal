/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.internal.upgrade.v2_11_1.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.service.FragmentCollectionService;
import com.liferay.fragment.test.util.FragmentTestUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.ExternalReferenceCodeModel;
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
public class FragmentCollectionExternalReferenceCodeUpgradeProcessTest
	extends BaseExternalReferenceCodeUpgradeProcessTestCase {

	@Override
	protected ExternalReferenceCodeModel addExternalReferenceCodeModel(
			String tableName)
		throws PortalException {

		return FragmentTestUtil.addFragmentCollection(group.getGroupId());
	}

	@Override
	protected ExternalReferenceCodeModel fetchExternalReferenceCodeModel(
			ExternalReferenceCodeModel externalReferenceCodeModel,
			String tableName)
		throws PortalException {

		FragmentCollection fragmentCollection =
			(FragmentCollection)externalReferenceCodeModel;

		return _fragmentCollectionService.fetchFragmentCollection(
			fragmentCollection.getFragmentCollectionId());
	}

	@Override
	protected String getExternalReferenceCode(
		ExternalReferenceCodeModel externalReferenceCodeModel,
		String tableName) {

		FragmentCollection fragmentCollection =
			(FragmentCollection)externalReferenceCodeModel;

		return fragmentCollection.getUuid();
	}

	@Override
	protected String[] getTableNames() {
		return new String[] {"FragmentCollection"};
	}

	@Override
	protected UpgradeProcess getUpgradeProcess() {
		return UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator, _CLASS_NAME);
	}

	private static final String _CLASS_NAME =
		"com.liferay.fragment.internal.upgrade.v2_11_1." +
			"FragmentCollectionExternalReferenceCodeUpgradeProcess";

	@Inject(
		filter = "(&(component.name=com.liferay.fragment.internal.upgrade.registry.FragmentServiceUpgradeStepRegistrator))"
	)
	private static UpgradeStepRegistrator _upgradeStepRegistrator;

	@Inject
	private FragmentCollectionService _fragmentCollectionService;

}