/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.asset.list.internal.upgrade.v1_4_0;

import com.liferay.asset.list.constants.AssetListEntryUsageConstants;
import com.liferay.asset.list.internal.upgrade.v1_4_0.util.AssetListEntryUsageTable;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.LoggingTimer;
import com.liferay.portal.kernel.util.PortalUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Víctor Galán
 */
public class AssetListEntryUsageUpgradeProcess extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		upgradeSchema();
	}

	protected void upgradeSchema() throws Exception {
		alter(
			AssetListEntryUsageTable.class,
			new AlterTableAddColumn("containerKey", "VARCHAR(255) null"),
			new AlterTableAddColumn("containerType", "LONG"),
			new AlterTableAddColumn("key_", "VARCHAR(255) null"),
			new AlterTableAddColumn("plid", "LONG"),
			new AlterTableAddColumn("type_", "INTEGER"));

		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			try (PreparedStatement ps1 = connection.prepareStatement(
					"select assetListEntryId, assetListEntryUsageId, classPK," +
						"portletId from AssetListEntryUsage");
				PreparedStatement ps2 =
					AutoBatchPreparedStatementUtil.autoBatch(
						connection.prepareStatement(
							StringBundler.concat(
								"update AssetListEntryUsage set classNameId = ",
								"?, containerKey = ?, containerType = ?, key_ ",
								"= ?, plid = ?, type_ = ? where ",
								"assetListEntryUsageId = ?")));
				ResultSet rs = ps1.executeQuery()) {

				while (rs.next()) {
					ps2.setLong(
						1, PortalUtil.getClassNameId(AssetListEntry.class));
					ps2.setString(2, String.valueOf(rs.getString(4)));
					ps2.setLong(3, PortalUtil.getClassNameId(Portlet.class));
					ps2.setString(4, String.valueOf(rs.getLong(1)));
					ps2.setLong(5, rs.getLong(3));
					ps2.setLong(6, AssetListEntryUsageConstants.TYPE_LAYOUT);
					ps2.setLong(7, rs.getLong(2));

					ps2.addBatch();
				}

				ps2.executeBatch();
			}
		}
	}

}