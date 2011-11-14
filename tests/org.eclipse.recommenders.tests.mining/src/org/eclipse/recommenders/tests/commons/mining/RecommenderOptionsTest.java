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

import java.util.Set;

import org.eclipse.recommenders.commons.mining.Identifier;
import org.eclipse.recommenders.commons.mining.RecommenderOptions;
import org.eclipse.recommenders.commons.mining.RecommenderOptions.Distance;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

public class RecommenderOptionsTest {

	private static final double DOUBLE_TRESHOLD = 0.01;
	private RecommenderOptions sut;

	@Test
	public void defaultValues() {
		sut = RecommenderOptions.create("");

		Assert.assertEquals(1.0, sut.weightContext, DOUBLE_TRESHOLD);
		Assert.assertEquals(1.0, sut.weightKind, DOUBLE_TRESHOLD);
		Assert.assertEquals(1.0, sut.weightDefinition, DOUBLE_TRESHOLD);

		Assert.assertTrue(sut.useCallSites);
		Assert.assertTrue(sut.useContext);
		Assert.assertFalse(sut.useKind);
		Assert.assertFalse(sut.useDefinition);
		Assert.assertFalse(sut.useInitAsCall);
		Assert.assertFalse(sut.useParameterSites);

		Assert.assertFalse(sut.keepRare);

		Assert.assertEquals(RecommenderOptions.Distance.MANHATTAN, sut.distance);
		Assert.assertEquals(RecommenderOptions.Clusterer.COUNTING, sut.clusterer);

		Assert.assertEquals(0.0, sut.canopyT1, DOUBLE_TRESHOLD);
		Assert.assertEquals(0.0, sut.canopyT2, DOUBLE_TRESHOLD);

		Assert.assertEquals(100, sut.kmeansClusterCount);
		Assert.assertEquals(0.01, sut.kmeansDistanceTreshold, DOUBLE_TRESHOLD);
		Assert.assertEquals(3, sut.kmeansIterations);

		Assert.assertEquals(0.0, sut.minPropability, DOUBLE_TRESHOLD);
	}

	@Test
	public void recommenderOptionsCanBeInit() {
		RecommenderOptions actual = new RecommenderOptions();
		RecommenderOptions expected = RecommenderOptions.create("");
		assertEquals(expected, actual);
	}

	@Test
	public void callgroupSetsExpectedValues() {
		sut = RecommenderOptions.create("callgroup");

		assertEquals(0.1, sut.canopyT1, DOUBLE_TRESHOLD);
		assertEquals(0.1, sut.canopyT2, DOUBLE_TRESHOLD);

		assertEquals(0.0, sut.weightContext, DOUBLE_TRESHOLD);
		assertEquals(0.0, sut.weightContext, DOUBLE_TRESHOLD);
		assertEquals(0.0, sut.weightContext, DOUBLE_TRESHOLD);

		assertEquals(Distance.MANHATTAN, sut.distance);
	}

	@Test
	public void parsingCallsitesInCreate() {
		sut = RecommenderOptions.create("+CS");
		Assert.assertTrue(sut.useCallSites);

		sut = RecommenderOptions.create("-CS");
		Assert.assertFalse(sut.useCallSites);
	}

	@Test
	public void parsingContextInCreate() {
		sut = RecommenderOptions.create("+CTX");
		Assert.assertTrue(sut.useContext);

		sut = RecommenderOptions.create("-CTX");
		Assert.assertEquals(sut.useContext, false);
	}

	@Test
	public void parsingKindInCreate() {
		sut = RecommenderOptions.create("+KIN");
		Assert.assertTrue(sut.useKind);

		sut = RecommenderOptions.create("-KIN");
		Assert.assertFalse(sut.useKind);
	}

	@Test
	public void parsingDefinitionInCreate() {
		sut = RecommenderOptions.create("+DEF");
		Assert.assertTrue(sut.useDefinition);

		sut = RecommenderOptions.create("-DEF");
		Assert.assertFalse(sut.useDefinition);
	}

	@Test
	public void parsingNewInCreate() {
		sut = RecommenderOptions.create("+INIT");
		Assert.assertTrue(sut.useInitAsCall);

		sut = RecommenderOptions.create("-INIT");
		Assert.assertFalse(sut.useInitAsCall);
	}

