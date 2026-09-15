/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.object.admin.rest.dto.v1_0.ObjectDefinition;
import com.liferay.object.admin.rest.resource.v1_0.ObjectDefinitionResource;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.site.cms.site.initializer.contributor.CMSStructureObjectFolderContributor;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Stefano Motta
 */
public class StructureBuilderDisplayContextTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetBaseObjectDefinition() throws Exception {
		String baseObjectDefinitionExternalReferenceCode =
			StringUtil.randomString();

		ObjectDefinition baseObjectDefinition = new ObjectDefinition();

		ObjectDefinitionResource objectDefinitionResource = Mockito.mock(
			ObjectDefinitionResource.class);

		Mockito.when(
			objectDefinitionResource.getObjectDefinitionByExternalReferenceCode(
				baseObjectDefinitionExternalReferenceCode)
		).thenReturn(
			baseObjectDefinition
		);

		ObjectDefinitionResource.Builder builder = Mockito.mock(
			ObjectDefinitionResource.Builder.class);

		Mockito.when(
			builder.user(Mockito.any(User.class))
		).thenReturn(
			builder
		);

		Mockito.when(
			builder.build()
		).thenReturn(
			objectDefinitionResource
		);

		ObjectDefinitionResource.Factory factory = Mockito.mock(
			ObjectDefinitionResource.Factory.class);

		Mockito.when(
			factory.create()
		).thenReturn(
			builder
		);

		CMSStructureObjectFolderContributor
			cmsStructureObjectFolderContributor = Mockito.mock(
				CMSStructureObjectFolderContributor.class);

		String objectFolderExternalReferenceCode = StringUtil.randomString();

		Mockito.when(
			cmsStructureObjectFolderContributor.
				getObjectFolderExternalReferenceCode()
		).thenReturn(
			objectFolderExternalReferenceCode
		);

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY,
			new ThemeDisplay() {
				{
					setUser(Mockito.mock(User.class));
				}
			});
		mockHttpServletRequest.setParameter(
			"objectFolderExternalReferenceCode",
			objectFolderExternalReferenceCode);

		StructureBuilderDisplayContext structureBuilderDisplayContext =
			new StructureBuilderDisplayContext(
				List.of(cmsStructureObjectFolderContributor),
				mockHttpServletRequest, null, factory, null);

		Assert.assertNull(
			ReflectionTestUtil.invoke(
				structureBuilderDisplayContext, "_getBaseObjectDefinition",
				new Class<?>[0]));

		Mockito.when(
			cmsStructureObjectFolderContributor.
				getBaseObjectDefinitionExternalReferenceCode()
		).thenReturn(
			baseObjectDefinitionExternalReferenceCode
		);

		Assert.assertSame(
			baseObjectDefinition,
			ReflectionTestUtil.invoke(
				structureBuilderDisplayContext, "_getBaseObjectDefinition",
				new Class<?>[0]));
	}

	@Test
	public void testGetSystemObjectFieldNames() throws Exception {
		ObjectDefinition objectDefinition = new ObjectDefinition();

		objectDefinition.setExternalReferenceCode(StringUtil.randomString());

		long objectDefinitionId = RandomTestUtil.randomLong();

		ObjectDefinitionResource objectDefinitionResource = Mockito.mock(
			ObjectDefinitionResource.class);

		Mockito.when(
			objectDefinitionResource.getObjectDefinition(objectDefinitionId)
		).thenReturn(
			objectDefinition
		);

		ObjectDefinitionResource.Builder builder = Mockito.mock(
			ObjectDefinitionResource.Builder.class);

		Mockito.when(
			builder.user(Mockito.any(User.class))
		).thenReturn(
			builder
		);

		Mockito.when(
			builder.build()
		).thenReturn(
			objectDefinitionResource
		);

		ObjectDefinitionResource.Factory factory = Mockito.mock(
			ObjectDefinitionResource.Factory.class);

		Mockito.when(
			factory.create()
		).thenReturn(
			builder
		);

		String baseObjectDefinitionExternalReferenceCode =
			StringUtil.randomString();
		String name1 = StringUtil.randomString();
		String name2 = StringUtil.randomString();

		Map<String, List<String>> systemObjectFieldNames =
			LinkedHashMapBuilder.<String, List<String>>put(
				baseObjectDefinitionExternalReferenceCode, List.of(name1, name2)
			).put(
				StringUtil.randomString(), List.of(StringUtil.randomString())
			).build();

		CMSStructureObjectFolderContributor
			cmsStructureObjectFolderContributor = Mockito.mock(
				CMSStructureObjectFolderContributor.class);

		Mockito.when(
			cmsStructureObjectFolderContributor.
				getBaseObjectDefinitionExternalReferenceCode()
		).thenReturn(
			baseObjectDefinitionExternalReferenceCode
		);

		String objectFolderExternalReferenceCode = StringUtil.randomString();

		Mockito.when(
			cmsStructureObjectFolderContributor.
				getObjectFolderExternalReferenceCode()
		).thenReturn(
			objectFolderExternalReferenceCode
		);

		Mockito.when(
			cmsStructureObjectFolderContributor.getSystemObjectFieldNames()
		).thenReturn(
			systemObjectFieldNames
		);

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY,
			new ThemeDisplay() {
				{
					setUser(Mockito.mock(User.class));
				}
			});
		mockHttpServletRequest.setParameter(
			"objectDefinitionId", String.valueOf(objectDefinitionId));
		mockHttpServletRequest.setParameter(
			"objectFolderExternalReferenceCode",
			objectFolderExternalReferenceCode);

		StructureBuilderDisplayContext structureBuilderDisplayContext =
			new StructureBuilderDisplayContext(
				List.of(cmsStructureObjectFolderContributor),
				mockHttpServletRequest, new JSONFactoryImpl(), factory, null);

		JSONObject jsonObject = ReflectionTestUtil.invoke(
			structureBuilderDisplayContext,
			"_getSystemObjectFieldNamesJSONObject", new Class<?>[0]);

		JSONArray jsonArray = jsonObject.getJSONArray(
			objectDefinition.getExternalReferenceCode());

		Assert.assertEquals(jsonArray.toString(), 2, jsonArray.length());
		Assert.assertEquals(name1, jsonArray.getString(0));
		Assert.assertEquals(name2, jsonArray.getString(1));
	}

}