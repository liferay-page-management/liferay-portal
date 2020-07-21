/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import ClayTabs from '@clayui/tabs';
import React, {useEffect, useState} from 'react';

import {useActiveItemId} from '../../../app/components/Controls';
import {useSelectorCallback} from '../../../app/store/index';
import {deepEqual} from '../../../app/utils/checkDeepEqual';
import {useId} from '../../../app/utils/useId';
import {useItemConfigurationPanels} from './ItemConfigurationContext';

const PANELS = {};

export default function ItemConfiguration() {
	const activeItemId = useActiveItemId();
	const activeItem = useSelectorCallback(
		(state) => state.layoutData.items[activeItemId],
		[activeItemId],
		deepEqual
	);

	const [activePanelIndex, setActivePanelIndex] = useState(null);
	const activePanels = useItemConfigurationPanels(activeItem);
	const tabIdPrefix = useId();
	const panelIdPrefix = useId();

	const panels = Object.entries(activePanels)
		.filter(([panelId, show]) => show && PANELS[panelId])
		.map(([panelId]) => ({...PANELS[panelId], panelId}));

	useEffect(() => {
		setActivePanelIndex(0);
	}, [activePanels]);

	return (
		<div className="page-editor__page-structure__item-configuration">
			<ClayTabs className="border-bottom" modern>
				{panels.map((panel, index) => (
					<ClayTabs.Item
						active={activePanelIndex === index}
						innerProps={{
							'aria-controls': `${panelIdPrefix}-${panel.panelId}`,
							id: `${tabIdPrefix}-${panel.panelId}`,
						}}
						key={panel.panelId}
						onClick={() => setActivePanelIndex(index)}
					>
						<span className="page-editor__page-structure__item-configuration-tab">
							{panel.label}
						</span>
					</ClayTabs.Item>
				))}
			</ClayTabs>

			<ClayTabs.Content activeIndex={activePanelIndex}>
				{panels.map((panel) => {
					const Component = panel.component;

					return (
						<ClayTabs.TabPane
							aria-labelledby={`${tabIdPrefix}-${panel.panelId}`}
							className="p-3"
							id={`${panelIdPrefix}-${panel.panelId}`}
							key={panel.panelId}
						>
							<Component item={activeItem} />
						</ClayTabs.TabPane>
					);
				})}
			</ClayTabs.Content>
		</div>
	);
}
