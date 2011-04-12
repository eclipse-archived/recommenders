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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
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

    private final BayesianNetwork bayesNetwork;
    private final ITypeName typeName;
    private Network smileNetwork;
    private NodeWrapper contextNode;
    private NodeWrapper patternNode;
    private final HashMap<VmMethodName, NodeWrapper> methodNodes;
    private HashMap<String, NodeWrapper> nodeMapping;

    public SmileNetWrapper(final ITypeName typeName, final BayesianNetwork bayesNetwork) {
        this.typeName = typeName;
        this.bayesNetwork = bayesNetwork;
        methodNodes = new HashMap<VmMethodName, NodeWrapper>();

        initializeSmileNetwork();
    }

    private void initializeSmileNetwork() {
        smileNetwork = new Network();
        initializeNodes();
        initializeArcs();
        initializeProbabilities();
    }

    private void initializeNodes() {
        nodeMapping = new HashMap<String, NodeWrapper>();
        final Collection<Node> nodes = bayesNetwork.getNodes();
        for (final Node node : nodes) {
            if (node.getIdentifier().equalsIgnoreCase("context")) {
                contextNode = new NodeWrapper(node, smileNetwork);
                nodeMapping.put(node.getIdentifier(), contextNode);
            } else if (node.getIdentifier().equalsIgnoreCase("Patterns")) {
                patternNode = new NodeWrapper(node, smileNetwork);
                nodeMapping.put(node.getIdentifier(), patternNode);
            } else {
                final NodeWrapper methodNode = initializeMethodNode(node);
                nodeMapping.put(node.getIdentifier(), methodNode);
            }
        }
    }

    private NodeWrapper initializeMethodNode(final Node node) {
        final NodeWrapper nodeWrapper = new NodeWrapper(node, smileNetwork);
        methodNodes.put(VmMethodName.get(node.getIdentifier()), nodeWrapper);
        return nodeWrapper;
    }

    private void initializeArcs() {
        final Collection<Node> nodes = bayesNetwork.getNodes();
        for (final Node node : nodes) {
            final NodeWrapper nodeWrapper = nodeMapping.get(node);
            final Node[] parents = node.getParents();
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
        final NodeWrapper nodeWrapper = methodNodes.get(calledMethod);
        nodeWrapper.observeState("true");
    }

    @Override
    public void updateBeliefs() {
        smileNetwork.updateBeliefs();
    }

    @Override
    public void clearEvidence() {
        smileNetwork.clearAllEvidence();
    }

    @Override
    public void setMethodContext(final IMethodName newActiveMethodContext) {
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
        for (final IMethodName method : methodNodes.keySet()) {
            final NodeWrapper nodeWrapper = methodNodes.get(method);

            if (nodeWrapper.isEvidence()) {
                continue;
            }
            final double probability = nodeWrapper.getProbability()[nodeWrapper.getStateIndex("true")];
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
        for (final VmMethodName method : methodNodes.keySet()) {
            if (method.isInit()) {
                methodNodes.get(method).observeState("false");
            }
        }
    }

}
