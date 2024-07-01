/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.test.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.cache.MultiVMPool;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.ExternalReferenceCodeModel;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Rubén Pulido
 */
public abstract class BaseUpgradeExternalReferenceCodeTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		_db = DBManagerUtil.getDB();
	}

	@Before
	public void setUp() throws Exception {
		group = GroupTestUtil.addGroup();

		serviceContext = ServiceContextTestUtil.getServiceContext(
			group.getGroupId());

		ServiceContextThreadLocal.pushServiceContext(serviceContext);
	}

	@After
	public void tearDown() {
		ServiceContextThreadLocal.popServiceContext();
	}

	@Test
	public void testUpgradeProcess() throws Exception {
		for (String tableName : getTableNames()) {
			ExternalReferenceCodeModel externalReferenceCodeModel =
				addExternalReferenceCodeModel(tableName);

			_testUpgradeProcess(externalReferenceCodeModel, tableName);

			ExternalReferenceCodeModel draftExternalReferenceCodeModel =
				fetchDraftExternalReferenceCodeModel(
					externalReferenceCodeModel, tableName);

			if (draftExternalReferenceCodeModel == null) {
				continue;
			}

			_testUpgradeProcess(draftExternalReferenceCodeModel, tableName);
		}
	}

	protected abstract ExternalReferenceCodeModel addExternalReferenceCodeModel(
			String tableName)
		throws PortalException;

	protected ExternalReferenceCodeModel fetchDraftExternalReferenceCodeModel(
		ExternalReferenceCodeModel externalReferenceCodeModel,
		String tableName) {

		return null;
	}

	protected abstract ExternalReferenceCodeModel
			fetchExternalReferenceCodeModel(
				ExternalReferenceCodeModel externalReferenceCodeModel,
				String tableName)
		throws PortalException;

	protected abstract String getExternalReferenceCode(
		ExternalReferenceCodeModel externalReferenceCodeModel,
		String tableName);

	protected abstract String[] getTableNames();

	protected abstract UpgradeProcess getUpgradeProcess();

	@DeleteAfterTestRun
	protected Group group;

	protected ServiceContext serviceContext;

	private void _runUpgrade() throws Exception {
		UpgradeProcess upgradeProcess = getUpgradeProcess();

		upgradeProcess.upgrade();

		_multiVMPool.clear();
	}

	private void _setExternalReferenceCodeToBlank(
			String tableName, String externalReferenceCode)
		throws Exception {

		_db.runSQL(
			StringBundler.concat(
				"update ", tableName, " set externalReferenceCode = '' where ",
				"externalReferenceCode = '", externalReferenceCode,
				"' and groupId =", group.getGroupId()));

		_multiVMPool.clear();
	}

	private void _testUpgradeProcess(
			ExternalReferenceCodeModel externalReferenceCodeModel,
			String tableName)
		throws Exception {

		String externalReferenceCode =
			externalReferenceCodeModel.getExternalReferenceCode();

		_setExternalReferenceCodeToBlank(tableName, externalReferenceCode);

		externalReferenceCodeModel = fetchExternalReferenceCodeModel(
			externalReferenceCodeModel, tableName);

		Assert.assertEquals(
			StringPool.BLANK,
			externalReferenceCodeModel.getExternalReferenceCode());

		Assert.assertNull(
			_layoutLocalService.fetchLayoutByExternalReferenceCode(
				externalReferenceCode, group.getGroupId()));

		_runUpgrade();

		ExternalReferenceCodeModel updatedExternalReferenceCodeModel =
			fetchExternalReferenceCodeModel(
				externalReferenceCodeModel, tableName);

		Assert.assertNotEquals(
			StringPool.BLANK,
			updatedExternalReferenceCodeModel.getExternalReferenceCode());
		Assert.assertEquals(
			getExternalReferenceCode(
				updatedExternalReferenceCodeModel, tableName),
			updatedExternalReferenceCodeModel.getExternalReferenceCode());
	}

	private static DB _db;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject
	private MultiVMPool _multiVMPool;

}