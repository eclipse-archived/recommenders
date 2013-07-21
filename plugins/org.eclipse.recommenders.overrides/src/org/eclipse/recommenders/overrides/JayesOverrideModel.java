/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.overrides;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.List;

import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.inference.junctionTree.JunctionTreeAlgorithm;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JayesOverrideModel implements IOverrideModel {

    private JunctionTreeAlgorithm junctionTreeAlgorithm;
    private ITypeName typeName;
    private BayesNode patternNode;
    private HashMap<IMethodName, BayesNode> methodNodes;

    protected JayesOverrideModel(final ITypeName typeName, final BayesNet network, final BayesNode patternNode,
            final List<BayesNode> methodNodes) {
        this.typeName = typeName;
        this.patternNode = patternNode;
        junctionTreeAlgorithm = new JunctionTreeAlgorithm();
        junctionTreeAlgorithm.setNetwork(network);
        createMethodNameMapping(methodNodes);
    }

    private void createMethodNameMapping(List<BayesNode> methods) {
        methodNodes = Maps.newHashMap();
        for (final BayesNode methodNode : methods) {
            methodNodes.put(VmMethodName.get(methodNode.getName()), methodNode);
        }
    }

    @Override
    public void reset() {
        junctionTreeAlgorithm.setEvidence(new HashMap<BayesNode, String>());
    }

    @Override
    public ITypeName getType() {
        return typeName;
    }

    @Override
    public ImmutableSet<String> getKnownPatterns() {
        return ImmutableSet.copyOf(patternNode.getOutcomes());
    }

    @Override
    public ImmutableSet<IMethodName> getKnownMethods() {
        return ImmutableSet.copyOf(methodNodes.keySet());
    }

    @Override
    public void setObservedMethod(final IMethodName methodName) {
        final BayesNode methodNode = methodNodes.get(methodName);
        if (methodNode != null) {
            junctionTreeAlgorithm.addEvidence(methodNode, "true");
        }
    }

    @Override
    public List<Recommendation<IMethodName>> recommendOverrides() {
        final List<Recommendation<IMethodName>> recommendations = Lists.newLinkedList();
        for (final BayesNode node : methodNodes.values()) {
            if (junctionTreeAlgorithm.getEvidence().containsKey(node)) {
                continue;
            }
            final double probability = junctionTreeAlgorithm.getBeliefs(node)[0];
            final IMethodName method = VmMethodName.get(node.getName());
            recommendations.add(Recommendation.newRecommendation(method, probability));
        }
        return recommendations;
    }

    @Override
    public String toString() {
        return format("Model for '%s'", typeName);
    }
}
