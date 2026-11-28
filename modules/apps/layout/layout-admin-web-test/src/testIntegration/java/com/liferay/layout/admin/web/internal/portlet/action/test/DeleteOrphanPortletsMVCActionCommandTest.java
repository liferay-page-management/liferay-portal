/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.admin.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.journal.constants.JournalContentPortletKeys;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.PortletPreferencesLocalService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import jakarta.portlet.PortletException;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Georgel Pop
 */
@RunWith(Arquillian.class)
public class DeleteOrphanPortletsMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testDeleteOrphanPortlets() throws Exception {
		Group group = GroupTestUtil.addGroup();

		Layout layout = LayoutTestUtil.addTypePortletLayout(group);

		String portletId = _addPortletToLayout(layout);

		_deleteOrphanPortletsMVCActionCommand.processAction(
			_getMockLiferayPortletActionRequest(
				layout, portletId, UserTestUtil.addGroupAdminUser(group)),
			new MockLiferayPortletActionResponse());

		Assert.assertEquals(
			0,
			_portletPreferencesLocalService.getPortletPreferencesCount(
				PortletKeys.PREFS_OWNER_TYPE_LAYOUT, layout.getPlid(),
				portletId));
	}

	@Test
	@TestInfo("LPD-116136")
	public void testDeleteOrphanPortletsWithoutPermissions() throws Exception {
		Layout layout = LayoutTestUtil.addTypePortletLayout(
			TestPropsValues.getGroupId());

		String portletId = _addPortletToLayout(layout);

		Group group = GroupTestUtil.addGroup();

		Exception exception = Assert.assertThrows(
			PortletException.class,
			() -> _deleteOrphanPortletsMVCActionCommand.processAction(
				_getMockLiferayPortletActionRequest(
					layout, portletId, UserTestUtil.addGroupAdminUser(group)),
				new MockLiferayPortletActionResponse()));

		Assert.assertTrue(exception.getCause() instanceof PrincipalException);

		Assert.assertEquals(
			1,
			_portletPreferencesLocalService.getPortletPreferencesCount(
				PortletKeys.PREFS_OWNER_TYPE_LAYOUT, layout.getPlid(),
				portletId));
	}

	private String _addPortletToLayout(Layout layout) throws Exception {
		return LayoutTestUtil.addPortletToLayout(
			layout, JournalContentPortletKeys.JOURNAL_CONTENT,
			HashMapBuilder.put(
				RandomTestUtil.randomString(),
				new String[] {RandomTestUtil.randomString()}
			).build());
	}

	private MockLiferayPortletActionRequest _getMockLiferayPortletActionRequest(
			Layout layout, String portletId, User user)
		throws Exception {

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.fetchCompany(user.getCompanyId()));
		themeDisplay.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(user));

		mockLiferayPortletActionRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		mockLiferayPortletActionRequest.setParameter("portletId", portletId);
		mockLiferayPortletActionRequest.setParameter(
			"selPlid", String.valueOf(layout.getPlid()));

		return mockLiferayPortletActionRequest;
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject(filter = "mvc.command.name=/layout_admin/delete_orphan_portlets")
	private MVCActionCommand _deleteOrphanPortletsMVCActionCommand;

	@Inject
	private PortletPreferencesLocalService _portletPreferencesLocalService;

}