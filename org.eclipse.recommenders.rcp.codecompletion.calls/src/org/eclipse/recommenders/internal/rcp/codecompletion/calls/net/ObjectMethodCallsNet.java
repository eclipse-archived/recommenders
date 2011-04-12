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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.net;

import static org.eclipse.recommenders.commons.utils.Checks.ensureEquals;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.annotations.Clumsy;
import org.eclipse.recommenders.commons.utils.annotations.Nullable;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;

import smile.Network;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Clumsy
public class ObjectMethodCallsNet implements IObjectMethodCallsNet {

    public static String escape(final IMethodName ref) {
        ensureIsNotNull(ref);
        return ref.toString().replaceAll("[^\\w]", "_");
    }

    private final Network network;

    private final Map<IMethodName, MethodNode> methodNodes;

    private final ContextNode contextNode;

    private final AvailabilityNode availabilityNode;

    private final PatternNode patternsNode;

    private HashMap<String, IMethodName> escapedMethodReferences;

    private final ITypeName type;

    protected ObjectMethodCallsNet(final ITypeName type, final Network network) {
        this.type = type;
        this.network = network;
        contextNode = new ContextNode(network);
        availabilityNode = new AvailabilityNode(network);
        patternsNode = new PatternNode(network);
        methodNodes = findMethodNodes();
        setAvailablity(true);
    }

    public IMethodName getMethodReferenceFromEscapedName(final String escapedName) {
        final IMethodName res = escapedMethodReferences.get(escapedName);
        ensureIsNotNull(res);
        return res;
    }

    private Map<IMethodName, MethodNode> findMethodNodes() {
        final Map<IMethodName, MethodNode> res = Maps.newHashMap();
        for (final int methodNodeId : network.getChildren(patternsNode.getNodeId())) {
            final MethodNode methodNode = new MethodNode(network, methodNodeId);
            res.put(methodNode.getMethod(), methodNode);
        }
        return res;
    }

    @Override
    public ITypeName getType() {
        return type;
    }

    public ContextNode getContextNode() {
        return contextNode;
    }

    public AvailabilityNode getAvailabilityNode() {
        return availabilityNode;
    }

    public PatternNode getPatternsNode() {
        return patternsNode;
    }

    public Collection<MethodNode> getMethodNodes() {
        return methodNodes.values();
    }

    @Override
    public void setCalled(final IMethodName calledMethod) {
        final MethodNode callNode = methodNodes.get(calledMethod);
        if (callNode == null) {
            System.err.printf("node %s not found in model.\n", calledMethod);
            return;
        }
        callNode.setEvidence(true);
    }

    @Override
    public void updateBeliefs() {
        network.updateBeliefs();
    }

    protected Network getNetwork() {
        return network;
    }

    @Override
    public void clearEvidence() {
        network.clearAllEvidence();
        setAvailablity(true);
    }

    public void saveNetwork() {
        final File tmpDir = SystemUtils.getJavaIoTmpDir();
        final File out = new File(tmpDir, "debug.xdsl").getAbsoluteFile();
        System.out.println("wrote file to " + out);
        network.writeFile(out.getAbsolutePath());
    }

    @Override
    public String toString() {
        return "Model for " + type.getIdentifier();
    }

    private void setAvailablity(final boolean newValue) {
        getAvailabilityNode().setEvidence(newValue);
    }

    @Override
    public void setMethodContext(final IMethodName newActiveMethodContext) {
        getContextNode().setContext(newActiveMethodContext);
    }

    @Override
    public void setObservedMethodCalls(final @Nullable ITypeName rebaseType, final Set<IMethodName> invokedMethods) {
        for (final IMethodName invokedMethod : invokedMethods) {
            final IMethodName rebased = rebaseType == null ? invokedMethod : VmMethodName.rebase(rebaseType,
                    invokedMethod);
            setCalled(rebased);
        }
    }

    @Override
    public SortedSet<Tuple<IMethodName, Double>> getRecommendedMethodCalls(final double minProbabilityThreshold) {
        final TreeSet<Tuple<IMethodName, Double>> res = createSortedSet();
        for (final MethodNode node : getMethodNodes()) {
            if (node.isEvidence()) {
                continue;
            }
            final double probability = node.getProbability();
            if (probability < minProbabilityThreshold) {
                continue;
            }
            final IMethodName method = node.getMethod();
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
        for (final MethodNode node : getMethodNodes()) {
            if (node.getMethod().isInit()) {
                node.setEvidence(false);
            }
        }
    }

    public void setPattern(final String patternName) {
        patternsNode.setPattern(patternName);

    }
}
