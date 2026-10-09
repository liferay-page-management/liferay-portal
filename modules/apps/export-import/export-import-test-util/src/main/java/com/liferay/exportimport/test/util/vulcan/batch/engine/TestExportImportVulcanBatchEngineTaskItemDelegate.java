/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.test.util.vulcan.batch.engine;

import com.liferay.exportimport.vulcan.batch.engine.ExportImportVulcanBatchEngineTaskItemDelegate;
import com.liferay.petra.function.UnsafeBiConsumer;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ResourceActionLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.vulcan.batch.engine.VulcanBatchEngineTaskItemDelegate;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import jakarta.ws.rs.core.UriInfo;

import java.io.Serializable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * @author Vendel Töreki
 */
public class TestExportImportVulcanBatchEngineTaskItemDelegate
	implements ExportImportVulcanBatchEngineTaskItemDelegate<Object>,
			   VulcanBatchEngineTaskItemDelegate<Object> {

	public TestExportImportVulcanBatchEngineTaskItemDelegate(
		String className, Function<BaseModel<?>, Boolean> function, String key,
		String languageKey, String portletId) {

		this(
			className, function, key, languageKey, portletId, Scope.COMPANY,
			(companyId, groupId) -> true);
	}

	public TestExportImportVulcanBatchEngineTaskItemDelegate(
		String className, Function<BaseModel<?>, Boolean> function, String key,
		String languageKey, String portletId, Scope scope,
		BiPredicate<Long, Long> scopeSupportedBiPredicate) {

		_className = className;
		_function = function;
		_key = key;
		_languageKey = languageKey;
		_portletId = portletId;
		_scope = scope;
		_scopeSupportedBiPredicate = scopeSupportedBiPredicate;
	}

	@Override
	public void create(
		Collection<Object> items, Map<String, Serializable> parameters) {
	}

	@Override
	public void delete(
		Collection<Object> items, Map<String, Serializable> parameters) {
	}

	@Override
	public EntityModel getEntityModel(
		Map<String, List<String>> multivaluedMap) {

		return null;
	}

	@Override
	public ExportImportDescriptor getExportImportDescriptor() {
		return new ExportImportDescriptor() {

			@Override
			public Function<BaseModel<?>, Boolean>
				getApplicableModelFunction() {

				return _function;
			}

			@Override
			public String getKey() {
				return _key;
			}

			@Override
			public String getLabelLanguageKey() {
				return _languageKey;
			}

			@Override
			public Class getModelClass() {
				return null;
			}

			@Override
			public String getModelClassName() {
				return _className;
			}

			@Override
			public String getPortletId() {
				return _portletId;
			}

			@Override
			public Scope getScope() {
				return _scope;
			}

			@Override
			public boolean isScopeSupported(long companyId, long groupId) {
				return _scopeSupportedBiPredicate.test(companyId, groupId);
			}

		};
	}

	@Override
	public Page<Object> read(
		Filter filter, Pagination pagination, Sort[] sorts,
		Map<String, Serializable> parameters, String search) {

		return null;
	}

	@Override
	public void setContextBatchUnsafeBiConsumer(
		UnsafeBiConsumer
			<Collection<Object>, UnsafeFunction<Object, Object, Exception>,
			 Exception> contextBatchUnsafeBiConsumer) {
	}

	@Override
	public void setContextCompany(Company contextCompany) {
	}

	@Override
	public void setContextUriInfo(UriInfo uriInfo) {
	}

	@Override
	public void setContextUser(User contextUser) {
	}

	@Override
	public void setGroupLocalService(GroupLocalService groupLocalService) {
	}

	@Override
	public void setLanguageId(String languageId) {
	}

	@Override
	public void setResourceActionLocalService(
		ResourceActionLocalService resourceActionLocalService) {
	}

	@Override
	public void setResourcePermissionLocalService(
		ResourcePermissionLocalService resourcePermissionLocalService) {
	}

	@Override
	public void setRoleLocalService(RoleLocalService roleLocalService) {
	}

	@Override
	public void update(
		Collection<Object> items, Map<String, Serializable> parameters) {
	}

	private final String _className;
	private final Function<BaseModel<?>, Boolean> _function;
	private final String _key;
	private final String _languageKey;
	private final String _portletId;
	private final Scope _scope;
	private final BiPredicate<Long, Long> _scopeSupportedBiPredicate;

}