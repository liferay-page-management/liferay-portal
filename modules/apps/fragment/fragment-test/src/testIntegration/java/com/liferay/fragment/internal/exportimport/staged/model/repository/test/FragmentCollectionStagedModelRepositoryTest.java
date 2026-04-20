/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.internal.exportimport.staged.model.repository.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.exportimport.staged.model.repository.StagedModelRepository;
import com.liferay.exportimport.staged.model.repository.StagedModelRepositoryRegistryUtil;
import com.liferay.fragment.exception.FragmentCollectionNameException;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.service.FragmentCollectionLocalService;
import com.liferay.fragment.test.util.FragmentTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Petteri Karttunen
 */
@RunWith(Arquillian.class)
public class FragmentCollectionStagedModelRepositoryTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_stagedModelRepository =
			(StagedModelRepository<FragmentCollection>)
				StagedModelRepositoryRegistryUtil.getStagedModelRepository(
					FragmentCollection.class.getName());
	}

	@Test
	public void testUpdateStagedModel() throws Exception {
		FragmentCollection fragmentCollection =
			FragmentTestUtil.addFragmentCollection(
				_group.getGroupId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString());

		String newDescription = RandomTestUtil.randomString();
		String newFragmentCollectionKey = RandomTestUtil.randomString();
		String newName = RandomTestUtil.randomString();

		fragmentCollection.setFragmentCollectionKey(newFragmentCollectionKey);
		fragmentCollection.setName(newName);
		fragmentCollection.setDescription(newDescription);

		_stagedModelRepository.updateStagedModel(null, fragmentCollection);

		FragmentCollection updatedFragmentCollection =
			_fragmentCollectionLocalService.getFragmentCollection(
				fragmentCollection.getFragmentCollectionId());

		Assert.assertEquals(newName, updatedFragmentCollection.getName());
		Assert.assertEquals(
			newDescription, updatedFragmentCollection.getDescription());
		Assert.assertEquals(
			newFragmentCollectionKey,
			updatedFragmentCollection.getFragmentCollectionKey());
	}

	@Test(expected = FragmentCollectionNameException.class)
	public void testUpdateStagedModelWithNullName() throws Exception {
		FragmentCollection fragmentCollection =
			FragmentTestUtil.addFragmentCollection(
				_group.getGroupId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString());

		fragmentCollection.setName(null);

		_stagedModelRepository.updateStagedModel(null, fragmentCollection);
	}

	@Inject
	private FragmentCollectionLocalService _fragmentCollectionLocalService;

	@DeleteAfterTestRun
	private Group _group;

	private StagedModelRepository<FragmentCollection> _stagedModelRepository;

}