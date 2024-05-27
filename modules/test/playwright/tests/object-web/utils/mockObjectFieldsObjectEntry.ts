/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {getRandomInt} from '../../../utils/getRandomInt';
import getRandomString from '../../../utils/getRandomString';

type objectFieldBusinessType = (typeof objectFieldBusinessTypes)[number];

type objectFieldBusinessTypesLabelName = {
	[K in objectFieldBusinessType]: objectFieldLabelName;
};

type objectFieldLabelName = {
	label: string;
	name: string;
};

const objectFieldBusinessTypes = [
	'autoIncrement',
	'boolean',
	'decimal',
	'encrypted',
	'integer',
	'longInteger',
	'longText',
	'multiselectPicklist',
	'picklist',
	'precisionDecimal',
	'richText',
	'text',
] as const;

function mockObjectEntry(
	objectFieldBusinessTypesInfo: objectFieldBusinessTypesLabelName,
	listTypeEntries: string[]
) {
	return {
		[objectFieldBusinessTypesInfo.boolean.name]: Math.random() < 0.5,
		[objectFieldBusinessTypesInfo.decimal.name]: parseFloat(
			Math.random().toFixed(10)
		).toString(),
		[objectFieldBusinessTypesInfo.encrypted.name]: getRandomString(),
		[objectFieldBusinessTypesInfo.integer.name]: Math.floor(
			Math.random() * 100
		),
		[objectFieldBusinessTypesInfo.longInteger.name]: getRandomInt(),
		[objectFieldBusinessTypesInfo.longText.name]: getRandomString(),
		[objectFieldBusinessTypesInfo.multiselectPicklist.name]: [
			listTypeEntries[0],
			listTypeEntries[1],
		],
		[objectFieldBusinessTypesInfo.picklist.name]: {
			key: listTypeEntries[0],
		},
		[objectFieldBusinessTypesInfo.precisionDecimal.name]: parseFloat(
			Math.random().toFixed(15)
		).toString(),
		[objectFieldBusinessTypesInfo.richText.name]: getRandomString(),
		[objectFieldBusinessTypesInfo.text.name]: getRandomString(),
	};
}

