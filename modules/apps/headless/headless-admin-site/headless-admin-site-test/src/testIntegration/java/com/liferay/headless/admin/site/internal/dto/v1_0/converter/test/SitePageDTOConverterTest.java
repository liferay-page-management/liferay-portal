/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.dto.v1_0.converter.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.exportimport.kernel.lar.ExportImportThreadLocal;
import com.liferay.headless.admin.site.dto.v1_0.ItemExternalReference;
import com.liferay.headless.admin.site.dto.v1_0.OpenGraphSettings;
import com.liferay.headless.admin.site.dto.v1_0.SitePage;
import com.liferay.headless.admin.site.dto.v1_0.WidgetPageSettings;
import com.liferay.layout.seo.service.LayoutSEOEntryLocalService;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.scope.Scope;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Brian Wulbern
 */
@RunWith(Arquillian.class)
public class SitePageDTOConverterTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_depotEntry = _depotEntryLocalService.addDepotEntry(
			Collections.singletonMap(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()),
			Collections.singletonMap(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()),
			DepotConstants.TYPE_ASSET_LIBRARY,
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));
	}

	@Test
	@TestInfo("LPD-110538")
	public void testToDTOWithRemoteStagingOpenGraphImageScope()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypePortletLayout(_group);

		Group depotEntryGroup = _depotEntry.getGroup();

		_layoutSEOEntryLocalService.updateLayoutSEOEntry(
			TestPropsValues.getUserId(), _group.getGroupId(), false,
			layout.getLayoutId(), false, Collections.emptyMap(),
			Collections.emptyMap(), RandomTestUtil.randomString(),
			depotEntryGroup.getExternalReferenceCode(), false,
			Collections.emptyMap(),
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		String remoteGroupExternalReferenceCode = RandomTestUtil.randomString();

		UnicodeProperties typeSettingsUnicodeProperties =
			depotEntryGroup.getTypeSettingsProperties();

		typeSettingsUnicodeProperties.setProperty(
			"remoteGroupExternalReferenceCode",
			remoteGroupExternalReferenceCode);
		typeSettingsUnicodeProperties.setProperty("staged", "true");
		typeSettingsUnicodeProperties.setProperty("stagedRemotely", "true");

		_groupLocalService.updateGroup(
			depotEntryGroup.getGroupId(),
			typeSettingsUnicodeProperties.toString());

		Scope scope = _getOpenGraphImageScope(layout);

		Assert.assertEquals(
			depotEntryGroup.getExternalReferenceCode(),
			scope.getExternalReferenceCode());
		Assert.assertEquals(Scope.Type.ASSET_LIBRARY, scope.getType());

		ExportImportThreadLocal.setPortletStagingInProcess(true);

		try {
			scope = _getOpenGraphImageScope(layout);
		}
		finally {
			ExportImportThreadLocal.setPortletStagingInProcess(false);
		}

		Assert.assertEquals(
			remoteGroupExternalReferenceCode, scope.getExternalReferenceCode());
		Assert.assertEquals(Scope.Type.ASSET_LIBRARY, scope.getType());
	}

	private Scope _getOpenGraphImageScope(Layout layout) throws Exception {
		SitePage sitePage = _dtoConverter.toDTO(
			new DefaultDTOConverterContext(
				_dtoConverterRegistry, layout.getPlid(),
				LocaleUtil.getDefault(), null, TestPropsValues.getUser()),
			layout);

		WidgetPageSettings widgetPageSettings =
			(WidgetPageSettings)sitePage.getPageSettings();

		OpenGraphSettings openGraphSettings =
			widgetPageSettings.getOpenGraphSettings();

		ItemExternalReference itemExternalReference =
			openGraphSettings.getImage();

		return itemExternalReference.getScope();
	}

	@DeleteAfterTestRun
	private DepotEntry _depotEntry;

	@Inject
	private DepotEntryLocalService _depotEntryLocalService;

	@Inject(
		filter = "component.name=com.liferay.headless.admin.site.internal.dto.v1_0.converter.SitePageDTOConverter"
	)
	private DTOConverter<Layout, SitePage> _dtoConverter;

	@Inject
	private DTOConverterRegistry _dtoConverterRegistry;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private LayoutSEOEntryLocalService _layoutSEOEntryLocalService;

}