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
import static org.junit.Assert.assertTrue;

import org.eclipse.recommenders.commons.mining.Pattern;
import org.eclipse.recommenders.commons.mining.features.CallFeature;
import org.eclipse.recommenders.commons.mining.features.Feature;
import org.eclipse.recommenders.commons.mining.features.KindFeature;
import org.eclipse.recommenders.internal.analysis.codeelements.DefinitionSite.Kind;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PatternTest {

	private static final double DOUBLE_TRESHOLD = 0.001;
	private Pattern sut;

	@Before
	public void setup() {
		sut = new Pattern();
	}

	@Test
	public void defaultValues() {
		Assert.assertEquals("", sut.getName());
		Assert.assertEquals(0, sut.getNumberOfObservations());
	}

	@Test
	public void nameCanBeSet() {
		sut.setName("abc");
		String actual = sut.getName();
		String expected = "abc";
		assertEquals(expected, actual);
	}

	@Test
	public void numberOfObservationsCanBeSet() {
		int expected = 1234;
		sut.setNumberOfObservations(expected);
		int actual = sut.getNumberOfObservations();
		assertEquals(expected, actual);
	}

	@Test
	public void unsetValuesHaveZeroPropability() {
		Feature unsetFeature = getCallFeature("LType.unsetMethod()V");
		Assert.assertEquals(0.0, sut.getProbability(unsetFeature), DOUBLE_TRESHOLD);
	}

	@Test
	public void setValuesHaveExpectedPropability() {

		for (Integer i = 0; i < 50; i++) {
			Feature f = getCallFeature("LType.method" + i + "()V");
			double probability = i / 100.0;
			sut.setProbability(f, probability);
		}

		for (Integer i = 0; i < 50; i++) {
			Feature f = getCallFeature("LType.method" + i + "()V");
			double expected = i / 100.0;
			double actual = sut.getProbability(f);
			Assert.assertEquals(expected, actual, DOUBLE_TRESHOLD);
		}
	}

	@Test
	public void toStringIsCorrect() {
		String method = "LType.method()V";
		Feature f = new CallFeature(VmMethodName.get(method));
		sut.setName("pN");
		sut.setProbability(f, 0.01);

		String actual = sut.toString();
		String expected = "[pattern \"pN\":\n\t" + method + " : 0.01\n]";

		assertEquals(expected, actual);
	}

	@Test
	public void patternsCanBeCloned() {
		Pattern p1 = new Pattern();
		p1.setName("p1");
		p1.setNumberOfObservations(13);
		p1.setProbability(new KindFeature(Kind.NEW), 0.1);
		p1.setProbability(new KindFeature(Kind.FIELD), 0.2);
		p1.setProbability(new KindFeature(Kind.PARAMETER), 0.7);

		Pattern actual = p1.clone("other");

		assertEquals("other", actual.getName());
		assertEquals(13, actual.getNumberOfObservations());
		assertEquals(0.1, actual.getProbability(new KindFeature(Kind.NEW)), DOUBLE_TRESHOLD);
		assertEquals(0.2, actual.getProbability(new KindFeature(Kind.FIELD)), DOUBLE_TRESHOLD);
		assertEquals(0.7, actual.getProbability(new KindFeature(Kind.PARAMETER)), DOUBLE_TRESHOLD);
	}

	@Test
	public void equalObjectsAreDetected() {
		Pattern a = getExamplePattern();
		Pattern b = getExamplePattern();
		assertEquals(a, b);
		assertTrue(a.hashCode() == b.hashCode());
	}

	@Test
	public void differentObjectsAreDetected() {
		Pattern a = getExamplePattern();
		Pattern b = getExamplePattern();
		b.setName("other");
		assertFalse(a.equals(b));
		assertFalse(a.hashCode() == b.hashCode());
	}

	private static Feature getCallFeature(String name) {
		return new CallFeature(VmMethodName.get(name));
	}

	private static Pattern getExamplePattern() {
		Pattern p = new Pattern();
		p.setName("aName");
		p.setNumberOfObservations(13);
		p.setProbability(new CallFeature(VmMethodName.get("LType.m()V")), 1.0);
		return p;
	}
}