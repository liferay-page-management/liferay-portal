/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.search.experiences.internal.web.cache;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.webcache.WebCacheItem;
import com.liferay.portal.kernel.webcache.WebCachePoolUtil;
import com.liferay.search.experiences.internal.configuration.IpstackConfiguration;

/**
 * @author Brian Wing Shun Chan
 */
public class IpstackWebCacheItem implements WebCacheItem {

	public static JSONObject get(
		String ipAddress, IpstackConfiguration ipstackConfiguration) {

		return (JSONObject)WebCachePoolUtil.get(
			IpstackWebCacheItem.class.getName() + StringPool.POUND + ipAddress,
			new IpstackWebCacheItem(ipAddress, ipstackConfiguration));
	}

	public IpstackWebCacheItem(
		String ipAddress, IpstackConfiguration ipstackConfiguration) {

		_ipAddress = ipAddress;
		_ipstackConfiguration = ipstackConfiguration;
	}

	@Override
	public JSONObject convert(String key) {
		try {
			if (!_ipstackConfiguration.enabled()) {
				return JSONFactoryUtil.createJSONObject();
			}

			String apiURL = _ipstackConfiguration.apiURL();

			if (!apiURL.endsWith("/")) {
				apiURL += "/";
			}

			String url = StringBundler.concat(
				apiURL, _ipAddress, "?access_key=",
				_ipstackConfiguration.apiKey());

			if (_log.isDebugEnabled()) {
				_log.debug("Reading " + url);
			}

			return JSONFactoryUtil.createJSONObject(HttpUtil.URLtoString(url));
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception, exception);
			}

			return JSONFactoryUtil.createJSONObject();
		}
	}

	@Override
	public long getRefreshTime() {
		if (_ipstackConfiguration.enabled()) {
			return _ipstackConfiguration.cacheTimeout();
		}

		return 0;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		IpstackWebCacheItem.class);

	private final String _ipAddress;
	private final IpstackConfiguration _ipstackConfiguration;

}