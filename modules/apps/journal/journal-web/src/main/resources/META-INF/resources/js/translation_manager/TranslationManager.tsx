/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locale, TranslationAdminSelector} from 'frontend-js-components-web';
import React, {useCallback, useEffect, useMemo, useRef, useState} from 'react';

type Field = Record<Liferay.Language.Locale, string>;

type Fields = Record<string, Field> | null;

interface Props {
	defaultLanguageId: Liferay.Language.Locale;
	fields: Record<string, Field>;
	locales: Locale[];
	selectedLanguageId: Liferay.Language.Locale;
}

export default function TranslationManager({
	defaultLanguageId,
	fields: initialFields,
	locales,
	selectedLanguageId: initialSelectedLanguageId,
}: Props) {
	const [fields, setFields] = useState<Fields>(null);
	const [selectedLanguageId, setSelectedLanguageId] = useState<
		Liferay.Language.Locale
	>(initialSelectedLanguageId);
	const [translations, setTranslations] = useState(
		fieldToTranslations(initialFields)
	);
	const triggerRef = useRef<HTMLButtonElement | null>(null);

	const updateTranslations = (fields: Fields) => {
		if (!fields) {
			return;
		}

		const newTranslations = Object.keys(fields).map((fieldName) => {
			const languages = Array.from(
				document.querySelectorAll<HTMLInputElement>(
					`[type="hidden"][data-field-name="${fieldName}"]`
				)
			)
				.filter((input) => input.value)
				.map(
					(input) =>
						input.dataset.languageid as Liferay.Language.Locale
				);

			return {
				fieldName,
				languages,
			};
		});

		setTranslations(newTranslations);
	};

	const updateTranslationStatus = useCallback(
		() => updateTranslations(fields),
		[fields]
	);

	const getLocalizableFields = useCallback(
		(event) => {
			if (
				event.type === 'keydown' &&
				event.key !== 'Enter' &&
				event.key !== 'Scape'
			) {
				return;
			}

			const ddmFields = Array.from(
				document.querySelectorAll<HTMLInputElement>(
					`[data-ddm-localizable-field]`
				)
			)
				.map((field) => field.dataset.fieldName!)
				.reduce((acc, name) => ({...acc, [name]: {}}), {});

			const fields = {...initialFields, ...ddmFields};

			setFields(fields);

			updateTranslations(fields);

			triggerRef.current?.removeEventListener(
				'click',
				getLocalizableFields
			);

			triggerRef.current?.removeEventListener(
				'keydown',
				getLocalizableFields
			);
		},
		[initialFields]
	);

	useEffect(() => {
		if (fields) {
			Liferay.on(
				'inputLocalized:updateTranslationStatus',
				updateTranslationStatus
			);
		}
		else {
			triggerRef.current?.addEventListener('click', getLocalizableFields);
			triggerRef.current?.addEventListener(
				'keydown',
				getLocalizableFields
			);
		}

		return () => {
			Liferay.detach(
				'inputLocalized:updateTranslationStatus',
				updateTranslationStatus
			);
		};
	}, [fields, initialFields, getLocalizableFields, updateTranslationStatus]);

	useEffect(() => {
		Liferay.fire('inputLocalized:localeChanged', {
			item: document.querySelector(
				`[data-languageid="${selectedLanguageId}"]`
			),
		});
	}, [selectedLanguageId]);

	const translatedItems = useMemo(
		() =>
			locales.reduce((acc, locale) => {
				const translatedItems = translations.filter(({languages}) =>
					languages.includes(locale.id)
				).length;

				return {
					...acc,
					...(translatedItems && {[locale.id]: translatedItems}),
				};
			}, {}),
		[translations, locales]
	);

	return (
		<TranslationAdminSelector
			activeLanguageIds={locales.map(({id}) => id)}
			availableLocales={locales}
			defaultLanguageId={defaultLanguageId}
			displayType="HORIZONTAL"
			onSelectedLanguageIdChange={setSelectedLanguageId}
			selectedLanguageId={selectedLanguageId}
			translationProgress={
				Object.keys(translatedItems).length
					? {
							totalItems: Object.keys(fields || initialFields)
								.length,
							translatedItems,
					  }
					: null
			}
			triggerRef={triggerRef}
		/>
	);
}

export function fieldToTranslations(fields: Record<string, Field>) {
	const translations = [];

	for (const fieldName in fields) {
		const languages = fields[fieldName]
			? (Object.keys(fields[fieldName]) as Liferay.Language.Locale[])
			: [];

		translations.push({
			fieldName,
			languages,
		});
	}

	return translations;
}
