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

package com.liferay.fragment.renderer;

import com.liferay.fragment.model.FragmentEntryLink;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Iván Zaera Avellón
 */
public interface FragmentEntryLinkRenderer {

	public String renderFragmentEntryLink(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse,
			FragmentEntryLink fragmentEntryLink, String css, String html,
			String js, String configuration)
		throws IOException;

}