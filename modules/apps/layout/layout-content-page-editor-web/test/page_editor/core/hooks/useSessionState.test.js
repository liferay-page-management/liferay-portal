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

import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import {useSessionState} from '../../../../src/main/resources/META-INF/resources/page_editor/core/hooks/useSessionState';

const renderHook = ({defaultValue = null, key}) => {
	const Component = () => {
		const [value, setValue] = useSessionState(key, defaultValue);

		return (
			<button onClick={() => setValue(1234)} type="button">
				{value}
			</button>
		);
	};

	render(<Component />);
};

describe('useSessionState', () => {
	beforeEach(() => {
		jest.spyOn(window.sessionStorage.__proto__, 'getItem');
		jest.spyOn(window.sessionStorage.__proto__, 'setItem');
	});

	afterEach(() => {
		jest.restoreAllMocks();
	});

	it('gets initial value from session storage', async () => {
		window.sessionStorage.getItem.mockImplementation(() =>
			JSON.stringify('hey!')
		);

		renderHook({key: 'key'});
		await screen.findByText('hey!');
	});

	it('uses given default value is there is nothing in sessionStorage', async () => {
		renderHook({defaultValue: 'default', key: 'key'});
		await screen.findByText('default');
	});

	it('updates sessionStorage when value is updated', async () => {
		renderHook({defaultValue: 'default', key: 'key'});

		const button = await screen.findByText('default');

		userEvent.click(button);

		expect(window.sessionStorage.setItem).toHaveBeenCalledWith(
			'key',
			JSON.stringify(1234)
		);
	});
});
