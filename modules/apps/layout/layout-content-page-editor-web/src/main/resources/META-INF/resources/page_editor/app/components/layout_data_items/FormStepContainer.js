/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React from 'react';

import {getLayoutDataItemPropTypes} from '../../../prop_types/index';
import getLayoutDataItemTopperUniqueClassName from '../../utils/getLayoutDataItemTopperUniqueClassName';
import TopperEmpty from '../topper/TopperEmpty';

const FormStepContainerWithControls = React.forwardRef(
	({children, item}, ref) => {
		return (
			<TopperEmpty
				className={getLayoutDataItemTopperUniqueClassName(item.itemId)}
				item={item}
			>
				<FormStepContainer ref={ref}>{children}</FormStepContainer>
			</TopperEmpty>
		);
	}
);

FormStepContainerWithControls.displayName = 'FormStepContainer';

FormStepContainerWithControls.propTypes = {
	item: getLayoutDataItemPropTypes().isRequired,
};

const FormStepContainer = React.forwardRef(({children}, ref) => {
	return (
		<div className="page-editor__form-step-container" ref={ref}>
			{children}
		</div>
	);
});

FormStepContainer.displayName = 'FormStepContainer';

FormStepContainer.propTypes = {
	item: getLayoutDataItemPropTypes().isRequired,
};

export {FormStepContainer, FormStepContainerWithControls};
