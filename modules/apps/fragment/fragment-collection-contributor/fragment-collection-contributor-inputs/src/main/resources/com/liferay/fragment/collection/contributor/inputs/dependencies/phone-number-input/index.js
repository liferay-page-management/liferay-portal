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

const COUNTRIES = [
	{a2: 'US', idd: '1', name: 'United States'},
	{a2: 'GB', idd: '44', name: 'United Kingdom'},
	{a2: 'DE', idd: '49', name: 'Germany'},
	{a2: 'FR', idd: '33', name: 'France'},
	{a2: 'IT', idd: '39', name: 'Italy'},
	{a2: 'ES', idd: '34', name: 'Spain'},
	{a2: 'PT', idd: '351', name: 'Portugal'},
	{a2: 'BR', idd: '55', name: 'Brazil'},
	{a2: 'JP', idd: '81', name: 'Japan'},
	{a2: 'CN', idd: '86', name: 'China'},
	{a2: 'IN', idd: '91', name: 'India'},
	{a2: 'AU', idd: '61', name: 'Australia'},
	{a2: 'CA', idd: '1', name: 'Canada'},
	{a2: 'MX', idd: '52', name: 'Mexico'},
	{a2: 'AR', idd: '54', name: 'Argentina'},
	{a2: 'CL', idd: '56', name: 'Chile'},
	{a2: 'CO', idd: '57', name: 'Colombia'},
	{a2: 'KR', idd: '82', name: 'South Korea'},
	{a2: 'NL', idd: '31', name: 'Netherlands'},
	{a2: 'BE', idd: '32', name: 'Belgium'},
	{a2: 'CH', idd: '41', name: 'Switzerland'},
	{a2: 'AT', idd: '43', name: 'Austria'},
	{a2: 'SE', idd: '46', name: 'Sweden'},
	{a2: 'NO', idd: '47', name: 'Norway'},
	{a2: 'DK', idd: '45', name: 'Denmark'},
	{a2: 'FI', idd: '358', name: 'Finland'},
	{a2: 'PL', idd: '48', name: 'Poland'},
	{a2: 'IE', idd: '353', name: 'Ireland'},
	{a2: 'NZ', idd: '64', name: 'New Zealand'},
	{a2: 'SG', idd: '65', name: 'Singapore'},
	{a2: 'ZA', idd: '27', name: 'South Africa'},
	{a2: 'RU', idd: '7', name: 'Russia'},
	{a2: 'TR', idd: '90', name: 'Turkey'},
	{a2: 'IL', idd: '972', name: 'Israel'},
	{a2: 'AE', idd: '971', name: 'United Arab Emirates'},
	{a2: 'SA', idd: '966', name: 'Saudi Arabia'},
	{a2: 'TH', idd: '66', name: 'Thailand'},
	{a2: 'PH', idd: '63', name: 'Philippines'},
	{a2: 'ID', idd: '62', name: 'Indonesia'},
	{a2: 'MY', idd: '60', name: 'Malaysia'},
];

function getFlag(a2) {
	return String.fromCodePoint(
		...[...a2.toUpperCase()].map((c) => 0x1f1e6 + c.charCodeAt(0) - 65)
	);
}

const COUNTRIES_BY_IDD_LENGTH = [...COUNTRIES].sort(
	(a, b) => b.idd.length - a.idd.length
);

function parsePhoneValue(value) {
	if (!value || !value.startsWith('+')) {
		return {countryA2: '', localNumber: value || ''};
	}

	const digits = value.slice(1);

	for (const country of COUNTRIES_BY_IDD_LENGTH) {
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

	COUNTRIES.sort((a, b) => a.name.localeCompare(b.name));

	COUNTRIES.forEach((country) => {
		const option = document.createElement('option');

		option.value = country.a2;
		option.setAttribute('data-idd', country.idd);
		option.textContent =
			getFlag(country.a2) + ' +' + country.idd + ' ' + country.name;
		countrySelect.appendChild(option);
	});

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
