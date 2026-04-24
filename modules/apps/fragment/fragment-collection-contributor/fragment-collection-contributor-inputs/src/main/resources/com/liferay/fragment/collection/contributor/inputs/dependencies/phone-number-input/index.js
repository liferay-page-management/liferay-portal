const countrySelect = document.getElementById(
	`${fragmentElementId}-country-select`
);
const displayInput = document.getElementById(
	`${fragmentElementId}-phone-display`
);
const formGroup = document.getElementById(`${fragmentElementId}-form-group`);
const inputsContainer = document.getElementById(
	`${fragmentElementId}-inputs-container`
);

function getCountriesByIddLength() {
	const countries = [];

	for (const option of countrySelect.options) {
		const idd = option.getAttribute('data-idd');

		if (idd) {
			countries.push({a2: option.value, idd});
		}
	}

	return countries.sort((a, b) => b.idd.length - a.idd.length);
}

function parsePhoneValue(value) {
	if (!value || !value.startsWith('+')) {
		return {countryA2: '', localNumber: value || ''};
	}

	const digits = value.slice(1);

	for (const country of getCountriesByIddLength()) {
		if (digits.startsWith(country.idd)) {
			return {
				countryA2: country.a2,
				localNumber: digits.slice(country.idd.length),
			};
		}
	}

	return {countryA2: '', localNumber: value};
}

function getCombinedValue() {
	const selectedOption = countrySelect.options[countrySelect.selectedIndex];
	const idd = selectedOption ? selectedOption.getAttribute('data-idd') : '';
	const number = displayInput.value;

	if (idd && number) {
		return '+' + idd + number;
	}

	return number;
}

function updateDisplayFromCombined(combinedValue) {
	const parsed = parsePhoneValue(combinedValue || '');

	countrySelect.value = parsed.countryA2;
	displayInput.value = parsed.localNumber;
}

function main() {
	if (!displayInput || !countrySelect) {
		return;
	}

	const currentValue = displayInput.value || '';

	updateDisplayFromCombined(currentValue);

	if (layoutMode === 'edit') {
		displayInput.setAttribute('disabled', true);
		countrySelect.setAttribute('disabled', true);
	}
	else {
		const defaultLanguageId = themeDisplay.getDefaultLanguageId();

		import('@liferay/fragment-impl/api').then(
			({
				focusInput,
				registerLocalizedInput,
				registerUnlocalizedInput,
			}) => {
				const hasError = formGroup.classList.contains('has-error');

				if (hasError) {
					focusInput(displayInput);
				}

				displayInput.addEventListener('input', () => {
					const filtered = displayInput.value.replace(
						/[^0-9\s\-().]/g,
						''
					);

					if (filtered !== displayInput.value) {
						displayInput.value = filtered;
					}
				});

				if (input.localizable) {
					const {onChange} = registerLocalizedInput({
						defaultLanguageId,
						initialValues: input.valueI18n,
						inputElement: displayInput,
						inputName: input.name,
						localizationInputsContainer: inputsContainer,
						namespace: fragmentElementId,
					});

					displayInput.addEventListener('change', () => {
						const combined = getCombinedValue();

						onChange(combined);
					});

					countrySelect.addEventListener('change', () => {
						const combined = getCombinedValue();

						onChange(combined);
					});

					Liferay.on(
						'localizationSelect:localeChanged',
						() => {
							requestAnimationFrame(() => {
								updateDisplayFromCombined(
									displayInput.value
								);
							});
						}
					);
				}
				else {
					registerUnlocalizedInput({
						defaultLanguageId,
						inputElement: displayInput,
						readOnlyInputLabel: document.getElementById(
							`${fragmentElementId}-phone-input-readonly`
						),
						unlocalizedFieldsState:
							input.attributes.unlocalizedFieldsState,
						unlocalizedMessageContainer: document.getElementById(
							`${fragmentElementId}-unlocalized-info`
						),
					});

					displayInput.closest('form')?.addEventListener(
						'submit',
						() => {
							displayInput.value = getCombinedValue();
						},
						true
					);
				}
			}
		);
	}
}

main();
