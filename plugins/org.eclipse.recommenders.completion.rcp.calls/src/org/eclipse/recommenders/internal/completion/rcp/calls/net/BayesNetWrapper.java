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
package org.eclipse.recommenders.internal.completion.rcp.calls.net;

import static org.eclipse.recommenders.commons.bayesnet.CallsNetConstants.NODE_ID_CONTEXT;
import static org.eclipse.recommenders.commons.bayesnet.CallsNetConstants.NODE_ID_DEFINITION;
import static org.eclipse.recommenders.commons.bayesnet.CallsNetConstants.NODE_ID_KIND;
import static org.eclipse.recommenders.commons.bayesnet.CallsNetConstants.NODE_ID_PATTERNS;
import static org.eclipse.recommenders.commons.bayesnet.CallsNetConstants.STATE_TRUE;
import static org.eclipse.recommenders.internal.analysis.codeelements.ObjectUsage.UNKNOWN_METHOD;
import static org.eclipse.recommenders.utils.Checks.ensureEquals;

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
import org.eclipse.recommenders.internal.analysis.codeelements.DefinitionSite;
import org.eclipse.recommenders.internal.analysis.codeelements.DefinitionSite.Kind;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectUsage;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.inference.junctionTree.JunctionTreeAlgorithm;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BayesNetWrapper implements IObjectMethodCallsNet {

    private final ITypeName typeName;

    private JunctionTreeAlgorithm junctionTreeAlgorithm;

    private BayesNet bayesNet;
    private BayesNode patternNode;
    private BayesNode contextNode;
    private BayesNode kindNode;
    private BayesNode definitionNode;
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

        junctionTreeAlgorithm = new JunctionTreeAlgorithm();
        junctionTreeAlgorithm.setNetwork(bayesNet);
    }

    private void initializeNodes(final BayesianNetwork network) {
        final Collection<Node> nodes = network.getNodes();
        for (final Node node : nodes) {
            final BayesNode bayesNode = new BayesNode(node.getIdentifier());
            final String[] states = node.getStates();
            for (int i = 0; i < states.length; i++) {
                bayesNode.addOutcome(states[i]);
            }
            bayesNet.addNode(bayesNode);

            if (node.getIdentifier().equals(NODE_ID_CONTEXT)) {
                contextNode = bayesNode;
            } else if (node.getIdentifier().equals(NODE_ID_PATTERNS)) {
                patternNode = bayesNode;
            } else if (node.getIdentifier().equals(NODE_ID_KIND)) {
                kindNode = bayesNode;
            } else if (node.getIdentifier().equals(NODE_ID_DEFINITION)) {
                definitionNode = bayesNode;
            } else {
                final VmMethodName vmMethodName = VmMethodName.get(node.getIdentifier());
                callNodes.put(vmMethodName, bayesNode);
            }
        }
    }

    private void initializeArcs(final BayesianNetwork network) {
        final Collection<Node> nodes = network.getNodes();
        for (final Node node : nodes) {
            final Node[] parents = node.getParents();
            final BayesNode children = bayesNet.getNode(node.getIdentifier());
            final LinkedList<BayesNode> bnParents = new LinkedList<BayesNode>();
            for (int i = 0; i < parents.length; i++) {
                bnParents.add(bayesNet.getNode(parents[i].getIdentifier()));
            }
            children.setParents(bnParents);
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
    public void clearEvidence() {
        junctionTreeAlgorithm.setEvidence(new HashMap<BayesNode, String>());
    }

    @Override
    public void setMethodContext(final IMethodName newActiveMethodContext) {
        final String identifier;
        if (newActiveMethodContext == null) {
            identifier = UNKNOWN_METHOD.getIdentifier();
        } else {
            identifier = newActiveMethodContext.getIdentifier();
        }

        if (contextNode.getOutcomes().contains(identifier)) {
            junctionTreeAlgorithm.addEvidence(contextNode, identifier);
        }
    }

    @Override
    public void setKind(final DefinitionSite.Kind newKind) {
        if (newKind == null) {
            clearEvidence();
            return;
        }
        final String identifier = newKind.toString();
        if (kindNode.getOutcomes().contains(identifier)) {
            junctionTreeAlgorithm.addEvidence(kindNode, identifier);
        }
    }

    @Override
    public void setDefinition(final IMethodName newDefinition) {
        final String identifier = newDefinition == null ? NetworkUtils.STATE_DUMMY_DEF : newDefinition.getIdentifier();
        if (definitionNode.getOutcomes().contains(identifier)) {
            junctionTreeAlgorithm.addEvidence(definitionNode, identifier);
        }
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
    public void setQuery(final ObjectUsage query) {
        clearEvidence();
        setMethodContext(query.contextFirst);
        setKind(query.kind);
        setDefinition(query.definition);
        setObservedMethodCalls(query.type, query.calls);
    }

    @Override
    public void setCalled(final IMethodName calledMethod) {
        final BayesNode node = bayesNet.getNode(calledMethod.getIdentifier());
        if (node != null) {
            junctionTreeAlgorithm.addEvidence(node, STATE_TRUE);
        }
    }

    @Override
    public SortedSet<Tuple<IMethodName, Double>> getRecommendedMethodCalls(final double minProbabilityThreshold) {

        final TreeSet<Tuple<IMethodName, Double>> res = createSortedSet();

        for (final IMethodName method : callNodes.keySet()) {
            final BayesNode bayesNode = callNodes.get(method);

            final boolean isAlreadyUsedAsEvidence = junctionTreeAlgorithm.getEvidence().containsKey(bayesNode);
            if (!isAlreadyUsedAsEvidence) {

                final int indexForTrue = bayesNode.getOutcomeIndex(STATE_TRUE);
                final double[] probabilities = junctionTreeAlgorithm.getBeliefs(bayesNode);
                final double probability = probabilities[indexForTrue];

                if (probability >= minProbabilityThreshold) {
                    res.add(Tuple.newTuple(method, probability));
                }
            }
        }

        return res;
    }

    private TreeSet<Tuple<IMethodName, Double>> createSortedSet() {
        final TreeSet<Tuple<IMethodName, Double>> res = Sets.newTreeSet(new Comparator<Tuple<IMethodName, Double>>() {

            @Override
            public int compare(final Tuple<IMethodName, Double> o1, final Tuple<IMethodName, Double> o2) {
                // the higher probability will be sorted above the lower values:
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
    public List<Tuple<String, Double>> getPatternsWithProbability() {
        final double[] probs = junctionTreeAlgorithm.getBeliefs(patternNode);
        final List<Tuple<String, Double>> res = Lists.newArrayListWithCapacity(probs.length);
        final Set<String> outcomes = patternNode.getOutcomes();
        for (final String outcome : outcomes) {
            final int probIndex = patternNode.getOutcomeIndex(outcome);
            final double p = probs[probIndex];
            if (0.01 > p) {
                continue;
            }
            res.add(Tuple.newTuple(outcome, p));
        }
        return res;
    }

    @Override
    public void setPattern(final String patternName) {
        junctionTreeAlgorithm.addEvidence(patternNode, patternName);
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
    public IMethodName getActiveContext() {
        return computeMethodNameFromState(contextNode);
    }

    private IMethodName computeMethodNameFromState(final BayesNode node) {
        final String stateId = junctionTreeAlgorithm.getEvidence().get(node);
        if (stateId == null) {
            return VmMethodName.NULL;
        }
        return VmMethodName.get(stateId);
    }

    @Override
    public IMethodName getActiveDefinition() {
        return computeMethodNameFromState(definitionNode);
    }

    @Override
    public Kind getActiveKind() {
        final String stateId = junctionTreeAlgorithm.getEvidence().get(kindNode);
        if (stateId == null) {
            return Kind.UNKNOWN;
        }
        return Kind.valueOf(stateId);
    }

    @Override
    public Set<IMethodName> getActiveCalls() {
        final TreeSet<IMethodName> res = Sets.newTreeSet();
        final Map<BayesNode, String> evidence = junctionTreeAlgorithm.getEvidence();
        for (final BayesNode methodNode : callNodes.values()) {
            if (evidence.containsKey(methodNode)) {
                res.add(VmMethodName.get(methodNode.getName()));
            }
        }
        // remove the NULL that may have been introduced by res.add(compute...)
        res.remove(VmMethodName.NULL);
        return res;
    }

    @Override
    public Set<DefinitionSite> getDefinitions() {
        double[] beliefs = junctionTreeAlgorithm.getBeliefs(definitionNode);
        for (int i = definitionNode.getOutcomeCount(); i-- > 0;) {
            if (beliefs[i] > 0.05) {
                String outcomeName = definitionNode.getOutcomeName(i);
                if (outcomeName.equals("LNone.none()V")) {
                    continue;
                }
                System.err.println(outcomeName);
                ;
            }
        }
        return Collections.emptySet();
    }
}