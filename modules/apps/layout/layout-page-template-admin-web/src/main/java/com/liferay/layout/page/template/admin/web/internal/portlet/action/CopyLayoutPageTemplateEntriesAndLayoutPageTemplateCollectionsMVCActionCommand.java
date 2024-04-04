/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.admin.web.internal.portlet.action;

import com.liferay.layout.helper.LayoutCopyHelper;
import com.liferay.layout.page.template.admin.constants.LayoutPageTemplateAdminPortletKeys;
import com.liferay.layout.page.template.model.LayoutPageTemplateCollection;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateCollectionService;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryService;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bárbara Cabrera
 */
@Component(
	property = {
		"javax.portlet.name=" + LayoutPageTemplateAdminPortletKeys.LAYOUT_PAGE_TEMPLATES,
		"mvc.command.name=/layout_page_template_admin/copy_layout_page_template_entries_and_layout_page_template_collections"
	},
	service = MVCActionCommand.class
)
public class
	CopyLayoutPageTemplateEntriesAndLayoutPageTemplateCollectionsMVCActionCommand
		extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		boolean copyPermissions = ParamUtil.getBoolean(
			actionRequest, "copyPermissions");
		long[] layoutPageTemplateCollectionsId = ParamUtil.getLongValues(
			actionRequest, "layoutPageTemplateCollectionsIds");
		long[] layoutPageTemplateEntriesId = ParamUtil.getLongValues(
			actionRequest, "layoutPageTemplateEntriesIds");
		long layoutParentPageTemplateCollectionId = ParamUtil.getLong(
			actionRequest, "layoutParentPageTemplateCollectionId");

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			actionRequest);

		for (long layoutPageTemplateCollectionId :
				layoutPageTemplateCollectionsId) {

			_copyLayoutPageTemplateCollection(
				layoutPageTemplateCollectionId,
				layoutParentPageTemplateCollectionId, serviceContext);
		}

		for (long layoutPageTemplateEntryId : layoutPageTemplateEntriesId) {
			_copyLayoutPageTemplateEntry(
				copyPermissions, layoutPageTemplateEntryId,
				layoutParentPageTemplateCollectionId, serviceContext,
				themeDisplay);
		}
	}

	private LayoutPageTemplateCollection _copyLayoutPageTemplateCollection(
			long layoutPageTemplateCollectionId,
			long layoutParentPageTemplateCollectionId,
			ServiceContext serviceContext)
		throws Exception {

		return _layoutPageTemplateCollectionService.
			copyLayoutPageTemplateCollection(
				serviceContext.getScopeGroupId(),
				layoutPageTemplateCollectionId,
				layoutParentPageTemplateCollectionId, serviceContext);
	}

	private LayoutPageTemplateEntry _copyLayoutPageTemplateEntry(
			boolean copyPermissions, long layoutPageTemplateEntryId,
			long layoutParentPageTemplateCollectionId,
			ServiceContext serviceContext, ThemeDisplay themeDisplay)
		throws Exception {

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			_layoutPageTemplateEntryService.copyLayoutPageTemplateEntry(
				themeDisplay.getScopeGroupId(),
				layoutParentPageTemplateCollectionId, layoutPageTemplateEntryId,
				copyPermissions, serviceContext);

		LayoutPageTemplateEntry sourceLayoutPageTemplateEntry =
			_layoutPageTemplateEntryLocalService.getLayoutPageTemplateEntry(
				layoutPageTemplateEntryId);

		Layout sourceLayout = _layoutLocalService.getLayout(
			sourceLayoutPageTemplateEntry.getPlid());

		Layout targetLayout = _layoutLocalService.getLayout(
			layoutPageTemplateEntry.getPlid());

		_layoutCopyHelper.copyLayoutContent(
			sourceLayout, targetLayout.fetchDraftLayout());

		_layoutCopyHelper.copyLayoutContent(sourceLayout, targetLayout);

		return layoutPageTemplateEntry;
	}

	@Reference
	private LayoutCopyHelper _layoutCopyHelper;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private LayoutPageTemplateCollectionService
		_layoutPageTemplateCollectionService;

	@Reference
	private LayoutPageTemplateEntryLocalService
		_layoutPageTemplateEntryLocalService;

	@Reference
	private LayoutPageTemplateEntryService _layoutPageTemplateEntryService;

}