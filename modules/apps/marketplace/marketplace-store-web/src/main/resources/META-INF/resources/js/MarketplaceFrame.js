/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

function receiveMessage(event, frame) {
	const response = JSON.parse(
		'{"' + event.data.replace(/&/g, '","').replace(/=/g, '":"') + '"}'
	);

	if (response) {
		if (response.cmd === 'resize' || response.cmd === 'init') {
			if (response.height) {
				frame.style.height = (+response.height + 50).toString() + 'px';
			}

			if (response.width) {
				frame.style.width = response.width + 'px';
			}
		}
		else if (response.cmd === 'scrollTo') {
			const scrollX = response.scrollX || 0;
			const scrollY = response.scrollY || 0;

			window.scrollTo(scrollX, scrollY);
		}
		else if (response.cmd === 'goto') {
			let url = '<%= themeDisplay.getURLControlPanel() %>';

			if (response.panel === 'purchased') {
				url =
					'<liferay-portlet:renderURL doAsGroupId="<%= themeDisplay.getScopeGroupId() %>" portletName="<%= MarketplaceStorePortletKeys.MARKETPLACE_PURCHASED %>" windowState="<%= LiferayWindowState.MAXIMIZED.toString() %>" />';
			}
			else if (response.panel === 'store') {
				url =
					'<liferay-portlet:renderURL doAsGroupId="<%= themeDisplay.getScopeGroupId() %>" portletName="<%= MarketplaceStorePortletKeys.MARKETPLACE_STORE %>" windowState="<%= LiferayWindowState.MAXIMIZED.toString() %>" />';

				if (response.appEntryId) {
					url = Liferay.Util.addParams(
						'<%= PortalUtil.getPortletNamespace(MarketplaceStorePortletKeys.MARKETPLACE_STORE) %>appEntryId=' +
							response.appEntryId,
						url
					);
				}
			}

			window.location = url;
		}
	}
}

export default function ({namespace: portletNamespace}) {
	const targetFrame = document.getElementById(`${portletNamespace}frame`);
	window.addEventListener('message', (event) => {
		receiveMessage(event, targetFrame);
	});

	return () => {
		window.removeEventListener('message', receiveMessage);
	};
}
