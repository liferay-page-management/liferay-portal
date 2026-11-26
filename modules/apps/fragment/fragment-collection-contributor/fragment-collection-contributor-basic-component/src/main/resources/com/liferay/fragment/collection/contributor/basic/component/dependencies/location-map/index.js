const container = document.getElementById(`${fragmentElementId}-location-map`);

function getPopover() {
	if (layoutMode === 'edit') {
		return configuration.keepPopoverOpenInEditMode ? 'always' : undefined;
	}

	return configuration.displayPopoverOnHover ? 'hover' : undefined;
}

if (container) {
	const {
		address,
		googleMapsApiKey,
		latitude,
		longitude,
		mapProviderKey,
		title,
	} = container.dataset;

	import('@liferay/fragment-impl/api').then(({renderLocationMap}) => {
		renderLocationMap(container, {
			googleMapsAPIKey: googleMapsApiKey,
			id: `${fragmentElementId}-location-map`,
			mapProviderKey,
			popover: getPopover(),
			title,
			value: {
				address,
				coordinates: {
					latitude: Number(latitude),
					longitude: Number(longitude),
				},
			},
		});
	});
}
