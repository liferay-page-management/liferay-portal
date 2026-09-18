/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.jsoup.internal;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.jsoup.JsoupDocumentFactory;
import com.liferay.portal.jsoup.configuration.JsoupConfiguration;

import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.jsoup.parser.TagSet;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Balázs Sáfrány-Kovalik
 */
@Component(
	configurationPid = "com.liferay.portal.jsoup.configuration.JsoupConfiguration",
	service = JsoupDocumentFactory.class
)
public class JsoupDocumentFactoryImpl implements JsoupDocumentFactory {

	@Override
	public Document parse(String html) {
		Parser parser = _getLegacySelfClosingTagParserInstance();

		if (parser == null) {
			return _disablePrettyPrint(Jsoup.parse(html));
		}

		return _disablePrettyPrint(
			_clearSelfCloseFlags(Jsoup.parse(html, StringPool.BLANK, parser)));
	}

	@Override
	public Document parseBodyFragment(String html) {
		Parser parser = _getLegacySelfClosingTagParserInstance();

		if (parser == null) {
			return _disablePrettyPrint(Jsoup.parseBodyFragment(html));
		}

		return _disablePrettyPrint(
			_clearSelfCloseFlags(_parseBodyFragmentWithParser(html, parser)));
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		JsoupConfiguration jsoupConfiguration =
			ConfigurableUtil.createConfigurable(
				JsoupConfiguration.class, properties);

		if (jsoupConfiguration.legacySelfClosingTagParsing()) {
			_legacySelfClosingTagParser = _createSelfClosingTagParser();
		}
		else {
			_legacySelfClosingTagParser = null;
		}
	}

	private Document _clearSelfCloseFlags(Document document) {
		for (Element element : document.getAllElements()) {
			Tag tag = element.tag();

			tag.clear(Tag.SelfClose);
		}

		return document;
	}

	private Parser _createSelfClosingTagParser() {
		Parser parser = Parser.htmlParser();

		TagSet tagSet = parser.tagSet();

		tagSet.onNewTag(tag -> tag.set(Tag.SelfClose));

		return parser;
	}

	private Document _disablePrettyPrint(Document document) {
		Document.OutputSettings outputSettings = document.outputSettings();

		outputSettings.prettyPrint(false);

		return document;
	}

	private Parser _getLegacySelfClosingTagParserInstance() {
		Parser parser = _legacySelfClosingTagParser;

		if (parser == null) {
			return null;
		}

		return parser.newInstance();
	}

	private Document _parseBodyFragmentWithParser(String html, Parser parser) {
		Document document = Document.createShell(StringPool.BLANK);

		document.parser(parser);

		Element bodyElement = document.body();

		bodyElement.appendChildren(
			parser.parseFragmentInput(html, bodyElement, StringPool.BLANK));

		return document;
	}

	private volatile Parser _legacySelfClosingTagParser;

}