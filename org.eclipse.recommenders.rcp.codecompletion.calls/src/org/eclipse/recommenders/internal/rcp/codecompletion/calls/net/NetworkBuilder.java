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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.util.MathUtils;
import org.eclipse.recommenders.commons.utils.Bag;
import org.eclipse.recommenders.commons.utils.HashBag;
import org.eclipse.recommenders.commons.utils.annotations.Clumsy;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;

import smile.DocItemInfo;
import smile.Network;
import smile.Network.NodeType;

import com.google.common.collect.Lists;

@Clumsy
public class NetworkBuilder {
    private static final IMethodName UNKNOWN_METHOD = VmMethodName.get("LUnknown.method()V");

    private static final double MIN = 0.0001;

    private static final double MAX = 1.0 - MIN;

    private final List<InstanceUsage> typeUsages;

    private Bag<IMethodName> methodContexts = HashBag.newHashBag();

    private final Network network;

    private final ITypeName typeName;

    private int availibilityNodeId;

    private int contextNodeId;

    private int patternNodeId;

    public NetworkBuilder(final ITypeName typeName, final List<InstanceUsage> typeUsages) {
        this.typeName = typeName;
        this.typeUsages = typeUsages;
        network = new Network();
        computeMethodContextFrequencies();
    }

    private void computeMethodContextFrequencies() {
        methodContexts = HashBag.newHashBag();
        for (final InstanceUsage usage : typeUsages) {
            for (final Entry<IMethodName, Integer> entry : usage.observedContexts.entrySet()) {
                methodContexts.add(entry.getKey(), entry.getValue());
            }
        }
        int unknownFrequency = 0;
        for (final Iterator<IMethodName> it = methodContexts.iterator(); it.hasNext();) {
            final IMethodName ctx = it.next();
            final int count = methodContexts.count(ctx);
            if (count < 20) {
                it.remove();
                unknownFrequency += count;
            }
        }
        methodContexts.add(UNKNOWN_METHOD, unknownFrequency);
    }

    public void createContextNode() {
        createContextNodeInNetwork();
        createContextNodeOutcomesInNetwork();
        createContextNodeDefinitionInNetwork();
    }

    private void createContextNodeInNetwork() {
        contextNodeId = network.addNode(Network.NodeType.Cpt, ContextNode.NAME);
    }

    private Set<IMethodName> createContextNodeOutcomesInNetwork() {
        final Set<IMethodName> methodCtxs = methodContexts.elements();
        network.addOutcome(contextNodeId, "none");
        for (final IMethodName methodCtx : methodCtxs) {
            final String escapedOutcomeId = ObjectMethodCallsNet.escape(methodCtx);
            network.addOutcome(contextNodeId, escapedOutcomeId);
        }
        network.deleteOutcome(contextNodeId, 0);
        network.deleteOutcome(contextNodeId, 0);
        addContextNodeDocInfo(methodCtxs);
        return methodCtxs;
    }

    private void addContextNodeDocInfo(final Set<IMethodName> methodCtxs) {
        final StringBuilder sb = new StringBuilder();
        for (final IMethodName methodCtx : methodCtxs) {
            sb.append(ObjectMethodCallsNet.escape(methodCtx)).append(":").append(methodCtx.getIdentifier())
                    .append("\n");
        }
        final DocItemInfo info = new DocItemInfo(ContextNode.PROPERTY_ESCAPED_METHOD_REFERENCES, sb.toString());
        network.setNodeDocumentation(contextNodeId, new DocItemInfo[] { info });
    }

    private void createContextNodeDefinitionInNetwork() {
        final Set<IMethodName> ctxMethods = methodContexts.elements();
        final double[] definition = new double[ctxMethods.size() + 1];
        Arrays.fill(definition, MIN);
        definition[0] = 1 - MIN * ctxMethods.size();
        network.setNodeDefinition(contextNodeId, definition);
    }

