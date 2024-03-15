/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.type.controller.content.internal.layout.type.controller.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.page.template.constants.LayoutPageTemplateCollectionTypeConstants;
import com.liferay.layout.page.template.constants.LayoutPageTemplateConstants;
import com.liferay.layout.page.template.constants.LayoutPageTemplateEntryTypeConstants;
import com.liferay.layout.page.template.model.LayoutPageTemplateCollection;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateCollectionServiceUtil;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryServiceUtil;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.layout.utility.page.kernel.constants.LayoutUtilityPageEntryConstants;
import com.liferay.layout.utility.page.model.LayoutUtilityPageEntry;
import com.liferay.layout.utility.page.service.LayoutUtilityPageEntryLocalServiceUtil;
import com.liferay.portal.kernel.exception.NoSuchLayoutException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutTypeController;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.service.LayoutSetLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.util.LayoutTypeControllerTracker;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Lourdes Fernández Besada
 */
@RunWith(Arquillian.class)
public class ContentLayoutTypeControllerTest {

	@ClassRule
	@Rule
	public static AggregateTestRule aggregateTestRule = new AggregateTestRule(
		new LiferayIntegrationTestRule(),
		PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		LayoutTestUtil.addTypePortletLayout(_group);

		ServiceContextThreadLocal.pushServiceContext(
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));
	}

	@After
	public void tearDown() throws Exception {
		ServiceContextThreadLocal.popServiceContext();
	}

	@FeatureFlags("LPD-11070")
	@Test(expected = PrincipalException.class)
	public void testContentLayoutTypeControllerDraftEditWithPreviewDraftPermission()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		Layout draftLayout = layout.fetchDraftLayout();

		_includeLayoutContentWithNonadminUser(
			ActionKeys.PREVIEW_DRAFT, draftLayout, Constants.EDIT);
	}

	@FeatureFlags("LPD-11070")
	@Test
	public void testContentLayoutTypeControllerDraftPreviewWithPreviewDraftPermission()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		Layout draftLayout = layout.fetchDraftLayout();

		Assert.assertFalse(
			_includeLayoutContentWithNonadminUser(
				ActionKeys.PREVIEW_DRAFT, draftLayout, Constants.PREVIEW));
	}

	@FeatureFlags("LPD-11070")
	@Test
	public void testContentLayoutTypeControllerDraftPreviewWithUpdatePermission()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		Layout draftLayout = layout.fetchDraftLayout();

		Assert.assertFalse(
			_includeLayoutContentWithNonadminUser(
				ActionKeys.UPDATE, draftLayout, Constants.PREVIEW));
	}

	@FeatureFlags("LPD-11070")
	@Test(expected = PrincipalException.class)
	public void testContentLayoutTypeControllerDraftPreviewWithViewPermission()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		Layout draftLayout = layout.fetchDraftLayout();

		_includeLayoutContentWithNonadminUser(
			ActionKeys.VIEW, draftLayout, Constants.PREVIEW);
	}

	@FeatureFlags("LPD-11070")
	@Test
	public void testContentLayoutTypeControllerDraftViewWithPreviewDraftPermission()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		Layout draftLayout = layout.fetchDraftLayout();

		Assert.assertFalse(
			_includeLayoutContentWithNonadminUser(
				ActionKeys.PREVIEW_DRAFT, draftLayout, Constants.VIEW));
	}

	@Test(expected = NoSuchLayoutException.class)
	public void testContentLayoutTypeControllerNoPublishedPageGuestUser()
		throws Exception {

		LayoutTypeController layoutTypeController =
			LayoutTypeControllerTracker.getLayoutTypeController(
				LayoutConstants.TYPE_CONTENT);

		layoutTypeController.includeLayoutContent(
			_getHttpServletRequest(
				_userLocalService.getGuestUser(_group.getCompanyId())),
			new MockHttpServletResponse(),
			LayoutTestUtil.addTypeContentLayout(_group));
	}

	@Test
	public void testContentLayoutTypeControllerNoPublishedPagePermissionUser()
		throws Exception {

		LayoutTypeController layoutTypeController =
			LayoutTypeControllerTracker.getLayoutTypeController(
				LayoutConstants.TYPE_CONTENT);

		Assert.assertFalse(
			layoutTypeController.includeLayoutContent(
				_getHttpServletRequest(TestPropsValues.getUser()),
				new MockHttpServletResponse(),
				LayoutTestUtil.addTypeContentLayout(_group)));
	}

	@FeatureFlags("LPD-11070")
	@Test(expected = PrincipalException.class)
	public void testContentLayoutTypeControllerPageTemplateDraftPreviewWithPreviewDraftPermission()
		throws Exception {

		Layout layout = _addTypePageTemplateEntryLayout();

		Layout draftLayout = layout.fetchDraftLayout();

		_includeLayoutContentWithNonadminUser(
			ActionKeys.PREVIEW_DRAFT, draftLayout, Constants.PREVIEW);
	}

	@FeatureFlags("LPD-11070")
	@Test
	public void testContentLayoutTypeControllerPageTemplateDraftPreviewWithUpdatePermission()
		throws Exception {

		Layout layout = _addTypePageTemplateEntryLayout();

		Layout draftLayout = layout.fetchDraftLayout();

		Assert.assertFalse(
			_includeLayoutContentWithNonadminUser(
				ActionKeys.UPDATE, draftLayout, Constants.PREVIEW));
	}

	@FeatureFlags("LPD-11070")
	@Test(expected = PrincipalException.class)
	public void testContentLayoutTypeControllerPageTemplateDraftPreviewWithViewPermission()
		throws Exception {

		Layout layout = _addTypePageTemplateEntryLayout();

		Layout draftLayout = layout.fetchDraftLayout();

		_includeLayoutContentWithNonadminUser(
			ActionKeys.VIEW, draftLayout, Constants.PREVIEW);
	}

	@Test
	public void testContentLayoutTypeControllerPublishedPageGuestUser()
		throws Exception {

		LayoutTypeController layoutTypeController =
			LayoutTypeControllerTracker.getLayoutTypeController(
				LayoutConstants.TYPE_CONTENT);

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		Layout draftLayout = layout.fetchDraftLayout();

		Assert.assertNotNull(draftLayout);

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				_group.getCompanyId(), _group.getGroupId(),
				TestPropsValues.getUserId());

		serviceContext.setRequest(
			_getHttpServletRequest(TestPropsValues.getUser()));

		_layoutLocalService.updateStatus(
			TestPropsValues.getUserId(), draftLayout.getPlid(),
			WorkflowConstants.STATUS_APPROVED, serviceContext);

		Assert.assertFalse(
			layoutTypeController.includeLayoutContent(
				_getHttpServletRequest(
					_userLocalService.getGuestUser(_group.getCompanyId())),
				new MockHttpServletResponse(), layout));
	}

	@Test
	public void testContentLayoutTypeControllerPublishedPagePermissionUser()
		throws Exception {

		LayoutTypeController layoutTypeController =
			LayoutTypeControllerTracker.getLayoutTypeController(
				LayoutConstants.TYPE_CONTENT);

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		Layout draftLayout = layout.fetchDraftLayout();

		Assert.assertNotNull(draftLayout);

		_layoutLocalService.updateStatus(
			TestPropsValues.getUserId(), draftLayout.getPlid(),
			WorkflowConstants.STATUS_APPROVED,
			ServiceContextTestUtil.getServiceContext(
				_group.getCompanyId(), _group.getGroupId(),
				TestPropsValues.getUserId()));

		Assert.assertFalse(
			layoutTypeController.includeLayoutContent(
				_getHttpServletRequest(TestPropsValues.getUser()),
				new MockHttpServletResponse(), layout));
	}

	@FeatureFlags("LPD-11070")
	@Test(expected = PrincipalException.class)
	public void testContentLayoutTypeControllerUtilityPageDraftPreviewWithPreviewDraftPermission()
		throws Exception {

		Layout layout = _addTypeUtilityPageEntryLayout();

		Layout draftLayout = layout.fetchDraftLayout();

		_includeLayoutContentWithNonadminUser(
			ActionKeys.PREVIEW_DRAFT, draftLayout, Constants.PREVIEW);
	}

	@FeatureFlags("LPD-11070")
	@Test
	public void testContentLayoutTypeControllerUtilityPageDraftPreviewWithUpdatePermission()
		throws Exception {

		Layout layout = _addTypeUtilityPageEntryLayout();

		Layout draftLayout = layout.fetchDraftLayout();

		Assert.assertFalse(
			_includeLayoutContentWithNonadminUser(
				ActionKeys.UPDATE, draftLayout, Constants.PREVIEW));
	}

	@FeatureFlags("LPD-11070")
	@Test(expected = PrincipalException.class)
	public void testContentLayoutTypeControllerUtilityPageDraftPreviewWithViewPermission()
		throws Exception {

		Layout layout = _addTypeUtilityPageEntryLayout();

		Layout draftLayout = layout.fetchDraftLayout();

		_includeLayoutContentWithNonadminUser(
			ActionKeys.VIEW, draftLayout, Constants.PREVIEW);
	}

	private Layout _addTypePageTemplateEntryLayout() throws Exception {
		LayoutPageTemplateCollection layoutPageTemplateCollection =
			LayoutPageTemplateCollectionServiceUtil.
				addLayoutPageTemplateCollection(
					_group.getGroupId(),
					LayoutPageTemplateConstants.
						PARENT_LAYOUT_PAGE_TEMPLATE_COLLECTION_ID_DEFAULT,
					RandomTestUtil.randomString(),
					RandomTestUtil.randomString(),
					LayoutPageTemplateCollectionTypeConstants.BASIC,
					ServiceContextTestUtil.getServiceContext());

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			LayoutPageTemplateEntryServiceUtil.addLayoutPageTemplateEntry(
				_group.getGroupId(),
				layoutPageTemplateCollection.
					getLayoutPageTemplateCollectionId(),
				RandomTestUtil.randomString(),
				LayoutPageTemplateEntryTypeConstants.BASIC, 0,
				WorkflowConstants.STATUS_DRAFT,
				ServiceContextTestUtil.getServiceContext());

		return LayoutLocalServiceUtil.getLayout(
			layoutPageTemplateEntry.getPlid());
	}

	private Layout _addTypeUtilityPageEntryLayout() throws Exception {
		LayoutUtilityPageEntry layoutUtilityPageEntry =
			LayoutUtilityPageEntryLocalServiceUtil.addLayoutUtilityPageEntry(
				null, TestPropsValues.getUserId(), _group.getGroupId(), 0, 0,
				false, RandomTestUtil.randomString(),
				LayoutUtilityPageEntryConstants.TYPE_SC_NOT_FOUND, 0,
				ServiceContextTestUtil.getServiceContext());

		return LayoutLocalServiceUtil.getLayout(
			layoutUtilityPageEntry.getPlid());
	}

	private HttpServletRequest _getHttpServletRequest(
			String layoutMode, User user)
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.CURRENT_URL, "http://www.liferay.com");

		UserTestUtil.setUser(user);

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY,
			_getThemeDisplay(user, mockHttpServletRequest));

		if (Validator.isNotNull(layoutMode)) {
			mockHttpServletRequest.setParameter("p_l_mode", layoutMode);
		}

		return mockHttpServletRequest;
	}

	private HttpServletRequest _getHttpServletRequest(User user)
		throws Exception {

		return _getHttpServletRequest(null, user);
	}

	private User _getNonadminUserWithPermission(String actionKey)
		throws Exception {

		Role role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		RoleTestUtil.addResourcePermission(
			role, Layout.class.getName(), ResourceConstants.SCOPE_COMPANY,
			String.valueOf(_group.getCompanyId()), actionKey);

		User user = UserTestUtil.addUser();

		_roleLocalService.clearUserRoles(user.getUserId());

		_roleLocalService.addUserRole(user.getUserId(), role);

		return user;
	}

	private ThemeDisplay _getThemeDisplay(
			User user, HttpServletRequest mockHttpServletRequest)
		throws Exception {

		ThemeDisplay themeDisplay = new ThemeDisplay();

		Company company = CompanyLocalServiceUtil.getCompany(
			_group.getCompanyId());

		themeDisplay.setCompany(company);

		themeDisplay.setLanguageId(_group.getDefaultLanguageId());
		themeDisplay.setLayout(LayoutTestUtil.addTypePortletLayout(_group));
		themeDisplay.setLayoutSet(
			_layoutSetLocalService.getLayoutSet(_group.getGroupId(), false));
		themeDisplay.setLocale(
			LocaleUtil.fromLanguageId(_group.getDefaultLanguageId()));
		themeDisplay.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(user));
		themeDisplay.setPortalDomain(company.getVirtualHostname());
		themeDisplay.setPortalURL(company.getPortalURL(_group.getGroupId()));
		themeDisplay.setRequest(mockHttpServletRequest);
		themeDisplay.setScopeGroupId(_group.getGroupId());
		themeDisplay.setServerPort(8080);
		themeDisplay.setSignedIn(true);
		themeDisplay.setSiteGroupId(_group.getGroupId());
		themeDisplay.setUser(user);

		return themeDisplay;
	}

	private boolean _includeLayoutContentWithNonadminUser(
			String actionKey, Layout layout, String layoutMode)
		throws Exception {

		User user = _getNonadminUserWithPermission(actionKey);

		LayoutTypeController layoutTypeController =
			LayoutTypeControllerTracker.getLayoutTypeController(
				LayoutConstants.TYPE_CONTENT);

		return layoutTypeController.includeLayoutContent(
			_getHttpServletRequest(layoutMode, user),
			new MockHttpServletResponse(), layout);
	}

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject
	private LayoutSetLocalService _layoutSetLocalService;

	@Inject
	private RoleLocalService _roleLocalService;

	@Inject
	private UserLocalService _userLocalService;

}