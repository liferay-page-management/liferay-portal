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

import PropTypes from 'prop-types';
import React from 'react';

import {getLayoutDataItemPropTypes} from '../../../prop-types/index';
import {config} from '../../config/index';
import selectSegmentsExperienceId from '../../selectors/selectSegmentsExperienceId';
import {useDispatch, useSelector} from '../../store/index';
import updateItemConfig from '../../thunks/updateItemConfig';
import {SelectField} from '../fragment-configuration-fields/SelectField';
import {FieldSet} from './FieldSet';

export const ContainerStylesPanel = ({item}) => {
	const dispatch = useDispatch();
	const segmentsExperienceId = useSelector(selectSegmentsExperienceId);

	const {commonStyles} = config;

	const onCustomStyleValueSelect = (name, value) => {
		const itemConfig = {[name]: value};

		dispatch(
			updateItemConfig({
				itemConfig,
				itemId: item.itemId,
				segmentsExperienceId,
			})
		);
	};

	const onCommonStyleValueSelect = (name, value) => {
		const styles = {[name]: value};

		dispatch(
			updateItemConfig({
				itemConfig: {styles},
				itemId: item.itemId,
				segmentsExperienceId,
			})
		);
	};

	return (
		<>
			<div className="page-editor__container-styles-panel__custom-styles">
				<SelectField
					field={{
						label: Liferay.Language.get('container-width'),
						name: 'widthType',
						typeOptions: {
							validValues: [
								{
									label: Liferay.Language.get('fluid'),
									value: 'fluid',
								},
								{
									label: Liferay.Language.get('fixed-width'),
									value: 'fixed',
								},
							],
						},
					}}
					onValueSelect={onCustomStyleValueSelect}
					value={item.config.widthType}
				/>
			</div>

			<div className="page-editor__container-styles-panel__common-styles">
				{commonStyles.map((fieldSet, index) => {
					return (
						<FieldSet
							fields={fieldSet.styles}
							key={index}
							label={fieldSet.label}
							onValueSelect={onCommonStyleValueSelect}
							values={item.config.styles}
						/>
					);
				})}
			</div>
		</>
	);
};

ContainerStylesPanel.propTypes = {
	item: getLayoutDataItemPropTypes({
		config: PropTypes.shape({
			styles: PropTypes.object,
		}),
	}),
};
