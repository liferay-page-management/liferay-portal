/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.instances.web.internal.taglib.util;

import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.instance.PortalInstancePool;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.portlet.PortletURL;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Mariano Álvaro Sáiz
 */
public class CompanyActionDropdownItemsTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		Mockito.when(
			_company.getCompanyId()
		).thenReturn(
			2L
		);

		_dbManagerUtilMockedStatic.when(
			DBManagerUtil::getDB
		).thenReturn(
			_db
		);

		Mockito.when(
			_liferayPortletResponse.createActionURL()
		).thenReturn(
			_portletURL
		);

		Mockito.when(
			_liferayPortletResponse.createRenderURL()
		).thenReturn(
			_portletURL
		);

		_portalInstancePoolMockedStatic.when(
			PortalInstancePool::getDefaultCompanyId
		).thenReturn(
			1L
		);
	}

	@After
	public void tearDown() {
		_dbManagerUtilMockedStatic.close();
		_languageUtilMockedStatic.close();
		_portalInstancePoolMockedStatic.close();
		_portalUtilMockedStatic.close();
	}

	@Test
	public void testGetActionDropdownItems() {
		Mockito.when(
			_db.isSupportsDBPartition()
		).thenReturn(
			true
		);

		Assert.assertTrue(_hasExportDropdownItem());

		Mockito.when(
			_db.isSupportsDBPartition()
		).thenReturn(
			false
		);

		Assert.assertFalse(_hasExportDropdownItem());
	}

	private boolean _hasExportDropdownItem() {
		CompanyActionDropdownItems companyActionDropdownItems =
			new CompanyActionDropdownItems(
				_company, _httpServletRequest, _liferayPortletResponse);

		for (DropdownItem dropdownGroupItem :
				companyActionDropdownItems.getActionDropdownItems()) {

			for (DropdownItem dropdownItem :
					(List<DropdownItem>)dropdownGroupItem.get("items")) {

				Map<String, Object> data =
					(Map<String, Object>)dropdownItem.get("data");

				if ((data != null) && data.containsKey("exportURL")) {
					return true;
				}
			}
		}

		return false;
	}

	private final Company _company = Mockito.mock(Company.class);
	private final DB _db = Mockito.mock(DB.class);
	private final MockedStatic<DBManagerUtil> _dbManagerUtilMockedStatic =
		Mockito.mockStatic(DBManagerUtil.class);
	private final HttpServletRequest _httpServletRequest = Mockito.mock(
		HttpServletRequest.class);
	private final MockedStatic<LanguageUtil> _languageUtilMockedStatic =
		Mockito.mockStatic(LanguageUtil.class);
	private final LiferayPortletResponse _liferayPortletResponse = Mockito.mock(
		LiferayPortletResponse.class);
	private final MockedStatic<PortalInstancePool>
		_portalInstancePoolMockedStatic = Mockito.mockStatic(
			PortalInstancePool.class);
	private final MockedStatic<PortalUtil> _portalUtilMockedStatic =
		Mockito.mockStatic(PortalUtil.class);
	private final PortletURL _portletURL = Mockito.mock(PortletURL.class);

}