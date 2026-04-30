/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page} from '@playwright/test';

import {backendPageTest} from './backendPageTest';

const ANALYTICS_CONFIGURATION_PID =
	'com.liferay.analytics.settings.configuration.AnalyticsConfiguration';

const HEADLESS_ADMIN_CONFIGURATION_FEATURE_FLAG = 'LPD-65399';

const HEADLESS_ADMIN_CONFIGURATION_FEATURE_FLAG_DEPENDENCY = 'LPS-155284';

const STUB_PROPERTIES = {
	liferayAnalyticsDataSourceId: 'playwright-stub-data-source',
	liferayAnalyticsFaroBackendSecuritySignature: 'playwright-stub-signature',
	liferayAnalyticsFaroBackendURL: 'http://playwright-stub.invalid',
};

/**
 * Activate Analytics Cloud for the current company by stubbing the three
 * config properties AnalyticsSettingsManager.isAnalyticsEnabled() requires.
 *
 * Self-contained: toggles its own feature flags (LPD-65399 + dependency
 * LPS-155284) via the feature-flag-web REST endpoint to avoid colliding
 * with the caller's own featureFlagsTest fixture, and writes the stub
 * AnalyticsConfiguration via the headless-admin-configuration REST API.
 */
const analyticsCloudConnectedTest = backendPageTest.extend<{
	analyticsCloudConnected: void;
}>({
	analyticsCloudConnected: [
		async ({backendPage}, use) => {
			const originalDependencyEnabled = await _getFeatureFlagEnabled(
				backendPage,
				HEADLESS_ADMIN_CONFIGURATION_FEATURE_FLAG_DEPENDENCY
			);

			const originalFlagEnabled = await _getFeatureFlagEnabled(
				backendPage,
				HEADLESS_ADMIN_CONFIGURATION_FEATURE_FLAG
			);

			await _setFeatureFlagEnabled(
				backendPage,
				HEADLESS_ADMIN_CONFIGURATION_FEATURE_FLAG_DEPENDENCY,
				true
			);

			await _setFeatureFlagEnabled(
				backendPage,
				HEADLESS_ADMIN_CONFIGURATION_FEATURE_FLAG,
				true
			);

			const originalProperties =
				await _getInstanceConfigurationProperties(backendPage);

			try {
				await _putInstanceConfigurationProperties(
					backendPage,
					STUB_PROPERTIES
				);

				await use(undefined);
			}
			finally {
				await _putInstanceConfigurationProperties(
					backendPage,
					originalProperties
				);

				await _setFeatureFlagEnabled(
					backendPage,
					HEADLESS_ADMIN_CONFIGURATION_FEATURE_FLAG,
					originalFlagEnabled
				);

				await _setFeatureFlagEnabled(
					backendPage,
					HEADLESS_ADMIN_CONFIGURATION_FEATURE_FLAG_DEPENDENCY,
					originalDependencyEnabled
				);
			}
		},
		{auto: true},
	],
});

async function _getFeatureFlagEnabled(
	backendPage: Page,
	key: string
): Promise<boolean> {
	return await backendPage.evaluate(async (key) => {
		const response = await Liferay.Util.fetch(
			'/o/com-liferay-feature-flag-web/is-enabled',
			{
				body: Liferay.Util.objectToFormData({
					companyId: Number(Liferay.ThemeDisplay.getCompanyId()),
					key,
				}),
				method: 'POST',
			}
		);

		if (!response?.ok) {
			return false;
		}

		const json = await response.json();

		return Boolean(json?.featureFlag?.enabled);
	}, key);
}

async function _getInstanceConfigurationProperties(
	backendPage: Page
): Promise<Record<string, unknown>> {
	return await backendPage.evaluate(async (pid) => {
		const response = await Liferay.Util.fetch(
			`/o/headless-admin-configuration/v1.0/instance-configurations/${pid}`
		);

		if (!response?.ok) {
			return {};
		}

		const json = await response.json();

		return json.properties ?? {};
	}, ANALYTICS_CONFIGURATION_PID);
}

async function _putInstanceConfigurationProperties(
	backendPage: Page,
	properties: Record<string, unknown>
): Promise<void> {
	const error = await backendPage.evaluate(
		async ({pid, properties}) => {
			const response = await Liferay.Util.fetch(
				`/o/headless-admin-configuration/v1.0/instance-configurations/${pid}`,
				{
					body: JSON.stringify({
						externalReferenceCode: pid,
						properties,
					}),
					headers: {
						'Content-Type': 'application/json',
					},
					method: 'PUT',
				}
			);

			if (!response?.ok) {
				return `${response?.status} ${await response?.text()}`;
			}

			return null;
		},
		{pid: ANALYTICS_CONFIGURATION_PID, properties}
	);

	if (error) {
		throw new Error(
			`Failed to PUT ${ANALYTICS_CONFIGURATION_PID}: ${error}`
		);
	}
}

async function _setFeatureFlagEnabled(
	backendPage: Page,
	key: string,
	enabled: boolean
): Promise<void> {
	await backendPage.evaluate(
		async ({enabled, key}) => {
			await Liferay.Util.fetch(
				'/o/com-liferay-feature-flag-web/set-enabled',
				{
					body: Liferay.Util.objectToFormData({
						companyId: Number(Liferay.ThemeDisplay.getCompanyId()),
						enabled,
						key,
					}),
					method: 'POST',
				}
			);
		},
		{enabled, key}
	);
}

export {analyticsCloudConnectedTest};
