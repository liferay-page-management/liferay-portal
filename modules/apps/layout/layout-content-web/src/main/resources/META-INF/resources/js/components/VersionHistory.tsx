/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLoadingIndicator from '@clayui/loading-indicator';
import {
	hideProductMenuIfPresent,
	useMediaQuery,
} from '@liferay/layout-js-components-web';
import {openToast} from 'frontend-js-components-web';
import {sub} from 'frontend-js-web';
import React, {useCallback, useEffect, useState} from 'react';

import {Config, initializeConfig} from '../config';
import PageVersionService from '../services/PageVersionService';
import {PageVersion} from '../types/PageVersion';
import DeleteVersionModal from './DeleteVersionModal';
import ResponsivePanel from './ResponsivePanel';
import Toolbar from './Toolbar';
import VersionList from './VersionList';

const LARGE_MEDIA_QUERY = '(min-width: 992px)';

interface Props {
	config: Config;
}

export default function VersionHistory({config}: Props) {
	initializeConfig(config);

	const [isPanelOpen, setIsPanelOpen] = useState(false);
	const [search, setSearch] = useState('');

	const [versions, setVersions] = useState<PageVersion[] | null>(null);
	const [versionToDelete, setVersionToDelete] = useState<PageVersion | null>(
		null
	);

	const isScreenLarge = useMediaQuery(LARGE_MEDIA_QUERY);

	useEffect(() => {
		hideProductMenuIfPresent({onHide: () => setIsPanelOpen(true)});
	}, []);

	const loadVersions = useCallback(async (signal?: AbortSignal) => {
		const {data, error} = await PageVersionService.getPageVersions(signal);

		if (signal?.aborted) {
			return;
		}

		if (error) {
			openToast({message: error, type: 'danger'});
		}

		setVersions(data?.items ?? []);
	}, []);

	useEffect(() => {
		const controller = new AbortController();

		loadVersions(controller.signal);

		return () => controller.abort();
	}, [loadVersions]);

	const handleConfirmDelete = async () => {
		if (!versionToDelete?.actions?.delete) {
			return;
		}

		const {error} = await PageVersionService.deletePageVersion(
			versionToDelete.actions.delete.href
		);

		setVersionToDelete(null);

		if (error) {
			openToast({message: error, type: 'danger'});

			return;
		}

		openToast({
			message: sub(Liferay.Language.get('x-was-deleted-successfully'), [
				versionToDelete.name,
			]),
			type: 'success',
		});

		loadVersions();
	};

	const keywords = search.trim().toLowerCase();

	const matches = (...names: Array<string | undefined>) =>
		names.some((name) => name?.toLowerCase().includes(keywords));

	return (
		<>
			<Toolbar
				isSidePanelOpen={isPanelOpen || isScreenLarge}
				openSidePanel={() => setIsPanelOpen(true)}
			/>

			<ResponsivePanel
				onOpenChange={setIsPanelOpen}
				onSearch={setSearch}
				open={isPanelOpen || isScreenLarge}
			>
				{versions ? (
					<VersionList
						layout={
							matches(config.layout.name)
								? config.layout
								: undefined
						}
						onDelete={setVersionToDelete}
						searching={Boolean(keywords)}
						versions={versions.filter(({creator, name}) =>
							matches(name, creator?.name)
						)}
					/>
				) : (
					<ClayLoadingIndicator
						displayType="secondary"
						size="sm"
						title={Liferay.Language.get('loading')}
					/>
				)}
			</ResponsivePanel>

			{versionToDelete ? (
				<DeleteVersionModal
					onClose={() => setVersionToDelete(null)}
					onConfirm={handleConfirmDelete}
				/>
			) : null}
		</>
	);
}
