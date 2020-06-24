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

package com.liferay.asset.list.web.internal.field;

import com.liferay.petra.lang.HashUtil;

import java.util.Date;

/**
 * @author Jürgen Kappler
 */
public class ListItemField {

	public ListItemField(
		String title, String className, long classPK, String author,
		Date modifiedDate, Date createDate) {

		_title = title;
		_className = className;
		_classPK = classPK;
		_author = author;
		_modifiedDate = modifiedDate;
		_createDate = createDate;
	}

	public String getAuthor() {
		return _author;
	}

	public String getClassName() {
		return _className;
	}

	public long getClassPK() {
		return _classPK;
	}

	public Date getCreateDate() {
		return _createDate;
	}

	public Date getModifiedDate() {
		return _modifiedDate;
	}

	public String getTitle() {
		return _title;
	}

	@Override
	public int hashCode() {
		int hash = HashUtil.hash(0, _title);

		hash = HashUtil.hash(hash, _className);

		hash = HashUtil.hash(hash, _classPK);

		hash = HashUtil.hash(hash, _author);

		hash = HashUtil.hash(hash, _modifiedDate);

		return HashUtil.hash(hash, _createDate);
	}

	private final String _author;
	private final String _className;
	private final long _classPK;
	private final Date _createDate;
	private final Date _modifiedDate;
	private final String _title;

}