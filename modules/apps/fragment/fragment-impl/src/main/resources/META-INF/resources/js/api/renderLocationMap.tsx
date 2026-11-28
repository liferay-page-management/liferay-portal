/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {render} from '@liferay/frontend-js-react-web';
import {LocationMap} from '@liferay/object-js-components-web';

import type React from 'react';

type Props = React.ComponentProps<typeof LocationMap>;

export function renderLocationMap(container: HTMLElement, props: Props): void {
	render(LocationMap, {componentId: `${props.id}-map`, ...props}, container);
}
