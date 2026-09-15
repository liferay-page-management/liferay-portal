/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.web.internal.portlet.action;

import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.model.DepotEntryGroupRel;
import com.liferay.depot.service.DepotEntryGroupRelLocalService;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.fragment.constants.FragmentPortletKeys;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.fragment.web.internal.exception.InvalidPropagationTargetGroupException;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseTransactionalMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.ScopeUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.portlet.ActionRequest;
import jakarta.portlet.ActionResponse;

import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
@Component(
	property = {
		"jakarta.portlet.name=" + FragmentPortletKeys.FRAGMENT,
		"mvc.command.name=/fragment/propagate_group_fragment_entry_changes"
	},
	service = MVCActionCommand.class
)
public class PropagateGroupFragmentEntryChangesMVCActionCommand
	extends BaseTransactionalMVCActionCommand {

	@Override
	protected void doTransactionalCommand(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String fragmentEntryERC = ParamUtil.getString(
			actionRequest, "fragmentEntryERC");
		long fragmentEntryGroupId = ParamUtil.getLong(
			actionRequest, "fragmentEntryGroupId");
		long[] groupIds = ParamUtil.getLongValues(actionRequest, "rowIds");

		try {
			_propagateFragmentEntryLinkChanges(
				fragmentEntryERC, fragmentEntryGroupId, groupIds, themeDisplay);
		}
		catch (InvalidPropagationTargetGroupException
					invalidPropagationTargetGroupException) {

			SessionErrors.add(
				actionRequest,
				invalidPropagationTargetGroupException.getClass());

			hideDefaultErrorMessage(actionRequest);

			String redirect = ParamUtil.getString(actionRequest, "redirect");

			sendRedirect(actionRequest, actionResponse, redirect);

			return;
		}

		String redirect = ParamUtil.getString(actionRequest, "redirect");

		sendRedirect(actionRequest, actionResponse, redirect);
	}

	private Set<Long> _getPropagationTargetGroupIds(
			long fragmentEntryGroupId, long[] groupIds)
		throws InvalidPropagationTargetGroupException {

		DepotEntry depotEntry = _depotEntryLocalService.fetchGroupDepotEntry(
			fragmentEntryGroupId);

		if (depotEntry == null) {
			return SetUtil.fromArray(groupIds);
		}

		long[] connectedGroupIds = TransformUtil.transformToLongArray(
			_depotEntryGroupRelLocalService.getDepotEntryGroupRels(depotEntry),
			DepotEntryGroupRel::getToGroupId);

		Set<Long> propagationTargetGroupIds = SetUtil.intersect(
			groupIds,
			ArrayUtil.append(connectedGroupIds, fragmentEntryGroupId));

		if (ArrayUtil.isNotEmpty(groupIds) &&
			propagationTargetGroupIds.isEmpty()) {

			throw new InvalidPropagationTargetGroupException();
		}

		return propagationTargetGroupIds;
	}

	private void _propagateFragmentEntryLinkChanges(
			String fragmentEntryERC, long fragmentEntryGroupId, long[] groupIds,
			ThemeDisplay themeDisplay)
		throws PortalException {

		for (long groupId :
				_getPropagationTargetGroupIds(fragmentEntryGroupId, groupIds)) {

			String fragmentEntryScopeERC =
				ScopeUtil.getItemScopeExternalReferenceCode(
					fragmentEntryGroupId, groupId);

			ActionableDynamicQuery actionableDynamicQuery =
				_fragmentEntryLinkLocalService.getActionableDynamicQuery();

			actionableDynamicQuery.setAddCriteriaMethod(
				dynamicQuery -> {
					Property fragmentEntryERCProperty =
						PropertyFactoryUtil.forName("fragmentEntryERC");

					dynamicQuery.add(
						fragmentEntryERCProperty.eq(fragmentEntryERC));

					Property fragmentEntryScopeERCProperty =
						PropertyFactoryUtil.forName("fragmentEntryScopeERC");

					if (Validator.isNull(fragmentEntryScopeERC)) {
						dynamicQuery.add(
							fragmentEntryScopeERCProperty.isNull());
					}
					else {
						dynamicQuery.add(
							fragmentEntryScopeERCProperty.eq(
								fragmentEntryScopeERC));
					}
				});
			actionableDynamicQuery.setCompanyId(themeDisplay.getCompanyId());
			actionableDynamicQuery.setGroupId(groupId);
			actionableDynamicQuery.setPerformActionMethod(
				(FragmentEntryLink fragmentEntryLink) ->
					_fragmentEntryLinkLocalService.updateLatestChanges(
						fragmentEntryLink.getFragmentEntryLinkId()));

			actionableDynamicQuery.performActions();
		}
	}

	@Reference
	private DepotEntryGroupRelLocalService _depotEntryGroupRelLocalService;

	@Reference
	private DepotEntryLocalService _depotEntryLocalService;

	@Reference
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

}