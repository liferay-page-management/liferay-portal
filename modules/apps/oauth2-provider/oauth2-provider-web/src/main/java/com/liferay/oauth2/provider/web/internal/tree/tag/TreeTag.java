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

package com.liferay.oauth2.provider.web.internal.tree.tag;

import com.liferay.oauth2.provider.web.internal.tree.Tree;

import java.io.IOException;

import java.util.Collection;
import java.util.LinkedList;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * @author Marta Medio
 */
public class TreeTag extends SimpleTagSupport {

	@Override
	public void doTag() throws IOException, JspException {
		JspContext jspContext = getJspContext();

		Object parentNodes = jspContext.getAttribute("parentNodes");

		try {
			jspContext.setAttribute("parentNodes", new LinkedList<>());

			for (Tree<?> tree : _trees) {
				renderTree(tree);
			}
		}
		finally {
			if (parentNodes == null) {
				jspContext.removeAttribute("parentNodes");
			}
			else {
				jspContext.setAttribute("parentNodes", parentNodes);
			}
		}
	}

	public JspFragment getLeafJspFragment() {
		return leafJspFragment;
	}

	public JspFragment getNodeJspFragment() {
		return nodeJspFragment;
	}

	public Collection<Tree<?>> getTrees() {
		return _trees;
	}

	public void setLeafJspFragment(JspFragment leafJspFragment) {
		this.leafJspFragment = leafJspFragment;
	}

	public void setNodeJspFragment(JspFragment nodeJspFragment) {
		this.nodeJspFragment = nodeJspFragment;
	}

	public void setTrees(Collection trees) {
		_trees = (Collection)trees;
	}

	protected void renderTree(Tree<?> tree) throws IOException, JspException {
		JspContext jspContext = getJspContext();

		Object treeObject = jspContext.getAttribute("tree");

		try {
			jspContext.setAttribute("tree", tree);

			if (tree instanceof Tree.Leaf) {
				getLeafJspFragment().invoke(null);
			}
			else {
				getNodeJspFragment().invoke(null);
			}
		}
		finally {
			if (treeObject == null) {
				jspContext.removeAttribute("tree");
			}
			else {
				jspContext.setAttribute("tree", treeObject);
			}
		}
	}

	protected JspFragment leafJspFragment;
	protected JspFragment nodeJspFragment;

	private Collection<Tree<?>> _trees;

}