/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.internal.resource.v1_0;

import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.headless.delivery.internal.dto.v1_0.util.DisplayPageRendererUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleService;
import com.liferay.layout.util.LayoutServiceContextHelper;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Mariano Alvaro
 */
public class StructuredContentResourceImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@AfterClass
	public static void tearDownClass() {
		_displayPageRendererUtilMockedStatic.close();
	}

	@Test
	@TestInfo("LPD-92750")
	public void testGetStructuredContentRenderedContentByDisplayPageDisplayPageKey()
		throws Exception {

		StructuredContentResourceImpl structuredContentResourceImpl =
			new StructuredContentResourceImpl();

		structuredContentResourceImpl.setContextHttpServletRequest(
			Mockito.mock(HttpServletRequest.class));

		JournalArticle journalArticle = Mockito.mock(JournalArticle.class);

		Mockito.when(
			journalArticle.getDDMStructure()
		).thenReturn(
			Mockito.mock(DDMStructure.class)
		);

		JournalArticleService journalArticleService = Mockito.mock(
			JournalArticleService.class);

		Mockito.when(
			journalArticleService.getLatestArticle(Mockito.anyLong())
		).thenReturn(
			journalArticle
		);

		ReflectionTestUtil.setFieldValue(
			structuredContentResourceImpl, "_journalArticleService",
			journalArticleService);

		LayoutServiceContextHelper layoutServiceContextHelper = Mockito.mock(
			LayoutServiceContextHelper.class);

		Mockito.when(
			layoutServiceContextHelper.getServiceContextAutoCloseable(
				Mockito.any(Company.class), Mockito.any(User.class))
		).thenReturn(
			() -> {
			}
		);

		ReflectionTestUtil.setFieldValue(
			structuredContentResourceImpl, "_layoutServiceContextHelper",
			layoutServiceContextHelper);

		HttpServletRequest swappedHttpServletRequest = Mockito.mock(
			HttpServletRequest.class);

		Portal portal = Mockito.mock(Portal.class);

		Mockito.when(
			portal.getOriginalServletRequest(swappedHttpServletRequest)
		).thenReturn(
			swappedHttpServletRequest
		);

		ReflectionTestUtil.setFieldValue(
			structuredContentResourceImpl, "_portal", portal);

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setRequest(swappedHttpServletRequest);

		ServiceContextThreadLocal.pushServiceContext(serviceContext);

		try {
			structuredContentResourceImpl.
				getStructuredContentRenderedContentByDisplayPageDisplayPageKey(
					RandomTestUtil.randomLong(), RandomTestUtil.randomString());

			_displayPageRendererUtilMockedStatic.verify(
				() -> DisplayPageRendererUtil.toHTML(
					Mockito.anyString(), Mockito.anyLong(), Mockito.anyString(),
					Mockito.anyLong(), Mockito.same(swappedHttpServletRequest),
					Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
					Mockito.any(), Mockito.any()));
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
		}
	}

	private static final MockedStatic<DisplayPageRendererUtil>
		_displayPageRendererUtilMockedStatic = Mockito.mockStatic(
			DisplayPageRendererUtil.class);

}