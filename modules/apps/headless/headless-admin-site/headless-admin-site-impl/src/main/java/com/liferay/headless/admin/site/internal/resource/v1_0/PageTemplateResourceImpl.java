/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.resource.v1_0;

import com.liferay.headless.admin.site.dto.v1_0.PageTemplate;
import com.liferay.headless.admin.site.resource.v1_0.PageTemplateResource;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryService;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Rubén Pulido
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/page-template.properties",
	scope = ServiceScope.PROTOTYPE, service = PageTemplateResource.class
)
public class PageTemplateResourceImpl extends BasePageTemplateResourceImpl {

	@Override
	public PageTemplate getSiteSiteByExternalReferenceCodePageTemplate(
			String siteExternalReferenceCode,
			String pageTemplateExternalReferenceCode)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled("LPD-35443")) {
			throw new UnsupportedOperationException();
		}

		return _toPageTemplate(
			siteExternalReferenceCode, pageTemplateExternalReferenceCode);
	}

	@Override
	public PageTemplate
			postSiteSiteByExternalReferenceCodePageTemplateSetPageTemplate(
				String siteExternalReferenceCode,
				String pageTemplateSetExternalReferenceCode,
				PageTemplate pageTemplate)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled("LPD-35443")) {
			throw new UnsupportedOperationException();
		}

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			_addLayoutPageTemplateEntry(
				siteExternalReferenceCode, pageTemplate);

		DefaultDTOConverterContext dtoConverterContext =
			new DefaultDTOConverterContext(
				contextAcceptLanguage.isAcceptAllLanguages(), null,
				_dtoConverterRegistry, contextHttpServletRequest,
				layoutPageTemplateEntry.getLayoutPageTemplateEntryId(),
				contextAcceptLanguage.getPreferredLocale(), contextUriInfo,
				contextUser);

		return _pageTemplateDTOConverter.toDTO(
			dtoConverterContext, layoutPageTemplateEntry);
	}

	private LayoutPageTemplateEntry _addLayoutPageTemplateEntry(
			String siteExternalReferenceCode, PageTemplate pageTemplate)
		throws Exception {

		// TODO

		return null;
	}

	private PageTemplate _toPageTemplate(
			String siteExternalReferenceCode,
			String sitePageExternalReferenceCode)
		throws Exception {

		Group group = _groupLocalService.getGroupByExternalReferenceCode(
			siteExternalReferenceCode, contextCompany.getCompanyId());

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			_layoutPageTemplateEntryService.
				getLayoutPageTemplateEntryByExternalReferenceCode(
					sitePageExternalReferenceCode, group.getGroupId());

		DTOConverterContext dtoConverterContext =
			new DefaultDTOConverterContext(
				contextAcceptLanguage.isAcceptAllLanguages(), null,
				_dtoConverterRegistry, contextHttpServletRequest,
				layoutPageTemplateEntry.getLayoutPageTemplateEntryId(),
				contextAcceptLanguage.getPreferredLocale(), contextUriInfo,
				contextUser);

		dtoConverterContext.setAttribute(
			"groupId", layoutPageTemplateEntry.getGroupId());

		return _pageTemplateDTOConverter.toDTO(
			dtoConverterContext, layoutPageTemplateEntry);
	}

	@Reference
	private DTOConverterRegistry _dtoConverterRegistry;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private LayoutPageTemplateEntryService _layoutPageTemplateEntryService;

	@Reference(
		target = "(component.name=com.liferay.headless.admin.site.internal.dto.v1_0.converter.PageTemplateDTOConverter)"
	)
	private DTOConverter<LayoutPageTemplateEntry, PageTemplate>
		_pageTemplateDTOConverter;

}