/**
 * Copyright (c) 2011 Sebastian Proksch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.commons.mining.features;

import org.eclipse.recommenders.utils.names.IMethodName;

public class DefinitionFeature extends Feature {

	private final IMethodName definitionName;

	public DefinitionFeature(IMethodName definitionName) {
		this.definitionName = definitionName;
	}

	public boolean isInit() {
		return definitionName.isInit();
	}

	@Override
	public void accept(FeatureVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return definitionName.getIdentifier();
	}
	
	@Override
	public String shortName() {
		return definitionName.getDeclaringType().getClassName() + "." + definitionName.getName() + "()";
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DefinitionFeature) {
			DefinitionFeature other = (DefinitionFeature) obj;
			return definitionName.equals(other.definitionName);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return definitionName.hashCode() + 17;
	}
}