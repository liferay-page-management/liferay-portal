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

package com.liferay.info.item.provider;

import com.liferay.info.exception.NoSuchClassTypeException;
import com.liferay.info.exception.NoSuchInfoFormVariationException;
import com.liferay.info.form.InfoForm;
import com.liferay.portal.kernel.util.GetterUtil;

/**
 * @author Jorge Ferrer
 */
public interface InfoItemFormProvider<T> {

	public InfoForm getInfoForm();

	public default InfoForm getInfoForm(long itemClassTypeId)
		throws NoSuchClassTypeException {

		return getInfoForm();
	}

	public default InfoForm getInfoForm(
			long groupId, String infoFormVariationKey)
		throws NoSuchInfoFormVariationException {

		return getInfoForm(infoFormVariationKey);
	}

	public default InfoForm getInfoForm(String infoFormVariationKey)
		throws NoSuchInfoFormVariationException {

		long itemClassTypeId = GetterUtil.getLong(infoFormVariationKey);

		if (itemClassTypeId > 0) {
			try {
				return getInfoForm(itemClassTypeId);
			}
			catch (NoSuchClassTypeException noSuchClassTypeException) {
				throw new NoSuchInfoFormVariationException(
					String.valueOf(noSuchClassTypeException.getClassTypeId()),
					noSuchClassTypeException.getCause());
			}
		}

		return getInfoForm();
	}

	public default InfoForm getInfoForm(T t) {
		return getInfoForm();
	}

}