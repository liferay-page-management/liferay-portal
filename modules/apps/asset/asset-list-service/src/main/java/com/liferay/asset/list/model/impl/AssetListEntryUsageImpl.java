/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.asset.list.model.impl;

import com.liferay.portal.kernel.util.GetterUtil;

/**
 * @author Brian Wing Shun Chan
 */
public class AssetListEntryUsageImpl extends AssetListEntryUsageBaseImpl {

	public AssetListEntryUsageImpl() {
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link #getKey()}
	 */
	@Deprecated
	public long getClassPK() {
		return GetterUtil.getLong(getKey());
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link #setKey(String)}
	 */
	@Deprecated
	public void setClassPK(long classPK) {
		setKey(String.valueOf(classPK));
	}

}