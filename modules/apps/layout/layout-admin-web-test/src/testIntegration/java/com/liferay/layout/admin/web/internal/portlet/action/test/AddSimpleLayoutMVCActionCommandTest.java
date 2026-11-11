/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.admin.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.page.template.constants.LayoutPageTemplateEntryTypeConstants;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.layout.page.template.test.util.LayoutPageTemplateTestUtil;
import com.liferay.layout.test.util.DesignLibraryTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.servlet.MultiSessionMessages;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Georgel Pop
 */
@RunWith(Arquillian.class)
public class AddSimpleLayoutMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-57283"), @FeatureFlag("LPD-81022")}
	)
	@Test
	@TestInfo("LPD-108824")
	public void testAddSimpleLayoutWithDesignLibraryMasterLayout()
		throws Exception {

		FeatureFlagTestUtil.invokeFeatureFlagListeners(
			TestPropsValues.getCompanyId(), true, "LPD-57283");

		Group designLibraryGroup = DesignLibraryTestUtil.addDesignLibraryGroup(
			TestPropsValues.getGroupId(), true);

		LayoutPageTemplateEntry masterLayoutPageTemplateEntry =
			LayoutPageTemplateTestUtil.addLayoutPageTemplateEntry(
				designLibraryGroup.getGroupId(),
				LayoutPageTemplateEntryTypeConstants.MASTER_LAYOUT,
				WorkflowConstants.STATUS_APPROVED);

		_assertMasterLayout(
			designLibraryGroup,
			_addSimpleLayout(
				masterLayoutPageTemplateEntry.getPlid(),
				LayoutConstants.TYPE_CONTENT),
			masterLayoutPageTemplateEntry);

		_layoutPageTemplateEntryLocalService.updateLayoutPageTemplateEntry(
			masterLayoutPageTemplateEntry.getLayoutPageTemplateEntryId(), true);

		_assertMasterLayout(
			designLibraryGroup,
			_addSimpleLayout(0, LayoutConstants.TYPE_PORTLET),
			masterLayoutPageTemplateEntry);

		Layout layout = _addSimpleLayout(0, LayoutConstants.TYPE_CONTENT);

		Assert.assertEquals(0, layout.getMasterLayoutPlid());
	}

	private Layout _addSimpleLayout(long masterLayoutPlid, String type)
		throws Exception {

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		mockLiferayPortletActionRequest.setAttribute(
			WebKeys.THEME_DISPLAY,
			_getThemeDisplay(mockLiferayPortletActionRequest));
		mockLiferayPortletActionRequest.setParameter(
			"groupId", String.valueOf(TestPropsValues.getGroupId()));
		mockLiferayPortletActionRequest.setParameter(
			"liveGroupId", String.valueOf(TestPropsValues.getGroupId()));
		mockLiferayPortletActionRequest.setParameter(
			"masterLayoutPlid", String.valueOf(masterLayoutPlid));
		mockLiferayPortletActionRequest.setParameter(
			"name", RandomTestUtil.randomString());
		mockLiferayPortletActionRequest.setParameter("type", type);

		_mvcActionCommand.processAction(
			mockLiferayPortletActionRequest,
			new MockLiferayPortletActionResponse());

		Layout layout = (Layout)MultiSessionMessages.get(
			mockLiferayPortletActionRequest, "layoutAdded");

		return _layoutLocalService.getLayout(layout.getPlid());
	}

	private void _assertMasterLayout(
		Group group, Layout layout,
		LayoutPageTemplateEntry masterLayoutPageTemplateEntry) {

		Assert.assertEquals(
			masterLayoutPageTemplateEntry.getExternalReferenceCode(),
			layout.getMasterLayoutPageTemplateEntryERC());
		Assert.assertEquals(
			group.getExternalReferenceCode(),
			layout.getMasterLayoutPageTemplateEntryScopeERC());
		Assert.assertEquals(
			masterLayoutPageTemplateEntry.getPlid(),
			layout.getMasterLayoutPlid());
	}

	private ThemeDisplay _getThemeDisplay(
			MockLiferayPortletActionRequest mockLiferayPortletActionRequest)
		throws Exception {

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(TestPropsValues.getCompanyId()));
		themeDisplay.setLocale(LocaleUtil.getSiteDefault());
		themeDisplay.setPermissionChecker(
			PermissionThreadLocal.getPermissionChecker());
		themeDisplay.setRequest(
			mockLiferayPortletActionRequest.getHttpServletRequest());
		themeDisplay.setScopeGroupId(TestPropsValues.getGroupId());
		themeDisplay.setSiteGroupId(TestPropsValues.getGroupId());
		themeDisplay.setUser(TestPropsValues.getUser());

		return themeDisplay;
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject
	private LayoutPageTemplateEntryLocalService
		_layoutPageTemplateEntryLocalService;

	@Inject(filter = "mvc.command.name=/layout_admin/add_simple_layout")
	private MVCActionCommand _mvcActionCommand;

}