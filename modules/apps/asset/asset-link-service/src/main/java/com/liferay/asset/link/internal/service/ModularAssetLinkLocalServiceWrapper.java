/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.link.internal.service;

import com.liferay.asset.kernel.model.AssetLink;
import com.liferay.asset.kernel.service.AssetLinkLocalServiceWrapper;
import com.liferay.asset.link.service.AssetLinkLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.model.adapter.ModelAdapterUtil;

import java.util.Date;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Eudaldo Aloso
 */
@Component(service = ServiceWrapper.class)
public class ModularAssetLinkLocalServiceWrapper
	extends AssetLinkLocalServiceWrapper {

	@Override
	public AssetLink addLink(
			long userId, long entryId1, long entryId2, int type, int weight)
		throws PortalException {

		return ModelAdapterUtil.adapt(
			AssetLink.class,
			_assetLinkLocalService.addLink(
				userId, entryId1, entryId2, type, weight));
	}

	@Override
	public AssetLink deleteAssetLink(AssetLink assetLink) {
		return ModelAdapterUtil.adapt(
			AssetLink.class,
			_assetLinkLocalService.deleteAssetLink(
				ModelAdapterUtil.adapt(
					com.liferay.asset.link.model.AssetLink.class, assetLink)));
	}

	@Override
	public AssetLink deleteAssetLink(long linkId) throws PortalException {
		return ModelAdapterUtil.adapt(
			AssetLink.class, _assetLinkLocalService.deleteAssetLink(linkId));
	}

	@Override
	public void deleteGroupLinks(long groupId) {
		_assetLinkLocalService.deleteGroupLinks(groupId);
	}

	@Override
	public void deleteLink(AssetLink link) {
		_assetLinkLocalService.deleteLink(
			ModelAdapterUtil.adapt(
				com.liferay.asset.link.model.AssetLink.class, link));
	}

	@Override
	public void deleteLink(long linkId) throws PortalException {
		_assetLinkLocalService.deleteLink(linkId);
	}

	@Override
	public void deleteLinks(long entryId) {
		_assetLinkLocalService.deleteLinks(entryId);
	}

	@Override
	public void deleteLinks(long entryId1, long entryId2) {
		_assetLinkLocalService.deleteLinks(entryId1, entryId2);
	}

	@Override
	public List<AssetLink> getDirectLinks(long entryId) {
		return ModelAdapterUtil.adapt(
			AssetLink.class, _assetLinkLocalService.getDirectLinks(entryId));
	}

	@Override
	public List<AssetLink> getDirectLinks(
		long entryId, boolean excludeInvisibleLinks) {

		return ModelAdapterUtil.adapt(
			AssetLink.class,
			_assetLinkLocalService.getDirectLinks(
				entryId, excludeInvisibleLinks));
	}

	@Override
	public List<AssetLink> getDirectLinks(long entryId, int typeId) {
		return ModelAdapterUtil.adapt(
			AssetLink.class,
			_assetLinkLocalService.getDirectLinks(entryId, typeId));
	}

	@Override
	public List<AssetLink> getDirectLinks(
		long entryId, int typeId, boolean excludeInvisibleLinks) {

		return ModelAdapterUtil.adapt(
			AssetLink.class,
			_assetLinkLocalService.getDirectLinks(
				entryId, typeId, excludeInvisibleLinks));
	}

	@Override
	public List<AssetLink> getLinks(long entryId) {
		return ModelAdapterUtil.adapt(
			AssetLink.class, _assetLinkLocalService.getLinks(entryId));
	}

	@Override
	public List<AssetLink> getLinks(
		long groupId, Date startDate, Date endDate, int start, int end) {

		return ModelAdapterUtil.adapt(
			AssetLink.class,
			_assetLinkLocalService.getLinks(
				groupId, startDate, endDate, start, end));
	}

	@Override
	public List<AssetLink> getLinks(long entryId, int typeId) {
		return ModelAdapterUtil.adapt(
			AssetLink.class, _assetLinkLocalService.getLinks(entryId, typeId));
	}

	@Override
	public List<AssetLink> getLinks(long classNameId, long classPK) {
		return ModelAdapterUtil.adapt(
			AssetLink.class,
			_assetLinkLocalService.getLinks(classNameId, classPK));
	}

	@Override
	public List<AssetLink> getReverseLinks(long entryId, int typeId) {
		return ModelAdapterUtil.adapt(
			AssetLink.class,
			_assetLinkLocalService.getReverseLinks(entryId, typeId));
	}

	@Override
	public AssetLink updateLink(
			long userId, long entryId1, long entryId2, int typeId, int weight)
		throws PortalException {

		return ModelAdapterUtil.adapt(
			AssetLink.class,
			_assetLinkLocalService.updateLink(
				userId, entryId1, entryId2, typeId, weight));
	}

	@Override
	public void updateLinks(
			long userId, long entryId, long[] linkEntryIds, int typeId)
		throws PortalException {

		_assetLinkLocalService.updateLinks(
			userId, entryId, linkEntryIds, typeId);
	}

	@Reference
	private AssetLinkLocalService _assetLinkLocalService;

}