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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.eclipse.recommenders.internal.jayes.io.util.XMLUtil;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.io.IBayesNetWriter;

public class XDSLWriter implements IBayesNetWriter {

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n";
    private static final String COMMENT = "<!--\n\t Bayesian Network in XDSL format \n-->\n";
    private final Writer out;

    public XDSLWriter(OutputStream out) {
        this.out = new OutputStreamWriter(out);

    }

    @Override
    public void write(BayesNet net) throws IOException {
        StringBuilder bldr = new StringBuilder();
        bldr.append(XML_HEADER);
        bldr.append(COMMENT);

        int offset = bldr.length();
        getVariableDefs(bldr, net);
        getGenieExtensions(bldr, net);
        XMLUtil.surround(offset, bldr, "smile", "version", "1.0", ID, XMLUtil.escape(net.getName()), "numsamples",
                "1000");

        out.write(bldr.toString());
        out.flush();
    }

    private void getGenieExtensions(StringBuilder bldr, BayesNet net) {
        int offset = bldr.length();
        for (BayesNode node : net.getNodes()) {
            int nodeOffset = bldr.length();
            bldr.append(XMLUtil.escape(node.getName()));
            XMLUtil.surround(nodeOffset, bldr, "name");
            bldr.append('\n');
            int posOffset = bldr.length();
            bldr.append("0 0 100 100");
            XMLUtil.surround(posOffset, bldr, "position");
            bldr.append('\n');
            XMLUtil.emptyTag(bldr, "font", "color", "000000", "name", "Arial", "size", "8");
            bldr.append('\n');
            XMLUtil.emptyTag(bldr, "interior", "color", "e5f6f7");
            bldr.append('\n');
            XMLUtil.emptyTag(bldr, "outline", "color", "000000");
            bldr.append('\n');
            XMLUtil.surround(nodeOffset, bldr, "node", "id", XMLUtil.escape(node.getName()));
        }
        XMLUtil.surround(offset, bldr, "genie", "version", "1.0", "name", XMLUtil.escape(net.getName()));
        XMLUtil.surround(offset, bldr, "extensions");

    }

    private void getVariableDefs(StringBuilder bldr, BayesNet net) {
        int offset = bldr.length();
        for (BayesNode node : net.getNodes()) {
            int nodeOffset = bldr.length();
            encodeStates(bldr, node);
            encodeParents(bldr, node);
            bldr.append('\n');
            encodeProbabilities(bldr, node);
            XMLUtil.surround(nodeOffset, bldr, CPT, ID, XMLUtil.escape(node.getName()));
            bldr.append('\n');
        }
        XMLUtil.surround(offset, bldr, "nodes");
    }

    private void encodeStates(StringBuilder bldr, BayesNode node) {
        for (String outcome : node.getOutcomes()) {
            XMLUtil.emptyTag(bldr, STATE, ID, XMLUtil.escape(outcome));
            bldr.append('\n');
        }
    }

    private void encodeParents(StringBuilder bldr, BayesNode node) {
        int offset = bldr.length();
        for (BayesNode p : node.getParents()) {
            // XDSL can't handle names containing whitespaces!
            bldr.append(XMLUtil.escape(p.getName()));
            bldr.append(' ');
        }
        if (!node.getParents().isEmpty()) {
            bldr.deleteCharAt(bldr.length() - 1); // delete last whitespace
        }

        XMLUtil.surround(offset, bldr, PARENTS);
    }

    private void encodeProbabilities(StringBuilder bldr, BayesNode node) {
        if (node.getProbabilities().length == 0) {
            throw new IllegalArgumentException("Bayesian Network is broken: " + node.getName()
                    + " has an empty conditional probability table");
        }
        int offset = bldr.length();
        for (double d : node.getProbabilities()) {
            bldr.append(d);
            bldr.append(' ');
        }
        bldr.deleteCharAt(bldr.length() - 1); // delete last whitespace
        XMLUtil.surround(offset, bldr, PROBABILITIES);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