	@Test
	public void parsingParameterSitesInCreate() {
		sut = RecommenderOptions.create("+PS");
		Assert.assertTrue(sut.useParameterSites);

		sut = RecommenderOptions.create("-PS");
		Assert.assertFalse(sut.useParameterSites);
	}

	@Test
	public void parsingMinPropabilityInCreate() {
		sut = RecommenderOptions.create("+MIN1");
		Assert.assertEquals(0.01, sut.minPropability, DOUBLE_TRESHOLD);

		sut = RecommenderOptions.create("+MIN10");
		Assert.assertEquals(0.1, sut.minPropability, DOUBLE_TRESHOLD);

		sut = RecommenderOptions.create("+MIN50");
		Assert.assertEquals(0.5, sut.minPropability, DOUBLE_TRESHOLD);

		sut = RecommenderOptions.create("+MIN100");
		Assert.assertEquals(1.0, sut.minPropability, DOUBLE_TRESHOLD);

		sut = RecommenderOptions.create("counting+DEF+KIND-INIT+MIN55");
		Assert.assertEquals(0.55, sut.minPropability, DOUBLE_TRESHOLD);
	}

	@Test
	public void parsingCanopyOptionsInCreate() {

		sut = RecommenderOptions.create("counting");
		Assert.assertEquals(0.0, sut.canopyT1, DOUBLE_TRESHOLD);
		Assert.assertEquals(0.0, sut.canopyT2, DOUBLE_TRESHOLD);

		sut = RecommenderOptions.create("canopy[2,1]");
		Assert.assertEquals(2.0, sut.canopyT1, DOUBLE_TRESHOLD);
		Assert.assertEquals(1.0, sut.canopyT2, DOUBLE_TRESHOLD);

		sut = RecommenderOptions.create("canopy[8,4.5]");
		Assert.assertEquals(8.0, sut.canopyT1, DOUBLE_TRESHOLD);
		Assert.assertEquals(4.5, sut.canopyT2, DOUBLE_TRESHOLD);

		sut = RecommenderOptions.create("canopy[3.2,1]");
		Assert.assertEquals(3.2, sut.canopyT1, DOUBLE_TRESHOLD);
		Assert.assertEquals(1.0, sut.canopyT2, DOUBLE_TRESHOLD);

		sut = RecommenderOptions.create("canopy[4.3,2.1]");
		Assert.assertEquals(4.3, sut.canopyT1, DOUBLE_TRESHOLD);
		Assert.assertEquals(2.1, sut.canopyT2, DOUBLE_TRESHOLD);
	}

	@Test
	public void parsingWeightsInCreate() {

		sut = RecommenderOptions.create("counting");
		Assert.assertEquals(1.0, sut.weightContext, DOUBLE_TRESHOLD);
		Assert.assertEquals(1.0, sut.weightKind, DOUBLE_TRESHOLD);
		Assert.assertEquals(1.0, sut.weightDefinition, DOUBLE_TRESHOLD);

		sut = RecommenderOptions.create("counting+W[2,3,4]");
		Assert.assertEquals(2.0, sut.weightContext, DOUBLE_TRESHOLD);
		Assert.assertEquals(3.0, sut.weightKind, DOUBLE_TRESHOLD);
		Assert.assertEquals(4.0, sut.weightDefinition, DOUBLE_TRESHOLD);

		sut = RecommenderOptions.create("counting+W[1.2,2.3,3.4]");
		Assert.assertEquals(1.2, sut.weightContext, DOUBLE_TRESHOLD);
		Assert.assertEquals(2.3, sut.weightKind, DOUBLE_TRESHOLD);
		Assert.assertEquals(3.4, sut.weightDefinition, DOUBLE_TRESHOLD);
	}

	@Test
	public void parsingRare() {

		sut = RecommenderOptions.create("combined+RARE");
		Assert.assertTrue(sut.keepRare);

		sut = RecommenderOptions.create("combined-RARE");
		Assert.assertFalse(sut.keepRare);
	}

