/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.internal.helper;

import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.fragment.contributor.FragmentCollectionContributorRegistry;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.renderer.FragmentRendererRegistry;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.layout.util.structure.DropZoneLayoutStructureItem;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.ScopeUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Lourdes Fernández Besada
 */
public class DropZoneAllowedFragmentEntriesHelperImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_featureFlagManagerUtilMockedStatic = Mockito.mockStatic(
			FeatureFlagManagerUtil.class);
		_scopeUtilMockedStatic = Mockito.mockStatic(ScopeUtil.class);

		_dropZoneAllowedFragmentEntriesHelperImpl =
			new DropZoneAllowedFragmentEntriesHelperImpl();

		ReflectionTestUtil.setFieldValue(
			_dropZoneAllowedFragmentEntriesHelperImpl,
			"_depotEntryLocalService", _depotEntryLocalService);
		ReflectionTestUtil.setFieldValue(
			_dropZoneAllowedFragmentEntriesHelperImpl,
			"_fragmentCollectionContributorRegistry",
			_fragmentCollectionContributorRegistry);
		ReflectionTestUtil.setFieldValue(
			_dropZoneAllowedFragmentEntriesHelperImpl,
			"_fragmentEntryLocalService", _fragmentEntryLocalService);
		ReflectionTestUtil.setFieldValue(
			_dropZoneAllowedFragmentEntriesHelperImpl,
			"_fragmentRendererRegistry", _fragmentRendererRegistry);
		ReflectionTestUtil.setFieldValue(
			_dropZoneAllowedFragmentEntriesHelperImpl, "_groupLocalService",
			_groupLocalService);

		Mockito.reset(
			_depotEntryLocalService, _fragmentCollectionContributorRegistry,
			_fragmentEntryLocalService, _fragmentRendererRegistry,
			_groupLocalService);

		Group companyGroup = Mockito.mock(Group.class);

		Mockito.when(
			companyGroup.getGroupId()
		).thenReturn(
			_COMPANY_GROUP_ID
		);

		Mockito.when(
			_groupLocalService.getCompanyGroup(_COMPANY_ID)
		).thenReturn(
			companyGroup
		);
	}

	@After
	public void tearDown() {
		_featureFlagManagerUtilMockedStatic.close();
		_scopeUtilMockedStatic.close();
	}

	@Test
	@TestInfo("LPP-65865")
	public void testGetFragmentEntryKeys() throws Exception {
		_testGetFragmentEntryKeys(null, Collections.emptyList());

		String legacyFragmentEntryKey = RandomTestUtil.randomString();

		_testGetFragmentEntryKeys(
			_getDropZoneLayoutStructureItem(
				null, Arrays.asList(legacyFragmentEntryKey)),
			Arrays.asList(legacyFragmentEntryKey));

		_testGetFragmentEntryKeys(
			_getDropZoneLayoutStructureItem(
				JSONUtil.putAll(), Arrays.asList(legacyFragmentEntryKey)),
			Arrays.asList(legacyFragmentEntryKey));

		String fragmentEntryRendererKey = RandomTestUtil.randomString();

		_testGetFragmentEntryKeys(
			_getDropZoneLayoutStructureItem(
				JSONUtil.putAll(
					JSONUtil.put(
						"fragmentEntryRendererKey", fragmentEntryRendererKey)),
				null),
			Arrays.asList(fragmentEntryRendererKey));

		String fragmentEntryERC = RandomTestUtil.randomString();
		String fragmentEntryKey = RandomTestUtil.randomString();
		String fragmentEntryScopeERC = RandomTestUtil.randomString();
		long fragmentEntryScopeGroupId = RandomTestUtil.randomLong();

		_mockFragmentEntryResolution(
			fragmentEntryERC, fragmentEntryKey, fragmentEntryScopeERC,
			fragmentEntryScopeGroupId);

		DropZoneLayoutStructureItem dropZoneLayoutStructureItem =
			_getDropZoneLayoutStructureItem(
				JSONUtil.putAll(
					JSONUtil.put(
						"fragmentEntryERC", fragmentEntryERC
					).put(
						"fragmentEntryScopeERC", fragmentEntryScopeERC
					)),
				null);

		_testGetFragmentEntryKeys(
			dropZoneLayoutStructureItem, Arrays.asList(fragmentEntryKey));

		_scopeUtilMockedStatic.when(
			() -> ScopeUtil.getItemGroupId(
				_COMPANY_ID, fragmentEntryScopeERC, _SCOPE_GROUP_ID)
		).thenReturn(
			null
		);

		_testGetFragmentEntryKeys(
			dropZoneLayoutStructureItem, Collections.emptyList());

		_scopeUtilMockedStatic.when(
			() -> ScopeUtil.getItemGroupId(
				_COMPANY_ID, fragmentEntryScopeERC, _SCOPE_GROUP_ID)
		).thenReturn(
			fragmentEntryScopeGroupId
		);

		Mockito.when(
			_fragmentEntryLocalService.
				fetchFragmentEntryByExternalReferenceCode(
					fragmentEntryERC, fragmentEntryScopeGroupId)
		).thenReturn(
			null
		);

		_testGetFragmentEntryKeys(
			dropZoneLayoutStructureItem, Collections.emptyList());
	}

	@Test
	@TestInfo("LPP-65865")
	public void testIsAllowedFragmentEntryKey() {
		String fragmentEntryKey = RandomTestUtil.randomString();

		_testIsAllowedFragmentEntryKey(null, true, fragmentEntryKey);

		DropZoneLayoutStructureItem dropZoneLayoutStructureItem =
			_getDropZoneLayoutStructureItem(
				null, Arrays.asList(fragmentEntryKey));

		_testIsAllowedFragmentEntryKey(
			dropZoneLayoutStructureItem, false, fragmentEntryKey);
		_testIsAllowedFragmentEntryKey(
			dropZoneLayoutStructureItem, true, RandomTestUtil.randomString());

		dropZoneLayoutStructureItem = _getDropZoneLayoutStructureItem(
			null, Collections.emptyList());

		_testIsAllowedFragmentEntryKey(
			dropZoneLayoutStructureItem, true, fragmentEntryKey);

		dropZoneLayoutStructureItem = _getDropZoneLayoutStructureItem(
			null, Arrays.asList(fragmentEntryKey));

		dropZoneLayoutStructureItem.setAllowNewFragmentEntries(false);

		_testIsAllowedFragmentEntryKey(
			dropZoneLayoutStructureItem, false, RandomTestUtil.randomString());
		_testIsAllowedFragmentEntryKey(
			dropZoneLayoutStructureItem, true, fragmentEntryKey);

		dropZoneLayoutStructureItem = _getDropZoneLayoutStructureItem(
			null, Collections.emptyList());

		dropZoneLayoutStructureItem.setAllowNewFragmentEntries(false);

		_testIsAllowedFragmentEntryKey(
			dropZoneLayoutStructureItem, false, fragmentEntryKey);
	}

	@Test
	@TestInfo("LPP-65865")
	public void testMergeFragmentEntrySettings() throws Exception {
		JSONObject itemConfigJSONObject = JSONUtil.put(
			"fragmentEntryKeys", JSONUtil.putAll());

		Assert.assertSame(
			itemConfigJSONObject,
			_dropZoneAllowedFragmentEntriesHelperImpl.
				mergeFragmentEntrySettings(
					_COMPANY_ID, null, itemConfigJSONObject, _SCOPE_GROUP_ID));

		DropZoneLayoutStructureItem dropZoneLayoutStructureItem =
			_getDropZoneLayoutStructureItem(null, null);

		JSONObject noFragmentEntryKeysJSONObject = JSONUtil.put(
			"allowNewFragmentEntries", true);

		Assert.assertSame(
			noFragmentEntryKeysJSONObject,
			_dropZoneAllowedFragmentEntriesHelperImpl.
				mergeFragmentEntrySettings(
					_COMPANY_ID, dropZoneLayoutStructureItem,
					noFragmentEntryKeysJSONObject, _SCOPE_GROUP_ID));

		_testMergeFragmentEntrySettings(
			dropZoneLayoutStructureItem, JSONUtil.putAll(),
			Collections.emptyList(), Collections.emptyList());

		String rendererKey = RandomTestUtil.randomString();

		JSONObject rendererKeyJSONObject = JSONUtil.put(
			"fragmentEntryRendererKey", rendererKey);

		dropZoneLayoutStructureItem = _getDropZoneLayoutStructureItem(
			JSONUtil.putAll(rendererKeyJSONObject), null);

		_testMergeFragmentEntrySettings(
			dropZoneLayoutStructureItem, JSONUtil.putAll(rendererKeyJSONObject),
			Collections.emptyList(), Arrays.asList(rendererKey));

		_testMergeFragmentEntrySettings(
			dropZoneLayoutStructureItem, JSONUtil.putAll(),
			Collections.emptyList(), Collections.emptyList());

		String fragmentEntryERC = RandomTestUtil.randomString();
		String fragmentEntryKey = RandomTestUtil.randomString();
		String fragmentEntryScopeERC = RandomTestUtil.randomString();
		long fragmentEntryScopeGroupId = RandomTestUtil.randomLong();

		_mockFragmentEntryResolution(
			fragmentEntryERC, fragmentEntryKey, fragmentEntryScopeERC,
			fragmentEntryScopeGroupId);

		JSONObject fragmentEntryJSONObject = JSONUtil.put(
			"fragmentEntryERC", fragmentEntryERC
		).put(
			"fragmentEntryScopeERC", fragmentEntryScopeERC
		);

		dropZoneLayoutStructureItem = _getDropZoneLayoutStructureItem(
			JSONUtil.putAll(fragmentEntryJSONObject), null);

		_testMergeFragmentEntrySettings(
			dropZoneLayoutStructureItem,
			JSONUtil.putAll(fragmentEntryJSONObject), Collections.emptyList(),
			Arrays.asList(fragmentEntryKey));

		_testMergeFragmentEntrySettings(
			dropZoneLayoutStructureItem, JSONUtil.putAll(),
			Collections.emptyList(), Collections.emptyList());

		_scopeUtilMockedStatic.when(
			() -> ScopeUtil.getItemGroupId(
				_COMPANY_ID, fragmentEntryScopeERC, _SCOPE_GROUP_ID)
		).thenReturn(
			null
		);

		_testMergeFragmentEntrySettings(
			dropZoneLayoutStructureItem,
			JSONUtil.putAll(fragmentEntryJSONObject), Collections.emptyList(),
			Collections.emptyList());

		_scopeUtilMockedStatic.when(
			() -> ScopeUtil.getItemGroupId(
				_COMPANY_ID, fragmentEntryScopeERC, _SCOPE_GROUP_ID)
		).thenReturn(
			fragmentEntryScopeGroupId
		);

		Mockito.when(
			_fragmentEntryLocalService.
				fetchFragmentEntryByExternalReferenceCode(
					fragmentEntryERC, fragmentEntryScopeGroupId)
		).thenReturn(
			null
		);

		_testMergeFragmentEntrySettings(
			dropZoneLayoutStructureItem,
			JSONUtil.putAll(fragmentEntryJSONObject), Collections.emptyList(),
			Collections.emptyList());

		String newFragmentEntryERC = RandomTestUtil.randomString();
		String newFragmentEntryKey = RandomTestUtil.randomString();

		_mockFragmentEntryPromotion(
			newFragmentEntryERC, _SCOPE_GROUP_ID, newFragmentEntryKey, null);

		dropZoneLayoutStructureItem = _getDropZoneLayoutStructureItem(
			null, null);

		_testMergeFragmentEntrySettings(
			dropZoneLayoutStructureItem,
			JSONUtil.putAll(
				JSONUtil.put(
					"fragmentEntryERC", newFragmentEntryERC
				).put(
					"fragmentEntryScopeERC", (String)null
				)),
			Collections.emptyList(), Arrays.asList(newFragmentEntryKey));

		Mockito.reset(_fragmentEntryLocalService);

		String companyGroupFragmentEntryERC = RandomTestUtil.randomString();
		String companyGroupFragmentEntryKey = RandomTestUtil.randomString();
		String companyGroupFragmentEntryScopeERC =
			RandomTestUtil.randomString();

		_mockFragmentEntryPromotion(
			companyGroupFragmentEntryERC, _COMPANY_GROUP_ID,
			companyGroupFragmentEntryKey, companyGroupFragmentEntryScopeERC);

		_testMergeFragmentEntrySettings(
			dropZoneLayoutStructureItem,
			JSONUtil.putAll(
				JSONUtil.put(
					"fragmentEntryERC", companyGroupFragmentEntryERC
				).put(
					"fragmentEntryScopeERC", companyGroupFragmentEntryScopeERC
				)),
			Collections.emptyList(),
			Arrays.asList(companyGroupFragmentEntryKey));

		String unresolvableFragmentEntryKey = RandomTestUtil.randomString();

		_testMergeFragmentEntrySettings(
			dropZoneLayoutStructureItem, JSONUtil.putAll(),
			Collections.emptyList(),
			Arrays.asList(unresolvableFragmentEntryKey));

		Mockito.reset(_fragmentEntryLocalService);

		String depotFragmentEntryERC = RandomTestUtil.randomString();
		String depotFragmentEntryKey = RandomTestUtil.randomString();
		String depotFragmentEntryScopeERC = RandomTestUtil.randomString();
		long depotGroupId = RandomTestUtil.randomLong();

		DepotEntry depotEntry = Mockito.mock(DepotEntry.class);

		Mockito.when(
			depotEntry.getGroupId()
		).thenReturn(
			depotGroupId
		);

		Mockito.when(
			_depotEntryLocalService.getGroupConnectedDepotEntries(
				_SCOPE_GROUP_ID, DepotConstants.TYPE_DESIGN_LIBRARY,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS)
		).thenReturn(
			Arrays.asList(depotEntry)
		);

		_mockFragmentEntryPromotion(
			depotFragmentEntryERC, depotGroupId, depotFragmentEntryKey,
			depotFragmentEntryScopeERC);

		_testMergeFragmentEntrySettings(
			dropZoneLayoutStructureItem, JSONUtil.putAll(),
			Collections.emptyList(), Arrays.asList(depotFragmentEntryKey));

		_featureFlagManagerUtilMockedStatic.when(
			() -> FeatureFlagManagerUtil.isEnabled(_COMPANY_ID, "LPD-57283")
		).thenReturn(
			true
		);

		_testMergeFragmentEntrySettings(
			dropZoneLayoutStructureItem,
			JSONUtil.putAll(
				JSONUtil.put(
					"fragmentEntryERC", depotFragmentEntryERC
				).put(
					"fragmentEntryScopeERC", depotFragmentEntryScopeERC
				)),
			Collections.emptyList(), Arrays.asList(depotFragmentEntryKey));
	}

	private DropZoneLayoutStructureItem _getDropZoneLayoutStructureItem(
		JSONArray fragmentEntriesJSONArray, List<String> fragmentEntryKeys) {

		DropZoneLayoutStructureItem dropZoneLayoutStructureItem =
			new DropZoneLayoutStructureItem(
				RandomTestUtil.randomString(), RandomTestUtil.randomString());

		dropZoneLayoutStructureItem.setFragmentEntriesJSONArray(
			fragmentEntriesJSONArray);
		dropZoneLayoutStructureItem.setFragmentEntryKeys(fragmentEntryKeys);

		return dropZoneLayoutStructureItem;
	}

	private void _mockFragmentEntryPromotion(
			String fragmentEntryERC, long fragmentEntryGroupId,
			String fragmentEntryKey, String fragmentEntryScopeERC)
		throws Exception {

		FragmentEntry fragmentEntry = Mockito.mock(FragmentEntry.class);

		Mockito.when(
			fragmentEntry.getExternalReferenceCode()
		).thenReturn(
			fragmentEntryERC
		);

		Mockito.when(
			fragmentEntry.getGroupId()
		).thenReturn(
			fragmentEntryGroupId
		);

		Mockito.when(
			_fragmentEntryLocalService.fetchFragmentEntry(
				fragmentEntryGroupId, fragmentEntryKey)
		).thenReturn(
			fragmentEntry
		);

		_scopeUtilMockedStatic.when(
			() -> ScopeUtil.getItemScopeExternalReferenceCode(
				fragmentEntryGroupId, _SCOPE_GROUP_ID)
		).thenReturn(
			fragmentEntryScopeERC
		);
	}

	private void _mockFragmentEntryResolution(
		String fragmentEntryERC, String fragmentEntryKey,
		String fragmentEntryScopeERC, long fragmentEntryScopeGroupId) {

		_scopeUtilMockedStatic.when(
			() -> ScopeUtil.getItemGroupId(
				_COMPANY_ID, fragmentEntryScopeERC, _SCOPE_GROUP_ID)
		).thenReturn(
			fragmentEntryScopeGroupId
		);

		FragmentEntry fragmentEntry = Mockito.mock(FragmentEntry.class);

		Mockito.when(
			fragmentEntry.getFragmentEntryKey()
		).thenReturn(
			fragmentEntryKey
		);

		Mockito.when(
			_fragmentEntryLocalService.
				fetchFragmentEntryByExternalReferenceCode(
					fragmentEntryERC, fragmentEntryScopeGroupId)
		).thenReturn(
			fragmentEntry
		);
	}

	private void _testGetFragmentEntryKeys(
		DropZoneLayoutStructureItem dropZoneLayoutStructureItem,
		List<String> expectedFragmentEntryKeys) {

		Assert.assertEquals(
			expectedFragmentEntryKeys,
			_dropZoneAllowedFragmentEntriesHelperImpl.getFragmentEntryKeys(
				_COMPANY_ID, dropZoneLayoutStructureItem, _SCOPE_GROUP_ID));
	}

	private void _testIsAllowedFragmentEntryKey(
		DropZoneLayoutStructureItem dropZoneLayoutStructureItem,
		boolean expected, String fragmentEntryKey) {

		Assert.assertEquals(
			expected,
			_dropZoneAllowedFragmentEntriesHelperImpl.isAllowedFragmentEntryKey(
				_COMPANY_ID, dropZoneLayoutStructureItem, fragmentEntryKey,
				_SCOPE_GROUP_ID));
	}

	private void _testMergeFragmentEntrySettings(
			DropZoneLayoutStructureItem dropZoneLayoutStructureItem,
			JSONArray expectedFragmentEntriesJSONArray,
			List<String> expectedFragmentEntryKeys,
			List<String> fragmentEntryKeys)
		throws Exception {

		JSONObject itemConfigJSONObject = JSONUtil.put(
			"fragmentEntryKeys", JSONUtil.putAll(fragmentEntryKeys.toArray()));

		JSONObject mergedItemConfigJSONObject =
			_dropZoneAllowedFragmentEntriesHelperImpl.
				mergeFragmentEntrySettings(
					_COMPANY_ID, dropZoneLayoutStructureItem,
					itemConfigJSONObject, _SCOPE_GROUP_ID);

		Assert.assertEquals(
			expectedFragmentEntriesJSONArray.toString(),
			mergedItemConfigJSONObject.getJSONArray(
				"fragmentEntries"
			).toString());
		Assert.assertEquals(
			expectedFragmentEntryKeys,
			JSONUtil.toStringList(
				mergedItemConfigJSONObject.getJSONArray("fragmentEntryKeys")));
	}

	private static final long _COMPANY_GROUP_ID = RandomTestUtil.randomLong();

	private static final long _COMPANY_ID = RandomTestUtil.randomLong();

	private static final long _SCOPE_GROUP_ID = RandomTestUtil.randomLong();

	private final DepotEntryLocalService _depotEntryLocalService = Mockito.mock(
		DepotEntryLocalService.class);
	private DropZoneAllowedFragmentEntriesHelperImpl
		_dropZoneAllowedFragmentEntriesHelperImpl;
	private MockedStatic<FeatureFlagManagerUtil>
		_featureFlagManagerUtilMockedStatic;
	private final FragmentCollectionContributorRegistry
		_fragmentCollectionContributorRegistry = Mockito.mock(
			FragmentCollectionContributorRegistry.class);
	private final FragmentEntryLocalService _fragmentEntryLocalService =
		Mockito.mock(FragmentEntryLocalService.class);
	private final FragmentRendererRegistry _fragmentRendererRegistry =
		Mockito.mock(FragmentRendererRegistry.class);
	private final GroupLocalService _groupLocalService = Mockito.mock(
		GroupLocalService.class);
	private MockedStatic<ScopeUtil> _scopeUtilMockedStatic;

}