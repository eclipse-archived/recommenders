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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import junit.framework.Assert;

import org.eclipse.recommenders.commons.mining.features.CallFeature;
import org.eclipse.recommenders.commons.mining.features.DefinitionFeature;
import org.eclipse.recommenders.commons.mining.features.FeatureVisitor;
import org.eclipse.recommenders.commons.mining.features.Feature;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.junit.Before;
import org.junit.Test;

public class DefinitionFeatureTest {

	public static IMethodName INIT_DEFINITION = VmMethodName.get("LType.<init>()V");
	public static IMethodName RETURN_DEFINITION = VmMethodName.get("LType.callOther()V");

	private DefinitionFeature sut;

	@Before
	public void setup() {
		sut = new DefinitionFeature(INIT_DEFINITION);
	}

	@Test
	public void isInitIsDelegatedToMethodName() {
		IMethodName definition = mock(IMethodName.class);
		sut = new DefinitionFeature(definition);
		sut.isInit();
		verify(definition).isInit();
	}

	@Test
	public void identifierIsEqualToProvidedMethodName() {
		String actual = sut.toString();
		String expected = INIT_DEFINITION.getIdentifier();
		Assert.assertEquals(expected, actual);
	}

	// @Test
	// public void prefixedIdentifierIsCorrectlyPrefixed() {
	// String actual = sut.getPrefixedIdentifier();
	// String expected = IFeature.DEF_PREFIX + SAMPLE_DEFINITION.getIdentifier();
	// Assert.assertEquals(expected, actual);
	// }

	@Test
	public void visitorMethodIsCalled() {
		final boolean[] isCalled = new boolean[] { false };

		sut.accept(new FeatureVisitor() {
			@Override
			public void visit(DefinitionFeature call) {
				isCalled[0] = true;
			}
		});

		Assert.assertTrue(isCalled[0]);
	}

	@Test
	public void equalObjectsAreDetected() {
		Feature a = new DefinitionFeature(INIT_DEFINITION);
		Feature b = new DefinitionFeature(INIT_DEFINITION);
		assertEquals(a, b);
		assertTrue(a.hashCode() == b.hashCode());
	}

	@Test
	public void differentObjectsAreDetected() {
		Feature a = new DefinitionFeature(INIT_DEFINITION);
		Feature b = new DefinitionFeature(RETURN_DEFINITION);
		Feature otherType = new CallFeature(VmMethodName.get("Lnone.none()V"));

		assertFalse(a.equals(b));
		assertFalse(a.hashCode() == b.hashCode());
		assertFalse(a.equals(otherType));
	}
}