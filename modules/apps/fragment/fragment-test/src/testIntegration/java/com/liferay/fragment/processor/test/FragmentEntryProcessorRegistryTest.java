/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.processor.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.fragment.constants.FragmentEntryLinkConstants;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.processor.DefaultFragmentEntryProcessorContext;
import com.liferay.fragment.processor.FragmentEntryProcessorContext;
import com.liferay.fragment.processor.FragmentEntryProcessorRegistry;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.layout.test.util.ContentLayoutTestUtil;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.test.util.IdempotentRetryAssert;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PersistenceTestRule;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Víctor Galán
 * @author Balázs Sáfrány-Kovalik
 */
@RunWith(Arquillian.class)
public class FragmentEntryProcessorRegistryTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(), PersistenceTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_layout = LayoutTestUtil.addTypeContentLayout(_group);
	}

	@Test
	public void testGetAvailableTagsJSONArray() {
		JSONArray availableTagsJSONArray =
			_fragmentEntryProcessorRegistry.getAvailableTagsJSONArray();

		for (int i = 0; i < availableTagsJSONArray.length(); i++) {
			JSONObject tagsJSONArrayJSONObject =
				availableTagsJSONArray.getJSONObject(i);

			String name = tagsJSONArrayJSONObject.getString("name");

			Assert.assertFalse(name.startsWith("lfr-editable:"));
		}
	}

	@Test
	@TestInfo("LPD-103665")
	public void testProcessFragmentEntryLinkHTML() throws Exception {
		_testWithLegacyParsingDisabled();
		_testWithLegacyParsingEnabled();
	}

	private void _assertProcessedHTML(
			String expectedHTML, String html,
			boolean legacySelfClosingTagParsing)
		throws Exception {

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					"com.liferay.portal.jsoup.configuration.JsoupConfiguration",
					HashMapDictionaryBuilder.<String, Object>put(
						"legacySelfClosingTagParsing",
						legacySelfClosingTagParsing
					).build())) {

			IdempotentRetryAssert.retryAssert(
				5, TimeUnit.SECONDS, 1, TimeUnit.SECONDS,
				() -> {
					Assert.assertEquals(
						expectedHTML, _processFragmentEntryLinkHTML(html));

					return null;
				});
		}
	}

	private FragmentEntryProcessorContext _getFragmentEntryProcessorContext()
		throws Exception {

		return new DefaultFragmentEntryProcessorContext(
			_layout.getCompanyId(),
			ContentLayoutTestUtil.getMockHttpServletRequest(
				_companyLocalService.getCompany(_group.getCompanyId()), _group,
				_layout),
			new MockHttpServletResponse(), LocaleUtil.US,
			FragmentEntryLinkConstants.VIEW, _layout.getGroupId());
	}

	private FragmentEntryProcessorRegistry
		_getFragmentEntryProcessorRegistry() {

		Bundle bundle = FrameworkUtil.getBundle(
			FragmentEntryProcessorRegistryTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		return bundleContext.getService(
			bundleContext.getServiceReference(
				FragmentEntryProcessorRegistry.class));
	}

	private String _processFragmentEntryLinkHTML(String html) throws Exception {
		FragmentEntryLink fragmentEntryLink =
			_fragmentEntryLinkLocalService.createFragmentEntryLink(0);

		fragmentEntryLink.setHtml(html);

		FragmentEntryProcessorRegistry fragmentEntryProcessorRegistry =
			_getFragmentEntryProcessorRegistry();

		return StringUtil.trim(
			fragmentEntryProcessorRegistry.processFragmentEntryLinkHTML(
				fragmentEntryLink, _getFragmentEntryProcessorContext()));
	}

	private void _testWithLegacyParsingDisabled() throws Exception {
		_assertProcessedHTML(
			"<div class=\"a\"><span>b</span></div>",
			"<div class=\"a\" /><span>b</span>", false);
	}

	private void _testWithLegacyParsingEnabled() throws Exception {
		_assertProcessedHTML(
			"<div class=\"a\"></div><span>b</span>",
			"<div class=\"a\" /><span>b</span>", true);
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@Inject
	private FragmentEntryProcessorRegistry _fragmentEntryProcessorRegistry;

	@DeleteAfterTestRun
	private Group _group;

	private Layout _layout;

}