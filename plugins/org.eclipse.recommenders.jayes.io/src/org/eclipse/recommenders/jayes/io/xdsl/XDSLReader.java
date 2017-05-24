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
package org.eclipse.recommenders.jayes.io.xdsl;

import static org.eclipse.recommenders.jayes.io.xdsl.Constants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.recommenders.internal.jayes.io.util.XMLUtil;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.io.IBayesNetReader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Doubles;

public class XDSLReader implements IBayesNetReader {

    private final InputStream str;

    public XDSLReader(InputStream str) {
        this.str = str;
    }

    @Override
    public BayesNet read() throws IOException {
        Document doc = obtainDocument(str);

        return readFromDocument(doc);
    }

    public BayesNet readFromString(String xdslString) throws IOException {
        Document doc = obtainDocument(new ByteArrayInputStream(xdslString.getBytes()));

        return readFromDocument(doc);
    }

    private Document obtainDocument(InputStream xdslStream) throws IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBldr = docBuilderFactory.newDocumentBuilder();
            Document doc = docBldr.parse(xdslStream);

            doc.normalize();
            return doc;
        } catch (ParserConfigurationException e) {
            throw new IOException("Bad parser configuration, probably missing dependency", e);
        } catch (SAXException e) {
            throw new IOException("Parse failed", e);
        }
    }

    private BayesNet readFromDocument(Document doc) {
        BayesNet net = new BayesNet();

        Node smileNode = doc.getElementsByTagName("smile").item(0);
        String networkName = getId(smileNode);
        net.setName(XMLUtil.unescape(networkName));

        intializeNodes(doc, net);
        initializeNodeOutcomes(doc, net);
        setParents(doc, net);

        parseProbabilities(doc, net);

        return net;
    }

    private void intializeNodes(Document doc, BayesNet net) {
        NodeList nodelist = doc.getElementsByTagName(CPT);

        for (int i = 0; i < nodelist.getLength(); i++) {
            net.createNode(getId(nodelist.item(i)));
        }

    }

    private String getId(Node node) {
        return XMLUtil.unescape(node.getAttributes().getNamedItem(ID).getTextContent());
    }

    private void initializeNodeOutcomes(Document doc, BayesNet net) {

        NodeList nodelist = doc.getElementsByTagName(STATE);
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node node = nodelist.item(i);

            BayesNode bNode = net.getNode(getId(node.getParentNode()));

            bNode.addOutcome(getId(node));

        }
    }

    private void setParents(Document doc, BayesNet net) {

        NodeList nodelist = doc.getElementsByTagName(PARENTS);
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node node = nodelist.item(i);

            BayesNode bNode = net.getNode(getId(node.getParentNode()));

            List<String> parentNames = new ArrayList<>();
            Iterables.addAll(parentNames,
                    Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings().split(node.getTextContent()));

            List<BayesNode> parents = new ArrayList<>();
            for (String parentname : parentNames) {
                parents.add(net.getNode(XMLUtil.unescape(parentname)));
            }
            bNode.setParents(parents);
        }
    }

    private void parseProbabilities(Document doc, BayesNet net) {
        NodeList nodelist = doc.getElementsByTagName(PROBABILITIES);
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node node = nodelist.item(i);

            BayesNode bNode = net.getNode(getId(node.getParentNode()));

            String table = node.getTextContent();
            List<Double> probabilities = new ArrayList<>();

            StringTokenizer tok = new StringTokenizer(table);
            while (tok.hasMoreTokens()) {
                probabilities.add(Double.valueOf(tok.nextToken()));
            }

            bNode.setProbabilities(Doubles.toArray(probabilities));
        }
    }

    @Override
    public void close() throws IOException {
        str.close();
    }
}
