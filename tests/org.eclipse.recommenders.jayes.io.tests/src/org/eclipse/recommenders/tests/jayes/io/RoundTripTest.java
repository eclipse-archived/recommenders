/*******************************************************************************
 * Copyright (c) 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andreas Sewe - initial API and implementation
 ******************************************************************************/
package org.eclipse.recommenders.tests.jayes.io;

import static org.eclipse.recommenders.tests.jayes.io.utils.Equality.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.io.JayesBifReader;
import org.eclipse.recommenders.jayes.io.JayesBifWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class RoundTripTest {

    private final Class<?> readerClass;
    private final Class<?> writerClass;

    public RoundTripTest(Class<?> readerClass, Class<?> writerClass) {
        this.readerClass = readerClass;
        this.writerClass = writerClass;
    }

    @Parameters
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(scenario(JayesBifReader.class, JayesBifWriter.class));

        return scenarios;
    }

    private static Object[] scenario(Class<JayesBifReader> readerClass, Class<JayesBifWriter> writerClass) {
        return new Object[] { readerClass, writerClass };
    }

    @Test
    public void testTopologicallySortedNetwork() throws Exception {
        BayesNet netBefore = new BayesNet();
        BayesNode a = netBefore.createNode("A");
        a.addOutcomes("t", "f");
        a.setProbabilities(0.5, 0.5);

        BayesNode b = netBefore.createNode("B");
        b.addOutcomes("t", "f");
        b.setParents(Arrays.asList(a));
        b.setProbabilities(0.5, 0.5, 0.5, 0.5);

        BayesNet netAfter = read(write(netBefore));

        assertThat(netAfter, is(equalTo(netBefore)));
    }

    /**
     * Tests a network where the nodes are not topologically sorted. The data format should not depend on the assumption
     * that parents have a lower ID than their children.
     */
    @Test
    public void nonTopologicallySortedNetworkTest() throws Exception {
        BayesNet netBefore = new BayesNet();
        BayesNode a = netBefore.createNode("A");
        a.addOutcomes("t", "f");

        BayesNode b = netBefore.createNode("B");
        b.addOutcomes("t", "f");

        a.setParents(Arrays.asList(b));

        a.setProbabilities(0.5, 0.5, 0.5, 0.5);
        b.setProbabilities(0.5, 0.5);

        BayesNet netAfter = read(write(netBefore));

        assertThat(netAfter, is(equalTo(netBefore)));
    }

    private BayesNet read(InputStream in) throws Exception {
        Object reader = readerClass.getConstructor(InputStream.class).newInstance(in);
        BayesNet net = (BayesNet) readerClass.getMethod("read").invoke(reader);
        readerClass.getMethod("close").invoke(reader);
        return net;
    }

    private InputStream write(BayesNet net) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Object writer = writerClass.getConstructor(OutputStream.class).newInstance(out);
        writerClass.getMethod("write", BayesNet.class).invoke(writer, net);
        writerClass.getMethod("close").invoke(writer);
        return new ByteArrayInputStream(out.toByteArray());
    }
}
