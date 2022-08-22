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

package com.liferay.layout.element.internal;

import com.liferay.layout.element.LayoutElement;
import com.liferay.layout.element.LayoutElementTracker;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerListFactory;
import com.liferay.osgi.service.tracker.collections.map.PropertyServiceReferenceComparator;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Lourdes Fernández Besada
 */
@Component(immediate = true, service = LayoutElementTracker.class)
public class LayoutElementTrackerImpl implements LayoutElementTracker {

	@Override
	public List<LayoutElement> getLayoutElements() {
		List<LayoutElement> layoutElements = new LinkedList<>();

		for (LayoutElement layoutElement : _serviceTrackerList) {
			layoutElements.add(layoutElement);
		}

		return layoutElements;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerList = ServiceTrackerListFactory.open(
			bundleContext, LayoutElement.class,
			Collections.reverseOrder(
				new PropertyServiceReferenceComparator<>(
					"layout.element.order")));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerList.close();
	}

	private ServiceTrackerList<LayoutElement> _serviceTrackerList;

}