/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import fetchMock from 'fetch-mock';

import AnalyticsClient from '../../src/analytics';
import {
	getExpectedViewDuration,
	viewDurationByCharacters,
	viewDurationByWords,
} from '../../src/plugins/read';
import {FLUSH_INTERVAL} from '../../src/utils/constants';

const ENGLISH_TEXT =
	'But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete account of the system, and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness. No one rejects, dislikes, or avoids pleasure itself, because it is pleasure, but because those who do not know how to pursue pleasure rationally encounter consequences that are extremely painful. Nor again is there anyone who loves or pursues or desires to obtain pain of itself, because it is pain, but because occasionally circumstances occur in which toil and pain can procure him some great pleasure. To take a trivial example, which of us ever undertakes laborious physical exercise, except to obtain some advantage from it? But who has any right to find fault with a man who chooses to enjoy a pleasure that has no annoying consequences, or one who avoids a pain that produces no resultant pleasure?';
const LOGOGRAPHIC_TEXT =
	'范例文字，请取代此段落文字。此段落文字为范例文字内容，请务必取代。范例文字，请取代此段落文字。此段落文字为范例文字内容，请务必取代。范例文字，请取代此段落文字。此段落文字为范例文字内容，请务必取代。范例文字，请取代此段落文字。此段落文字为范例文字内容，请务必取代。';

const PAGE_HEIGHT = 1000;
const SCROLL_HEIGHT = 2000;

const createMainContent = (isPhonological = true) => {
	const mainContent = document.createElement('div');
	mainContent.id = 'main-content';
	mainContent.innerText = isPhonological ? ENGLISH_TEXT : LOGOGRAPHIC_TEXT;
	document.body.appendChild(mainContent);

	return mainContent;
};

const createMetaTag = () => {
	const meta = document.createElement('meta');
	meta.name = 'data-analytics-readable-content';
	meta.content = 'true';
	document.getElementsByTagName('head')[0].appendChild(meta);

	return meta;
};

describe('Read Plugin', () => {
	let Analytics;

	beforeAll(() => {
		jest.useFakeTimers();

		createMetaTag();
	});

	beforeEach(() => {

		// Force attaching DOM Content Loaded event

		Object.defineProperty(document, 'readyState', {
			value: 'loading',
		});

		// Avoid: "Error: Not implemented: window.scrollTo."

		window.scrollTo = (_x, y) => {
			window.pageYOffset = y;
		};

		Object.defineProperty(document.documentElement, 'clientHeight', {
			value: PAGE_HEIGHT,
		});

		Object.defineProperty(document.documentElement, 'scrollHeight', {
			value: SCROLL_HEIGHT,
			writable: true,
		});

		fetchMock.mock('*', () => 200);

		Analytics = AnalyticsClient.create();
	});

	afterEach(() => {
		Analytics.reset();
		Analytics.dispose();

		fetchMock.restore();
	});

	afterAll(() => {
		jest.useRealTimers();
	});

	describe('readPage event', () => {
		it('is fired when reaches scroll and time', () => {
			const blogElement = createMainContent();
			const expectedReadDuration = Math.trunc(
				getExpectedViewDuration(blogElement.innerText)
			);

			const domContentLoaded = new Event('DOMContentLoaded');
			document.dispatchEvent(domContentLoaded);

			window.scrollTo(0, SCROLL_HEIGHT);
			document.dispatchEvent(new Event('scroll'));

			jest.advanceTimersByTime(expectedReadDuration);

			const events = Analytics.getEvents().filter(
				({eventId}) => eventId === 'pageRead'
			);

			expect(events.length).toEqual(1);

			document.body.removeChild(blogElement);
		});

		it('is not fired when reaches scroll only', () => {
			const blogElement = createMainContent();
			const expectedReadDuration = Math.trunc(
				getExpectedViewDuration(blogElement.innerText)
			);

			const domContentLoaded = new Event('DOMContentLoaded');
			document.dispatchEvent(domContentLoaded);

			window.scrollTo(0, SCROLL_HEIGHT);
			document.dispatchEvent(new Event('scroll'));

			jest.advanceTimersByTime(expectedReadDuration / 2);

			const events = Analytics.getEvents().filter(
				({eventId}) => eventId === 'pageRead'
			);

			expect(events.length).toEqual(0);

			document.body.removeChild(blogElement);
		});

		it('is not fired when reaches time only', () => {
			const blogElement = createMainContent();
			const expectedReadDuration = Math.trunc(
				getExpectedViewDuration(blogElement.innerText)
			);

			const domContentLoaded = new Event('DOMContentLoaded');
			document.dispatchEvent(domContentLoaded);

			window.scrollTo(0, PAGE_HEIGHT / 2);
			document.dispatchEvent(new Event('scroll'));

			jest.advanceTimersByTime(expectedReadDuration + 1000);

			const events = Analytics.getEvents().filter(
				({eventId}) => eventId === 'pageRead'
			);

			expect(events.length).toEqual(0);

			document.body.removeChild(blogElement);
		});

		it('is fired when there is not scroll on the page and reaches time', () => {

			// Redefining scrollHeight

			Object.defineProperty(document.documentElement, 'scrollHeight', {
				value: PAGE_HEIGHT,
			});

			// Restart Analytics

			Analytics.dispose();
			Analytics = AnalyticsClient.create();

			const blogElement = createMainContent();
			const expectedReadDuration = Math.trunc(
				getExpectedViewDuration(blogElement.innerText)
			);

			const domContentLoaded = new Event('DOMContentLoaded');
			document.dispatchEvent(domContentLoaded);

			jest.advanceTimersByTime(expectedReadDuration);

			const events = Analytics.getEvents().filter(
				({eventId}) => eventId === 'pageRead'
			);

			expect(events.length).toEqual(1);

			document.body.removeChild(blogElement);
		});

		it('is not fired twice when reaches scroll 75 and 100', () => {
			const blogElement = createMainContent();
			const expectedReadDuration = Math.trunc(
				getExpectedViewDuration(blogElement.innerText)
			);

			const domContentLoaded = new Event('DOMContentLoaded');
			document.dispatchEvent(domContentLoaded);

			window.scrollTo(0, SCROLL_HEIGHT * 0.5);
			document.dispatchEvent(new Event('scroll'));

			jest.advanceTimersByTime(expectedReadDuration);

			window.scrollTo(0, SCROLL_HEIGHT);
			document.dispatchEvent(new Event('scroll'));

			jest.advanceTimersByTime(FLUSH_INTERVAL);

			const events = Analytics.getEvents().filter(
				({eventId}) => eventId === 'pageRead'
			);

			expect(events.length).toEqual(1);

			document.body.removeChild(blogElement);
		});

		it('set expectedViewDuration based on lang', () => {
			Object.defineProperty(document.documentElement, 'lang', {
				value: 'zh',
			});

			const blogElement = createMainContent(false);
			const expectedReadDuration = Math.trunc(
				getExpectedViewDuration(blogElement.innerText)
			);

			const durationByCharacters = viewDurationByCharacters(
				blogElement.innerText
			);
			const durationByWords = viewDurationByWords(blogElement.innerText);

			expect(expectedReadDuration).toEqual(durationByCharacters);
			expect(expectedReadDuration).not.toEqual(durationByWords);

			document.body.removeChild(blogElement);
		});
	});
});
