/*******************************************************************************
 * Copyright (c) 2013 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Michael Kutschke - initial API and implementation
 ******************************************************************************/
package org.eclipse.recommenders.commons.bayesnet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.io.IBayesNetWriter;

import com.google.common.collect.Lists;

public class CommonsWriter implements IBayesNetWriter {

    private OutputStream out;

    public CommonsWriter(OutputStream out) {
        this.out = out;

    }

    @Override
    public void close() throws IOException {
        out.close();

    }

    @Override
    public void write(BayesNet bayesNet) throws IOException {
        try {
            BayesianNetwork.write(toBayesianNetwork(bayesNet), out);
        } catch (Exception e) {
            throw new IOException(e);
        }

    }

    private BayesianNetwork toBayesianNetwork(BayesNet bayesNet) {
        BayesianNetwork network = new BayesianNetwork();
        for (BayesNode node : bayesNet.getNodes()) {
            Node n = new Node(node.getName());
            n.setStates(node.getOutcomes().toArray(new String[node.getOutcomeCount()]));
            network.addNode(n);
        }

        for (BayesNode node : bayesNet.getNodes()) {
            Node n = network.getNode(node.getName());
            List<Node> parents = Lists.newArrayList();
            for (BayesNode parent : node.getParents()) {
                parents.add(network.getNode(parent.getName()));
            }
            n.setParents(parents.toArray(new Node[parents.size()]));
            n.setProbabilities(node.getProbabilities());
        }

        return network;
    }

}
