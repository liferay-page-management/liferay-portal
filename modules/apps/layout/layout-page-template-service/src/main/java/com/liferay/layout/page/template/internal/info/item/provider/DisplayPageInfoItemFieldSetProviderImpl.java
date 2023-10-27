/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.internal.info.item.provider;

import com.liferay.info.field.InfoField;
import com.liferay.info.field.InfoFieldSet;
import com.liferay.info.field.InfoFieldSetEntry;
import com.liferay.info.field.InfoFieldValue;
import com.liferay.info.field.type.DisplayPageInfoFieldType;
import com.liferay.info.field.type.InfoFieldType;
import com.liferay.info.field.type.URLInfoFieldType;
import com.liferay.info.item.InfoItemReference;
import com.liferay.info.localized.InfoLocalizedValue;
import com.liferay.info.localized.bundle.FunctionInfoLocalizedValue;
import com.liferay.info.type.WebURL;
import com.liferay.layout.page.template.constants.LayoutPageTemplateEntryTypeConstants;
import com.liferay.layout.page.template.info.item.provider.DisplayPageInfoItemFieldSetProvider;
import com.liferay.layout.page.template.info.item.provider.DisplayPageURLProvider;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Eudaldo Alonso
 */
@Component(service = DisplayPageInfoItemFieldSetProvider.class)
public class DisplayPageInfoItemFieldSetProviderImpl
	implements DisplayPageInfoItemFieldSetProvider {

	@Override
	public InfoFieldSet getInfoFieldSet(
		String itemClassName, String infoItemFormVariationKey, String namespace,
		long scopeGroupId) {

		return InfoFieldSet.builder(
		).infoFieldSetEntries(
			_getInfoFieldSetEntries(
				itemClassName, infoItemFormVariationKey, namespace,
				scopeGroupId)
		).labelInfoLocalizedValue(
			InfoLocalizedValue.localize(getClass(), "display-page")
		).name(
			"display-page"
		).build();
	}

	@Override
	public List<InfoFieldValue<Object>> getInfoFieldValues(
			InfoItemReference infoItemReference,
			String infoItemFormVariationKey, String namespace,
			ThemeDisplay themeDisplay)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled("LPS-195205") ||
			(themeDisplay == null)) {

			return Collections.emptyList();
		}

		List<InfoFieldValue<Object>> infoFieldValues = new ArrayList<>();

		infoFieldValues.add(
			new InfoFieldValue<>(
				InfoField.builder(
					namespace
				).infoFieldType(
					URLInfoFieldType.INSTANCE
				).name(
					"displayPageURL"
				).labelInfoLocalizedValue(
					InfoLocalizedValue.localize(getClass(), "default")
				).build(),
				_displayPageURLProvider.getDefaultURL(
					infoItemReference, themeDisplay)));

		List<LayoutPageTemplateEntry> layoutPageTemplateEntries =
			_layoutPageTemplateEntryService.getLayoutPageTemplateEntries(
				themeDisplay.getScopeGroupId(),
				_portal.getClassNameId(infoItemReference.getClassName()),
				GetterUtil.getLong(infoItemFormVariationKey),
				LayoutPageTemplateEntryTypeConstants.DISPLAY_PAGE);

		for (LayoutPageTemplateEntry layoutPageTemplateEntry :
				layoutPageTemplateEntries) {

			infoFieldValues.add(
				new InfoFieldValue<>(
					InfoField.builder(
					).infoFieldType(
						URLInfoFieldType.INSTANCE
					).uniqueId(
						_getUniqueId(
							layoutPageTemplateEntry.
								getLayoutPageTemplateEntryKey())
					).name(
						layoutPageTemplateEntry.getName()
					).attribute(
						URLInfoFieldType.NOFOLLOW, Boolean.TRUE
					).labelInfoLocalizedValue(
						InfoLocalizedValue.singleValue(
							layoutPageTemplateEntry.getName())
					).build(),
					new FunctionInfoLocalizedValue<>(
						locale -> {
							WebURL webURL = null;

							try {
								webURL = new WebURL(
									_displayPageURLProvider.getURL(
										infoItemReference,
										layoutPageTemplateEntry, locale,
										themeDisplay));
							}
							catch (PortalException portalException) {
								if (_log.isDebugEnabled()) {
									_log.debug(portalException);
								}

								return null;
							}

							webURL.setNofollow(true);

							return webURL;
						})));
		}

		return infoFieldValues;
	}

	private InfoField<InfoFieldType> _getDefaultDisplayPageURLInfoField(
		String namespace) {

		return InfoField.builder(
			namespace
		).infoFieldType(
			_getDisplayPageInfoFieldType()
		).name(
			"displayPageURL"
		).labelInfoLocalizedValue(
			InfoLocalizedValue.localize(getClass(), "default")
		).build();
	}

	private InfoFieldType _getDisplayPageInfoFieldType() {
		return DisplayPageInfoFieldType.INSTANCE;
	}

	private List<InfoFieldSetEntry> _getInfoFieldSetEntries(
		String itemClassName, String infoItemFormVariationKey, String namespace,
		long scopeGroupId) {

		List<InfoFieldSetEntry> infoFieldSetEntries = new ArrayList<>();

		infoFieldSetEntries.add(_getDefaultDisplayPageURLInfoField(namespace));

		List<LayoutPageTemplateEntry> layoutPageTemplateEntries =
			_layoutPageTemplateEntryService.getLayoutPageTemplateEntries(
				scopeGroupId, _portal.getClassNameId(itemClassName),
				GetterUtil.getLong(infoItemFormVariationKey),
				LayoutPageTemplateEntryTypeConstants.DISPLAY_PAGE,
				WorkflowConstants.STATUS_APPROVED);

		for (LayoutPageTemplateEntry layoutPageTemplateEntry :
				layoutPageTemplateEntries) {

			infoFieldSetEntries.add(
				InfoField.builder(
				).infoFieldType(
					_getDisplayPageInfoFieldType()
				).uniqueId(
					_getUniqueId(
						layoutPageTemplateEntry.getLayoutPageTemplateEntryKey())
				).name(
					layoutPageTemplateEntry.getName()
				).labelInfoLocalizedValue(
					InfoLocalizedValue.singleValue(
						layoutPageTemplateEntry.getName())
				).build());
		}

		return infoFieldSetEntries;
	}

	private String _getUniqueId(String layoutPageTemplateEntryKey) {
		return LayoutPageTemplateEntry.class.getSimpleName() +
			StringPool.UNDERLINE + layoutPageTemplateEntryKey;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DisplayPageInfoItemFieldSetProviderImpl.class);

	@Reference
	private DisplayPageURLProvider _displayPageURLProvider;

	@Reference
	private LayoutPageTemplateEntryService _layoutPageTemplateEntryService;

	@Reference
	private Portal _portal;

}