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

import static org.eclipse.recommenders.commons.bayesnet.CallsNetConstants.NODE_ID_DEFINITION;
import static org.eclipse.recommenders.commons.bayesnet.CallsNetConstants.NODE_ID_KIND;
import static org.eclipse.recommenders.commons.bayesnet.CallsNetConstants.NODE_ID_PATTERNS;
import static org.eclipse.recommenders.commons.bayesnet.CallsNetConstants.STATE_FALSE;
import static org.eclipse.recommenders.commons.bayesnet.CallsNetConstants.STATE_TRUE;
import static org.eclipse.recommenders.commons.udc.ObjectUsage.DUMMY_METHOD;
import static org.eclipse.recommenders.commons.udc.ObjectUsage.NO_METHOD;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.commons.bayesnet.CallsNetConstants;
import org.eclipse.recommenders.commons.bayesnet.Node;
import org.eclipse.recommenders.commons.mining.Pattern;
import org.eclipse.recommenders.commons.mining.dictionary.Dictionary;
import org.eclipse.recommenders.commons.mining.features.CallFeature;
import org.eclipse.recommenders.commons.mining.features.ContextFeature;
import org.eclipse.recommenders.commons.mining.features.DefinitionFeature;
import org.eclipse.recommenders.commons.mining.features.Feature;
import org.eclipse.recommenders.commons.mining.features.KindFeature;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.DefinitionSite;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.DefinitionSite.Kind;
import org.eclipse.recommenders.mining.calls.generation.GenericNetworkBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class GenericNetworkBuilderTest {

	public static final ITypeName TYPE = VmTypeName.get("LType");
	public static final ITypeName NOT_REBASED_TYPE = VmTypeName.get("LParentType");

	public static final Feature DUMMY_CONTEXT = new ContextFeature(DUMMY_METHOD);
	public static final Feature DUMMY_DEFINITION = new ContextFeature(DUMMY_METHOD);

	public static final Feature CTX1 = new ContextFeature(VmMethodName.get("LType.ctx1()V"));
	public static final Feature CTX2 = new ContextFeature(VmMethodName.get("LType.ctx2()V"));

	public static final Feature KIND1 = new KindFeature(DefinitionSite.Kind.METHOD_RETURN);
	public static final Feature KIND2 = new KindFeature(DefinitionSite.Kind.NEW);

	public static final Feature DEF1 = new DefinitionFeature(NO_METHOD);
	public static final Feature DEF2 = new DefinitionFeature(VmMethodName.get("LType.<init>()V"));

	public static final Feature CALL1 = new CallFeature(VmMethodName.get("LType.call1()V"));
	public static final Feature CALL2 = new CallFeature(VmMethodName.get("LType.call2()V"));

	public GenericNetworkBuilder uut;

	public Dictionary<Feature> dictionary;

	public List<Pattern> patterns;

	private BayesianNetwork network;

	private Node patternNode;

	@Before
	public void setup() {
		prepareDictionary();
		preparePatterns();
		// prepareExpected();

		uut = new GenericNetworkBuilder();
		network = uut.buildNetwork(TYPE, patterns, dictionary);

		patternNode = new Node(NODE_ID_PATTERNS);
		patternNode.setStates(new String[] { "p1", "p2" });
		patternNode.setProbabilities(new double[] { 0.6, 0.4 });
	}

	@Test
	public void networksAreBuilt() {
		BayesianNetwork actual = uut.buildNetwork(TYPE, patterns, dictionary);
		Assert.assertNotNull(actual);
	}

	@Test
	public void networkContainsAllExpectedNodes() {
		Collection<Node> allNodes = network.getNodes();
		Assert.assertEquals(6, allNodes.size());
	}

	@Test
	public void patternNodeIsCorrect() {

		Node actual = network.getNode(CallsNetConstants.NODE_ID_PATTERNS);
		Assert.assertEquals(patternNode, actual);
	}

	@Test
	public void contextNodeIsCorrect() {

		Node actual = network.getNode(CallsNetConstants.NODE_ID_CONTEXT);

		Node expected = new Node(CallsNetConstants.NODE_ID_CONTEXT);
		expected.setParents(new Node[] { patternNode });
		expected.setStates(new String[] { CTX1.toString(), CTX2.toString(), DUMMY_CONTEXT.toString() });
		expected.setProbabilities(new double[] { 0.1, 0.9, 0, 0.2, 0.8, 0 });

		assertEquals(expected, actual);
	}

	@Test
	public void kindNodeContainsAllPossibleStates() {

		String[] actualStates = network.getNode(NODE_ID_KIND).getStates();
		for (Kind k : Kind.values()) {
			assertTrue(Arrays.asList(actualStates).contains(k.toString()));
		}
	}

	@Test
	public void kindNodeIsCorrect() {

		Node actual = network.getNode(NODE_ID_KIND);
		Node expected = new Node(NODE_ID_KIND);
		expected.setParents(new Node[] { patternNode });

		String[] allKinds = new String[Kind.values().length];
		int i = 0;
		for (Kind k : Kind.values()) {
			allKinds[i++] = k.toString();
		}

		expected.setStates(allKinds);
		expected.setProbabilities(new double[] { 0.3, 0.7, 0, 0, 0, 0, 0.4, 0.6, 0, 0, 0, 0 });

		assertEquals(expected, actual);
	}

	@Test
	public void definitionNodeIsCorrect() {

		Node actual = network.getNode(NODE_ID_DEFINITION);

		Node expected = new Node(NODE_ID_DEFINITION);
		expected.setParents(new Node[] { patternNode });
		expected.setStates(new String[] { DEF1.toString(), DEF2.toString(), DUMMY_DEFINITION.toString() });
		expected.setProbabilities(new double[] { 0.45, 0.55, 0, 0.35, 0.65, 0 });

		assertEquals(expected, actual);
	}

	@Test
	public void call1NodeIsCorrect() {
		String nodeName = GenericNetworkBuilder.CALL_NODE_PREFIX + CALL1.toString();
		Node actual = network.getNode(nodeName);

		Node expected = new Node(nodeName);
		expected.setParents(new Node[] { patternNode });
		expected.setStates(new String[] { STATE_TRUE, STATE_FALSE });
		expected.setProbabilities(new double[] { 1, 0, 0.15, 0.85 });

		assertEquals(expected, actual);
	}

	@Test
	public void call2NodeIsCorrect() {
		String nodeName = GenericNetworkBuilder.CALL_NODE_PREFIX + CALL2.toString();

		Node actual = network.getNode(nodeName);

		Node expected = new Node(nodeName);
		expected.setParents(new Node[] { patternNode });
		expected.setStates(new String[] { STATE_TRUE, STATE_FALSE });
		expected.setProbabilities(new double[] { 0.75, 0.25, 0.9, 0.1 });

		assertEquals(expected, actual);
	}

	@Test
	public void aSecondPatternIsCreatedIfOnlyOneIsProvided() {
		Pattern p1 = new Pattern();
		p1.setName("p1");
		p1.setNumberOfObservations(1);
		p1.setProbability(CTX1, 1.0);
		p1.setProbability(KIND1, 1.0);
		p1.setProbability(DEF1, 1.0);
		p1.setProbability(CALL1, 1.0);

		patterns = Lists.newArrayList();
		patterns.add(p1);

		uut.buildNetwork(TYPE, patterns, dictionary);

		Assert.assertEquals(2, patterns.size());

		Pattern actual = patterns.get(1);
		Pattern expected = p1.clone("other");

		Assert.assertEquals(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void anExceptionIsThrownIfCallsAreNotRebased() {
		uut.buildNetwork(NOT_REBASED_TYPE, patterns, dictionary);
	}

	private static void assertEquals(Node expected, Node actual) {

		String expectedId = expected.getIdentifier();
		String actualId = actual.getIdentifier();
		Assert.assertEquals(expectedId, actualId);

		String[] expectedStates = expected.getStates();
		String[] actualStates = actual.getStates();
		Assert.assertArrayEquals(expectedStates, actualStates);

		Node[] expectedParents = expected.getParents();
		Node[] actualParents = actual.getParents();

		Assert.assertEquals(expectedParents.length, actualParents.length);
		for (int i = 0; i < expectedParents.length; i++) {
			assertEquals(expectedParents[i], actualParents[i]);
		}

		double[] actuals = actual.getProbabilities();
		double[] expecteds = expected.getProbabilities();
		Assert.assertArrayEquals(expecteds, actuals, 0.0001);
	}

	private void prepareDictionary() {

		dictionary = new Dictionary<Feature>();
		// ctx
		dictionary.add(CTX1);
		dictionary.add(CTX2);
		// kind
		dictionary.add(KIND1);
		dictionary.add(KIND2);
		// def
		dictionary.add(DEF1);
		dictionary.add(DEF2);
		// calls
		dictionary.add(CALL1);
		dictionary.add(CALL2);
	}

	private void preparePatterns() {

		patterns = new ArrayList<Pattern>();

		Pattern p = new Pattern();
		p.setName("p1");
		p.setNumberOfObservations(6);

		p.setProbability(CTX1, 0.1);
		p.setProbability(CTX2, 0.9);
		p.setProbability(KIND1, 0.3);
		p.setProbability(KIND2, 0.7);
		p.setProbability(DEF1, 0.45);
		p.setProbability(DEF2, 0.55);
		p.setProbability(CALL1, 1);
		p.setProbability(CALL2, 0.75);

		patterns.add(p);

		p = new Pattern();
		p.setName("p2");
		p.setNumberOfObservations(4);

		p.setProbability(CTX1, 0.2);
		p.setProbability(CTX2, 0.8);
		p.setProbability(KIND1, 0.4);
		p.setProbability(KIND2, 0.6);
		p.setProbability(DEF1, 0.35);
		p.setProbability(DEF2, 0.65);
		p.setProbability(CALL1, 0.15);
		p.setProbability(CALL2, 0.9);

		patterns.add(p);
	}
}