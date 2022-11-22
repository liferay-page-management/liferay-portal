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

package com.liferay.layout.taglib.internal.servlet.taglib;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutTemplate;
import com.liferay.portal.kernel.model.LayoutTypePortlet;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.LayoutTemplateLocalService;
import com.liferay.portal.kernel.util.LayoutTypePortletFactoryUtil;
import com.liferay.portal.util.PropsValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Matthias Bläsing
 */
@Component(immediate = true, service = GuardedLayoutResetter.class)
public class GuardedLayoutResetter implements SynchronousBundleListener {

	@Override
	public void bundleChanged(BundleEvent be) {
		if (_log.isDebugEnabled()) {
			Bundle bundle = be.getBundle();

			String bundleSymbolicName = bundle.getSymbolicName();

			_log.debug(
				String.format(
					"bundleChanged: %s: %d", bundleSymbolicName, be.getType()));
		}

		synchronized (_updater) {
			for (Map.Entry<Long, LayoutTemplateUpdater> entry :
					_updater.entrySet()) {

				ScheduledFuture<?> updateRequest = _updatesScheduled.get(
					entry.getKey());

				if (!updateRequest.cancel(false)) {
					continue;
				}

				_updatesScheduled.remove(entry.getKey());
				_updatesScheduled.put(
					entry.getKey(),
					_scheduledExecutorService.schedule(
						entry.getValue(), _REDEPLOYMENT_GRACE_PERIOD,
						TimeUnit.MILLISECONDS));
			}
		}
	}

	public void checkAndResetTemplatteId(long plid) {
		synchronized (_updater) {
			if (!_updater.containsKey(plid)) {
				LayoutTemplateUpdater layoutTemplateUpdater =
					new LayoutTemplateUpdater(plid);

				_updater.put(plid, layoutTemplateUpdater);
				_updatesScheduled.put(
					plid,
					_scheduledExecutorService.schedule(
						layoutTemplateUpdater, _REDEPLOYMENT_GRACE_PERIOD,
						TimeUnit.MILLISECONDS));
			}
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;

		bundleContext.addBundleListener(this);
	}

	@Deactivate
	protected void deactivate() {
		_bundleContext.removeBundleListener(this);

		synchronized (_updater) {
			for (Long plid : new ArrayList<>(_updater.keySet())) {
				ScheduledFuture<?> updateRequest = _updatesScheduled.get(plid);

				updateRequest.cancel(false);
			}

			_updater.clear();
			_updatesScheduled.clear();
		}
	}

	private static final long _REDEPLOYMENT_GRACE_PERIOD = 1L * 60L * 1000L;

	private static final Log _log = LogFactoryUtil.getLog(
		GuardedLayoutResetter.class);

	private BundleContext _bundleContext;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private LayoutTemplateLocalService _layoutTemplateLocalService;

	private final ScheduledExecutorService _scheduledExecutorService =
		Executors.newScheduledThreadPool(0);
	private final Map<Long, LayoutTemplateUpdater> _updater = new HashMap<>();
	private final Map<Long, ScheduledFuture<?>> _updatesScheduled =
		new HashMap<>();

	private class LayoutTemplateUpdater implements Runnable {

		public LayoutTemplateUpdater(long plid) {
			_plid = plid;
		}

		public void run() {
			try {
				Layout layout = _layoutLocalService.getLayout(_plid);

				LayoutTypePortlet layoutTypePortlet =
					LayoutTypePortletFactoryUtil.create(layout);

				if (layoutTypePortlet.getLayoutTemplateId() == null) {
					return;
				}

				LayoutTemplate layoutTemplate =
					_layoutTemplateLocalService.getLayoutTemplate(
						layoutTypePortlet.getLayoutTemplateId(), false,
						layout.getThemeId());

				if (layoutTemplate != null) {
					if (_log.isDebugEnabled()) {
						_log.debug(
							String.format(
								_LAYOUT_FOUND_LOG_MSG, _plid,
								layout.getThemeId(),
								layoutTypePortlet.getLayoutTemplateId()));
					}

					return;
				}

				layoutTypePortlet.setLayoutTemplateId(
					layout.getUserId(), PropsValues.DEFAULT_LAYOUT_TEMPLATE_ID);

				_layoutLocalService.updateLayout(
					layout.getGroupId(), layout.isPrivateLayout(),
					layout.getLayoutId(), layout.getTypeSettings());

				if (_log.isInfoEnabled()) {
					String logInfo = String.format(
						_RESET_LAYOUT_LOG_MSG, _plid, layout.getGroupId(),
						layout.isPrivateLayout(), layout.getLayoutId());

					_log.info(logInfo);
				}
			}
			catch (Throwable throwable) {
				if (_log.isWarnEnabled()) {
					_log.warn(throwable);
				}
			}
			finally {
				synchronized (_updater) {
					_updater.remove(_plid);
					_updatesScheduled.remove(_plid);
				}
			}
		}

		private static final String _LAYOUT_FOUND_LOG_MSG =
			"Layout template found (plid: %d, themeId: %s, layoutTemplateId: " +
				"%s)";

		private static final String _RESET_LAYOUT_LOG_MSG =
			"Resetting layout to default (plid: %d, groupId: %d, " +
				"privateLayout: %b, layoutId: %d)";

		private final long _plid;

	}

}