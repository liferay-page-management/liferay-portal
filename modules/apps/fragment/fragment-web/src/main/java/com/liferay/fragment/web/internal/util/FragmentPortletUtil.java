/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.web.internal.util;

import com.liferay.fragment.constants.FragmentPortletKeys;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.util.comparator.FragmentCollectionCreateDateComparator;
import com.liferay.fragment.util.comparator.FragmentCollectionNameComparator;
import com.liferay.fragment.util.comparator.FragmentCompositionFragmentEntryModifiedDateComparator;
import com.liferay.fragment.util.comparator.FragmentCompositionFragmentEntryNameComparator;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Jürgen Kappler
 */
public class FragmentPortletUtil {

	public static String getEditFragmentEntryURL(
		HttpServletRequest httpServletRequest) {

		RequestBackedPortletURLFactory requestBackedPortletURLFactory =
			RequestBackedPortletURLFactoryUtil.create(httpServletRequest);

		return PortletURLBuilder.create(
			requestBackedPortletURLFactory.createRenderURL(
				FragmentPortletKeys.FRAGMENT)
		).setMVCRenderCommandName(
			"/fragment/edit_fragment_entry"
		).setRedirect(
			() -> {
				ThemeDisplay themeDisplay =
					(ThemeDisplay)httpServletRequest.getAttribute(
						WebKeys.THEME_DISPLAY);

				return themeDisplay.getURLCurrent();
			}
		).setParameter(
			"externalReferenceCode", "EXTERNAL_REFERENCE_CODE"
		).buildString();
	}

	public static OrderByComparator<FragmentCollection>
		getFragmentCollectionOrderByComparator(
			String orderByCol, String orderByType) {

		boolean orderByAsc = false;

		if (orderByType.equals("asc")) {
			orderByAsc = true;
		}

		OrderByComparator<FragmentCollection> orderByComparator = null;

		if (orderByCol.equals("create-date")) {
			orderByComparator =
				FragmentCollectionCreateDateComparator.getInstance(orderByAsc);
		}
		else if (orderByCol.equals("name")) {
			orderByComparator = FragmentCollectionNameComparator.getInstance(
				orderByAsc);
		}

		return orderByComparator;
	}

	public static OrderByComparator<Object>
		getFragmentCompositionAndEntryOrderByComparator(
			String orderByCol, String orderByType) {

		boolean orderByAsc = false;

		if (orderByType.equals("asc")) {
			orderByAsc = true;
		}

		OrderByComparator<Object> orderByComparator = null;

		if (orderByCol.equals("name")) {
			orderByComparator =
				FragmentCompositionFragmentEntryNameComparator.getInstance(
					orderByAsc);
		}
		else if (orderByCol.equals("modified-date")) {
			orderByComparator =
				FragmentCompositionFragmentEntryModifiedDateComparator.
					getInstance(orderByAsc);
		}

		return orderByComparator;
	}

}