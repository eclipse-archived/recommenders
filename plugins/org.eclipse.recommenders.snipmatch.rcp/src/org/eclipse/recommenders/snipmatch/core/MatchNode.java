/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.recommenders.snipmatch.core;

/**
 * Represents one node in a match tree structure.
 * Can be either an effect call (EffectMatchNode) or an argument (ArgumentMatchNode).
 * Effect calls can be nested into other effect calls.
 */
public abstract class MatchNode {
	
	protected MatchNode parent;

	protected MatchType matchType;
	
	/**
	 * Gets the parent node of this node.
	 * @return
	 */
	public MatchNode getParent() {
		
		return parent;
	}
	
	/**
	 * Recursively get the root node of the tree this node is in.
	 * @return
	 */
	public MatchNode getRoot() {
		
		if (parent == null) return this;
		return parent.getRoot();
	}
	
	public MatchType getMatchType() {
		
		return matchType;
	}
	
	public void setMatchType(MatchType matchType) {
		
		this.matchType = matchType;
	}
	
	public abstract MatchNode clone();
}
