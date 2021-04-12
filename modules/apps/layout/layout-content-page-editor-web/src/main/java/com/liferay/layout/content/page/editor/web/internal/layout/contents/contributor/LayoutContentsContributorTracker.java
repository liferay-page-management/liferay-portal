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

package com.liferay.layout.content.page.editor.web.internal.layout.contents.contributor;

import com.liferay.layout.content.page.editor.web.internal.configuration.FFLayoutContentPageEditorConfiguration;
import com.liferay.layout.contents.contributor.LayoutContentsContributor;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerListFactory;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONUtil;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán
 */
@Component(
	configurationPid = "com.liferay.layout.content.page.editor.web.internal.configuration.FFLayoutContentPageEditorConfiguration",
	immediate = true, service = LayoutContentsContributorTracker.class
)
public class LayoutContentsContributorTracker {

	public JSONArray getAllLayoutContentsJSONArray(
		HttpServletRequest httpServletRequest, long plid) {

		if (!_ffLayoutContentPageEditorConfiguration.contentBrowsingEnabled()) {
			return _layoutClassedModelUsageLayoutContentsContributor.
				getLayoutContentsJSONArray(httpServletRequest, plid);
		}

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (LayoutContentsContributor layoutContentsContributor :
				_serviceTrackerList) {

			jsonArray = JSONUtil.concat(
				jsonArray,
				layoutContentsContributor.getLayoutContentsJSONArray(
					httpServletRequest, plid));
		}

		return jsonArray;
	}

	@Activate
	protected void activate(
		BundleContext bundleContext, Map<String, Object> properties) {

		_ffLayoutContentPageEditorConfiguration =
			ConfigurableUtil.createConfigurable(
				FFLayoutContentPageEditorConfiguration.class, properties);
		_serviceTrackerList = ServiceTrackerListFactory.open(
			bundleContext, LayoutContentsContributor.class);
	}

	private FFLayoutContentPageEditorConfiguration
		_ffLayoutContentPageEditorConfiguration;

	@Reference
	private LayoutClassedModelUsageLayoutContentsContributor
		_layoutClassedModelUsageLayoutContentsContributor;

	private ServiceTrackerList
		<LayoutContentsContributor, LayoutContentsContributor>
			_serviceTrackerList;

}