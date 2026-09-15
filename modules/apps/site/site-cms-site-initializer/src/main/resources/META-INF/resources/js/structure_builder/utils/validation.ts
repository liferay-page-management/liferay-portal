/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {isNullOrUndefined} from '@liferay/layout-js-components-web';
import {sub} from 'frontend-js-web';
import {useCallback} from 'react';

import {ObjectDefinitions} from '../../common/types/ObjectDefinition';
import focusInvalidElement from '../../common/utils/focusInvalidElement';
import {State, useSelector, useStateDispatch} from '../contexts/StateContext';
import selectState from '../selectors/selectState';
import selectStructureChildren from '../selectors/selectStructureChildren';
import {
	Group,
	RelatedContent,
	Structure,
	StructureChild,
} from '../types/Structure';
import {Field, SelectFromListField} from './field';
import isField from './isField';
import isGroup from './isGroup';

const NAME_MAX_LENGTH = 41;
const ERC_MAX_LENGTH = 75;

export type ValidationProperty =
	| 'erc'
	| 'slug'
	| 'global'
	| 'name'
	| 'label'
	| 'max-length'
	| 'picklist'
	| 'related-content'
	| 'spaces';

export type ValidationError =
	| 'empty'
	| 'group-without-fields'
	| 'invalid-character'
	| 'in-use'
	| 'lowercase'
	| 'max-length'
	| 'default-language-label'
	| 'prefix-reserved'
	| 'unexpected'
	| 'uppercase';

export type ErrorMap = Map<ValidationProperty, ValidationError>;

export function validateField({
	children,
	currentErrors,
	data,
	deletedChildren,
	uuid,
}: {
	children?: Structure['children'];
	currentErrors?: ErrorMap;
	data: {
		erc?: Field['erc'];
		label?: Field['label'];
		name?: Field['name'];
		picklistId?: SelectFromListField['picklistId'];

		settings?: Field['settings'];
	};
	deletedChildren: State['history']['deletedChildren'];
	uuid?: Field['uuid'];
}): ErrorMap {
	const {erc, label, name, picklistId, settings} = data;

	const errors = new Map(currentErrors);

	if (!isNullOrUndefined(erc)) {
		if (!erc) {
			errors.set('erc', 'empty');
		}
		else if (erc.length > ERC_MAX_LENGTH) {
			errors.set('erc', 'max-length');
		}
		else if (erc.startsWith('L_')) {
			errors.set('erc', 'prefix-reserved');
		}
		else {
			errors.delete('erc');
		}
	}

	if (!isNullOrUndefined(name)) {
		const names = getSiblingFieldNames(uuid, children, deletedChildren);

		if (!name) {
			errors.set('name', 'empty');
		}
		else if (names.includes(name)) {
			errors.set('name', 'in-use');
		}
		else if (!/^[a-z]$/.test(name[0])) {
			errors.set('name', 'lowercase');
		}
		else if (!/^[a-zA-Z0-9]+$/.test(name)) {
			errors.set('name', 'invalid-character');
		}
		else if (name.length > NAME_MAX_LENGTH) {
			errors.set('name', 'max-length');
		}
		else {
			errors.delete('name');
		}
	}

	if (!isNullOrUndefined(label)) {
		Object.values(label ?? {}).every(Boolean)
			? errors.delete('label')
			: errors.set('label', 'empty');
	}

	if (!isNullOrUndefined(picklistId)) {
		picklistId
			? errors.delete('picklist')
			: errors.set('picklist', 'empty');
	}

	if (
		settings &&
		'maxLength' in settings &&
		!isNullOrUndefined(settings.maxLength)
	) {
		settings.maxLength
			? errors.delete('max-length')
			: errors.set('max-length', 'empty');
	}

	return errors;
}

export function validateRelatedContent({
	currentErrors,
	data,
}: {
	currentErrors?: ErrorMap;
	data: Partial<RelatedContent>;
}): ErrorMap {
	const {erc, label, relatedStructureERC} = data;

	const errors = new Map(currentErrors);

	if (!isNullOrUndefined(erc)) {
		if (!erc) {
			errors.set('erc', 'empty');
		}
		else if (erc.length > ERC_MAX_LENGTH) {
			errors.set('erc', 'max-length');
		}
		else if (erc.startsWith('L_')) {
			errors.set('erc', 'prefix-reserved');
		}
		else {
			errors.delete('erc');
		}
	}

	if (!isNullOrUndefined(label)) {
		Object.values(label ?? {}).every(Boolean)
			? errors.delete('label')
			: errors.set('label', 'empty');
	}

	if (!isNullOrUndefined(relatedStructureERC)) {
		relatedStructureERC
			? errors.delete('related-content')
			: errors.set('related-content', 'empty');
	}

	return errors;
}

