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
package org.eclipse.recommenders.tests.jayes.io;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.xml.HasXPath.hasXPath;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.inference.junctionTree.JunctionTreeAlgorithm;
import org.eclipse.recommenders.jayes.io.XDSLReader;
import org.eclipse.recommenders.jayes.io.XDSLWriter;
import org.eclipse.recommenders.jayes.io.XMLBIFReader;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class IOTest {

    @Test
    public void XMLBIFreaderTest() throws IOException {
        // tests whether parsing functions
        XMLBIFReader rdr = new XMLBIFReader(getClass().getResourceAsStream("/test/models/dog.xml"));
        BayesNet net = rdr.read();
        rdr.close();
        assertTrue(net != null);
        assertEquals(5, net.getNodes().size());
    }

    /**
     * assert that a network directly generated from GeNIe is (1) parsed correctly and (2) gives the same results as
     * GeNIe
     * 
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    @Test
    public void XDSLreaderTest() throws IOException {
        XDSLReader rdr = new XDSLReader(getClass().getResourceAsStream("/test/models/rain.xdsl"));

        BayesNet net = rdr.read();
        rdr.close();

        JunctionTreeAlgorithm jta = new JunctionTreeAlgorithm();
        jta.setNetwork(net);
        jta.addEvidence(net.getNode("grass_wet"), "yes");
        jta.addEvidence(net.getNode("neighbor_grass_wet"), "yes");

        // compare with computed results from GeNIe
        assertArrayEquals(new double[] { 0.7271, 0.2729 }, jta.getBeliefs(net.getNode("sprinkler_on")), 1e-4);
        assertArrayEquals(new double[] { 0.4596, 0.5404 }, jta.getBeliefs(net.getNode("rain")), 1e-4);

    }

    @Test
    public void XDSLWriterTest() throws Exception {
        XDSLReader rdr = new XDSLReader(getClass().getResourceAsStream("/test/models/rain.xdsl"));

        BayesNet net = rdr.read();
        rdr.close();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XDSLWriter wrtr = new XDSLWriter(out);
        wrtr.write(net);
        wrtr.close();
        String xdslRepresentation = out.toString();

        // check that there are no nested cpt's
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBldr = docBuilderFactory.newDocumentBuilder();
        Document doc = docBldr.parse(new InputSource(new StringReader(xdslRepresentation)));
        doc.normalize();

        assertThat(doc.getDocumentElement(), hasXPath("//cpt"));
        assertThat(doc.getDocumentElement(), not(hasXPath("//cpt/cpt")));
    }

}
