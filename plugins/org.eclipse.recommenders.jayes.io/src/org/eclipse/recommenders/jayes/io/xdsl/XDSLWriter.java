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

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.recommenders.internal.jayes.io.util.XMLUtil;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.io.IBayesNetWriter;

public class XDSLWriter implements IBayesNetWriter {

    private static final String xmlHeader = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n";
    private static final String comment = "<!--\n\t Bayesian Network in XDSL format \n-->\n";
    private final Writer out;

    public XDSLWriter(OutputStream out) {
        this.out = new OutputStreamWriter(out);

    }

    public void write(BayesNet net) throws IOException {
        StringBuilder bldr = new StringBuilder();
        bldr.append(xmlHeader);
        bldr.append(comment);

        int offset = bldr.length();
        getVariableDefs(bldr, net);
        XMLUtil.surround(offset, bldr, "nodes");
        XMLUtil.surround(offset, bldr, "smile", "version", "1.0", ID, net.getName(), "numsamples", "1000",
                "discsamples", "10000");

        out.write(bldr.toString());
        out.flush();
    }

    private void getVariableDefs(StringBuilder bldr, BayesNet net) {
        for (BayesNode node : net.getNodes()) {
            int offset = bldr.length();
            encodeStates(bldr, node);
            encodeParents(bldr, node);
            bldr.append('\n');
            encodeProbabilities(bldr, node);
            XMLUtil.surround(offset, bldr, CPT, ID, node.getName());
            bldr.append('\n');
        }
    }

    private void encodeStates(StringBuilder bldr, BayesNode node) {
        for (String outcome : node.getOutcomes()) {
            XMLUtil.emptyTag(bldr, STATE, ID, StringEscapeUtils.escapeXml(outcome));
            bldr.append('\n');
        }
    }

    private void encodeParents(StringBuilder bldr, BayesNode node) {
        int offset = bldr.length();
        for (BayesNode p : node.getParents()) {
            // XDSL can't handle names containing whitespaces!
            bldr.append(p.getName().trim().replaceAll("\\s+", "_"));
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
