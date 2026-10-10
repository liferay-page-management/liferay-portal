/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.entry.processor.freemarker.internal.template;

import java.util.Collections;

/**
 * @author Jiefeng Wu
 */
public class DummyRESTClient {

	public Object get(String path) {
		return Collections.emptyMap();
	}

}