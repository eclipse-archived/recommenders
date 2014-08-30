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
package org.eclipse.recommenders.jayes.io;

import static org.eclipse.recommenders.jayes.io.Equality.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.recommenders.commons.bayesnet.CommonsReader;
import org.eclipse.recommenders.commons.bayesnet.CommonsWriter;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.io.jbif.JayesBifReader;
import org.eclipse.recommenders.jayes.io.jbif.JayesBifWriter;
import org.eclipse.recommenders.jayes.io.xdsl.XDSLReader;
import org.eclipse.recommenders.jayes.io.xdsl.XDSLWriter;
import org.eclipse.recommenders.jayes.io.xmlbif.XMLBIFReader;
import org.eclipse.recommenders.jayes.io.xmlbif.XMLBIFWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class RoundTripTest {

    private final Class<? extends IBayesNetReader> readerClass;
    private final Class<? extends IBayesNetWriter> writerClass;

    public RoundTripTest(Class<? extends IBayesNetReader> readerClass, Class<? extends IBayesNetWriter> writerClass) {
        this.readerClass = readerClass;
        this.writerClass = writerClass;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(scenario(JayesBifReader.class, JayesBifWriter.class));
        scenarios.add(scenario(XDSLReader.class, XDSLWriter.class));
        scenarios.add(scenario(XMLBIFReader.class, XMLBIFWriter.class));
        scenarios.add(scenario(CommonsReader.class, CommonsWriter.class));

        return scenarios;
    }

    private static Object[] scenario(Class<? extends IBayesNetReader> readerClass,
            Class<? extends IBayesNetWriter> writerClass) {
        return new Object[] { readerClass, writerClass };
    }

    @Test
    public void testTopologicallySortedNetwork() throws Exception {
        BayesNet netBefore = new BayesNet();
        BayesNode a = netBefore.createNode("A");
        a.addOutcomes("t", "f");
        a.setProbabilities(0.4, 0.6);

        BayesNode b = netBefore.createNode("B");
        b.addOutcomes("t", "f");
        b.setParents(Arrays.asList(a));
        b.setProbabilities(0.4, 0.6, 0.7, 0.3);

        BayesNet netAfter = read(write(netBefore));

        assertThat(netAfter, is(equalTo(netBefore)));
    }

    /**
     * Tests a network where the nodes are not topologically sorted. The data format should not depend on the assumption
     * that parents have a lower ID than their children.
     */
    @Test
    public void testNonTopologicallySortedNetwork() throws Exception {
        BayesNet netBefore = new BayesNet();
        BayesNode a = netBefore.createNode("A");
        a.addOutcomes("t", "f");

        BayesNode b = netBefore.createNode("B");
        b.addOutcomes("t", "f");

        a.setParents(Arrays.asList(b));

        a.setProbabilities(0.4, 0.6, 0.7, 0.3);
        b.setProbabilities(0.4, 0.6);

        BayesNet netAfter = read(write(netBefore));

        assertThat(netAfter, is(equalTo(netBefore)));
    }

    @Test
    public void testNetworkWith3Outcomes() throws Exception {
        BayesNet netBefore = new BayesNet();
        BayesNode a = netBefore.createNode("A");
        a.addOutcomes("t", "f", "a");
        a.setProbabilities(0.3, 0.3, 0.4);

        BayesNode b = netBefore.createNode("B");
        b.addOutcomes("t", "f", "u");
        b.setParents(Arrays.asList(a));
        b.setProbabilities(0.4, 0.6, 0.0, 0.0, 0.7, 0.3, 0.1, 0.2, 0.7);

        BayesNet netAfter = read(write(netBefore));

        assertThat(netAfter, is(equalTo(netBefore)));
    }

    @Test
    public void testEscapes() throws Exception {
        BayesNet netBefore = new BayesNet();
        BayesNode node1 = netBefore.createNode("i am spec/al");
        node1.addOutcomes(" ", "/", "#", "+", "ü");
        node1.setProbabilities(0.1, 0.1, 0.1, 0.1, 0.6);

        BayesNet netAfter = read(write(netBefore));

        assertThat(netAfter, is(equalTo(netBefore)));
    }

    private BayesNet read(InputStream in) throws Exception {
        IBayesNetReader reader = readerClass.getConstructor(InputStream.class).newInstance(in);
        BayesNet net = reader.read();
        reader.close();
        return net;
    }

    private InputStream write(BayesNet net) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IBayesNetWriter writer = writerClass.getConstructor(OutputStream.class).newInstance(out);
        writer.write(net);
        writer.close();
        return new ByteArrayInputStream(out.toByteArray());
    }
}
