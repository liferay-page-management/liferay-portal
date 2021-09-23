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
import com.liferay.search.experiences.blueprint.parameter.BooleanSXPParameter;
import com.liferay.search.experiences.blueprint.parameter.SXPParameter;
import com.liferay.search.experiences.blueprint.parameter.StringArraySXPParameter;
import com.liferay.search.experiences.blueprint.parameter.contributor.SXPParameterContributor;
import com.liferay.search.experiences.blueprint.parameter.contributor.SXPParameterContributorDefinition;
import com.liferay.search.experiences.model.SXPBlueprint;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Petteri Karttunen
 */
public class SystemSXPParameterContributor implements SXPParameterContributor {

	@Override
	public void contribute(
		SearchContext searchContext, SXPBlueprint sxpBlueprint,
		Set<SXPParameter> sxpParameters) {

		String[] stringArrayValue = (String[])searchContext.getAttribute(
			"search.experiences.excluded_search_request_body_contributors");

		if (stringArrayValue != null) {
			sxpParameters.add(
				new StringArraySXPParameter(
					"system.excluded_search_request_body_contributors", false,
					stringArrayValue));
		}

		Boolean booleanValue = (Boolean)searchContext.getAttribute(
			"search.experiences.explain");

		if (booleanValue != null) {
			sxpParameters.add(
				new BooleanSXPParameter("explain", false, booleanValue));
		}

		booleanValue = (Boolean)searchContext.getAttribute(
			"search.experiences.include_response_string");

		if (booleanValue != null) {
			sxpParameters.add(
				new BooleanSXPParameter(
					"system.include_response_string", false, booleanValue));
		}

		booleanValue = (Boolean)searchContext.getAttribute(
			"search.experiences.preview");

		if (booleanValue != null) {
			sxpParameters.add(
				new BooleanSXPParameter("preview", false, booleanValue));
		}
	}

	@Override
	public String getSXPParameterCategoryNameKey() {
		return "system";
	}

	@Override
	public List<SXPParameterContributorDefinition>
		getSXPParameterContributorDefinitions(long companyId) {

		return Collections.<SXPParameterContributorDefinition>emptyList();
	}

}