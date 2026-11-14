/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.util;

import com.liferay.design.library.util.DesignLibraryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.util.ArrayUtil;

/**
 * @author Lourdes Fernández Besada
 */
public class GroupUtil {

	public static long[] getSiteAndConnectedDesignLibraryGroupIds(
		long scopeGroupId) {

		return ArrayUtil.append(
			new long[] {scopeGroupId},
			DesignLibraryUtil.fetchConnectedDesignLibraryGroupIds(
				scopeGroupId));
	}

	public static long[] getSiteAndConnectedDesignLibraryGroupIds(
			long companyId, boolean includeCompanyGroup, long scopeGroupId)
		throws PortalException {

		if (!includeCompanyGroup) {
			return getSiteAndConnectedDesignLibraryGroupIds(scopeGroupId);
		}

		Group companyGroup = GroupLocalServiceUtil.getCompanyGroup(companyId);

		return ArrayUtil.append(
			getSiteAndConnectedDesignLibraryGroupIds(scopeGroupId),
			new long[] {companyGroup.getGroupId()});
	}

}