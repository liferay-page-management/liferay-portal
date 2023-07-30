/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portlet.asset.service.impl;

import com.liferay.asset.kernel.model.AssetLink;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portlet.asset.service.base.AssetLinkLocalServiceBaseImpl;

import java.util.Date;
import java.util.List;

/**
 * This class implements the methods needed to handle AssetLinks, the entity
 * that relates different assets in the portal.
 *
 * The basic information stored for every link includes both assets entry IDs,
 * the userId, the link type and the link's weight.
 *
 * @author Brian Wing Shun Chan
 * @author Juan Fernández
 */
public class AssetLinkLocalServiceImpl extends AssetLinkLocalServiceBaseImpl {

	/**
	 * Adds a new asset link.
	 *
	 * @param  userId the primary key of the link's creator
	 * @param  entryId1 the primary key of the first asset entry
	 * @param  entryId2 the primary key of the second asset entry
	 * @param  type the link type. Acceptable values include {@link
	 *         AssetLinkConstants#TYPE_RELATED} which is a bidirectional
	 *         relationship and {@link AssetLinkConstants#TYPE_CHILD} which is a
	 *         unidirectional relationship. For more information see {@link
	 *         AssetLinkConstants}
	 * @param  weight the weight of the relationship, allowing precedence
	 *         ordering of links
	 * @return the asset link
	 */
	@Override
	public AssetLink addLink(
			long userId, long entryId1, long entryId2, int type, int weight)
		throws PortalException {

		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	@Override
	public AssetLink deleteAssetLink(AssetLink assetLink) {
		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	@Override
	public AssetLink deleteAssetLink(long linkId) throws PortalException {
		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	@Override
	public void deleteGroupLinks(long groupId) {
		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	/**
	 * Deletes the asset link.
	 *
	 * @param link the asset link
	 */
	@Override
	public void deleteLink(AssetLink link) {
		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	/**
	 * Deletes the asset link.
	 *
	 * @param linkId the primary key of the asset link
	 */
	@Override
	public void deleteLink(long linkId) throws PortalException {
		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	/**
	 * Deletes all links associated with the asset entry.
	 *
	 * @param entryId the primary key of the asset entry
	 */
	@Override
	public void deleteLinks(long entryId) {
		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	/**
	 * Delete all links that associate the two asset entries.
	 *
	 * @param entryId1 the primary key of the first asset entry
	 * @param entryId2 the primary key of the second asset entry
	 */
	@Override
	public void deleteLinks(long entryId1, long entryId2) {
		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	/**
	 * Returns all the asset links whose first entry ID is the given entry ID.
	 *
	 * @param  entryId the primary key of the asset entry
	 * @return the asset links whose first entry ID is the given entry ID
	 */
	@Override
	public List<AssetLink> getDirectLinks(long entryId) {
		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	@Override
	public List<AssetLink> getDirectLinks(
		long entryId, boolean excludeInvisibleLinks) {

		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	/**
	 * Returns all the asset links of the given link type whose first entry ID
	 * is the given entry ID.
	 *
	 * @param  entryId the primary key of the asset entry
	 * @param  typeId the link type. Acceptable values include {@link
	 *         AssetLinkConstants#TYPE_RELATED} which is a bidirectional
	 *         relationship and {@link AssetLinkConstants#TYPE_CHILD} which is a
	 *         unidirectional relationship. For more information see {@link
	 *         AssetLinkConstants}
	 * @return the asset links of the given link type whose first entry ID is
	 *         the given entry ID
	 */
	@Override
	public List<AssetLink> getDirectLinks(long entryId, int typeId) {
		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	@Override
	public List<AssetLink> getDirectLinks(
		long entryId, int typeId, boolean excludeInvisibleLinks) {

		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	/**
	 * Returns all the asset links whose first or second entry ID is the given
	 * entry ID.
	 *
	 * @param  entryId the primary key of the asset entry
	 * @return the asset links whose first or second entry ID is the given entry
	 *         ID
	 */
	@Override
	public List<AssetLink> getLinks(long entryId) {
		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	@Override
	public List<AssetLink> getLinks(
		long groupId, Date startDate, Date endDate, int start, int end) {

		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	/**
	 * Returns all the asset links of the given link type whose first or second
	 * entry ID is the given entry ID.
	 *
	 * @param  entryId the primary key of the asset entry
	 * @param  typeId the link type. Acceptable values include {@link
	 *         AssetLinkConstants#TYPE_RELATED} which is a bidirectional
	 *         relationship and {@link AssetLinkConstants#TYPE_CHILD} which is a
	 *         unidirectional relationship. For more information see {@link
	 *         AssetLinkConstants}
	 * @return the asset links of the given link type whose first or second
	 *         entry ID is the given entry ID
	 */
	@Override
	public List<AssetLink> getLinks(long entryId, int typeId) {
		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	/**
	 * Returns all the asset links of an AssetEntry.
	 *
	 * @param  classNameId AssetEntry's classNameId
	 * @param  classPK AssetEntry's classPK
	 * @return the asset links of the given entry params
	 */
	@Override
	public List<AssetLink> getLinks(long classNameId, long classPK) {
		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	/**
	 * Returns all the asset links of the given link type whose second entry ID
	 * is the given entry ID.
	 *
	 * @param  entryId the primary key of the asset entry
	 * @param  typeId the link type. Acceptable values include {@link
	 *         AssetLinkConstants#TYPE_RELATED} which is a bidirectional
	 *         relationship and {@link AssetLinkConstants#TYPE_CHILD} which is a
	 *         unidirectional relationship. For more information see {@link
	 *         AssetLinkConstants}
	 * @return the asset links of the given link type whose second entry ID is
	 *         the given entry ID
	 */
	@Override
	public List<AssetLink> getReverseLinks(long entryId, int typeId) {
		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	@Override
	public AssetLink updateLink(
			long userId, long entryId1, long entryId2, int typeId, int weight)
		throws PortalException {

		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

	/**
	 * Updates all links of the asset entry, replacing them with links
	 * associating the asset entry with the asset entries of the given link
	 * entry IDs.
	 *
	 * <p>
	 * If no link exists with a given link entry ID, a new link is created
	 * associating the current asset entry with the asset entry of that link
	 * entry ID. An existing link is deleted if either of its entry IDs is not
	 * contained in the given link entry IDs.
	 * </p>
	 *
	 * @param userId the primary key of the user updating the links
	 * @param entryId the primary key of the asset entry to be managed
	 * @param linkEntryIds the primary keys of the asset entries to be linked
	 *        with the asset entry to be managed
	 * @param typeId the type of the asset links to be created. Acceptable
	 *        values include {@link AssetLinkConstants#TYPE_RELATED} which is a
	 *        bidirectional relationship and {@link
	 *        AssetLinkConstants#TYPE_CHILD} which is a unidirectional
	 *        relationship. For more information see {@link AssetLinkConstants}
	 */
	@Override
	public void updateLinks(
			long userId, long entryId, long[] linkEntryIds, int typeId)
		throws PortalException {

		throw new UnsupportedOperationException(
			"This class is deprecate and replaced by " +
				"com.liferay.asset.link.service.impl." +
					"AssetLinkLocalServiceImpl");
	}

}