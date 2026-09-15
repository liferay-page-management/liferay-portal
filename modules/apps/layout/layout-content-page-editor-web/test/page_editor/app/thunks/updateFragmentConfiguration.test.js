/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import updateFragmentEntryLinkConfiguration from '../../../../src/main/resources/META-INF/resources/page_editor/app/actions/updateFragmentEntryLinkConfiguration';
import {FREEMARKER_FRAGMENT_ENTRY_PROCESSOR} from '../../../../src/main/resources/META-INF/resources/page_editor/app/config/constants/freemarkerFragmentEntryProcessor';
import FragmentService from '../../../../src/main/resources/META-INF/resources/page_editor/app/services/FragmentService';
import updateFragmentConfiguration from '../../../../src/main/resources/META-INF/resources/page_editor/app/thunks/updateFragmentConfiguration';

jest.mock(
	'../../../../src/main/resources/META-INF/resources/page_editor/app/actions/updateFragmentEntryLinkConfiguration',
	() => jest.fn()
);

jest.mock(
	'../../../../src/main/resources/META-INF/resources/page_editor/app/services/FragmentService',
	() => ({
		updateConfigurationValues: jest.fn(() =>
			Promise.resolve({
				fragmentEntryLink: {fragmentEntryLinkId: '1'},
				layoutData: {items: {}},
			})
		),
	})
);

const DISPLAY_PAGE_PREVIEW_ITEM = {
	data: {
		className: 'com.liferay.journal.model.JournalArticle',
		classPK: '42',
	},
	label: 'Article',
};

const FRAGMENT_ENTRY_LINK = {
	editableValues: {[FREEMARKER_FRAGMENT_ENTRY_PROCESSOR]: {}},
	fragmentEntryLinkId: '1',
};

describe('updateFragmentConfiguration', () => {
	afterEach(() => {
		FragmentService.updateConfigurationValues.mockClear();
		updateFragmentEntryLinkConfiguration.mockClear();
	});

	const runThunk = (displayPagePreviewItem) =>
		updateFragmentConfiguration({
			configurationValues: {headingLevel: 'h2'},
			displayPagePreviewItem,
			fragmentEntryLink: FRAGMENT_ENTRY_LINK,
		})(
			() => {},
			() => ({languageId: 'en_US', segmentsExperienceId: '0'})
		);

	it('sends the display page preview item with the configuration values', async () => {
		await runThunk(DISPLAY_PAGE_PREVIEW_ITEM);

		expect(FragmentService.updateConfigurationValues).toHaveBeenCalledWith(
			expect.objectContaining({
				displayPagePreviewItem: DISPLAY_PAGE_PREVIEW_ITEM,
				editableValues: {
					[FREEMARKER_FRAGMENT_ENTRY_PROCESSOR]: {headingLevel: 'h2'},
				},
				fragmentEntryLinkId: '1',
				languageId: 'en_US',
				segmentsExperienceId: '0',
			})
		);
	});

	it('keeps the display page preview item in the dispatched action so undo can replay it', async () => {
		await runThunk(DISPLAY_PAGE_PREVIEW_ITEM);

		expect(updateFragmentEntryLinkConfiguration).toHaveBeenCalledWith({
			displayPagePreviewItem: DISPLAY_PAGE_PREVIEW_ITEM,
			fragmentEntryLink: {fragmentEntryLinkId: '1'},
			fragmentEntryLinkId: '1',
			layoutData: {items: {}},
		});
	});

	it('works without a display page preview item', async () => {
		await runThunk(null);

		expect(FragmentService.updateConfigurationValues).toHaveBeenCalledWith(
			expect.objectContaining({displayPagePreviewItem: null})
		);
	});
});
