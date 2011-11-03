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
package org.eclipse.recommenders.commons.mining;

import java.io.Serializable;

public class Identifier implements Serializable {

	private static final long serialVersionUID = 1L;

	public String fqName = "";

	private Identifier() {
		// gson
	}

	public static Identifier create(String fqName) {
		Identifier type = new Identifier();
		type.fqName = fqName;
		return type;
	}

	public String plain() {
		return fqName.replaceAll("[^\\w]", "_");
	}

	public String latex() {
		return fqName.replaceAll("_", "\\\\_").replaceAll("\\$", "\\\\\\$");
	}

	@Override
	public String toString() {
		return fqName;
	}

	@Override
	public Object clone() {
		return Identifier.create(fqName);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Identifier) {
			Identifier other = (Identifier) obj;
			return other.fqName.equals(fqName);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return fqName.hashCode() + 1; // do not share hash with strings
	}
}
