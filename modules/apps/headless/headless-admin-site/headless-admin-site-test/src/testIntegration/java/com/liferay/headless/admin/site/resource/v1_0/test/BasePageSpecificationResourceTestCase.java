/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.resource.v1_0.test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import com.liferay.headless.admin.site.client.dto.v1_0.ContentPageSpecification;
import com.liferay.headless.admin.site.client.dto.v1_0.PageExperience;
import com.liferay.headless.admin.site.client.dto.v1_0.PageSpecification;
import com.liferay.headless.admin.site.client.dto.v1_0.WidgetPageSpecification;
import com.liferay.headless.admin.site.client.http.HttpInvoker;
import com.liferay.headless.admin.site.client.pagination.Page;
import com.liferay.headless.admin.site.client.resource.v1_0.PageSpecificationResource;
import com.liferay.headless.admin.site.client.serdes.v1_0.PageSpecificationSerDes;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.util.PropsValues;
import com.liferay.portal.vulcan.resource.EntityModelResource;

import java.lang.reflect.Method;

import java.text.DateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Generated;

import javax.ws.rs.core.MultivaluedHashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Rubén Pulido
 * @generated
 */
@Generated("")
public abstract class BasePageSpecificationResourceTestCase {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_dateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	}

	@Before
	public void setUp() throws Exception {
		irrelevantGroup = GroupTestUtil.addGroup();
		testGroup = GroupTestUtil.addGroup();

		testCompany = CompanyLocalServiceUtil.getCompany(
			testGroup.getCompanyId());

		_pageSpecificationResource.setContextCompany(testCompany);

		PageSpecificationResource.Builder builder =
			PageSpecificationResource.builder();

		pageSpecificationResource = builder.authentication(
			"test@liferay.com", PropsValues.DEFAULT_ADMIN_PASSWORD
		).locale(
			LocaleUtil.getDefault()
		).build();
	}

	@After
	public void tearDown() throws Exception {
		GroupTestUtil.deleteGroup(irrelevantGroup);
		GroupTestUtil.deleteGroup(testGroup);
	}

	@Test
	public void testClientSerDesToDTO() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper() {
			{
				configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
				configure(
					SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
				enable(SerializationFeature.INDENT_OUTPUT);
				setDateFormat(new ISO8601DateFormat());
				setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
				setSerializationInclusion(JsonInclude.Include.NON_NULL);
				setVisibility(
					PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
				setVisibility(
					PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
			}
		};

		PageSpecification pageSpecification1 = randomPageSpecification();

		String json = objectMapper.writeValueAsString(pageSpecification1);

		PageSpecification pageSpecification2 = PageSpecificationSerDes.toDTO(
			json);

		Assert.assertTrue(equals(pageSpecification1, pageSpecification2));
	}

	@Test
	public void testClientSerDesToJSON() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper() {
			{
				configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
				configure(
					SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
				setDateFormat(new ISO8601DateFormat());
				setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
				setSerializationInclusion(JsonInclude.Include.NON_NULL);
				setVisibility(
					PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
				setVisibility(
					PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
			}
		};

		PageSpecification pageSpecification = randomPageSpecification();

		String json1 = objectMapper.writeValueAsString(pageSpecification);
		String json2 = PageSpecificationSerDes.toJSON(pageSpecification);

		Assert.assertEquals(
			objectMapper.readTree(json1), objectMapper.readTree(json2));
	}

	@Test
	public void testEscapeRegexInStringFields() throws Exception {
		String regex = "^[0-9]+(\\.[0-9]{1,2})\"?";

		PageSpecification pageSpecification = randomPageSpecification();

		pageSpecification.setExternalReferenceCode(regex);

		String json = PageSpecificationSerDes.toJSON(pageSpecification);

		Assert.assertFalse(json.contains(regex));

		pageSpecification = PageSpecificationSerDes.toDTO(json);

		Assert.assertEquals(
			regex, pageSpecification.getExternalReferenceCode());
	}

	@Test
	public void testDeleteSiteSiteByExternalReferenceCodePageSpecification()
		throws Exception {

		Assert.assertTrue(false);
	}

	@Test
	public void testGetSiteSiteByExternalReferenceCodePageSpecification()
		throws Exception {

		Assert.assertTrue(false);
	}

	@Test
	public void testGraphQLGetSiteSiteByExternalReferenceCodePageSpecification()
		throws Exception {

		Assert.assertTrue(true);
	}

	@Test
	public void testGraphQLGetSiteSiteByExternalReferenceCodePageSpecificationNotFound()
		throws Exception {

		Assert.assertTrue(true);
	}

	@Test
	public void testPatchSiteSiteByExternalReferenceCodePageSpecification()
		throws Exception {

		Assert.assertTrue(false);
	}

	@Test
	public void testPutSiteSiteByExternalReferenceCodePageSpecification()
		throws Exception {

		Assert.assertTrue(false);
	}

	@Test
	public void testGetSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage()
		throws Exception {

		String siteExternalReferenceCode =
			testGetSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage_getSiteExternalReferenceCode();
		String irrelevantSiteExternalReferenceCode =
			testGetSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage_getIrrelevantSiteExternalReferenceCode();
		String pageSpecificationExternalReferenceCode =
			testGetSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage_getPageSpecificationExternalReferenceCode();
		String irrelevantPageSpecificationExternalReferenceCode =
			testGetSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage_getIrrelevantPageSpecificationExternalReferenceCode();

		Page<PageSpecification> page =
			pageSpecificationResource.
				getSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage(
					siteExternalReferenceCode,
					pageSpecificationExternalReferenceCode);

		long totalCount = page.getTotalCount();

		if ((irrelevantSiteExternalReferenceCode != null) &&
			(irrelevantPageSpecificationExternalReferenceCode != null)) {

			PageSpecification irrelevantPageSpecification =
				testGetSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage_addPageSpecification(
					irrelevantSiteExternalReferenceCode,
					irrelevantPageSpecificationExternalReferenceCode,
					randomIrrelevantPageSpecification());

			page =
				pageSpecificationResource.
					getSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage(
						irrelevantSiteExternalReferenceCode,
						irrelevantPageSpecificationExternalReferenceCode);

			Assert.assertEquals(totalCount + 1, page.getTotalCount());

			assertContains(
				irrelevantPageSpecification,
				(List<PageSpecification>)page.getItems());
			assertValid(
				page,
				testGetSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage_getExpectedActions(
					irrelevantSiteExternalReferenceCode,
					irrelevantPageSpecificationExternalReferenceCode));
		}

		PageSpecification pageSpecification1 =
			testGetSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage_addPageSpecification(
				siteExternalReferenceCode,
				pageSpecificationExternalReferenceCode,
				randomPageSpecification());

		PageSpecification pageSpecification2 =
			testGetSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage_addPageSpecification(
				siteExternalReferenceCode,
				pageSpecificationExternalReferenceCode,
				randomPageSpecification());

		page =
			pageSpecificationResource.
				getSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage(
					siteExternalReferenceCode,
					pageSpecificationExternalReferenceCode);

		Assert.assertEquals(totalCount + 2, page.getTotalCount());

		assertContains(
			pageSpecification1, (List<PageSpecification>)page.getItems());
		assertContains(
			pageSpecification2, (List<PageSpecification>)page.getItems());
		assertValid(
			page,
			testGetSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage_getExpectedActions(
				siteExternalReferenceCode,
				pageSpecificationExternalReferenceCode));
	}

	protected Map<String, Map<String, String>>
			testGetSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage_getExpectedActions(
				String siteExternalReferenceCode,
				String pageSpecificationExternalReferenceCode)
		throws Exception {

		Map<String, Map<String, String>> expectedActions = new HashMap<>();

		return expectedActions;
	}

	protected PageSpecification
			testGetSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage_addPageSpecification(
				String siteExternalReferenceCode,
				String pageSpecificationExternalReferenceCode,
				PageSpecification pageSpecification)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage_getSiteExternalReferenceCode()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage_getIrrelevantSiteExternalReferenceCode()
		throws Exception {

		return null;
	}

	protected String
			testGetSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage_getPageSpecificationExternalReferenceCode()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteSiteByExternalReferenceCodePageSpecificationPageExperiencesPage_getIrrelevantPageSpecificationExternalReferenceCode()
		throws Exception {

		return null;
	}

	@Test
	public void testPostSiteSiteByExternalReferenceCodePageSpecificationPublish()
		throws Exception {

		PageSpecification randomPageSpecification = randomPageSpecification();

		PageSpecification postPageSpecification =
			testPostSiteSiteByExternalReferenceCodePageSpecificationPublish_addPageSpecification(
				randomPageSpecification);

		assertEquals(randomPageSpecification, postPageSpecification);
		assertValid(postPageSpecification);
	}

	protected PageSpecification
			testPostSiteSiteByExternalReferenceCodePageSpecificationPublish_addPageSpecification(
				PageSpecification pageSpecification)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testPostSiteSiteByExternalReferenceCodePageSpecificationPageExperience()
		throws Exception {

		Assert.assertTrue(true);
	}

	protected void assertContains(
		PageSpecification pageSpecification,
		List<PageSpecification> pageSpecifications) {

		boolean contains = false;

		for (PageSpecification item : pageSpecifications) {
			if (equals(pageSpecification, item)) {
				contains = true;

				break;
			}
		}

		Assert.assertTrue(
			pageSpecifications + " does not contain " + pageSpecification,
			contains);
	}

	protected void assertHttpResponseStatusCode(
		int expectedHttpResponseStatusCode,
		HttpInvoker.HttpResponse actualHttpResponse) {

		Assert.assertEquals(
			expectedHttpResponseStatusCode, actualHttpResponse.getStatusCode());
	}

	protected void assertEquals(
		PageSpecification pageSpecification1,
		PageSpecification pageSpecification2) {

		Assert.assertTrue(
			pageSpecification1 + " does not equal " + pageSpecification2,
			equals(pageSpecification1, pageSpecification2));
	}

	protected void assertEquals(
		List<PageSpecification> pageSpecifications1,
		List<PageSpecification> pageSpecifications2) {

		Assert.assertEquals(
			pageSpecifications1.size(), pageSpecifications2.size());

		for (int i = 0; i < pageSpecifications1.size(); i++) {
			PageSpecification pageSpecification1 = pageSpecifications1.get(i);
			PageSpecification pageSpecification2 = pageSpecifications2.get(i);

			assertEquals(pageSpecification1, pageSpecification2);
		}
	}

	protected void assertEquals(
		PageExperience pageExperience1, PageExperience pageExperience2) {

		Assert.assertTrue(
			pageExperience1 + " does not equal " + pageExperience2,
			equals(pageExperience1, pageExperience2));
	}

	protected void assertEqualsIgnoringOrder(
		List<PageSpecification> pageSpecifications1,
		List<PageSpecification> pageSpecifications2) {

		Assert.assertEquals(
			pageSpecifications1.size(), pageSpecifications2.size());

		for (PageSpecification pageSpecification1 : pageSpecifications1) {
			boolean contains = false;

			for (PageSpecification pageSpecification2 : pageSpecifications2) {
				if (equals(pageSpecification1, pageSpecification2)) {
					contains = true;

					break;
				}
			}

			Assert.assertTrue(
				pageSpecifications2 + " does not contain " + pageSpecification1,
				contains);
		}
	}

	protected void assertValid(PageSpecification pageSpecification)
		throws Exception {

		boolean valid = true;

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals(
					"externalReferenceCode", additionalAssertFieldName)) {

				if (pageSpecification.getExternalReferenceCode() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("settings", additionalAssertFieldName)) {
				if (pageSpecification.getSettings() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("type", additionalAssertFieldName)) {
				if (pageSpecification.getType() == null) {
					valid = false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		Assert.assertTrue(valid);
	}

	protected void assertValid(Page<PageSpecification> page) {
		assertValid(page, Collections.emptyMap());
	}

	protected void assertValid(
		Page<PageSpecification> page,
		Map<String, Map<String, String>> expectedActions) {

		boolean valid = false;

		java.util.Collection<PageSpecification> pageSpecifications =
			page.getItems();

		int size = pageSpecifications.size();

		if ((page.getLastPage() > 0) && (page.getPage() > 0) &&
			(page.getPageSize() > 0) && (page.getTotalCount() > 0) &&
			(size > 0)) {

			valid = true;
		}

		Assert.assertTrue(valid);

		assertValid(page.getActions(), expectedActions);
	}

	protected void assertValid(
		Map<String, Map<String, String>> actions1,
		Map<String, Map<String, String>> actions2) {

		for (String key : actions2.keySet()) {
			Map action = actions1.get(key);

			Assert.assertNotNull(key + " does not contain an action", action);

			Map<String, String> expectedAction = actions2.get(key);

			Assert.assertEquals(
				expectedAction.get("method"), action.get("method"));
			Assert.assertEquals(expectedAction.get("href"), action.get("href"));
		}
	}

	protected void assertValid(PageExperience pageExperience) {
		boolean valid = true;

		for (String additionalAssertFieldName :
				getAdditionalPageExperienceAssertFieldNames()) {

			if (Objects.equals(
					"externalReferenceCode", additionalAssertFieldName)) {

				if (pageExperience.getExternalReferenceCode() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("key", additionalAssertFieldName)) {
				if (pageExperience.getKey() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("name_i18n", additionalAssertFieldName)) {
				if (pageExperience.getName_i18n() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("pageElements", additionalAssertFieldName)) {
				if (pageExperience.getPageElements() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("pageRules", additionalAssertFieldName)) {
				if (pageExperience.getPageRules() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("priority", additionalAssertFieldName)) {
				if (pageExperience.getPriority() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals(
					"segmentItemExternalReferences",
					additionalAssertFieldName)) {

				if (pageExperience.getSegmentItemExternalReferences() == null) {
					valid = false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		Assert.assertTrue(valid);
	}

	protected String[] getAdditionalAssertFieldNames() {
		return new String[0];
	}

	protected String[] getAdditionalPageExperienceAssertFieldNames() {
		return new String[0];
	}

	protected List<GraphQLField> getGraphQLFields() throws Exception {
		List<GraphQLField> graphQLFields = new ArrayList<>();

		for (java.lang.reflect.Field field :
				getDeclaredFields(
					com.liferay.headless.admin.site.dto.v1_0.PageSpecification.
						class)) {

			if (!ArrayUtil.contains(
					getAdditionalAssertFieldNames(), field.getName())) {

				continue;
			}

			graphQLFields.addAll(getGraphQLFields(field));
		}

		return graphQLFields;
	}

	protected List<GraphQLField> getGraphQLFields(
			java.lang.reflect.Field... fields)
		throws Exception {

		List<GraphQLField> graphQLFields = new ArrayList<>();

		for (java.lang.reflect.Field field : fields) {
			com.liferay.portal.vulcan.graphql.annotation.GraphQLField
				vulcanGraphQLField = field.getAnnotation(
					com.liferay.portal.vulcan.graphql.annotation.GraphQLField.
						class);

			if (vulcanGraphQLField != null) {
				Class<?> clazz = field.getType();

				if (clazz.isArray()) {
					clazz = clazz.getComponentType();
				}

				List<GraphQLField> childrenGraphQLFields = getGraphQLFields(
					getDeclaredFields(clazz));

				graphQLFields.add(
					new GraphQLField(field.getName(), childrenGraphQLFields));
			}
		}

		return graphQLFields;
	}

	protected String[] getIgnoredEntityFieldNames() {
		return new String[0];
	}

	protected boolean equals(
		PageSpecification pageSpecification1,
		PageSpecification pageSpecification2) {

		if (pageSpecification1 == pageSpecification2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals(
					"externalReferenceCode", additionalAssertFieldName)) {

				if (!Objects.deepEquals(
						pageSpecification1.getExternalReferenceCode(),
						pageSpecification2.getExternalReferenceCode())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("settings", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						pageSpecification1.getSettings(),
						pageSpecification2.getSettings())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("type", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						pageSpecification1.getType(),
						pageSpecification2.getType())) {

					return false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		return true;
	}

	protected boolean equals(
		Map<String, Object> map1, Map<String, Object> map2) {

		if (Objects.equals(map1.keySet(), map2.keySet())) {
			for (Map.Entry<String, Object> entry : map1.entrySet()) {
				if (entry.getValue() instanceof Map) {
					if (!equals(
							(Map)entry.getValue(),
							(Map)map2.get(entry.getKey()))) {

						return false;
					}
				}
				else if (!Objects.deepEquals(
							entry.getValue(), map2.get(entry.getKey()))) {

					return false;
				}
			}

			return true;
		}

		return false;
	}

	protected boolean equals(
		PageExperience pageExperience1, PageExperience pageExperience2) {

		if (pageExperience1 == pageExperience2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalPageExperienceAssertFieldNames()) {

			if (Objects.equals(
					"externalReferenceCode", additionalAssertFieldName)) {

				if (!Objects.deepEquals(
						pageExperience1.getExternalReferenceCode(),
						pageExperience2.getExternalReferenceCode())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("key", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						pageExperience1.getKey(), pageExperience2.getKey())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("name_i18n", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						pageExperience1.getName_i18n(),
						pageExperience2.getName_i18n())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("pageElements", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						pageExperience1.getPageElements(),
						pageExperience2.getPageElements())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("pageRules", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						pageExperience1.getPageRules(),
						pageExperience2.getPageRules())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("priority", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						pageExperience1.getPriority(),
						pageExperience2.getPriority())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(
					"segmentItemExternalReferences",
					additionalAssertFieldName)) {

				if (!Objects.deepEquals(
						pageExperience1.getSegmentItemExternalReferences(),
						pageExperience2.getSegmentItemExternalReferences())) {

					return false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		return true;
	}

	protected java.lang.reflect.Field[] getDeclaredFields(Class clazz)
		throws Exception {

		if (clazz.getClassLoader() == null) {
			return new java.lang.reflect.Field[0];
		}

		return TransformUtil.transform(
			ReflectionUtil.getDeclaredFields(clazz),
			field -> {
				if (field.isSynthetic()) {
					return null;
				}

				return field;
			},
			java.lang.reflect.Field.class);
	}

	protected java.util.Collection<EntityField> getEntityFields()
		throws Exception {

		if (!(_pageSpecificationResource instanceof EntityModelResource)) {
			throw new UnsupportedOperationException(
				"Resource is not an instance of EntityModelResource");
		}

		EntityModelResource entityModelResource =
			(EntityModelResource)_pageSpecificationResource;

		EntityModel entityModel = entityModelResource.getEntityModel(
			new MultivaluedHashMap());

		if (entityModel == null) {
			return Collections.emptyList();
		}

		Map<String, EntityField> entityFieldsMap =
			entityModel.getEntityFieldsMap();

		return entityFieldsMap.values();
	}

	protected List<EntityField> getEntityFields(EntityField.Type type)
		throws Exception {

		return TransformUtil.transform(
			getEntityFields(),
			entityField -> {
				if (!Objects.equals(entityField.getType(), type) ||
					ArrayUtil.contains(
						getIgnoredEntityFieldNames(), entityField.getName())) {

					return null;
				}

				return entityField;
			});
	}

	protected String getFilterString(
		EntityField entityField, String operator,
		PageSpecification pageSpecification) {

		StringBundler sb = new StringBundler();

		String entityFieldName = entityField.getName();

		sb.append(entityFieldName);

		sb.append(" ");
		sb.append(operator);
		sb.append(" ");

		if (entityFieldName.equals("externalReferenceCode")) {
			Object object = pageSpecification.getExternalReferenceCode();

			String value = String.valueOf(object);

			if (operator.equals("contains")) {
				sb = new StringBundler();

				sb.append("contains(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 2)) {
					sb.append(value.substring(1, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else if (operator.equals("startswith")) {
				sb = new StringBundler();

				sb.append("startswith(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 1)) {
					sb.append(value.substring(0, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else {
				sb.append("'");
				sb.append(value);
				sb.append("'");
			}

			return sb.toString();
		}

		if (entityFieldName.equals("settings")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("type")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		throw new IllegalArgumentException(
			"Invalid entity field " + entityFieldName);
	}

	protected String invoke(String query) throws Exception {
		HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

		httpInvoker.body(
			JSONUtil.put(
				"query", query
			).toString(),
			"application/json");
		httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);
		httpInvoker.path("http://localhost:8080/o/graphql");
		httpInvoker.userNameAndPassword(
			"test@liferay.com:" + PropsValues.DEFAULT_ADMIN_PASSWORD);

		HttpInvoker.HttpResponse httpResponse = httpInvoker.invoke();

		return httpResponse.getContent();
	}

	protected JSONObject invokeGraphQLMutation(GraphQLField graphQLField)
		throws Exception {

		GraphQLField mutationGraphQLField = new GraphQLField(
			"mutation", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(mutationGraphQLField.toString()));
	}

	protected JSONObject invokeGraphQLQuery(GraphQLField graphQLField)
		throws Exception {

		GraphQLField queryGraphQLField = new GraphQLField(
			"query", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(queryGraphQLField.toString()));
	}

	protected PageSpecification randomPageSpecification() throws Exception {
		switch (RandomTestUtil.randomInt(0, 1)) {
			case 0:
				return new ContentPageSpecification() {
					{
						externalReferenceCode = StringUtil.toLowerCase(
							RandomTestUtil.randomString());
						type = Type.create("ContentPageSpecification");
					}
				};

			case 1:
				return new WidgetPageSpecification() {
					{
						externalReferenceCode = StringUtil.toLowerCase(
							RandomTestUtil.randomString());
						type = Type.create("WidgetPageSpecification");
					}
				};
		}

		return null;
	}

	protected PageSpecification randomIrrelevantPageSpecification()
		throws Exception {

		PageSpecification randomIrrelevantPageSpecification =
			randomPageSpecification();

		return randomIrrelevantPageSpecification;
	}

	protected PageSpecification randomPatchPageSpecification()
		throws Exception {

		return randomPageSpecification();
	}

	protected PageExperience randomPageExperience() throws Exception {
		return new PageExperience() {
			{
				externalReferenceCode = RandomTestUtil.randomString();
				key = RandomTestUtil.randomString();
				priority = RandomTestUtil.randomInteger();
			}
		};
	}

	protected PageSpecificationResource pageSpecificationResource;
	protected com.liferay.portal.kernel.model.Group irrelevantGroup;
	protected com.liferay.portal.kernel.model.Company testCompany;
	protected com.liferay.portal.kernel.model.Group testGroup;

	protected static class BeanTestUtil {

		public static void copyProperties(Object source, Object target)
			throws Exception {

			Class<?> sourceClass = _getSuperClass(source.getClass());

			Class<?> targetClass = target.getClass();

			for (java.lang.reflect.Field field :
					sourceClass.getDeclaredFields()) {

				if (field.isSynthetic()) {
					continue;
				}

				Method getMethod = _getMethod(
					sourceClass, field.getName(), "get");

				Method setMethod = _getMethod(
					targetClass, field.getName(), "set",
					getMethod.getReturnType());

				setMethod.invoke(target, getMethod.invoke(source));
			}
		}

		public static boolean hasProperty(Object bean, String name) {
			Method setMethod = _getMethod(
				bean.getClass(), "set" + StringUtil.upperCaseFirstLetter(name));

			if (setMethod != null) {
				return true;
			}

			return false;
		}

		public static void setProperty(Object bean, String name, Object value)
			throws Exception {

			Class<?> clazz = bean.getClass();

			Method setMethod = _getMethod(
				clazz, "set" + StringUtil.upperCaseFirstLetter(name));

			if (setMethod == null) {
				throw new NoSuchMethodException();
			}

			Class<?>[] parameterTypes = setMethod.getParameterTypes();

			setMethod.invoke(bean, _translateValue(parameterTypes[0], value));
		}

		private static Method _getMethod(Class<?> clazz, String name) {
			for (Method method : clazz.getMethods()) {
				if (name.equals(method.getName()) &&
					(method.getParameterCount() == 1) &&
					_parameterTypes.contains(method.getParameterTypes()[0])) {

					return method;
				}
			}

			return null;
		}

		private static Method _getMethod(
				Class<?> clazz, String fieldName, String prefix,
				Class<?>... parameterTypes)
			throws Exception {

			return clazz.getMethod(
				prefix + StringUtil.upperCaseFirstLetter(fieldName),
				parameterTypes);
		}

		private static Class<?> _getSuperClass(Class<?> clazz) {
			Class<?> superClass = clazz.getSuperclass();

			if ((superClass == null) || (superClass == Object.class)) {
				return clazz;
			}

			return superClass;
		}

		private static Object _translateValue(
			Class<?> parameterType, Object value) {

			if ((value instanceof Integer) &&
				parameterType.equals(Long.class)) {

				Integer intValue = (Integer)value;

				return intValue.longValue();
			}

			return value;
		}

		private static final Set<Class<?>> _parameterTypes = new HashSet<>(
			Arrays.asList(
				Boolean.class, Date.class, Double.class, Integer.class,
				Long.class, Map.class, String.class));

	}

	protected class GraphQLField {

		public GraphQLField(String key, GraphQLField... graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(String key, List<GraphQLField> graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			GraphQLField... graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = Arrays.asList(graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			List<GraphQLField> graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = graphQLFields;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(_key);

			if (!_parameterMap.isEmpty()) {
				sb.append("(");

				for (Map.Entry<String, Object> entry :
						_parameterMap.entrySet()) {

					sb.append(entry.getKey());
					sb.append(": ");
					sb.append(entry.getValue());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append(")");
			}

			if (!_graphQLFields.isEmpty()) {
				sb.append("{");

				for (GraphQLField graphQLField : _graphQLFields) {
					sb.append(graphQLField.toString());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append("}");
			}

			return sb.toString();
		}

		private final List<GraphQLField> _graphQLFields;
		private final String _key;
		private final Map<String, Object> _parameterMap;

	}

	private static final com.liferay.portal.kernel.log.Log _log =
		LogFactoryUtil.getLog(BasePageSpecificationResourceTestCase.class);

	private static DateFormat _dateFormat;

	@Inject
	private
		com.liferay.headless.admin.site.resource.v1_0.PageSpecificationResource
			_pageSpecificationResource;

}