/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.design.library.internal.resource.v1_0;

import com.liferay.headless.design.library.dto.v1_0.StyleBook;
import com.liferay.headless.design.library.resource.v1_0.StyleBookResource;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.crud.VulcanCRUDItemDelegate;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.util.SearchUtil;
import com.liferay.style.book.constants.StyleBookActionKeys;
import com.liferay.style.book.constants.StyleBookConstants;
import com.liferay.style.book.model.StyleBookEntry;
import com.liferay.style.book.service.StyleBookEntryService;
import com.liferay.style.book.util.StyleBookEntryProviderUtil;
import com.liferay.style.book.util.StyleBookUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Luis Ortiz
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/style-book.properties",
	property = {
		"crud.entity.class.name=com.liferay.headless.design.library.dto.v1_0.StyleBook",
		"crud.item.delegate=true"
	},
	scope = ServiceScope.PROTOTYPE, service = StyleBookResource.class
)
public class StyleBookResourceImpl
	extends BaseStyleBookResourceImpl
	implements VulcanCRUDItemDelegate<StyleBook> {

	@Override
	public void deleteAssetLibraryStyleBook(
			String assetLibraryExternalReferenceCode,
			String styleBookExternalReferenceCode)
		throws Exception {

		_checkFeatureFlag();

		StyleBookEntry styleBookEntry = _getStyleBookEntry(
			assetLibraryExternalReferenceCode, styleBookExternalReferenceCode);

		_styleBookEntryService.deleteStyleBookEntry(
			styleBookEntry.getStyleBookEntryId());
	}

	@Override
	public StyleBook getAssetLibraryStyleBook(
			String assetLibraryExternalReferenceCode,
			String styleBookExternalReferenceCode)
		throws Exception {

		_checkFeatureFlag();

		return _toStyleBook(
			_getStyleBookEntry(
				assetLibraryExternalReferenceCode,
				styleBookExternalReferenceCode));
	}

	@Override
	public Page<StyleBook> getAssetLibraryStyleBooksPage(
			String assetLibraryExternalReferenceCode, String search,
			Aggregation aggregation, Filter filter, Pagination pagination,
			Sort[] sorts)
		throws Exception {

		_checkFeatureFlag();

		long groupId = _getGroupId(assetLibraryExternalReferenceCode);

		return SearchUtil.search(
			Collections.emptyMap(),
			booleanQuery -> {
			},
			filter, StyleBookEntry.class.getName(), search, pagination,
			queryConfig -> queryConfig.setSelectedFieldNames(
				Field.ENTRY_CLASS_PK),
			searchContext -> {
				searchContext.addVulcanAggregation(aggregation);
				searchContext.setCompanyId(contextCompany.getCompanyId());
				searchContext.setGroupIds(new long[] {groupId});
			},
			sorts,
			document -> _toStyleBook(
				_styleBookEntryService.getStyleBookEntry(
					GetterUtil.getLong(document.get(Field.ENTRY_CLASS_PK)))));
	}

	@Override
	public StyleBook getItem(Long id) throws Exception {
		_checkFeatureFlag();

		return _toStyleBook(_styleBookEntryService.getStyleBookEntry(id));
	}

	@Override
	public Page<StyleBook> getSiteStyleBooksPage(
			Long siteId, String search, Pagination pagination)
		throws Exception {

		_checkFeatureFlag();

		List<StyleBook> styleBooks = new ArrayList<>();

		StyleBookEntry themeStyleBookEntry = _getThemeStyleBookEntry();

		List<StyleBookEntry> allStyleBookEntries =
			StyleBookEntryProviderUtil.getStyleBookEntries(
				contextCompany.getCompanyId(), siteId);

		List<StyleBookEntry> styleBookEntries;

		if (themeStyleBookEntry != null) {
			String themeId = themeStyleBookEntry.getThemeId();

			styleBookEntries = ListUtil.filter(
				allStyleBookEntries,
				styleBookEntry ->
					Objects.equals(themeId, styleBookEntry.getThemeId()));
		}
		else {
			styleBookEntries = allStyleBookEntries;
		}

		int totalCount = styleBookEntries.size();

		if (themeStyleBookEntry != null) {
			totalCount++;
		}

		if (themeStyleBookEntry != null) {
			if (pagination.getStartPosition() == 0) {
				styleBooks.add(_toStyleBook(themeStyleBookEntry));

				for (StyleBookEntry styleBookEntry :
						ListUtil.subList(
							styleBookEntries, 0,
							pagination.getEndPosition() - 1)) {

					styleBooks.add(_toStyleBook(styleBookEntry));
				}
			}
			else {
				for (StyleBookEntry styleBookEntry :
						ListUtil.subList(
							styleBookEntries, pagination.getStartPosition() - 1,
							pagination.getEndPosition() - 1)) {

					styleBooks.add(_toStyleBook(styleBookEntry));
				}
			}
		}
		else {
			for (StyleBookEntry styleBookEntry :
					ListUtil.subList(
						styleBookEntries, pagination.getStartPosition(),
						pagination.getEndPosition())) {

				styleBooks.add(_toStyleBook(styleBookEntry));
			}
		}

		return Page.of(styleBooks, pagination, totalCount);
	}

	private void _checkFeatureFlag() {
		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-57283")) {

			throw new UnsupportedOperationException();
		}
	}

	private Map<String, Map<String, String>> _getActions(
		StyleBookEntry styleBookEntry) {

		Group group = _groupLocalService.fetchGroup(
			styleBookEntry.getGroupId());

		if ((group == null) || !group.isDepot()) {
			return Collections.emptyMap();
		}

		return _getAssetLibraryActions(styleBookEntry);
	}

	private Map<String, Map<String, String>> _getAssetLibraryActions(
		StyleBookEntry styleBookEntry) {

		if (!_portletResourcePermission.contains(
				PermissionThreadLocal.getPermissionChecker(),
				styleBookEntry.getGroupId(),
				StyleBookActionKeys.MANAGE_STYLE_BOOK_ENTRIES)) {

			return Collections.emptyMap();
		}

		Long siteId = styleBookEntry.getGroupId();
		String styleBookExternalReferenceCode =
			styleBookEntry.getExternalReferenceCode();

		return HashMapBuilder.<String, Map<String, String>>put(
			"delete",
			_resolveStyleBookERCInActionHref(
				addAction(
					StyleBookActionKeys.MANAGE_STYLE_BOOK_ENTRIES,
					"deleteAssetLibraryStyleBook",
					StyleBookConstants.RESOURCE_NAME, siteId),
				styleBookExternalReferenceCode)
		).put(
			"get",
			_resolveStyleBookERCInActionHref(
				addAction(
					StyleBookActionKeys.MANAGE_STYLE_BOOK_ENTRIES,
					"getAssetLibraryStyleBook",
					StyleBookConstants.RESOURCE_NAME, siteId),
				styleBookExternalReferenceCode)
		).build();
	}

	private long _getGroupId(String externalReferenceCode) throws Exception {
		Group group = _groupLocalService.getGroupByExternalReferenceCode(
			externalReferenceCode, contextCompany.getCompanyId());

		return group.getGroupId();
	}

	private StyleBookEntry _getStyleBookEntry(
			String groupExternalReferenceCode,
			String styleBookExternalReferenceCode)
		throws Exception {

		return _styleBookEntryService.getStyleBookEntryByExternalReferenceCode(
			styleBookExternalReferenceCode,
			_getGroupId(groupExternalReferenceCode));
	}

	private StyleBookEntry _getThemeStyleBookEntry() {
		long plid = GetterUtil.getLong(
			contextHttpServletRequest.getParameter("plid"));

		if (plid <= 0) {
			return null;
		}

		Layout layout = _layoutLocalService.fetchLayout(plid);

		if (layout == null) {
			return null;
		}

		StyleBookEntry themeStyleBookEntry =
			StyleBookUtil.getStyleFromThemeStyleBookEntry(
				layout, contextAcceptLanguage.getPreferredLocale());

		if (themeStyleBookEntry.getName() == null) {
			return null;
		}

		return themeStyleBookEntry;
	}

	private Map<String, String> _resolveStyleBookERCInActionHref(
		Map<String, String> action, String styleBookExternalReferenceCode) {

		String href = action.get("href");

		if (href != null) {
			action.put(
				"href",
				StringUtil.replace(
					href, "{styleBookExternalReferenceCode}",
					styleBookExternalReferenceCode));
		}

		return action;
	}

	private StyleBook _toStyleBook(StyleBookEntry styleBookEntry)
		throws Exception {

		return _styleBookDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				contextAcceptLanguage.isAcceptAllLanguages(),
				_getActions(styleBookEntry), _dtoConverterRegistry,
				contextHttpServletRequest, styleBookEntry.getStyleBookEntryId(),
				contextAcceptLanguage.getPreferredLocale(), contextUriInfo,
				contextUser),
			styleBookEntry);
	}

	@Reference
	private DTOConverterRegistry _dtoConverterRegistry;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference(
		target = "(resource.name=" + StyleBookConstants.RESOURCE_NAME + ")"
	)
	private PortletResourcePermission _portletResourcePermission;

	@Reference(
		target = "(component.name=com.liferay.headless.design.library.internal.dto.v1_0.converter.StyleBookDTOConverter)"
	)
	private DTOConverter<StyleBookEntry, StyleBook> _styleBookDTOConverter;

	@Reference
	private StyleBookEntryService _styleBookEntryService;

}