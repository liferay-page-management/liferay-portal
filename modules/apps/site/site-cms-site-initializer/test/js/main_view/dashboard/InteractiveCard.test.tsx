/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {TrendClassification} from '@liferay/analytics-reports-js-components-web';
import {render, screen} from '@testing-library/react';
import React from 'react';

import InteractiveCard from '../../../../src/main/resources/META-INF/resources/js/main_view/dashboard/performance/components/InteractiveCard';

const trend = {
	classification: TrendClassification.Positive,
	percentage: 22.5,
};

const renderComponent = (props: React.ComponentProps<typeof InteractiveCard>) =>
	render(<InteractiveCard {...props} />);

describe('InteractiveCard', () => {
	it('renders the title and value when not loading', () => {
		renderComponent({
			color: 'purple',
			icon: 'low-vision',
			title: 'Impressions',
			trend,
			value: '31.9k',
		});

		expect(screen.getByText('Impressions')).toBeInTheDocument();
		expect(screen.getByText('31.9k')).toBeInTheDocument();
	});

	it('hides the value while loading', () => {
		renderComponent({
			color: 'purple',
			icon: 'low-vision',
			loading: true,
			title: 'Impressions',
			trend,
			value: '31.9k',
		});

		expect(screen.getByText('Impressions')).toBeInTheDocument();
		expect(screen.queryByText('31.9k')).not.toBeInTheDocument();
	});

	it('renders a zero value through the metric rather than as bare text', () => {
		const {container} = renderComponent({
			color: 'red',
			icon: 'link',
			title: 'Broken Links',
			value: 0,
		});

		const metric = container.querySelector(
			'.cms-dashboard__interactive-card__metric'
		);

		expect(metric).not.toBeEmptyDOMElement();
		expect(metric?.firstElementChild).toHaveClass('text-lowercase');
		expect(screen.getByText('0')).toBeInTheDocument();
	});

	it('renders no metric when there is no value', () => {
		const {container} = renderComponent({
			color: 'red',
			icon: 'link',
			title: 'Broken Links',
		});

		expect(
			container.querySelector('.cms-dashboard__interactive-card__metric')
		).toBeEmptyDOMElement();
	});

	it('renders the custom content below the value', () => {
		renderComponent({
			children: <span>2.6% of 350 assets</span>,
			title: 'Long-Standing Drafts',
			value: 9,
		});

		expect(screen.getByText('2.6% of 350 assets')).toBeInTheDocument();
	});

	it('hides the custom content while loading', () => {
		renderComponent({
			children: <span>2.6% of 350 assets</span>,
			loading: true,
			title: 'Long-Standing Drafts',
			value: 9,
		});

		expect(
			screen.queryByText('2.6% of 350 assets')
		).not.toBeInTheDocument();
	});

	it('marks the card as active', () => {
		renderComponent({
			active: true,
			color: 'dark',
			icon: 'view',
			title: 'Views',
			trend,
			value: '18.1k',
		});

		expect(screen.getByRole('button')).toHaveClass('active');
	});
});