export function validateGroup({
	currentErrors,
	data,
	isPublishing = false,
}: {
	currentErrors?: ErrorMap;
	data: Partial<Group>;
	isPublishing?: boolean;
}): ErrorMap {
	const errors = validateLabel({currentErrors, label: data.label});

	if (isPublishing && data.children) {
		_hasField(data.children)
			? errors.delete('global')
			: errors.set('global', 'group-without-fields');
	}

	return errors;
}

function _hasField(children: Group['children']): boolean {
	for (const child of children.values()) {
		if (isField(child)) {
			return true;
		}

		if (isGroup(child) && _hasField(child.children)) {
			return true;
		}
	}

	return false;
}

function validateLabel({
	currentErrors,
	label,
}: {
	currentErrors?: ErrorMap;
	label?: Liferay.Language.LocalizedValue<string>;
}): ErrorMap {
	const errors = new Map(currentErrors);

	if (!isNullOrUndefined(label)) {
		Object.values(label ?? {}).every(Boolean)
			? errors.delete('label')
			: errors.set('label', 'empty');
	}

	return errors;
}

export function validateStructure({
	currentErrors,
	data,
	isGlobalValidation = false,
	objectDefinitions,
}: {
	currentErrors?: ErrorMap;
	data: Partial<Structure>;
	isGlobalValidation?: boolean;
	objectDefinitions?: ObjectDefinitions;
}): ErrorMap {
	const {erc, label, name, spaces} = data;

	const errors = new Map(currentErrors);

	if (!isNullOrUndefined(erc) && !data.system) {
		if (!erc) {
			errors.set('erc', 'empty');
		}
		else if (erc.length > ERC_MAX_LENGTH) {
			errors.set('erc', 'max-length');
		}
		else if (erc.startsWith('L_')) {
			errors.set('erc', 'prefix-reserved');
		}
		else {
			errors.delete('erc');
		}
	}

	if (!isNullOrUndefined(name) && !data.system) {
		const names = getStructureNames(objectDefinitions);

		if (!name) {
			errors.set('name', 'empty');
		}
		else if (names.includes(name)) {
			errors.set('name', 'in-use');
		}
		else if (!/^[A-Z]$/.test(name[0])) {
			errors.set('name', 'uppercase');
		}
		else if (!/^[a-zA-Z0-9]+$/.test(name)) {
			errors.set('name', 'invalid-character');
		}
		else if (name.length > NAME_MAX_LENGTH) {
			errors.set('name', 'max-length');
		}
		else {
			errors.delete('name');
		}
	}

	if (!isNullOrUndefined(label)) {
		const defaultLanguageValue =
			label[Liferay.ThemeDisplay.getDefaultLanguageId()];

		const values = Object.values(label ?? {});

		if (!!values.length && values.every(Boolean)) {
			errors.delete('label');
		}
		else {
			errors.set('label', 'empty');
		}

		if (isGlobalValidation && !defaultLanguageValue) {
			errors.set('global', 'default-language-label');
		}
		else if (defaultLanguageValue) {
			errors.delete('global');
		}
	}

	if (!isNullOrUndefined(spaces)) {
		spaces.length ? errors.delete('spaces') : errors.set('spaces', 'empty');
	}

	return errors;
}

