/**
 * Copyright (c) 2017 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.statics;

import static org.eclipse.recommenders.utils.Constants.N_STATE_TRUE;
import static org.eclipse.recommenders.utils.Recommendation.newRecommendation;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.inference.jtree.JunctionTreeAlgorithm;
import org.eclipse.recommenders.jayes.inference.jtree.JunctionTreeBuilder;
import org.eclipse.recommenders.jayes.io.IBayesNetReader;
import org.eclipse.recommenders.jayes.io.jbif.JayesBifReader;
import org.eclipse.recommenders.jayes.util.triangulation.MinDegree;
import org.eclipse.recommenders.utils.IOUtils;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;

import com.google.common.annotations.Beta;

/**
 * A thin wrapper around a {@link BayesianNetwork} for recommending static method calls.
 */
@Beta
public class JayesStaticsModel implements IStaticsModel {

    private static final String VERBS_NODE_NAME = "method-verbs";
    private static final String VERB_OUTCOME_PRIOR = "#prior";

    public static IStaticsModel load(InputStream is, ITypeName type) throws IOException {
        BayesNet net = getModel(is, type);
        return new JayesStaticsModel(type, net);
    }

    private static BayesNet getModel(InputStream is, ITypeName type) throws IOException {
        IBayesNetReader rdr = new JayesBifReader(is);
        try {
            return rdr.read();
        } finally {
            IOUtils.closeQuietly(rdr);
        }
    }

    private final BayesNet net;
    private final BayesNode verbNode;
    private final JunctionTreeAlgorithm junctionTree;

    private final ITypeName declaringType;

    public JayesStaticsModel(final ITypeName name, final BayesNet net) {
        this.net = net;
        this.declaringType = name;
        this.junctionTree = new JunctionTreeAlgorithm();

        junctionTree.setJunctionTreeBuilder(JunctionTreeBuilder.forHeuristic(new MinDegree()));
        junctionTree.setNetwork(net);

        verbNode = net.getNode(VERBS_NODE_NAME);
    }

    @Override
    public boolean setEnclosingMethod(IMethodName context) {
        if (context == null) {
            junctionTree.addEvidence(verbNode, VERB_OUTCOME_PRIOR);
            return false;
        }
        String verb = MethodNameUtils.extractVerb(context.getName()).orNull();
        if (verb == null) {
            junctionTree.addEvidence(verbNode, VERB_OUTCOME_PRIOR);
            return false;
        } else if (verbNode.getOutcomes().contains(verb)) {
            junctionTree.addEvidence(verbNode, verb);
            return true;
        } else {
            junctionTree.addEvidence(verbNode, VERB_OUTCOME_PRIOR);
            return false;
        }
    }

    @Override
    public List<Recommendation<IMethodName>> recommendCalls() {
        List<Recommendation<IMethodName>> recommendations = new LinkedList<>();
        Map<BayesNode, String> evidence = junctionTree.getEvidence();
        for (BayesNode node : net.getNodes()) {
            if (node == verbNode) {
                continue;
            }
            boolean isAlreadyUsedAsEvidence = evidence.containsKey(node);
            if (!isAlreadyUsedAsEvidence) {
                int indexForTrue = node.getOutcomeIndex(N_STATE_TRUE);
                double[] probabilities = junctionTree.getBeliefs(node);
                double probability = probabilities[indexForTrue];
                IMethodName method = VmMethodName.get(node.getName());
                recommendations.add(newRecommendation(method, probability));
            }
        }
        return recommendations;
    }

    @Override
    public ITypeName getDeclaringType() {
        return declaringType;
    }

    @Override
    public void reset() {
        junctionTree.getEvidence().clear();
    }

    @Override
    public double recommendCall(IMethodName method) {
        BayesNode node = net.getNode(method.getIdentifier());
        if (node == null) {
            return 0;
        }
        int indexForTrue = node.getOutcomeIndex(N_STATE_TRUE);
        double[] probabilities = junctionTree.getBeliefs(node);
        return probabilities[indexForTrue];
    }
}
