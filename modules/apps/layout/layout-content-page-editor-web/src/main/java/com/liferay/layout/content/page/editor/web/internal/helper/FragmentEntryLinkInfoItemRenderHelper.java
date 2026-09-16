/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.content.page.editor.web.internal.helper;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.util.LinkedAssetEntryIdsUtil;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.renderer.DefaultFragmentRendererContext;
import com.liferay.info.constants.InfoDisplayWebKeys;
import com.liferay.info.exception.NoSuchInfoItemException;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.InfoItemIdentifier;
import com.liferay.info.item.InfoItemReference;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.info.item.provider.InfoItemDetailsProvider;
import com.liferay.info.item.provider.InfoItemObjectProvider;
import com.liferay.layout.content.page.editor.web.internal.manager.FragmentEntryLinkManager;
import com.liferay.layout.content.page.editor.web.internal.util.InfoItemReferenceUtil;
import com.liferay.layout.display.page.LayoutDisplayPageProvider;
import com.liferay.layout.display.page.LayoutDisplayPageProviderRegistry;
import com.liferay.layout.display.page.constants.LayoutDisplayPageWebKeys;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Chaitanya Sammetla
 */
@Component(service = FragmentEntryLinkInfoItemRenderHelper.class)
public class FragmentEntryLinkInfoItemRenderHelper {

	public JSONObject getFragmentEntryLinkJSONObject(
			FragmentEntryLink fragmentEntryLink,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse,
			InfoItemReference infoItemReference,
			LayoutStructure layoutStructure)
		throws PortalException {

		DefaultFragmentRendererContext defaultFragmentRendererContext =
			new DefaultFragmentRendererContext(fragmentEntryLink);

		LayoutDisplayPageProvider<?> currentLayoutDisplayPageProvider =
			(LayoutDisplayPageProvider<?>)httpServletRequest.getAttribute(
				LayoutDisplayPageWebKeys.LAYOUT_DISPLAY_PAGE_PROVIDER);

		if (infoItemReference != null) {
			_setInfoItemContext(
				defaultFragmentRendererContext, fragmentEntryLink,
				httpServletRequest, infoItemReference);
		}

		try {
			return _fragmentEntryLinkManager.getFragmentEntryLinkJSONObject(
				defaultFragmentRendererContext, fragmentEntryLink,
				httpServletRequest, httpServletResponse, layoutStructure);
		}
		finally {
			httpServletRequest.removeAttribute(
				InfoDisplayWebKeys.INFO_ITEM_REFERENCE);

			httpServletRequest.setAttribute(
				LayoutDisplayPageWebKeys.LAYOUT_DISPLAY_PAGE_PROVIDER,
				currentLayoutDisplayPageProvider);
		}
	}

	public JSONObject getFragmentEntryLinkJSONObject(
			FragmentEntryLink fragmentEntryLink,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, String itemClassName,
			long itemClassPK, String itemExternalReferenceCode,
			LayoutStructure layoutStructure)
		throws PortalException {

		return getFragmentEntryLinkJSONObject(
			fragmentEntryLink, httpServletRequest, httpServletResponse,
			InfoItemReferenceUtil.getInfoItemReference(
				itemClassName, itemClassPK, itemExternalReferenceCode),
			layoutStructure);
	}

	private void _addLinkedAssetEntryId(
		HttpServletRequest httpServletRequest, String className,
		InfoItemIdentifier infoItemIdentifier) {

		if (!(infoItemIdentifier instanceof ClassPKInfoItemIdentifier)) {
			return;
		}

		AssetRendererFactory<?> assetRendererFactory =
			AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(
				className);

		if (assetRendererFactory == null) {
			return;
		}

		ClassPKInfoItemIdentifier classPKInfoItemIdentifier =
			(ClassPKInfoItemIdentifier)infoItemIdentifier;

		try {
			AssetEntry assetEntry = assetRendererFactory.getAssetEntry(
				className, classPKInfoItemIdentifier.getClassPK());

			if (assetEntry != null) {
				LinkedAssetEntryIdsUtil.addLinkedAssetEntryId(
					httpServletRequest, assetEntry.getEntryId());
			}
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}
	}

	private Object _getInfoItemObject(
		String className, InfoItemIdentifier infoItemIdentifier) {

		InfoItemObjectProvider<Object> infoItemObjectProvider =
			_infoItemServiceRegistry.getFirstInfoItemService(
				InfoItemObjectProvider.class, className,
				infoItemIdentifier.getInfoItemServiceFilter());

		if (infoItemObjectProvider == null) {
			return null;
		}

		try {
			return infoItemObjectProvider.getInfoItem(infoItemIdentifier);
		}
		catch (NoSuchInfoItemException noSuchInfoItemException) {
			if (_log.isDebugEnabled()) {
				_log.debug(noSuchInfoItemException);
			}

			return null;
		}
	}

	private void _setInfoItemContext(
		DefaultFragmentRendererContext defaultFragmentRendererContext,
		FragmentEntryLink fragmentEntryLink,
		HttpServletRequest httpServletRequest,
		InfoItemReference infoItemReference) {

		String className = infoItemReference.getClassName();
		InfoItemIdentifier infoItemIdentifier =
			infoItemReference.getInfoItemIdentifier();

		Object infoItemObject = _getInfoItemObject(
			className, infoItemIdentifier);

		if (infoItemObject != null) {
			defaultFragmentRendererContext.setContextInfoItemReference(
				infoItemReference);

			httpServletRequest.setAttribute(
				InfoDisplayWebKeys.INFO_ITEM, infoItemObject);

			InfoItemDetailsProvider infoItemDetailsProvider =
				_infoItemServiceRegistry.getFirstInfoItemService(
					InfoItemDetailsProvider.class, className);

			if (infoItemDetailsProvider != null) {
				httpServletRequest.setAttribute(
					InfoDisplayWebKeys.INFO_ITEM_DETAILS,
					infoItemDetailsProvider.getInfoItemDetails(infoItemObject));
			}

			httpServletRequest.setAttribute(
				InfoDisplayWebKeys.INFO_ITEM_REFERENCE, infoItemReference);

			_addLinkedAssetEntryId(
				httpServletRequest, className, infoItemIdentifier);
		}

		LayoutDisplayPageProvider<?> layoutDisplayPageProvider =
			_layoutDisplayPageProviderRegistry.
				getLayoutDisplayPageProviderByClassName(
					fragmentEntryLink.getCompanyId(), className);

		if (layoutDisplayPageProvider != null) {
			httpServletRequest.setAttribute(
				LayoutDisplayPageWebKeys.LAYOUT_DISPLAY_PAGE_PROVIDER,
				layoutDisplayPageProvider);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FragmentEntryLinkInfoItemRenderHelper.class);

	@Reference
	private FragmentEntryLinkManager _fragmentEntryLinkManager;

	@Reference
	private InfoItemServiceRegistry _infoItemServiceRegistry;

	@Reference
	private LayoutDisplayPageProviderRegistry
		_layoutDisplayPageProviderRegistry;

}