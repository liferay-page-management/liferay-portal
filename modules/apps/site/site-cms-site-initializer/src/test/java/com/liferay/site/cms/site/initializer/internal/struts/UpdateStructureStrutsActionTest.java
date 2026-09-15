/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.struts;

import com.liferay.object.admin.rest.dto.v1_0.ObjectDefinition;
import com.liferay.object.admin.rest.dto.v1_0.ObjectDefinitionSetting;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Stefano Motta
 */
public class UpdateStructureStrutsActionTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testMergeObjectDefinitionSettings() throws Exception {
		ObjectDefinition existingObjectDefinition = new ObjectDefinition();
		String key1 = RandomTestUtil.randomString();

		existingObjectDefinition.setObjectDefinitionSettings(
			new ObjectDefinitionSetting[] {
				_createObjectDefinitionSetting("acceptAllGroups", "true"),
				_createObjectDefinitionSetting(
					"allowStandaloneObjectEntry", "true"),
				_createObjectDefinitionSetting(
					key1, RandomTestUtil.randomString())
			});

		String titleObjectFieldName = RandomTestUtil.randomString();

		existingObjectDefinition.setTitleObjectFieldName(titleObjectFieldName);

		ObjectDefinition objectDefinition = new ObjectDefinition();

		String key2 = RandomTestUtil.randomString();
		String value1 = RandomTestUtil.randomString();
		String value2 = RandomTestUtil.randomString();

		objectDefinition.setObjectDefinitionSettings(
			new ObjectDefinitionSetting[] {
				_createObjectDefinitionSetting(key1, value1),
				_createObjectDefinitionSetting(key2, value2)
			});

		objectDefinition.setTitleObjectFieldName(RandomTestUtil.randomString());

		ReflectionTestUtil.invoke(
			new UpdateStructureStrutsAction(), "_mergeObjectDefinitionSettings",
			new Class<?>[] {ObjectDefinition.class, ObjectDefinition.class},
			existingObjectDefinition, objectDefinition);

		ObjectDefinitionSetting[] objectDefinitionSettings =
			objectDefinition.getObjectDefinitionSettings();

		Map<String, Object> valuesMap = new HashMap<>();

		for (ObjectDefinitionSetting objectDefinitionSetting :
				objectDefinitionSettings) {

			valuesMap.put(
				objectDefinitionSetting.getName(),
				objectDefinitionSetting.getValue());
		}

		Assert.assertFalse(valuesMap.containsKey("acceptAllGroups"));
		Assert.assertFalse(
			valuesMap.containsKey("acceptedGroupExternalReferenceCodes"));
		Assert.assertFalse(valuesMap.containsKey("allowStandaloneObjectEntry"));
		Assert.assertEquals(value1, valuesMap.get(key1));
		Assert.assertEquals(value2, valuesMap.get(key2));

		Assert.assertEquals(
			titleObjectFieldName, objectDefinition.getTitleObjectFieldName());
	}

	private ObjectDefinitionSetting _createObjectDefinitionSetting(
		String name, Object value) {

		ObjectDefinitionSetting objectDefinitionSetting =
			new ObjectDefinitionSetting();

		objectDefinitionSetting.setName(name);
		objectDefinitionSetting.setValue(value);

		return objectDefinitionSetting;
	}

}