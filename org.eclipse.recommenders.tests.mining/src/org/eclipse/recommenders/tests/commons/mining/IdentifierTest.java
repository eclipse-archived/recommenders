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
package org.eclipse.recommenders.tests.commons.mining;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.recommenders.commons.mining.Identifier;
import org.junit.Test;

public class IdentifierTest {

	@Test
	public void idsCanBeCloned() {
		Identifier actual = Identifier.create("example");
		Identifier clone = (Identifier) actual.clone();

		assertEquals(clone, actual);
		assertTrue(actual.hashCode() == clone.hashCode());
		assertNotSame(actual, clone);
	}

	@Test
	public void otherTypesAreDetected() {
		Identifier a = Identifier.create("blubb");
		String b = "blubb";

		assertFalse(a.equals(b));
	}

	@Test
	public void identifiersAndStringDoNotShareHashCodes() {
		Identifier a = Identifier.create("blubb");
		String b = "blubb";

		assertFalse(a.hashCode() == b.hashCode());
	}

	@Test
	public void escapingLatexSpecificCharacters() {
		Identifier uut = Identifier.create("string with _ and $");
		String actual = uut.latex();
		String expected = "string with \\_ and \\$";

		assertEquals(expected, actual);
	}
}