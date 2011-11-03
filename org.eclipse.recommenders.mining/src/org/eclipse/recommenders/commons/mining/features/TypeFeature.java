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

import org.eclipse.recommenders.commons.utils.names.ITypeName;

public class TypeFeature extends Feature {

	private final ITypeName typeName;

	public TypeFeature(ITypeName typeName) {
		this.typeName = typeName;
	}

	@Override
	public void accept(FeatureVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return typeName.getIdentifier();
	}

	@Override
	public String shortName() {
		return typeName.getClassName();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TypeFeature) {
			TypeFeature other = (TypeFeature) obj;
			return typeName.equals(other.typeName);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return typeName.hashCode() + 23;
	}
}