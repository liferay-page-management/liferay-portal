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

package com.liferay.journal.internal.upgrade.v4_3_1;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author István András Dézsi
 */
public class GeolocationDDMFieldAttributeNameUpgradeProcess
	extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		try (PreparedStatement selectPreparedStatement =
				connection.prepareStatement(
					StringBundler.concat(
						"select fieldAttributeId, attributeName from ",
						"DDMFieldAttribute where attributeName in ('",
						_LATITUDE, "', '", _LONGITUDE, "')"));
			PreparedStatement updatePreparedStatement =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection,
					"update DDMFieldAttribute set attributeName = ? where " +
						"fieldAttributeId = ?");
			ResultSet resultSet = selectPreparedStatement.executeQuery()) {

			while (resultSet.next()) {
				String attributeName = resultSet.getString("attributeName");

				if (StringUtil.equals(attributeName, _LATITUDE)) {
					updatePreparedStatement.setString(1, _LAT);
				}
				else if (StringUtil.equals(attributeName, _LONGITUDE)) {
					updatePreparedStatement.setString(1, _LNG);
				}

				long fieldAttributeId = resultSet.getLong("fieldAttributeId");

				updatePreparedStatement.setLong(2, fieldAttributeId);

				updatePreparedStatement.addBatch();
			}

			updatePreparedStatement.executeBatch();
		}
	}

	private static final String _LAT = "lat";

	private static final String _LATITUDE = "latitude";

	private static final String _LNG = "lng";

	private static final String _LONGITUDE = "longitude";

}