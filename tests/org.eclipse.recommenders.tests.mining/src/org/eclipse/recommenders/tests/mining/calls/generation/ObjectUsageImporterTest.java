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
package org.eclipse.recommenders.tests.mining.calls.generation;

import static org.eclipse.recommenders.tests.mining.calls.generation.ObjectUsageImporterFixture.CALL1;
import static org.eclipse.recommenders.tests.mining.calls.generation.ObjectUsageImporterFixture.CALL2_REBASED;
import static org.eclipse.recommenders.tests.mining.calls.generation.ObjectUsageImporterFixture.CONTEXT;
import static org.eclipse.recommenders.tests.mining.calls.generation.ObjectUsageImporterFixture.DEFINITION;
import static org.eclipse.recommenders.tests.mining.calls.generation.ObjectUsageImporterFixture.INIT_CALL;
import static org.eclipse.recommenders.tests.mining.calls.generation.ObjectUsageImporterFixture.KIND_FIELD;
import static org.eclipse.recommenders.tests.mining.calls.generation.ObjectUsageImporterFixture.KIND_NEW;
import static org.eclipse.recommenders.tests.mining.calls.generation.ObjectUsageImporterFixture.KIND_PARAM;
import static org.eclipse.recommenders.tests.mining.calls.generation.ObjectUsageImporterFixture.KIND_RETURN;
import static org.eclipse.recommenders.tests.mining.calls.generation.ObjectUsageImporterFixture.NO_DEFINITION;

import java.util.Set;

import org.eclipse.recommenders.commons.mining.Observation;
import org.eclipse.recommenders.commons.mining.features.CallFeature;
import org.eclipse.recommenders.commons.mining.features.ContextFeature;
import org.eclipse.recommenders.commons.mining.features.DefinitionFeature;
import org.eclipse.recommenders.commons.mining.features.Feature;
import org.eclipse.recommenders.commons.mining.features.KindFeature;
import org.eclipse.recommenders.commons.mining.features.TypeFeature;
import org.eclipse.recommenders.mining.calls.generation.ObjectUsageImporter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

public class ObjectUsageImporterTest {

	public ObjectUsageImporter uut;
	public ObjectUsageImporterFixture fixture;

	@Before
	public void setup() {
		uut = new ObjectUsageImporter();
		fixture = new ObjectUsageImporterFixture();
	}

	@Test
	public void objectUsagesCanBeTransformed() {
		Observation o = uut.transform(fixture.getReturnUsage());
		Assert.assertNotNull(o);
	}

	@Test
	public void typeIsTransformed() {

		Observation o = uut.transform(fixture.getReturnUsage());

		Feature actual = o.getType();
		Feature expected = new TypeFeature(ObjectUsageImporterFixture.TYPE);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void contextIsTransformed() {

		Observation o = uut.transform(fixture.getReturnUsage());

		Feature actual = o.getContext();
		Feature expected = new ContextFeature(CONTEXT);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void callsAreTransformedAndGetRebased() {
		Observation o = uut.transform(fixture.getReturnUsage());

		Set<CallFeature> actual = o.getCalls();
		Set<CallFeature> expected = Sets.newLinkedHashSet();
		expected.add(new CallFeature(CALL1));
		expected.add(new CallFeature(CALL2_REBASED));

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void NewKindIsTransformed() {

		Observation o = uut.transform(fixture.getNewUsage());

		Feature actual = o.getKind();
		Feature expected = new KindFeature(KIND_NEW);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void NewKindDoesNotHaveInitCall() {
		Observation o = uut.transform(fixture.getNewUsage());

		Set<CallFeature> actual = o.getCalls();
		CallFeature initCall = new CallFeature(INIT_CALL);

		Assert.assertFalse(actual.contains(initCall));
	}

	@Test
	public void NewDefinitionIsTransformed() {

		Observation o = uut.transform(fixture.getNewUsage());

		DefinitionFeature actual = o.getDefinition();
		DefinitionFeature expected = new DefinitionFeature(INIT_CALL);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void ReturnKindIsTransformed() {

		Observation o = uut.transform(fixture.getReturnUsage());

		KindFeature actual = o.getKind();
		KindFeature expected = new KindFeature(KIND_RETURN);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void ReturnDefinitionIsTransformed() {

		Observation o = uut.transform(fixture.getReturnUsage());

		DefinitionFeature actual = o.getDefinition();
		DefinitionFeature expected = new DefinitionFeature(DEFINITION);

		Assert.assertEquals(expected, actual);

	}

	@Test
	public void FieldKindIsTransformed() {

		Observation o = uut.transform(fixture.getFieldUsage());

		KindFeature actual = o.getKind();
		KindFeature expected = new KindFeature(KIND_FIELD);

		Assert.assertEquals(expected, actual);

	}

	@Test
	public void FieldDefinitionIsTransformed() {

		Observation o = uut.transform(fixture.getFieldUsage());

		DefinitionFeature actual = o.getDefinition();
		DefinitionFeature expected = new DefinitionFeature(NO_DEFINITION);

		Assert.assertEquals(expected, actual);

	}

	@Test
	public void ParameterKindIsTransformed() {

		Observation o = uut.transform(fixture.getParameterUsage());

		KindFeature actual = o.getKind();
		KindFeature expected = new KindFeature(KIND_PARAM);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void ParameterDefinitionIsTransformed() {

		Observation o = uut.transform(fixture.getParameterUsage());

		DefinitionFeature actual = o.getDefinition();
		DefinitionFeature expected = new DefinitionFeature(NO_DEFINITION);

		Assert.assertEquals(expected, actual);
	}
}