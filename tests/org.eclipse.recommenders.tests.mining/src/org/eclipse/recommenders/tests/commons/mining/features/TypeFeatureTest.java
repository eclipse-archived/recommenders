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
package org.eclipse.recommenders.tests.commons.mining.features;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import junit.framework.Assert;

import org.eclipse.recommenders.commons.mining.features.CallFeature;
import org.eclipse.recommenders.commons.mining.features.FeatureVisitor;
import org.eclipse.recommenders.commons.mining.features.Feature;
import org.eclipse.recommenders.commons.mining.features.TypeFeature;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Before;
import org.junit.Test;

public class TypeFeatureTest {

	public static ITypeName SAMPLE_TYPE = VmTypeName.get("LType");
	private static ITypeName OTHER_SAMPLE_TYPE = VmTypeName.get("LOtherType");

	private TypeFeature sut;

	@Before
	public void setup() {
		sut = new TypeFeature(SAMPLE_TYPE);
	}

	@Test
	public void identifierIsEqualToProvidedMethodName() {
		String actual = sut.toString();
		String expected = SAMPLE_TYPE.getIdentifier();
		Assert.assertEquals(expected, actual);
	}

	// @Test
	// public void prefixedIdentifierIsCorrectlyPrefixed() {
	// String actual = sut.getPrefixedIdentifier();
	// String expected = IFeature.TYPE_PREFIX + SAMPLE_TYPE.getIdentifier();
	// Assert.assertEquals(expected, actual);
	// }

	@Test
	public void visitorMethodIsCalled() {
		final boolean[] isCalled = new boolean[] { false };

		sut.accept(new FeatureVisitor() {
			@Override
			public void visit(TypeFeature call) {
				isCalled[0] = true;
			}
		});

		Assert.assertTrue(isCalled[0]);
	}

	@Test
	public void equalObjectsAreDetected() {
		Feature a = new TypeFeature(SAMPLE_TYPE);
		Feature b = new TypeFeature(SAMPLE_TYPE);
		assertEquals(a, b);
		assertTrue(a.hashCode() == b.hashCode());
	}

	@Test
	public void differentObjectsAreDetected() {
		Feature a = new TypeFeature(SAMPLE_TYPE);
		Feature b = new TypeFeature(OTHER_SAMPLE_TYPE);
		Feature otherType = new CallFeature(VmMethodName.get("Lnone.none()V"));

		assertFalse(a.equals(b));
		assertFalse(a.hashCode() == b.hashCode());
		assertFalse(a.equals(otherType));
	}
}