/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.jsoup;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.service.Snapshot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Reaches {@link JsoupDocumentFactory} from the classes that cannot hold a
 * reference to it. Prefer injecting the service itself wherever the caller is a
 * component.
 *
 * @author Balázs Sáfrány-Kovalik
 */
public class JsoupDocumentFactoryUtil {

	public static JsoupDocumentFactory getJsoupDocumentFactory() {
		return _snapshot.get();
	}

	public static Document parse(String html) {
		JsoupDocumentFactory jsoupDocumentFactory = getJsoupDocumentFactory();

		if (jsoupDocumentFactory == null) {
			_log.error("Jsoup document factory is null");

			return _setOutputSettings(Jsoup.parse(html));
		}

		return jsoupDocumentFactory.parse(html);
	}

	public static Document parseBodyFragment(String html) {
		JsoupDocumentFactory jsoupDocumentFactory = getJsoupDocumentFactory();

		if (jsoupDocumentFactory == null) {
			_log.error("Jsoup document factory is null");

			return _setOutputSettings(Jsoup.parseBodyFragment(html));
		}

		return jsoupDocumentFactory.parseBodyFragment(html);
	}

	private static Document _setOutputSettings(Document document) {
		Document.OutputSettings outputSettings = document.outputSettings();

		outputSettings.prettyPrint(false);

		return document;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		JsoupDocumentFactoryUtil.class);

	private static final Snapshot<JsoupDocumentFactory> _snapshot =
		new Snapshot<>(
			JsoupDocumentFactoryUtil.class, JsoupDocumentFactory.class);

}