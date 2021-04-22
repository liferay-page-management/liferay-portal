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

package com.liferay.layout.content.page.editor.web.internal.layout.contents.contributor;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.kernel.model.ClassType;
import com.liferay.asset.kernel.model.ClassTypeReader;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.info.item.InfoItemReference;
import com.liferay.journal.model.JournalArticle;
import com.liferay.layout.content.page.editor.constants.ContentPageEditorPortletKeys;
import com.liferay.layout.content.page.editor.web.internal.util.InfoEditURLProviderUtil;
import com.liferay.layout.content.page.editor.web.internal.util.LayoutDisplayPageProviderTrackerUtil;
import com.liferay.layout.content.page.editor.web.internal.util.layout.structure.LayoutStructureUtil;
import com.liferay.layout.contents.contributor.LayoutContentsContributor;
import com.liferay.layout.display.page.LayoutDisplayPageObjectProvider;
import com.liferay.layout.display.page.LayoutDisplayPageProvider;
import com.liferay.layout.model.LayoutClassedModelUsage;
import com.liferay.layout.security.permission.resource.LayoutContentModelResourcePermission;
import com.liferay.layout.service.LayoutClassedModelUsageLocalService;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.liferay.petra.portlet.url.builder.PortletURLBuilder;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.taglib.security.PermissionsURLTag;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.portlet.PortletRequest;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán
 */
