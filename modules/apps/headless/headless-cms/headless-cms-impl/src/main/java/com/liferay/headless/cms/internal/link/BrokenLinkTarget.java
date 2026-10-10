/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.link;

import com.liferay.portal.kernel.workflow.WorkflowConstants;

/**
 * @author Mikel Lorza
 */
public class BrokenLinkTarget {

	public BrokenLinkTarget() {
		_deleted = true;
		_objectEntryId = 0;
		_status = WorkflowConstants.STATUS_ANY;
	}

	public BrokenLinkTarget(long objectEntryId, int status) {
		_objectEntryId = objectEntryId;
		_status = status;

		_deleted = false;
	}

	public long getObjectEntryId() {
		return _objectEntryId;
	}

	public int getStatus() {
		return _status;
	}

	public boolean isDeleted() {
		return _deleted;
	}

	private final boolean _deleted;
	private final long _objectEntryId;
	private final int _status;

}