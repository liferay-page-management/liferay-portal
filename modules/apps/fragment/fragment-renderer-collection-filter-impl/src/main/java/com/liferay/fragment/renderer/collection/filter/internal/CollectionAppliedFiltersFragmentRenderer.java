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

package com.liferay.fragment.renderer.collection.filter.internal;

import com.liferay.fragment.renderer.FragmentRenderer;
import com.liferay.fragment.renderer.FragmentRendererContext;
import com.liferay.fragment.renderer.collection.filter.internal.configuration.FFFragmentRendererCollectionFilterConfiguration;
import com.liferay.fragment.renderer.collection.filter.internal.constants.CollectionAppliedFiltersFragmentRendererWebKeys;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ResourceBundleUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Pablo Molina
 */
@Component(
	configurationPid = "com.liferay.fragment.renderer.collection.filter.internal.configuration.FFFragmentRendererCollectionFilterConfiguration",
	service = FragmentRenderer.class
)
public class CollectionAppliedFiltersFragmentRenderer
	implements FragmentRenderer {

	@Override
	public String getCollectionKey() {
		return "content-display";
	}

	@Override
	public String getIcon() {
		return "filter";
	}

	@Override
	public String getLabel(Locale locale) {
		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			"content.Language", getClass());

		return LanguageUtil.get(resourceBundle, "applied-filters");
	}

	@Override
	public boolean isSelectable(HttpServletRequest httpServletRequest) {
		if (!_ffFragmentRendererCollectionFilterConfiguration.enabled()) {
			return false;
		}

		return true;
	}

	@Override
	public void render(
		FragmentRendererContext fragmentRendererContext,
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) {

		httpServletRequest.setAttribute(
			CollectionAppliedFiltersFragmentRendererWebKeys.APPLIED_FILTER_LIST,
			_getAppliedFilters(httpServletRequest));

		try {
			RequestDispatcher requestDispatcher =
				_servletContext.getRequestDispatcher("/applied_filters.jsp");

			requestDispatcher.include(httpServletRequest, httpServletResponse);
		}
		catch (Exception exception) {
			_log.error("Unable to render applied filters fragment", exception);
		}
	}

	@Modified
	protected void activate(Map<String, Object> properties) {
		_ffFragmentRendererCollectionFilterConfiguration =
			ConfigurableUtil.createConfigurable(
				FFFragmentRendererCollectionFilterConfiguration.class,
				properties);
	}

	private List<Map<String, String>> _getAppliedFilters(
		HttpServletRequest httpServletRequest) {

		List<Map<String, String>> appliedFilters = new ArrayList<>();

		Map<String, String[]> parameters = httpServletRequest.getParameterMap();

		for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
			String parameterName = entry.getKey();

			if (!parameterName.startsWith(
					CollectionAppliedFiltersFragmentRendererWebKeys.
						COLLECTION_FILTER_PARAMETER_PREFIX)) {

				continue;
			}

			List<String> parameterData = StringUtil.split(
				parameterName, CharPool.UNDERLINE);

			if (parameterData.size() != 3) {
				continue;
			}

			for (String filterValue : entry.getValue()) {
				appliedFilters.add(
					HashMapBuilder.put(
						"filterFragmentEntryLinkId", parameterData.get(2)
					).put(
						"filterType", parameterData.get(1)
					).put(
						"filterValue", filterValue
					).build());
			}
		}

		return appliedFilters;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CollectionAppliedFiltersFragmentRenderer.class);

	private volatile FFFragmentRendererCollectionFilterConfiguration
		_ffFragmentRendererCollectionFilterConfiguration;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.fragment.renderer.collection.filter.impl)"
	)
	private ServletContext _servletContext;

}