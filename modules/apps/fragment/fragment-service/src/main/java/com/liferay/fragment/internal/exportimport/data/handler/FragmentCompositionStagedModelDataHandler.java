/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.internal.exportimport.data.handler;

import com.liferay.exportimport.data.handler.base.BaseStagedModelDataHandler;
import com.liferay.exportimport.kernel.lar.ExportImportPathUtil;
import com.liferay.exportimport.kernel.lar.ExportImportThreadLocal;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.kernel.lar.StagedModelDataHandler;
import com.liferay.exportimport.kernel.lar.StagedModelDataHandlerUtil;
import com.liferay.exportimport.staged.model.repository.StagedModelRepository;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.model.FragmentComposition;
import com.liferay.fragment.service.FragmentCollectionLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portletfilerepository.PortletFileRepositoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.xml.Element;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = StagedModelDataHandler.class)
public class FragmentCompositionStagedModelDataHandler
	extends BaseStagedModelDataHandler<FragmentComposition> {

	public static final String[] CLASS_NAMES = {
		FragmentComposition.class.getName()
	};

	@Override
	public void deleteStagedModel(FragmentComposition fragmentComposition)
		throws PortalException {

		_stagedModelRepository.deleteStagedModel(fragmentComposition);
	}

	@Override
	public void deleteStagedModel(
			String uuid, long groupId, String className, String extraData)
		throws PortalException {

		_stagedModelRepository.deleteStagedModel(
			uuid, groupId, className, extraData);
	}

	@Override
	public String[] getClassNames() {
		return CLASS_NAMES;
	}

	@Override
	public String getDisplayName(FragmentComposition fragmentComposition) {
		return fragmentComposition.getName();
	}

	@Override
	protected void doExportStagedModel(
			PortletDataContext portletDataContext,
			FragmentComposition fragmentComposition)
		throws Exception {

		if (fragmentComposition.isMarketplace() &&
			!ExportImportThreadLocal.isStagingInProcess()) {

			return;
		}

		FragmentCollection fragmentCollection =
			_fragmentCollectionLocalService.fetchFragmentCollection(
				fragmentComposition.getFragmentCollectionId());

		if (fragmentCollection == null) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to export fragment composition with key " +
						fragmentComposition.getFragmentCompositionKey());
			}

			return;
		}

		StagedModelDataHandlerUtil.exportReferenceStagedModel(
			portletDataContext, fragmentComposition, fragmentCollection,
			PortletDataContext.REFERENCE_TYPE_PARENT);

		if (fragmentComposition.getPreviewFileEntryId() > 0) {
			try {
				FileEntry fileEntry =
					PortletFileRepositoryUtil.getPortletFileEntry(
						fragmentComposition.getPreviewFileEntryId());

				StagedModelDataHandlerUtil.exportReferenceStagedModel(
					portletDataContext, fragmentComposition, fileEntry,
					PortletDataContext.REFERENCE_TYPE_WEAK);
			}
			catch (PortalException portalException) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to export file entry " +
							fragmentComposition.getPreviewFileEntryId(),
						portalException);
				}
			}
		}

		Element entryElement = portletDataContext.getExportDataElement(
			fragmentComposition);

		portletDataContext.addClassedModel(
			entryElement,
			ExportImportPathUtil.getModelPath(fragmentComposition),
			fragmentComposition);
	}

	@Override
	protected void doImportMissingReference(
			PortletDataContext portletDataContext, String uuid, long groupId,
			long fragmentCompositionId)
		throws Exception {

		FragmentComposition existingFragmentComposition = fetchMissingReference(
			uuid, groupId);

		if (existingFragmentComposition == null) {
			return;
		}

		Map<Long, Long> fragmentCompositionIds =
			(Map<Long, Long>)portletDataContext.getNewPrimaryKeysMap(
				FragmentComposition.class);

		fragmentCompositionIds.put(
			fragmentCompositionId,
			existingFragmentComposition.getFragmentCompositionId());
	}

	@Override
	protected void doImportStagedModel(
			PortletDataContext portletDataContext,
			FragmentComposition fragmentComposition)
		throws Exception {

		StagedModelDataHandlerUtil.importReferenceStagedModels(
			portletDataContext, fragmentComposition, FileEntry.class);

		Map<Long, Long> fragmentCollectionIds =
			(Map<Long, Long>)portletDataContext.getNewPrimaryKeysMap(
				FragmentCollection.class);

		long fragmentCollectionId = MapUtil.getLong(
			fragmentCollectionIds,
			fragmentComposition.getFragmentCollectionId(),
			fragmentComposition.getFragmentCollectionId());

		long previewFileEntryId = 0;

		if (fragmentComposition.getPreviewFileEntryId() > 0) {
			Map<Long, Long> fileEntryIds =
				(Map<Long, Long>)portletDataContext.getNewPrimaryKeysMap(
					FileEntry.class);

			previewFileEntryId = MapUtil.getLong(
				fileEntryIds, fragmentComposition.getPreviewFileEntryId(), 0);
		}

		FragmentComposition importedFragmentComposition =
			(FragmentComposition)fragmentComposition.clone();

		importedFragmentComposition.setGroupId(
			portletDataContext.getScopeGroupId());
		importedFragmentComposition.setFragmentCollectionId(
			fragmentCollectionId);
		importedFragmentComposition.setPreviewFileEntryId(previewFileEntryId);

		FragmentComposition existingFragmentComposition =
			_stagedModelRepository.fetchStagedModelByUuidAndGroupId(
				fragmentComposition.getUuid(),
				portletDataContext.getScopeGroupId());

		if ((existingFragmentComposition == null) ||
			!portletDataContext.isDataStrategyMirror()) {

			importedFragmentComposition = _stagedModelRepository.addStagedModel(
				portletDataContext, importedFragmentComposition);
		}
		else {
			if ((previewFileEntryId == 0) &&
				(existingFragmentComposition.getPreviewFileEntryId() > 0)) {

				PortletFileRepositoryUtil.deletePortletFileEntry(
					existingFragmentComposition.getPreviewFileEntryId());
			}

			importedFragmentComposition.setMvccVersion(
				existingFragmentComposition.getMvccVersion());
			importedFragmentComposition.setFragmentCompositionId(
				existingFragmentComposition.getFragmentCompositionId());

			importedFragmentComposition =
				_stagedModelRepository.updateStagedModel(
					portletDataContext, importedFragmentComposition);
		}

		portletDataContext.importClassedModel(
			fragmentComposition, importedFragmentComposition);
	}

	@Override
	protected StagedModelRepository<FragmentComposition>
		getStagedModelRepository() {

		return _stagedModelRepository;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FragmentCompositionStagedModelDataHandler.class);

	@Reference
	private FragmentCollectionLocalService _fragmentCollectionLocalService;

	@Reference(
		target = "(model.class.name=com.liferay.fragment.model.FragmentComposition)",
		unbind = "-"
	)
	private StagedModelRepository<FragmentComposition> _stagedModelRepository;

}