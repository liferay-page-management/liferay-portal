/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.model.DepotEntryGroupRel;
import com.liferay.depot.service.DepotEntryGroupRelLocalService;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.fragment.constants.FragmentPortletKeys;
import com.liferay.fragment.exception.InvalidPropagationTargetGroupException;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.fragment.test.util.FragmentEntryTestUtil;
import com.liferay.fragment.test.util.FragmentTestUtil;
import com.liferay.layout.manager.LayoutLockManager;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.LiferayPortletConfig;
import com.liferay.portal.kernel.portlet.PortletConfigFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.PortletLocalService;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.ScopeUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.segments.service.SegmentsExperienceLocalService;

import jakarta.portlet.ActionRequest;
import jakarta.portlet.ActionResponse;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jürgen Kappler
 */
@RunWith(Arquillian.class)
@Sync
public class PropagateGroupFragmentEntryChangesMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = _groupLocalService.fetchGroup(TestPropsValues.getGroupId());

		Group companyGroup = _groupLocalService.getCompanyGroup(
			TestPropsValues.getCompanyId());

		FragmentCollection fragmentCollection =
			FragmentTestUtil.addFragmentCollection(_group.getGroupId());

		FragmentCollection globalFragmentCollection =
			FragmentTestUtil.addFragmentCollection(companyGroup.getGroupId());

		_fragmentEntry = FragmentEntryTestUtil.addFragmentEntry(
			fragmentCollection.getFragmentCollectionId());

		_globalFragmentEntry = FragmentEntryTestUtil.addFragmentEntry(
			globalFragmentCollection.getFragmentCollectionId());
	}

	@Test
	public void testAddFragmentEntryLink() throws Exception {
		_testAddFragmentEntryLink(_fragmentEntry);
		_testAddFragmentEntryLink(_globalFragmentEntry);
	}

	@Test
	public void testPropagateChanges() throws Exception {
		_testPropagateChangesAddsSessionErrorWhenNoGroupIsValid();
		_testPropagateChangesInMixedBatchSkipsInvalidGroup();
		_testPropagateChangesOfFragmentEntryToLockedContentLayout(
			_fragmentEntry);
		_testPropagateChangesOfFragmentEntryToLockedContentLayout(
			_globalFragmentEntry);
		_testPropagateChangesToDepotOwnGroup();
	}

	private DepotEntry _addDepotEntry() throws Exception {
		return _depotEntryLocalService.addDepotEntry(
			HashMapBuilder.put(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()
			).build(),
			Collections.emptyMap(), DepotConstants.TYPE_DESIGN_LIBRARY,
			ServiceContextTestUtil.getServiceContext());
	}

	private FragmentEntry _addFragmentEntry(long groupId) throws Exception {
		FragmentCollection fragmentCollection =
			FragmentTestUtil.addFragmentCollection(groupId);

		return FragmentEntryTestUtil.addFragmentEntry(
			fragmentCollection.getFragmentCollectionId());
	}

	private FragmentEntryLink _addFragmentEntryLink(
			FragmentEntry fragmentEntry, Group group, Layout layout)
		throws Exception {

		return _fragmentEntryLinkLocalService.addFragmentEntryLink(
			null, TestPropsValues.getUserId(), group.getGroupId(), null,
			fragmentEntry.getExternalReferenceCode(),
			ScopeUtil.getItemScopeExternalReferenceCode(
				fragmentEntry.getGroupId(), group.getGroupId()),
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperienceId(
				layout.getPlid()),
			layout.getPlid(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			"{fieldSets: []}", StringPool.BLANK, StringPool.BLANK, 0, null,
			fragmentEntry.getType(),
			ServiceContextTestUtil.getServiceContext(
				group, TestPropsValues.getUserId()));
	}

	private void _assertFragmentEntryLinkContent(
		String css, FragmentEntryLink fragmentEntryLink, String html,
		String js) {

		FragmentEntryLink persistedFragmentEntryLink =
			_fragmentEntryLinkLocalService.fetchFragmentEntryLink(
				fragmentEntryLink.getFragmentEntryLinkId());

		Assert.assertEquals(css, persistedFragmentEntryLink.getCss());
		Assert.assertEquals(html, persistedFragmentEntryLink.getHtml());
		Assert.assertEquals(js, persistedFragmentEntryLink.getJs());
	}

	private MockLiferayPortletActionRequest _getMockLiferayPortletActionRequest(
			FragmentEntry fragmentEntry, Layout layout)
		throws Exception {

		return _getMockLiferayPortletActionRequest(
			fragmentEntry, layout, new long[] {_group.getGroupId()});
	}

	private MockLiferayPortletActionRequest _getMockLiferayPortletActionRequest(
			FragmentEntry fragmentEntry, Layout layout, long[] rowIds)
		throws Exception {

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		mockLiferayPortletActionRequest.setAttribute(
			JavaConstants.JAKARTA_PORTLET_RESPONSE,
			new MockLiferayPortletActionResponse());

		Portlet portlet = _portletLocalService.getPortletById(
			FragmentPortletKeys.FRAGMENT);

		LiferayPortletConfig liferayPortletConfig =
			(LiferayPortletConfig)PortletConfigFactoryUtil.create(
				portlet, null);

		mockLiferayPortletActionRequest.setAttribute(
			JavaConstants.JAKARTA_PORTLET_CONFIG, liferayPortletConfig);

		mockLiferayPortletActionRequest.setAttribute(
			WebKeys.THEME_DISPLAY, _getThemeDisplay());
		mockLiferayPortletActionRequest.setParameter(
			"segmentsExperienceId",
			String.valueOf(
				_segmentsExperienceLocalService.
					fetchDefaultSegmentsExperienceId(layout.getPlid())));
		mockLiferayPortletActionRequest.setParameter(
			"fragmentEntryERC", fragmentEntry.getExternalReferenceCode());
		mockLiferayPortletActionRequest.setParameter(
			"fragmentEntryGroupId", String.valueOf(fragmentEntry.getGroupId()));

		String[] rowIdsStrings = TransformUtil.transform(
			rowIds, String::valueOf, String.class);

		mockLiferayPortletActionRequest.setParameter("rowIds", rowIdsStrings);

		return mockLiferayPortletActionRequest;
	}

	private ThemeDisplay _getThemeDisplay() throws Exception {
		ThemeDisplay themeDisplay = new ThemeDisplay();

		Company company = _companyLocalService.getCompany(
			_group.getCompanyId());

		themeDisplay.setCompany(company);

		Layout controlPanelLayout = _layoutLocalService.getLayout(
			_portal.getControlPanelPlid(company.getCompanyId()));

		themeDisplay.setLayout(controlPanelLayout);

		LayoutSet layoutSet = _group.getPublicLayoutSet();

		themeDisplay.setLookAndFeel(layoutSet.getTheme(), null);

		themeDisplay.setPermissionChecker(
			PermissionThreadLocal.getPermissionChecker());
		themeDisplay.setRealUser(TestPropsValues.getUser());
		themeDisplay.setScopeGroupId(_group.getGroupId());
		themeDisplay.setSiteGroupId(_group.getGroupId());
		themeDisplay.setUser(TestPropsValues.getUser());

		return themeDisplay;
	}

	private void _testAddFragmentEntryLink(FragmentEntry fragmentEntry)
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		FragmentEntryLink fragmentEntryLink =
			_fragmentEntryLinkLocalService.addFragmentEntryLink(
				null, TestPropsValues.getUserId(), _group.getGroupId(), null,
				fragmentEntry.getExternalReferenceCode(),
				ScopeUtil.getItemScopeExternalReferenceCode(
					fragmentEntry.getGroupId(), _group.getGroupId()),
				_segmentsExperienceLocalService.
					fetchDefaultSegmentsExperienceId(layout.getPlid()),
				layout.getPlid(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				"{fieldSets: []}", StringPool.BLANK, StringPool.BLANK, 0, null,
				fragmentEntry.getType(),
				ServiceContextTestUtil.getServiceContext(
					_group, TestPropsValues.getUserId()));

		fragmentEntry.setCss(RandomTestUtil.randomString());
		fragmentEntry.setHtml(RandomTestUtil.randomString());
		fragmentEntry.setJs(RandomTestUtil.randomString());

		fragmentEntry = _fragmentEntryLocalService.updateFragmentEntry(
			fragmentEntry);

		ReflectionTestUtil.invoke(
			_mvcActionCommand, "processAction",
			new Class<?>[] {ActionRequest.class, ActionResponse.class},
			_getMockLiferayPortletActionRequest(fragmentEntry, layout),
			new MockLiferayPortletActionResponse());

		FragmentEntryLink persistedFragmentEntryLink =
			_fragmentEntryLinkLocalService.fetchFragmentEntryLink(
				fragmentEntryLink.getFragmentEntryLinkId());

		Assert.assertEquals(
			fragmentEntry.getCss(), persistedFragmentEntryLink.getCss());
		Assert.assertEquals(
			fragmentEntry.getHtml(), persistedFragmentEntryLink.getHtml());
		Assert.assertEquals(
			fragmentEntry.getJs(), persistedFragmentEntryLink.getJs());

		_layoutLocalService.deleteLayout(layout);
	}

	private void _testPropagateChangesAddsSessionErrorWhenNoGroupIsValid()
		throws Exception {

		DepotEntry depotEntry = _addDepotEntry();
		Group disconnectedGroup = GroupTestUtil.addGroup();

		try {
			FragmentEntry fragmentEntry = _addFragmentEntry(
				depotEntry.getGroupId());

			Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

			fragmentEntry = _updateFragmentEntry(fragmentEntry);

			MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
				_getMockLiferayPortletActionRequest(
					fragmentEntry, layout,
					new long[] {disconnectedGroup.getGroupId()});

			boolean success = ReflectionTestUtil.invoke(
				_mvcActionCommand, "processAction",
				new Class<?>[] {ActionRequest.class, ActionResponse.class},
				mockLiferayPortletActionRequest,
				new MockLiferayPortletActionResponse());

			Assert.assertFalse(success);

			Assert.assertTrue(
				SessionErrors.contains(
					mockLiferayPortletActionRequest,
					InvalidPropagationTargetGroupException.class));

			_layoutLocalService.deleteLayout(layout);
		}
		finally {
			_depotEntryLocalService.deleteDepotEntry(depotEntry);
			_groupLocalService.deleteGroup(disconnectedGroup);
		}
	}

	private void _testPropagateChangesInMixedBatchSkipsInvalidGroup()
		throws Exception {

		Group connectedGroup = GroupTestUtil.addGroup();
		DepotEntry depotEntry = _addDepotEntry();
		Group disconnectedGroup = GroupTestUtil.addGroup();

		try {
			FragmentEntry fragmentEntry = _addFragmentEntry(
				depotEntry.getGroupId());

			_depotEntryGroupRelLocalService.addDepotEntryGroupRel(
				depotEntry.getDepotEntryId(), connectedGroup.getGroupId());

			Layout connectedLayout = LayoutTestUtil.addTypeContentLayout(
				connectedGroup);

			FragmentEntryLink connectedFragmentEntryLink =
				_addFragmentEntryLink(
					fragmentEntry, connectedGroup, connectedLayout);

			_depotEntryGroupRelLocalService.addDepotEntryGroupRel(
				depotEntry.getDepotEntryId(), disconnectedGroup.getGroupId());

			Layout disconnectedLayout = LayoutTestUtil.addTypeContentLayout(
				disconnectedGroup);

			FragmentEntryLink disconnectedFragmentEntryLink =
				_addFragmentEntryLink(
					fragmentEntry, disconnectedGroup, disconnectedLayout);

			String originalDisconnectedCss =
				disconnectedFragmentEntryLink.getCss();
			String originalDisconnectedHtml =
				disconnectedFragmentEntryLink.getHtml();
			String originalDisconnectedJs =
				disconnectedFragmentEntryLink.getJs();

			fragmentEntry = _updateFragmentEntry(fragmentEntry);

			// Disconnect after the propagate dialog would have opened, to
			// mirror the exact stale tab race reported in the ticket

			DepotEntryGroupRel disconnectedDepotEntryGroupRel =
				_depotEntryGroupRelLocalService.
					fetchDepotEntryGroupRelByDepotEntryIdToGroupId(
						depotEntry.getDepotEntryId(),
						disconnectedGroup.getGroupId());

			_depotEntryGroupRelLocalService.deleteDepotEntryGroupRel(
				disconnectedDepotEntryGroupRel);

			boolean success = ReflectionTestUtil.invoke(
				_mvcActionCommand, "processAction",
				new Class<?>[] {ActionRequest.class, ActionResponse.class},
				_getMockLiferayPortletActionRequest(
					fragmentEntry, connectedLayout,
					new long[] {
						connectedGroup.getGroupId(),
						disconnectedGroup.getGroupId()
					}),
				new MockLiferayPortletActionResponse());

			Assert.assertTrue(success);

			_assertFragmentEntryLinkContent(
				fragmentEntry.getCss(), connectedFragmentEntryLink,
				fragmentEntry.getHtml(), fragmentEntry.getJs());

			_assertFragmentEntryLinkContent(
				originalDisconnectedCss, disconnectedFragmentEntryLink,
				originalDisconnectedHtml, originalDisconnectedJs);

			_layoutLocalService.deleteLayout(connectedLayout);
			_layoutLocalService.deleteLayout(disconnectedLayout);
		}
		finally {
			_depotEntryLocalService.deleteDepotEntry(depotEntry);
			_groupLocalService.deleteGroup(connectedGroup);
			_groupLocalService.deleteGroup(disconnectedGroup);
		}
	}

	private void _testPropagateChangesOfFragmentEntryToLockedContentLayout(
			FragmentEntry fragmentEntry)
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		FragmentEntryLink fragmentEntryLink =
			_fragmentEntryLinkLocalService.addFragmentEntryLink(
				null, TestPropsValues.getUserId(), _group.getGroupId(), null,
				fragmentEntry.getExternalReferenceCode(),
				ScopeUtil.getItemScopeExternalReferenceCode(
					fragmentEntry.getGroupId(), _group.getGroupId()),
				_segmentsExperienceLocalService.
					fetchDefaultSegmentsExperienceId(layout.getPlid()),
				layout.getPlid(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				"{fieldSets: []}", StringPool.BLANK, StringPool.BLANK, 0, null,
				fragmentEntry.getType(),
				ServiceContextTestUtil.getServiceContext(
					_group, TestPropsValues.getUserId()));

		fragmentEntry.setCss(RandomTestUtil.randomString());
		fragmentEntry.setHtml(RandomTestUtil.randomString());
		fragmentEntry.setJs(RandomTestUtil.randomString());

		fragmentEntry = _fragmentEntryLocalService.updateFragmentEntry(
			fragmentEntry);

		Layout draftLayout = layout.fetchDraftLayout();

		Assert.assertNotNull(draftLayout);

		User user = UserTestUtil.getAdminUser(_group.getCompanyId());

		_layoutLockManager.getLock(draftLayout, user.getUserId());

		ReflectionTestUtil.invoke(
			_mvcActionCommand, "processAction",
			new Class<?>[] {ActionRequest.class, ActionResponse.class},
			_getMockLiferayPortletActionRequest(fragmentEntry, layout),
			new MockLiferayPortletActionResponse());

		FragmentEntryLink persistedFragmentEntryLink =
			_fragmentEntryLinkLocalService.fetchFragmentEntryLink(
				fragmentEntryLink.getFragmentEntryLinkId());

		Assert.assertEquals(
			fragmentEntry.getCss(), persistedFragmentEntryLink.getCss());
		Assert.assertEquals(
			fragmentEntry.getHtml(), persistedFragmentEntryLink.getHtml());
		Assert.assertEquals(
			fragmentEntry.getJs(), persistedFragmentEntryLink.getJs());

		_layoutLocalService.deleteLayout(layout);
	}

	private void _testPropagateChangesToDepotOwnGroup() throws Exception {
		DepotEntry depotEntry = _addDepotEntry();

		Group depotGroup = _groupLocalService.getGroup(depotEntry.getGroupId());

		try {
			FragmentEntry fragmentEntry = _addFragmentEntry(
				depotEntry.getGroupId());

			Layout layout = LayoutTestUtil.addTypeContentLayout(depotGroup);

			FragmentEntryLink fragmentEntryLink = _addFragmentEntryLink(
				fragmentEntry, depotGroup, layout);

			fragmentEntry = _updateFragmentEntry(fragmentEntry);

			ReflectionTestUtil.invoke(
				_mvcActionCommand, "processAction",
				new Class<?>[] {ActionRequest.class, ActionResponse.class},
				_getMockLiferayPortletActionRequest(
					fragmentEntry, layout,
					new long[] {depotGroup.getGroupId()}),
				new MockLiferayPortletActionResponse());

			_assertFragmentEntryLinkContent(
				fragmentEntry.getCss(), fragmentEntryLink,
				fragmentEntry.getHtml(), fragmentEntry.getJs());

			_layoutLocalService.deleteLayout(layout);
		}
		finally {
			_depotEntryLocalService.deleteDepotEntry(depotEntry);
		}
	}

	private FragmentEntry _updateFragmentEntry(FragmentEntry fragmentEntry)
		throws Exception {

		fragmentEntry.setCss(RandomTestUtil.randomString());
		fragmentEntry.setHtml(RandomTestUtil.randomString());
		fragmentEntry.setJs(RandomTestUtil.randomString());

		return _fragmentEntryLocalService.updateFragmentEntry(fragmentEntry);
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private DepotEntryGroupRelLocalService _depotEntryGroupRelLocalService;

	@Inject
	private DepotEntryLocalService _depotEntryLocalService;

	private FragmentEntry _fragmentEntry;

	@Inject
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@Inject
	private FragmentEntryLocalService _fragmentEntryLocalService;

	private FragmentEntry _globalFragmentEntry;
	private Group _group;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject
	private LayoutLockManager _layoutLockManager;

	@Inject(
		filter = "mvc.command.name=/fragment/propagate_group_fragment_entry_changes"
	)
	private MVCActionCommand _mvcActionCommand;

	@Inject
	private Portal _portal;

	@Inject
	private PortletLocalService _portletLocalService;

	@Inject
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

}