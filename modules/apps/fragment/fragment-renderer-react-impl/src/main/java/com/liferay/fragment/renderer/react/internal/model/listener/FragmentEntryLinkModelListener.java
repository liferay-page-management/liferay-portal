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
import com.liferay.fragment.renderer.react.internal.helper.FragmentEntryLinkJSModuleHelper;
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

		_fragmentEntryLinkJSModuleHelper.registerJSModule(fragmentEntryLink);
	}

	@Override
	public void onAfterRemove(FragmentEntryLink fragmentEntryLink) {
		if (!fragmentEntryLink.isTypeReact()) {
			return;
		}

		_fragmentEntryLinkJSModuleHelper.ensureInitialized();

		_fragmentEntryLinkJSModuleHelper.unregisterJSModule(fragmentEntryLink);
	}

	@Override
	public void onAfterUpdate(
		FragmentEntryLink originalFragmentEntryLink,
		FragmentEntryLink fragmentEntryLink) {

		if (!fragmentEntryLink.isTypeReact()) {
			return;
		}

		_fragmentEntryLinkJSModuleHelper.ensureInitialized();

		_fragmentEntryLinkJSModuleHelper.updateJSModule(
			fragmentEntryLink, originalFragmentEntryLink);
	}

	@Reference
	private FragmentEntryLinkJSModuleHelper _fragmentEntryLinkJSModuleHelper;

}