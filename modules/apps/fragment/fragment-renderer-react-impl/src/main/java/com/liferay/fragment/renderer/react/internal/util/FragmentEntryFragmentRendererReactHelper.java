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

package com.liferay.fragment.renderer.react.internal.util;

import com.liferay.fragment.constants.FragmentConstants;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.frontend.js.loader.modules.extender.npm.JSPackage;
import com.liferay.frontend.js.loader.modules.extender.npm.ModuleNameUtil;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMRegistry;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMRegistryUpdate;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMResolver;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán
 */
@Component(service = FragmentEntryFragmentRendererReactHelper.class)
public class FragmentEntryFragmentRendererReactHelper {

	public void ensureInitialized() {
		if (_initialized) {
			return;
		}

		synchronized (this) {
			if (_initialized) {
				return;
			}

			JSPackage jsPackage = _npmResolver.getJSPackage();

			if (jsPackage == null) {
				if (_log.isDebugEnabled()) {
					_log.debug(
						"Unable to initialize because JS package is null");
				}

				return;
			}

			List<FragmentEntryLink> fragmentEntryLinks =
				_fragmentEntryLinkLocalService.getFragmentEntryLinks(
					FragmentConstants.TYPE_REACT, QueryUtil.ALL_POS,
					QueryUtil.ALL_POS, null);

			NPMRegistryUpdate npmRegistryUpdate = _npmRegistry.update();

			for (FragmentEntryLink fragmentEntryLink : fragmentEntryLinks) {
				npmRegistryUpdate.registerJSModule(
					jsPackage, getModuleName(fragmentEntryLink), _dependencies,
					getJs(fragmentEntryLink, jsPackage), null);
			}

			npmRegistryUpdate.finish();

			_initialized = true;
		}
	}

	public List<String> getDependencies() {
		return _dependencies;
	}

	public String getJs(
		FragmentEntryLink fragmentEntryLink, JSPackage jsPackage) {

		return StringUtil.replace(
			fragmentEntryLink.getJs(),
			new String[] {
				"'__FRAGMENT_MODULE_NAME__'", "'__REACT_PROVIDER__$react'",
				"'frontend-js-react-web$react'"
			},
			new String[] {
				com.liferay.petra.string.StringBundler.concat(
					StringPool.APOSTROPHE,
					ModuleNameUtil.getModuleResolvedId(
						jsPackage, getModuleName(fragmentEntryLink)),
					StringPool.APOSTROPHE),
				com.liferay.petra.string.StringBundler.concat(
					StringPool.APOSTROPHE, _DEPENDENCY_PORTAL_REACT,
					StringPool.APOSTROPHE),
				com.liferay.petra.string.StringBundler.concat(
					StringPool.APOSTROPHE, _DEPENDENCY_PORTAL_REACT,
					StringPool.APOSTROPHE)
			});
	}

	public String getModuleName(FragmentEntryLink fragmentEntryLink) {
		Date modifiedDate = fragmentEntryLink.getModifiedDate();

		return StringBundler.concat(
			"fragmentEntryLink/",
			String.valueOf(fragmentEntryLink.getFragmentEntryLinkId()),
			StringPool.DASH, String.valueOf(modifiedDate.getTime()));
	}

	@Deactivate
	protected void deactivate() {
		List<FragmentEntryLink> fragmentEntryLinks =
			_fragmentEntryLinkLocalService.getFragmentEntryLinks(
				FragmentConstants.TYPE_REACT, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, null);

		NPMRegistryUpdate npmRegistryUpdate = _npmRegistry.update();

		JSPackage jsPackage = _npmResolver.getJSPackage();

		for (FragmentEntryLink fragmentEntryLink : fragmentEntryLinks) {
			npmRegistryUpdate.unregisterJSModule(
				jsPackage.getJSModule(getModuleName(fragmentEntryLink)));
		}

		npmRegistryUpdate.finish();
	}

	private static final String _DEPENDENCY_PORTAL_REACT =
		"liferay!frontend-js-react-web$react";

	private static final Log _log = LogFactoryUtil.getLog(
		FragmentEntryFragmentRendererReactHelper.class);

	private static final List<String> _dependencies = Collections.singletonList(
		_DEPENDENCY_PORTAL_REACT);

	@Reference
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	private volatile boolean _initialized;

	@Reference
	private NPMRegistry _npmRegistry;

	@Reference
	private NPMResolver _npmResolver;

}