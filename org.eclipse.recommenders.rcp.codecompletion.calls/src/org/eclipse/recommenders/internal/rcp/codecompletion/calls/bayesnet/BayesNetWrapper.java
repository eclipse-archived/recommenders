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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.bayesnet;

import static org.eclipse.recommenders.commons.utils.Checks.ensureEquals;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.recommenders.bayes.BayesNet;
import org.eclipse.recommenders.bayes.BayesNode;
import org.eclipse.recommenders.bayes.inference.junctionTree.JunctionTree;
import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.commons.bayesnet.Node;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.bayes.NetworkUtils;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BayesNetWrapper implements IObjectMethodCallsNet {

    private static final String S_FALSE = "False";
    private static final String S_TRUE = "True";
    private static final String N_CALLING_CONTEXT = "calling context";
    private static final String N_CALL_GROUPS = "call groups";
    private final ITypeName typeName;
    private BayesNet bayesNet;
    private JunctionTree junctionTree;
    private BayesNode contextNode;
    private BayesNode patternNode;
    private final HashMap<IMethodName, BayesNode> callNodes;

    public BayesNetWrapper(final ITypeName name, final BayesianNetwork network) {
        this.typeName = name;
        callNodes = new HashMap<IMethodName, BayesNode>();
        initializeNetwork(network);
    }

    private void initializeNetwork(final BayesianNetwork network) {
        bayesNet = new BayesNet();

        initializeNodes(network);
        initializeArcs(network);
        initializeProbabilities(network);

        junctionTree = new JunctionTree();
        junctionTree.setNetwork(bayesNet);
    }

    private void initializeNodes(final BayesianNetwork network) {
        final Collection<Node> nodes = network.getNodes();
        for (final Node node : nodes) {
            final BayesNode bayesNode = new BayesNode();
            final String[] states = node.getStates();
            for (int i = 0; i < states.length; i++) {
                bayesNode.addOutcome(states[i]);
            }
            bayesNet.addNode(node.getIdentifier(), bayesNode);

            if (node.getIdentifier().equalsIgnoreCase(N_CALLING_CONTEXT)) {
                contextNode = bayesNode;
            } else if (node.getIdentifier().equalsIgnoreCase(N_CALL_GROUPS)) {
                patternNode = bayesNode;
            } else {
                callNodes.put(VmMethodName.get(node.getIdentifier()), bayesNode);
            }
        }
    }

    private void initializeArcs(final BayesianNetwork network) {
        final Collection<Node> nodes = network.getNodes();
        for (final Node node : nodes) {
            final Node[] parents = node.getParents();
            final BayesNode children = bayesNet.getNode(node.getIdentifier());
            final LinkedList<Integer> parentIds = new LinkedList<Integer>();
            for (int i = 0; i < parents.length; i++) {
                final int parentId = bayesNet.getNodeId(parents[i].getIdentifier());
                parentIds.add(parentId);
            }
            bayesNet.setParents(children.getId(), parentIds);
        }

    }

    private void initializeProbabilities(final BayesianNetwork network) {
        final Collection<Node> nodes = network.getNodes();
        for (final Node node : nodes) {
            final BayesNode bayesNode = bayesNet.getNode(node.getIdentifier());
            bayesNode.setProbabilities(node.getProbabilities());
        }
    }

    @Override
    public ITypeName getType() {
        return typeName;
    }

    @Override
    public void setCalled(final IMethodName calledMethod) {
        final BayesNode node = bayesNet.getNode(calledMethod.getIdentifier());
        if (node != null) {
            junctionTree.addEvidence(node.getId(), node.getOutcomeId(S_TRUE));
        }
    }

    @Override
    public void updateBeliefs() {
        junctionTree.updateBeliefs();
    }

    @Override
    public void clearEvidence() {
        junctionTree.setEvidence(new HashMap<Integer, Integer>());
    }

    @Override
    public void setMethodContext(IMethodName newActiveMethodContext) {
        if (newActiveMethodContext == null) {
            newActiveMethodContext = NetworkUtils.CTX_NULL;
        }
        junctionTree.addEvidence(contextNode.getId(), contextNode.getOutcomeId(newActiveMethodContext.getIdentifier()));
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
            final BayesNode bayesNode = callNodes.get(method);

            if (junctionTree.getEvidence().containsKey(bayesNode.getId())) {
                continue;
            }
            final double probability = junctionTree.getBeliefs(bayesNode.getId())[bayesNode.getOutcomeId(S_TRUE)];
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
        for (final IMethodName method : callNodes.keySet()) {
            if (method.isInit()) {
                final BayesNode bayesNode = callNodes.get(method);
                junctionTree.addEvidence(bayesNode.getId(), bayesNode.getOutcomeId(S_FALSE));
            }
        }
    }

    @Override
    public List<Tuple<String, Double>> getPatternsWithProbability() {
        final double[] probs = junctionTree.getBeliefs(patternNode.getId());
        final List<Tuple<String, Double>> res = Lists.newArrayListWithCapacity(probs.length);
        final Set<String> outcomes = patternNode.getOutcomes();
        for (final String outcome : outcomes) {
            final int probIndex = patternNode.getOutcomeId(outcome);
            final double p = probs[probIndex];
            if (0.01 > p) {
                continue;
            }
            res.add(Tuple.create(outcome, p));
        }
        return res;
    }

    @Override
    public void setPattern(final String patternName) {
        junctionTree.addEvidence(patternNode.getId(), patternNode.getOutcomeId(patternName));
    }

    @Override
    public Collection<IMethodName> getMethodCalls() {
        return new LinkedList<IMethodName>(callNodes.keySet());
    }

    @Override
    public Collection<IMethodName> getContexts() {
        final Set<String> outcomes = contextNode.getOutcomes();
        final LinkedList<IMethodName> result = new LinkedList<IMethodName>();
        for (final String outcome : outcomes) {
            result.add(VmMethodName.get(outcome));
        }
        return result;
    }

    @Override
    public Collection<String> getPatterns() {
        return new LinkedList<String>(patternNode.getOutcomes());
    }

}
