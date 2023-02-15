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

package com.liferay.fragment.renderer.react.internal.model.listener;

import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.renderer.react.internal.util.FragmentEntryFragmentRendererReactHelper;
import com.liferay.frontend.js.loader.modules.extender.npm.JSPackage;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMRegistry;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMRegistryUpdate;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMResolver;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(
	service = {FragmentEntryLinkModelListener.class, ModelListener.class}
)
public class FragmentEntryLinkModelListener
	extends BaseModelListener<FragmentEntryLink> {

	@Override
	public void onAfterCreate(FragmentEntryLink fragmentEntryLink) {
		if (!fragmentEntryLink.isTypeReact()) {
			return;
		}

		NPMRegistryUpdate npmRegistryUpdate = _npmRegistry.update();

		JSPackage jsPackage = _npmResolver.getJSPackage();

		npmRegistryUpdate.registerJSModule(
			jsPackage,
			_fragmentEntryFragmentRendererReactHelper.getModuleName(
				fragmentEntryLink),
			_fragmentEntryFragmentRendererReactHelper.getDependencies(),
			_fragmentEntryFragmentRendererReactHelper.getJs(
				fragmentEntryLink, jsPackage),
			null);

		npmRegistryUpdate.finish();
	}

	@Override
	public void onAfterRemove(FragmentEntryLink fragmentEntryLink) {
		if (!fragmentEntryLink.isTypeReact()) {
			return;
		}

		_fragmentEntryFragmentRendererReactHelper.ensureInitialized();

		NPMRegistryUpdate npmRegistryUpdate = _npmRegistry.update();

		JSPackage jsPackage = _npmResolver.getJSPackage();

		npmRegistryUpdate.unregisterJSModule(
			jsPackage.getJSModule(
				_fragmentEntryFragmentRendererReactHelper.getModuleName(
					fragmentEntryLink)));

		npmRegistryUpdate.finish();
	}

	@Override
	public void onAfterUpdate(
		FragmentEntryLink originalFragmentEntryLink,
		FragmentEntryLink fragmentEntryLink) {

		if (!fragmentEntryLink.isTypeReact()) {
			return;
		}

		_fragmentEntryFragmentRendererReactHelper.ensureInitialized();

		NPMRegistryUpdate npmRegistryUpdate = _npmRegistry.update();

		JSPackage jsPackage = _npmResolver.getJSPackage();

		npmRegistryUpdate.unregisterJSModule(
			jsPackage.getJSModule(
				_fragmentEntryFragmentRendererReactHelper.getModuleName(
					originalFragmentEntryLink)));

		npmRegistryUpdate.registerJSModule(
			jsPackage,
			_fragmentEntryFragmentRendererReactHelper.getModuleName(
				fragmentEntryLink),
			_fragmentEntryFragmentRendererReactHelper.getDependencies(),
			_fragmentEntryFragmentRendererReactHelper.getJs(
				fragmentEntryLink, jsPackage),
			null);

		npmRegistryUpdate.finish();
	}

	@Reference
	private FragmentEntryFragmentRendererReactHelper
		_fragmentEntryFragmentRendererReactHelper;

	@Reference
	private NPMRegistry _npmRegistry;

	@Reference
	private NPMResolver _npmResolver;

}