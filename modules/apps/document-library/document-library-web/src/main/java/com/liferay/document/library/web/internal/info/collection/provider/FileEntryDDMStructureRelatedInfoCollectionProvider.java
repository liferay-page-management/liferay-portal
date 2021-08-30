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

package com.liferay.document.library.web.internal.info.collection.provider;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.depot.util.SiteConnectedGroupGroupProviderUtil;
import com.liferay.document.library.kernel.model.DLFileEntryConstants;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.info.collection.provider.CollectionQuery;
import com.liferay.info.collection.provider.RelatedInfoItemCollectionProvider;
import com.liferay.info.collection.provider.SingleFormVariationInfoCollectionProvider;
import com.liferay.info.pagination.InfoPage;
import com.liferay.info.pagination.Pagination;
import com.liferay.info.sort.Sort;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.LocalRepository;
import com.liferay.portal.kernel.repository.RepositoryProvider;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Jürgen Kappler
 */
public class FileEntryDDMStructureRelatedInfoCollectionProvider
	implements RelatedInfoItemCollectionProvider<AssetCategory, FileEntry>,
			   SingleFormVariationInfoCollectionProvider<FileEntry> {

	public FileEntryDDMStructureRelatedInfoCollectionProvider(
		DDMStructure ddmStructure, RepositoryProvider repositoryProvider) {

		_ddmStructure = ddmStructure;
		_repositoryProvider = repositoryProvider;
	}

	@Override
	public InfoPage<FileEntry> getCollectionInfoPage(
		CollectionQuery collectionQuery) {

		Indexer<?> indexer = IndexerRegistryUtil.getIndexer(
			DLFileEntryConstants.getClassName());

		SearchContext searchContext = _buildSearchContext(collectionQuery);

		try {
			Hits hits = indexer.search(searchContext);

			List<FileEntry> fileEntries = new ArrayList<>();

			for (Document document : hits.getDocs()) {
				String className = document.get(Field.ENTRY_CLASS_NAME);

				if (className.equals(DLFileEntryConstants.getClassName())) {
					long classPK = GetterUtil.getLong(
						document.get(Field.ENTRY_CLASS_PK));

					LocalRepository localRepository =
						_repositoryProvider.fetchFileEntryLocalRepository(
							classPK);

					if (localRepository == null) {
						continue;
					}

					FileEntry fileEntry = localRepository.getFileEntry(classPK);

					if (fileEntry.isInTrash()) {
						return null;
					}

					fileEntries.add(fileEntry);
				}
			}

			return InfoPage.of(
				fileEntries, collectionQuery.getPagination(), hits.getLength());
		}
		catch (PortalException portalException) {
			if (_log.isWarnEnabled()) {
				_log.warn(portalException, portalException);
			}
		}

		return null;
	}

	@Override
	public String getCollectionItemClassName() {
		return FileEntry.class.getName();
	}

	@Override
	public String getFormVariationKey() {
		return String.valueOf(_ddmStructure.getStructureId());
	}

	@Override
	public String getKey() {
		return RelatedInfoItemCollectionProvider.class.getName() +
			StringPool.DASH + _ddmStructure.getStructureKey();
	}

	@Override
	public String getLabel(Locale locale) {
		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			locale, getClass());

		return LanguageUtil.get(resourceBundle, _ddmStructure.getName(locale));
	}

	@Override
	public String getSourceItemClassName() {
		return AssetCategory.class.getName();
	}

	@Override
	public boolean isAvailable() {
		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		try {
			long[] currentAndAncestorSiteAndDepotGroupIds =
				SiteConnectedGroupGroupProviderUtil.
					getCurrentAndAncestorSiteAndDepotGroupIds(
						serviceContext.getScopeGroupId(), true);

			if (ArrayUtil.contains(
					currentAndAncestorSiteAndDepotGroupIds,
					_ddmStructure.getGroupId())) {

				return true;
			}
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException, portalException);
			}
		}

		return false;
	}

	private SearchContext _buildSearchContext(CollectionQuery collectionQuery) {
		SearchContext searchContext = new SearchContext();

		searchContext.setAndSearch(true);
		searchContext.setAttributes(
			HashMapBuilder.<String, Serializable>put(
				Field.STATUS, WorkflowConstants.STATUS_APPROVED
			).put(
				"ddmStructureKey", _ddmStructure.getStructureId()
			).put(
				"head", true
			).put(
				"latest", true
			).build());

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		searchContext.setCompanyId(serviceContext.getCompanyId());

		Pagination pagination = collectionQuery.getPagination();

		searchContext.setEnd(pagination.getEnd());

		searchContext.setGroupIds(
			new long[] {serviceContext.getScopeGroupId()});

		Optional<Sort> sortOptional = collectionQuery.getSortOptional();

		if (sortOptional.isPresent()) {
			Sort sort = sortOptional.get();

			searchContext.setSorts(
				new com.liferay.portal.kernel.search.Sort(
					sort.getFieldName(),
					com.liferay.portal.kernel.search.Sort.LONG_TYPE,
					sort.isReverse()));
		}

		searchContext.setStart(pagination.getStart());

		QueryConfig queryConfig = searchContext.getQueryConfig();

		queryConfig.setHighlightEnabled(false);
		queryConfig.setScoreEnabled(false);

		return searchContext;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FileEntryDDMStructureRelatedInfoCollectionProvider.class);

	private final DDMStructure _ddmStructure;
	private final RepositoryProvider _repositoryProvider;

}