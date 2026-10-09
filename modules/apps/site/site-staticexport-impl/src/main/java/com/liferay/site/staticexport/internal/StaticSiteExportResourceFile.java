/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.staticexport.internal;

import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.File;

import java.util.Map;

/**
 * @author Víctor Galán
 */
public class StaticSiteExportResourceFile {

	public StaticSiteExportResourceFile(String contentType, File file) {
		_contentType = contentType;
		_file = file;
	}

	public String getExtension() {
		if (Validator.isNull(_contentType)) {
			return null;
		}

		String contentType = _contentType;

		int semicolonIndex = contentType.indexOf(CharPool.SEMICOLON);

		if (semicolonIndex != -1) {
			contentType = contentType.substring(0, semicolonIndex);
		}

		return _extensions.get(
			StringUtil.toLowerCase(StringUtil.trim(contentType)));
	}

	public File getFile() {
		return _file;
	}

	private static final Map<String, String> _extensions = HashMapBuilder.put(
		ContentTypes.APPLICATION_JAVASCRIPT, "js"
	).put(
		ContentTypes.TEXT_CSS, "css"
	).put(
		ContentTypes.TEXT_JAVASCRIPT, "js"
	).build();

	private final String _contentType;
	private final File _file;

}