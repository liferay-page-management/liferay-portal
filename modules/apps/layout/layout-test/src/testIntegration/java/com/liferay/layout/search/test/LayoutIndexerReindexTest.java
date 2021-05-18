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

package com.liferay.layout.search.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.util.BaseLayoutSearchTestCase;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.util.LocaleThreadLocal;
import com.liferay.portal.search.test.util.IndexerFixture;
import com.liferay.users.admin.test.util.search.UserSearchFixture;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Igor Fabiano Nazar
 * @author Vagner B.C
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class LayoutIndexerReindexTest extends BaseLayoutSearchTestCase {

	@Before
	public void setUp() throws Exception {
		setUpUserSearchFixture();

		setUpLayoutIndexerFixture();

		setUpLayoutFixture();
	}

	@Test
	public void testReindex() throws Exception {
		Layout layout = layoutFixture.createLayout();

		layout = publishLayout(layout);

		Locale locale = LocaleThreadLocal.getDefaultLocale();

		String searchTerm = layout.getName(locale);

		layoutIndexerFixture.searchOnlyOne(searchTerm);

		Document document = layoutIndexerFixture.searchOnlyOne(
			searchTerm, locale);

		layoutIndexerFixture.deleteDocument(document);

		layoutIndexerFixture.searchNoOne(searchTerm, locale);

		layoutIndexerFixture.reindex(layout.getCompanyId());

		layoutIndexerFixture.searchOnlyOne(searchTerm, locale);
	}

	protected void setUpLayoutFixture() {
		layoutFixture = new LayoutFixture(_group);
	}

	protected void setUpLayoutIndexerFixture() {
		layoutIndexerFixture = new IndexerFixture<>(Layout.class);
	}

	protected void setUpUserSearchFixture() throws Exception {
		userSearchFixture = new UserSearchFixture();

		userSearchFixture.setUp();

		_group = userSearchFixture.addGroup();
	}

	protected LayoutFixture layoutFixture;
	protected IndexerFixture<Layout> layoutIndexerFixture;
	protected UserSearchFixture userSearchFixture;

	private Group _group;

}