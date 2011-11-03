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

import org.eclipse.recommenders.internal.commons.analysis.codeelements.DefinitionSite.Kind;

public class KindFeature extends Feature {

	private final Kind kind;

	public KindFeature(Kind kind) {
		this.kind = kind;
	}

	@Override
	public void accept(FeatureVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return kind.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof KindFeature) {
			KindFeature other = (KindFeature) obj;
			return kind.equals(other.kind);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return kind.hashCode() + 7;
	}
}