    public void createAvailabilityNode() {
        createAvailabilityNodeInNetwork();
        createAvailabilityNodeDefinitionInNetwork();
    }

    private void createAvailabilityNodeInNetwork() {
        availibilityNodeId = network.addNode(NodeType.Cpt, AvailabilityNode.NAME);
        network.addArc(contextNodeId, availibilityNodeId);
        network.setOutcomeId(availibilityNodeId, 0, "true");
        network.setOutcomeId(availibilityNodeId, 1, "false");
    }

    private void createAvailabilityNodeDefinitionInNetwork() {
        final ArrayDoubleList probabilities = new ArrayDoubleList();
        probabilities.add(0.0);
        final double totalCtxCount = methodContexts.totalElementsCount();
        for (final IMethodName ctx : methodContexts.elements()) {
            final double ctxCount = methodContexts.count(ctx);
            double ctxProbability = ctxCount / totalCtxCount;
            ctxProbability = Math.max(MIN, MathUtils.round(ctxProbability, 3, BigDecimal.ROUND_FLOOR));
            probabilities.add(ctxProbability);
        }
        final double[] array = probabilities.toArray();
        final double[] res = new double[array.length * 2];
        for (int i = 0; i < array.length; i++) {
            res[2 * i] = array[i];
            res[2 * i + 1] = 1.0 - array[i];
        }
        network.setNodeDefinition(availibilityNodeId, res);
    }

    public void createPatternsNode() {
        createPatternNodeInNetwork();
        createPatternNodeOutcomesInNetwork();
        createPatternNodeDefinitionInNetwork();
    }

    private void createPatternNodeInNetwork() {
        patternNodeId = network.addNode(Network.NodeType.Cpt, PatternNode.NAME);
        network.setNodeName(patternNodeId, "Patterns");
        network.addArc(contextNodeId, patternNodeId);
        network.addArc(availibilityNodeId, patternNodeId);
    }

    private void createPatternNodeOutcomesInNetwork() {
        network.addOutcome(patternNodeId, "none");
        int i = 0;
        for (final InstanceUsage pattern : typeUsages) {
            i++;
            final String name = pattern.name == null ? "pattern" + String.valueOf(i) : pattern.name;
            addPatternNodeOutcome(name);
        }
        network.deleteOutcome(patternNodeId, 0);
        network.deleteOutcome(patternNodeId, 0);
    }

    private void addPatternNodeOutcome(final String name) {
        network.addOutcome(patternNodeId, name);
    }

    private void createPatternNodeDefinitionInNetwork() {
        final List<InstanceUsage> pPatterns = createPseudoPatterns();
        //
        final LinkedList<IMethodName> pContexts = createPseudoContexts();
        //
        final ArrayDoubleList definition = new ArrayDoubleList();
        // tuple: [ctx=0 & available?T, ctx=0 & avail=F ][ctx=1 & ...]
        for (final IMethodName ctx : pContexts) {
            final ArrayDoubleList subDefinition = new ArrayDoubleList();
            final double countOfAllUsagesInThisParticularContexts = methodContexts.count(ctx);
            for (final InstanceUsage pattern : pPatterns) {
                final double countUsagesOfThisPatternInThisParticularContext = getPatternCountInParticularContext(ctx,
                        pattern);
                double probabilityOfPatternInContext = countUsagesOfThisPatternInThisParticularContext == 0.0d ? 0.0d
                        : countUsagesOfThisPatternInThisParticularContext / countOfAllUsagesInThisParticularContexts;
                probabilityOfPatternInContext = ensureValidDoubleValue(probabilityOfPatternInContext);
                subDefinition.add(probabilityOfPatternInContext);
            }
            final double sum = StatUtils.sum(subDefinition.toArray()) - MIN;
            final double negation = ensureValidDoubleValue(1.0 - sum);
            subDefinition.set(0, negation);
            scaleMaximalValue(subDefinition);
            //
            subDefinition.add(1.0);
            for (int i = pPatterns.size() - 1; i-- > 0;) {
                subDefinition.add(0.0);
            }
            definition.addAll(subDefinition);
        }
        network.setNodeDefinition(patternNodeId, definition.toArray());
    }

