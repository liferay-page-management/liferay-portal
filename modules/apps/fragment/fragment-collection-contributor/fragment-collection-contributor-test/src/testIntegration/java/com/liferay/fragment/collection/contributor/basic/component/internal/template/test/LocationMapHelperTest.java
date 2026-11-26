/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.collection.contributor.basic.component.internal.template.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Sandro Chinea
 */
@RunWith(Arquillian.class)
public class LocationMapHelperTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@After
	public void tearDown() throws Exception {
		_companyLocalService.removePreferences(
			TestPropsValues.getCompanyId(),
			new String[] {"MAP_PROVIDER_KEY", "googleMapsAPIKey"});
	}

	@Test
	public void testGetGoogleMapsAPIKey() throws Exception {
		Assert.assertEquals(StringPool.BLANK, _getGoogleMapsAPIKey());

		String companyGoogleMapsAPIKey = RandomTestUtil.randomString();

		_updatePreferences("googleMapsAPIKey", companyGoogleMapsAPIKey);

		Assert.assertEquals(companyGoogleMapsAPIKey, _getGoogleMapsAPIKey());

		String groupGoogleMapsAPIKey = RandomTestUtil.randomString();

		_updateTypeSettingsProperty("googleMapsAPIKey", groupGoogleMapsAPIKey);

		Assert.assertEquals(groupGoogleMapsAPIKey, _getGoogleMapsAPIKey());
	}

	@Test
	public void testGetMapProviderKey() throws Exception {
		Assert.assertEquals("OpenStreetMap", _getMapProviderKey());

		String companyMapProviderKey = RandomTestUtil.randomString();

		_updatePreferences("MAP_PROVIDER_KEY", companyMapProviderKey);

		Assert.assertEquals(companyMapProviderKey, _getMapProviderKey());

		String groupMapProviderKey = RandomTestUtil.randomString();

		_updateTypeSettingsProperty("MAP_PROVIDER_KEY", groupMapProviderKey);

		Assert.assertEquals(groupMapProviderKey, _getMapProviderKey());
	}

	private String _getGoogleMapsAPIKey() {
		return ReflectionTestUtil.invoke(
			_locationMapHelper, "getGoogleMapsAPIKey",
			new Class<?>[] {long.class}, _group.getGroupId());
	}

	private String _getMapProviderKey() {
		return ReflectionTestUtil.invoke(
			_locationMapHelper, "getMapProviderKey", new Class<?>[] {long.class},
			_group.getGroupId());
	}

	private void _updatePreferences(String key, String value)
		throws Exception {

		_companyLocalService.updatePreferences(
			TestPropsValues.getCompanyId(),
			UnicodePropertiesBuilder.create(
				true
			).put(
				key, value
			).build());
	}

	private void _updateTypeSettingsProperty(String key, String value)
		throws Exception {

		UnicodeProperties typeSettingsUnicodeProperties =
			_group.getTypeSettingsProperties();

		typeSettingsUnicodeProperties.put(key, value);

		_group = _groupLocalService.updateGroup(
			_group.getGroupId(), typeSettingsUnicodeProperties.toString());
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject(
		filter = "component.name=com.liferay.fragment.collection.contributor.basic.component.internal.template.LocationMapHelper",
		type = Inject.NoType.class
	)
	private Object _locationMapHelper;

}