@Component(
	service = {
		LayoutClassedModelUsageLayoutContentsContributor.class,
		LayoutContentsContributor.class
	}
)
public class LayoutClassedModelUsageLayoutContentsContributor
	implements LayoutContentsContributor {

	@Override
	public JSONArray getLayoutContentsJSONArray(
		HttpServletRequest httpServletRequest, long plid) {

		JSONArray mappedContentsJSONArray = JSONFactoryUtil.createJSONArray();

		long fragmentEntryLinkClassNameId = _portal.getClassNameId(
			FragmentEntryLink.class);

		Set<String> uniqueLayoutClassedModelUsageKeys = new HashSet<>();

		List<LayoutClassedModelUsage> layoutClassedModelUsages =
			_layoutClassedModelUsageLocalService.
				getLayoutClassedModelUsagesByPlid(plid);

		for (LayoutClassedModelUsage layoutClassedModelUsage :
				layoutClassedModelUsages) {

			if (uniqueLayoutClassedModelUsageKeys.contains(
					_generateUniqueLayoutClassedModelUsageKey(
						layoutClassedModelUsage))) {

				continue;
			}

			if (layoutClassedModelUsage.getContainerType() ==
					fragmentEntryLinkClassNameId) {

				FragmentEntryLink fragmentEntryLink =
					_fragmentEntryLinkLocalService.fetchFragmentEntryLink(
						GetterUtil.getLong(
							layoutClassedModelUsage.getContainerKey()));

				if (fragmentEntryLink == null) {
					_layoutClassedModelUsageLocalService.
						deleteLayoutClassedModelUsage(layoutClassedModelUsage);

					continue;
				}

				if (_isFragmentEntryLinkDeleted(fragmentEntryLink)) {
					continue;
				}
			}

			try {
				mappedContentsJSONArray.put(
					_getPageContentJSONObject(
						layoutClassedModelUsage, httpServletRequest));
			}
			catch (Exception exception) {
				if (_log.isDebugEnabled()) {
					_log.debug(
						StringBundler.concat(
							"An error occurred while getting mapped content ",
							"with class PK ",
							layoutClassedModelUsage.getClassPK(),
							" and class name ID ",
							layoutClassedModelUsage.getClassNameId()),
						exception);
				}
			}

			uniqueLayoutClassedModelUsageKeys.add(
				_generateUniqueLayoutClassedModelUsageKey(
					layoutClassedModelUsage));
		}

		return mappedContentsJSONArray;
	}

	private String _generateUniqueLayoutClassedModelUsageKey(
		LayoutClassedModelUsage layoutClassedModelUsage) {

		return layoutClassedModelUsage.getClassNameId() + StringPool.DASH +
			layoutClassedModelUsage.getClassPK();
	}

	private JSONObject _getActionsJSONObject(
			LayoutClassedModelUsage layoutClassedModelUsage,
			ThemeDisplay themeDisplay, HttpServletRequest httpServletRequest)
		throws Exception {

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		String className = layoutClassedModelUsage.getClassName();

		LayoutDisplayPageProvider<?> layoutDisplayPageProvider =
			LayoutDisplayPageProviderTrackerUtil.getLayoutDisplayPageProvider(
				layoutClassedModelUsage.getClassName());

		LayoutDisplayPageObjectProvider<?> layoutDisplayPageObjectProvider =
			layoutDisplayPageProvider.getLayoutDisplayPageObjectProvider(
				new InfoItemReference(
					className, layoutClassedModelUsage.getClassPK()));

		if (_layoutContentModelResourcePermission.contains(
				themeDisplay.getPermissionChecker(),
				layoutClassedModelUsage.getClassName(),
				layoutClassedModelUsage.getClassPK(), ActionKeys.UPDATE)) {

			String editURL = InfoEditURLProviderUtil.getURLEdit(
				layoutClassedModelUsage.getClassName(),
				layoutDisplayPageObjectProvider.getDisplayObject(),
				httpServletRequest);

			if (editURL != null) {
				jsonObject.put("editURL", editURL);
			}
		}

		if (_layoutContentModelResourcePermission.contains(
				themeDisplay.getPermissionChecker(),
				layoutClassedModelUsage.getClassName(),
				layoutClassedModelUsage.getClassPK(), ActionKeys.PERMISSIONS)) {

			String permissionsURL = PermissionsURLTag.doTag(
				StringPool.BLANK, layoutClassedModelUsage.getClassName(),
				HtmlUtil.escape(
					layoutDisplayPageObjectProvider.getTitle(
						themeDisplay.getLocale())),
				null, String.valueOf(layoutClassedModelUsage.getClassPK()),
				LiferayWindowState.POP_UP.toString(), null, httpServletRequest);

			if (Validator.isNotNull(permissionsURL)) {
				jsonObject.put("permissionsURL", permissionsURL);
			}
		}

		return jsonObject.put(
			"viewUsagesURL",
			PortletURLBuilder.create(
				PortletURLFactoryUtil.create(
					httpServletRequest,
					ContentPageEditorPortletKeys.CONTENT_PAGE_EDITOR_PORTLET,
					PortletRequest.RENDER_PHASE)
			).setMVCPath(
				"/view_layout_classed_model_usages.jsp"
			).setParameter(
				"className", layoutClassedModelUsage.getClassName()
			).setParameter(
				"classPK", layoutClassedModelUsage.getClassPK()
			).setWindowState(
				LiferayWindowState.POP_UP
			).buildString());
	}

	private String _getIcon(String className) {
		if (Objects.equals(FileEntry.class.getName(), className)) {
			return "document-image";
		}

		if (Objects.equals(JournalArticle.class.getName(), className)) {
			return "web-content";
		}

		return "blogs";
	}

	private JSONObject _getPageContentJSONObject(
			LayoutClassedModelUsage layoutClassedModelUsage,
			HttpServletRequest httpServletRequest)
		throws Exception {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		JSONObject mappedContentJSONObject = JSONUtil.put(
			"actions",
			_getActionsJSONObject(
				layoutClassedModelUsage, themeDisplay, httpServletRequest)
		).put(
			"className", layoutClassedModelUsage.getClassName()
		).put(
			"classNameId", layoutClassedModelUsage.getClassNameId()
		).put(
			"classPK", layoutClassedModelUsage.getClassPK()
		).put(
			"icon", _getIcon(layoutClassedModelUsage.getClassName())
		);

		LayoutDisplayPageProvider<?> layoutDisplayPageProvider =
			LayoutDisplayPageProviderTrackerUtil.getLayoutDisplayPageProvider(
				layoutClassedModelUsage.getClassName());

		LayoutDisplayPageObjectProvider<?> layoutDisplayPageObjectProvider =
			layoutDisplayPageProvider.getLayoutDisplayPageObjectProvider(
				new InfoItemReference(
					layoutClassedModelUsage.getClassName(),
					layoutClassedModelUsage.getClassPK()));

		return mappedContentJSONObject.put(
			"status", _getStatusJSONObject(layoutClassedModelUsage)
		).put(
			"subtype",
			_getSubtype(
				layoutClassedModelUsage.getClassName(),
				layoutDisplayPageObjectProvider.getClassTypeId(),
				themeDisplay.getLocale())
		).put(
			"title",
			layoutDisplayPageObjectProvider.getTitle(themeDisplay.getLocale())
		).put(
			"type",
			ResourceActionsUtil.getModelResource(
				themeDisplay.getLocale(),
				layoutClassedModelUsage.getClassName())
		).put(
			"usagesCount",
			_layoutClassedModelUsageLocalService.
				getUniqueLayoutClassedModelUsagesCount(
					layoutClassedModelUsage.getClassNameId(),
					layoutClassedModelUsage.getClassPK())
		);
	}

	private JSONObject _getStatusJSONObject(
			LayoutClassedModelUsage layoutClassedModelUsage)
		throws Exception {

		AssetRendererFactory<?> assetRendererFactory =
			AssetRendererFactoryRegistryUtil.
				getAssetRendererFactoryByClassNameId(
					layoutClassedModelUsage.getClassNameId());

		if (assetRendererFactory == null) {
			return JSONUtil.put(
				"hasApprovedVersion", false
			).put(
				"label",
				WorkflowConstants.getStatusLabel(
					WorkflowConstants.STATUS_APPROVED)
			).put(
				"style",
				WorkflowConstants.getStatusStyle(
					WorkflowConstants.STATUS_APPROVED)
			);
		}

		AssetRenderer<?> latestAssetRenderer =
			assetRendererFactory.getAssetRenderer(
				layoutClassedModelUsage.getClassPK(),
				AssetRendererFactory.TYPE_LATEST);

		boolean hasApprovedVersion = false;

		if (latestAssetRenderer.getStatus() !=
				WorkflowConstants.STATUS_APPROVED) {

			AssetRenderer<?> assetRenderer =
				assetRendererFactory.getAssetRenderer(
					layoutClassedModelUsage.getClassPK(),
					AssetRendererFactory.TYPE_LATEST_APPROVED);

			if (assetRenderer.getStatus() ==
					WorkflowConstants.STATUS_APPROVED) {

				hasApprovedVersion = true;
			}
		}

		return JSONUtil.put(
			"hasApprovedVersion", hasApprovedVersion
		).put(
			"label",
			WorkflowConstants.getStatusLabel(latestAssetRenderer.getStatus())
		).put(
			"style",
			WorkflowConstants.getStatusStyle(latestAssetRenderer.getStatus())
		);
	}

	private String _getSubtype(
			String className, long classTypeId, Locale locale)
		throws Exception {

		// LPS-111037

		if (Objects.equals(className, FileEntry.class.getName())) {
			className = DLFileEntry.class.getName();
		}

		AssetRendererFactory<?> assetRendererFactory =
			AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(
				className);

		if (assetRendererFactory == null) {
			return StringPool.BLANK;
		}

		ClassTypeReader classTypeReader =
			assetRendererFactory.getClassTypeReader();

		try {
			ClassType classType = classTypeReader.getClassType(
				classTypeId, locale);

			return classType.getName();
		}
		catch (Exception exception) {
			return StringPool.BLANK;
		}
	}

	private boolean _isFragmentEntryLinkDeleted(
		FragmentEntryLink fragmentEntryLink) {

		try {
			LayoutStructure layoutStructure =
				LayoutStructureUtil.getLayoutStructure(
					fragmentEntryLink.getGroupId(), fragmentEntryLink.getPlid(),
					fragmentEntryLink.getSegmentsExperienceId());

			LayoutStructureItem layoutStructureItem =
				layoutStructure.getLayoutStructureItemByFragmentEntryLinkId(
					fragmentEntryLink.getFragmentEntryLinkId());

			if (ListUtil.exists(
					layoutStructure.getDeletedLayoutStructureItems(),
					deletedLayoutStructureItem -> Objects.equals(
						deletedLayoutStructureItem.getItemId(),
						layoutStructureItem.getItemId()))) {

				return true;
			}

			return false;
		}
		catch (PortalException portalException) {
			_log.error(portalException, portalException);

			return true;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LayoutClassedModelUsageLayoutContentsContributor.class);

	@Reference
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@Reference
	private LayoutClassedModelUsageLocalService
		_layoutClassedModelUsageLocalService;

	@Reference
	private LayoutContentModelResourcePermission
		_layoutContentModelResourcePermission;

	@Reference
	private Portal _portal;

}