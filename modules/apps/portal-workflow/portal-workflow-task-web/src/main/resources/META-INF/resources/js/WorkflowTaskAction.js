/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openSimpleInputModal} from 'frontend-js-web';

export default function ({
	namespace: portletNamespace,
	randomId,
	workflowTasks,
}) {
	const onTaskClickFn = (event) => {
		event.preventDefault();

		const icon = event.currentTarget;

		openSimpleInputModal({
			dialogTitle: icon.text,
			formSubmitURL: icon.href,
			isTextArea: true,
			mainFieldLabel: Liferay.Language.get('comment'),
			mainFieldName: 'comment',
			namespace: portletNamespace,
			onFormSuccess: () => window.location.reload(),
			placeholder: Liferay.Language.get('comment'),
		});
	};

	workflowTasks.forEach((workflowTask) => {
		const element = document.getElementById(
			`${portletNamespace}${randomId}${workflowTask}taskChangeStatusLink`
		);
		element?.addEventListener('click', onTaskClickFn);
	});

	return {
		dispose() {
			workflowTasks?.forEach((workflowTask) => {
				workflowTask.removeEventListener('click', onTaskClickFn);
			});
		},
	};
}
