/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.internal.upgrade.v6_1_0;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Marco Leo
 */
public class FragmentEntryLinkEditableValuesUpgradeProcessTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_objectMapper = new ObjectMapper() {
			{
				configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
			}
		};

		_upgradeProcess = new FragmentEntryLinkEditableValuesUpgradeProcess();

		_populateCaches();
	}

	@Test
	public void testAlreadyAlignedIdsAreNotModified() throws Exception {
		String input = _read("editable_values_with_aligned_ids_input.json");

		JSONObject inputJSONObject = JSONFactoryUtil.createJSONObject(input);

		boolean editableModified = _invokeProcessEditableEntryProcessor(
			inputJSONObject);
		boolean backgroundModified =
			_invokeProcessBackgroundImageEntryProcessor(inputJSONObject);

		Assert.assertFalse(editableModified || backgroundModified);
	}

	@Test
	public void testBackgroundImageProcessorFixesIds() throws Exception {
		_assertUpgrade(
			"editable_values_with_background_image_input.json",
			"editable_values_with_background_image_expected.json");
	}

	@Test
	public void testBothProcessorsAreFixed() throws Exception {
		_assertUpgrade(
			"editable_values_with_both_processors_input.json",
			"editable_values_with_both_processors_expected.json");
	}

	@Test
	public void testDeletedFileEntriesAreSkipped() throws Exception {
		_assertUpgrade(
			"editable_values_with_deleted_file_entry_input.json",
			"editable_values_with_deleted_file_entry_expected.json");
	}

	@Test
	public void testFallbackToFileEntryIdWhenClassPKInvalid() throws Exception {
		_assertUpgrade(
			"editable_values_with_fallback_to_file_entry_id_input.json",
			"editable_values_with_fallback_to_file_entry_id_expected.json");
	}

	@Test
	public void testLocaleSpecificImagesAreFixed() throws Exception {
		_assertUpgrade(
			"editable_values_with_locale_specific_images_input.json",
			"editable_values_with_locale_specific_images_expected.json");
	}

	@Test
	public void testMappedBackgroundImagesAreSkipped() throws Exception {
		_assertUpgrade(
			"editable_values_with_mapped_background_image_input.json",
			"editable_values_with_mapped_background_image_expected.json");
	}

	@Test
	public void testMappedEditablesAreSkipped() throws Exception {
		_assertUpgrade(
			"editable_values_with_mapped_editable_input.json",
			"editable_values_with_mapped_editable_expected.json");
	}

	@Test
	public void testMismatchedClassPKAndFileEntryIdAreAligned()
		throws Exception {

		_assertUpgrade(
			"editable_values_with_mismatched_class_pk_input.json",
			"editable_values_with_mismatched_class_pk_expected.json");
	}

	@Test
	public void testNoImageFieldsAreNotModified() throws Exception {
		String input = _read("editable_values_without_image_fields_input.json");

		JSONObject inputJSONObject = JSONFactoryUtil.createJSONObject(input);

		boolean editableModified = _invokeProcessEditableEntryProcessor(
			inputJSONObject);
		boolean backgroundModified =
			_invokeProcessBackgroundImageEntryProcessor(inputJSONObject);

		Assert.assertFalse(editableModified || backgroundModified);

		Assert.assertEquals(
			_objectMapper.readTree(input),
			_objectMapper.readTree(inputJSONObject.toString()));
	}

	private void _assertUpgrade(String inputFileName, String expectedFileName)
		throws Exception {

		String input = _read(inputFileName);

		JSONObject inputJSONObject = JSONFactoryUtil.createJSONObject(input);

		_invokeProcessEditableEntryProcessor(inputJSONObject);
		_invokeProcessBackgroundImageEntryProcessor(inputJSONObject);

		Assert.assertEquals(
			_objectMapper.readTree(_read(expectedFileName)),
			_objectMapper.readTree(inputJSONObject.toString()));
	}

	private boolean _invokeProcessBackgroundImageEntryProcessor(
			JSONObject jsonObject)
		throws Exception {

		Method method =
			FragmentEntryLinkEditableValuesUpgradeProcess.class.
				getDeclaredMethod(
					"_processBackgroundImageFragmentEntryProcessor",
					JSONObject.class);

		method.setAccessible(true);

		return (boolean)method.invoke(_upgradeProcess, jsonObject);
	}

	private boolean _invokeProcessEditableEntryProcessor(JSONObject jsonObject)
		throws Exception {

		Method method =
			FragmentEntryLinkEditableValuesUpgradeProcess.class.
				getDeclaredMethod(
					"_processEditableFragmentEntryProcessor", JSONObject.class);

		method.setAccessible(true);

		return (boolean)method.invoke(_upgradeProcess, jsonObject);
	}

	@SuppressWarnings("unchecked")
	private void _populateCaches() throws Exception {
		Class<?> dlFileEntryInfoClass = Class.forName(
			FragmentEntryLinkEditableValuesUpgradeProcess.class.getName() +
				"$DLFileEntryInfo");

		Constructor<?> dlFileEntryInfoConstructor =
			dlFileEntryInfoClass.getDeclaredConstructor(
				String.class, long.class);

		dlFileEntryInfoConstructor.setAccessible(true);

		Field dlFileEntryInfoCacheField =
			FragmentEntryLinkEditableValuesUpgradeProcess.class.
				getDeclaredField("_dlFileEntryInfoCache");

		dlFileEntryInfoCacheField.setAccessible(true);

		Map<Long, Object> dlFileEntryInfoCache =
			(Map<Long, Object>)dlFileEntryInfoCacheField.get(_upgradeProcess);

		dlFileEntryInfoCache.put(
			100L, dlFileEntryInfoConstructor.newInstance("file-erc-100", 10L));
		dlFileEntryInfoCache.put(
			200L, dlFileEntryInfoConstructor.newInstance("file-erc-200", 20L));
		dlFileEntryInfoCache.put(
			300L, dlFileEntryInfoConstructor.newInstance("file-erc-300", 30L));
		dlFileEntryInfoCache.put(
			400L, dlFileEntryInfoConstructor.newInstance("file-erc-400", 40L));

		// Deleted file entries (not found in database)

		dlFileEntryInfoCache.put(998L, null);
		dlFileEntryInfoCache.put(999L, null);

		Field groupERCCacheField =
			FragmentEntryLinkEditableValuesUpgradeProcess.class.
				getDeclaredField("_groupERCCache");

		groupERCCacheField.setAccessible(true);

		Map<Long, String> groupERCCache =
			(Map<Long, String>)groupERCCacheField.get(_upgradeProcess);

		groupERCCache.put(10L, "group-erc-10");
		groupERCCache.put(20L, "group-erc-20");
		groupERCCache.put(30L, "group-erc-30");
		groupERCCache.put(40L, "group-erc-40");
	}

	private String _read(String fileName) throws Exception {
		return new String(
			FileUtil.getBytes(getClass(), "dependencies/" + fileName));
	}

	private ObjectMapper _objectMapper;
	private FragmentEntryLinkEditableValuesUpgradeProcess _upgradeProcess;

}