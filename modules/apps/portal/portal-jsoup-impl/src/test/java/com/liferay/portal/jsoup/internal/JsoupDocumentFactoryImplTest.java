/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.jsoup.internal;

import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Balázs Sáfrány-Kovalik
 */
public class JsoupDocumentFactoryImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	@TestInfo({"LPD-97574", "LPD-103665"})
	public void testParse() {
		_testDisablesPrettyPrinting(JsoupDocumentFactoryImpl::parse);
		_testEmptyTagsWithLegacyParsingDisabled(
			JsoupDocumentFactoryImpl::parse);
		_testEmptyTagsWithLegacyParsingEnabled(JsoupDocumentFactoryImpl::parse);
		_testEmptyTagWithLegacyParsingDisabled(JsoupDocumentFactoryImpl::parse);
		_testEmptyTagWithLegacyParsingEnabled(JsoupDocumentFactoryImpl::parse);
		_testForeignAndVoidTags(JsoupDocumentFactoryImpl::parse);
		_testSeenSelfClosingTagWithLegacyParsingDisabled(
			JsoupDocumentFactoryImpl::parse);
		_testSeenSelfClosingTagWithLegacyParsingEnabled(
			JsoupDocumentFactoryImpl::parse);
		_testSelfClosingTagWithLegacyParsingDisabled(
			JsoupDocumentFactoryImpl::parse);
		_testSelfClosingTagWithLegacyParsingEnabled(
			JsoupDocumentFactoryImpl::parse);
	}

	@Test
	@TestInfo({"LPD-97574", "LPD-103665"})
	public void testParseBodyFragment() {
		_testDisablesPrettyPrinting(
			JsoupDocumentFactoryImpl::parseBodyFragment);
		_testEmptyTagsWithLegacyParsingDisabled(
			JsoupDocumentFactoryImpl::parseBodyFragment);
		_testEmptyTagsWithLegacyParsingEnabled(
			JsoupDocumentFactoryImpl::parseBodyFragment);
		_testEmptyTagWithLegacyParsingDisabled(
			JsoupDocumentFactoryImpl::parseBodyFragment);
		_testEmptyTagWithLegacyParsingEnabled(
			JsoupDocumentFactoryImpl::parseBodyFragment);
		_testForeignAndVoidTags(JsoupDocumentFactoryImpl::parseBodyFragment);
		_testSeenSelfClosingTagWithLegacyParsingDisabled(
			JsoupDocumentFactoryImpl::parseBodyFragment);
		_testSeenSelfClosingTagWithLegacyParsingEnabled(
			JsoupDocumentFactoryImpl::parseBodyFragment);
		_testSelfClosingTagWithLegacyParsingDisabled(
			JsoupDocumentFactoryImpl::parseBodyFragment);
		_testSelfClosingTagWithLegacyParsingEnabled(
			JsoupDocumentFactoryImpl::parseBodyFragment);
		_testStyleElementStaysInBody();
	}

	private void _assertBodyHTML(
		BiFunction<JsoupDocumentFactoryImpl, String, Document> parseFunction,
		boolean legacySelfClosingTagParsing, String html, String expectedHTML,
		String expectedXMLHTML) {

		JsoupDocumentFactoryImpl jsoupDocumentFactoryImpl =
			_createJsoupDocumentFactoryImpl(legacySelfClosingTagParsing);

		Document document = parseFunction.apply(jsoupDocumentFactoryImpl, html);

		Assert.assertEquals(expectedHTML, _getBodyHTML(document));
		Assert.assertEquals(expectedXMLHTML, _getXMLBodyHTML(document));
	}

	private JsoupDocumentFactoryImpl _createJsoupDocumentFactoryImpl(
		boolean legacySelfClosingTagParsing) {

		JsoupDocumentFactoryImpl jsoupDocumentFactoryImpl =
			new JsoupDocumentFactoryImpl();

		Map<String, Object> properties = Collections.emptyMap();

		if (legacySelfClosingTagParsing) {
			properties = HashMapBuilder.<String, Object>put(
				"legacySelfClosingTagParsing", true
			).build();
		}

		jsoupDocumentFactoryImpl.activate(properties);

		return jsoupDocumentFactoryImpl;
	}

	private String _getBodyHTML(Document document) {
		Element bodyElement = document.body();

		return bodyElement.html();
	}

	private String _getXMLBodyHTML(Document document) {
		Document.OutputSettings outputSettings = document.outputSettings();

		outputSettings.syntax(Document.OutputSettings.Syntax.xml);

		return _getBodyHTML(document);
	}

	private void _testDisablesPrettyPrinting(
		BiFunction<JsoupDocumentFactoryImpl, String, Document> parseFunction) {

		_assertBodyHTML(
			parseFunction, false,
			"<div><ul><li>a</li><li>b</li></ul><p>c  d</p></div>",
			"<div><ul><li>a</li><li>b</li></ul><p>c  d</p></div>",
			"<div><ul><li>a</li><li>b</li></ul><p>c  d</p></div>");
	}

	private void _testEmptyTagsWithLegacyParsingDisabled(
		BiFunction<JsoupDocumentFactoryImpl, String, Document> parseFunction) {

		_assertBodyHTML(
			parseFunction, false,
			"<p>x</p><div class=\"c\"></div><span></span>",
			"<p>x</p><div class=\"c\"></div><span></span>",
			"<p>x</p><div class=\"c\"></div><span></span>");
	}

	private void _testEmptyTagsWithLegacyParsingEnabled(
		BiFunction<JsoupDocumentFactoryImpl, String, Document> parseFunction) {

		_assertBodyHTML(
			parseFunction, true, "<p>x</p><div class=\"c\"></div><span></span>",
			"<p>x</p><div class=\"c\"></div><span></span>",
			"<p>x</p><div class=\"c\"></div><span></span>");
	}

	private void _testEmptyTagWithLegacyParsingDisabled(
		BiFunction<JsoupDocumentFactoryImpl, String, Document> parseFunction) {

		_assertBodyHTML(
			parseFunction, false, "<div class=\"c\"></div>",
			"<div class=\"c\"></div>", "<div class=\"c\"></div>");
	}

	private void _testEmptyTagWithLegacyParsingEnabled(
		BiFunction<JsoupDocumentFactoryImpl, String, Document> parseFunction) {

		_assertBodyHTML(
			parseFunction, true, "<div class=\"c\"></div>",
			"<div class=\"c\"></div>", "<div class=\"c\"></div>");
	}

	private void _testForeignAndVoidTags(
		BiFunction<JsoupDocumentFactoryImpl, String, Document> parseFunction) {

		_assertBodyHTML(
			parseFunction, true, "<svg><path d=\"M0 0\" /></svg><br /><p>a</p>",
			"<svg><path d=\"M0 0\" /></svg><br><p>a</p>",
			"<svg><path d=\"M0 0\" /></svg><br /><p>a</p>");
	}

	private void _testSeenSelfClosingTagWithLegacyParsingDisabled(
		BiFunction<JsoupDocumentFactoryImpl, String, Document> parseFunction) {

		_assertBodyHTML(
			parseFunction, false, "<div class=\"a\" /><div class=\"b\"></div>",
			"<div class=\"a\"><div class=\"b\"></div></div>",
			"<div class=\"a\"><div class=\"b\" /></div>");
	}

	private void _testSeenSelfClosingTagWithLegacyParsingEnabled(
		BiFunction<JsoupDocumentFactoryImpl, String, Document> parseFunction) {

		_assertBodyHTML(
			parseFunction, true, "<div class=\"a\" /><div class=\"b\"></div>",
			"<div class=\"a\"></div><div class=\"b\"></div>",
			"<div class=\"a\" /><div class=\"b\" />");
	}

	private void _testSelfClosingTagWithLegacyParsingDisabled(
		BiFunction<JsoupDocumentFactoryImpl, String, Document> parseFunction) {

		_assertBodyHTML(
			parseFunction, false, "<div class=\"a\" /><span>b</span>",
			"<div class=\"a\"><span>b</span></div>",
			"<div class=\"a\"><span>b</span></div>");
	}

	private void _testSelfClosingTagWithLegacyParsingEnabled(
		BiFunction<JsoupDocumentFactoryImpl, String, Document> parseFunction) {

		_assertBodyHTML(
			parseFunction, true, "<div class=\"a\" /><span>b</span>",
			"<div class=\"a\"></div><span>b</span>",
			"<div class=\"a\" /><span>b</span>");
	}

	private void _testStyleElementStaysInBody() {
		JsoupDocumentFactoryImpl jsoupDocumentFactoryImpl =
			_createJsoupDocumentFactoryImpl(true);

		Assert.assertEquals(
			"<style>.a {color: red}</style><div>b</div>",
			_getBodyHTML(
				jsoupDocumentFactoryImpl.parseBodyFragment(
					"<style>.a {color: red}</style><div>b</div>")));
	}

}