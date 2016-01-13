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
package org.eclipse.recommenders.calls;

import static com.google.common.base.Optional.*;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.ImmutableSet.copyOf;
import static org.eclipse.recommenders.utils.Constants.*;
import static org.eclipse.recommenders.utils.Recommendation.newRecommendation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.inference.jtree.JunctionTreeAlgorithm;
import org.eclipse.recommenders.jayes.inference.jtree.JunctionTreeBuilder;
import org.eclipse.recommenders.jayes.io.IBayesNetReader;
import org.eclipse.recommenders.jayes.io.jbif.JayesBifReader;
import org.eclipse.recommenders.jayes.util.triangulation.MinDegree;
import org.eclipse.recommenders.utils.Constants;
import org.eclipse.recommenders.utils.IOUtils;
import org.eclipse.recommenders.utils.Nullable;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.names.IFieldName;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Lists;

/**
 * A thin wrapper around a {@link BayesianNetwork} for recommending method calls.
 * <p>
 * The Bayesian network is expected to follow the structure specified below:
 * <ul>
 * <li>every node must have at least <b>2 states</b>!
 * <li>the first state is supposed to be a dummy state. Call it like {@link Constants#DUMMY_METHOD}
 * <li>the second state <b>may</b> to be a dummy state too if no valuable other state could be found.
 * </ul>
 * <ul>
 * <li><b>callgroup node (formerly called pattern node):</b>
 * <ul>
 * <li>node name: {@link Constants#N_NODEID_CALL_GROUPS}
 * <li>state names: no constraints. Recommended schema is to use 'p'#someNumber.
 * </ul>
 * <li><b>context node:</b>
 * <ul>
 * <li>node name: {@link Constants#N_NODEID_CONTEXT}
 * <li>state names: fully-qualified method names as returned by {@link IMethodName#getIdentifier()}.
 * </ul>
 * <li><b>definition node:</b>
 * <ul>
 * <li>node name: {@link Constants#N_NODEID_DEF}
 * <li>state names: fully-qualified names as returned by {@link IMethodName#getIdentifier()} or
 * {@link IFieldName#getIdentifier()}.
 * </ul>
 * <li><b>definition kind node:</b>
 * <ul>
 * <li>node name: {@link Constants#N_NODEID_DEF_KIND}
 * <li>state names: one of {@link DefinitionKind}, i.e., METHOD_RETURN, NEW, FIELD, PARAMETER, THIS, UNKNOWN, or ANY
 * </ul>
 * <li><b>method call node:</b>
 * <ul>
 * <li>node name: {@link IMethodName#getIdentifier()}
 * <li>state names: {@link Constants#N_STATE_TRUE} or {@link Constants#N_STATE_FALSE}
 * </ul>
 * </ul>
 */
@SuppressWarnings("deprecation")
@Beta
public class JayesCallModel implements ICallModel {

    public static ICallModel load(InputStream is, ITypeName type) throws IOException {
        BayesNet net = getModel(is, type);
        return new JayesCallModel(type, net);
    }

    private static BayesNet getModel(InputStream is, ITypeName type) throws IOException {
        IBayesNetReader rdr = new JayesBifReader(is);
        try {
            return rdr.read();
        } finally {
            IOUtils.closeQuietly(rdr);
        }
    }

    private static final class StringToMethodNameFunction implements Function<String, IMethodName> {

        @Override
        public IMethodName apply(final String input) {
            return VmMethodName.get(input);
        }
    }

    private final BayesNet net;
    private final BayesNode callgroupNode;
    private final BayesNode overridesNode;
    private final BayesNode definedByNode;
    private final BayesNode defKindNode;
    private final JunctionTreeAlgorithm junctionTree;

    private final ITypeName typeName;
    private final Map<IMethodName, BayesNode> callNodes;

    private static final List<String> SPECIAL_NODES = Arrays.asList(N_NODEID_CONTEXT, N_NODEID_CALL_GROUPS,
            N_NODEID_DEF_KIND, N_NODEID_DEF);

