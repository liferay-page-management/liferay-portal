/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.events.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.events.ServicePreAction;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.events.LifecycleEvent;
import com.liferay.portal.kernel.exception.LayoutPermissionException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.InstancePool;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.util.PropsUtil;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Preston Crary
 */
@RunWith(Arquillian.class)
public class ServicePreActionTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_company = CompanyTestUtil.addCompany();

		_safeCloseable = CompanyThreadLocal.setCompanyIdWithSafeCloseable(
			_company.getCompanyId());

		Role role = _roleLocalService.getRole(
			_company.getCompanyId(), RoleConstants.GUEST);

		_guestRoleId = role.getRoleId();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		_safeCloseable.close();

		UserTestUtil.setUser(
			UserTestUtil.getAdminUser(CompanyThreadLocal.getCompanyId()));

		CompanyLocalServiceUtil.deleteCompany(_company.getCompanyId());
	}

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroupToCompany(_company.getCompanyId());

		LayoutTestUtil.addTypePortletLayout(_group);

		LayoutTestUtil.addTypePortletLayout(
			_group.getGroupId(), "Page not visible", false, null, false, true);

		_mockHttpServletRequest.setAttribute(WebKeys.COMPANY, _company);
		_mockHttpServletRequest.setAttribute(
			WebKeys.VIRTUAL_HOST_LAYOUT_SET, _group.getPublicLayoutSet());
		_mockHttpServletRequest.setPathInfo("/portal/login");
		_mockHttpServletRequest.setRequestURI(
			_portal.getPathMain() + "/portal/login");
	}

	@Test
	public void testCustomErrorPage() throws Exception {
		try (SafeCloseable safeCloseable1 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"SERVLET_SERVICE_EVENTS_PRE",
					new String[] {TestLifecycleAction.class.getName()});
			SafeCloseable safeCloseable2 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"SERVLET_SERVICE_EVENTS_PRE_ERROR_PAGE",
					"/portal/portlet_error.jsp")) {

			_testErrorPage(true, false, false, "http://localhost:8080");
			_testErrorPage(true, false, false, "http://localhost:8080/c");
		}
	}

	@Test
	public void testCustomErrorPageFallback() throws Exception {
		try (SafeCloseable safeCloseable1 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"SERVLET_SERVICE_EVENTS_PRE",
					new String[] {TestLifecycleAction.class.getName()});
			SafeCloseable safeCloseable2 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"SERVLET_SERVICE_EVENTS_PRE_ERROR_PAGE",
					"/portal/portlet_error.jsp")) {

			_testErrorPage(false, true, false, "http://localhost:8080");
			_testErrorPage(false, true, false, "http://localhost:8080/c");
		}
	}

	@Test
	public void testErrorPage() throws Exception {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"SERVLET_SERVICE_EVENTS_PRE",
					new String[] {TestLifecycleAction.class.getName()})) {

			_testErrorPage(false, false, false, "http://localhost:8080");
			_testErrorPage(false, false, false, "http://localhost:8080/c");
		}
	}

	@Test
	public void testErrorPageWithServletJSPExceptionsIncluded()
		throws Exception {

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"SERVLET_SERVICE_EVENTS_PRE",
					new String[] {TestLifecycleAction.class.getName()})) {

			_testErrorPage(false, false, true, "http://localhost:8080");
			_testErrorPage(false, false, true, "http://localhost:8080/c");
		}
	}

	@Test
	public void testHiddenLayoutsVirtualHostLayoutCompositeWithNonexistentLayout()
		throws Exception {

		long plid = _getThemeDisplayPlid(true, false);

		Object viewableLayoutComposite = _getViewableLayoutComposite(
			"/nonexistent_page", false);

		Layout layout = _getLayout(viewableLayoutComposite);

		List<Layout> layouts = _getLayouts(viewableLayoutComposite);

		Assert.assertEquals(layout.getPlid(), plid);

		Assert.assertEquals(layouts.toString(), 1, layouts.size());
	}

	@Test
	public void testHiddenLayoutsVirtualHostLayoutCompositeWithoutPublicPath()
		throws Exception {

		long plid = _getThemeDisplayPlid(true, false);

		try (AutoCloseable autoCloseable =
				_setLayoutsToHiddenAndDefaultLayoutToNotGuestViewable(plid)) {

			Object viewableLayoutComposite = _getViewableLayoutComposite(
				"/non/public/path", true);

			Layout layout = _getLayout(viewableLayoutComposite);

			List<Layout> layouts = _getLayouts(viewableLayoutComposite);

			Assert.assertEquals(layout.getPlid(), plid);

			Assert.assertNull(layouts);

			Assert.assertTrue(
				SessionErrors.contains(
					_mockHttpServletRequest,
					LayoutPermissionException.class.getName()));
		}
	}

	@Test
	public void testHiddenLayoutsVirtualHostLayoutCompositeWithPublicPath()
		throws Exception {

		long plid = _getThemeDisplayPlid(true, false);

		try (AutoCloseable autoCloseable =
				_setLayoutsToHiddenAndDefaultLayoutToNotGuestViewable(plid)) {

			Object viewableLayoutComposite = _getViewableLayoutComposite(
				"/portal/saml/metadata", true);

			Layout layout = _getLayout(viewableLayoutComposite);

			List<Layout> layouts = _getLayouts(viewableLayoutComposite);

			Assert.assertEquals(layout.getPlid(), plid);

			Assert.assertNull(layouts);

			Assert.assertTrue(SessionErrors.isEmpty(_mockHttpServletRequest));
		}
	}

	@Test
	public void testInitServiceContextScopeGroupId() throws Exception {
		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		Assert.assertNotNull(serviceContext);
		Assert.assertNotEquals(
			_group.getGroupId(), serviceContext.getScopeGroupId());

		ThemeDisplay themeDisplay =
			(ThemeDisplay)_mockHttpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		Assert.assertNull(themeDisplay);

		_servicePreAction.servicePre(
			_mockHttpServletRequest, _mockHttpServletResponse, false);

		themeDisplay = (ThemeDisplay)_mockHttpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		Assert.assertNotNull(themeDisplay);
		Assert.assertEquals(
			_group.getGroupId(), themeDisplay.getScopeGroupId());

		serviceContext = ServiceContextThreadLocal.getServiceContext();

		Assert.assertNotNull(serviceContext);
		Assert.assertEquals(
			_group.getGroupId(), serviceContext.getScopeGroupId());
	}

	@Test
	public void testInitThemeDisplayPlidDefaultUserPersonalSiteLayoutComposite()
		throws Exception {

		try {
			long plid = _getThemeDisplayPlid(false, true);

			Layout layout = _getLayout(
				ReflectionTestUtil.invoke(
					_servicePreAction,
					"_getDefaultUserPersonalSiteLayoutComposite",
					new Class<?>[] {User.class}, _user));

			Assert.assertEquals(layout.getPlid(), plid);
		}
		finally {
			if (_user != null) {
				_userLocalService.deleteUser(_user);
			}
		}
	}

	@Test
	public void testInitThemeDisplayPlidDefaultUserSitesLayoutComposite()
		throws Exception {

		boolean publicLayoutsAutoCreate = PrefsPropsUtil.getBoolean(
			CompanyThreadLocal.getCompanyId(),
			PropsKeys.LAYOUT_USER_PUBLIC_LAYOUTS_AUTO_CREATE);
		boolean privateLayoutsAutoCreate = PrefsPropsUtil.getBoolean(
			CompanyThreadLocal.getCompanyId(),
			PropsKeys.LAYOUT_USER_PRIVATE_LAYOUTS_AUTO_CREATE);

		PropsUtil.set(
			PropsKeys.LAYOUT_USER_PUBLIC_LAYOUTS_AUTO_CREATE, "false");
		PropsUtil.set(
			PropsKeys.LAYOUT_USER_PRIVATE_LAYOUTS_AUTO_CREATE, "false");

		try {
			long plid = _getThemeDisplayPlid(false, true);

			Layout layout = _getLayout(
				ReflectionTestUtil.invoke(
					_servicePreAction, "_getDefaultUserSitesLayoutComposite",
					new Class<?>[] {User.class}, _user));

			Assert.assertEquals(layout.getPlid(), plid);
		}
		finally {
			PropsUtil.set(
				PropsKeys.LAYOUT_USER_PUBLIC_LAYOUTS_AUTO_CREATE,
				GetterUtil.getString(publicLayoutsAutoCreate));
			PropsUtil.set(
				PropsKeys.LAYOUT_USER_PRIVATE_LAYOUTS_AUTO_CREATE,
				GetterUtil.getString(privateLayoutsAutoCreate));

			if (_user != null) {
				_userLocalService.deleteUser(_user);
			}
		}
	}

	@Test
	public void testInitThemeDisplayPlidGuestSiteLayoutComposite()
		throws Exception {

		long plid = _getThemeDisplayPlid(false, false);

		Layout layout = _getLayout(
			ReflectionTestUtil.invoke(
				_servicePreAction, "_getGuestSiteLayoutComposite",
				new Class<?>[] {User.class}, _user));

		Assert.assertEquals(layout.getPlid(), plid);
	}

	@Test
	public void testInitThemeDisplayPlidVirtualHostLayoutComposite()
		throws Exception {

		long plid = _getThemeDisplayPlid(true, false);

		Layout layout = _getLayout(
			ReflectionTestUtil.invoke(
				_servicePreAction, "_getDefaultVirtualHostLayoutComposite",
				new Class<?>[] {HttpServletRequest.class},
				_mockHttpServletRequest));

		Assert.assertEquals(layout.getPlid(), plid);
	}

	private Layout _getLayout(Object layoutComposite) {
		return ReflectionTestUtil.invoke(
			layoutComposite, "getLayout", null, null);
	}

	private List<Layout> _getLayouts(Object layoutComposite) {
		return ReflectionTestUtil.invoke(
			layoutComposite, "getLayouts", null, null);
	}

	private long _getThemeDisplayPlid(
			boolean hasGuestViewPermission, boolean signedIn)
		throws Exception {

		if (!hasGuestViewPermission) {
			_resourcePermissionLocalService.removeResourcePermissions(
				_group.getCompanyId(), Layout.class.getName(),
				ResourceConstants.SCOPE_INDIVIDUAL, _guestRoleId,
				ActionKeys.VIEW);
		}

		try {
			if (signedIn) {
				_user = UserTestUtil.addUser(_company);
			}
			else {
				_user = _portal.initUser(_mockHttpServletRequest);
			}

			_mockHttpServletRequest.setAttribute(WebKeys.USER, _user);

			_servicePreAction.run(
				_mockHttpServletRequest, _mockHttpServletResponse);
		}
		finally {
			if (!hasGuestViewPermission) {
				for (Layout layout :
						_layoutLocalService.getLayouts(_group.getCompanyId())) {

					_resourcePermissionLocalService.setResourcePermissions(
						layout.getCompanyId(), Layout.class.getName(),
						ResourceConstants.SCOPE_INDIVIDUAL,
						String.valueOf(layout.getPrimaryKey()), _guestRoleId,
						new String[] {ActionKeys.VIEW});
				}
			}
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)_mockHttpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return themeDisplay.getPlid();
	}

	private Object _getViewableLayoutComposite(
		String requestURI, boolean prependPathMain) {

		_mockHttpServletRequest.setPathInfo(requestURI);

		if (prependPathMain) {
			_mockHttpServletRequest.setRequestURI(
				_portal.getPathMain() + requestURI);
		}
		else {
			_mockHttpServletRequest.setRequestURI(requestURI);
		}

		Object defaultLayoutComposite = ReflectionTestUtil.invoke(
			_servicePreAction, "_getDefaultVirtualHostLayoutComposite",
			new Class<?>[] {HttpServletRequest.class}, _mockHttpServletRequest);

		return ReflectionTestUtil.invoke(
			_servicePreAction, "_getViewableLayoutComposite",
			new Class<?>[] {
				HttpServletRequest.class, User.class, PermissionChecker.class,
				Layout.class, List.class, boolean.class
			},
			_mockHttpServletRequest, _user,
			_permissionCheckerFactory.create(_user),
			_getLayout(defaultLayoutComposite),
			_getLayouts(defaultLayoutComposite), false);
	}

	private AutoCloseable _setLayoutsToHiddenAndDefaultLayoutToNotGuestViewable(
			long defaultLayoutPlid)
		throws PortalException {

		_resourcePermissionLocalService.removeResourcePermission(
			_group.getCompanyId(), Layout.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(defaultLayoutPlid), _guestRoleId, ActionKeys.VIEW);

		List<Layout> updatedLayouts = new ArrayList<>();

		List<Layout> layouts = _layoutLocalService.getLayouts(
			_group.getCompanyId());

		for (Layout layout : layouts) {
			if (!layout.isHidden()) {
				layout.setHidden(true);

				updatedLayouts.add(_layoutLocalService.updateLayout(layout));
			}
		}

		return () -> {
			_resourcePermissionLocalService.setResourcePermissions(
				_group.getCompanyId(), Layout.class.getName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				String.valueOf(defaultLayoutPlid), _guestRoleId,
				new String[] {ActionKeys.VIEW});

			for (Layout layout : updatedLayouts) {
				layout.setHidden(false);

				_layoutLocalService.updateLayout(layout);
			}
		};
	}

	private void _testCustomErrorPage(String expectedMessage, String location)
		throws Exception {

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.portal.internal.servlet.MainServlet",
				LoggerTestUtil.ERROR)) {

			String content = _http.URLtoString(location);

			Assert.assertTrue(
				content.contains("Portlet is temporarily unavailable."));

			List<LogEntry> logEntries = logCapture.getLogEntries();

			LogEntry logEntry = logEntries.get(0);

			Throwable throwable = logEntry.getThrowable();

			Assert.assertEquals(expectedMessage, throwable.getMessage());
		}
	}

	private void _testErrorPage(
			boolean expectCustomErrorPage, boolean includeInvalidRenderPortlet,
			boolean includeServletJspExceptions, String location)
		throws Exception {

		InstancePool.put(
			TestLifecycleAction.class.getName(),
			new TestLifecycleAction(
				includeInvalidRenderPortlet, includeServletJspExceptions));

		String expectedMessage = ServicePreActionTest.class.getName();

		if (expectCustomErrorPage) {
			_testCustomErrorPage(expectedMessage, location);
		}
		else {
			_testErrorPage(expectedMessage, location);
		}
	}

	private void _testErrorPage(String expectedMessage, String location)
		throws Exception {

		try (LogCapture logCapture1 = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.portal.internal.servlet.MainServlet",
				LoggerTestUtil.ERROR);
			LogCapture logCapture2 = LoggerTestUtil.configureLog4JLogger(
				"portal_web.docroot.html.common.error_jsp",
				LoggerTestUtil.ERROR)) {

			String content = _http.URLtoString(location);

			Assert.assertTrue(content.contains(expectedMessage));
			Assert.assertTrue(
				content.contains("An unexpected system error occurred."));

			List<LogEntry> logEntries = logCapture1.getLogEntries();

			LogEntry logEntry = logEntries.get(0);

			Throwable throwable = logEntry.getThrowable();

			Assert.assertEquals(expectedMessage, throwable.getMessage());

			logEntries = logCapture2.getLogEntries();

			logEntry = logEntries.get(0);

			throwable = logEntry.getThrowable();

			Assert.assertEquals(expectedMessage, throwable.getMessage());
		}
	}

	private static Company _company;
	private static long _guestRoleId;

	@Inject
	private static RoleLocalService _roleLocalService;

	private static SafeCloseable _safeCloseable;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private Http _http;

	@Inject
	private LayoutLocalService _layoutLocalService;

	private final MockHttpServletRequest _mockHttpServletRequest =
		new MockHttpServletRequest();
	private final MockHttpServletResponse _mockHttpServletResponse =
		new MockHttpServletResponse();

	@Inject
	private PermissionCheckerFactory _permissionCheckerFactory;

	@Inject
	private Portal _portal;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	private final ServicePreAction _servicePreAction = new ServicePreAction();
	private User _user;

	@Inject
	private UserLocalService _userLocalService;

	private class TestLifecycleAction implements LifecycleAction {

		public TestLifecycleAction(
			boolean includeInvalidRenderPortlet,
			boolean includeServletJspExceptions) {

			_includeInvalidRenderPortlet = includeInvalidRenderPortlet;
			_includeServletJspExceptions = includeServletJspExceptions;
		}

		@Override
		public void processLifecycleEvent(LifecycleEvent lifecycleEvent)
			throws ActionException {

			HttpServletRequest httpServletRequest = lifecycleEvent.getRequest();

			if (_includeInvalidRenderPortlet) {
				httpServletRequest.setAttribute(
					WebKeys.RENDER_PORTLET, ServicePreActionTest.class);
			}

			if (_includeServletJspExceptions) {
				Exception exception = new ActionException(
					ServicePreActionTest.class.getName() + "_JspException");

				httpServletRequest.setAttribute(
					"javax.servlet.error.exception", exception);
				httpServletRequest.setAttribute(
					"javax.servlet.jsp.jspException", exception);
			}

			throw new ActionException(ServicePreActionTest.class.getName());
		}

		private final boolean _includeInvalidRenderPortlet;
		private final boolean _includeServletJspExceptions;

	}

}