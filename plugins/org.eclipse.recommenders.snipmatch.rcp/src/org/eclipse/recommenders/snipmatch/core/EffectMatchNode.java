/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.recommenders.snipmatch.core;

/**
 * A non-leaf match node that represents an effect call.
 */
public class EffectMatchNode extends MatchNode {

	private Effect effect;
	private String pattern;
	private MatchNode[] children;
	private ArgumentMatchNode ghostChild;
	
	public EffectMatchNode(Effect effect, String pattern, MatchNode[] children) {

		this.effect = effect;
		this.pattern = pattern;
		this.children = children;
		
		for (MatchNode child : children) {
			child.parent = this;
		}
		
		ghostChild = new ArgumentMatchNode(null, null);
		ghostChild.parent = this;
	}
	
	public MatchNode clone() {

		MatchNode[] childClones = new MatchNode[children.length];
		
		for (int i = 0; i < children.length; i++) {
			childClones[i] = children[i].clone();
		}
		
		EffectMatchNode clone = new EffectMatchNode(effect, pattern, childClones);

		clone.setMatchType(matchType);
		return clone;
	}

	public Effect getEffect() {
		
		return effect;
	}

	/**
	 * Gets the pattern of the effect that was matched against.
	 * @return
	 */
	public String getPattern() {
		
		return pattern;
	}
	
	public MatchNode getChild(int index) {
		
		return children[index];
	}
	
	public MatchNode getChild(String name) {
		
		for (int i = 0; i < effect.numParameters(); i++) {
			
			if (effect.getParameter(i).getName().equals(name)) return children[i];
		}
		
		return null;
	}

	public MatchNode[] getChildren() {
		
		return children;
	}
	
	public int numChildren() {
		
		return children.length;
	}
	
	public ArgumentMatchNode getGhostChild() {
		
		return ghostChild;
	}
	
	/**
	 * Gets whether or not this effect node is complete (all arguments are non-empty).
	 * @return
	 */
	public boolean isComplete() {
		
		for (MatchNode child : children) {
			
			if (child instanceof ArgumentMatchNode) {

				ArgumentMatchNode argChild = (ArgumentMatchNode) child;
				if (argChild.getArgument() == null || argChild.getArgument().isEmpty()) return false;
			}
			else {
				
				EffectMatchNode effectChild = (EffectMatchNode) child;
				if (!effectChild.isComplete()) return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Gets whether or not this effect node is empty (all arguments are empty).
	 * @return
	 */
	public boolean isEmpty() {
		
		for (MatchNode child : children) {
			
			if (child instanceof ArgumentMatchNode) {

				ArgumentMatchNode argChild = (ArgumentMatchNode) child;
				if (argChild.getArgument() != null &&
						!argChild.getArgument().isEmpty()) return false;
			}
			else {
				
				EffectMatchNode effectChild = (EffectMatchNode) child;
				if (!effectChild.isEmpty()) return false;
			}
		}
		
		return true;
	}
	
	public boolean equals(Object other) {

		if (!(other instanceof EffectMatchNode)) return false;
		EffectMatchNode otherEffectNode = (EffectMatchNode) other;
		
		if (effect != otherEffectNode.getEffect()) return false;

		if (otherEffectNode.numChildren() != numChildren()) return false;
		
		for (int i = 0; i < numChildren(); i++) {
			if (!getChild(i).equals(otherEffectNode.getChild(i))) return false;
		}
		
		return true;
	}
}
