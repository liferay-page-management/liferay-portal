/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayLoadingIndicator from '@clayui/loading-indicator';
import ClayModal from '@clayui/modal';
import React from 'react';

import ContentTypeModalForm from './components/ContentTypeModalForm';
import {MODAL_TYPES} from './constants/modalTypes';
import useAddDesignLibraryResource from './hooks/useAddDesignLibraryResource';

interface Props {
	addMasterLayoutURL: string;
	closeModal: () => void;
	namespace: string;
}

export default function AddMasterPageDesignLibraryModalContent({
	addMasterLayoutURL,
	closeModal,
	namespace,
}: Props) {
	const {error, formRef, handleSubmit, loading} = useAddDesignLibraryResource(
		{
			closeModal,
			formSubmitURL: addMasterLayoutURL,
			namespace,
		}
	);

	return (
		<>
			<ClayModal.Header>
				{Liferay.Language.get('new-master-page')}
			</ClayModal.Header>

			<ClayModal.Body>
				<ContentTypeModalForm
					displayPageName=""
					error={error}
					formRef={formRef}
					mappingTypes={[]}
					namespace={namespace}
					onSubmit={handleSubmit}
					type={MODAL_TYPES.create}
				/>
			</ClayModal.Body>

			<ClayModal.Footer
				last={
					<ClayButton.Group spaced>
						<ClayButton
							displayType="secondary"
							onClick={closeModal}
						>
							{Liferay.Language.get('cancel')}
						</ClayButton>

						<ClayButton
							disabled={loading}
							displayType="primary"
							onClick={handleSubmit}
						>
							{loading && (
								<span className="inline-item inline-item-before">
									<ClayLoadingIndicator
										displayType="secondary"
										size="sm"
									/>
								</span>
							)}

							{Liferay.Language.get('save')}
						</ClayButton>
					</ClayButton.Group>
				}
			/>
		</>
	);
}
