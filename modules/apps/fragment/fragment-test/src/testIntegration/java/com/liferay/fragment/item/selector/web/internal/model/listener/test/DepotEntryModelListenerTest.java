/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.item.selector.web.internal.model.listener.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryGroupRelLocalService;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.item.selector.ItemSelectorView;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.CollatorUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.text.Collator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.util.promise.Promise;

/**
 * @author Lourdes Fernández Besada
 */
@FeatureFlag("LPD-57283")
@RunWith(Arquillian.class)
public class DepotEntryModelListenerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		FeatureFlagTestUtil.invokeFeatureFlagListeners(
			TestPropsValues.getCompanyId(), true, "LPD-57283");

		_bundleContext = SystemBundleUtil.getBundleContext();
		_group = GroupTestUtil.addGroup();

		_setBaselineDesignLibraryItemSelectorViewTitles();
	}

	@Test
	@TestInfo("LPD-112439")
	public void testDeactivateAndActivate() throws Exception {
		String name = RandomTestUtil.randomString();

		_addConnectedDesignLibraryDepotEntry(name);

		Assert.assertEquals(
			Collections.singletonList(name),
			_getAddedDesignLibraryItemSelectorViewTitles());

		Class<?> depotEntryModelListenerClass =
			_depotEntryModelListener.getClass();

		ComponentDescriptionDTO componentDescriptionDTO =
			_serviceComponentRuntime.getComponentDescriptionDTO(
				FrameworkUtil.getBundle(depotEntryModelListenerClass),
				"com.liferay.fragment.item.selector.web.internal.model." +
					"listener.DepotEntryModelListener");

		Promise<Void> disablePromise =
			_serviceComponentRuntime.disableComponent(componentDescriptionDTO);

		disablePromise.getValue();

		try {
			Assert.assertEquals(
				Collections.emptyList(),
				_getDesignLibraryItemSelectorViewTitles());
		}
		finally {
			Promise<Void> enablePromise =
				_serviceComponentRuntime.enableComponent(
					componentDescriptionDTO);

			enablePromise.getValue();
		}

		Assert.assertEquals(
			Collections.singletonList(name),
			_waitForAddedDesignLibraryItemSelectorViewTitles(
				Collections.singletonList(name)));
	}

	@Test
	@TestInfo("LPD-112439")
	public void testOnAfterCreateAndOnAfterRemove() throws Exception {
		String name1 = RandomTestUtil.randomString();

		DepotEntry depotEntry1 = _addConnectedDesignLibraryDepotEntry(name1);

		Assert.assertEquals(
			_sortByCollator(name1),
			_getAddedDesignLibraryItemSelectorViewTitles());

		String name2 = RandomTestUtil.randomString();

		DepotEntry depotEntry2 = _addConnectedDesignLibraryDepotEntry(name2);

		Assert.assertEquals(
			_sortByCollator(name1, name2),
			_getAddedDesignLibraryItemSelectorViewTitles());

		String name3 = RandomTestUtil.randomString();

		DepotEntry depotEntry3 = _addConnectedDesignLibraryDepotEntry(name3);

		List<String> expectedTitles = _sortByCollator(name1, name2, name3);

		Assert.assertEquals(
			expectedTitles, _getAddedDesignLibraryItemSelectorViewTitles());

		DepotEntry unrelatedDepotEntry = _addConnectedDepotEntry(
			DepotConstants.TYPE_SPACE, RandomTestUtil.randomString());

		Assert.assertEquals(
			expectedTitles, _getAddedDesignLibraryItemSelectorViewTitles());

		String middleName = expectedTitles.remove(1);

		DepotEntry middleDepotEntry = depotEntry1;

		if (middleName.equals(name2)) {
			middleDepotEntry = depotEntry2;
		}
		else if (middleName.equals(name3)) {
			middleDepotEntry = depotEntry3;
		}

		_deleteDepotEntry(middleDepotEntry);

		Assert.assertEquals(
			expectedTitles, _getAddedDesignLibraryItemSelectorViewTitles());

		_deleteDepotEntry(unrelatedDepotEntry);

		Assert.assertEquals(
			expectedTitles, _getAddedDesignLibraryItemSelectorViewTitles());
	}

	private DepotEntry _addConnectedDepotEntry(int type, String name)
		throws Exception {

		DepotEntry depotEntry = _depotEntryLocalService.addDepotEntry(
			Collections.singletonMap(LocaleUtil.getDefault(), name),
			Collections.emptyMap(), type,
			ServiceContextTestUtil.getServiceContext());

		_depotEntryGroupRelLocalService.addDepotEntryGroupRel(
			depotEntry.getDepotEntryId(), _group.getGroupId());

		_depotEntries.add(depotEntry);

		return depotEntry;
	}

	private DepotEntry _addConnectedDesignLibraryDepotEntry(String name)
		throws Exception {

		return _addConnectedDepotEntry(
			DepotConstants.TYPE_DESIGN_LIBRARY, name);
	}

	private void _deleteDepotEntry(DepotEntry depotEntry) throws Exception {
		_depotEntryLocalService.deleteDepotEntry(depotEntry);

		_depotEntries.remove(depotEntry);
	}

	private List<String> _getAddedDesignLibraryItemSelectorViewTitles()
		throws Exception {

		List<String> titles = _getDesignLibraryItemSelectorViewTitles();

		titles.removeAll(_baselineDesignLibraryItemSelectorViewTitles);

		return titles;
	}

	private List<ServiceReference<?>>
			_getDesignLibraryItemSelectorViewServiceReferences()
		throws Exception {

		ServiceReference<?>[] serviceReferences =
			_bundleContext.getServiceReferences(
				ItemSelectorView.class.getName(), null);

		if (serviceReferences == null) {
			return Collections.emptyList();
		}

		return ListUtil.sort(
			TransformUtil.transformToList(
				serviceReferences,
				serviceReference -> {
					Object service = _bundleContext.getService(
						serviceReference);

					try {
						Class<?> serviceClass = service.getClass();

						String className = serviceClass.getName();

						String designLibraryViewClassName =
							"com.liferay.fragment.item.selector.web.internal." +
								"DesignLibraryFragmentEntryItemSelectorView";

						if (className.equals(designLibraryViewClassName)) {
							return serviceReference;
						}

						return null;
					}
					finally {
						_bundleContext.ungetService(serviceReference);
					}
				}),
			(serviceReference1, serviceReference2) -> {
				int order1 = GetterUtil.getInteger(
					serviceReference1.getProperty("item.selector.view.order"));
				int order2 = GetterUtil.getInteger(
					serviceReference2.getProperty("item.selector.view.order"));

				return Integer.compare(order1, order2);
			});
	}

	private List<String> _getDesignLibraryItemSelectorViewTitles()
		throws Exception {

		return TransformUtil.transform(
			_getDesignLibraryItemSelectorViewServiceReferences(),
			serviceReference -> {
				ItemSelectorView<?> itemSelectorView =
					(ItemSelectorView<?>)_bundleContext.getService(
						serviceReference);

				try {
					return itemSelectorView.getTitle(LocaleUtil.getDefault());
				}
				finally {
					_bundleContext.ungetService(serviceReference);
				}
			});
	}

	private void _setBaselineDesignLibraryItemSelectorViewTitles()
		throws Exception {

		List<String> previous = _getDesignLibraryItemSelectorViewTitles();

		long deadline = System.currentTimeMillis() + 5000;

		while (System.currentTimeMillis() < deadline) {
			Thread.sleep(100);

			List<String> current = _getDesignLibraryItemSelectorViewTitles();

			if (current.equals(previous)) {
				break;
			}

			previous = current;
		}

		_baselineDesignLibraryItemSelectorViewTitles = previous;
	}

	private List<String> _sortByCollator(String... names) {
		Collator collator = CollatorUtil.getInstance(LocaleUtil.getDefault());

		List<String> sorted = new ArrayList<>(Arrays.asList(names));

		sorted.sort(collator);

		return sorted;
	}

	private List<String> _waitForAddedDesignLibraryItemSelectorViewTitles(
			List<String> expected)
		throws Exception {

		long deadline = System.currentTimeMillis() + 5000;

		List<String> titles = _getAddedDesignLibraryItemSelectorViewTitles();

		while (!titles.equals(expected) &&
			   (System.currentTimeMillis() < deadline)) {

			Thread.sleep(100);

			titles = _getAddedDesignLibraryItemSelectorViewTitles();
		}

		return titles;
	}

	private List<String> _baselineDesignLibraryItemSelectorViewTitles;
	private BundleContext _bundleContext;

	@DeleteAfterTestRun
	private final List<DepotEntry> _depotEntries = new ArrayList<>();

	@Inject
	private DepotEntryGroupRelLocalService _depotEntryGroupRelLocalService;

	@Inject
	private DepotEntryLocalService _depotEntryLocalService;

	@Inject(
		filter = "component.name=com.liferay.fragment.item.selector.web.internal.model.listener.DepotEntryModelListener"
	)
	private ModelListener<DepotEntry> _depotEntryModelListener;

	private Group _group;

	@Inject
	private ServiceComponentRuntime _serviceComponentRuntime;

}