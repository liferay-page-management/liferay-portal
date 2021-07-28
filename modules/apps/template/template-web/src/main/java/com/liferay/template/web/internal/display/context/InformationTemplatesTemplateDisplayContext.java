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

package com.liferay.template.web.internal.display.context;

import com.liferay.dynamic.data.mapping.configuration.DDMWebConfiguration;
import com.liferay.dynamic.data.mapping.constants.DDMActionKeys;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenuBuilder;
import com.liferay.info.form.InfoForm;
import com.liferay.info.item.InfoItemFormVariation;
import com.liferay.info.item.InfoItemServiceTracker;
import com.liferay.info.item.provider.InfoItemFormProvider;
import com.liferay.info.item.provider.InfoItemFormVariationsProvider;
import com.liferay.petra.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.template.constants.TemplatePortletKeys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Eudaldo Alonso
 * @author Lourdes Fernández Besada
 */
public class InformationTemplatesTemplateDisplayContext
	extends BaseTemplateDisplayContext {

	public InformationTemplatesTemplateDisplayContext(
		DDMWebConfiguration ddmWebConfiguration,
		InfoItemServiceTracker infoItemServiceTracker,
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse) {

		super(
			ddmWebConfiguration, liferayPortletRequest, liferayPortletResponse);

		_infoItemServiceTracker = infoItemServiceTracker;
	}

	public Map<String, Object> getAdditionalProps() {
		return HashMapBuilder.<String, Object>put(
			"addDDMTemplateURL",
			PortletURLBuilder.createActionURL(
				liferayPortletResponse
			).setActionName(
				"/template/add_ddm_template"
			).setRedirect(
				themeDisplay.getURLCurrent()
			).setParameter(
				"groupId", themeDisplay.getScopeGroupId()
			).setParameter(
				"resourceClassNameId", getResourceClassNameId()
			).buildString()
		).put(
			"itemTypes", _getItemTypesJSONArray()
		).build();
	}

	@Override
	public long[] getClassNameIds() {
		if (_classNameIds != null) {
			return _classNameIds;
		}

		List<String> infoItemClassNamesList =
			_infoItemServiceTracker.getInfoItemClassNames(
				InfoItemFormProvider.class);

		Stream<String> infoItemClassNamesStream =
			infoItemClassNamesList.stream();

		_classNameIds = infoItemClassNamesStream.mapToLong(
			className -> PortalUtil.getClassNameId(className)
		).toArray();

		return _classNameIds;
	}

	@Override
	public long getResourceClassNameId() {
		if (_resourceClassNameId != null) {
			return _resourceClassNameId;
		}

		_resourceClassNameId = PortalUtil.getClassNameId(
			InfoItemFormProvider.class);

		return _resourceClassNameId;
	}

	@Override
	public String getTemplateType(long classNameId) {
		return ResourceActionsUtil.getModelResource(
			themeDisplay.getLocale(), PortalUtil.getClassName(classNameId));
	}

	@Override
	protected CreationMenu buildCreationMenu() {
		if (!containsAddPortletDisplayTemplatePermission(
				TemplatePortletKeys.TEMPLATE, DDMActionKeys.ADD_TEMPLATE)) {

			return null;
		}

		return CreationMenuBuilder.addDropdownItem(
			dropdownItem -> {
				dropdownItem.putData("action", "addInformationTemplate");
				dropdownItem.setLabel(
					LanguageUtil.get(themeDisplay.getLocale(), "add"));
			}
		).build();
	}

	private JSONArray _getItemTypesJSONArray() {
		JSONArray itemTypesJSONArray = JSONFactoryUtil.createJSONArray();

		if (!containsAddPortletDisplayTemplatePermission(
				TemplatePortletKeys.TEMPLATE, DDMActionKeys.ADD_TEMPLATE)) {

			return itemTypesJSONArray;
		}

		List<String> infoItemClassNamesList =
			_infoItemServiceTracker.getInfoItemClassNames(
				InfoItemFormProvider.class);

		Stream<String> infoItemClassNamesStream =
			infoItemClassNamesList.stream();

		List<InfoForm> infoFormsList = infoItemClassNamesStream.map(
			infoItemClassName ->
				_infoItemServiceTracker.getFirstInfoItemService(
					InfoItemFormProvider.class, infoItemClassName)
		).map(
			infoItemFormProvider -> infoItemFormProvider.getInfoForm()
		).filter(
			infoForm -> Validator.isNotNull(infoForm.getName())
		).collect(
			Collectors.toList()
		);

		infoFormsList.sort(
			Comparator.comparing(
				infoForm -> infoForm.getLabel(themeDisplay.getLocale())));

		for (InfoForm infoForm : infoFormsList) {
			JSONArray itemSubtypesJSONArray = JSONFactoryUtil.createJSONArray();

			long classNameId = PortalUtil.getClassNameId(infoForm.getName());

			InfoItemFormVariationsProvider<?> infoItemFormVariationsProvider =
				_infoItemServiceTracker.getFirstInfoItemService(
					InfoItemFormVariationsProvider.class, infoForm.getName());

			if (infoItemFormVariationsProvider != null) {
				Collection<InfoItemFormVariation>
					infoItemFormVariationCollection =
						infoItemFormVariationsProvider.
							getInfoItemFormVariations(
								themeDisplay.getScopeGroupId());

				List<InfoItemFormVariation> infoItemFormVariationList =
					new ArrayList<>(infoItemFormVariationCollection);

				infoItemFormVariationList.sort(
					Comparator.comparing(
						infoItemFormVariation -> infoItemFormVariation.getLabel(
							themeDisplay.getLocale())));

				for (InfoItemFormVariation infoItemFormVariation :
						infoItemFormVariationList) {

					itemSubtypesJSONArray.put(
						JSONUtil.put(
							"label",
							infoItemFormVariation.getLabel(
								themeDisplay.getLocale())
						).put(
							"value", infoItemFormVariation.getKey()
						));
				}
			}

			itemTypesJSONArray.put(
				JSONUtil.put(
					"label", infoForm.getLabel(themeDisplay.getLocale())
				).put(
					"subtypes", itemSubtypesJSONArray
				).put(
					"value", String.valueOf(classNameId)
				));
		}

		return itemTypesJSONArray;
	}

	private long[] _classNameIds;
	private final InfoItemServiceTracker _infoItemServiceTracker;
	private Long _resourceClassNameId;

}