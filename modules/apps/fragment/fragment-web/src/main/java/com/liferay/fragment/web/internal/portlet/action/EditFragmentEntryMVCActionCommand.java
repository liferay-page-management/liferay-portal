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
import com.liferay.fragment.exception.FragmentEntryConfigurationException;
import com.liferay.fragment.exception.FragmentEntryContentException;
import com.liferay.fragment.exception.NoSuchEntryException;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.fragment.service.FragmentEntryService;
import com.liferay.fragment.web.internal.portlet.helper.FragmentEntryKeyGenerator;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Repository;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.portletfilerepository.PortletFileRepositoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + FragmentPortletKeys.FRAGMENT,
		"mvc.command.name=/fragment/edit_fragment_entry"
	},
	service = {AopService.class, MVCActionCommand.class}
)
public class EditFragmentEntryMVCActionCommand
	extends BaseMVCActionCommand implements AopService {

	@Override
	@Transactional(rollbackFor = Exception.class)
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		long fragmentEntryId = ParamUtil.getLong(
			actionRequest, "fragmentEntryId");

		FragmentEntry fragmentEntry = _fragmentEntryService.fetchFragmentEntry(
			fragmentEntryId);

		if (fragmentEntry == null) {
			throw new NoSuchEntryException();
		}

		FragmentEntry draftFragmentEntry = null;

		if (fragmentEntry.isDraft()) {
			draftFragmentEntry = fragmentEntry;
		}
		else {
			draftFragmentEntry = fragmentEntry.getDraftFragmentEntry();

			if (draftFragmentEntry == null) {
				ServiceContext serviceContext =
					ServiceContextFactory.getInstance(actionRequest);

				draftFragmentEntry = _fragmentEntryService.addFragmentEntry(
					fragmentEntry.getGroupId(),
					fragmentEntry.getFragmentCollectionId(),
					FragmentEntryKeyGenerator.getFragmentEntryKey(
						draftFragmentEntry.getGroupId(),
						draftFragmentEntry.getFragmentEntryKey() + "_draft",
						_fragmentEntryLocalService),
					fragmentEntry.getName(), fragmentEntry.getCss(),
					fragmentEntry.getHtml(), fragmentEntry.getJs(),
					fragmentEntry.isCacheable(),
					fragmentEntry.getConfiguration(), 0,
					fragmentEntry.getFragmentEntryId(), fragmentEntry.getType(),
					WorkflowConstants.STATUS_DRAFT, serviceContext);

				if (fragmentEntry.getPreviewFileEntryId() != 0) {
					FileEntry publishedFragmentFileEntry =
						_dlAppLocalService.getFileEntry(
							fragmentEntry.getPreviewFileEntryId());

					Repository repository =
						PortletFileRepositoryUtil.fetchPortletRepository(
							fragmentEntry.getGroupId(),
							FragmentPortletKeys.FRAGMENT);

					if (repository == null) {
						ServiceContext addPortletRepositoryServiceContext =
							new ServiceContext();

						addPortletRepositoryServiceContext.
							setAddGroupPermissions(true);
						addPortletRepositoryServiceContext.
							setAddGuestPermissions(true);

						repository =
							PortletFileRepositoryUtil.addPortletRepository(
								fragmentEntry.getGroupId(),
								FragmentPortletKeys.FRAGMENT,
								addPortletRepositoryServiceContext);
					}

					String fileName =
						draftFragmentEntry.getFragmentEntryId() + "_preview." +
							publishedFragmentFileEntry.getExtension();

					FileEntry draftFragmentFileEntry =
						PortletFileRepositoryUtil.addPortletFileEntry(
							draftFragmentEntry.getGroupId(),
							draftFragmentEntry.getUserId(),
							FragmentEntry.class.getName(),
							draftFragmentEntry.getFragmentEntryId(),
							FragmentPortletKeys.FRAGMENT,
							repository.getDlFolderId(),
							publishedFragmentFileEntry.getContentStream(),
							fileName, publishedFragmentFileEntry.getMimeType(),
							false);

					_fragmentEntryService.updateFragmentEntry(
						draftFragmentEntry.getFragmentEntryId(),
						draftFragmentFileEntry.getFileEntryId());
				}
			}
		}

		String name = ParamUtil.getString(actionRequest, "name");
		String css = ParamUtil.getString(actionRequest, "cssContent");
		String html = ParamUtil.getString(actionRequest, "htmlContent");
		String js = ParamUtil.getString(actionRequest, "jsContent");
		boolean cacheable = ParamUtil.getBoolean(actionRequest, "cacheable");
		String configuration = ParamUtil.getString(
			actionRequest, "configurationContent");
		int status = ParamUtil.getInteger(actionRequest, "status");

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		try {
			FragmentEntry updatedDraftFragmentEntry =
				_fragmentEntryService.updateFragmentEntry(
					draftFragmentEntry.getFragmentEntryId(), name, css, html,
					js, cacheable, configuration, status);

			if (status == WorkflowConstants.ACTION_SAVE_DRAFT) {
				String redirect = _getSaveAndContinueRedirect(
					actionRequest, updatedDraftFragmentEntry);

				jsonObject.put("redirect", redirect);
			}
		}
		catch (FragmentEntryConfigurationException |
			   FragmentEntryContentException exception) {

			hideDefaultErrorMessage(actionRequest);

			String errorMessage = exception.getLocalizedMessage();

			if (exception instanceof FragmentEntryConfigurationException) {
				ThemeDisplay themeDisplay =
					(ThemeDisplay)actionRequest.getAttribute(
						WebKeys.THEME_DISPLAY);

				ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
					themeDisplay.getLocale(),
					EditFragmentEntryMVCActionCommand.class);

				errorMessage = LanguageUtil.get(
					resourceBundle,
					"please-provide-a-valid-configuration-for-the-fragment");
			}

			jsonObject.put("error", errorMessage);
		}

		JSONPortletResponseUtil.writeJSON(
			actionRequest, actionResponse, jsonObject);
	}

	private String _getSaveAndContinueRedirect(
		ActionRequest actionRequest, FragmentEntry fragmentEntry) {

		PortletURL portletURL = PortletURLFactoryUtil.create(
			actionRequest, FragmentPortletKeys.FRAGMENT,
			PortletRequest.RENDER_PHASE);

		portletURL.setParameter(
			"mvcRenderCommandName", "/fragment/edit_fragment_entry");
		portletURL.setParameter(
			"fragmentCollectionId",
			String.valueOf(fragmentEntry.getFragmentCollectionId()));
		portletURL.setParameter(
			"fragmentEntryId",
			String.valueOf(fragmentEntry.getFragmentEntryId()));

		return portletURL.toString();
	}

	@Reference
	private DLAppLocalService _dlAppLocalService;

	@Reference
	private FragmentEntryLocalService _fragmentEntryLocalService;

	@Reference
	private FragmentEntryService _fragmentEntryService;

}