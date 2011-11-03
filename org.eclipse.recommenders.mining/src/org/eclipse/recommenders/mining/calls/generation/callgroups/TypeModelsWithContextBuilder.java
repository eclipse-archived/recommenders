/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.mining.calls.generation.callgroups;

import static org.apache.commons.lang3.ArrayUtils.toArray;
import static org.eclipse.recommenders.commons.udc.ObjectUsage.DUMMY_METHOD;
import static org.eclipse.recommenders.commons.utils.Checks.ensureEquals;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.NODE_ID_CALLING_CONTEXT;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.NODE_ID_CALL_GROUPS;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.P_MAX;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.P_MIN;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.P_ROUNDING_PRECISION;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.STATE_FALSE;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.STATE_TRUE;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.createPriorProbabilitiesForContextNodeAssumingDummyStateAtFirstIndex;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.ensureAllProbabilitiesInValidRange;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.ensureCorrectNumberOfProbabilities;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.ensureDummyContextAtIndex0;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.ensureMinimumTwoStates;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.round;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.safeDivMaxMin;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.scaleMaximalValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.commons.bayesnet.Node;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

//XXX delete this class
public class TypeModelsWithContextBuilder {

	private final ITypeName type;
	private final BayesianNetwork net;

	private IMethodName[] allCallingContexts;;
	private ReceiverCallGroupsContainer[] allCallGroups;

	private Node callingContextsNode;
	private Node callGroupsNode;
	private List<Node> callNodes;

	public TypeModelsWithContextBuilder(final ITypeName type, final Collection<ReceiverCallGroupsContainer> callGroups) {
		this.type = type;
		this.net = new BayesianNetwork();
		computeAllCallGroups(callGroups);
		computeAllKnownContexts();

	}

	/**
	 * Converts call groups to array and adds the dummy pattern to the list of known call groups( at index 0).
	 */
	private void computeAllCallGroups(final Collection<ReceiverCallGroupsContainer> knownReceiverCallGroupsContainer) {
		final LinkedList<ReceiverCallGroupsContainer> l = Lists.newLinkedList(knownReceiverCallGroupsContainer);
		final ReceiverCallGroupsContainer dummyReceiverCallGroup = ReceiverCallGroupsContainer.create();
		dummyReceiverCallGroup.observedContexts.add(DUMMY_METHOD);
		l.add(0, dummyReceiverCallGroup);
		this.allCallGroups = Iterables.toArray(l, ReceiverCallGroupsContainer.class);
	}

	/**
	 * Converts call groups to array and adds the dummy calling context to the list of known contexts.
	 */
	private void computeAllKnownContexts() {
		final TreeSet<IMethodName> tmp = Sets.newTreeSet();
		for (final ReceiverCallGroupsContainer callgroup : allCallGroups) {
			tmp.addAll(callgroup.observedContexts.elements());
		}
		allCallingContexts = tmp.toArray(new IMethodName[0]);
		ensureDummyContextAtIndex0(allCallingContexts);
	}

	public Node buildCallGroupsNode() {
		this.callGroupsNode = createAndRegisterNewNode(NODE_ID_CALL_GROUPS);
		callGroupsNode.setParents(new Node[] { callingContextsNode });
		final String[] states = new String[allCallGroups.length];
		ensureMinimumTwoStates(states);

		for (int i = 0; i < states.length; i++) {
			states[i] = "group " + i;
		}
		callGroupsNode.setStates(states);

		final double[] nodeDefinition = computeCallGroupsNodeDefinition();
		callGroupsNode.setProbabilities(nodeDefinition);
		ensureMinimumTwoStates(callGroupsNode);
		ensureCorrectNumberOfProbabilities(callGroupsNode);
		return callGroupsNode;
	}

