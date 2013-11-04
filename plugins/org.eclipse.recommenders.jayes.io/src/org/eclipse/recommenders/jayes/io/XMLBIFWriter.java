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
package org.eclipse.recommenders.jayes.io;

import static org.eclipse.recommenders.internal.jayes.io.util.XMLBIFConstants.DEFINITION;
import static org.eclipse.recommenders.internal.jayes.io.util.XMLBIFConstants.FOR;
import static org.eclipse.recommenders.internal.jayes.io.util.XMLBIFConstants.GIVEN;
import static org.eclipse.recommenders.internal.jayes.io.util.XMLBIFConstants.NAME;
import static org.eclipse.recommenders.internal.jayes.io.util.XMLBIFConstants.OUTCOME;
import static org.eclipse.recommenders.internal.jayes.io.util.XMLBIFConstants.TABLE;
import static org.eclipse.recommenders.internal.jayes.io.util.XMLBIFConstants.VARIABLE;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.recommenders.internal.jayes.io.util.XMLUtil;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;

/**
 * a Writer thats writes the XMLBIF v0.3 format (<a href="http://www.cs.cmu.edu/~fgcozman/Research/InterchangeFormat/"
 * >specification</a>)
 * 
 * @author Michael Kutschke
 * 
 */
public class XMLBIFWriter implements IBayesNetWriter {

    private static final String xmlHeader = "<?xml version=\"1.0\"?>\n";
    private static final String comment = "<!--\n\t Bayesian Network in XMLBIF v0.3 \n-->\n";
    private static final String DTD = "<!-- DTD for the XMLBIF 0.3 format -->\n" + "<!DOCTYPE BIF [\n"
            + "\t<!ELEMENT BIF ( NETWORK )*>\n" + "\t\t<!ATTLIST BIF VERSION CDATA #REQUIRED>\n"
            + "\t<!ELEMENT NETWORK ( NAME, ( PROPERTY | VARIABLE | DEFINITION )* )>\n"
            + "\t<!ELEMENT NAME (#PCDATA)>\n" + "\t<!ELEMENT VARIABLE ( NAME, ( OUTCOME |  PROPERTY )* ) >\n"
            + "\t\t<!ATTLIST VARIABLE TYPE (nature|decision|utility) \"nature\">\n"
            + "\t<!ELEMENT OUTCOME (#PCDATA)>\n" + "\t<!ELEMENT DEFINITION ( FOR | GIVEN | TABLE | PROPERTY )* >\n"
            + "\t<!ELEMENT FOR (#PCDATA)>\n" + "\t<!ELEMENT GIVEN (#PCDATA)>\n" + "\t<!ELEMENT TABLE (#PCDATA)>\n"
            + "\t<!ELEMENT PROPERTY (#PCDATA)>\n" + "]>\n";

    private final Writer wrtr;

    public XMLBIFWriter(OutputStream out) {
        this.wrtr = new OutputStreamWriter(out);
    }

    public void write(BayesNet net) throws IOException {
        StringBuilder bldr = new StringBuilder();
        bldr.append(xmlHeader);
        bldr.append(comment);
        bldr.append(DTD);

        int offset = bldr.length();
        bldr.append(net.getName());
        XMLUtil.surround(offset, bldr, NAME);

        bldr.append("\n<!-- Variables -->\n");

        writeVariables(bldr, net);

        bldr.append("\n<!-- Probability Distributions -->\n");

        writeVariableDefs(bldr, net);

        XMLUtil.surround(offset, bldr, "NETWORK");
        XMLUtil.surround(offset, bldr, "BIF", "VERSION", "0.3");

        wrtr.write(bldr.toString());
    }

    private void writeVariableDefs(StringBuilder bldr, BayesNet net) {
        for (BayesNode node : net.getNodes()) {
            int offset = bldr.length();
            bldr.append(node.getName());
            XMLUtil.surround(offset, bldr, FOR);
            writeParents(bldr, node);
            writeProbabilities(bldr, node);
            XMLUtil.surround(offset, bldr, DEFINITION);
        }
    }

    private void writeParents(StringBuilder bldr, BayesNode node) {
        for (BayesNode parent : node.getParents()) {
            bldr.append("\n\t");
            int offset = bldr.length();
            bldr.append(parent.getName());
            XMLUtil.surround(offset, bldr, GIVEN);
        }
    }

    private void writeProbabilities(StringBuilder bldr, BayesNode node) {
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
        XMLUtil.surround(offset, bldr, TABLE);
    }

    private void writeVariables(StringBuilder bldr, BayesNet net) {
        for (BayesNode node : net.getNodes()) {
            int offset = bldr.length();
            bldr.append(node.getName());
            XMLUtil.surround(offset, bldr, NAME);
            bldr.append("\n");
            for (String outcome : node.getOutcomes()) {
                int offset2 = bldr.length();
                bldr.append(StringEscapeUtils.escapeXml(outcome));
                XMLUtil.surround(offset2, bldr, OUTCOME);
                bldr.append("\n");
            }
            XMLUtil.surround(offset, bldr, VARIABLE);
        }
    }

    @Override
    public void close() throws IOException {
        wrtr.close();

    }
}
