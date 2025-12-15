/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator} from '@playwright/test';

export default async function dragAndDropElement({
	dragTarget,
	dropTarget,
	offset,
}: {
	dragTarget: Locator;
	dropTarget: Locator;
	offset?: {x?: number; y?: number};
}) {
	const boundingClientRect = await dropTarget.evaluate((element) =>
		element.getBoundingClientRect()
	);

	const targetPosition = {
		x: offset?.x ?? boundingClientRect.width / 2,
		y: offset?.y ?? boundingClientRect.height / 2,
	};

	await dragTarget.dragTo(dropTarget, {
		targetPosition,
	});
}
