/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.segments.internal.upgrade.v2_8_1;

import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.segments.constants.SegmentsExperimentConstants;
import com.liferay.segments.service.SegmentsExperimentLocalService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Marcos Martins
 */
public class SegmentsExperimentUpgradeProcess extends UpgradeProcess {

	public SegmentsExperimentUpgradeProcess(
		SegmentsExperimentLocalService segmentsExperimentLocalService) {

		_segmentsExperimentLocalService = segmentsExperimentLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select plid from SegmentsExperiment group by plid")) {

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					long plid = resultSet.getLong("plid");

					_deleteSegmentsExperiments(plid);
				}
			}
		}
	}

	private void _deleteSegmentsExperiments(long plid) throws Exception {
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select segmentsExperimentId, status from SegmentsExperiment " +
					"where plid = ? order by createDate desc")) {

			preparedStatement.setLong(1, plid);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				boolean first = true;

				while (resultSet.next()) {
					if (first) {
						first = false;

						continue;
					}

					if (resultSet.getInt("status") !=
							SegmentsExperimentConstants.STATUS_TERMINATED) {

						continue;
					}

					_segmentsExperimentLocalService.deleteSegmentsExperiment(
						resultSet.getLong("segmentsExperimentId"));
				}
			}
		}
	}

	private final SegmentsExperimentLocalService
		_segmentsExperimentLocalService;

}