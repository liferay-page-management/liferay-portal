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

package com.liferay.layout.page.template.internal.upgrade.v3_5_1;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutModel;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Márk Gulácsy
 */
public class LayoutPageTemplateStructureUpgradeProcess extends UpgradeProcess {

	public LayoutPageTemplateStructureUpgradeProcess(
		LayoutLocalService layoutLocalService) {

		_layoutLocalService = layoutLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {

		// Delete orphan LayoutPageTemplateStructure-s

		DynamicQuery layoutsQuery = _layoutLocalService.dynamicQuery();

		List<Layout> layouts = _layoutLocalService.dynamicQuery(layoutsQuery);

		Stream<Layout> layoutsStream = layouts.stream();

		long[] plids = layoutsStream.mapToLong(
			LayoutModel::getPlid
		).toArray();

		String markers = StringUtils.repeat(",?", plids.length);

		markers = markers.substring(1);

		String sql =
			"delete from LayoutPageTemplateStructure where classPK not in (" +
				markers + ")";

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				sql)) {

			for (int i = 0; i < plids.length; i++) {
				preparedStatement.setString(i + 1, String.valueOf(plids[i]));
			}

			boolean result = preparedStatement.execute();

			if (!result) {
				if (_log.isDebugEnabled()) {
					_log.debug(
						String.format(
							"%d orphan LayoutPageTemplateStructures have been" +
								"deleted!",
							preparedStatement.getUpdateCount()));
				}
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception, exception);
			}
		}
		//Get LayoutPageTemplateStructures of widget layouts
		DynamicQuery widgetLayoutsQuery = _layoutLocalService.dynamicQuery();

		widgetLayoutsQuery.add(
			RestrictionsFactoryUtil.eq("type", LayoutConstants.TYPE_PORTLET));

		List<Layout> widgetLayouts = _layoutLocalService.dynamicQuery(
			widgetLayoutsQuery);

		Stream<Layout> widgetLayoutsStream = widgetLayouts.stream();

		long[] widgetPlids = widgetLayoutsStream.mapToLong(
			LayoutModel::getPlid
		).toArray();

		markers = StringUtils.repeat(",?", widgetPlids.length);
		markers = markers.substring(1);

		sql = StringBundler.concat(
			"select classPK from LayoutPageTemplateStructure where classPK in ",
			"(", markers, ")");

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				sql)) {

			for (int i = 0; i < widgetPlids.length; i++) {
				preparedStatement.setString(
					i + 1, String.valueOf(widgetPlids[i]));
			}

			ResultSet widgetLayoutTemplateStructuresResultSet =
				preparedStatement.executeQuery();
			ArrayList<Long> widgetLayoutsWithStructurePlids = new ArrayList<>();

			while (widgetLayoutTemplateStructuresResultSet.next()) {
				widgetLayoutsWithStructurePlids.add(
					widgetLayoutTemplateStructuresResultSet.getLong("classPK"));
			}
			//Get draft widget layouts with structures
			DynamicQuery draftWidgetLayoutsWithStructureQuery =
				_layoutLocalService.dynamicQuery();

			draftWidgetLayoutsWithStructureQuery.add(
				RestrictionsFactoryUtil.and(
					RestrictionsFactoryUtil.in(
						"plid", widgetLayoutsWithStructurePlids),
					RestrictionsFactoryUtil.eq(
						"status", WorkflowConstants.STATUS_DRAFT)));

			List<Layout> draftWidgetLayoutsWithStructure =
				_layoutLocalService.dynamicQuery(
					draftWidgetLayoutsWithStructureQuery);

			ServiceContext serviceContext = new ServiceContext();

			//Update page statuses

			for (Layout layout : draftWidgetLayoutsWithStructure) {
				_layoutLocalService.updateStatus(
					layout.getUserId(), layout.getPlid(),
					WorkflowConstants.STATUS_APPROVED, serviceContext);
			}
			//Delete Structures

			try (PreparedStatement preparedStatement1 =
					AutoBatchPreparedStatementUtil.autoBatch(
						connection.prepareStatement(
							"delete from LayoutPageTemplateStructure where " +
								"classPK = ?"))) {

				for (long plid : widgetLayoutsWithStructurePlids) {
					preparedStatement1.setLong(1, plid);
					preparedStatement1.addBatch();
				}

				preparedStatement1.executeBatch();
			}
			catch (Exception exception) {
				if (_log.isDebugEnabled()) {
					_log.debug(exception, exception);
				}
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception, exception);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LayoutPageTemplateStructureUpgradeProcess.class);

	private final LayoutLocalService _layoutLocalService;

}