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

import static org.eclipse.recommenders.utils.Checks.ensureIsGreaterOrEqualTo;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.stat.StatUtils;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.Lists;

public class JayesOverrideModelBuilder {

    private static final double MIN = 0.0001;
    private static final double MAX = 1.0 - MIN;

    private final ITypeName typeName;

    private final BayesNet network;

    private final Collection<OverrideObservation> overriddenMethods;

    private int totalNumberOfSubtypesFound;

    private BayesNode patternNode;

    private LinkedList<BayesNode> methodNodes;

    public JayesOverrideModelBuilder(final ITypeName typeName, final Collection<OverrideObservation> overriddenMethods) {
        this.typeName = typeName;
        this.overriddenMethods = overriddenMethods;
        // filterInfrequentOverridingPatterns();
        ensureIsGreaterOrEqualTo(overriddenMethods.size(), 1, "at least one observation is required");
        computeTotalNumberOfSubtypes();
        network = new BayesNet();
    }

    private void computeTotalNumberOfSubtypes() {
        for (final OverrideObservation usage : overriddenMethods) {
            totalNumberOfSubtypesFound += usage.frequency;
        }
    }

    public IOverrideModel build() {
        createPatternNodeInNetwork();
        createMethodNodes();
        return new JayesOverrideModel(typeName, network, patternNode, methodNodes);
    }

    private void createPatternNodeInNetwork() {
        patternNode = network.createNode("patternNode");
        patternNode.addOutcome("none");

        final double[] def = new double[1 + overriddenMethods.size()];
        def[0] = MIN;
        int i = 0;
        for (final OverrideObservation obs : overriddenMethods) {
            i++;
            final String name = "observation_" + i;
            patternNode.addOutcome(name);
            final double priorPatternProbability = obs.frequency / (double) totalNumberOfSubtypesFound;
            def[i] = priorPatternProbability;
        }
        scaleMaximalValue(def);
        patternNode.setProbabilities(def);
    }

    private void scaleMaximalValue(final double[] subDefinition) {
        final double diff = StatUtils.sum(subDefinition) - 1.0;
        final double max = StatUtils.max(subDefinition);
        final int indexOf = ArrayUtils.indexOf(subDefinition, max);
        subDefinition[indexOf] = subDefinition[indexOf] - diff;
    }

    private void createMethodNodes() {
        final Set<IMethodName> methods = collectInvokedMethodsFromPatterns();
        methodNodes = Lists.newLinkedList();
        for (final IMethodName ref : methods) {
            final BayesNode methodNode = network.createNode(ref.getIdentifier());
            methodNode.setParents(Lists.newArrayList(patternNode));
            methodNode.addOutcome("true");
            methodNode.addOutcome("false");
            methodNode.setProbabilities(createMethodNodeDefinition(ref));
            methodNodes.add(methodNode);
        }
    }

    private double[] createMethodNodeDefinition(final IMethodName ref) {
        final double[] definition = new double[2 + 2 * overriddenMethods.size()];
        definition[0] = 0.0;
        definition[1] = 1.0;
        int i = 2;
        for (final OverrideObservation pattern : overriddenMethods) {
            final boolean overridesMethod = pattern.overriddenMethods.contains(ref);
            if (overridesMethod) {
                definition[i++] = MAX;
                definition[i++] = MIN;
            } else {
                // just flip
                definition[i++] = MIN;
                definition[i++] = MAX;
            }
        }
        return definition;
    }

    private TreeSet<IMethodName> collectInvokedMethodsFromPatterns() {
        final TreeSet<IMethodName> methods = new TreeSet<IMethodName>();
        for (final OverrideObservation observation : overriddenMethods) {
            methods.addAll(observation.overriddenMethods);
        }
        return methods;
    }
}
