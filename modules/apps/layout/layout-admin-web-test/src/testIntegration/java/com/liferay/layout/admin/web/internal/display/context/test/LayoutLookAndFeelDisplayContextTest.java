/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.admin.web.internal.display.context.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.depot.service.DepotEntryGroupRelLocalService;
import com.liferay.layout.page.template.constants.LayoutPageTemplateEntryTypeConstants;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.test.util.LayoutPageTemplateTestUtil;
import com.liferay.layout.test.util.DesignLibraryTestUtil;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.module.util.BundleUtil;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.portlet.bridges.mvc.constants.MVCRenderConstants;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portlet.test.MockLiferayPortletContext;

import jakarta.portlet.Portlet;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Constructor;

import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;

/**
 * @author Georgel Pop
 */
@RunWith(Arquillian.class)
public class LayoutLookAndFeelDisplayContextTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@FeatureFlag("LPD-57283")
	@Test
	@TestInfo("LPD-109972")
	public void testGetMasterLayoutConfigurationProps() throws Exception {
		FeatureFlagTestUtil.invokeFeatureFlagListeners(
			TestPropsValues.getCompanyId(), true, "LPD-57283");

		Layout layout = LayoutTestUtil.addTypeContentLayout(
			_groupLocalService.getGroup(TestPropsValues.getGroupId()));

		Group designLibraryGroup = DesignLibraryTestUtil.addDesignLibraryGroup(
			TestPropsValues.getGroupId(), true);

		LayoutPageTemplateEntry masterLayoutPageTemplateEntry =
			LayoutPageTemplateTestUtil.addLayoutPageTemplateEntry(
				designLibraryGroup.getGroupId(),
				LayoutPageTemplateEntryTypeConstants.MASTER_LAYOUT,
				WorkflowConstants.STATUS_APPROVED);

		layout = _layoutLocalService.updateMasterLayoutPageTemplateEntry(
			layout.getGroupId(), layout.isPrivateLayout(), layout.getLayoutId(),
			masterLayoutPageTemplateEntry.getExternalReferenceCode(),
			designLibraryGroup.getExternalReferenceCode());

		Assert.assertEquals(
			masterLayoutPageTemplateEntry.getPlid(),
			layout.getMasterLayoutPlid());

		_depotEntryGroupRelLocalService.deleteDepotEntryGroupRel(
			_depotEntryGroupRelLocalService.
				fetchDepotEntryGroupRelByDepotEntryIdToGroupId(
					designLibraryGroup.getClassPK(), layout.getGroupId()));

		layout = _layoutLocalService.getLayout(layout.getPlid());

		Assert.assertEquals(0, layout.getMasterLayoutPlid());

		Map<String, Object> masterLayoutConfigurationProps =
			_getMasterLayoutConfigurationProps(layout);

		Assert.assertEquals(
			masterLayoutPageTemplateEntry.getExternalReferenceCode(),
			masterLayoutConfigurationProps.get(
				"masterLayoutPageTemplateEntryERC"));
		Assert.assertEquals(
			designLibraryGroup.getExternalReferenceCode(),
			masterLayoutConfigurationProps.get(
				"masterLayoutPageTemplateEntryScopeERC"));
	}

	private Map<String, Object> _getMasterLayoutConfigurationProps(
			Layout layout)
		throws Exception {

		MockLiferayPortletRenderRequest mockLiferayPortletRenderRequest =
			new MockLiferayPortletRenderRequest();

		String path = "/view.jsp";

		mockLiferayPortletRenderRequest.setAttribute(
			MVCRenderConstants.
				PORTLET_CONTEXT_OVERRIDE_REQUEST_ATTIBUTE_NAME_PREFIX + path,
			new MockLiferayPortletContext(path));

		mockLiferayPortletRenderRequest.addParameter(
			"selPlid", String.valueOf(layout.getPlid()));
		mockLiferayPortletRenderRequest.setAttribute(
			JavaConstants.JAKARTA_PORTLET_CONFIG, null);
		mockLiferayPortletRenderRequest.setAttribute(
			WebKeys.COMPANY_ID, layout.getCompanyId());

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(layout.getCompanyId()));
		themeDisplay.setLocale(LocaleUtil.getDefault());
		themeDisplay.setPermissionChecker(
			PermissionThreadLocal.getPermissionChecker());
		themeDisplay.setScopeGroupId(layout.getGroupId());
		themeDisplay.setSiteGroupId(layout.getGroupId());
		themeDisplay.setUser(TestPropsValues.getUser());

		mockLiferayPortletRenderRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		MockLiferayPortletRenderResponse mockLiferayPortletRenderResponse =
			new MockLiferayPortletRenderResponse();
		MVCPortlet mvcPortlet = (MVCPortlet)_portlet;

		mvcPortlet.render(
			mockLiferayPortletRenderRequest, mockLiferayPortletRenderResponse);

		Bundle bundle = BundleUtil.getBundle(
			SystemBundleUtil.getBundleContext(),
			"com.liferay.layout.admin.web");

		Class<?> clazz = bundle.loadClass(
			"com.liferay.layout.admin.web.internal.display.context." +
				"LayoutLookAndFeelDisplayContext");

		Object layoutsAdminDisplayContext =
			mockLiferayPortletRenderRequest.getAttribute(
				"LAYOUT_PAGE_LAYOUT_ADMIN_DISPLAY_CONTEXT");

		Constructor<?> constructor = clazz.getConstructor(
			HttpServletRequest.class, layoutsAdminDisplayContext.getClass(),
			LiferayPortletResponse.class);

		return ReflectionTestUtil.invoke(
			constructor.newInstance(
				mockLiferayPortletRenderRequest.getHttpServletRequest(),
				layoutsAdminDisplayContext, mockLiferayPortletRenderResponse),
			"getMasterLayoutConfigurationProps", new Class<?>[0]);
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private DepotEntryGroupRelLocalService _depotEntryGroupRelLocalService;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject(
		filter = "component.name=com.liferay.layout.admin.web.internal.portlet.GroupPagesPortlet"
	)
	private Portlet _portlet;

}