	private double[] computeCallGroupsNodeDefinition() {
		final double[] nodeDefinition = new double[allCallingContexts.length * allCallGroups.length];
		int nextValueIndex = 0;
		for (final IMethodName curCallingContext : allCallingContexts) {
			final int numberOfUsagesInCallingContext = findTotalNumberOfUsagesInContext(curCallingContext);

			final int nextColumnStartIndex = nextValueIndex;
			for (final ReceiverCallGroupsContainer curCallGroup : allCallGroups) {
				final int numberOfUsagesOfCurrentGroupInCurrentContext = curCallGroup
						.numberOfUsagesInContext(curCallingContext);
				final double callGroupProb = safeDivMaxMin(numberOfUsagesOfCurrentGroupInCurrentContext,
						numberOfUsagesInCallingContext);
				nodeDefinition[nextValueIndex++] = round(callGroupProb, P_ROUNDING_PRECISION);
			}
			final double[] subarray = ArrayUtils.subarray(nodeDefinition, nextColumnStartIndex, nextValueIndex);
			ensureEquals(subarray.length, allCallGroups.length, "error");
			scaleMaximalValue(subarray);
			System.arraycopy(subarray, 0, nodeDefinition, nextColumnStartIndex, subarray.length);
		}
		ensureAllProbabilitiesInValidRange(nodeDefinition);
		return nodeDefinition;
	}

	private int findTotalNumberOfUsagesInContext(final IMethodName ctx) {
		int count = 0;
		for (final ReceiverCallGroupsContainer group : allCallGroups) {
			count += group.numberOfUsagesInContext(ctx);
		}
		return count;
	}

	public List<Node> buildMethodCallNodes() {

		final Multimap<IMethodName, ReceiverCallGroupsContainer> allCalls = HashMultimap.create();
		for (final ReceiverCallGroupsContainer group : allCallGroups) {
			for (final IMethodName call : group.invokedMethods) {
				allCalls.put(call, group);
			}
		}
		final TreeSet<IMethodName> sortedCalls = Sets.newTreeSet(allCalls.keySet());
		callNodes = Lists.newArrayList();
		for (final IMethodName call : sortedCalls) {
			final Node callNode = createAndRegisterNewNode(call.getIdentifier());
			callNode.setParents(toArray(callGroupsNode));
			callNode.setStates(toArray(STATE_TRUE, STATE_FALSE));
			final ArrayDoubleList probabilities = new ArrayDoubleList(allCallingContexts.length * allCallGroups.length);
			for (final ReceiverCallGroupsContainer curCallGroup : allCallGroups) {
				final Collection<ReceiverCallGroupsContainer> groupsContaingCurCall = allCalls.get(call);
				if (groupsContaingCurCall.contains(curCallGroup)) {
					probabilities.add(P_MAX);
					probabilities.add(P_MIN);
				} else {
					probabilities.add(P_MIN);
					probabilities.add(P_MAX);
				}
			}
			callNode.setProbabilities(probabilities.toArray());
			ensureMinimumTwoStates(callNode);
			ensureCorrectNumberOfProbabilities(callNode);
			callNodes.add(callNode);
		}
		return callNodes;
	}

	public Node buildCallingContextNode() {
		this.callingContextsNode = createAndRegisterNewNode(NODE_ID_CALLING_CONTEXT);

		final String[] states = new String[allCallingContexts.length];
		for (int i = 0; i < states.length; i++) {
			states[i] = allCallingContexts[i].getIdentifier();
		}
		callingContextsNode.setStates(states);

		final double[] definition = createPriorProbabilitiesForContextNodeAssumingDummyStateAtFirstIndex(states.length);
		callingContextsNode.setProbabilities(definition);
		ensureMinimumTwoStates(callingContextsNode);
		ensureCorrectNumberOfProbabilities(callingContextsNode);
		return callingContextsNode;
	}

	private Node createAndRegisterNewNode(final String name) {
		final Node callNode = new Node(name);
		net.addNode(callNode);
		return callNode;
	}

	public static BayesianNetwork createNetwork(final ITypeName type,
			final Collection<ReceiverCallGroupsContainer> callGroups) {

		final TypeModelsWithContextBuilder netBuilder = new TypeModelsWithContextBuilder(type, callGroups);
		netBuilder.buildCallingContextNode();
		netBuilder.buildCallGroupsNode();
		netBuilder.buildMethodCallNodes();
		final BayesianNetwork net = netBuilder.get();
		return net;
	}

	public BayesianNetwork get() {
		return net;
	}
}
