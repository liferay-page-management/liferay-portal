const inputElement = document.getElementById(
	`${fragmentNamespace}-rich-text-input`
);

if (input.attributes?.readOnly) {
	if (inputElement) {
		const parser = new DOMParser();
		const html = parser.parseFromString(input.value, 'text/html');

		inputElement.appendChild(html.documentElement);
	}
}
else if (layoutMode === 'edit') {
	if (inputElement) {
		inputElement.setAttribute('disabled', true);
	}
}
