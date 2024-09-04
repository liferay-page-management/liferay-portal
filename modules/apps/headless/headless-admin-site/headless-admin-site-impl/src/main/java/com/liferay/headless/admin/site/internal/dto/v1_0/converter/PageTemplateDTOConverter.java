/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.dto.v1_0.converter;

import com.liferay.headless.admin.site.dto.v1_0.ContentPageTemplateSettings;
import com.liferay.headless.admin.site.dto.v1_0.PageTemplate;
import com.liferay.headless.admin.site.dto.v1_0.PageTemplateSettings;
import com.liferay.headless.admin.site.dto.v1_0.WidgetPageTemplateSettings;
import com.liferay.layout.page.template.constants.LayoutPageTemplateEntryTypeConstants;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutPrototype;
import com.liferay.portal.kernel.service.LayoutPrototypeLocalService;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rubén Pulido
 */
@Component(
	property = "dto.class.name=com.liferay.layout.page.template.model.LayoutPageTemplateEntry",
	service = DTOConverter.class
)
public class PageTemplateDTOConverter
	implements DTOConverter<LayoutPageTemplateEntry, PageTemplate> {

	@Override
	public String getContentType() {
		return PageTemplate.class.getSimpleName();
	}

	@Override
	public PageTemplate toDTO(
			DTOConverterContext dtoConverterContext,
			LayoutPageTemplateEntry layoutPageTemplateEntry)
		throws Exception {

		return new PageTemplate() {
			{
				setDateCreated(layoutPageTemplateEntry::getCreateDate);
				setDateModified(layoutPageTemplateEntry::getModifiedDate);
				setDatePublished(layoutPageTemplateEntry::getLastPublishDate);
				setName(layoutPageTemplateEntry::getName);
				setPageTemplateSettings(
					() -> _toPageTemplateSettings(layoutPageTemplateEntry));
				setType(() -> _toType(layoutPageTemplateEntry));
				setUuid(layoutPageTemplateEntry::getUuid);
			}
		};
	}

	private ContentPageTemplateSettings _toContentPageTemplateSettings() {
		return new ContentPageTemplateSettings() {
			{
				setType(Type.CONTENT_PAGE_TEMPLATE_SETTINGS);
			}
		};
	}

	private PageTemplateSettings _toPageTemplateSettings(
		LayoutPageTemplateEntry layoutPageTemplateEntry) {

		int type = layoutPageTemplateEntry.getType();

		if (type == LayoutPageTemplateEntryTypeConstants.BASIC) {
			return _toContentPageTemplateSettings();
		}
		else if (type == LayoutPageTemplateEntryTypeConstants.WIDGET_PAGE) {
			return _toWidgetPageTemplateSettings(layoutPageTemplateEntry);
		}

		return null;
	}

	private PageTemplate.Type _toType(
		LayoutPageTemplateEntry layoutPageTemplateEntry) {

		int type = layoutPageTemplateEntry.getType();

		if (type == LayoutPageTemplateEntryTypeConstants.BASIC) {
			return PageTemplate.Type.CONTENT_PAGE_TEMPLATE;
		}

		if (type == LayoutPageTemplateEntryTypeConstants.WIDGET_PAGE) {
			return PageTemplate.Type.WIDGET_PAGE_TEMPLATE;
		}

		return null;
	}

	private WidgetPageTemplateSettings _toWidgetPageTemplateSettings(
		LayoutPageTemplateEntry layoutPageTemplateEntry) {

		return new WidgetPageTemplateSettings() {
			{
				setLayoutTemplateId(
					() -> {
						LayoutPrototype layoutPrototype =
							_layoutPrototypeLocalService.getLayoutPrototype(
								layoutPageTemplateEntry.getLayoutPrototypeId());

						Layout layout = layoutPrototype.getLayout();

						return layout.getTypeSettingsProperty(
							"layout-template-id");
					});
				setType(Type.WIDGET_PAGE_TEMPLATE_SETTINGS);
			}
		};
	}

	@Reference
	private LayoutPrototypeLocalService _layoutPrototypeLocalService;

}