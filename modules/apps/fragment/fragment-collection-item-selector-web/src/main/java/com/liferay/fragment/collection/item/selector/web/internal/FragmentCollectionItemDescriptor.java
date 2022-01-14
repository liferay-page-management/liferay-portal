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

package com.liferay.fragment.collection.item.selector.web.internal;

import com.liferay.fragment.collection.item.selector.criterion.FragmentCollectionItemSelectorCriterion;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.item.selector.ItemSelectorViewDescriptor;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Locale;

/**
 * @author Rubén Pulido
 */
public class FragmentCollectionItemDescriptor
	implements ItemSelectorViewDescriptor.ItemDescriptor {

	public FragmentCollectionItemDescriptor(
		FragmentCollection fragmentCollection,
		FragmentCollectionItemSelectorCriterion
			fragmentCollectionItemSelectorCriterion) {

		_fragmentCollection = fragmentCollection;
		_fragmentCollectionItemSelectorCriterion =
			fragmentCollectionItemSelectorCriterion;
	}

	@Override
	public String getIcon() {
		return "documents-and-media";
	}

	@Override
	public String getImageURL() {
		return null;
	}

	@Override
	public String getPayload() {
		return JSONUtil.put(
			"name", _fragmentCollection.getName()
		).put(
			"previewURL",
			StringUtil.replace(
				_fragmentCollectionItemSelectorCriterion.
					getPreviewURLTemplate(),
				StringPool.UNDERLINE, StringPool.UNDERLINE,
				HashMapBuilder.put(
					"fragmentCollectionKey",
					_fragmentCollection.getFragmentCollectionKey()
				).build())
		).toString();
	}

	@Override
	public String getSubtitle(Locale locale) {
		return _fragmentCollection.getDescription();
	}

	@Override
	public String getTitle(Locale locale) {
		return _fragmentCollection.getName();
	}

	private final FragmentCollection _fragmentCollection;
	private final FragmentCollectionItemSelectorCriterion
		_fragmentCollectionItemSelectorCriterion;

}