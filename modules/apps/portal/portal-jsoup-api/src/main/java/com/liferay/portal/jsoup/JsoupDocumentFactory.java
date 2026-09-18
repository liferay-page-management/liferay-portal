/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.jsoup;

import org.jsoup.nodes.Document;

/**
 * Creates the jsoup documents Liferay parses HTML into, so that the parsing
 * and serialization options apply everywhere instead of being repeated at each
 * call site.
 *
 * @author Balázs Sáfrány-Kovalik
 */
public interface JsoupDocumentFactory {

	/**
	 * Parses the HTML as a complete document, the way <code>Jsoup.parse</code>
	 * does.
	 *
	 * @param  html the HTML to parse
	 * @return the document, with pretty printing disabled
	 */
	public Document parse(String html);

	/**
	 * Parses the HTML into the body of a document shell, the way
	 * <code>Jsoup.parseBodyFragment</code> does. Unlike {@link #parse(String)},
	 * elements that belong to the head of a complete document, such as
	 * <code>style</code>, stay in the body.
	 *
	 * @param  html the HTML to parse
	 * @return the document, with pretty printing disabled
	 */
	public Document parseBodyFragment(String html);

}