	@Test
	public void parsingDistance() {
		sut = RecommenderOptions.create("counting+MANHATTAN");
		Assert.assertEquals(RecommenderOptions.Distance.MANHATTAN, sut.distance);

		sut = RecommenderOptions.create("counting+COSINE");
		Assert.assertEquals(RecommenderOptions.Distance.COSINE, sut.distance);

		sut = RecommenderOptions.create("counting+TANIMOTO");
		Assert.assertEquals(RecommenderOptions.Distance.TANIMOTO, sut.distance);
	}

	@Test
	public void correctIdentifierIsCreated() {
		sut = RecommenderOptions.create("counting");
		Identifier actual = sut.getIdentifier();
		Identifier expected = Identifier.create(sut.toString());
		assertEquals(expected, actual);
	}

	@Test
	public void parsingClusterer() {
		sut = RecommenderOptions.create("counting");
		Assert.assertEquals(RecommenderOptions.Clusterer.COUNTING, sut.clusterer);

		sut = RecommenderOptions.create("canopy[0.21,0.12]");
		Assert.assertEquals(RecommenderOptions.Clusterer.CANOPY, sut.clusterer);

		sut = RecommenderOptions.create("kmeans[20,2,0.3]");
		Assert.assertEquals(RecommenderOptions.Clusterer.KMEANS, sut.clusterer);

		sut = RecommenderOptions.create("combined[0.3,0.15,5,0.123]");
		Assert.assertEquals(RecommenderOptions.Clusterer.COMBINED, sut.clusterer);
	}

	@Test
	public void equalObjectsAreDetected() {
		RecommenderOptions a = RecommenderOptions.create("kmeans[20,2,0.3]");
		RecommenderOptions b = RecommenderOptions.create("kmeans[20,2,0.3]");

		assertEquals(a, b);
		assertTrue(a.hashCode() == b.hashCode());
	}

	@Test
	public void differentObjectsAreDetected() {
		RecommenderOptions a = RecommenderOptions.create("kmeans[20,2,0.3]");
		RecommenderOptions b = RecommenderOptions.create("combined[0.3,0.15,5,0.123]");

		assertFalse(a.equals(b));
		assertFalse(a.hashCode() == b.hashCode());
	}

	@Test
	public void combinations() {

		String[] combinations = new String[] { "canopy[6.0,4.0]+COSINE+W[2.0,3.0,4.0]+RARE+CTX+KIN+DEF-INIT+CS-PS",
				"kmeans[30,5,2.1]+MANHATTAN+W[0.75,0.333333333,0.5]+RARE-CTX+KIN-DEF+INIT-CS+PS+MIN11",
				"combined[6.0,4.0,8,0.33]+COSINE+W[2.5,0.1,0.7]-RARE+CTX-KIN-DEF+INIT-CS+PS+MIN22",
				"combined[6.0,4.0,8,0.33]+COSINE+W[2.5,0.1,0.7]+RARE+CTX-KIN+DEF+INIT-CS+PS+MIN55",
				"canopy[9.3,7.3]+MANHATTAN+W[0.2,1.0,0.234]+RARE+CTX-KIN-DEF+INIT+CS-PS+MIN88",
				"canopy[9.3,7.3]+TANIMOTO+W[0.2,1.0,0.234]+RARE+CTX-KIN-DEF+INIT+CS-PS+MIN88",
				"counting+COSINE+W[3.0,4.0,0.0]-RARE+CTX+KIN+DEF-INIT+CS-PS+MIN100" };

		for (String expected : combinations) {
			sut = RecommenderOptions.create(expected);

			String actual = sut.toString();
			Assert.assertEquals(expected, actual);
		}
	}

	@Test
	public void allNeededCombinationsAreCreated() {
		Set<RecommenderOptions> actuals = RecommenderOptions.getAll();
		Set<RecommenderOptions> expecteds = Sets.newHashSet();
		expecteds.add(RecommenderOptions.create("combined[0.45,0.3,10,0.01]+COSINE+W[0.3,0.3,0.3]+CTX+KIN+DEF"));

		for (RecommenderOptions o : expecteds) {
			assertTrue(actuals.contains(o));
		}
	}
}