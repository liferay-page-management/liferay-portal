/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openToast} from 'frontend-js-components-web';
import {fetch, navigate} from 'frontend-js-web';
import {useCallback, useRef, useState} from 'react';

import {validateForm} from '../components/ContentTypeModalForm';
import {ValidationError} from '../types/ValidationError';

interface Props {
	closeModal: () => void;
	formSubmitURL: string;
	namespace: string;
}

export default function useAddDesignLibraryResource({
	closeModal,
	formSubmitURL,
	namespace,
}: Props) {
	const [error, setError] = useState<ValidationError>({});
	const [loading, setLoading] = useState(false);

	const formRef = useRef<HTMLFormElement>(null);

	const handleSubmit = useCallback(
		(event: React.SyntheticEvent) => {
			event.preventDefault();

			const form = formRef.current;

			const error = validateForm(form, namespace);

			if (Object.keys(error).length !== 0) {
				setError(error);

				return;
			}

			setLoading(true);

			fetch(formSubmitURL, {
				body: new FormData(form!),
				method: 'POST',
			})
				.then((response) => response.json())
				.then(({error, redirectURL}) => {
					if (error) {
						setError(
							typeof error === 'string' ? {name: error} : error
						);
						setLoading(false);
					}
					else if (redirectURL) {
						navigate(redirectURL, {beforeScreenFlip: closeModal});
					}
				})
				.catch(() => {
					setLoading(false);

					openToast({
						message: Liferay.Language.get(
							'an-unexpected-error-occurred'
						),
						type: 'danger',
					});
				});
		},
		[closeModal, formSubmitURL, namespace]
	);

	return {error, formRef, handleSubmit, loading};
}
