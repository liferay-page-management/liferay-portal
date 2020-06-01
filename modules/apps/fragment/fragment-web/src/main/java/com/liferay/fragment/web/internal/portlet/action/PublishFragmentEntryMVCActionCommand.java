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

package com.liferay.fragment.web.internal.portlet.action;

import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.fragment.constants.FragmentPortletKeys;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.fragment.service.FragmentEntryService;
import com.liferay.fragment.web.internal.handler.FragmentEntryExceptionRequestHandler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Repository;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.portletfilerepository.PortletFileRepositoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletURL;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rubén Pulido
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + FragmentPortletKeys.FRAGMENT,
		"mvc.command.name=/fragment/publish_fragment_entry"
	},
	service = {AopService.class, MVCActionCommand.class}
)
public class PublishFragmentEntryMVCActionCommand
	extends BaseMVCActionCommand implements AopService {

	@Override
	@Transactional(rollbackFor = Exception.class)
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		long fragmentEntryId = ParamUtil.getLong(
			actionRequest, "fragmentEntryId");

		try {
			FragmentEntry fragmentEntry =
				_fragmentEntryService.fetchFragmentEntry(fragmentEntryId);

			FragmentEntry draftFragmentEntry = null;
			FragmentEntry publishedFragmentEntry = null;

			if (fragmentEntry.isDraft()) {
				draftFragmentEntry = fragmentEntry;

				if (draftFragmentEntry.getPublishedFragmentEntryId() != 0) {
					publishedFragmentEntry =
						_fragmentEntryService.fetchFragmentEntry(
							fragmentEntry.getPublishedFragmentEntryId());
				}
			}
			else {
				publishedFragmentEntry = fragmentEntry;

				draftFragmentEntry =
					publishedFragmentEntry.getDraftFragmentEntry();
			}

			if (publishedFragmentEntry == null) {
				publishedFragmentEntry = _fragmentEntryService.addFragmentEntry(
					draftFragmentEntry.getGroupId(),
					draftFragmentEntry.getFragmentCollectionId(),
					_getFragmentEntryKey(
						draftFragmentEntry.getGroupId(),
						draftFragmentEntry.getFragmentEntryKey()),
					draftFragmentEntry.getName(), draftFragmentEntry.getCss(),
					draftFragmentEntry.getHtml(), draftFragmentEntry.getJs(),
					draftFragmentEntry.isCacheable(),
					draftFragmentEntry.getConfiguration(), 0, 0,
					draftFragmentEntry.getType(),
					WorkflowConstants.STATUS_APPROVED,
					ServiceContextFactory.getInstance(actionRequest));

				if (draftFragmentEntry.getPreviewFileEntryId() != 0) {
					FileEntry fileEntry = _dlAppLocalService.getFileEntry(
						draftFragmentEntry.getPreviewFileEntryId());

					Repository repository =
						PortletFileRepositoryUtil.fetchPortletRepository(
							draftFragmentEntry.getGroupId(),
							FragmentPortletKeys.FRAGMENT);

					if (repository == null) {
						ServiceContext serviceContext = new ServiceContext();

						serviceContext.setAddGroupPermissions(true);
						serviceContext.setAddGuestPermissions(true);

						repository =
							PortletFileRepositoryUtil.addPortletRepository(
								draftFragmentEntry.getGroupId(),
								FragmentPortletKeys.FRAGMENT, serviceContext);
					}

					String fileName =
						publishedFragmentEntry.getFragmentEntryId() +
							"_preview." + fileEntry.getExtension();

					fileEntry = PortletFileRepositoryUtil.addPortletFileEntry(
						draftFragmentEntry.getGroupId(),
						draftFragmentEntry.getUserId(),
						FragmentEntry.class.getName(),
						publishedFragmentEntry.getFragmentEntryId(),
						FragmentPortletKeys.FRAGMENT,
						repository.getDlFolderId(),
						fileEntry.getContentStream(), fileName,
						fileEntry.getMimeType(), false);

					_fragmentEntryService.updateFragmentEntry(
						publishedFragmentEntry.getFragmentEntryId(),
						fileEntry.getFileEntryId());
				}
			}
			else {
				publishedFragmentEntry =
					_fragmentEntryService.updateFragmentEntry(
						publishedFragmentEntry.getFragmentEntryId(),
						publishedFragmentEntry.getName(),
						draftFragmentEntry.getCss(),
						draftFragmentEntry.getHtml(),
						draftFragmentEntry.getJs(),
						draftFragmentEntry.isCacheable(),
						draftFragmentEntry.getConfiguration(),
						publishedFragmentEntry.getPreviewFileEntryId(),
						WorkflowConstants.STATUS_APPROVED);
			}

			_fragmentEntryService.deleteFragmentEntry(
				draftFragmentEntry.getFragmentEntryId());

			JSONObject jsonObject = JSONUtil.put(
				"redirectURL",
				getRedirectURL(actionResponse, publishedFragmentEntry));

			JSONPortletResponseUtil.writeJSON(
				actionRequest, actionResponse, jsonObject);
		}
		catch (PortalException portalException) {
			_fragmentEntryExceptionRequestHandler.handlePortalException(
				actionRequest, actionResponse, portalException);
		}
	}

	protected String getRedirectURL(
		ActionResponse actionResponse, FragmentEntry fragmentEntry) {

		LiferayPortletResponse liferayPortletResponse =
			_portal.getLiferayPortletResponse(actionResponse);

		PortletURL portletURL = liferayPortletResponse.createRenderURL();

		portletURL.setParameter(
			"mvcRenderCommandName", "/fragment/view_fragment_entries");
		portletURL.setParameter(
			"fragmentCollectionId",
			String.valueOf(fragmentEntry.getFragmentCollectionId()));

		return portletURL.toString();
	}

	private String _getFragmentEntryKey(long groupId, String fragmentEntryKey) {
		if (fragmentEntryKey == null) {
			fragmentEntryKey = StringPool.BLANK;
		}
		else {
			fragmentEntryKey = fragmentEntryKey.trim();
			fragmentEntryKey = StringUtil.toLowerCase(fragmentEntryKey);
		}

		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.fetchFragmentEntry(
				groupId, fragmentEntryKey);

		if (fragmentEntry == null) {
			return fragmentEntryKey;
		}

		String newFragmentEntryKey = null;

		for (int i = 1;; i++) {
			newFragmentEntryKey = fragmentEntryKey + i;

			fragmentEntry = _fragmentEntryLocalService.fetchFragmentEntry(
				groupId, newFragmentEntryKey);

			if (fragmentEntry == null) {
				return newFragmentEntryKey;
			}
		}
	}

	@Reference
	private DLAppLocalService _dlAppLocalService;

	@Reference
	private FragmentEntryExceptionRequestHandler
		_fragmentEntryExceptionRequestHandler;

	@Reference
	private FragmentEntryLocalService _fragmentEntryLocalService;

	@Reference
	private FragmentEntryService _fragmentEntryService;

	@Reference
	private Portal _portal;

}