    public JayesCallModel(final ITypeName name, final BayesNet net) {
        this.net = net;
        this.typeName = name;
        this.callNodes = new HashMap<IMethodName, BayesNode>();
        this.junctionTree = new JunctionTreeAlgorithm();

        junctionTree.setJunctionTreeBuilder(JunctionTreeBuilder.forHeuristic(new MinDegree()));
        junctionTree.setNetwork(net);

        overridesNode = net.getNode(N_NODEID_CONTEXT);
        callgroupNode = net.getNode(N_NODEID_CALL_GROUPS);
        defKindNode = net.getNode(N_NODEID_DEF_KIND);
        definedByNode = net.getNode(N_NODEID_DEF);

        setCallNodes(net);
    }

    private void setCallNodes(BayesNet net) {
        for (BayesNode bayesNode : net.getNodes()) {
            String name = bayesNode.getName();
            if (!SPECIAL_NODES.contains(name)) {
                VmMethodName vmMethodName = VmMethodName.get(name);
                callNodes.put(vmMethodName, bayesNode);
            }
        }
    }

    private Optional<IMethodName> computeMethodNameFromState(final BayesNode node) {
        String stateId = junctionTree.getEvidence().get(node);
        if (stateId == null) {
            return absent();
        }
        return Optional.<IMethodName>of(VmMethodName.get(stateId));
    }

    @Override
    public ImmutableSet<IMethodName> getKnownCalls() {
        return ImmutableSet.<IMethodName>builder().addAll(callNodes.keySet()).build();
    }

    @Override
    public ImmutableSet<IMethodName> getKnownOverrideContexts() {
        Collection<IMethodName> tmp = transform(overridesNode.getOutcomes(), new StringToMethodNameFunction());
        return copyOf(tmp);
    }

    @Override
    public ImmutableSet<IMethodName> getKnownDefiningMethods() {
        Collection<IMethodName> tmp = transform(definedByNode.getOutcomes(), new StringToMethodNameFunction());
        return copyOf(tmp);
    }

    @Override
    public ImmutableSet<DefinitionKind> getKnownDefinitionKinds() {
        Builder<DefinitionKind> b = ImmutableSet.builder();
        for (String s : defKindNode.getOutcomes()) {
            b.add(DefinitionKind.valueOf(s));
        }
        return b.build();
    }

    @Override
    public ImmutableSet<String> getKnownPatterns() {
        return copyOf(callgroupNode.getOutcomes());
    }

    @Override
    public ImmutableSet<IMethodName> getObservedCalls() {
        Builder<IMethodName> builder = ImmutableSet.<IMethodName>builder();
        Map<BayesNode, String> evidence = junctionTree.getEvidence();
        for (Entry<IMethodName, BayesNode> pair : callNodes.entrySet()) {
            BayesNode node = pair.getValue();
            IMethodName method = pair.getKey();
            if (evidence.containsKey(node) && evidence.get(node).equals(Constants.N_STATE_TRUE)
            // remove the NULL that may have been introduced by
            // res.add(compute...)
                    && !VmMethodName.NULL.equals(method)) {
                builder.add(method);
            }
        }
        return builder.build();
    }

    @Override
    public Optional<IMethodName> getObservedDefiningMethod() {
        return computeMethodNameFromState(definedByNode);
    }

    @Override
    public Optional<IMethodName> getObservedOverrideContext() {
        return computeMethodNameFromState(overridesNode);
    }

    @Override
    public Optional<DefinitionKind> getObservedDefinitionKind() {
        String stateId = junctionTree.getEvidence().get(defKindNode);
        if (stateId == null) {
            return absent();
        }
        return of(DefinitionKind.valueOf(stateId));
    }

    @Override
    public List<Recommendation<IMethodName>> recommendCalls() {
        List<Recommendation<IMethodName>> recs = Lists.newLinkedList();
        for (Entry<IMethodName, BayesNode> entry : callNodes.entrySet()) {
            IMethodName method = entry.getKey();
            BayesNode bayesNode = entry.getValue();
            boolean isAlreadyUsedAsEvidence = junctionTree.getEvidence().containsKey(bayesNode);
            if (!isAlreadyUsedAsEvidence) {
                int indexForTrue = bayesNode.getOutcomeIndex(N_STATE_TRUE);
                double[] probabilities = junctionTree.getBeliefs(bayesNode);
                double probability = probabilities[indexForTrue];
                recs.add(newRecommendation(method, probability));
            }
        }
        return recs;
    }

