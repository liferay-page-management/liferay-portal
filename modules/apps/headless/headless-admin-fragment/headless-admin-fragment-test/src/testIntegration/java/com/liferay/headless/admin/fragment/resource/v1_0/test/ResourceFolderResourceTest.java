/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.fragment.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.exportimport.test.util.LazyReferencingTestUtil;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.service.FragmentCollectionLocalService;
import com.liferay.headless.admin.fragment.client.dto.v1_0.FragmentSet;
import com.liferay.headless.admin.fragment.client.dto.v1_0.ResourceFolder;
import com.liferay.headless.admin.fragment.client.pagination.Page;
import com.liferay.headless.admin.fragment.client.pagination.Pagination;
import com.liferay.headless.admin.fragment.client.problem.Problem;
import com.liferay.headless.admin.fragment.client.resource.v1_0.ResourceFolderResource;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Repository;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portletfilerepository.PortletFileRepositoryUtil;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.HTTPTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.InputStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Rubén Pulido
 */
@FeatureFlag("LPD-39244")
@RunWith(Arquillian.class)
public class ResourceFolderResourceTest
	extends BaseResourceFolderResourceTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		String password = RandomTestUtil.randomString();

		User user = UserTestUtil.addUser(testCompany, password);

		_resourceFolderResource = ResourceFolderResource.builder(
		).authentication(
			user.getEmailAddress(), password
		).endpoint(
			testCompany.getVirtualHostname(),
			PortalUtil.getPortalServerPort(false), "http"
		).locale(
			LocaleUtil.getDefault()
		).build();
	}

	@Override
	@Test
	public void testGetSiteFragmentSetResourceFoldersPage() throws Exception {
		super.testGetSiteFragmentSetResourceFoldersPage();

		_testGetSiteFragmentSetResourceFoldersPage();
		_testGetSiteFragmentSetResourceFoldersPageEmpty();
		_testGetSiteFragmentSetResourceFoldersPageFragmentSetNonexistentProblemException();
		_testGetSiteFragmentSetResourceFoldersPageNoPermissionProblemException();
	}

	@Override
	@Test
	public void testGetSiteResourceFolder() throws Exception {
		super.testGetSiteResourceFolder();

		_testGetSiteResourceFolderNoPermissionProblemException();
		_testGetSiteResourceFolderResourceFolderNonexistentProblemException();
	}

	@Override
	@Test
	public void testGetSiteResourceFolderResourceFoldersPage()
		throws Exception {

		super.testGetSiteResourceFolderResourceFoldersPage();

		_testGetSiteResourceFolderResourceFoldersPage();
		_testGetSiteResourceFolderResourceFoldersPageEmpty();
		_testGetSiteResourceFolderResourceFoldersPageNoPermissionProblemException();
		_testGetSiteResourceFolderResourceFoldersPageResourceFolderNonexistentProblemException();
	}

	@Override
	@Test
	public void testGetSiteResourceFoldersPage() throws Exception {
		super.testGetSiteResourceFoldersPage();

		_testGetSiteResourceFoldersPage();
		_testGetSiteResourceFoldersPageNoPermissionProblemException();
		_testGetSiteResourceFoldersPagePortletFolder();
	}

	@Override
	@Test
	public void testPostSiteFragmentSetResourceFolder() throws Exception {
		super.testPostSiteFragmentSetResourceFolder();

		_testPostSiteFragmentSetResourceFolderNoPermissionProblemException();
	}

	@Override
	@Test
	public void testPostSiteResourceFolder() throws Exception {
		super.testPostSiteResourceFolder();

		_testPostSiteResourceFolder();
		_testPostSiteResourceFolderBatch();
		_testPostSiteResourceFolderDuplicateExternalReferenceCodeProblemException();
		_testPostSiteResourceFolderFragmentSetExternalReferenceCodeNullProblemException();
		_testPostSiteResourceFolderFragmentSetNonexistingProblemException();
		_testPostSiteResourceFolderNoPermissionProblemException();
	}

	@Override
	@Test
	public void testPutSiteResourceFolder() throws Exception {
		_testPutSiteResourceFolder();
		_testPutSiteResourceFolderNoPermissionProblemException();
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"externalReferenceCode", "name"};
	}

	@Override
	protected ResourceFolder randomResourceFolder() throws Exception {
		return _randomResourceFolder(
			_toFragmentSet(_getFragmentSetExternalReferenceCode()));
	}

	@Override
	protected ResourceFolder
			testGetSiteFragmentSetResourceFoldersPage_addResourceFolder(
				String siteExternalReferenceCode,
				String fragmentSetExternalReferenceCode,
				ResourceFolder resourceFolder)
		throws Exception {

		resourceFolder.setFragmentSet(
			_toFragmentSet(fragmentSetExternalReferenceCode));

		return resourceFolderResource.postSiteFragmentSetResourceFolder(
			siteExternalReferenceCode, fragmentSetExternalReferenceCode,
			resourceFolder);
	}

	@Override
	protected Map<String, Map<String, String>>
			testGetSiteFragmentSetResourceFoldersPage_getExpectedActions(
				String siteExternalReferenceCode,
				String fragmentSetExternalReferenceCode)
		throws Exception {

		return new HashMap<>();
	}

	@Override
	protected String
			testGetSiteFragmentSetResourceFoldersPage_getFragmentSetExternalReferenceCode()
		throws Exception {

		return _getFragmentSetExternalReferenceCode();
	}

	@Override
	protected ResourceFolder
			testGetSiteResourceFolderResourceFoldersPage_addResourceFolder(
				String siteExternalReferenceCode,
				String resourceFolderExternalReferenceCode,
				ResourceFolder resourceFolder)
		throws Exception {

		resourceFolder.setParentResourceFolder(
			_toResourceFolder(resourceFolderExternalReferenceCode));

		return resourceFolderResource.postSiteResourceFolder(
			siteExternalReferenceCode, resourceFolder);
	}

	@Override
	protected String
			testGetSiteResourceFolderResourceFoldersPage_getResourceFolderExternalReferenceCode()
		throws Exception {

		ResourceFolder resourceFolder =
			resourceFolderResource.postSiteResourceFolder(
				testGroup.getExternalReferenceCode(), randomResourceFolder());

		return resourceFolder.getExternalReferenceCode();
	}

	@Override
	protected ResourceFolder testGetSiteResourceFoldersPage_addResourceFolder(
			String siteExternalReferenceCode, ResourceFolder resourceFolder)
		throws Exception {

		Group group = _groupLocalService.getGroupByExternalReferenceCode(
			siteExternalReferenceCode, testCompany.getCompanyId());

		resourceFolder.setFragmentSet(
			_toFragmentSet(_addFragmentCollection(group.getGroupId())));

		return resourceFolderResource.postSiteResourceFolder(
			siteExternalReferenceCode, resourceFolder);
	}

	@Override
	protected Map<String, Map<String, String>>
			testGetSiteResourceFoldersPage_getExpectedActions(
				String siteExternalReferenceCode)
		throws Exception {

		return new HashMap<>();
	}

	@Override
	protected ResourceFolder
			testPostSiteFragmentSetResourceFolder_addResourceFolder(
				ResourceFolder resourceFolder)
		throws Exception {

		FragmentSet fragmentSet = resourceFolder.getFragmentSet();

		return resourceFolderResource.postSiteFragmentSetResourceFolder(
			testGroup.getExternalReferenceCode(),
			fragmentSet.getExternalReferenceCode(), resourceFolder);
	}

	@Override
	protected ResourceFolder testPostSiteResourceFolder_addResourceFolder(
			ResourceFolder resourceFolder)
		throws Exception {

		return resourceFolderResource.postSiteResourceFolder(
			testGroup.getExternalReferenceCode(), resourceFolder);
	}

	private String _addFragmentCollection(long groupId) throws Exception {
		FragmentCollection fragmentCollection =
			_fragmentCollectionLocalService.addFragmentCollection(
				RandomTestUtil.randomString(), TestPropsValues.getUserId(),
				groupId, RandomTestUtil.randomString(),
				RandomTestUtil.randomString(), null, false,
				ServiceContextTestUtil.getServiceContext(groupId));

		return fragmentCollection.getExternalReferenceCode();
	}

	private void _addPortletFolder() throws Exception {
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(testGroup.getGroupId());

		Repository repository = PortletFileRepositoryUtil.addPortletRepository(
			testGroup.getGroupId(), RandomTestUtil.randomString(),
			serviceContext);

		PortletFileRepositoryUtil.addPortletFolder(
			TestPropsValues.getUserId(), repository.getRepositoryId(),
			repository.getDlFolderId(), RandomTestUtil.randomString(),
			serviceContext);
	}

	private String _exportResourceFoldersToJSON(
			String siteExternalReferenceCode)
		throws Exception {

		JSONObject exportTaskJSONObject = _waitForExportFinish(
			HTTPTestUtil.invokeToJSONObject(
				null,
				"headless-admin-fragment/v1.0/sites/" +
					siteExternalReferenceCode +
						"/resource-folders/export-batch?contentType=JSON",
				Http.Method.POST));

		try (InputStream inputStream = HTTPTestUtil.invokeToInputStream(
				null,
				StringBundler.concat(
					"headless-batch-engine/v1.0/export-task",
					"/by-external-reference-code/",
					exportTaskJSONObject.getString("externalReferenceCode"),
					"/content"),
				HashMapBuilder.put(
					HttpHeaders.ACCEPT, ContentTypes.APPLICATION_OCTET_STREAM
				).build(),
				Http.Method.GET)) {

			ZipInputStream zipInputStream = new ZipInputStream(inputStream);

			zipInputStream.getNextEntry();

			return StringUtil.read(zipInputStream);
		}
	}

	private String _getFragmentSetExternalReferenceCode() throws Exception {
		if (_fragmentSetExternalReferenceCode == null) {
			_fragmentSetExternalReferenceCode = _addFragmentCollection(
				testGroup.getGroupId());
		}

		return _fragmentSetExternalReferenceCode;
	}

	private ResourceFolder _postSiteResourceFolder(
			ResourceFolder parentResourceFolder)
		throws Exception {

		return resourceFolderResource.postSiteResourceFolder(
			testGroup.getExternalReferenceCode(),
			_randomResourceFolder(parentResourceFolder));
	}

	private ResourceFolder _postSiteResourceFolder(
			String fragmentSetExternalReferenceCode)
		throws Exception {

		return resourceFolderResource.postSiteResourceFolder(
			testGroup.getExternalReferenceCode(),
			_randomResourceFolder(
				_toFragmentSet(fragmentSetExternalReferenceCode)));
	}

	private ResourceFolder _randomResourceFolder(FragmentSet fragmentSet)
		throws Exception {

		ResourceFolder resourceFolder = super.randomResourceFolder();

		resourceFolder.setFragmentSet(fragmentSet);

		return resourceFolder;
	}

	private ResourceFolder _randomResourceFolder(
			ResourceFolder parentResourceFolder)
		throws Exception {

		ResourceFolder resourceFolder = super.randomResourceFolder();

		resourceFolder.setFragmentSet(parentResourceFolder.getFragmentSet());
		resourceFolder.setParentResourceFolder(parentResourceFolder);

		return resourceFolder;
	}

	private void _testGetSiteFragmentSetResourceFoldersPage() throws Exception {
		String fragmentSetExternalReferenceCode = _addFragmentCollection(
			testGroup.getGroupId());

		ResourceFolder resourceFolder = _postSiteResourceFolder(
			fragmentSetExternalReferenceCode);

		_postSiteResourceFolder(resourceFolder);

		Page<ResourceFolder> page =
			resourceFolderResource.getSiteFragmentSetResourceFoldersPage(
				testGroup.getExternalReferenceCode(),
				fragmentSetExternalReferenceCode, Pagination.of(1, 10));

		assertContains(resourceFolder, (List<ResourceFolder>)page.getItems());
		Assert.assertEquals(1, page.getTotalCount());
	}

	private void _testGetSiteFragmentSetResourceFoldersPageEmpty()
		throws Exception {

		String fragmentSetExternalReferenceCode = _addFragmentCollection(
			testGroup.getGroupId());

		Page<ResourceFolder> page =
			resourceFolderResource.getSiteFragmentSetResourceFoldersPage(
				testGroup.getExternalReferenceCode(),
				fragmentSetExternalReferenceCode, Pagination.of(1, 10));

		Assert.assertEquals(0, page.getTotalCount());
	}

	private void _testGetSiteFragmentSetResourceFoldersPageFragmentSetNonexistentProblemException()
		throws Exception {

		try {
			resourceFolderResource.getSiteFragmentSetResourceFoldersPage(
				testGroup.getExternalReferenceCode(),
				RandomTestUtil.randomString(), Pagination.of(1, 10));

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("NOT_FOUND", problem.getStatus());
		}
	}

	private void _testGetSiteFragmentSetResourceFoldersPageNoPermissionProblemException()
		throws Exception {

		String fragmentSetExternalReferenceCode = _addFragmentCollection(
			testGroup.getGroupId());

		FragmentSet fragmentSet = _toFragmentSet(
			_addFragmentCollection(testGroup.getGroupId()));

		ResourceFolder resourceFolder = _randomResourceFolder(fragmentSet);

		resourceFolderResource.postSiteFragmentSetResourceFolder(
			testGroup.getExternalReferenceCode(),
			fragmentSetExternalReferenceCode, resourceFolder);

		Page<ResourceFolder> page =
			_resourceFolderResource.getSiteFragmentSetResourceFoldersPage(
				testGroup.getExternalReferenceCode(),
				fragmentSetExternalReferenceCode, Pagination.of(1, 10));

		Assert.assertEquals(0, page.getTotalCount());
	}

	private void _testGetSiteResourceFolderNoPermissionProblemException()
		throws Exception {

		ResourceFolder resourceFolder =
			resourceFolderResource.postSiteResourceFolder(
				testGroup.getExternalReferenceCode(), randomResourceFolder());

		try {
			_resourceFolderResource.getSiteResourceFolder(
				testGroup.getExternalReferenceCode(),
				resourceFolder.getExternalReferenceCode());

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("NOT_FOUND", problem.getStatus());
		}
	}

	private void _testGetSiteResourceFolderResourceFolderNonexistentProblemException()
		throws Exception {

		try {
			resourceFolderResource.getSiteResourceFolder(
				testGroup.getExternalReferenceCode(),
				RandomTestUtil.randomString());

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("NOT_FOUND", problem.getStatus());
		}
	}

	private void _testGetSiteResourceFolderResourceFoldersPage()
		throws Exception {

		String fragmentSetExternalReferenceCode = _addFragmentCollection(
			testGroup.getGroupId());

		ResourceFolder resourceFolder = _postSiteResourceFolder(
			fragmentSetExternalReferenceCode);

		ResourceFolder childResourceFolder = _postSiteResourceFolder(
			resourceFolder);

		_postSiteResourceFolder(childResourceFolder);

		Page<ResourceFolder> page =
			resourceFolderResource.getSiteResourceFolderResourceFoldersPage(
				testGroup.getExternalReferenceCode(),
				resourceFolder.getExternalReferenceCode(),
				Pagination.of(1, 10));

		assertContains(
			childResourceFolder, (List<ResourceFolder>)page.getItems());
		Assert.assertEquals(1, page.getTotalCount());
	}

	private void _testGetSiteResourceFolderResourceFoldersPageEmpty()
		throws Exception {

		ResourceFolder resourceFolder = _postSiteResourceFolder(
			_addFragmentCollection(testGroup.getGroupId()));

		Page<ResourceFolder> page =
			resourceFolderResource.getSiteResourceFolderResourceFoldersPage(
				testGroup.getExternalReferenceCode(),
				resourceFolder.getExternalReferenceCode(),
				Pagination.of(1, 10));

		Assert.assertEquals(0, page.getTotalCount());
	}

	private void _testGetSiteResourceFolderResourceFoldersPageNoPermissionProblemException()
		throws Exception {

		ResourceFolder resourceFolder = _postSiteResourceFolder(
			_addFragmentCollection(testGroup.getGroupId()));

		try {
			_resourceFolderResource.getSiteResourceFolderResourceFoldersPage(
				testGroup.getExternalReferenceCode(),
				resourceFolder.getExternalReferenceCode(),
				Pagination.of(1, 10));

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("NOT_FOUND", problem.getStatus());
		}
	}

	private void _testGetSiteResourceFolderResourceFoldersPageResourceFolderNonexistentProblemException()
		throws Exception {

		try {
			resourceFolderResource.getSiteResourceFolderResourceFoldersPage(
				testGroup.getExternalReferenceCode(),
				RandomTestUtil.randomString(), Pagination.of(1, 10));

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("NOT_FOUND", problem.getStatus());
		}
	}

	private void _testGetSiteResourceFoldersPage() throws Exception {
		ResourceFolder resourceFolder1 = _postSiteResourceFolder(
			_addFragmentCollection(testGroup.getGroupId()));

		ResourceFolder childResourceFolder1 = _postSiteResourceFolder(
			resourceFolder1);

		ResourceFolder resourceFolder2 = _postSiteResourceFolder(
			_addFragmentCollection(testGroup.getGroupId()));

		ResourceFolder childResourceFolder2 = _postSiteResourceFolder(
			resourceFolder2);

		Page<ResourceFolder> page =
			resourceFolderResource.getSiteResourceFoldersPage(
				testGroup.getExternalReferenceCode(), null,
				Pagination.of(1, 1));

		long totalCount = page.getTotalCount();

		page = resourceFolderResource.getSiteResourceFoldersPage(
			testGroup.getExternalReferenceCode(), null,
			Pagination.of(1, (int)totalCount));

		List<ResourceFolder> resourceFolders =
			(List<ResourceFolder>)page.getItems();

		assertContains(childResourceFolder1, resourceFolders);
		assertContains(childResourceFolder2, resourceFolders);
		assertContains(resourceFolder1, resourceFolders);
		assertContains(resourceFolder2, resourceFolders);
	}

	private void _testGetSiteResourceFoldersPageNoPermissionProblemException()
		throws Exception {

		resourceFolderResource.postSiteResourceFolder(
			testGroup.getExternalReferenceCode(), randomResourceFolder());

		Page<ResourceFolder> page =
			_resourceFolderResource.getSiteResourceFoldersPage(
				testGroup.getExternalReferenceCode(), null,
				Pagination.of(1, 10));

		Assert.assertEquals(0, page.getTotalCount());
	}

	private void _testGetSiteResourceFoldersPagePortletFolder()
		throws Exception {

		ResourceFolder resourceFolder = _postSiteResourceFolder(
			_addFragmentCollection(testGroup.getGroupId()));

		Page<ResourceFolder> page =
			resourceFolderResource.getSiteResourceFoldersPage(
				testGroup.getExternalReferenceCode(), null,
				Pagination.of(1, 1));

		assertContains(resourceFolder, (List<ResourceFolder>)page.getItems());

		long totalCount = page.getTotalCount();

		_addPortletFolder();

		page = resourceFolderResource.getSiteResourceFoldersPage(
			testGroup.getExternalReferenceCode(), null,
			Pagination.of(1, (int)totalCount));

		assertContains(resourceFolder, (List<ResourceFolder>)page.getItems());
		Assert.assertEquals(totalCount, page.getTotalCount());
	}

	private void _testPostSiteFragmentSetResourceFolderNoPermissionProblemException()
		throws Exception {

		String fragmentSetExternalReferenceCode = _addFragmentCollection(
			testGroup.getGroupId());

		ResourceFolder resourceFolder = new ResourceFolder();

		resourceFolder.setExternalReferenceCode(RandomTestUtil.randomString());
		resourceFolder.setName(RandomTestUtil.randomString());

		try {
			_resourceFolderResource.postSiteFragmentSetResourceFolder(
				testGroup.getExternalReferenceCode(),
				fragmentSetExternalReferenceCode, resourceFolder);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("FORBIDDEN", problem.getStatus());
		}
	}

	private void _testPostSiteResourceFolder() throws Exception {
		String fragmentSetExternalReferenceCode = _addFragmentCollection(
			testGroup.getGroupId());

		ResourceFolder parentResourceFolder = _randomResourceFolder(
			_toFragmentSet(fragmentSetExternalReferenceCode));

		ResourceFolder postParentResourceFolder =
			resourceFolderResource.postSiteResourceFolder(
				testGroup.getExternalReferenceCode(), parentResourceFolder);

		ResourceFolder resourceFolder = _randomResourceFolder(
			parentResourceFolder);

		ResourceFolder postResourceFolder =
			resourceFolderResource.postSiteResourceFolder(
				testGroup.getExternalReferenceCode(), resourceFolder);

		ResourceFolder getResourceFolder =
			resourceFolderResource.getSiteResourceFolder(
				testGroup.getExternalReferenceCode(),
				postResourceFolder.getExternalReferenceCode());

		ResourceFolder getParentResourceFolder =
			getResourceFolder.getParentResourceFolder();

		FragmentSet getFragmentSet = getResourceFolder.getFragmentSet();

		Assert.assertEquals(
			fragmentSetExternalReferenceCode,
			getFragmentSet.getExternalReferenceCode());

		Assert.assertEquals(
			postParentResourceFolder.getExternalReferenceCode(),
			getParentResourceFolder.getExternalReferenceCode());
	}

	private void _testPostSiteResourceFolderBatch() throws Exception {
		String fragmentSetExternalReferenceCode = _addFragmentCollection(
			testGroup.getGroupId());

		ResourceFolder parentResourceFolder = _randomResourceFolder(
			_toFragmentSet(fragmentSetExternalReferenceCode));

		ResourceFolder postParentResourceFolder =
			resourceFolderResource.postSiteResourceFolder(
				testGroup.getExternalReferenceCode(), parentResourceFolder);

		ResourceFolder resourceFolder = _randomResourceFolder(
			parentResourceFolder);

		ResourceFolder postResourceFolder =
			resourceFolderResource.postSiteResourceFolder(
				testGroup.getExternalReferenceCode(), resourceFolder);

		try (SafeCloseable safeCloseable =
				LazyReferencingTestUtil.setLazyReferencingWithSafeCloseable(
					true)) {

			waitForFinish(
				"COMPLETED",
				HTTPTestUtil.invokeToJSONObject(
					_exportResourceFoldersToJSON(
						testGroup.getExternalReferenceCode()),
					"headless-admin-fragment/v1.0/sites/" +
						irrelevantGroup.getExternalReferenceCode() +
							"/resource-folders/batch?createStrategy=INSERT",
					Http.Method.POST));
		}

		ResourceFolder importedResourceFolder =
			resourceFolderResource.getSiteResourceFolder(
				irrelevantGroup.getExternalReferenceCode(),
				postResourceFolder.getExternalReferenceCode());

		ResourceFolder importedParentResourceFolder =
			importedResourceFolder.getParentResourceFolder();

		Assert.assertEquals(
			postParentResourceFolder.getExternalReferenceCode(),
			importedParentResourceFolder.getExternalReferenceCode());

		FragmentSet importedFragmentSet =
			importedResourceFolder.getFragmentSet();

		Assert.assertEquals(
			fragmentSetExternalReferenceCode,
			importedFragmentSet.getExternalReferenceCode());

		Assert.assertNotNull(
			_fragmentCollectionLocalService.
				fetchFragmentCollectionByExternalReferenceCode(
					fragmentSetExternalReferenceCode,
					irrelevantGroup.getGroupId()));
	}

	private void _testPostSiteResourceFolderDuplicateExternalReferenceCodeProblemException()
		throws Exception {

		String fragmentSetExternalReferenceCode = _addFragmentCollection(
			testGroup.getGroupId());

		ResourceFolder postResourceFolder =
			resourceFolderResource.postSiteResourceFolder(
				testGroup.getExternalReferenceCode(),
				_randomResourceFolder(
					_toFragmentSet(fragmentSetExternalReferenceCode)));

		ResourceFolder resourceFolder = _randomResourceFolder(
			_toFragmentSet(fragmentSetExternalReferenceCode));

		resourceFolder.setExternalReferenceCode(
			postResourceFolder.getExternalReferenceCode());

		try {
			resourceFolderResource.postSiteResourceFolder(
				testGroup.getExternalReferenceCode(), resourceFolder);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("BAD_REQUEST", problem.getStatus());
			Assert.assertEquals(
				LanguageUtil.get(
					LocaleUtil.getDefault(),
					"this-external-reference-code-is-already-in-use"),
				problem.getTitle());
		}
	}

	private void _testPostSiteResourceFolderFragmentSetExternalReferenceCodeNullProblemException()
		throws Exception {

		ResourceFolder resourceFolder = _randomResourceFolder(
			new FragmentSet());

		try {
			resourceFolderResource.postSiteResourceFolder(
				testGroup.getExternalReferenceCode(), resourceFolder);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("BAD_REQUEST", problem.getStatus());
			Assert.assertEquals(
				LanguageUtil.get(
					LocaleUtil.getDefault(),
					"a-fragment-set-external-reference-code-is-required-to-" +
						"create-a-new-resource-folder"),
				problem.getTitle());
		}
	}

	private void _testPostSiteResourceFolderFragmentSetNonexistingProblemException()
		throws Exception {

		String fragmentSetExternalReferenceCode = RandomTestUtil.randomString();

		ResourceFolder resourceFolder = _randomResourceFolder(
			_toFragmentSet(fragmentSetExternalReferenceCode));

		try {
			resourceFolderResource.postSiteResourceFolder(
				testGroup.getExternalReferenceCode(), resourceFolder);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("BAD_REQUEST", problem.getStatus());
			Assert.assertEquals(
				LanguageUtil.format(
					LocaleUtil.getDefault(),
					"no-fragment-set-was-found-with-external-reference-code-x",
					fragmentSetExternalReferenceCode),
				problem.getTitle());
		}
	}

	private void _testPostSiteResourceFolderNoPermissionProblemException()
		throws Exception {

		try {
			_resourceFolderResource.postSiteResourceFolder(
				testGroup.getExternalReferenceCode(), randomResourceFolder());

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("FORBIDDEN", problem.getStatus());
		}
	}

	private void _testPutSiteResourceFolder() throws Exception {
		String fragmentSetExternalReferenceCode = _addFragmentCollection(
			testGroup.getGroupId());

		ResourceFolder originalResourceFolder = _randomResourceFolder(
			_toFragmentSet(fragmentSetExternalReferenceCode));

		ResourceFolder putResourceFolder =
			resourceFolderResource.putSiteResourceFolder(
				testGroup.getExternalReferenceCode(),
				originalResourceFolder.getExternalReferenceCode(),
				originalResourceFolder);

		assertEquals(originalResourceFolder, putResourceFolder);
		assertValid(putResourceFolder);

		ResourceFolder updatedResourceFolder = _randomResourceFolder(
			_postSiteResourceFolder(
				_addFragmentCollection(testGroup.getGroupId())));

		Assert.assertNotNull(updatedResourceFolder.getParentResourceFolder());

		putResourceFolder = resourceFolderResource.putSiteResourceFolder(
			testGroup.getExternalReferenceCode(),
			originalResourceFolder.getExternalReferenceCode(),
			updatedResourceFolder);

		Assert.assertEquals(
			originalResourceFolder.getExternalReferenceCode(),
			putResourceFolder.getExternalReferenceCode());
		Assert.assertEquals(
			updatedResourceFolder.getName(), putResourceFolder.getName());
		Assert.assertNull(putResourceFolder.getParentResourceFolder());

		FragmentSet fragmentSet = putResourceFolder.getFragmentSet();

		Assert.assertEquals(
			fragmentSetExternalReferenceCode,
			fragmentSet.getExternalReferenceCode());

		ResourceFolder getResourceFolder =
			resourceFolderResource.getSiteResourceFolder(
				testGroup.getExternalReferenceCode(),
				originalResourceFolder.getExternalReferenceCode());

		Assert.assertEquals(
			updatedResourceFolder.getName(), getResourceFolder.getName());

		ResourceFolder parentResourceFolder = _postSiteResourceFolder(
			fragmentSetExternalReferenceCode);

		ResourceFolder childResourceFolder = _postSiteResourceFolder(
			parentResourceFolder);

		putResourceFolder = resourceFolderResource.putSiteResourceFolder(
			testGroup.getExternalReferenceCode(),
			childResourceFolder.getExternalReferenceCode(),
			_randomResourceFolder(
				_postSiteResourceFolder(
					_addFragmentCollection(testGroup.getGroupId()))));

		ResourceFolder putParentResourceFolder =
			putResourceFolder.getParentResourceFolder();

		Assert.assertEquals(
			parentResourceFolder.getExternalReferenceCode(),
			putParentResourceFolder.getExternalReferenceCode());
	}

	private void _testPutSiteResourceFolderNoPermissionProblemException()
		throws Exception {

		ResourceFolder resourceFolder =
			resourceFolderResource.postSiteResourceFolder(
				testGroup.getExternalReferenceCode(), randomResourceFolder());

		resourceFolder.setName(RandomTestUtil.randomString());

		try {
			_resourceFolderResource.putSiteResourceFolder(
				testGroup.getExternalReferenceCode(),
				resourceFolder.getExternalReferenceCode(), resourceFolder);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("FORBIDDEN", problem.getStatus());
		}
	}

	private FragmentSet _toFragmentSet(String externalReferenceCode) {
		FragmentSet fragmentSet = new FragmentSet();

		fragmentSet.setExternalReferenceCode(externalReferenceCode);

		return fragmentSet;
	}

	private ResourceFolder _toResourceFolder(String externalReferenceCode) {
		ResourceFolder resourceFolder = new ResourceFolder();

		resourceFolder.setExternalReferenceCode(externalReferenceCode);

		return resourceFolder;
	}

	private JSONObject _waitForExportFinish(JSONObject jsonObject)
		throws Exception {

		String externalReferenceCode = jsonObject.getString(
			"externalReferenceCode");

		long time = System.currentTimeMillis() + _EXPORT_TIMEOUT;

		while (true) {
			jsonObject = HTTPTestUtil.invokeToJSONObject(
				null,
				"headless-batch-engine/v1.0/export-task" +
					"/by-external-reference-code/" + externalReferenceCode,
				Http.Method.GET);

			String executeStatus = jsonObject.getString("executeStatus");

			if (StringUtil.equals(executeStatus, "COMPLETED") ||
				StringUtil.equals(executeStatus, "FAILED")) {

				Assert.assertEquals("COMPLETED", executeStatus);

				return jsonObject;
			}

			if (System.currentTimeMillis() > time) {
				throw new AssertionError(
					StringBundler.concat(
						"Export task ", externalReferenceCode,
						" did not finish within ", _EXPORT_TIMEOUT, " ms"));
			}

			Thread.sleep(_EXPORT_POLL_INTERVAL);
		}
	}

	private static final long _EXPORT_POLL_INTERVAL = 500;

	private static final long _EXPORT_TIMEOUT = 60000;

	@Inject
	private FragmentCollectionLocalService _fragmentCollectionLocalService;

	private String _fragmentSetExternalReferenceCode;

	@Inject
	private GroupLocalService _groupLocalService;

	private ResourceFolderResource _resourceFolderResource;

}