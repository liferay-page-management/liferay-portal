/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.exportimport.kernel.service.StagingLocalService;
import com.liferay.headless.admin.site.client.dto.v1_0.DisplayPageTemplateFolder;
import com.liferay.headless.admin.site.client.problem.Problem;
import com.liferay.layout.page.template.service.LayoutPageTemplateCollectionService;
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Rubén Pulido
 * @author Bárbara Cabrera
 */
@FeatureFlag("LPD-35443")
@RunWith(Arquillian.class)
public class DisplayPageTemplateFolderResourceTest
	extends BaseDisplayPageTemplateFolderResourceTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Override
	@Test
	public void testDeleteSiteDisplayPageTemplateFolder() throws Exception {
		DisplayPageTemplateFolder postDisplayPageTemplateFolder =
			testGetSiteDisplayPageTemplateFoldersPage_addDisplayPageTemplateFolder(
				testGroup.getExternalReferenceCode(),
				randomDisplayPageTemplateFolder());

		displayPageTemplateFolderResource.deleteSiteDisplayPageTemplateFolder(
			testGroup.getExternalReferenceCode(),
			postDisplayPageTemplateFolder.getExternalReferenceCode());

		Assert.assertNull(
			_layoutPageTemplateCollectionService.
				fetchLayoutPageTemplateCollection(
					postDisplayPageTemplateFolder.getExternalReferenceCode(),
					testGroup.getGroupId()));

		DisplayPageTemplateFolder liveGroupDisplayPageTemplateFolder =
			testGetSiteDisplayPageTemplateFoldersPage_addDisplayPageTemplateFolder(
				testGroup.getExternalReferenceCode(),
				randomDisplayPageTemplateFolder());

		_enableLocalStaging();

		_assertProblemException(
			"BAD_REQUEST", null,
			() ->
				displayPageTemplateFolderResource.
					deleteSiteDisplayPageTemplateFolder(
						testGroup.getExternalReferenceCode(),
						liveGroupDisplayPageTemplateFolder.
							getExternalReferenceCode()));
	}

	@Override
	@Test
	public void testGetSiteDisplayPageTemplateFolder() throws Exception {
		DisplayPageTemplateFolder postDisplayPageTemplateFolder =
			testGetSiteDisplayPageTemplateFoldersPage_addDisplayPageTemplateFolder(
				testGroup.getExternalReferenceCode(),
				randomDisplayPageTemplateFolder());

		DisplayPageTemplateFolder getDisplayPageTemplateFolder =
			displayPageTemplateFolderResource.getSiteDisplayPageTemplateFolder(
				testGroup.getExternalReferenceCode(),
				postDisplayPageTemplateFolder.getExternalReferenceCode());

		assertEquals(
			postDisplayPageTemplateFolder, getDisplayPageTemplateFolder);
		assertValid(getDisplayPageTemplateFolder);

		_enableLocalStaging();

		assertEquals(
			postDisplayPageTemplateFolder,
			displayPageTemplateFolderResource.getSiteDisplayPageTemplateFolder(
				testGroup.getExternalReferenceCode(),
				postDisplayPageTemplateFolder.getExternalReferenceCode()));
	}

	@Ignore
	@Override
	@Test
	public void testGetSiteDisplayPageTemplateFolderPermissionsPage()
		throws Exception {

		super.testGetSiteDisplayPageTemplateFolderPermissionsPage();
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetSiteDisplayPageTemplateFolder() throws Exception {
		super.testGraphQLGetSiteDisplayPageTemplateFolder();
	}

	@Override
	@Test
	public void testPatchSiteDisplayPageTemplateFolder() throws Exception {
		DisplayPageTemplateFolder parentDisplayPageTemplateFolder =
			testPostSiteDisplayPageTemplateFolder_addDisplayPageTemplateFolder(
				randomDisplayPageTemplateFolder());

		DisplayPageTemplateFolder displayPageTemplateFolder =
			testPostSiteDisplayPageTemplateFolder_addDisplayPageTemplateFolder(
				randomDisplayPageTemplateFolder());

		Assert.assertNull(
			displayPageTemplateFolder.
				getParentDisplayPageTemplateFolderExternalReferenceCode());

		_testPatchSiteDisplayPageTemplateFolder(
			displayPageTemplateFolder.getExternalReferenceCode(),
			parentDisplayPageTemplateFolder.getExternalReferenceCode());

		_testPatchSiteDisplayPageTemplateFolder(
			displayPageTemplateFolder.getExternalReferenceCode(), null);
		_testPatchSiteDisplayPageTemplateFolder(
			displayPageTemplateFolder.getExternalReferenceCode(),
			StringPool.BLANK);

		_assertProblemException(
			"NOT_FOUND", null,
			() ->
				displayPageTemplateFolderResource.
					patchSiteDisplayPageTemplateFolder(
						testGroup.getExternalReferenceCode(),
						RandomTestUtil.randomString(),
						randomDisplayPageTemplateFolder()));

		_enableLocalStaging();

		_assertProblemException(
			"BAD_REQUEST", null,
			() ->
				displayPageTemplateFolderResource.
					patchSiteDisplayPageTemplateFolder(
						testGroup.getExternalReferenceCode(),
						displayPageTemplateFolder.getExternalReferenceCode(),
						displayPageTemplateFolder));
	}

	@Override
	@Test
	public void testPostSiteDisplayPageTemplateFolder() throws Exception {
		super.testPostSiteDisplayPageTemplateFolder();

		_testPostSiteDisplayPageTemplateFolderWithExistingParentExternalReferenceCode();

		DisplayPageTemplateFolder postDisplayPageTemplateFolder =
			testPostSiteDisplayPageTemplateFolder_addDisplayPageTemplateFolder(
				randomDisplayPageTemplateFolder());

		_testPostSiteDisplayPageTemplateFolderWithInvalidKey(
			postDisplayPageTemplateFolder.getKey(),
			StringBundler.concat(
				"Duplicate display page template folder for group ",
				testGroup.getGroupId(), " with key ",
				postDisplayPageTemplateFolder.getKey()));

		String key =
			RandomTestUtil.randomString() + StringPool.AMPERSAND +
				RandomTestUtil.randomString();

		_testPostSiteDisplayPageTemplateFolderWithInvalidKey(
			key,
			StringBundler.concat(
				"Key ", key,
				" must contain only alphanumeric characters, dashes, and ",
				"underscores"));

		key = RandomTestUtil.randomString(80);

		_testPostSiteDisplayPageTemplateFolderWithInvalidKey(
			key,
			StringBundler.concat(
				"Key ", key, " must have fewer than 75 characters"));

		_enableLocalStaging();

		_assertProblemException(
			"BAD_REQUEST", null,
			() ->
				displayPageTemplateFolderResource.
					postSiteDisplayPageTemplateFolder(
						testGroup.getExternalReferenceCode(),
						randomDisplayPageTemplateFolder()));
	}

	@Override
	@Test
	public void testPutSiteDisplayPageTemplateFolder() throws Exception {
		DisplayPageTemplateFolder displayPageTemplateFolder =
			_testPutSiteDisplayPageTemplateFolder(
				randomDisplayPageTemplateFolder(),
				RandomTestUtil.randomString());

		Assert.assertNull(
			displayPageTemplateFolder.
				getParentDisplayPageTemplateFolderExternalReferenceCode());

		DisplayPageTemplateFolder parentDisplayPageTemplateFolder =
			testPostSiteDisplayPageTemplateFolder_addDisplayPageTemplateFolder(
				randomDisplayPageTemplateFolder());

		displayPageTemplateFolder = _testPutSiteDisplayPageTemplateFolder(
			randomDisplayPageTemplateFolder(),
			parentDisplayPageTemplateFolder.getExternalReferenceCode());

		Assert.assertEquals(
			parentDisplayPageTemplateFolder.getExternalReferenceCode(),
			displayPageTemplateFolder.
				getParentDisplayPageTemplateFolderExternalReferenceCode());

		displayPageTemplateFolder = _testPutSiteDisplayPageTemplateFolder(
			displayPageTemplateFolder, StringPool.BLANK);

		Assert.assertNull(
			displayPageTemplateFolder.
				getParentDisplayPageTemplateFolderExternalReferenceCode());

		DisplayPageTemplateFolder liveGroupDisplayPageTemplateFolder =
			_testPutSiteDisplayPageTemplateFolder(
				randomDisplayPageTemplateFolder(),
				RandomTestUtil.randomString());

		_enableLocalStaging();

		_assertProblemException(
			"BAD_REQUEST", null,
			() ->
				displayPageTemplateFolderResource.
					putSiteDisplayPageTemplateFolder(
						testGroup.getExternalReferenceCode(),
						liveGroupDisplayPageTemplateFolder.
							getExternalReferenceCode(),
						parentDisplayPageTemplateFolder));
	}

	@Ignore
	@Override
	@Test
	public void testPutSiteDisplayPageTemplateFolderPermissionsPage()
		throws Exception {

		super.testPutSiteDisplayPageTemplateFolderPermissionsPage();
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"description", "externalReferenceCode", "name"};
	}

	@Override
	protected DisplayPageTemplateFolder randomDisplayPageTemplateFolder()
		throws Exception {

		DisplayPageTemplateFolder displayPageTemplateFolder =
			super.randomDisplayPageTemplateFolder();

		displayPageTemplateFolder.
			setParentDisplayPageTemplateFolderExternalReferenceCode(
				(String)null);

		return displayPageTemplateFolder;
	}

	@Ignore
	@Override
	@Test
	protected DisplayPageTemplateFolder
			testGetSiteDisplayPageTemplateFolderPermissionsPage_addDisplayPageTemplateFolder()
		throws Exception {

		return super.
			testGetSiteDisplayPageTemplateFolderPermissionsPage_addDisplayPageTemplateFolder();
	}

	@Override
	protected DisplayPageTemplateFolder
			testGetSiteDisplayPageTemplateFoldersPage_addDisplayPageTemplateFolder(
				String siteExternalReferenceCode,
				DisplayPageTemplateFolder displayPageTemplateFolder)
		throws Exception {

		return displayPageTemplateFolderResource.
			postSiteDisplayPageTemplateFolder(
				siteExternalReferenceCode, displayPageTemplateFolder);
	}

	@Override
	protected DisplayPageTemplateFolder
			testPostSiteDisplayPageTemplateFolder_addDisplayPageTemplateFolder(
				DisplayPageTemplateFolder displayPageTemplateFolder)
		throws Exception {

		return testGetSiteDisplayPageTemplateFoldersPage_addDisplayPageTemplateFolder(
			testGroup.getExternalReferenceCode(), displayPageTemplateFolder);
	}

	@Ignore
	@Override
	@Test
	protected DisplayPageTemplateFolder
			testPutSiteDisplayPageTemplateFolderPermissionsPage_addDisplayPageTemplateFolder()
		throws Exception {

		return super.
			testPutSiteDisplayPageTemplateFolderPermissionsPage_addDisplayPageTemplateFolder();
	}

	private void _assertProblemException(
			String status, String title,
			UnsafeRunnable<Exception> unsafeRunnable)
		throws Exception {

		try {
			unsafeRunnable.run();

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals(status, problem.getStatus());
			Assert.assertEquals(title, problem.getTitle());
		}
	}

	private void _enableLocalStaging() throws Exception {
		_stagingLocalService.enableLocalStaging(
			TestPropsValues.getUserId(), testGroup, true, false,
			ServiceContextTestUtil.getServiceContext(
				testGroup, TestPropsValues.getUserId()));
	}

	private void _testPatchSiteDisplayPageTemplateFolder(
			String displayPageTemplateFolderExternalReferenceCode,
			String parentDisplayPageTemplateFolderExternalReferenceCode)
		throws Exception {

		DisplayPageTemplateFolder getDisplayPageTemplateFolder =
			displayPageTemplateFolderResource.getSiteDisplayPageTemplateFolder(
				testGroup.getExternalReferenceCode(),
				displayPageTemplateFolderExternalReferenceCode);

		DisplayPageTemplateFolder randomDisplayPageTemplateFolder =
			randomDisplayPageTemplateFolder();

		randomDisplayPageTemplateFolder.setExternalReferenceCode(
			displayPageTemplateFolderExternalReferenceCode);
		randomDisplayPageTemplateFolder.
			setParentDisplayPageTemplateFolderExternalReferenceCode(
				parentDisplayPageTemplateFolderExternalReferenceCode);

		DisplayPageTemplateFolder patchDisplayPageTemplateFolder =
			displayPageTemplateFolderResource.
				patchSiteDisplayPageTemplateFolder(
					testGroup.getExternalReferenceCode(),
					displayPageTemplateFolderExternalReferenceCode,
					randomDisplayPageTemplateFolder);

		assertEquals(
			randomDisplayPageTemplateFolder, patchDisplayPageTemplateFolder);
		assertValid(patchDisplayPageTemplateFolder);

		if (parentDisplayPageTemplateFolderExternalReferenceCode == null) {
			parentDisplayPageTemplateFolderExternalReferenceCode =
				getDisplayPageTemplateFolder.
					getParentDisplayPageTemplateFolderExternalReferenceCode();
		}

		if (Validator.isBlank(
				parentDisplayPageTemplateFolderExternalReferenceCode)) {

			Assert.assertNull(
				patchDisplayPageTemplateFolder.
					getParentDisplayPageTemplateFolderExternalReferenceCode());
		}
		else {
			Assert.assertEquals(
				parentDisplayPageTemplateFolderExternalReferenceCode,
				patchDisplayPageTemplateFolder.
					getParentDisplayPageTemplateFolderExternalReferenceCode());
		}
	}

	private void _testPostSiteDisplayPageTemplateFolderWithExistingParentExternalReferenceCode()
		throws Exception {

		DisplayPageTemplateFolder displayPageTemplateFolder =
			randomDisplayPageTemplateFolder();

		displayPageTemplateFolder.setKey(StringPool.BLANK);

		DisplayPageTemplateFolder parentDisplayPageTemplateFolder =
			testPostSiteDisplayPageTemplateFolder_addDisplayPageTemplateFolder(
				displayPageTemplateFolder);

		Assert.assertNotNull(
			Validator.isNotNull(parentDisplayPageTemplateFolder.getKey()));

		DisplayPageTemplateFolder randomDisplayPageTemplateFolder =
			randomDisplayPageTemplateFolder();

		randomDisplayPageTemplateFolder.
			setParentDisplayPageTemplateFolderExternalReferenceCode(
				parentDisplayPageTemplateFolder.getExternalReferenceCode());

		DisplayPageTemplateFolder postDisplayPageTemplateFolder =
			testPostSiteDisplayPageTemplateFolder_addDisplayPageTemplateFolder(
				randomDisplayPageTemplateFolder);

		assertEquals(
			randomDisplayPageTemplateFolder, postDisplayPageTemplateFolder);
		Assert.assertEquals(
			randomDisplayPageTemplateFolder.getKey(),
			postDisplayPageTemplateFolder.getKey());
		Assert.assertEquals(
			randomDisplayPageTemplateFolder.
				getParentDisplayPageTemplateFolderExternalReferenceCode(),
			postDisplayPageTemplateFolder.
				getParentDisplayPageTemplateFolderExternalReferenceCode());
		assertValid(postDisplayPageTemplateFolder);
		Assert.assertNotNull(
			postDisplayPageTemplateFolder.
				getParentDisplayPageTemplateFolderExternalReferenceCode());
	}

	private void _testPostSiteDisplayPageTemplateFolderWithInvalidKey(
			String key, String title)
		throws Exception {

		DisplayPageTemplateFolder displayPageTemplateFolder =
			randomDisplayPageTemplateFolder();

		displayPageTemplateFolder.setKey(key);

		_assertProblemException(
			"CONFLICT", title,
			() ->
				displayPageTemplateFolderResource.
					postSiteDisplayPageTemplateFolder(
						testGroup.getExternalReferenceCode(),
						displayPageTemplateFolder));
	}

	private DisplayPageTemplateFolder _testPutSiteDisplayPageTemplateFolder(
			DisplayPageTemplateFolder displayPageTemplateFolder,
			String parentDisplayPageTemplateFolderExternalReferenceCode)
		throws Exception {

		displayPageTemplateFolder.
			setParentDisplayPageTemplateFolderExternalReferenceCode(
				parentDisplayPageTemplateFolderExternalReferenceCode);

		DisplayPageTemplateFolder putDisplayPageTemplateFolder =
			displayPageTemplateFolderResource.putSiteDisplayPageTemplateFolder(
				testGroup.getExternalReferenceCode(),
				displayPageTemplateFolder.getExternalReferenceCode(),
				displayPageTemplateFolder);

		assertEquals(displayPageTemplateFolder, putDisplayPageTemplateFolder);
		assertValid(putDisplayPageTemplateFolder);

		return putDisplayPageTemplateFolder;
	}

	@Inject
	private LayoutPageTemplateCollectionService
		_layoutPageTemplateCollectionService;

	@Inject
	private StagingLocalService _stagingLocalService;

}