function mockObjectFields(
	objectFieldBusinessTypesInfo: objectFieldBusinessTypesLabelName,
	listTypeDefinition: ListTypeDefinition
) {
	return [
		{
			DBType: 'String',
			businessType: 'AutoIncrement',
			indexed: true,
			indexedAsKeyword: false,
			indexedLanguageId: 'en_US',
			label: {
				en_US: objectFieldBusinessTypesInfo.autoIncrement.label,
			},
			localized: false,
			name: objectFieldBusinessTypesInfo.autoIncrement.name,
			objectFieldSettings: [
				{
					name: 'initialValue',
					value: '1',
				},
			],
			readOnly: 'false',
			readOnlyConditionExpression: '',
			required: false,
			state: false,
			system: false,
			type: 'String',
			unique: false,
		},
		{
			DBType: 'Boolean',
			businessType: 'Boolean',
			indexed: true,
			indexedAsKeyword: false,
			indexedLanguageId: '',
			label: {
				en_US: objectFieldBusinessTypesInfo.boolean.label,
			},
			localized: false,
			name: objectFieldBusinessTypesInfo.boolean.name,
			objectFieldSettings: [],
			readOnly: 'false',
			readOnlyConditionExpression: '',
			required: false,
			state: false,
			system: false,
			type: 'Boolean',
			unique: false,
		},
		{
			DBType: 'Double',
			businessType: 'Decimal',
			indexed: true,
			indexedAsKeyword: false,
			indexedLanguageId: '',
			label: {
				en_US: objectFieldBusinessTypesInfo.decimal.label,
			},
			listTypeDefinitionExternalReferenceCode: '',
			localized: false,
			name: objectFieldBusinessTypesInfo.decimal.name,
			objectFieldSettings: [],
			readOnly: 'false',
			readOnlyConditionExpression: '',
			required: false,
			state: false,
		},
		{
			DBType: 'Clob',
			businessType: 'Encrypted',
			indexed: false,
			indexedAsKeyword: false,
			indexedLanguageId: '',
			label: {
				en_US: objectFieldBusinessTypesInfo.encrypted.label,
			},
			localized: false,
			name: objectFieldBusinessTypesInfo.encrypted.name,
			objectFieldSettings: [],
			readOnly: 'false',
			readOnlyConditionExpression: '',
			required: false,
			state: false,
			system: false,
			type: 'Clob',
			unique: false,
		},
		{
			DBType: 'Integer',
			businessType: 'Integer',
			indexed: true,
			indexedAsKeyword: false,
			indexedLanguageId: '',
			label: {
				en_US: objectFieldBusinessTypesInfo.integer.label,
			},
			localized: false,
			name: objectFieldBusinessTypesInfo.integer.name,
			objectFieldSettings: [],
			readOnly: 'false',
			readOnlyConditionExpression: '',
			required: false,
			state: false,
			system: false,
			type: 'Integer',
			unique: false,
		},
		{
			DBType: 'Long',
			businessType: 'LongInteger',
			indexed: true,
			indexedAsKeyword: false,
			indexedLanguageId: '',
			label: {
				en_US: objectFieldBusinessTypesInfo.longInteger.label,
			},
			localized: false,
			name: objectFieldBusinessTypesInfo.longInteger.name,
			objectFieldSettings: [],
			readOnly: 'false',
			readOnlyConditionExpression: '',
			required: false,
			state: false,
			system: false,
			type: 'Long',
			unique: false,
		},
		{
			DBType: 'Clob',
			businessType: 'LongText',
			indexed: true,
			indexedAsKeyword: false,
			indexedLanguageId: 'en_US',
			label: {
				en_US: objectFieldBusinessTypesInfo.longText.label,
			},
			localized: false,
			name: objectFieldBusinessTypesInfo.longText.name,
			objectFieldSettings: [
				{
					name: 'showCounter',
					value: false,
				},
			],
			readOnly: 'false',
			readOnlyConditionExpression: '',
			required: false,
			state: false,
			system: false,
			type: 'Clob',
			unique: false,
		},
		{
			DBType: 'String',
			businessType: 'MultiselectPicklist',
			indexed: true,
			indexedAsKeyword: false,
			indexedLanguageId: 'en_US',
			label: {
				en_US: objectFieldBusinessTypesInfo.multiselectPicklist.label,
			},
			listTypeDefinitionExternalReferenceCode:
				listTypeDefinition.externalReferenceCode,
			listTypeDefinitionId: listTypeDefinition.id,
			localized: false,
			name: objectFieldBusinessTypesInfo.multiselectPicklist.name,
			objectFieldSettings: [],
			readOnly: 'false',
			readOnlyConditionExpression: '',
			required: false,
			state: false,
			system: false,
			type: 'String',
			unique: false,
		},
		{
			DBType: 'String',
			businessType: 'Picklist',
			indexed: true,
			indexedAsKeyword: false,
			indexedLanguageId: 'en_US',
			label: {
				en_US: objectFieldBusinessTypesInfo.picklist.label,
			},
			listTypeDefinitionExternalReferenceCode:
				listTypeDefinition.externalReferenceCode,
			localized: false,
			name: objectFieldBusinessTypesInfo.picklist.name,
			objectFieldSettings: [],
			readOnly: 'false',
			readOnlyConditionExpression: '',
			required: false,
			state: false,
			system: false,
			type: 'String',
			unique: false,
		},
		{
			DBType: 'BigDecimal',
			businessType: 'PrecisionDecimal',
			indexed: true,
			indexedAsKeyword: false,
			indexedLanguageId: '',
			label: {
				en_US: objectFieldBusinessTypesInfo.precisionDecimal.label,
			},
			localized: false,
			name: objectFieldBusinessTypesInfo.precisionDecimal.name,
			objectFieldSettings: [],
			readOnly: 'false',
			readOnlyConditionExpression: '',
			required: false,
			state: false,
			system: false,
			type: 'BigDecimal',
			unique: false,
		},
		{
			DBType: 'Clob',
			businessType: 'RichText',
			indexed: true,
			indexedAsKeyword: false,
			indexedLanguageId: 'en_US',
			label: {
				en_US: objectFieldBusinessTypesInfo.richText.label,
			},
			localized: false,
			name: objectFieldBusinessTypesInfo.richText.name,
			objectFieldSettings: [],
			readOnly: 'false',
			readOnlyConditionExpression: '',
			required: false,
			state: false,
			system: false,
			type: 'Clob',
			unique: false,
		},
		{
			DBType: 'String',
			businessType: 'Text',
			indexed: true,
			indexedAsKeyword: false,
			indexedLanguageId: 'en_US',
			label: {
				en_US: objectFieldBusinessTypesInfo.text.label,
			},
			localized: false,
			name: objectFieldBusinessTypesInfo.text.name,
			objectFieldSettings: [],
			readOnly: 'false',
			readOnlyConditionExpression: '',
			required: false,
			state: false,
			system: false,
			type: 'String',
			unique: false,
		},
	];
}

export function mockObjectFieldsObjectEntry(
	listTypeDefinition: ListTypeDefinition,
	listTypeDefinitionItems: string[]
) {
	const objectFieldBusinessTypeLabelName =
		{} as objectFieldBusinessTypesLabelName;

	const setLabelName = (
		target: objectFieldBusinessTypesLabelName,
		businessType: string,
		{label, name}
	) => {
		Object.defineProperty(target, businessType, {value: {label, name}});
	};

	objectFieldBusinessTypes.forEach((objectFieldBusinessType) => {
		setLabelName(
			objectFieldBusinessTypeLabelName,
			objectFieldBusinessType,
			{
				label: `${objectFieldBusinessType}ObjectFieldLabel`,
				name: `${objectFieldBusinessType}ObjectFieldName`,
			}
		);
	});

	return {
		objectEntry: mockObjectEntry(
			objectFieldBusinessTypeLabelName,
			listTypeDefinitionItems
		),
		objectFields: mockObjectFields(
			objectFieldBusinessTypeLabelName,
			listTypeDefinition
		),
	};
}
