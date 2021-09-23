/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.search.experiences.internal.blueprint.parameter.contributor;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.search.experiences.blueprint.parameter.LongArraySXPParameter;
import com.liferay.search.experiences.blueprint.parameter.LongSXPParameter;
import com.liferay.search.experiences.blueprint.parameter.SXPParameter;
import com.liferay.search.experiences.blueprint.parameter.contributor.SXPParameterContributor;
import com.liferay.search.experiences.blueprint.parameter.contributor.SXPParameterContributorDefinition;
import com.liferay.search.experiences.model.SXPBlueprint;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Petteri Karttunen
 */
public class CommerceSXPParameterContributor
	implements SXPParameterContributor {

	@Override
	public void contribute(
		SearchContext searchContext, SXPBlueprint sxpBlueprint,
		Set<SXPParameter> sxpParameters) {

		long[] commerceAccountGroupIds = GetterUtil.getLongValues(
			searchContext.getAttribute("commerceAccountGroupIds"));

		if (!ArrayUtil.isEmpty(commerceAccountGroupIds)) {
			sxpParameters.add(
				new LongArraySXPParameter(
					"commerce.account_group_ids", true,
					ArrayUtil.toLongArray(commerceAccountGroupIds)));
		}

		long commerceChannelGroupId = GetterUtil.getLong(
			searchContext.getAttribute("commerceChannelGroupId"));

		if (commerceChannelGroupId > 0) {
			sxpParameters.add(
				new LongSXPParameter(
					"commerce.channel_group_id", true, commerceChannelGroupId));
		}
	}

	@Override
	public String getSXPParameterCategoryNameKey() {
		return "commerce";
	}

	@Override
	public List<SXPParameterContributorDefinition>
		getSXPParameterContributorDefinitions(long companyId) {

		return Arrays.asList(
			new SXPParameterContributorDefinition(
				LongSXPParameter.class, "account-group-ids",
				"commerce.account_group_ids"),
			new SXPParameterContributorDefinition(
				LongSXPParameter.class, "channel-group-id",
				"commerce.channel_group_id"));
	}

}