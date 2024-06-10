/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.info.collection.provider.item.selector.web.internal.item.selector;

import com.liferay.info.collection.provider.item.selector.criterion.RepeatableFieldInfoCollectionProviderItemSelectorCriterion;
import com.liferay.info.exception.NoSuchFormVariationException;
import com.liferay.info.field.InfoField;
import com.liferay.info.field.InfoFieldSetEntry;
import com.liferay.info.form.InfoForm;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.info.item.provider.RepeatableFieldsInfoItemFormProvider;
import com.liferay.info.list.provider.item.selector.criterion.InfoListProviderItemSelectorReturnType;
import com.liferay.item.selector.ItemSelectorReturnType;
import com.liferay.item.selector.ItemSelectorView;
import com.liferay.item.selector.ItemSelectorViewDescriptorRenderer;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.IOException;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.portlet.PortletURL;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán
 */
@Component(service = ItemSelectorView.class)
public class RepeatableFieldInfoCollectionProviderItemSelectorView
	implements ItemSelectorView
		<RepeatableFieldInfoCollectionProviderItemSelectorCriterion> {

	@Override
	public Class
		<? extends RepeatableFieldInfoCollectionProviderItemSelectorCriterion>
			getItemSelectorCriterionClass() {

		return RepeatableFieldInfoCollectionProviderItemSelectorCriterion.class;
	}

	@Override
	public List<ItemSelectorReturnType> getSupportedItemSelectorReturnTypes() {
		return _supportedItemSelectorReturnTypes;
	}

	@Override
	public String getTitle(Locale locale) {
		return _language.get(locale, "repeatable-field-collection-providers");
	}

	@Override
	public boolean isVisible(
		RepeatableFieldInfoCollectionProviderItemSelectorCriterion
			itemSelectorCriterion,
		ThemeDisplay themeDisplay) {

		RepeatableFieldsInfoItemFormProvider<?>
			repeatableFieldsInfoItemFormProvider =
				_getRepeatableFieldsInfoItemFormProvider(
					itemSelectorCriterion.getItemType());

		if ((repeatableFieldsInfoItemFormProvider != null) &&
			FeatureFlagManagerUtil.isEnabled("LPD-11377")) {

			return true;
		}

		return false;
	}

	@Override
	public void renderHTML(
			ServletRequest servletRequest, ServletResponse servletResponse,
			RepeatableFieldInfoCollectionProviderItemSelectorCriterion
				repeatableFieldInfoCollectionProviderItemSelectorCriterion,
			PortletURL portletURL, String itemSelectedEventName, boolean search)
		throws IOException, ServletException {

		String itemType =
			repeatableFieldInfoCollectionProviderItemSelectorCriterion.
				getItemType();

		String itemSubtype =
			repeatableFieldInfoCollectionProviderItemSelectorCriterion.
				getItemSubtype();

		_itemSelectorViewDescriptorRenderer.renderHTML(
			servletRequest, servletResponse,
			repeatableFieldInfoCollectionProviderItemSelectorCriterion,
			portletURL, itemSelectedEventName, search,
			new RepeatableFieldInfoCollectionProviderItemSelectorViewDescriptor(
				_getRepeatableInfoFields(
					(HttpServletRequest)servletRequest, itemType, itemSubtype),
				itemType, itemSubtype, (HttpServletRequest)servletRequest,
				portletURL));
	}

	private RepeatableFieldsInfoItemFormProvider<?>
		_getRepeatableFieldsInfoItemFormProvider(String itemType) {

		return _infoItemServiceRegistry.getFirstInfoItemService(
			RepeatableFieldsInfoItemFormProvider.class, itemType);
	}

	private List<InfoFieldSetEntry> _getRepeatableInfoFields(
		HttpServletRequest httpServletRequest, String itemType,
		String itemSubtype) {

		RepeatableFieldsInfoItemFormProvider<?>
			repeatableFieldsInfoItemFormProvider =
				_getRepeatableFieldsInfoItemFormProvider(itemType);

		if (repeatableFieldsInfoItemFormProvider == null) {
			return Collections.emptyList();
		}

		try {
			ThemeDisplay themeDisplay =
				(ThemeDisplay)httpServletRequest.getAttribute(
					WebKeys.THEME_DISPLAY);

			InfoForm infoForm =
				repeatableFieldsInfoItemFormProvider.
					getRepeatableFieldsInfoForm(
						itemSubtype, themeDisplay.getScopeGroupId());

			return ListUtil.filter(
				infoForm.getInfoFieldSetEntries(),
				infoFieldSetEntry -> {
					if (infoFieldSetEntry instanceof InfoField) {
						InfoField<?> infoField =
							(InfoField<?>)infoFieldSetEntry;

						return infoField.isRepeatable();
					}

					return true;
				});
		}
		catch (NoSuchFormVariationException noSuchFormVariationException) {
			_log.error(noSuchFormVariationException);

			return Collections.emptyList();
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		RepeatableFieldInfoCollectionProviderItemSelectorView.class);

	private static final List<ItemSelectorReturnType>
		_supportedItemSelectorReturnTypes = Collections.singletonList(
			new InfoListProviderItemSelectorReturnType());

	@Reference
	private InfoItemServiceRegistry _infoItemServiceRegistry;

	@Reference
	private ItemSelectorViewDescriptorRenderer
		<RepeatableFieldInfoCollectionProviderItemSelectorCriterion>
			_itemSelectorViewDescriptorRenderer;

	@Reference
	private Language _language;

}