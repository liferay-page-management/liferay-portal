/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.fragment.internal.dto.v1_0.util;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.processor.ImageProcessorUtil;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.exportimport.attachment.ExportImportAttachmentManagerUtil;
import com.liferay.headless.admin.fragment.dto.v1_0.Thumbnail;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.repository.model.FileEntry;

/**
 * @author Rubén Pulido
 */
public class ThumbnailUtil {

	public static Thumbnail getThumbnail(long fileEntryId)
		throws PortalException {

		if (fileEntryId <= 0) {
			return null;
		}

		DLFileEntry dlFileEntry = DLFileEntryLocalServiceUtil.getFileEntry(
			fileEntryId);

		return new Thumbnail() {
			{
				setExternalReferenceCode(dlFileEntry::getExternalReferenceCode);
				setUrl(
					() -> {
						FileEntry fileEntry =
							DLAppLocalServiceUtil.getFileEntry(fileEntryId);

						if ((fileEntry == null) ||
							!ImageProcessorUtil.hasImages(
								fileEntry.getFileVersion())) {

							return null;
						}

						return ExportImportAttachmentManagerUtil.getFileURL(
							dlFileEntry);
					});
			}
		};
	}

}