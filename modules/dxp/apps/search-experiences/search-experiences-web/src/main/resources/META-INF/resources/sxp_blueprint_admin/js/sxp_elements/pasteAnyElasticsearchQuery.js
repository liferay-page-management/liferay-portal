/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 */

export default {
	description_i18n: {
		en_US: 'Paste any Elasticsearch query body in the element as is.',
	},
	elementDefinition: {
		category: 'custom',
		configuration: {
			queryConfiguration: {
				queryEntries: [
					{
						clauses: [
							{
								context: 'query',
								occur: '${configuration.occur}',
								query: '${configuration.query}',
							},
						],
					},
				],
			},
		},
		icon: 'custom-field',
		uiConfiguration: {
			fieldSets: [
				{
					fields: [
						{
							defaultValue: 'must',
							label: 'Occur',
							name: 'occur',
							type: 'select',
							typeOptions: {
								options: [
									{
										label: 'MUST',
										value: 'must',
									},
									{
										label: 'SHOULD',
										value: 'should',
									},
									{
										label: 'MUST NOT',
										value: 'must_not',
									},
									{
										label: 'FILTER',
										value: 'filter',
									},
								],
							},
						},
						{
							defaultValue: {},
							label: 'Query',
							name: 'query',
							type: 'json',
						},
					],
				},
			],
		},
	},
	title_i18n: {
		en_US: 'Paste Any Elasticsearch Query',
	},
};
