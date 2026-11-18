/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.exception;

import com.liferay.portal.kernel.exception.PortalException;

/**
 * @author Balázs Sáfrány-Kovalik
 */
public class LayoutPageTemplateEntryPlidException extends PortalException {

	public LayoutPageTemplateEntryPlidException(long groupId, long plid) {
		super(
			String.format(
				"Layout %s does not belong to group %s", plid, groupId));

		this.plid = plid;
	}

	public final long plid;

}