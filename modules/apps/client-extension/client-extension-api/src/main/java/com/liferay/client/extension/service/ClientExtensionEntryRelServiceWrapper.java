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

package com.liferay.client.extension.service;

import com.liferay.portal.kernel.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link ClientExtensionEntryRelService}.
 *
 * @author Brian Wing Shun Chan
 * @see ClientExtensionEntryRelService
 * @generated
 */
public class ClientExtensionEntryRelServiceWrapper
	implements ClientExtensionEntryRelService,
			   ServiceWrapper<ClientExtensionEntryRelService> {

	public ClientExtensionEntryRelServiceWrapper() {
		this(null);
	}

	public ClientExtensionEntryRelServiceWrapper(
		ClientExtensionEntryRelService clientExtensionEntryRelService) {

		_clientExtensionEntryRelService = clientExtensionEntryRelService;
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _clientExtensionEntryRelService.getOSGiServiceIdentifier();
	}

	@Override
	public ClientExtensionEntryRelService getWrappedService() {
		return _clientExtensionEntryRelService;
	}

	@Override
	public void setWrappedService(
		ClientExtensionEntryRelService clientExtensionEntryRelService) {

		_clientExtensionEntryRelService = clientExtensionEntryRelService;
	}

	private ClientExtensionEntryRelService _clientExtensionEntryRelService;

}