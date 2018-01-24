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
package org.eclipse.recommenders.jayes.io.xmlbif;

import static org.eclipse.recommenders.jayes.io.xmlbif.Constants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.recommenders.internal.jayes.io.util.XPathUtil;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.io.IBayesNetReader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.primitives.Doubles;

/**
 * a Reader thats reads the XMLBIF v0.3 format
 * (<a href="http://www.cs.cmu.edu/~fgcozman/Research/InterchangeFormat/" >specification</a>)
 */
public class XMLBIFReader implements IBayesNetReader {

    private final InputStream in;

    public XMLBIFReader(InputStream in) {
        this.in = in;
    }

    @Override
    public BayesNet read() throws IOException {
        Document doc;
        try {
            doc = obtainDocument(in);
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        } catch (SAXException e) {
            throw new IOException(e);
        }

        return readFromDocument(doc);
    }

    private Document obtainDocument(InputStream biffile)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setValidating(true);
        DocumentBuilder docBldr = docBuilderFactory.newDocumentBuilder();

        Document doc = docBldr.parse(biffile);
        doc.normalize();

        return doc;
    }

    public BayesNet readFromString(String xmlBif) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setValidating(true);
        DocumentBuilder docBldr = docBuilderFactory.newDocumentBuilder();

        Document doc = docBldr.parse(new ByteArrayInputStream(xmlBif.getBytes()));

        return readFromDocument(doc);

    }

    private BayesNet readFromDocument(Document doc) {
        BayesNet net = new BayesNet();

        net.setName(doc.getElementsByTagName(NAME).item(0).getTextContent());

        initializeNodes(doc, net);

        XPath xpath = getXPathEvaluator();

        NodeList nodelist = doc.getElementsByTagName(DEFINITION);
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node node = nodelist.item(i);
            String name = XPathUtil.evalXPath(xpath, FOR, node).next().getTextContent();

            BayesNode bNode = net.getNode(name);

            setParents(bNode, net, node, xpath);

            parseProbabilities(xpath, node, bNode);
        }

        return net;
    }

    private void initializeNodes(Document doc, BayesNet net) {
        XPath xpath = getXPathEvaluator();

        NodeList nodelist = doc.getElementsByTagName(VARIABLE);
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node node = nodelist.item(i);
            Node name = XPathUtil.evalXPath(xpath, NAME, node).next();

            BayesNode bNode = net.createNode(name.getTextContent());

            for (Iterator<Node> it = XPathUtil.evalXPath(xpath, OUTCOME, node); it.hasNext();) {
                bNode.addOutcome(StringEscapeUtils.unescapeXml(it.next().getTextContent()));
            }
        }
    }

    private XPath getXPathEvaluator() {
        return XPathFactory.newInstance().newXPath();
    }

    private void setParents(BayesNode bNode, BayesNet net, Node node, XPath xpath) {
        List<BayesNode> parents = new ArrayList<BayesNode>();
        for (Iterator<Node> it = XPathUtil.evalXPath(xpath, GIVEN, node); it.hasNext();) {
            parents.add(net.getNode(it.next().getTextContent()));
        }
        bNode.setParents(parents);
    }

    private void parseProbabilities(XPath xpath, Node node, BayesNode bNode) {
        String table = XPathUtil.evalXPath(xpath, TABLE, node).next().getTextContent();

        List<Double> probabilities = new ArrayList<Double>();
        StringTokenizer tok = new StringTokenizer(table);
        while (tok.hasMoreTokens()) {
            probabilities.add(Double.valueOf(tok.nextToken()));
        }

        bNode.setProbabilities(Doubles.toArray(probabilities));
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
