/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.dto.v1_0.converter;

import com.liferay.fragment.contributor.FragmentCollectionContributorRegistry;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.headless.admin.site.dto.v1_0.DefaultFragmentReference;
import com.liferay.headless.admin.site.dto.v1_0.FragmentInstancePageElementDefinition;
import com.liferay.headless.admin.site.dto.v1_0.FragmentItemExternalReference;
import com.liferay.headless.admin.site.dto.v1_0.PageElementDefinition;
import com.liferay.headless.admin.site.dto.v1_0.Scope;
import com.liferay.layout.util.structure.FragmentStyledLayoutStructureItem;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Eudaldo Alonso
 */
@Component(
	property = "dto.class.name=com.liferay.layout.util.structure.FragmentStyledLayoutStructureItem",
	service = DTOConverter.class
)
public class FragmentInstancePageElementDefinitionDTOConverter
	implements DTOConverter
		<FragmentStyledLayoutStructureItem,
		 FragmentInstancePageElementDefinition> {

	@Override
	public String getContentType() {
		return FragmentInstancePageElementDefinition.class.getSimpleName();
	}

	@Override
	public FragmentInstancePageElementDefinition toDTO(
			DTOConverterContext dtoConverterContext,
			FragmentStyledLayoutStructureItem fragmentStyledLayoutStructureItem)
		throws Exception {

		FragmentEntryLink fragmentEntryLink =
			_fragmentEntryLinkLocalService.fetchFragmentEntryLink(
				fragmentStyledLayoutStructureItem.getFragmentEntryLinkId());

		if (fragmentEntryLink == null) {
			throw new UnsupportedOperationException();
		}

		FragmentInstancePageElementDefinition
			fragmentInstancePageElementDefinition =
				new FragmentInstancePageElementDefinition();

		fragmentInstancePageElementDefinition.setConfiguration(
			fragmentEntryLink::getConfiguration);
		fragmentInstancePageElementDefinition.setCss(fragmentEntryLink::getCss);
		fragmentInstancePageElementDefinition.setCssClasses(
			() -> {
				if (SetUtil.isEmpty(
						fragmentStyledLayoutStructureItem.getCssClasses())) {

					return null;
				}

				return ArrayUtil.toStringArray(
					fragmentStyledLayoutStructureItem.getCssClasses());
			});
		fragmentInstancePageElementDefinition.setCustomCSS(
			fragmentStyledLayoutStructureItem::getCustomCSS);
		fragmentInstancePageElementDefinition.setDatePropagated(
			fragmentEntryLink::getLastPropagationDate);
		fragmentInstancePageElementDefinition.
			setDraftFragmentInstanceExternalReferenceCode(
				() -> _getDraftFragmentInstanceExternalReferenceCode(
					fragmentEntryLink));
		fragmentInstancePageElementDefinition.
			setFragmentInstanceExternalReferenceCode(
				fragmentEntryLink::getExternalReferenceCode);
		fragmentInstancePageElementDefinition.setFragmentReference(
			() -> {
				FragmentEntry fragmentEntry =
					_fragmentEntryLocalService.fetchFragmentEntry(
						fragmentEntryLink.getFragmentEntryId());

				if (fragmentEntry != null) {
					return new FragmentItemExternalReference() {
						{
							setExternalReferenceCode(
								fragmentEntry::getExternalReferenceCode);
							setFragmentReferenceType(
								() ->
									FragmentReferenceType.
										FRAGMENT_ITEM_EXTERNAL_REFERENCE);
							setScope(
								() -> {
									if (fragmentEntry.getGroupId() ==
											fragmentEntryLink.getGroupId()) {

										return null;
									}

									Group group = _groupLocalService.getGroup(
										fragmentEntry.getGroupId());

									return new Scope() {
										{
											setExternalReferenceCode(
												group::
													getExternalReferenceCode);
											setType(() -> Type.SITE);
										}
									};
								});
						}
					};
				}

				Map<String, FragmentEntry> fragmentEntries =
					_fragmentCollectionContributorRegistry.getFragmentEntries();

				if (!fragmentEntries.containsKey(
						fragmentEntryLink.getRendererKey())) {

					return null;
				}

				return new DefaultFragmentReference() {
					{
						setDefaultFragmentKey(
							fragmentEntryLink::getRendererKey);
						setFragmentReferenceType(
							() ->
								FragmentReferenceType.
									DEFAULT_FRAGMENT_REFERENCE);
					}
				};
			});
		fragmentInstancePageElementDefinition.setFragmentType(
			() -> {
				if (fragmentEntryLink.isTypeComponent()) {
					return FragmentInstancePageElementDefinition.FragmentType.
						BASIC;
				}

				return FragmentInstancePageElementDefinition.FragmentType.FORM;
			});
		fragmentInstancePageElementDefinition.setHtml(
			fragmentEntryLink::getHtml);
		fragmentInstancePageElementDefinition.setIndexed(
			fragmentStyledLayoutStructureItem::isIndexed);
		fragmentInstancePageElementDefinition.setJs(fragmentEntryLink::getJs);
		fragmentInstancePageElementDefinition.setName(
			fragmentStyledLayoutStructureItem::getName);
		fragmentInstancePageElementDefinition.setNamespace(
			fragmentEntryLink::getNamespace);
		fragmentInstancePageElementDefinition.setType(
			PageElementDefinition.Type.FRAGMENT);
		fragmentInstancePageElementDefinition.setUuid(
			fragmentEntryLink::getUuid);

		return fragmentInstancePageElementDefinition;
	}

	private String _getDraftFragmentInstanceExternalReferenceCode(
		FragmentEntryLink fragmentEntryLink) {

		long originalFragmentEntryLinkId =
			fragmentEntryLink.getOriginalFragmentEntryLinkId();

		if (originalFragmentEntryLinkId == 0) {
			return null;
		}

		FragmentEntryLink originalFragmentEntryLink =
			_fragmentEntryLinkLocalService.fetchFragmentEntryLink(
				originalFragmentEntryLinkId);

		if (originalFragmentEntryLink == null) {
			return null;
		}

		return originalFragmentEntryLink.getExternalReferenceCode();
	}

	@Reference
	private FragmentCollectionContributorRegistry
		_fragmentCollectionContributorRegistry;

	@Reference
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@Reference
	private FragmentEntryLocalService _fragmentEntryLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

}