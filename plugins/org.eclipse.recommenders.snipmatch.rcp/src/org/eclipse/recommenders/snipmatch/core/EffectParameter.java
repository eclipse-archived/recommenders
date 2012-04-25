/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.recommenders.snipmatch.core;

/**
 * Represents one parameter of an effect.
 */
public class EffectParameter {

	private String name = "";
	private String majorType = "";
	private String minorType = "";
	
	public String getName() {

		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	public String getMajorType() {
		
		return majorType;
	}

	public void setMajorType(String majorType) {
		
		this.majorType = majorType;
	}

	public String getMinorType() {
		
		return minorType;
	}

	public void setMinorType(String minorType) {

		this.minorType = minorType;
	}

	public String getFullType() {
		
		if (!minorType.isEmpty()) return majorType + ":" + minorType;
		else return majorType;
	}
	
	public boolean equals(Object other) {
		
		EffectParameter otherParam = (EffectParameter)other;
		return name.equals(otherParam.name) && majorType.equals(otherParam.majorType) &&
				minorType.equals(otherParam.minorType);
	}
}
