/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.entry.processor.editable.internal.mapper;

import com.liferay.asset.display.page.portlet.AssetDisplayPageFriendlyURLProvider;
import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.fragment.entry.processor.editable.element.constants.ActionEditableElementConstants;
import com.liferay.fragment.entry.processor.editable.mapper.EditableElementMapper;
import com.liferay.fragment.processor.FragmentEntryProcessorContext;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.InfoItemIdentifier;
import com.liferay.info.item.InfoItemReference;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;

import java.util.Locale;

import org.jsoup.nodes.Element;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rubén Pulido
 */
@Component(property = "type=action", service = EditableElementMapper.class)
public class ActionEditableElementMapper implements EditableElementMapper {

	@Override
	public void map(
			Element element, JSONObject configJSONObject,
			FragmentEntryProcessorContext fragmentEntryProcessorContext)
		throws PortalException {

		JSONObject mappedActionJSONObject = configJSONObject.getJSONObject(
			"mappedAction");

		if (mappedActionJSONObject == null) {
			return;
		}

		String fieldId = mappedActionJSONObject.getString("fieldId");

		if (Validator.isNull(fieldId)) {
			fieldId = mappedActionJSONObject.getString("collectionFieldId");
		}

		if (Validator.isNull(fieldId)) {
			fieldId = mappedActionJSONObject.getString("mappedField");
		}

		if (Validator.isNull(fieldId)) {
			return;
		}

		String classNameId = mappedActionJSONObject.getString("classNameId");
		String classPK = mappedActionJSONObject.getString("classPK");

		if (Validator.isNull(classNameId) || Validator.isNull(classPK)) {
			InfoItemReference infoItemReference =
				fragmentEntryProcessorContext.getContextInfoItemReference();

			if (infoItemReference == null) {
				return;
			}

			classNameId = String.valueOf(
				_portal.getClassNameId(infoItemReference.getClassName()));

			InfoItemIdentifier infoItemIdentifier =
				infoItemReference.getInfoItemIdentifier();

			if (infoItemIdentifier instanceof ClassPKInfoItemIdentifier) {
				ClassPKInfoItemIdentifier classPKInfoItemIdentifier =
					(ClassPKInfoItemIdentifier)infoItemIdentifier;

				classPK = String.valueOf(
					classPKInfoItemIdentifier.getClassPK());
			}

			if (Validator.isNull(classNameId) || Validator.isNull(classPK)) {
				return;
			}
		}

		element.attr("data-lfr-class-name-id", classNameId);
		element.attr("data-lfr-class-pk", classPK);
		element.attr("data-lfr-field-id", fieldId);

		_addDataAtributes(
			classNameId, classPK, element,
			configJSONObject.getJSONObject("onError"), "error");
		_addDataAtributes(
			classNameId, classPK, element,
			configJSONObject.getJSONObject("onSuccess"), "success");
	}

