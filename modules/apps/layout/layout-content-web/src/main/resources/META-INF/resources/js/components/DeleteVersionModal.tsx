/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayModal, {useModal} from '@clayui/modal';
import React from 'react';

interface Props {
	onClose: () => void;
	onConfirm: () => void;
}

export default function DeleteVersionModal({onClose, onConfirm}: Props) {
	const {observer, onOpenChange} = useModal({onClose});

	return (
		<ClayModal observer={observer} size="sm" status="warning">
			<ClayModal.Header>
				{Liferay.Language.get('delete-version')}
			</ClayModal.Header>

			<ClayModal.Body>
				{Liferay.Language.get(
					'deleting-a-version-is-an-action-impossible-to-revert'
				)}
			</ClayModal.Body>

			<ClayModal.Footer
				last={
					<ClayButton.Group spaced>
						<ClayButton
							displayType="secondary"
							onClick={() => onOpenChange(false)}
						>
							{Liferay.Language.get('cancel')}
						</ClayButton>

						<ClayButton displayType="danger" onClick={onConfirm}>
							{Liferay.Language.get('delete')}
						</ClayButton>
					</ClayButton.Group>
				}
			/>
		</ClayModal>
	);
}
