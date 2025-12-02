/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openModal} from 'frontend-js-components-web';
import {sub} from 'frontend-js-web';

import ApiHelper from '../../../common/services/ApiHelper';
import DeleteStructureModalContent from '../../modal/DeleteStructureModalContent';
import {executeAsyncItemAction} from '../utils/executeAsyncItemAction';

const REPEATABLE_GROUPS_FOLDER_ERC = 'L_CMS_STRUCTURE_REPEATABLE_GROUPS';

export default async function deleteStructureAction({
	deleteAction,
	getObjectDefinitionDeleteInfoURL,
	loadData,
	name,
	status,
}: {
	deleteAction: {href: string; method: string};
	getObjectDefinitionDeleteInfoURL: string;
	loadData: () => {};
	name: string;
	status: number;
}) {
	const deleteStructureToast = async () => {
		await executeAsyncItemAction({
			method: deleteAction.method,
			refreshData: loadData,
			successMessage: sub(
				Liferay.Language.get('x-was-deleted-successfully'),
				`<strong>${Liferay.Util.escapeHTML(name)}</strong>`
			),
			url: deleteAction.href,
		});
	};

	if (status !== 0) {
		await deleteStructureToast();

		return;
	}

	const {data, error} = await ApiHelper.get<{
		hasObjectRelationship: boolean;
		objectEntriesCount: number;
	}>(getObjectDefinitionDeleteInfoURL);

	if (!data || error) {
		return;
	}

	const {hasObjectRelationship, objectEntriesCount} = data;

	if (hasObjectRelationship) {
		openModal({
			bodyHTML: `<p>${sub(
				Liferay.Language.get(
					'x-is-currently-referenced-by-or-referencing-other-content-structures,-and-so-cannot-be-deleted'
				),
				`<strong>${Liferay.Util.escapeHTML(name)}</strong>`
			)}</p>`,
			buttons: [
				{
					displayType: 'warning',
					label: Liferay.Language.get('ok'),
					onClick: ({processClose}: {processClose: Function}) => {
						processClose();
					},
				},
			],
			size: 'md',
			status: 'warning',
			title: Liferay.Language.get('deletion-not-allowed'),
		});
	}
	else {
		openModal({
			contentComponent: ({closeModal}: {closeModal: () => void}) =>
				DeleteStructureModalContent({
					closeModal,
					name,
					onDelete: deleteStructureToast,
					usesCount: objectEntriesCount,
				}),
			size: 'md',
			status: 'danger',
		});
	}
}

async function classifyRelationships(
	relationships: ObjectDefinition['objectRelationships']
): Promise<{
	referencedStructureIds: number[];
	repeatableGroupIds: number[];
}> {
	const referencedStructureIds: number[] = [];
	const repeatableGroupIds: number[] = [];

	if (!relationships?.length) {
		return {referencedStructureIds, repeatableGroupIds};
	}

	// Get all related object definitions

	const names = relationships.map((rel) => rel.objectDefinitionName2!);

	const objectDefinitions = await ApiHelper.getAll<ObjectDefinition>({
		filter: names
			.map((n) => `name eq '${n.replace(/'/g, "''")}'`)
			.join(' or '),
		url: '/o/object-admin/v1.0/object-definitions',
	});

	for (const objectDefinition of objectDefinitions) {

		// If it's a referenced structure, just push id to proper array

		if (
			objectDefinition.objectFolderExternalReferenceCode !==
			REPEATABLE_GROUPS_FOLDER_ERC
		) {
			referencedStructureIds.push(objectDefinition.id!);

			continue;
		}

		// If it's a repeatable group, push id to proper array and get nested ones

		if (objectDefinition.objectRelationships?.length) {
			const {repeatableGroupIds: nestedIds} = await classifyRelationships(
				objectDefinition.objectRelationships
			);

			repeatableGroupIds.push(...nestedIds);
		}

		repeatableGroupIds.push(objectDefinition.id!);
	}

	return {referencedStructureIds, repeatableGroupIds};
}