    private void scaleMaximalValue(final ArrayDoubleList subDefinition) {
        final double[] values = subDefinition.toArray();
        final double diff = StatUtils.sum(values) - 1.0;
        final double max = StatUtils.max(values);
        final int indexOf = ArrayUtils.indexOf(values, max);
        subDefinition.set(indexOf, values[indexOf] - diff);
    }

    private double ensureValidDoubleValue(double probabilityOfPatternInContext) {
        if (Double.isNaN(probabilityOfPatternInContext) || Double.isInfinite(probabilityOfPatternInContext)) {
            probabilityOfPatternInContext = 0.0;
        }
        return Math.max(MIN, MathUtils.round(probabilityOfPatternInContext, 4, BigDecimal.ROUND_FLOOR));
    }

    private double getPatternCountInParticularContext(final IMethodName ctx, final InstanceUsage pattern) {
        final Map<IMethodName, Integer> contextCounter = pattern.observedContexts;
        final double patternInCtxCount = contextCounter.containsKey(ctx) ? contextCounter.get(ctx) : 0.0;
        return patternInCtxCount;
    }

    private LinkedList<IMethodName> createPseudoContexts() {
        final LinkedList<IMethodName> l2 = new LinkedList<IMethodName>(methodContexts.elements());
        l2.add(0, VmMethodName.get("Lnone/Existing.context()V"));
        return l2;
    }

    private List<InstanceUsage> createPseudoPatterns() {
        final List<InstanceUsage> l = Lists.newLinkedList(typeUsages);
        l.add(0, InstanceUsage.create("none"));
        return l;
    }

    public void saveNetwork() {
        network.writeFile("debug.xdsl");
    }

    public List<MethodNode> createMethodNodes() {
        final Set<IMethodName> methods = collectInvokedMethodsFromPatterns();
        final LinkedList<MethodNode> res = new LinkedList<MethodNode>();
        int i = 1;
        for (final IMethodName ref : methods) {
            final String nodeName = "m" + i++;
            final int methodNodeId = network.addNode(NodeType.Cpt, nodeName);
            network.setNodeName(nodeName, ref.toString());
            network.addArc(patternNodeId, methodNodeId);
            network.setOutcomeId(methodNodeId, 0, "true");
            network.setOutcomeId(methodNodeId, 1, "false");
            createMethodNodeDefinition(ref, methodNodeId);
            createMethodNodeDocumentation(ref, methodNodeId);
            res.add(new MethodNode(network, methodNodeId));
        }
        return res;
    }

    public ObjectMethodCallsNet build() {
        return new ObjectMethodCallsNet(typeName, network);
    }

    private void createMethodNodeDocumentation(final IMethodName ref, final int nodeId) {
        final DocItemInfo IMethodNameInfo = new DocItemInfo("IMethodName", ref.getIdentifier());
        network.setNodeDocumentation(nodeId, new DocItemInfo[] { IMethodNameInfo });
    }

    private void createMethodNodeDefinition(final IMethodName ref, final int nodeId) {
        final ArrayDoubleList definition = new ArrayDoubleList();
        definition.add(0.0);
        definition.add(1.0);
        for (final InstanceUsage usage : typeUsages) {
            final boolean callsMethod = doesPatternCallMethod(usage, ref);
            if (callsMethod) {
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

    private boolean doesPatternCallMethod(final InstanceUsage pattern, final IMethodName ref) {
        return pattern.invokedMethods.contains(ref);
    }

    private TreeSet<IMethodName> collectInvokedMethodsFromPatterns() {
        final TreeSet<IMethodName> methods = new TreeSet<IMethodName>();
        for (final InstanceUsage pattern : createPseudoPatterns()) {
            for (final IMethodName directive : pattern.invokedMethods) {
                methods.add(directive);
            }
        }
        return methods;
    }
}
