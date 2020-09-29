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

package com.liferay.layout.content.page.editor.web.internal.adaptive.media;

import com.liferay.adaptive.media.content.transformer.ContentTransformerHandler;
import com.liferay.adaptive.media.content.transformer.constants.ContentTransformerContentTypes;
import com.liferay.layout.adaptive.media.LayoutAdaptiveMediaProcessor;
import com.liferay.layout.content.page.editor.web.internal.configuration.FFLayoutAdaptiveMediaConfiguration;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Pavel Savinov
 */
@Component(
	configurationPid = "com.liferay.layout.content.page.editor.web.internal.configuration.FFLayoutAdaptiveMediaConfiguration",
	immediate = true, service = LayoutAdaptiveMediaProcessor.class
)
public class LayoutAdaptiveMediaProcessorImpl
	implements LayoutAdaptiveMediaProcessor {

	@Override
	public String processAdaptiveMediaContent(String content) {
		if (!_ffLayoutAdaptiveMediaConfiguration.adaptiveMediaEnabled()) {
			return content;
		}

		return _contentTransformerHandler.transform(
			ContentTransformerContentTypes.HTML, content);
	}

	@Modified
	protected void activate(Map<String, Object> properties) {
		_ffLayoutAdaptiveMediaConfiguration =
			ConfigurableUtil.createConfigurable(
				FFLayoutAdaptiveMediaConfiguration.class, properties);
	}

	@Reference
	private ContentTransformerHandler _contentTransformerHandler;

	private volatile FFLayoutAdaptiveMediaConfiguration
		_ffLayoutAdaptiveMediaConfiguration;

}