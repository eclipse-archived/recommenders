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

import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

public class CallFeature extends Feature implements Comparable<Feature> {

	private final IMethodName callName;

	public CallFeature(IMethodName callName) {
		this.callName = callName;
	}

	public ITypeName getType() {
		return callName.getDeclaringType();
	}

	public IMethodName getMethod() {
		return callName;
	}

	@Override
	public int compareTo(Feature o) {
		if (o instanceof CallFeature) {
			CallFeature other = (CallFeature) o;
			String a = toString();
			String b = other.toString();
			return a.compareTo(b);
		} else {
			return 1;
		}
	}

	@Override
	public void accept(FeatureVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return callName.getIdentifier();
	}

	@Override
	public String shortName() {
		return callName.getName() + "()";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CallFeature) {
			CallFeature other = (CallFeature) obj;
			return callName.equals(other.callName);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return callName.hashCode() + 37;
	}
}