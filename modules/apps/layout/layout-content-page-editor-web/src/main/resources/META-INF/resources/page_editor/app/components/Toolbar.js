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

import {ClayButtonWithIcon, default as ClayButton} from '@clayui/button';
import ClayLayout from '@clayui/layout';
import {useModal} from '@clayui/modal';
import {useIsMounted} from 'frontend-js-react-web';
import React, {Suspense, useCallback, useRef, useState} from 'react';
import ReactDOM from 'react-dom';

import useLazy from '../../core/hooks/useLazy';
import useLoad from '../../core/hooks/useLoad';
import usePlugins from '../../core/hooks/usePlugins';
import * as Actions from '../actions/index';
import {LAYOUT_TYPES} from '../config/constants/layoutTypes';
import {SERVICE_NETWORK_STATUS_TYPES} from '../config/constants/serviceNetworkStatusTypes';
import {config} from '../config/index';
import {useDispatch, useSelector} from '../store/index';
import redo from '../thunks/redo';
import undo from '../thunks/undo';
import {useDropClear} from '../utils/dragAndDrop/useDragAndDrop';
import {useSelectItem} from './Controls';
import EditModeSelector from './EditModeSelector';
import ExperimentsLabel from './ExperimentsLabel';
import NetworkStatusBar from './NetworkStatusBar';
import PreviewModal from './PreviewModal';
import Translation from './Translation';
import UnsafeHTML from './UnsafeHTML';
import ViewportSizeSelector from './ViewportSizeSelector';
import Undo from './undo/Undo';

