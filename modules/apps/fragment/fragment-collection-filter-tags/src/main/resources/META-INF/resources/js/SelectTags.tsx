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

import ClayMultiSelect from '@clayui/multi-select';
import {
	getCollectionFilterValue,
	setCollectionFilterValue,
} from '@liferay/fragment-renderer-collection-filter-impl';
import React, {useCallback, useMemo} from 'react';

interface IProps {
	fragmentEntryLinkId: string;
	label: string;
	showHelpText: boolean;
	showLabel: boolean;
}

export default function SelectTags({
	fragmentEntryLinkId,
	showHelpText,
}: IProps) {
	const selectedTagIds = useMemo(() => {
		const value = getCollectionFilterValue('tags', fragmentEntryLinkId);

		if (Array.isArray(value)) {
			return value.map((tagId) => ({label: tagId, value: tagId}));
		}
		else if (value) {
			return [{label: value, value}];
		}

		return [];
	}, [fragmentEntryLinkId]);

	const setSelectedTagIds = useCallback(
		(nextTags: Array<{label: string; value: string}>) => {
			setCollectionFilterValue(
				'tags',
				fragmentEntryLinkId,
				nextTags.map((tag) => tag.value)
			);
		},
		[fragmentEntryLinkId]
	);

	return (
		<ClayMultiSelect
			aria-describedby={
				showHelpText ? `fragment_${fragmentEntryLinkId}_helpText` : null
			}
			aria-labelledby={`fragment_${fragmentEntryLinkId}_label`}
			items={selectedTagIds}
			onItemsChange={setSelectedTagIds}
		/>
	);
}
