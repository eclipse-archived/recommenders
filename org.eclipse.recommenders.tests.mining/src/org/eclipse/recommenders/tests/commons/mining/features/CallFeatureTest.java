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
import org.eclipse.recommenders.commons.mining.features.ContextFeature;
import org.eclipse.recommenders.commons.mining.features.FeatureVisitor;
import org.eclipse.recommenders.commons.mining.features.Feature;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.junit.Before;
import org.junit.Test;

public class CallFeatureTest {

	public static IMethodName SAMPLE_CALL = VmMethodName.get("LType.call()V");
	public static IMethodName OTHER_SAMPLE_CALL = VmMethodName.get("LType.call2()V");

	private CallFeature sut;

	@Before
	public void setup() {
		sut = new CallFeature(SAMPLE_CALL);
	}

	@Test
	public void compareTo() {
		CallFeature a = new CallFeature(VmMethodName.get("LType.mA()V"));
		CallFeature b = new CallFeature(VmMethodName.get("LType.mB()V"));

		Assert.assertEquals(-1, a.compareTo(b));
		Assert.assertEquals(0, a.compareTo(a));
		Assert.assertEquals(1, b.compareTo(a));
	}

	@Test
	public void otherTypesAreSmallerThanCalls() {
		CallFeature a = new CallFeature(VmMethodName.get("LType.mA()V"));
		Feature b = new ContextFeature(SAMPLE_CALL);
		Assert.assertEquals(1, a.compareTo(b));
	}

	@Test
	public void identifierIsEqualToProvidedMethodName() {
		String actual = sut.toString();
		String expected = SAMPLE_CALL.getIdentifier();
		Assert.assertEquals(expected, actual);
	}

	// @Test
	// public void prefixedIdentifierIsCorrectlyPrefixed() {
	// String actual = sut.getPrefixedIdentifier();
	// String expected = IFeature.CALLSITE_PREFIX + SAMPLE_CALL.getIdentifier();
	// Assert.assertEquals(expected, actual);
	// }

	@Test
	public void visitorMethodIsCalled() {
		final boolean[] isCalled = new boolean[] { false };

		sut.accept(new FeatureVisitor() {
			@Override
			public void visit(CallFeature call) {
				isCalled[0] = true;
			}
		});

		Assert.assertTrue(isCalled[0]);
	}

	@Test
	public void equalObjectsAreDetected() {
		Feature a = new CallFeature(SAMPLE_CALL);
		Feature b = new CallFeature(SAMPLE_CALL);
		assertEquals(a, b);
		assertTrue(a.hashCode() == b.hashCode());
	}

	@Test
	public void differentObjectsAreDetected() {
		Feature a = new CallFeature(SAMPLE_CALL);
		Feature b = new CallFeature(OTHER_SAMPLE_CALL);
		Feature otherType = new ContextFeature(VmMethodName.get("Lnone.none()V"));

		assertFalse(a.equals(b));
		assertFalse(a.hashCode() == b.hashCode());
		assertFalse(a.equals(otherType));
	}
}