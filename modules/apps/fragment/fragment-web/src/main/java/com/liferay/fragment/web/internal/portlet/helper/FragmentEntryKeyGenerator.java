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

package com.liferay.fragment.web.internal.portlet.helper;

import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;

/**
 * @author Rubén Pulido
 */
public class FragmentEntryKeyGenerator {

	public static String getFragmentEntryKey(
		long groupId, String fragmentEntryKey,
		FragmentEntryLocalService fragmentEntryLocalService) {

		if (fragmentEntryKey == null) {
			fragmentEntryKey = StringPool.BLANK;
		}
		else {
			fragmentEntryKey = fragmentEntryKey.trim();
			fragmentEntryKey = StringUtil.toLowerCase(fragmentEntryKey);
		}

		FragmentEntry fragmentEntry =
			fragmentEntryLocalService.fetchFragmentEntry(
				groupId, fragmentEntryKey);

		if (fragmentEntry == null) {
			return fragmentEntryKey;
		}

		String newFragmentEntryKey = null;

		for (int i = 1;; i++) {
			newFragmentEntryKey = fragmentEntryKey + i;

			fragmentEntry = fragmentEntryLocalService.fetchFragmentEntry(
				groupId, newFragmentEntryKey);

			if (fragmentEntry == null) {
				return newFragmentEntryKey;
			}
		}
	}

}