	private void _addDataAtributes(
			String classNameId, String classPK, Element element,
			JSONObject jsonObject, String resultType)
		throws PortalException {

		if (jsonObject == null) {
			return;
		}

		String interaction = jsonObject.getString("interaction");

		if (Validator.isNull(interaction)) {
			interaction = ActionEditableElementConstants.INTERACTION_NONE;
		}

		element.attr("data-lfr-on-" + resultType + "-interaction", interaction);

		if ((interaction.equals(
				ActionEditableElementConstants.INTERACTION_NONE) ||
			 interaction.equals(
				 ActionEditableElementConstants.INTERACTION_NOTIFICATION)) &&
			jsonObject.getBoolean("reload")) {

			element.attr(
				"data-lfr-on-" + resultType + "-reload", StringPool.TRUE);
		}

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		if (serviceContext == null) {
			return;
		}

		ThemeDisplay themeDisplay = serviceContext.getThemeDisplay();

		if (themeDisplay == null) {
			return;
		}

		if (interaction.equals(
				ActionEditableElementConstants.INTERACTION_DISPLAY_PAGE)) {

			if (!resultType.equals("success")) {
				return;
			}

			String layoutPageTemplateEntryKey = jsonObject.getString(
				"displayPage");

			if (Validator.isNull(layoutPageTemplateEntryKey)) {
				return;
			}

			String url = null;

			if (layoutPageTemplateEntryKey.equals(
					"ObjectEntry_displayPageURL")) {

				url = _getDefaultDisplayPageURL(
					new InfoItemReference(
						_portal.getClassName(GetterUtil.getLong(classNameId)),
						GetterUtil.getLong(classPK)),
					themeDisplay);
			}
			else if (layoutPageTemplateEntryKey.startsWith(
						"LayoutPageTemplateEntry_")) {

				layoutPageTemplateEntryKey =
					layoutPageTemplateEntryKey.substring(
						"LayoutPageTemplateEntry_".length());

				LayoutPageTemplateEntry layoutPageTemplateEntry =
					_layoutPageTemplateEntryLocalService.
						fetchLayoutPageTemplateEntry(
							themeDisplay.getScopeGroupId(),
							layoutPageTemplateEntryKey);

				Layout layout = _layoutLocalService.fetchLayout(
					layoutPageTemplateEntry.getPlid());

				if (layout == null) {
					return;
				}

				Group group = themeDisplay.getScopeGroup();

				url = StringBundler.concat(
					_portal.getGroupFriendlyURL(
						group.getPublicLayoutSet(), themeDisplay, false, false),
					"/e", layout.getFriendlyURL(themeDisplay.getLocale()),
					StringPool.SLASH, classNameId, StringPool.SLASH, classPK);
			}

			if (Validator.isNull(url)) {
				return;
			}

			element.attr("data-lfr-on-" + resultType + "-page-url", url);
		}

		if (interaction.equals(
				ActionEditableElementConstants.INTERACTION_NOTIFICATION)) {

			JSONObject textJSONObject = jsonObject.getJSONObject("text");

			if (textJSONObject == null) {
				return;
			}

			String text = textJSONObject.getString(
				themeDisplay.getLanguageId());

			if (Validator.isNull(text)) {
				return;
			}

			element.attr("data-lfr-on-" + resultType + "-text", text);
		}
		else if (interaction.equals(
					ActionEditableElementConstants.INTERACTION_PAGE)) {

			JSONObject pageJSONObject = jsonObject.getJSONObject("page");

			if (pageJSONObject == null) {
				return;
			}

			Layout layout = _layoutLocalService.fetchLayout(
				pageJSONObject.getLong("groupId"),
				pageJSONObject.getBoolean("privateLayout"),
				pageJSONObject.getLong("layoutId"));

			if (layout == null) {
				return;
			}

			element.attr(
				"data-lfr-on-" + resultType + "-page-url",
				_portal.getLayoutURL(layout, themeDisplay));
		}
		else if (interaction.equals(
					ActionEditableElementConstants.INTERACTION_URL)) {

			JSONObject urlJSONObject = jsonObject.getJSONObject("url");

			if (urlJSONObject == null) {
				return;
			}

			String url = urlJSONObject.getString(themeDisplay.getLanguageId());

			if (Validator.isNull(url)) {
				Locale locale = LocaleUtil.getSiteDefault();

				url = urlJSONObject.getString(locale.getLanguage());
			}

			if (Validator.isNull(url)) {
				return;
			}

			element.attr("data-lfr-on-" + resultType + "-page-url", url);
		}
	}

	private String _getDefaultDisplayPageURL(
		InfoItemReference infoItemReference, ThemeDisplay themeDisplay) {

		AssetRendererFactory<?> assetRendererFactory =
			AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(
				infoItemReference.getClassName());

		try {
			if (assetRendererFactory == null) {
				return _assetDisplayPageFriendlyURLProvider.getFriendlyURL(
					infoItemReference, themeDisplay);
			}

			AssetRenderer<?> assetRenderer = null;

			if (infoItemReference.getInfoItemIdentifier() instanceof
					ClassPKInfoItemIdentifier) {

				ClassPKInfoItemIdentifier classPKInfoItemIdentifier =
					(ClassPKInfoItemIdentifier)
						infoItemReference.getInfoItemIdentifier();

				assetRenderer = assetRendererFactory.getAssetRenderer(
					classPKInfoItemIdentifier.getClassPK());
			}

			if (assetRenderer == null) {
				return _assetDisplayPageFriendlyURLProvider.getFriendlyURL(
					infoItemReference, themeDisplay);
			}

			String viewInContextURL = assetRenderer.getURLViewInContext(
				themeDisplay, StringPool.BLANK);

			if (Validator.isNotNull(viewInContextURL)) {
				return viewInContextURL;
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}

		try {
			return _assetDisplayPageFriendlyURLProvider.getFriendlyURL(
				infoItemReference, themeDisplay);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}

		return null;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ActionEditableElementMapper.class);

	@Reference
	private AssetDisplayPageFriendlyURLProvider
		_assetDisplayPageFriendlyURLProvider;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private LayoutPageTemplateEntryLocalService
		_layoutPageTemplateEntryLocalService;

	@Reference
	private Portal _portal;

}