export function getErrorMessage(
	property: ValidationProperty,
	error: ValidationError,
	values: {
		erc?: string;
		name?: string;
	}
) {
	const {erc, name} = values;

	if (property === 'global') {
		if (error === 'group-without-fields') {
			return Liferay.Language.get(
				'this-group-needs-at-least-one-field-that-isnt-a-referenced-structure-or-select-related-content-to-be-published'
			);
		}

		if (error === 'unexpected') {
			return Liferay.Language.get(
				'an-unexpected-error-occurred-while-saving-or-publishing-the-content-structure'
			);
		}

		if (error === 'default-language-label') {
			return sub(
				Liferay.Language.get(
					'please-enter-a-valid-label-for-the-default-language-x'
				),
				Liferay.ThemeDisplay.getDefaultLanguageId()
			);
		}
	}

	if (property === 'erc') {
		if (error === 'max-length' && erc) {
			return `${Liferay.Language.get(
				'maximum-number-of-characters-exceeded'
			)}: ${erc.length}/${ERC_MAX_LENGTH}`;
		}
		else if (error === 'prefix-reserved') {
			return sub(Liferay.Language.get('the-prefix-x-is-reserved'), 'L_');
		}
	}

	if (property === 'slug') {
		if (error === 'in-use') {
			return Liferay.Language.get(
				'the-friendly-url-is-already-in-use.-please-enter-a-unique-friendly-url'
			);
		}
	}

	if (property === 'name') {
		if (error === 'uppercase') {
			return Liferay.Language.get(
				'the-first-character-must-be-an-uppercase-letter'
			);
		}
		else if (error === 'lowercase') {
			return Liferay.Language.get(
				'the-first-character-must-be-a-lowercase-letter'
			);
		}
		else if (error === 'max-length' && name) {
			return `${Liferay.Language.get(
				'maximum-number-of-characters-exceeded'
			)}: ${name.length}/${NAME_MAX_LENGTH}`;
		}
		else if (error === 'invalid-character') {
			return Liferay.Language.get(
				'this-field-must-only-contain-letters-and-digits'
			);
		}
		else if (error === 'in-use') {
			return Liferay.Language.get(
				'this-name-is-already-in-use-try-another-one'
			);
		}
	}

	if (property === 'spaces' && error === 'empty') {
		return Liferay.Language.get('spaces-must-be-selected');
	}

	if (error === 'empty') {
		return Liferay.Language.get('this-field-is-required');
	}

	return Liferay.Language.get('an-unexpected-error-occurred');
}

function getSiblingFieldNames(
	uuid?: Field['uuid'],
	children?: Structure['children'],
	deletedChildren?: State['history']['deletedChildren']
) {
	if (!uuid || !children) {
		return [];
	}

	const deletedFields =
		deletedChildren?.filter(
			(child) => child.type !== 'referenced-structure' && !isGroup(child)
		) || [];

	const fields = [...deletedFields, ...children.values()];

	return fields
		.filter(
			(child): child is Field | RelatedContent =>
				child.type !== 'referenced-structure' &&
				!isGroup(child) &&
				child.uuid !== uuid
		)
		.map((child) => child.name);
}

function getStructureNames(objectDefinitions?: ObjectDefinitions) {
	if (!objectDefinitions) {
		return [];
	}

	return Object.values(objectDefinitions).map(
		(ObjectDefinition) => ObjectDefinition.name
	);
}

export function useValidate() {
	const dispatch = useStateDispatch();
	const children = useSelector(selectStructureChildren);
	const state = useSelector(selectState);

	const {structure} = state;

	const validateChild = useCallback(
		(
			child: StructureChild,
			invalids: State['invalids'],
			deletedChildren: State['history']['deletedChildren'],
			isPublishing: boolean
		) => {
			let errors: ErrorMap = new Map();

			if (child.type === 'related-content') {
				errors = validateRelatedContent({data: child});

				if (errors.size) {
					invalids.set(child.uuid, errors);
				}
			}
			else if (isGroup(child)) {
				errors = validateGroup({data: child, isPublishing});

				if (errors.size) {
					invalids.set(child.uuid, errors);
				}
				else {

					// The group may have been reported before it held a field,
					// so drop the stale error rather than blocking publishing.

					invalids.delete(child.uuid);
				}

				for (const grandChild of child.children.values()) {
					if (grandChild.type === 'referenced-structure') {
						continue;
					}

					validateChild(
						grandChild,
						invalids,
						deletedChildren,
						isPublishing
					);
				}
			}
			else if (child.type !== 'referenced-structure') {
				errors = validateField({
					data: child as Field,
					deletedChildren,
				});

				if (errors.size) {
					invalids.set(child.uuid, errors);
				}
			}
		},
		[]
	);

	return useCallback(
		(isPublishing = false) => {

			// Validate structure

			let errors: ErrorMap = new Map();

			const invalids = new Map(state.invalids);

			errors = validateStructure({
				data: structure,
				isGlobalValidation: true,
			});

			if (errors.size) {
				invalids.set(structure.uuid, errors);
			}

			// Validate children

			for (const child of children.values()) {
				validateChild(
					child,
					invalids,
					state.history.deletedChildren,
					isPublishing
				);
			}

			// If there's some invalid, dispatch validate action

			if (invalids.size) {
				dispatch({
					invalids,
					type: 'validate',
				});

				focusInvalidElement();

				return false;
			}

			// It's valid

			return true;
		},
		[
			children,
			dispatch,
			state.history.deletedChildren,
			state.invalids,
			structure,
			validateChild,
		]
	);
}
