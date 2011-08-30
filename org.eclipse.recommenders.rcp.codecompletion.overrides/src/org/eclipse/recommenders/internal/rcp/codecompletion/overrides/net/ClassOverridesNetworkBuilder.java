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

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsGreaterOrEqualTo;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.stat.StatUtils;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;

import com.google.common.collect.Lists;

public class ClassOverridesNetworkBuilder {
    private static final double MIN = 0.0001;

    private static final double MAX = 1.0 - MIN;

    private final ITypeName typeName;

    private final BayesNet network;

    private final Collection<ClassOverridesObservation> overriddenMethods;

    private int totalNumberOfSubtypesFound;

    private BayesNode patternNode;

    private LinkedList<BayesNode> methodNodes;

    public ClassOverridesNetworkBuilder(final ITypeName typeName,
            final Collection<ClassOverridesObservation> overriddenMethods) {
        this.typeName = typeName;
        this.overriddenMethods = overriddenMethods;
        // filterInfrequentOverridingPatterns();
        ensureIsGreaterOrEqualTo(overriddenMethods.size(), 1, "at least one observation is required");
        computeTotalNumberOfSubtypes();
        network = new BayesNet();
    }

    private void filterInfrequentOverridingPatterns() {
        for (final Iterator<ClassOverridesObservation> it = overriddenMethods.iterator(); it.hasNext();) {
            final ClassOverridesObservation next = it.next();
            if (next.frequency < 5) {
                it.remove();
            }
        }
    }

    private void computeTotalNumberOfSubtypes() {
        for (final ClassOverridesObservation usage : overriddenMethods) {
            totalNumberOfSubtypesFound += usage.frequency;
        }
    }

    public ClassOverridesNetwork build() {
        return new ClassOverridesNetwork(typeName, network, patternNode, methodNodes);
    }

    public void createPatternsNode() {
        createPatternNodeInNetwork();
    }

    private void createPatternNodeInNetwork() {
        patternNode = new BayesNode("patternNode");
        network.addNode(patternNode);
        patternNode.addOutcome("none");
        //
        final ArrayDoubleList def = new ArrayDoubleList();
        def.add(MIN);
        int i = 0;
        for (final ClassOverridesObservation obs : overriddenMethods) {
            i++;
            final String name = "observation_" + String.valueOf(i);
            patternNode.addOutcome(name);
            final double priorPatternProbability = obs.frequency / (double) totalNumberOfSubtypesFound;
            def.add(priorPatternProbability);
        }
        scaleMaximalValue(def);
        patternNode.setProbabilities(def.toArray());
    }

    private void scaleMaximalValue(final ArrayDoubleList subDefinition) {
        final double[] values = subDefinition.toArray();
        final double diff = StatUtils.sum(values) - 1.0;
        final double max = StatUtils.max(values);
        final int indexOf = ArrayUtils.indexOf(values, max);
        subDefinition.set(indexOf, values[indexOf] - diff);
    }

    public void createMethodNodes() {
        final Set<IMethodName> methods = collectInvokedMethodsFromPatterns();
        methodNodes = Lists.newLinkedList();
        final int i = 1;
        for (final IMethodName ref : methods) {
            final BayesNode methodNode = new BayesNode(ref.getIdentifier());
            network.addNode(methodNode);
            methodNode.setParents(Lists.newArrayList(patternNode));
            methodNode.addOutcome("true");
            methodNode.addOutcome("false");
            methodNode.setProbabilities(createMethodNodeDefinition(ref));
            methodNodes.add(methodNode);
        }
    }

    private double[] createMethodNodeDefinition(final IMethodName ref) {
        final ArrayDoubleList definition = new ArrayDoubleList();
        definition.add(0.0);
        definition.add(1.0);
        for (final ClassOverridesObservation pattern : overriddenMethods) {
            final boolean overridesMethod = pattern.overriddenMethods.contains(ref);
            if (overridesMethod) {
                definition.add(MAX);
                definition.add(MIN);
            } else {
                // just flip
                definition.add(MIN);
                definition.add(MAX);
            }
        }
        return definition.toArray();
    }

    private TreeSet<IMethodName> collectInvokedMethodsFromPatterns() {
        final TreeSet<IMethodName> methods = new TreeSet<IMethodName>();
        for (final ClassOverridesObservation observation : overriddenMethods) {
            methods.addAll(observation.overriddenMethods);
        }
        return methods;
    }
}
