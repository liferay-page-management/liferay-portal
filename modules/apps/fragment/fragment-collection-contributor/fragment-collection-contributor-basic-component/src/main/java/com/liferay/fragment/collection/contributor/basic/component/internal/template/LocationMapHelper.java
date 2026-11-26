/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.collection.contributor.basic.component.internal.template;

import com.liferay.map.util.MapProviderHelperUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.security.key.secret.SecretResolver;

import jakarta.portlet.PortletPreferences;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Sandro Chinea
 */
@Component(service = LocationMapHelper.class)
public class LocationMapHelper {

	public String getGoogleMapsAPIKey(long groupId) {
		long companyId = CompanyThreadLocal.getCompanyId();

		PortletPreferences companyPortletPreferences =
			PrefsPropsUtil.getPreferences(companyId);

		String googleMapsAPIKey = companyPortletPreferences.getValue(
			"googleMapsAPIKey", null);

		Group group = _groupLocalService.fetchGroup(groupId);

		if ((group != null) && (group.getCompanyId() == companyId) &&
			!group.isControlPanel()) {

			googleMapsAPIKey = GetterUtil.getString(
				group.getTypeSettingsProperty("googleMapsAPIKey"),
				googleMapsAPIKey);
		}

		return GetterUtil.getString(
			_secretResolver.resolve(companyId, googleMapsAPIKey));
	}

	public String getMapProviderKey(long groupId) {
		return GetterUtil.getString(
			MapProviderHelperUtil.getMapProviderKey(
				_groupLocalService, CompanyThreadLocal.getCompanyId(), groupId),
			"OpenStreetMap");
	}

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private SecretResolver _secretResolver;

}