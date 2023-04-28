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

package com.liferay.layout.admin.web.internal.portlet.action;

import com.liferay.layout.admin.constants.LayoutAdminPortletKeys;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.service.impl.LayoutLocalServiceHelper;
import com.liferay.sites.kernel.util.Sites;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Vendel Toreki
 */
@Component(
	property = {
		"javax.portlet.name=" + LayoutAdminPortletKeys.GROUP_PAGES,
		"mvc.command.name=/layout_admin/get_layout_set_prototype_conflicts"
	},
	service = MVCResourceCommand.class
)
public class GetLayoutSetPrototypeConflictsMVCResourceCommand
	extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		long plid = ParamUtil.getLong(
			resourceRequest, "plid", LayoutConstants.DEFAULT_PLID);

		List<Layout> layouts = new ArrayList<>();

		if (plid != LayoutConstants.DEFAULT_PLID) {
			String friendlyURL = ParamUtil.getString(
				resourceRequest, "friendlyURL", "");

			layouts = _getConflictsOfExistingLayout(plid, friendlyURL);
		}
		else {
			String name = ParamUtil.getString(resourceRequest, "name", "");

			long groupId = ParamUtil.getLong(resourceRequest, "groupId");

			boolean privateLayout = ParamUtil.getBoolean(
				resourceRequest, "privateLayout");

			String friendlyURL =
				StringPool.SLASH +
					_layoutLocalServiceHelper.getFriendlyURL(name);

			layouts = _getConflictsOfNewLayout(
				groupId, privateLayout, friendlyURL);
		}

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse,
			JSONUtil.put("conflictsCount", layouts.size()));
	}

	private List<Layout> _getConflictsOfExistingLayout(
			long plid, String friendlyURL)
		throws Exception {

		if ((plid == LayoutConstants.DEFAULT_PLID) ||
			Validator.isNull(friendlyURL)) {

			return new ArrayList<>();
		}

		Layout layout = _layoutLocalService.fetchLayout(plid);

		if (layout == null) {
			return new ArrayList<>();
		}

		Group group = layout.getGroup();

		if (group.isLayoutSetPrototype()) {
			return _sites.getLayoutSetPrototypeFriendlyURLConflictSitesLayouts(
				layout, friendlyURL);
		}

		List<Layout> conflictLayouts = new ArrayList<>();
		Layout conflictLayout =
			_sites.getLayoutSetPrototypeFriendlyURLConflictPrototypeLayout(
				layout, friendlyURL);

		if (conflictLayout != null) {
			conflictLayouts.add(conflictLayout);
		}

		return conflictLayouts;
	}

	private List<Layout> _getConflictsOfNewLayout(
			long groupId, boolean privateLayout, String friendlyURL)
		throws Exception {

		if ((groupId == 0) || Validator.isNull(friendlyURL)) {
			return new ArrayList<>();
		}

		Group group = _groupLocalService.getGroup(groupId);

		if (group.isLayoutSetPrototype()) {
			return _sites.getLayoutSetPrototypeFriendlyURLConflictSitesLayouts(
				groupId, null, friendlyURL);
		}

		List<Layout> conflictLayouts = new ArrayList<>();

		Layout conflictLayout =
			_sites.getLayoutSetPrototypeFriendlyURLConflictPrototypeLayout(
				groupId, privateLayout, null, friendlyURL);

		if (conflictLayout != null) {
			conflictLayouts.add(conflictLayout);
		}

		return conflictLayouts;
	}

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private LayoutLocalServiceHelper _layoutLocalServiceHelper;

	@Reference
	private Sites _sites;

}