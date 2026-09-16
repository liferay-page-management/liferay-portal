/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.content.page.editor.web.internal.util;

import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.ERCInfoItemIdentifier;
import com.liferay.info.item.InfoItemReference;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Chaitanya Sammetla
 */
public class InfoItemReferenceUtil {

	public static InfoItemReference getInfoItemReference(
		String className, long classPK, String externalReferenceCode) {

		if (Validator.isNull(className) ||
			((classPK <= 0) && Validator.isNull(externalReferenceCode))) {

			return null;
		}

		if (classPK > 0) {
			return new InfoItemReference(
				className, new ClassPKInfoItemIdentifier(classPK));
		}

		return new InfoItemReference(
			className, new ERCInfoItemIdentifier(externalReferenceCode));
	}

}