/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.admin.web.internal.item.selector.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.page.template.constants.LayoutPageTemplateEntryTypeConstants;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.layout.page.template.test.util.LayoutPageTemplateTestUtil;
import com.liferay.layout.test.util.DesignLibraryTestUtil;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.LayoutPrototype;
import com.liferay.portal.kernel.module.util.BundleUtil;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.service.LayoutPrototypeLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletURL;
import com.liferay.portal.kernel.test.portlet.MockLiferayResourceRequest;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import jakarta.portlet.PortletURL;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Constructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Georgel Pop
 */
@FeatureFlags(
	featureFlags = {@FeatureFlag("LPD-57283"), @FeatureFlag("LPD-81022")}
)
@RunWith(Arquillian.class)
public class MasterLayoutPageTemplateEntryItemSelectorViewDescriptorTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		FeatureFlagTestUtil.invokeFeatureFlagListeners(
			_group.getCompanyId(), true, "LPD-57283");
	}

	@Test
	@TestInfo("LPD-109972")
	public void testGetSearchContainer() throws Exception {
		_testGetSearchContainer();
		_testGetSearchContainerWithLayoutPrototypeGroup();
	}

	@Test
	@TestInfo("LPD-109972")
	public void testIsShowBreadcrumb() throws Exception {
		Assert.assertFalse(
			ReflectionTestUtil.invoke(
				_getMasterLayoutPageTemplateEntryItemSelectorViewDescriptor(
					_group, _group.getGroupId()),
				"isShowBreadcrumb", new Class<?>[0]));

		DesignLibraryTestUtil.addDesignLibraryGroup(_group.getGroupId(), true);

		Assert.assertTrue(
			ReflectionTestUtil.invoke(
				_getMasterLayoutPageTemplateEntryItemSelectorViewDescriptor(
					_group, _group.getGroupId()),
				"isShowBreadcrumb", new Class<?>[0]));
	}

	private Object _getMasterLayoutPageTemplateEntryItemSelectorViewDescriptor(
			Group group, long refererGroupId)
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setLocale(LocaleUtil.US);
		themeDisplay.setRefererGroupId(refererGroupId);
		themeDisplay.setScopeGroupId(group.getGroupId());

		mockHttpServletRequest.setAttribute(
			JavaConstants.JAKARTA_PORTLET_REQUEST,
			new MockLiferayResourceRequest());
		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		Bundle bundle = BundleUtil.getBundle(
			SystemBundleUtil.getBundleContext(),
			"com.liferay.layout.admin.web");

		Class<?> clazz = bundle.loadClass(
			"com.liferay.layout.admin.web.internal.item.selector." +
				"MasterLayoutPageTemplateEntryItemSelectorViewDescriptor");

		Constructor<?> constructor = clazz.getConstructor(
			HttpServletRequest.class, PortletURL.class);

		return constructor.newInstance(
			mockHttpServletRequest, new MockLiferayPortletURL());
	}

	private List<String> _getNames(Group group) throws Exception {
		SearchContainer<LayoutPageTemplateEntry> searchContainer =
			ReflectionTestUtil.invoke(
				_getMasterLayoutPageTemplateEntryItemSelectorViewDescriptor(
					group, _group.getGroupId()),
				"getSearchContainer", new Class<?>[0]);

		return TransformUtil.transform(
			searchContainer.getResults(), LayoutPageTemplateEntry::getName);
	}

	private void _testGetSearchContainer() throws Exception {
		LayoutPageTemplateEntry layoutPageTemplateEntry =
			LayoutPageTemplateTestUtil.addLayoutPageTemplateEntry(
				_group.getGroupId(),
				LayoutPageTemplateEntryTypeConstants.MASTER_LAYOUT,
				WorkflowConstants.STATUS_APPROVED);

		Group designLibraryGroup = DesignLibraryTestUtil.addDesignLibraryGroup(
			_group.getGroupId(), true);

		LayoutPageTemplateEntry designLibraryLayoutPageTemplateEntry =
			LayoutPageTemplateTestUtil.addLayoutPageTemplateEntry(
				designLibraryGroup.getGroupId(),
				LayoutPageTemplateEntryTypeConstants.MASTER_LAYOUT,
				WorkflowConstants.STATUS_APPROVED);

		Group disconnectedDesignLibraryGroup =
			DesignLibraryTestUtil.addDesignLibraryGroup(
				_group.getGroupId(), false);

		LayoutPageTemplateTestUtil.addLayoutPageTemplateEntry(
			disconnectedDesignLibraryGroup.getGroupId(),
			LayoutPageTemplateEntryTypeConstants.MASTER_LAYOUT,
			WorkflowConstants.STATUS_APPROVED);

		Assert.assertEquals(
			Arrays.asList("Blank", layoutPageTemplateEntry.getName()),
			_getNames(_group));
		Assert.assertEquals(
			Arrays.asList(designLibraryLayoutPageTemplateEntry.getName()),
			_getNames(designLibraryGroup));
		Assert.assertEquals(
			Collections.emptyList(), _getNames(disconnectedDesignLibraryGroup));
	}

	private void _testGetSearchContainerWithLayoutPrototypeGroup()
		throws Exception {

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			LayoutPageTemplateTestUtil.addLayoutPageTemplateEntry(
				_group.getGroupId(),
				LayoutPageTemplateEntryTypeConstants.MASTER_LAYOUT,
				WorkflowConstants.STATUS_APPROVED);

		_layoutPageTemplateEntryLocalService.updateLayoutPageTemplateEntry(
			layoutPageTemplateEntry.getLayoutPageTemplateEntryId(), true);

		LayoutPageTemplateEntry widgetPageLayoutPageTemplateEntry =
			LayoutPageTemplateTestUtil.addLayoutPageTemplateEntry(
				_group.getGroupId(),
				LayoutPageTemplateEntryTypeConstants.WIDGET_PAGE,
				WorkflowConstants.STATUS_APPROVED);

		LayoutPrototype layoutPrototype =
			_layoutPrototypeLocalService.getLayoutPrototype(
				widgetPageLayoutPageTemplateEntry.getLayoutPrototypeId());

		SearchContainer<LayoutPageTemplateEntry> searchContainer =
			ReflectionTestUtil.invoke(
				_getMasterLayoutPageTemplateEntryItemSelectorViewDescriptor(
					layoutPrototype.getGroup(), 0),
				"getSearchContainer", new Class<?>[0]);

		Assert.assertEquals(
			Arrays.asList(layoutPageTemplateEntry.getName()),
			TransformUtil.transform(
				ListUtil.filter(
					searchContainer.getResults(),
					LayoutPageTemplateEntry::isDefaultTemplate),
				LayoutPageTemplateEntry::getName));
	}

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private LayoutPageTemplateEntryLocalService
		_layoutPageTemplateEntryLocalService;

	@Inject
	private LayoutPrototypeLocalService _layoutPrototypeLocalService;

}