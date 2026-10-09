/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.dto.v1_0.converter;

import com.liferay.headless.admin.site.dto.v1_0.DisplayPageTemplateFolder;
import com.liferay.headless.admin.site.internal.dto.v1_0.util.CreatorUtil;
import com.liferay.layout.page.template.constants.LayoutPageTemplateCollectionTypeConstants;
import com.liferay.layout.page.template.model.LayoutPageTemplateCollection;
import com.liferay.layout.page.template.service.LayoutPageTemplateCollectionService;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bárbara Cabrera
 */
@Component(
	property = {
		"default=true",
		"dto.class.name=com.liferay.layout.page.template.model.LayoutPageTemplateCollection",
		"dto.class.type=" + LayoutPageTemplateCollectionTypeConstants.DISPLAY_PAGE
	},
	service = DTOConverter.class
)
public class DisplayPageTemplateFolderDTOConverter
	implements DTOConverter
		<LayoutPageTemplateCollection, DisplayPageTemplateFolder> {

	@Override
	public String getContentType() {
		return DisplayPageTemplateFolder.class.getSimpleName();
	}

	@Override
	public DisplayPageTemplateFolder toDTO(
			DTOConverterContext dtoConverterContext,
			LayoutPageTemplateCollection layoutPageTemplateCollection)
		throws Exception {

		DisplayPageTemplateFolder displayPageTemplateFolder =
			_getDisplayPageTemplateFolder(layoutPageTemplateCollection);

		displayPageTemplateFolder.setActions(
			() -> {
				if (dtoConverterContext == null) {
					return null;
				}

				return dtoConverterContext.getActions();
			});

		return displayPageTemplateFolder;
	}

	private DisplayPageTemplateFolder _getDisplayPageTemplateFolder(
			LayoutPageTemplateCollection layoutPageTemplateCollection)
		throws Exception {

		LayoutPageTemplateCollection parentLayoutPageTemplateCollection =
			_layoutPageTemplateCollectionService.
				fetchLayoutPageTemplateCollection(
					layoutPageTemplateCollection.
						getParentLayoutPageTemplateCollectionId());

		return new DisplayPageTemplateFolder() {
			{
				setCreator(
					() -> CreatorUtil.toCreator(
						layoutPageTemplateCollection.getUserId(),
						layoutPageTemplateCollection.getUserName()));
				setDateCreated(layoutPageTemplateCollection::getCreateDate);
				setDateModified(layoutPageTemplateCollection::getModifiedDate);
				setDescription(layoutPageTemplateCollection::getDescription);
				setExternalReferenceCode(
					layoutPageTemplateCollection::getExternalReferenceCode);
				setKey(
					layoutPageTemplateCollection::
						getLayoutPageTemplateCollectionKey);
				setName(layoutPageTemplateCollection::getName);
				setParentDisplayPageTemplateFolder(
					() -> {
						if (parentLayoutPageTemplateCollection == null) {
							return null;
						}

						return _getDisplayPageTemplateFolder(
							parentLayoutPageTemplateCollection);
					});
				setParentDisplayPageTemplateFolderExternalReferenceCode(
					() -> {
						if (parentLayoutPageTemplateCollection == null) {
							return null;
						}

						return parentLayoutPageTemplateCollection.
							getExternalReferenceCode();
					});
				setUuid(layoutPageTemplateCollection::getUuid);
			}
		};
	}

	@Reference
	private LayoutPageTemplateCollectionService
		_layoutPageTemplateCollectionService;

}