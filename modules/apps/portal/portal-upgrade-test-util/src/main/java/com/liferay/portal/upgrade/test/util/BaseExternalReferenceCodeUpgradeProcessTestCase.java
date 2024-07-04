/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.test.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.cache.MultiVMPool;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.ExternalReferenceCodeModel;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.StagedModel;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.InfrastructureUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.version.Version;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

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
public abstract class BaseExternalReferenceCodeUpgradeProcessTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		_dataSource = InfrastructureUtil.getDataSource();
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
			ExternalReferenceCodeModel[] externalReferenceCodeModels =
				addExternalReferenceCodeModels(tableName);

			_prepareDatabaseForUpgradeProcess(
				externalReferenceCodeModels, tableName);

			_runUpgrade();

			_assertDatabaseAfterUpgradeProcess(
				externalReferenceCodeModels, tableName);
		}
	}

	protected abstract ExternalReferenceCodeModel[]
			addExternalReferenceCodeModels(String tableName)
		throws PortalException;

	protected abstract ExternalReferenceCodeModel
			fetchExternalReferenceCodeModel(
				ExternalReferenceCodeModel externalReferenceCodeModel,
				String tableName)
		throws PortalException;

	protected String getExternalReferenceCode(
		ExternalReferenceCodeModel externalReferenceCodeModel,
		String tableName) {

		if (externalReferenceCodeModel instanceof StagedModel) {
			StagedModel stagedModel = (StagedModel)externalReferenceCodeModel;

			return stagedModel.getUuid();
		}

		return null;
	}

	protected abstract String[] getTableNames();

	protected UpgradeProcess getUpgradeProcess() {
		UpgradeProcess[] upgradeProcesses = UpgradeTestUtil.getUpgradeSteps(
			getUpgradeStepRegistrator(), getVersion());

		return upgradeProcesses[0];
	}

	protected UpgradeStepRegistrator getUpgradeStepRegistrator() {
		return null;
	}

	protected Version getVersion() {
		return null;
	}

	@DeleteAfterTestRun
	protected Group group;

	protected ServiceContext serviceContext;

	private void _assertDatabaseAfterUpgradeProcess(
			ExternalReferenceCodeModel[] externalReferenceCodeModels,
			String tableName)
		throws Exception {

		for (ExternalReferenceCodeModel externalReferenceCodeModel :
				externalReferenceCodeModels) {

			ExternalReferenceCodeModel updatedExternalReferenceCodeModel =
				fetchExternalReferenceCodeModel(
					externalReferenceCodeModel, tableName);

			Assert.assertTrue(
				updatedExternalReferenceCodeModel.getExternalReferenceCode(),
				Validator.isNotNull(
					updatedExternalReferenceCodeModel.
						getExternalReferenceCode()));
			Assert.assertEquals(
				getExternalReferenceCode(
					updatedExternalReferenceCodeModel, tableName),
				updatedExternalReferenceCodeModel.getExternalReferenceCode());
		}
	}

	private void _assertSelectCountExternalReferenceCodeIsNullEqualsZero(
			String tableName)
		throws Exception {

		try (Connection connection = _dataSource.getConnection()) {
			try (PreparedStatement preparedStatement =
					connection.prepareStatement(
						StringBundler.concat(
							"select count(1) from ", tableName,
							" where externalReferenceCode = null and groupId ",
							"= ", group.getGroupId()));
				ResultSet resultSet = preparedStatement.executeQuery()) {

				int count = 0;

				if (resultSet.next()) {
					count = resultSet.getInt(1);
				}

				Assert.assertEquals(0, count);
			}
		}
	}

	private void _prepareDatabaseForUpgradeProcess(
			ExternalReferenceCodeModel[] externalReferenceCodeModels,
			String tableName)
		throws Exception {

		_setExternalReferenceCodeToNull(tableName, externalReferenceCodeModels);

		_assertSelectCountExternalReferenceCodeIsNullEqualsZero(tableName);
	}

	private void _runUpgrade() throws Exception {
		UpgradeProcess upgradeProcess = getUpgradeProcess();

		upgradeProcess.upgrade();

		_entityCache.clearCache();
		_multiVMPool.clear();
	}

	private void _setExternalReferenceCodeToNull(
			String tableName,
			ExternalReferenceCodeModel[] externalReferenceCodeModels)
		throws Exception {

		String[] clauses = new String[externalReferenceCodeModels.length];

		for (int i = 0; i < externalReferenceCodeModels.length; i++) {
			clauses[i] =
				"externalReferenceCode = '" +
					externalReferenceCodeModels[i].getExternalReferenceCode() +
						"'";
		}

		_db.runSQL(
			StringBundler.concat(
				"update ", tableName,
				" set externalReferenceCode = null where (",
				ArrayUtil.toString(clauses, (String)null, " or "),
				") and groupId =", group.getGroupId()));

		_entityCache.clearCache();
		_multiVMPool.clear();
	}

	private static DataSource _dataSource;
	private static DB _db;

	@Inject
	private EntityCache _entityCache;

	@Inject
	private MultiVMPool _multiVMPool;

}