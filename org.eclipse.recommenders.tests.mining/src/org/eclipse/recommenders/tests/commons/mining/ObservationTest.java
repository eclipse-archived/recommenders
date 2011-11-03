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

import static org.eclipse.recommenders.commons.udc.ObjectUsage.UNKNOWN_KIND;
import static org.eclipse.recommenders.commons.udc.ObjectUsage.UNKNOWN_METHOD;
import static org.eclipse.recommenders.commons.udc.ObjectUsage.UNKNOWN_TYPE;
import static org.eclipse.recommenders.internal.commons.analysis.codeelements.DefinitionSite.Kind.UNKNOWN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.Assert;

import org.eclipse.recommenders.commons.mining.Observation;
import org.eclipse.recommenders.commons.mining.features.CallFeature;
import org.eclipse.recommenders.commons.mining.features.ContextFeature;
import org.eclipse.recommenders.commons.mining.features.DefinitionFeature;
import org.eclipse.recommenders.commons.mining.features.Feature;
import org.eclipse.recommenders.commons.mining.features.KindFeature;
import org.eclipse.recommenders.commons.mining.features.TypeFeature;
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.DefinitionSite.Kind;
import org.junit.Before;
import org.junit.Test;

public class ObservationTest {

	private static final ITypeName SAMPLE_TYPE = VmTypeName.get("LSomeType");
	private static final IMethodName SAMPLE_CONTEXT = VmMethodName.get("LContext.method()V");
	private static final Kind SAMPLE_KIND = Kind.METHOD_RETURN;
	private static final IMethodName SAMPLE_DEFINITION = VmMethodName.get("LHelper.method()V");
	private static final IMethodName SAMPLE_CALL1 = VmMethodName.get("LType.m1()V");
	private static final IMethodName SAMPLE_CALL2 = VmMethodName.get("LType.m2()V");
	private static final IMethodName SAMPLE_CALL3 = VmMethodName.get("LType.m3()V");

	private Observation sut;

	@Before
	public void setup() {
		sut = new Observation();
	}

	@Test
	public void defaults() {
		Assert.assertEquals(new TypeFeature(UNKNOWN_TYPE), sut.getType());
		Assert.assertEquals(new ContextFeature(UNKNOWN_METHOD), sut.getContext());
		Assert.assertEquals(new KindFeature(UNKNOWN_KIND), sut.getKind());
		Assert.assertEquals(new DefinitionFeature(UNKNOWN_METHOD), sut.getDefinition());
		Assert.assertEquals(0, sut.getCalls().size());
	}

	@Test
	public void equalObjectsAreRecognized() {
		sut = getSampleData();
		Observation test = getSampleData();

		assertEquals(sut, test);
		assertTrue(sut.hashCode() == test.hashCode());
	}

	@Test
	public void differentObjectsAreRecognized() {

		sut = getSampleData();

		Observation test = getSampleData();
		test.setContext(UNKNOWN_METHOD);

		assertFalse(sut.equals(test));
		assertFalse(sut.hashCode() == test.hashCode());
		assertFalse(sut.equals("otherType"));
	}

	@Test
	public void equalityIsInaffectedByOrdering() {

		Observation a = new Observation();
		a.addCall(VmMethodName.get("LBla.blubb()V"));
		a.addCall(VmMethodName.get("LBlubb.bla()V"));

		Observation b = new Observation();
		b.addCall(VmMethodName.get("LBlubb.bla()V"));
		b.addCall(VmMethodName.get("LBla.blubb()V"));

		assertEquals(a, b);
		assertTrue(a.hashCode() == b.hashCode());
	}

	@Test
	public void cloning() {

		sut = getSampleData();

		Observation clone = sut.clone();

		Assert.assertEquals(clone, sut);
		Assert.assertNotSame(clone, sut);
	}

	@Test
	public void serializationRoundtrip() throws IOException {
		sut = getSampleData();

		String json = GsonUtil.serialize(sut);
		Observation out = GsonUtil.deserialize(json, Observation.class);
		out.getContext(); // just to make sure "ensureCache()" is called

		assertEquals(sut, out);
	}

