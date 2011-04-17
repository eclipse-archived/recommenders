/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.bayes;

import static org.eclipse.recommenders.commons.utils.Checks.ensureEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.commons.bayesnet.Node;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;

import smile.Network;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class SmileNetWrapper implements IObjectMethodCallsNet {

    private static final String S_FALSE = "False";
    private static final String S_TRUE = "True";
    private static final String N_CALLING_CONTEXT = "calling context";
    private static final String N_CALL_GROUPS = "call groups";

    private final BayesianNetwork bayesNetwork;
    private final ITypeName typeName;
    private Network smileNetwork;
    private NodeWrapper contextNode;
    private NodeWrapper patternNode;

    private final Set<IMethodName> knownContexts = Sets.newHashSet();
    private final Map<String, NodeWrapper> nodeMapping = new HashMap<String, NodeWrapper>();
    private final Map<VmMethodName, NodeWrapper> callNodes = new HashMap<VmMethodName, NodeWrapper>();

    public SmileNetWrapper(final ITypeName typeName, final BayesianNetwork bayesNetwork) {
        this.typeName = typeName;
        this.bayesNetwork = bayesNetwork;
        initializeSmileNetwork();
    }

    private void initializeSmileNetwork() {
        smileNetwork = new Network();
        initializeNodes();
        initializeArcs();
        initializeProbabilities();
    }

    private void initializeNodes() {
        final Collection<Node> nodes = bayesNetwork.getNodes();
        for (final Node node : nodes) {
            if (node.getIdentifier().equalsIgnoreCase(N_CALLING_CONTEXT)) {
                initalizeContextNode(node);
            } else if (node.getIdentifier().equalsIgnoreCase(N_CALL_GROUPS)) {
                patternNode = new NodeWrapper(node, smileNetwork);
                nodeMapping.put(node.getIdentifier(), patternNode);
                // } else if (node.getIdentifier().equals("availability")) {
                // availabilityNode = new NodeWrapper(node, smileNetwork);
                // nodeMapping.put(node.getIdentifier(), availabilityNode);
            } else {
                final NodeWrapper methodNode = initializeMethodNode(node);
                nodeMapping.put(node.getIdentifier(), methodNode);
            }
        }
    }

    private void initalizeContextNode(final Node node) {
        contextNode = new NodeWrapper(node, smileNetwork);
        nodeMapping.put(node.getIdentifier(), contextNode);
        for (final String s : contextNode.getStates()) {
            final VmMethodName m = VmMethodName.get(s);
            knownContexts.add(m);
        }
    }

    private NodeWrapper initializeMethodNode(final Node node) {
        final NodeWrapper nodeWrapper = new NodeWrapper(node, smileNetwork);
        final String methodeName = node.getIdentifier();
        callNodes.put(VmMethodName.get(methodeName), nodeWrapper);
        return nodeWrapper;
    }

    private void initializeArcs() {
        final Collection<Node> nodes = bayesNetwork.getNodes();
        for (final Node node : nodes) {
            final NodeWrapper nodeWrapper = nodeMapping.get(node.getIdentifier());
            final Node[] parents = node.getParents();
            if (parents == null) {
                continue;
            }
            for (int i = 0; i < parents.length; i++) {
                final NodeWrapper parentWrapper = nodeMapping.get(parents[i].getIdentifier());
                smileNetwork.addArc(parentWrapper.getHandle(), nodeWrapper.getHandle());
            }
        }
    }

    private void initializeProbabilities() {
        final Collection<Node> nodes = bayesNetwork.getNodes();
        for (final Node node : nodes) {
            final NodeWrapper nodeWrapper = nodeMapping.get(node.getIdentifier());
            smileNetwork.setNodeDefinition(nodeWrapper.getHandle(), node.getProbabilities());
        }
    }

    @Override
    public ITypeName getType() {
        return typeName;
    }

    @Override
    public void setCalled(final IMethodName calledMethod) {
        final NodeWrapper nodeWrapper = callNodes.get(calledMethod);
        if (nodeWrapper != null) {
            nodeWrapper.observeState(S_TRUE);
        }
    }

    @Override
    public void updateBeliefs() {
        smileNetwork.updateBeliefs();
    }

    @Override
    public void clearEvidence() {
        smileNetwork.clearAllEvidence();
        // availabilityNode.observeState("true");
    }

    @Override
    public void setMethodContext(IMethodName newActiveMethodContext) {
        if (newActiveMethodContext == null) {
            newActiveMethodContext = NetworkUtils.CTX_DUMMY;
        }
        contextNode.observeState(newActiveMethodContext.getIdentifier());
    }

    @Override
    public void setObservedMethodCalls(final ITypeName rebaseType, final Set<IMethodName> invokedMethods) {
        for (final IMethodName invokedMethod : invokedMethods) {
            final IMethodName rebased = rebaseType == null ? invokedMethod : VmMethodName.rebase(rebaseType,
                    invokedMethod);
            setCalled(rebased);
        }
    }

    @Override
    public SortedSet<Tuple<IMethodName, Double>> getRecommendedMethodCalls(final double minProbabilityThreshold) {
        final TreeSet<Tuple<IMethodName, Double>> res = createSortedSet();
        for (final IMethodName method : callNodes.keySet()) {
            final NodeWrapper nodeWrapper = callNodes.get(method);

            if (nodeWrapper.isEvidence()) {
                continue;
            }
            final double probability = nodeWrapper.getProbability()[nodeWrapper.getStateIndex(S_TRUE)];
            if (probability < minProbabilityThreshold) {
                continue;
            }
            res.add(Tuple.create(method, probability));
        }
        return res;
    }

    private TreeSet<Tuple<IMethodName, Double>> createSortedSet() {
        final TreeSet<Tuple<IMethodName, Double>> res = Sets.newTreeSet(new Comparator<Tuple<IMethodName, Double>>() {

            @Override
            public int compare(final Tuple<IMethodName, Double> o1, final Tuple<IMethodName, Double> o2) {
                // the higher probability will be sorted above the lower
                // values:
                final int probabilityCompare = Double.compare(o2.getSecond(), o1.getSecond());
                return probabilityCompare != 0 ? probabilityCompare : o1.getFirst().compareTo(o2.getFirst());
            }
        });
        return res;
    }

    @Override
    public SortedSet<Tuple<IMethodName, Double>> getRecommendedMethodCalls(final double minProbabilityThreshold,
            final int maxNumberOfRecommendations) {
        final SortedSet<Tuple<IMethodName, Double>> recommendations = getRecommendedMethodCalls(minProbabilityThreshold);
        if (recommendations.size() <= maxNumberOfRecommendations) {
            return recommendations;
        }
        // need to remove smaller items:
        final Tuple<IMethodName, Double> firstExcludedRecommendation = Iterables.get(recommendations,
                maxNumberOfRecommendations);
        final SortedSet<Tuple<IMethodName, Double>> res = recommendations.headSet(firstExcludedRecommendation);
        ensureEquals(res.size(), maxNumberOfRecommendations,
                "filter op did not return expected number of compilationUnits2recommendationsIndex");
        return res;
    }

    @Override
    public void negateConstructors() {
        for (final VmMethodName method : callNodes.keySet()) {
            if (method.isInit()) {
                callNodes.get(method).observeState(S_FALSE);
            }
        }
    }

    @Override
    public List<Tuple<String, Double>> getPatternsWithProbability() {
        return patternNode.getStatesWithProbability();
    }

    @Override
    public void setPattern(final String patternName) {
        patternNode.observeState(patternName);
    }

    @Override
    public Collection<IMethodName> getMethodCalls() {
        return new LinkedList<IMethodName>(callNodes.keySet());
    }

    private void printNetworkState() {
        System.out.println("---- Context Node ----");
        printStates(contextNode);
        System.out.println("---- Pattern Node ----");
        printStates(patternNode);
        System.out.println("---- Probabilities for Calls ----");
        for (final IMethodName method : callNodes.keySet()) {
            final NodeWrapper nodeWrapper = callNodes.get(method);
            final double probability = nodeWrapper.getProbability()[nodeWrapper.getStateIndex(S_TRUE)];
            System.out.printf("%.4f\t%s\n", probability, method.getIdentifier());
        }
    }

    private void printStates(final NodeWrapper wrapper) {
        final List<Tuple<String, Double>> statesWithProbability = wrapper.getStatesWithProbability();
        Collections.sort(statesWithProbability, new Comparator<Tuple<String, Double>>() {
            @Override
            public int compare(final Tuple<String, Double> o1, final Tuple<String, Double> o2) {
                return o2.getSecond().compareTo(o1.getSecond());
            }
        });
        for (final Tuple<String, Double> tuple : statesWithProbability) {
            System.out.printf("%.4f\t%s\n", tuple.getSecond(), tuple.getFirst());
        }
    }

    @Override
    public Collection<IMethodName> getContexts() {
        return knownContexts;
    }

    @Override
    public Collection<String> getPatterns() {
        final String[] states = patternNode.getStates();
        return Arrays.asList(states);
    }
}