    @Override
    public List<Recommendation<IMethodName>> recommendDefinitions() {
        List<Recommendation<IMethodName>> recs = Lists.newLinkedList();
        double[] beliefs = junctionTree.getBeliefs(definedByNode);
        for (int i = definedByNode.getOutcomeCount(); i-- > 0;) {
            if (beliefs[i] > 0.01d) {
                String outcomeName = definedByNode.getOutcomeName(i);
                if (outcomeName.equals(NONE_METHOD.getIdentifier())) {
                    continue;
                }
                if (outcomeName.equals(UNKNOWN_METHOD.getIdentifier())) {
                    continue;
                }
                VmMethodName definition = VmMethodName.get(outcomeName);
                Recommendation<IMethodName> r = newRecommendation(definition, beliefs[i]);
                recs.add(r);
            }
        }
        return recs;
    }

    @Override
    public List<Recommendation<String>> recommendPatterns() {
        List<Recommendation<String>> recs = Lists.newLinkedList();
        double[] probs = junctionTree.getBeliefs(callgroupNode);
        for (String outcome : callgroupNode.getOutcomes()) {
            int probIndex = callgroupNode.getOutcomeIndex(outcome);
            double p = probs[probIndex];
            recs.add(newRecommendation(outcome, p));
        }
        return recs;
    }

    @Override
    public ITypeName getReceiverType() {
        return typeName;
    }

    @Override
    public void reset() {
        junctionTree.getEvidence().clear();
    }

    @Override
    public boolean setObservedCalls(final Set<IMethodName> calls) {
        for (IMethodName m : getObservedCalls()) {
            // clear previously called methods by setting observation state to NULL
            setCalled(m, null);
        }
        if (calls == null) {
            return true;
        }

        boolean pass = true;
        for (IMethodName m : calls) {
            pass &= setCalled(m, N_STATE_TRUE);
        }
        // explicitly set the "no-method" used node to false:
        // TODO we disabled this for testing purpose:
        // pass &= setCalled(Constants.NO_METHOD, N_STATE_FALSE);
        return pass;
    }

    @Override
    public boolean setObservedDefiningMethod(@Nullable final IMethodName newDefinition) {
        if (newDefinition == null) {
            junctionTree.removeEvidence(definedByNode);
            return true;
        }
        // else:
        String identifier = newDefinition.getIdentifier();
        boolean contains = definedByNode.getOutcomes().contains(identifier);
        if (contains) {
            junctionTree.addEvidence(definedByNode, identifier);
        }
        return contains;
    }

    @Override
    public boolean setObservedOverrideContext(@Nullable final IMethodName newEnclosingMethod) {
        if (newEnclosingMethod == null) {
            junctionTree.removeEvidence(overridesNode);
            return true;
        }
        // else:
        String id = newEnclosingMethod.getIdentifier();
        boolean contains = overridesNode.getOutcomes().contains(id);
        if (contains) {
            junctionTree.addEvidence(overridesNode, id);
        }
        return contains;
    }

    @Override
    public boolean setObservedDefinitionKind(@Nullable final DefinitionKind newDef) {
        if (newDef == null) {
            junctionTree.removeEvidence(defKindNode);
            return true;
        }
        // else:
        String identifier = newDef.toString();
        boolean contains = defKindNode.getOutcomes().contains(identifier);
        if (contains) {
            junctionTree.addEvidence(defKindNode, identifier);
        }
        return contains;
    }

    @Override
    public boolean setObservedPattern(@Nullable final String patternName) {
        if (patternName == null) {
            junctionTree.removeEvidence(callgroupNode);
            return true;
        }
        // else:
        boolean contains = callgroupNode.getOutcomes().contains(patternName);
        if (contains) {
            junctionTree.addEvidence(callgroupNode, patternName);
        }
        return contains;
    }

    private boolean setCalled(final IMethodName m, @Nullable final String state) {
        IMethodName rebased = VmMethodName.rebase(typeName, m);
        BayesNode node = net.getNode(rebased.getIdentifier());
        if (node == null) {
            return false;
        }

        if (state == null) {
            junctionTree.removeEvidence(node);
        } else {
            junctionTree.addEvidence(node, state);
        }
        return true;
    }
}
