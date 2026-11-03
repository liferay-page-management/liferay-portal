/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.item.selector.web.internal.model.listener;

import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.fragment.item.selector.web.internal.DesignLibraryFragmentEntryItemSelectorView;
import com.liferay.item.selector.ItemSelectorView;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.CollatorUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;

import jakarta.servlet.ServletContext;

import java.text.Collator;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Lourdes Fernández Besada
 */
@Component(service = ModelListener.class)
public class DepotEntryModelListener extends BaseModelListener<DepotEntry> {

	@Override
	public void onAfterCreate(DepotEntry depotEntry)
		throws ModelListenerException {

		if (depotEntry.getType() != DepotConstants.TYPE_DESIGN_LIBRARY) {
			return;
		}

		_reregisterCompanyDesignLibraryDepotEntries(depotEntry.getCompanyId());
	}

	@Override
	public void onAfterRemove(DepotEntry depotEntry)
		throws ModelListenerException {

		if (depotEntry.getType() != DepotConstants.TYPE_DESIGN_LIBRARY) {
			return;
		}

		synchronized (_serviceRegistrations) {
			Map<Long, ServiceRegistration<?>> serviceRegistrations =
				_serviceRegistrations.get(depotEntry.getCompanyId());

			if (serviceRegistrations == null) {
				return;
			}

			ServiceRegistration<?> serviceRegistration =
				serviceRegistrations.remove(depotEntry.getGroupId());

			if (serviceRegistration != null) {
				serviceRegistration.unregister();
			}
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;

		_serviceRegistration = bundleContext.registerService(
			PortalInstanceLifecycleListener.class,
			new DesignLibraryDepotEntryPortalInstanceLifecycleListener(), null);
	}

	@Deactivate
	protected void deactivate() {
		_companyLocalService.forEachCompanyId(
			companyId -> _unregisterCompanyDesignLibraryDepotEntries(
				companyId));

		_serviceRegistration.unregister();
	}

	private String _getDescriptiveName(Group group, Locale locale) {
		try {
			return group.getDescriptiveName(locale);
		}
		catch (PortalException portalException) {
			if (_log.isWarnEnabled()) {
				_log.warn(portalException);
			}
		}

		return group.getName();
	}

	private Locale _getLocale(Company company) {
		try {
			return company.getLocale();
		}
		catch (PortalException portalException) {
			if (_log.isWarnEnabled()) {
				_log.warn(portalException);
			}
		}

		return LocaleUtil.getDefault();
	}

	private void _registerCompanyDesignLibraryDepotEntries(Company company) {
		Locale locale = _getLocale(company);

		Collator collator = CollatorUtil.getInstance(locale);

		List<Group> groups = ListUtil.sort(
			TransformUtil.transform(
				_depotEntryLocalService.getDepotEntryGroupIds(
					company.getCompanyId(), DepotConstants.TYPE_DESIGN_LIBRARY),
				groupId -> _groupLocalService.fetchGroup(groupId)),
			(group1, group2) -> collator.compare(
				_getDescriptiveName(group1, locale),
				_getDescriptiveName(group2, locale)));

		if (ListUtil.isEmpty(groups)) {
			return;
		}

		Map<Long, ServiceRegistration<?>> serviceRegistrations =
			new HashMap<>();

		int order = 400;

		for (Group group : groups) {
			serviceRegistrations.put(
				group.getGroupId(),
				_bundleContext.registerService(
					ItemSelectorView.class,
					new DesignLibraryFragmentEntryItemSelectorView(
						group.getGroupId(), _groupLocalService, _portal,
						_servletContext),
					HashMapDictionaryBuilder.<String, Object>put(
						"item.selector.view.order", order++
					).build()));
		}

		synchronized (_serviceRegistrations) {
			_serviceRegistrations.put(
				company.getCompanyId(), serviceRegistrations);
		}
	}

	private void _reregisterCompanyDesignLibraryDepotEntries(long companyId) {
		synchronized (_serviceRegistrations) {
			_unregisterCompanyDesignLibraryDepotEntries(companyId);

			Company company = _companyLocalService.fetchCompany(companyId);

			if (company == null) {
				return;
			}

			_registerCompanyDesignLibraryDepotEntries(company);
		}
	}

	private void _unregisterCompanyDesignLibraryDepotEntries(long companyId) {
		Map<Long, ServiceRegistration<?>> serviceRegistrations;

		synchronized (_serviceRegistrations) {
			serviceRegistrations = _serviceRegistrations.remove(companyId);
		}

		if (serviceRegistrations == null) {
			return;
		}

		for (ServiceRegistration<?> serviceRegistration :
				serviceRegistrations.values()) {

			serviceRegistration.unregister();
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DepotEntryModelListener.class);

	private BundleContext _bundleContext;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private DepotEntryLocalService _depotEntryLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Portal _portal;

	private ServiceRegistration<PortalInstanceLifecycleListener>
		_serviceRegistration;
	private final Map<Long, Map<Long, ServiceRegistration<?>>>
		_serviceRegistrations = new HashMap<>();

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.fragment.item.selector.web)"
	)
	private ServletContext _servletContext;

	private class DesignLibraryDepotEntryPortalInstanceLifecycleListener
		implements PortalInstanceLifecycleListener {

		@Override
		public void portalInstanceRegistered(Company company) {
			_registerCompanyDesignLibraryDepotEntries(company);
		}

		@Override
		public void portalInstanceUnregistered(Company company) {
			_unregisterCompanyDesignLibraryDepotEntries(company.getCompanyId());
		}

	}

}