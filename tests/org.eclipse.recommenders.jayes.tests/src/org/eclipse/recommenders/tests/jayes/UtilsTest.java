/*******************************************************************************
 * Copyright (c) 2012 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Michael Kutschke - initial API and implementation
 ******************************************************************************/
package org.eclipse.recommenders.tests.jayes;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.recommenders.internal.jayes.util.AddressCalc;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.util.BayesNodeUtil;
import org.eclipse.recommenders.jayes.util.MathUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class UtilsTest {

    private static final int[] testVector = new int[] { 2, 3, 5, 7 };
    private static final double[] testDoubleVector = new double[] { 2.0, 3.0, 5.0, 7.0 };
    private static final double TOLERANCE = 1e-5;

    @Test
    public void testSum() {
        assertEquals(MathUtils.sum(testDoubleVector), 17.0, TOLERANCE);
    }

    @Test
    public void testProductOfRange() {
        assertEquals(MathUtils.productOfRange(testVector, 1, 3), 3 * 5);
        assertEquals(MathUtils.productOfRange(testVector, 0, 1), 2);
        assertEquals(MathUtils.productOfRange(testVector, 2, 2), 1);

        // having a result of 1 if start > end is consistent,
        // because the set of numbers that are multiplied is still empty,
        // as in the case start == end
        assertEquals(MathUtils.productOfRange(testVector, 3, 2), 1);
    }

    @Test
    public void testProduct() {
        assertEquals(MathUtils.product(testVector), 2 * 3 * 5 * 7);
    }

    @Test
    public void testScalarProduct() {
        assertEquals(MathUtils.scalarProduct(testVector, testVector), 2 * 2 + 3 * 3 + 5 * 5 + 7 * 7);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testScalaProductFailure() {
        int[] pair = new int[2];
        int[] triple = new int[3];
        MathUtils.scalarProduct(pair, triple);
    }

    @Test
    public void testNormalize() {
        assertArrayEquals(new double[] { 2.0 / 17.0, 3.0 / 17.0, 5.0 / 17.0, 7.0 / 17.0 },
                MathUtils.normalize(testDoubleVector.clone()), TOLERANCE);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testMultiDimensionalCounter() {
        int[] dimensions = new int[] { 3, 2, 1 };
        int[] counter = new int[] { 0, 0, -1 };

        AddressCalc.incrementMultiDimensionalCounter(counter, dimensions);

        assertArrayEquals(new int[] { 0, 0, 0 }, counter);

        AddressCalc.incrementMultiDimensionalCounter(counter, dimensions);

        assertArrayEquals(new int[] { 0, 1, 0 }, counter);

        AddressCalc.incrementMultiDimensionalCounter(counter, dimensions);

        assertArrayEquals(new int[] { 1, 0, 0 }, counter);

        AddressCalc.incrementMultiDimensionalCounter(counter, dimensions);

        assertArrayEquals(new int[] { 1, 1, 0 }, counter);

        AddressCalc.incrementMultiDimensionalCounter(counter, dimensions);

        assertArrayEquals(new int[] { 2, 0, 0 }, counter);

        AddressCalc.incrementMultiDimensionalCounter(counter, dimensions);

        assertArrayEquals(new int[] { 2, 1, 0 }, counter);

        thrown.expect(ArrayIndexOutOfBoundsException.class);

        AddressCalc.incrementMultiDimensionalCounter(counter, dimensions);
    }

    @Test
    public void testMarginalize() {
        BayesNet net = NetExamples.testNet1();

        BayesNode a = net.getNode("a");
        BayesNode b = net.getNode("b");
        BayesNode c = net.getNode("c");
        BayesNode d = net.getNode("d");

        Map<BayesNode, String> evidence = new HashMap<BayesNode, String>();
        assertArrayEquals(new double[] { 0.2, 0.8 }, a.marginalize(evidence), TOLERANCE);
        assertArrayEquals(new double[] { 0.2, 0.4, 0.4 }, b.marginalize(evidence), TOLERANCE);
        assertArrayEquals(new double[] { 0.25, 0.75 }, c.marginalize(evidence), TOLERANCE);
        assertArrayEquals(new double[] { 0.35, 0.65 }, d.marginalize(evidence), TOLERANCE);

        evidence.put(a, "true");
        evidence.put(b, "le");
        evidence.put(c, "true");
        evidence.put(d, "true");

        assertArrayEquals(new double[] { 0.2, 0.8 }, a.marginalize(evidence), TOLERANCE);
        assertArrayEquals(new double[] { 0.1, 0.4, 0.5 }, b.marginalize(evidence), TOLERANCE);
        assertArrayEquals(new double[] { 0.0, 1.0 }, c.marginalize(evidence), TOLERANCE);
        assertArrayEquals(new double[] { 0.5, 0.5 }, d.marginalize(evidence), TOLERANCE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSelectSubCptOnIncompleteEvidence() {
        BayesNet net = NetExamples.testNet1();

        Map<BayesNode, String> evidence = new HashMap<BayesNode, String>();

        BayesNodeUtil.getSubCpt(net.getNode("c"), evidence);
    }

    @Test
    public void testSelectSubCptOnCompleteEvidence() {
        BayesNet net = NetExamples.testNet1();

        Map<BayesNode, String> evidence = new HashMap<BayesNode, String>();
        evidence.put(net.getNode("a"), "true");
        evidence.put(net.getNode("b"), "le");

        assertArrayEquals(new double[] { 0.0, 1.0 }, BayesNodeUtil.getSubCpt(net.getNode("c"), evidence), TOLERANCE);
    }

}
