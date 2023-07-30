/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.kernel.model.adapter;

import com.liferay.asset.kernel.model.AssetLink;
import com.liferay.portal.kernel.model.StagedModel;

/**
 * @author Máté Thurzó
 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
 *             com.liferay.asset.link.model.adapter.StagedAssetLink}
 */
@Deprecated
public interface StagedAssetLink extends AssetLink, StagedModel {

	public String getEntry1ClassName();

	public String getEntry1Uuid();

	public String getEntry2ClassName();

	public String getEntry2Uuid();

}