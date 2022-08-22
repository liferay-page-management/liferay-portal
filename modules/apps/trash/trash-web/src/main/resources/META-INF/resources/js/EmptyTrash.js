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

import {fetch} from 'frontend-js-web';

export default function ({emptyTrashURL, namespace}) {
	const emptyTrashButton = document.getElementById(
		`${namespace}emptyTrashButton`
	);

	const onClick = () => {
		fetch(emptyTrashURL).then(() => window.location.reload());
	};

	emptyTrashButton?.addEventListener('click', onClick);

	return {
		dispose() {
			emptyTrashButton?.removeEventListener('click', onClick);
		},
	};
}
