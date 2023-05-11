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

import {render} from '@liferay/frontend-js-react-web';
import {unmountComponentAtNode} from 'react-dom';

import ImportModal from './modal/ImportModal';

export default function ({namespace, url}) {
	Liferay.Util.setPortletConfigurationIconAction(`${namespace}import`, () => {
		if (Liferay.FeatureFlags['LPS-174939']) {
			const modalContainer = document.createElement('div');
			modalContainer.classList.add('cadmin');
			document.body.appendChild(modalContainer);

			const disposeModal = () => {
				if (modalContainer) {
					unmountComponentAtNode(modalContainer);
					document.body.removeChild(modalContainer);
				}
			};

			render(
				ImportModal,
				{
					disposeModal,
					namespace,
				},
				modalContainer
			);
		}
		else {
			Liferay.Util.openModal({
				onClose() {
					window.location.reload();
				},
				title: Liferay.Language.get('import'),
				url,
			});
		}
	});
}
