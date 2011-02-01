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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.stat.StatUtils;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

import smile.DocItemInfo;
import smile.Network;
import smile.Network.NodeType;

public class ClassOverridesNetworkBuilder {
    private static final double MIN = 0.0001;

    private static final double MAX = 1.0 - MIN;

    private final ITypeName typeName;

    private final Network network;

    private final Collection<ClassOverridesObservation> overriddenMethods;

    private int totalNumberOfSubtypesFound;

    public ClassOverridesNetworkBuilder(final ITypeName typeName,
            final Collection<ClassOverridesObservation> overriddenMethods) {
        this.typeName = typeName;
        this.overriddenMethods = overriddenMethods;
        // filterInfrequentOverridingPatterns();
        ensureIsGreaterOrEqualTo(overriddenMethods.size(), 1, "at least one observation is required");
        computeTotalNumberOfSubtypes();
        network = new Network();
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
        return new ClassOverridesNetwork(typeName, network);
    }

    public PatternNode createPatternsNode() {
        createPatternNodeInNetwork();
        return new PatternNode(network);
    }

    private void createPatternNodeInNetwork() {
        network.addNode(Network.NodeType.Cpt, PatternNode.ID);
        network.setNodeName(PatternNode.ID, "Patterns  ");
        network.addOutcome(PatternNode.ID, "none");
        //
        final ArrayDoubleList def = new ArrayDoubleList();
        def.add(MIN);
        int i = 0;
        for (final ClassOverridesObservation obs : overriddenMethods) {
            i++;
            final String name = "observation_" + String.valueOf(i);
            network.addOutcome(PatternNode.ID, name);
            final double priorPatternProbability = obs.frequency / (double) totalNumberOfSubtypesFound;
            def.add(priorPatternProbability);
        }
        network.deleteOutcome(PatternNode.ID, 0);
        network.deleteOutcome(PatternNode.ID, 0);
        scaleMaximalValue(def);
        network.setNodeDefinition(PatternNode.ID, def.toArray());
    }

    private void scaleMaximalValue(final ArrayDoubleList subDefinition) {
        final double[] values = subDefinition.toArray();
        final double diff = StatUtils.sum(values) - 1.0;
        final double max = StatUtils.max(values);
        final int indexOf = ArrayUtils.indexOf(values, max);
        subDefinition.set(indexOf, values[indexOf] - diff);
    }

    public void saveNetwork() {
        network.writeFile("debug.xdsl");
    }

    public List<MethodNode> createMethodNodes() {
        final Set<IMethodName> methods = collectInvokedMethodsFromPatterns();
        final LinkedList<MethodNode> res = new LinkedList<MethodNode>();
        int i = 1;
        for (final IMethodName ref : methods) {
            final String nodeId = "m" + i++;
            network.addNode(NodeType.Cpt, nodeId);
            network.setNodeName(nodeId, ref.toString());
            network.addArc(PatternNode.ID, nodeId);
            network.setOutcomeId(nodeId, 0, "true");
            network.setOutcomeId(nodeId, 1, "false");
            createMethodNodeDefinition(ref, nodeId);
            createMethodNodeDocumentation(ref, nodeId);
            res.add(new MethodNode(network, nodeId));
        }
        return res;
    }

    private void createMethodNodeDocumentation(final IMethodName ref, final String nodeId) {
        final DocItemInfo IMethodNameInfo = new DocItemInfo("IMethodName", ref.getIdentifier());
        network.setNodeDocumentation(nodeId, new DocItemInfo[] { IMethodNameInfo });
    }

    private void createMethodNodeDefinition(final IMethodName ref, final String nodeId) {
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
        network.setNodeDefinition(nodeId, definition.toArray());
    }

    private TreeSet<IMethodName> collectInvokedMethodsFromPatterns() {
        final TreeSet<IMethodName> methods = new TreeSet<IMethodName>();
        for (final ClassOverridesObservation observation : overriddenMethods) {
            methods.addAll(observation.overriddenMethods);
        }
        return methods;
    }
}
