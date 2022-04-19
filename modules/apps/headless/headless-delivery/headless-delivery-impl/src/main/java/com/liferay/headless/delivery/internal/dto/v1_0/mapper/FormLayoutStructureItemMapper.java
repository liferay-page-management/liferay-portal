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

package com.liferay.headless.delivery.internal.dto.v1_0.mapper;

import com.liferay.headless.delivery.dto.v1_0.ClassTypeReference;
import com.liferay.headless.delivery.dto.v1_0.ContextReference;
import com.liferay.headless.delivery.dto.v1_0.PageElement;
import com.liferay.headless.delivery.dto.v1_0.PageFormDefinition;
import com.liferay.headless.delivery.dto.v1_0.Source;
import com.liferay.layout.util.structure.FormStyledLayoutStructureItem;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.Portal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Eudaldo Alonso
 */
@Component(service = LayoutStructureItemMapper.class)
public class FormLayoutStructureItemMapper
	extends BaseStyledLayoutStructureItemMapper {

	@Override
	public String getClassName() {
		return FormStyledLayoutStructureItem.class.getName();
	}

	@Override
	public PageElement getPageElement(
		long groupId, LayoutStructureItem layoutStructureItem,
		boolean saveInlineContent, boolean saveMappingConfiguration) {

		FormStyledLayoutStructureItem formStyledLayoutStructureItem =
			(FormStyledLayoutStructureItem)layoutStructureItem;

		return new PageElement() {
			{
				definition = new PageFormDefinition() {
					{
						source = new Source() {
							{
								itemReference = _toItemReference(
									formStyledLayoutStructureItem);
							}
						};

						setFragmentStyle(
							() -> {
								JSONObject itemConfigJSONObject =
									formStyledLayoutStructureItem.
										getItemConfigJSONObject();

								return toFragmentStyle(
									itemConfigJSONObject.getJSONObject(
										"styles"),
									saveMappingConfiguration);
							});
						setFragmentViewports(
							() -> getFragmentViewPorts(
								formStyledLayoutStructureItem.
									getItemConfigJSONObject()));
					}
				};
				type = Type.FORM;
			}
		};
	}

	private Object _toItemReference(
		FormStyledLayoutStructureItem formStyledLayoutStructureItem) {

		if (formStyledLayoutStructureItem.getSource() ==
				FormStyledLayoutStructureItem.OTHER_ITEM_TYPE) {

			return new ClassTypeReference() {
				{
					className = _portal.getClassName(
						formStyledLayoutStructureItem.getClassNameId());
					classTypeId =
						formStyledLayoutStructureItem.getClassTypeId();
				}
			};
		}

		return new ContextReference() {
			{
				contextSource = ContextSource.DISPLAY_PAGE_ITEM;
			}
		};
	}

	@Reference
	private Portal _portal;

}