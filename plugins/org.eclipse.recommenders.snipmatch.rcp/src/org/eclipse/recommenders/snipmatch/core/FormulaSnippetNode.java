/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.recommenders.snipmatch.core;


/**
 * Represents a snippet node that is a formula. A formula has a name, arguments, and evaluates to text.
 * Optionally, the formula can also specify the name of a new variable to store the formula's
 * evaluated result for future use.
 */
public class FormulaSnippetNode implements ISnippetNode {

	private String name;
	private String[] args;
	private String newVarName;
	private Effect effect;
	
	public FormulaSnippetNode(String name, String[] args, Effect effect) {

		this.name = name;
		this.args = args;
		this.effect = effect;
		
		if (args == null) this.args = new String[] {};
	}

	public String getName() {
		
		return name;
	}

	public String[] getArguments() {

		return args;
	}
	
	public String getArgument(int index) {
		
		return args[index];
	}
	
	public int numArguments() {
		
		return args.length;
	}
	
	public void setNewVariableName(String newVarName) {

		this.newVarName = newVarName;
	}
	
	public String getNewVariableName() {
		
		return newVarName;
	}

	@Override
	public Effect getEffect() {
		
		return effect;
	}
}
