/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.dto.v1_0.util;

import com.liferay.exportimport.attachment.ExportImportAttachmentManagerUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.InetAddressUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.net.InetAddress;
import java.net.URL;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Balázs Sáfrány-Kovalik
 */
public class URLUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetByteArrayWithLocalNetworkAddress() throws Exception {
		try (MockedStatic<ExportImportAttachmentManagerUtil>
				exportImportAttachmentManagerUtilMockedStatic =
					Mockito.mockStatic(ExportImportAttachmentManagerUtil.class);
			MockedStatic<InetAddressUtil> inetAddressUtilMockedStatic =
				Mockito.mockStatic(InetAddressUtil.class)) {

			exportImportAttachmentManagerUtilMockedStatic.when(
				() -> ExportImportAttachmentManagerUtil.getURL(_LOCAL_URL)
			).thenReturn(
				new URL(_LOCAL_URL)
			);

			inetAddressUtilMockedStatic.when(
				() -> InetAddressUtil.isLocalInetAddress(Mockito.any())
			).thenReturn(
				true
			);

			Assert.assertThrows(
				IllegalArgumentException.class,
				() -> URLUtil.getByteArray(_LOCAL_URL));
		}
	}

	@Test
	public void testGetByteArrayWithRemoteAddress() throws Exception {
		try (MockedStatic<ExportImportAttachmentManagerUtil>
				exportImportAttachmentManagerUtilMockedStatic =
					Mockito.mockStatic(ExportImportAttachmentManagerUtil.class);
			MockedStatic<HttpUtil> httpUtilMockedStatic = Mockito.mockStatic(
				HttpUtil.class);
			MockedStatic<InetAddressUtil> inetAddressUtilMockedStatic =
				Mockito.mockStatic(InetAddressUtil.class)) {

			exportImportAttachmentManagerUtilMockedStatic.when(
				() -> ExportImportAttachmentManagerUtil.getURL(_REMOTE_URL)
			).thenReturn(
				new URL(_REMOTE_URL)
			);

			inetAddressUtilMockedStatic.when(
				() -> InetAddressUtil.getInetAddressByName(Mockito.anyString())
			).thenReturn(
				InetAddress.getByName("1.2.3.4")
			);

			inetAddressUtilMockedStatic.when(
				() -> InetAddressUtil.isLocalInetAddress(Mockito.any())
			).thenReturn(
				false
			);

			byte[] expectedBytes = {1, 2, 3};

			httpUtilMockedStatic.when(
				() -> HttpUtil.URLtoByteArray(Mockito.any(Http.Options.class))
			).thenReturn(
				expectedBytes
			);

			Assert.assertArrayEquals(
				expectedBytes, URLUtil.getByteArray(_REMOTE_URL));
		}
	}

	@Test
	public void testGetByteArrayWithUnsupportedProtocol() throws Exception {
		try (MockedStatic<ExportImportAttachmentManagerUtil>
				exportImportAttachmentManagerUtilMockedStatic =
					Mockito.mockStatic(
						ExportImportAttachmentManagerUtil.class)) {

			exportImportAttachmentManagerUtilMockedStatic.when(
				() -> ExportImportAttachmentManagerUtil.getURL(_FTP_URL)
			).thenReturn(
				new URL(_FTP_URL)
			);

			Assert.assertThrows(
				UnsupportedOperationException.class,
				() -> URLUtil.getByteArray(_FTP_URL));
		}
	}

	private static final String _FTP_URL = "ftp://1.2.3.4";

	private static final String _LOCAL_URL = "http://127.0.0.1";

	private static final String _REMOTE_URL = "http://1.2.3.4";

}