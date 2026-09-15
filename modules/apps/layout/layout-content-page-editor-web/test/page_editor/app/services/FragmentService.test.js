/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import FragmentService from '../../../../src/main/resources/META-INF/resources/page_editor/app/services/FragmentService';
import draftServiceFetch from '../../../../src/main/resources/META-INF/resources/page_editor/app/services/draftServiceFetch';

jest.mock(
	'../../../../src/main/resources/META-INF/resources/page_editor/app/services/draftServiceFetch',
	() => jest.fn(() => Promise.resolve({}))
);

const REQUEST = {
	editableValues: {},
	fragmentEntryLinkId: '1',
	languageId: 'en_US',
	onNetworkStatus: () => {},
	segmentsExperienceId: '0',
};

const getBody = () => draftServiceFetch.mock.calls[0][1].body;

describe('FragmentService', () => {
	afterEach(() => {
		draftServiceFetch.mockClear();
	});

	it('sends the display page preview item identified by class PK on a configuration update', () => {
		FragmentService.updateConfigurationValues({
			...REQUEST,
			displayPagePreviewItem: {
				data: {
					className: 'com.liferay.journal.model.JournalArticle',
					classPK: '42',
				},
				label: 'Article',
			},
		});

		expect(getBody()).toEqual({
			editableValues: '{}',
			fragmentEntryLinkId: '1',
			itemClassName: 'com.liferay.journal.model.JournalArticle',
			itemClassPK: '42',
			languageId: 'en_US',
			segmentsExperienceId: '0',
		});
	});

	it('sends the display page preview item identified by external reference code on a configuration update', () => {
		FragmentService.updateConfigurationValues({
			...REQUEST,
			displayPagePreviewItem: {
				data: {
					className: 'com.liferay.object.model.ObjectEntry',
					externalReferenceCode: 'ERC',
				},
				label: 'Entry',
			},
		});

		expect(getBody()).toEqual(
			expect.objectContaining({
				itemClassName: 'com.liferay.object.model.ObjectEntry',
				itemExternalReferenceCode: 'ERC',
			})
		);
		expect(getBody()).not.toHaveProperty('itemClassPK');
	});

	it('omits the item parameters from a configuration update when there is no display page preview item', () => {
		FragmentService.updateConfigurationValues({
			...REQUEST,
			displayPagePreviewItem: null,
		});

		expect(getBody()).toEqual({
			editableValues: '{}',
			fragmentEntryLinkId: '1',
			languageId: 'en_US',
			segmentsExperienceId: '0',
		});
	});
});
