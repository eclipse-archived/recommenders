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
package org.eclipse.recommenders.internal.completion.rcp.overrides;

import static java.lang.String.format;
import static org.eclipse.recommenders.utils.Checks.ensureEquals;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.inference.junctionTree.JunctionTreeAlgorithm;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ClassOverridesNetwork {

    private final ITypeName typeName;

    private final List<BayesNode> methodNodes;

    private final JunctionTreeAlgorithm junctionTreeAlgorithm;

    private HashMap<IMethodName, BayesNode> methodNameMapping;

    protected ClassOverridesNetwork(final ITypeName typeName, final BayesNet network, final BayesNode patternNode,
            final List<BayesNode> methodNodes) {
        this.typeName = typeName;
        this.methodNodes = methodNodes;
        junctionTreeAlgorithm = new JunctionTreeAlgorithm();
        junctionTreeAlgorithm.setNetwork(network);
        createMethodNameMapping();
    }

    private void createMethodNameMapping() {
        methodNameMapping = Maps.newHashMap();
        for (final BayesNode methodNode : methodNodes) {
            methodNameMapping.put(VmMethodName.get(methodNode.getName()), methodNode);
        }
    }

    public void clearEvidence() {
        junctionTreeAlgorithm.setEvidence(new HashMap<BayesNode, String>());
    }

    @Override
    public String toString() {
        return format("Model for '%s'", typeName);
    }

    public void observeMethodNode(final IMethodName methodName) {
        final BayesNode methodNode = methodNameMapping.get(methodName);
        if (methodNode != null) {
            junctionTreeAlgorithm.addEvidence(methodNode, "true");
        }
    }

    public ITypeName getType() {
        return typeName;
    }

    public Set<IMethodName> getMethods() {
        return Sets.newHashSet(methodNameMapping.keySet());
    }

    public SortedSet<Tuple<IMethodName, Double>> getRecommendedMethodOverrides(final double minProbability) {
        final TreeSet<Tuple<IMethodName, Double>> recommendations = createSortedSetForMethodRecommendations();
        for (final BayesNode node : methodNodes) {
            if (junctionTreeAlgorithm.getEvidence().containsKey(node)) {
                continue;
            }
            final double probability = junctionTreeAlgorithm.getBeliefs(node)[0];
            if (probability < minProbability) {
                continue;
            }
            final IMethodName method = VmMethodName.get(node.getName());
            final Tuple<IMethodName, Double> item = Tuple.newTuple(method, probability);
            recommendations.add(item);
        }
        return recommendations;
    }

    public SortedSet<Tuple<IMethodName, Double>> getRecommendedMethodOverrides(final double minProbabilityThreshold,
            final int maxNumberOfRecommendations) {
        final SortedSet<Tuple<IMethodName, Double>> recommendations = getRecommendedMethodOverrides(minProbabilityThreshold);
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

    public static TreeSet<Tuple<IMethodName, Double>> createSortedSetForMethodRecommendations() {
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

}
