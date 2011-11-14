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
package org.eclipse.recommenders.mining.calls.generation;

import static java.lang.System.arraycopy;
import static org.eclipse.recommenders.commons.bayesnet.CallsNetConstants.NODE_ID_CONTEXT;
import static org.eclipse.recommenders.commons.bayesnet.CallsNetConstants.NODE_ID_DEFINITION;
import static org.eclipse.recommenders.commons.bayesnet.CallsNetConstants.NODE_ID_KIND;
import static org.eclipse.recommenders.commons.bayesnet.CallsNetConstants.NODE_ID_PATTERNS;
import static org.eclipse.recommenders.commons.bayesnet.CallsNetConstants.STATE_FALSE;
import static org.eclipse.recommenders.commons.bayesnet.CallsNetConstants.STATE_TRUE;
import static org.eclipse.recommenders.commons.udc.ObjectUsage.DUMMY_METHOD;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.P_ROUNDING_PRECISION;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.ensureAllProbabilitiesInValidRange;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.getProbabilityInMinMaxRange;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.round;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.scaleMaximalValue;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.commons.bayesnet.Node;
import org.eclipse.recommenders.commons.mining.Pattern;
import org.eclipse.recommenders.commons.mining.dictionary.Dictionary;
import org.eclipse.recommenders.commons.mining.dictionary.IMatcher;
import org.eclipse.recommenders.commons.mining.features.CallFeature;
import org.eclipse.recommenders.commons.mining.features.ContextFeature;
import org.eclipse.recommenders.commons.mining.features.DefinitionFeature;
import org.eclipse.recommenders.commons.mining.features.Feature;
import org.eclipse.recommenders.commons.mining.features.KindFeature;
import org.eclipse.recommenders.internal.analysis.codeelements.DefinitionSite.Kind;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.names.ITypeName;

/**
 * creates the networkData out of the clustering results
 */
public class GenericNetworkBuilder {

	// TODO include prefixes for nodes
	public static final String CALL_NODE_PREFIX = "";
	// public static final String PARAMETER_NODE_PREFIX = "param:";

	private Dictionary<Feature> dictionary;
	private List<Pattern> patterns;

	private BayesianNetwork network;
	private Node patternNode;

	private ITypeName type;

	public BayesianNetwork buildNetwork(ITypeName type, List<Pattern> patterns, Dictionary<Feature> dictionary) {

		this.network = new BayesianNetwork();

		this.type = type;
		this.dictionary = dictionary;
		this.patterns = patterns;

		addDummyStatesToEnsureAtLeastTwoStatesPerNode();
		ensureAtLeastTwoPatternsExist();

		createPattern();
		createContextNode();
		createKind();
		createDefinition();

		createCalls();
		// createParameterSites();

		return network;
	}

	private void addDummyStatesToEnsureAtLeastTwoStatesPerNode() {

		dictionary.add(new ContextFeature(DUMMY_METHOD));
		dictionary.add(new DefinitionFeature(DUMMY_METHOD));

		// add all kinds to the kind node
		for (Kind k : Kind.values()) {
			dictionary.add(new KindFeature(k));
		}
	}

	private void ensureAtLeastTwoPatternsExist() {
		if (patterns.size() < 2) {
			Pattern other = patterns.get(0).clone("other");
			patterns.add(other);
		}
	}

	private void createPattern() {

		int countAll = countAll(patterns);

		patternNode = new Node(NODE_ID_PATTERNS);

		String[] states = new String[patterns.size()];
		double[] probabilities = new double[patterns.size()];

		for (int i = 0; i < patterns.size(); i++) {
			Pattern p = patterns.get(i);
			double probability = NetworkUtils.safeDivMaxMin(p.getNumberOfObservations(), countAll);
			probability = round(probability, P_ROUNDING_PRECISION);

			states[i] = p.getName();
			probabilities[i] = probability;
		}

		scaleMaximalValue(probabilities);
		ensureAllProbabilitiesInValidRange(probabilities);

		patternNode.setStates(states);
		patternNode.setProbabilities(probabilities);

		network.addNode(patternNode);
	}

