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

import {READ_MINIMUM_SCROLL_DEPTH} from '../utils/constants';

/**
 * Returns if there is vertical scroll bar on the client.
 * @returns {boolean} True if there is vertical scroll bar
 */
function hasVerticalScrollBar() {
	const {clientHeight, scrollHeight} = document.documentElement;

	return (clientHeight * 100) / scrollHeight <= READ_MINIMUM_SCROLL_DEPTH;
}

class ReadTracker {
	constructor() {
		this.timeReached = false;
		this.depthReached = !hasVerticalScrollBar();
	}

	dispose() {
		this.timeReached = false;
		this.depthReached = !hasVerticalScrollBar();

		if (this.timeoutId) {
			clearTimeout(this.timeoutId);
		}
	}

	/**
	 * Set the expected time to be considered read
	 *
	 * @param {Function} fn The callback function that will process if the depth and time are reached.
	 * @param {number} expectedViewDuration The expected view duration to be considered read
	 */
	setExpectedViewDuration(fn, expectedViewDuration) {
		if (expectedViewDuration && !this.timeoutId) {
			this.timeoutId = setTimeout(
				() => this.onTimeReached(fn),
				Math.trunc(expectedViewDuration)
			);
		}
	}

	/**
	 * Set depth reached
	 * @param {Function} fn The callback function that will process if the depth and time are reached.
	 */
	onDepthReached(fn) {
		if (!this.depthReached) {
			this.depthReached = true;
			this.checkIsRead(fn);
		}
	}

	/**
	 * Set time reached
	 * @param {Function} fn The callback function that will process if the depth and time are reached.
	 */
	onTimeReached(fn) {
		this.timeReached = true;
		this.checkIsRead(fn);
	}

	/**
	 * Check if the client reached both conditions to be considered read
	 * If the client reaches both it sends an analytics event
	 * @param {Function} fn The callback function that will process if the depth and time are reached.
	 */
	checkIsRead(fn) {
		if (this.timeReached && this.depthReached) {
			fn();
		}
	}
}

export {ReadTracker};
export default ReadTracker;
