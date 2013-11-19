/**
 * Copyright (c) 2011 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Michael Kutschke - initial API and implementation.
 */
package org.eclipse.recommenders.tests.jayes;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.factor.AbstractFactor;
import org.eclipse.recommenders.jayes.factor.FactorFactory;
import org.eclipse.recommenders.jayes.inference.IBayesInferer;
import org.eclipse.recommenders.jayes.inference.junctionTree.JunctionTreeAlgorithm;
import org.eclipse.recommenders.tests.jayes.lbp.LoopyBeliefPropagation;
import org.junit.Test;

public class JunctionTreeTest {

    private static final double TOLERANCE = 1e-2;

    @Test
    public void testInference1() {
        BayesNet net = NetExamples.testNet1();
        BayesNode a = net.getNode("a");
        BayesNode b = net.getNode("b");

        IBayesInferer inference = new JunctionTreeAlgorithm();
        inference.addEvidence(a, "false");
        inference.addEvidence(b, "lu");
        inference.setNetwork(net);

        IBayesInferer compare = new LoopyBeliefPropagation();
        compare.setNetwork(net);
        compare.addEvidence(a, "false");
        compare.addEvidence(b, "lu");

        for (BayesNode n : net.getNodes()) {
            assertArrayEquals(compare.getBeliefs(n), inference.getBeliefs(n), TOLERANCE);
        }
    }

    @Test
    public void testLogScale() {
        BayesNet net = NetExamples.testNet1();
        BayesNode a = net.getNode("a");
        BayesNode b = net.getNode("b");

        JunctionTreeAlgorithm inferer = new JunctionTreeAlgorithm();
        inferer.getFactory().setUseLogScale(true);
        inferer.addEvidence(a, "false");
        inferer.addEvidence(b, "lu");
        inferer.setNetwork(net);

        IBayesInferer compare = new LoopyBeliefPropagation();
        compare.setNetwork(net);
        compare.addEvidence(a, "false");
        compare.addEvidence(b, "lu");

        for (BayesNode n : net.getNodes()) {
            assertArrayEquals(compare.getBeliefs(n), inferer.getBeliefs(n), TOLERANCE);
        }
    }

    @Test
    public void testMixedScale() {
        BayesNet net = NetExamples.testNet1();
        BayesNode a = net.getNode("a");
        BayesNode b = net.getNode("b");

        JunctionTreeAlgorithm inferer = new JunctionTreeAlgorithm();
        // this will make the a,b,c clique log scale but the
        // c,d clique normal
        inferer.setFactorFactory(new FactorFactory() {
            @Override
            protected boolean getUseLogScale(AbstractFactor f) {
                return f.getDimensions().length > 2;
            }
        });
        inferer.addEvidence(a, "false");
        inferer.addEvidence(b, "lu");
        inferer.setNetwork(net);

        IBayesInferer compare = new LoopyBeliefPropagation();
        compare.setNetwork(net);
        compare.addEvidence(a, "false");
        compare.addEvidence(b, "lu");

        for (BayesNode n : net.getNodes()) {
            assertArrayEquals(compare.getBeliefs(n), inferer.getBeliefs(n), TOLERANCE);
        }
    }

    @Test
    public void testFailedCase1() {
        BayesNet net = NetExamples.testNet1();

        BayesNode a = net.getNode("a");
        BayesNode b = net.getNode("b");
        BayesNode c = net.getNode("c");

        JunctionTreeAlgorithm inferer = new JunctionTreeAlgorithm();
        inferer.setNetwork(net);

        Map<BayesNode, String> evidence = new HashMap<BayesNode, String>();
        evidence.put(a, "false");
        evidence.put(c, "true");
        inferer.setEvidence(evidence);
        assertEquals(0.22, inferer.getBeliefs(b)[0], TOLERANCE);
    }

    @Test
    public void testUnconnected() {
        BayesNet net = NetExamples.unconnectedNet();
        BayesNode a = net.getNode("a");
        BayesNode b = net.getNode("b");

        IBayesInferer inference = new JunctionTreeAlgorithm();
        inference.addEvidence(a, "false");
        inference.addEvidence(b, "true");
        inference.setNetwork(net);

        IBayesInferer compare = new LoopyBeliefPropagation();
        compare.setNetwork(net);
        compare.addEvidence(a, "false");
        compare.addEvidence(b, "true");

        for (BayesNode n : net.getNodes()) {
            assertArrayEquals(inference.getBeliefs(n), compare.getBeliefs(n), TOLERANCE);
        }
    }

    @Test
    public void testSparseFactors() {
        BayesNet net = NetExamples.sparseNet();

        BayesNode a = net.getNode("a");
        BayesNode b = net.getNode("b");

        IBayesInferer inference = new JunctionTreeAlgorithm();
        inference.addEvidence(a, "false");
        inference.addEvidence(b, "lu");
        inference.setNetwork(net);

        IBayesInferer compare = new LoopyBeliefPropagation();
        compare.setNetwork(net);
        compare.addEvidence(a, "false");
        compare.addEvidence(b, "lu");

        for (BayesNode n : net.getNodes()) {
            assertArrayEquals(compare.getBeliefs(n), inference.getBeliefs(n), TOLERANCE);
        }
    }

}