	private static int countAll(List<Pattern> patterns) {
		int count = 0;
		for (Pattern p : patterns) {
			count += p.getNumberOfObservations();
		}
		return count;
	}

	private void createContextNode() {

		Node node = new Node(NODE_ID_CONTEXT);
		node.setParents(new Node[] { patternNode });
		network.addNode(node);

		IMatcher<Feature> matcher = new IMatcher<Feature>() {
			@Override
			public boolean matches(Feature f) {
				return f instanceof ContextFeature;
			}
		};
		Set<Feature> contexts = dictionary.getAllMatchings(matcher);

		addGenericPropabilities(node, contexts);
	}

	private void createKind() {

		Node node = new Node(NODE_ID_KIND);
		node.setParents(new Node[] { patternNode });
		network.addNode(node);

		IMatcher<Feature> matcher = new IMatcher<Feature>() {
			@Override
			public boolean matches(Feature f) {
				return f instanceof KindFeature;
			}
		};
		Set<Feature> kinds = dictionary.getAllMatchings(matcher);

		addGenericPropabilities(node, kinds);
	}

	private void createDefinition() {

		Node node = new Node(NODE_ID_DEFINITION);
		node.setParents(new Node[] { patternNode });
		network.addNode(node);

		IMatcher<Feature> matcher = new IMatcher<Feature>() {
			@Override
			public boolean matches(Feature f) {
				return f instanceof DefinitionFeature;
			}
		};
		Set<Feature> definitions = dictionary.getAllMatchings(matcher);

		addGenericPropabilities(node, definitions);
	}

	private void addGenericPropabilities(Node node, Set<Feature> statesSet) {

		String[] states = new String[statesSet.size()];
		double[] probabilities = new double[patterns.size() * statesSet.size()];

		int i = 0;
		for (Feature f : statesSet) {
			final String state = f.toString();
			ensureIsNotNull(state);
			states[i++] = state;
		}

		int j = 0;
		for (Pattern pattern : patterns) {

			double[] subprobs = new double[statesSet.size()];
			int k = 0;
			for (Feature state : statesSet) {
				double probability = pattern.getProbability(state);

				probability = getProbabilityInMinMaxRange(probability);
				probability = round(probability, P_ROUNDING_PRECISION);

				subprobs[k++] = probability;
			}

			scaleMaximalValue(subprobs);
			arraycopy(subprobs, 0, probabilities, j, statesSet.size());

			j += statesSet.size();
		}

		ensureAllProbabilitiesInValidRange(probabilities);

		node.setStates(states);
		node.setProbabilities(probabilities);
	}

	private void createCalls() {

		IMatcher<Feature> matcher = new IMatcher<Feature>() {
			@Override
			public boolean matches(Feature entry) {
				boolean isCall = entry instanceof CallFeature;
				return isCall;
			}
		};
		Set<Feature> calls = dictionary.getAllMatchings(matcher);

		for (Feature call : calls) {
			addCallNode((CallFeature) call);
			// addCallNode(call);
		}
	}

	private void addCallNode(CallFeature call) {

		Checks.ensureEquals(call.getType(), type, "all calls have to be rebased to match the type of the network");

		Node node = new Node(call.toString());
		node.setParents(new Node[] { patternNode });
		network.addNode(node);

		node.setStates(new String[] { STATE_TRUE, STATE_FALSE });

		double[] probabilities = new double[2 * patterns.size()];

		int i = 0;
		for (Pattern pattern : patterns) {
			double probability = pattern.getProbability(call);

			probability = NetworkUtils.getProbabilityInMinMaxRange(probability);
			probability = round(probability, P_ROUNDING_PRECISION);

			probabilities[i++] = probability;
			probabilities[i++] = round(1.0 - probability, P_ROUNDING_PRECISION);
		}

		ensureAllProbabilitiesInValidRange(probabilities);

		node.setProbabilities(probabilities);
	}
}