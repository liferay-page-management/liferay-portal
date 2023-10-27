/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.internal.info.item.provider;

import com.liferay.asset.display.page.portlet.AssetDisplayPageFriendlyURLProvider;
import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.ERCInfoItemIdentifier;
import com.liferay.info.item.InfoItemIdentifier;
import com.liferay.info.item.InfoItemReference;
import com.liferay.layout.page.template.info.item.provider.DisplayPageURLProvider;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rubén Pulido
 */
@Component(service = DisplayPageURLProvider.class)
public class DisplayPageURLProviderImpl implements DisplayPageURLProvider {

	@Override
	public String getDefaultURL(
			InfoItemReference infoItemReference, ThemeDisplay themeDisplay)
		throws PortalException {

		AssetRendererFactory<?> assetRendererFactory =
			AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(
				infoItemReference.getClassName());

		if (assetRendererFactory == null) {
			return _assetDisplayPageFriendlyURLProvider.getFriendlyURL(
				infoItemReference, themeDisplay);
		}

		try {
			AssetRenderer<?> assetRenderer = null;

			if (infoItemReference.getInfoItemIdentifier() instanceof
					ClassPKInfoItemIdentifier) {

				ClassPKInfoItemIdentifier classPKInfoItemIdentifier =
					(ClassPKInfoItemIdentifier)
						infoItemReference.getInfoItemIdentifier();

				assetRenderer = assetRendererFactory.getAssetRenderer(
					classPKInfoItemIdentifier.getClassPK());
			}

			if (assetRenderer == null) {
				return _assetDisplayPageFriendlyURLProvider.getFriendlyURL(
					infoItemReference, themeDisplay);
			}

			String viewInContextURL = assetRenderer.getURLViewInContext(
				themeDisplay, StringPool.BLANK);

			if (Validator.isNotNull(viewInContextURL)) {
				return viewInContextURL;
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}

		return _assetDisplayPageFriendlyURLProvider.getFriendlyURL(
			infoItemReference, themeDisplay);
	}

	@Override
	public String getURL(
			InfoItemReference infoItemReference,
			LayoutPageTemplateEntry layoutPageTemplateEntry, Locale locale,
			ThemeDisplay themeDisplay)
		throws PortalException {

		Layout layout = _layoutLocalService.fetchLayout(
			layoutPageTemplateEntry.getPlid());

		Group group = themeDisplay.getScopeGroup();

		return StringBundler.concat(
			_portal.getGroupFriendlyURL(
				group.getPublicLayoutSet(), themeDisplay, false, false),
			"/e", layout.getFriendlyURL(locale), StringPool.SLASH,
			_portal.getClassNameId(infoItemReference.getClassName()),
			StringPool.SLASH, _getInfoItemIdentifier(infoItemReference));
	}

	private String _getInfoItemIdentifier(InfoItemReference infoItemReference) {
		InfoItemIdentifier infoItemIdentifier =
			infoItemReference.getInfoItemIdentifier();

		if (infoItemIdentifier instanceof ClassPKInfoItemIdentifier) {
			ClassPKInfoItemIdentifier classPKInfoItemIdentifier =
				(ClassPKInfoItemIdentifier)infoItemIdentifier;

			return String.valueOf(classPKInfoItemIdentifier.getClassPK());
		}

		if (infoItemIdentifier instanceof ERCInfoItemIdentifier) {
			ERCInfoItemIdentifier ercInfoItemIdentifier =
				(ERCInfoItemIdentifier)infoItemIdentifier;

			return ercInfoItemIdentifier.getExternalReferenceCode();
		}

		return StringPool.BLANK;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DisplayPageURLProviderImpl.class);

	@Reference
	private AssetDisplayPageFriendlyURLProvider
		_assetDisplayPageFriendlyURLProvider;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private Portal _portal;

}