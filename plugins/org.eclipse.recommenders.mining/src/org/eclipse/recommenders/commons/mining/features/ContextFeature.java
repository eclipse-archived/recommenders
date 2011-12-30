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

public class ContextFeature extends Feature {

	private final IMethodName context;

	public ContextFeature(IMethodName context) {
		this.context = context;
	}

	@Override
	public void accept(FeatureVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return context.getIdentifier();
	}

	@Override
	public String shortName() {
		return context.getDeclaringType().getClassName() + "." + context.getName() + "()";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ContextFeature) {
			ContextFeature other = (ContextFeature) obj;
			return context.equals(other.context);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return context.hashCode() + 13;
	}

}