function ToolbarBody() {
	const dispatch = useDispatch();
	const dropClearRef = useDropClear();
	const {getInstance, register} = usePlugins();
	const isMounted = useIsMounted();
	const load = useLoad();
	const publishForm = useRef();
	const selectItem = useSelectItem();
	const store = useSelector((state) => state);

	const {
		network,
		segmentsExperienceId,
		segmentsExperimentStatus,
		selectedViewportSize,
	} = store;

	const publishButtonDisabled =
		store.network.status === SERVICE_NETWORK_STATUS_TYPES.savingDraft ||
		config.pending;

	const [openPreviewModal, setOpenPreviewModal] = useState(false);

	const {observer} = useModal({
		onClose: () => {
			if (isMounted()) {
				setOpenPreviewModal(false);
			}
		},
	});

	const loading = useRef(() => {
		Promise.all(
			config.toolbarPlugins.map((toolbarPlugin) => {
				const {pluginEntryPoint} = toolbarPlugin;
				const promise = load(pluginEntryPoint, pluginEntryPoint);

				const app = {
					Actions,
					config,
					dispatch,
					store,
				};

				return register(pluginEntryPoint, promise, {
					app,
					toolbarPlugin,
				}).then((plugin) => {
					if (!plugin) {
						throw new Error(
							`Failed to get instance from ${pluginEntryPoint}`
						);
					}
					else if (isMounted()) {
						if (typeof plugin.activate === 'function') {
							plugin.activate();
						}
					}
				});
			})
		).catch((error) => {
			if (process.env.NODE_ENV === 'development') {
				console.error(error);
			}
		});
	});

	if (loading.current) {

		// Do this once only.

		loading.current();
		loading.current = null;
	}

	const ToolbarSection = useLazy(
		useCallback(({instance}) => {
			if (typeof instance.renderToolbarSection === 'function') {
				return instance.renderToolbarSection();
			}
			else {
				return null;
			}
		}, [])
	);

	const handleDiscardVariant = (event) => {
		if (
			!confirm(
				Liferay.Language.get(
					'are-you-sure-you-want-to-discard-current-draft-and-apply-latest-published-changes'
				)
			)
		) {
			event.preventDefault();
		}
	};

	const handlePublishButtonClick = (event) => {
		event.preventDefault();

		if (
			config.masterUsed &&
			!confirm(
				Liferay.Language.get(
					'changes-made-on-this-master-are-going-to-be-propagated-to-all-page-templates,-display-page-templates,-and-pages-using-it.are-you-sure-you-want-to-proceed'
				)
			)
		) {
			return;
		}

		// LPS-119924
		//
		// When user has pressed a submit form, we might have some extra change
		// that needs to be submitted to the backend. Sadly, with the
		// React-based store that we are using, we have no way to know if there
		// is some pending action that has to be processed, because everything
		// happens asynchronously.
		//
		// This arbitrary value allows us to wait until the client CPU has
		// processed everything and React have resolved any pending dispatch.
		// This should only happen on critical use cases like the one
		// described in the issue.

		setTimeout(() => {
			if (!publishButtonDisabled && publishForm.current) {
				publishForm.current.submit();
			}
		}, 300);
	};

	const onUndo = () => {
		dispatch(undo({store}));
	};

	const onRedo = () => {
		dispatch(redo({store}));
	};

	const deselectItem = (event) => {
		if (event.target === event.currentTarget) {
			selectItem(null);
		}
	};

	let publishButtonLabel = Liferay.Language.get('publish');

	if (config.layoutType === LAYOUT_TYPES.master) {
		publishButtonLabel = Liferay.Language.get('publish-master');
	}
	else if (config.singleSegmentsExperienceMode) {
		publishButtonLabel = Liferay.Language.get('save-variant');
	}
	else if (config.workflowEnabled) {
		publishButtonLabel = Liferay.Language.get('submit-for-publication');
	}

	return (
		<ClayLayout.ContainerFluid onClick={deselectItem} ref={dropClearRef}>
			<ul className="navbar-nav responsive-mode" onClick={deselectItem}>
				{config.toolbarPlugins.map(
					({loadingPlaceholder, pluginEntryPoint}) => {
						return (
							<li className="nav-item" key={pluginEntryPoint}>
								<ErrorBoundary>
									<Suspense
										fallback={
											<UnsafeHTML
												markup={loadingPlaceholder}
											/>
										}
									>
										<ToolbarSection
											getInstance={getInstance}
											pluginId={pluginEntryPoint}
										/>
									</Suspense>
								</ErrorBoundary>
							</li>
						);
					}
				)}
				<li className="nav-item">
					<Translation
						availableLanguages={config.availableLanguages}
						defaultLanguageId={config.defaultLanguageId}
						dispatch={dispatch}
						fragmentEntryLinks={store.fragmentEntryLinks}
						languageId={store.languageId}
						segmentsExperienceId={segmentsExperienceId}
					/>
				</li>
				{!config.singleSegmentsExperienceMode &&
					segmentsExperimentStatus && (
						<li className="nav-item pl-2">
							<ExperimentsLabel
								label={segmentsExperimentStatus.label}
								value={segmentsExperimentStatus.value}
							/>
						</li>
					)}
				<li className="nav-item">
					<ViewportSizeSelector
						onSizeSelected={(size) =>
							dispatch(Actions.switchViewportSize({size}))
						}
						selectedSize={selectedViewportSize}
					/>
				</li>
			</ul>

			<ul className="navbar-nav" onClick={deselectItem}>
				<NetworkStatusBar {...network} />
				<Undo onRedo={onRedo} onUndo={onUndo} />

				<li className="nav-item">
					<EditModeSelector />
				</li>

				<li className="nav-item">
					<ClayButtonWithIcon
						className="btn btn-secondary mr-3"
						displayType="secondary"
						onClick={() => setOpenPreviewModal(true)}
						small
						symbol="view"
						title={Liferay.Language.get('preview')}
						type="button"
					>
						{Liferay.Language.get('preview')}
					</ClayButtonWithIcon>
				</li>
				{config.singleSegmentsExperienceMode && (
					<li className="nav-item">
						<form action={config.discardDraftURL} method="POST">
							<input
								name={`${config.portletNamespace}redirect`}
								type="hidden"
								value={config.discardDraftRedirectURL}
							/>

							<ClayButton
								className="btn btn-secondary mr-3"
								displayType="secondary"
								onClick={handleDiscardVariant}
								small
								type="submit"
							>
								{Liferay.Language.get('discard-variant')}
							</ClayButton>
						</form>
					</li>
				)}

				<li className="nav-item">
					<form
						action={config.publishURL}
						method="POST"
						ref={publishForm}
					>
						<input
							name={`${config.portletNamespace}redirect`}
							type="hidden"
							value={config.redirectURL}
						/>

						<ClayButton
							disabled={publishButtonDisabled}
							displayType="primary"
							onClick={handlePublishButtonClick}
							small
							type="submit"
						>
							{publishButtonLabel}
						</ClayButton>
					</form>
				</li>
			</ul>

			{openPreviewModal && <PreviewModal observer={observer} />}
		</ClayLayout.ContainerFluid>
	);
}

class ErrorBoundary extends React.Component {
	static getDerivedStateFromError(_error) {
		return {hasError: true};
	}

	constructor(props) {
		super(props);

		this.state = {hasError: false};
	}

	componentDidCatch(error) {
		if (process.env.NODE_ENV === 'development') {
			console.error(error);
		}
	}

	render() {
		if (this.state.hasError) {
			return null;
		}
		else {
			return this.props.children;
		}
	}
}

export default function Toolbar() {
	const container = document.getElementById(config.toolbarId);
	const isMounted = useIsMounted();

	if (!isMounted()) {

		// First time here, must empty JSP-rendered markup from container.

		while (container.firstChild) {
			container.removeChild(container.firstChild);
		}
	}

	return ReactDOM.createPortal(<ToolbarBody />, container);
}