	@Test
	public void getContext() {

		sut = getSampleData();
		Feature actual = sut.getContext();
		Feature expected = new ContextFeature(SAMPLE_CONTEXT);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void getKind() {

		sut = getSampleData();
		Feature actual = sut.getKind();
		Feature expected = new KindFeature(SAMPLE_KIND);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void getDefinition() {

		sut = getSampleData();
		Feature actual = sut.getDefinition();
		Feature expected = new DefinitionFeature(SAMPLE_DEFINITION);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void getAllFeatures() {

		sut = getSampleData();

		Set<Feature> expected = new LinkedHashSet<Feature>();

		expected.add(new TypeFeature(SAMPLE_TYPE));
		expected.add(new ContextFeature(SAMPLE_CONTEXT));
		expected.add(new KindFeature(SAMPLE_KIND));
		expected.add(new DefinitionFeature(SAMPLE_DEFINITION));
		expected.add(new CallFeature(SAMPLE_CALL1));
		expected.add(new CallFeature(SAMPLE_CALL2));
		expected.add(new CallFeature(SAMPLE_CALL3));

		Assert.assertEquals(expected, sut.getAllFeatures());
	}

	@Test
	public void hasFeature() {
		sut = getSampleData();

		Assert.assertTrue(sut.hasFeature(new TypeFeature(SAMPLE_TYPE)));
		Assert.assertFalse(sut.hasFeature(new TypeFeature(VmTypeName.get("LSomeOtherType"))));
	}

	@Test
	public void callsCanBeAdded() {
		IMethodName call = VmMethodName.get("LType.call()V");
		sut.addCall(call);

		Set<CallFeature> actual = sut.getCalls();
		Set<CallFeature> expected = new HashSet<CallFeature>();
		expected.add(new CallFeature(call));

		assertEquals(expected, actual);
	}

	@Test
	public void callsCanBeCleared() {
		sut.addCall(VmMethodName.get("LType.call()V"));
		sut.clearCalls();

		Set<CallFeature> actual = sut.getCalls();
		Set<CallFeature> expected = Collections.emptySet();

		assertEquals(expected, actual);
	}

	@Test
	public void possibleQueriesAreDetected() {
		Observation a = getSampleData();
		Observation b = getSampleData();
		b.clearCalls();
		assertTrue(b.isQueryOf(a));
	}

	@Test
	public void impossibleQueriesAreDetected() {
		Observation a = getSampleData();
		Observation b = getSampleData();
		b.addCall(VmMethodName.get("LType.unmatchedCall()V"));
		assertFalse(b.isQueryOf(a));
	}

	@Test
	public void containedFeaturesAreDetected() {
		Observation o = getSampleData();
		assertTrue(o.hasFeature(new TypeFeature(SAMPLE_TYPE)));
		assertTrue(o.hasFeature(new ContextFeature(SAMPLE_CONTEXT)));
		assertTrue(o.hasFeature(new KindFeature(SAMPLE_KIND)));
		assertTrue(o.hasFeature(new DefinitionFeature(SAMPLE_DEFINITION)));
		assertTrue(o.hasFeature(new CallFeature(SAMPLE_CALL1)));
		assertTrue(o.hasFeature(new CallFeature(SAMPLE_CALL1)));
		assertTrue(o.hasFeature(new CallFeature(SAMPLE_CALL1)));
	}

	@Test
	public void notContainedFeaturesAreDetected() {
		Observation o = getSampleData();
		assertFalse(o.hasFeature(new TypeFeature(UNKNOWN_TYPE)));
		assertFalse(o.hasFeature(new ContextFeature(UNKNOWN_METHOD)));
		assertFalse(o.hasFeature(new KindFeature(UNKNOWN)));
		assertFalse(o.hasFeature(new DefinitionFeature(UNKNOWN_METHOD)));
		assertFalse(o.hasFeature(new CallFeature(UNKNOWN_METHOD)));
	}

	private static Observation getSampleData() {

		Observation sample = new Observation();

		sample.setType(SAMPLE_TYPE);
		sample.setContext(SAMPLE_CONTEXT);
		sample.setKind(SAMPLE_KIND);
		sample.setDefinition(SAMPLE_DEFINITION);
		sample.addCall(SAMPLE_CALL1);
		sample.addCall(SAMPLE_CALL2);
		sample.addCall(SAMPLE_CALL3);

		return sample;
	}
}
