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
package org.eclipse.recommenders.internal.rcp.codecompletion.overrides.net;

import static java.lang.String.format;
import static org.eclipse.recommenders.commons.utils.Checks.ensureEquals;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

import smile.Network;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class ClassOverridesNetwork {
    public static String escape(final IMethodName ref) {
        return ref.toString().replaceAll("[^\\w]", "_");
    }

    private final Network network;

    private final List<MethodNode> methodNodes;

    private final PatternNode patternsNode;

    private HashMap<String, IMethodName> escapedMethodReferences;

    private final ITypeName typeName;

    protected ClassOverridesNetwork(final ITypeName typeName, final Network network) {
        this.typeName = typeName;
        this.network = network;
        patternsNode = new PatternNode(network);
        methodNodes = findMethodNodes();
    }

    public IMethodName getMethodReferenceFromEscapedName(final String escapedName) {
        final IMethodName res = escapedMethodReferences.get(escapedName);
        ensureIsNotNull(res);
        return res;
    }

    private List<MethodNode> findMethodNodes() {
        final LinkedList<MethodNode> res = new LinkedList<MethodNode>();
        for (final String methodNodeId : network.getChildIds(patternsNode.getNodeId())) {
            final MethodNode methodNode = new MethodNode(network, methodNodeId);
            res.add(methodNode);
        }
        return res;
    }

    public PatternNode getPatternsNode() {
        return patternsNode;
    }

    public List<MethodNode> getMethodNodes() {
        return methodNodes;
    }

    public void updateBeliefs() {
        network.updateBeliefs();
    }

    protected Network getNetwork() {
        return network;
    }

    public void clearEvidence() {
        network.clearAllEvidence();
    }

    @Override
    public String toString() {
        return format("Model for '%s'", typeName);
    }

    public void saveNetwork() {
        final File out = new File("debug.xdsl").getAbsoluteFile();
        System.out.println("wrote file to " + out);
        network.writeFile(out.getAbsolutePath());
    }

    public SortedSet<Tuple<IMethodName, Double>> getRecommendedMethodOverrides(final double minProbability) {
        final TreeSet<Tuple<IMethodName, Double>> recommendations = createSortedSetForMethodRecommendations();
        for (final MethodNode node : getMethodNodes()) {
            if (node.isEvidence()) {
                continue;
            }
            final double probability = node.getProbability();
            if (probability < minProbability) {
                continue;
            }
            final IMethodName method = node.getMethod();
            final Tuple<IMethodName, Double> item = Tuple.create(method, probability);
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
