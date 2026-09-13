/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAlert from '@clayui/alert';
import {ClayToggle} from '@clayui/form';
import ClayLabel from '@clayui/label';
import ClayLayout from '@clayui/layout';
import ClayTabs from '@clayui/tabs';
import {useId} from 'frontend-js-components-web';
import React, {useEffect} from 'react';

import focusInvalidElement from '../../../common/utils/focusInvalidElement';
import {config} from '../../config';
import {useSelector, useStateDispatch} from '../../contexts/StateContext';
import selectErrors from '../../selectors/selectErrors';
import selectPublishedChildren from '../../selectors/selectPublishedChildren';
import selectStructure from '../../selectors/selectStructure';
import {Group} from '../../types/Structure';
import getGroupRepeatableBlocker from '../../utils/getGroupRepeatableBlocker';
import handleSetGroupRepeatable from '../../utils/handleSetGroupRepeatable';
import StructureBreadcrumb from '../Breadcrumb';
import {LocalizedInput} from '../LocalizedInput';

export default function GroupSettings({
	disabled,
	group,
}: {
	disabled?: boolean;
	group: Group;
}) {
	useEffect(() => {
		focusInvalidElement();
	}, []);

	return (
		<ClayLayout.ContainerFluid className="px-4" size="md" view>
			<StructureBreadcrumb uuid={group.uuid} />

			<GroupAlert uuid={group.uuid} />

			<ClayTabs>
				<ClayTabs.List>
					<ClayTabs.Item>
						{Liferay.Language.get('general')}
					</ClayTabs.Item>
				</ClayTabs.List>

				<ClayTabs.Panels fade>
					<ClayTabs.TabPane className="px-0">
						<GeneralTab disabled={disabled} group={group} />
					</ClayTabs.TabPane>
				</ClayTabs.Panels>
			</ClayTabs>
		</ClayLayout.ContainerFluid>
	);
}

function GroupAlert({uuid}: {uuid: Group['uuid']}) {
	const error = useSelector(selectErrors(uuid)).get('global');

	if (!error) {
		return null;
	}

	return (
		<ClayAlert
			displayType="danger"
			role={null}
			title={Liferay.Language.get('error')}
		>
			{error}
		</ClayAlert>
	);
}

function GeneralTab({disabled, group}: {disabled?: boolean; group: Group}) {
	const dispatch = useStateDispatch();

	const errors = useSelector(selectErrors(group.uuid));

	const publishedChildren = useSelector(selectPublishedChildren);
	const structure = useSelector(selectStructure);

	const labelInputId = useId();

	const blockerMessage = getGroupRepeatableBlocker({
		group,
		isRepeatable: !group.isRepeatable,
		publishedChildren,
		structure,
	});

	return (
		<div>
			<div className="pb-2">
				<p className="font-weight-semi-bold mb-0 text-3">
					{Liferay.Language.get('field-type')}
				</p>

				<ClayLabel displayType="success" inverse>
					{config.isNonRepeatableGroupsEnabled
						? Liferay.Language.get('group')
						: Liferay.Language.get('repeatable-group')}
				</ClayLabel>
			</div>

			<LocalizedInput
				disabled={disabled}
				error={errors.get('label')}
				formGroupClassName="mt-4"
				id={labelInputId}
				label={Liferay.Language.get('label')}
				onSave={(translations) =>
					dispatch({
						label: translations,
						type: 'update-group',
						uuid: group.uuid,
					})
				}
				required
				translations={group.label}
			/>

			{config.isNonRepeatableGroupsEnabled ? (
				<div className="align-items-center d-flex mt-4">
					<ClayToggle
						aria-label={Liferay.Language.get('repeatable')}
						disabled={disabled || Boolean(blockerMessage)}
						onToggle={(isRepeatable) =>
							handleSetGroupRepeatable({
								dispatch,
								group,
								isRepeatable,
								publishedChildren,
								structure,
							})
						}
						title={blockerMessage ?? undefined}
						toggled={group.isRepeatable}
					/>

					<span className="ml-2">
						{Liferay.Language.get('repeatable')}
					</span>
				</div>
			) : null}
		</div>
	);
}
