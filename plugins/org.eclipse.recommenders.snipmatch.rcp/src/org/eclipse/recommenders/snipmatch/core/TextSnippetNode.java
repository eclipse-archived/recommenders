/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.recommenders.snipmatch.core;


/**
 * A text snippet node. Unlike a formula snippet node, text snippet nodes contain just text.
 */
public class TextSnippetNode implements ISnippetNode {

	private String text;
	private Effect effect;
	
	public TextSnippetNode(String text, Effect effect) {

		this.text = text;
		this.effect = effect;
	}
	
	public String getText() {
		
		return text;
	}

	@Override
	public Effect getEffect() {
		
		return effect;
	}
}
