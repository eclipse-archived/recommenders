/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.commons.bayesnet.util;

import java.util.Collection;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.commons.bayesnet.Node;

import smile.Network;

public class SmileExporter {

    private final BayesianNetwork bayesNetwork;
    private final Network smileNetwork;

    public SmileExporter(final BayesianNetwork bayesNetwork) {
        this.bayesNetwork = bayesNetwork;
        this.smileNetwork = new Network();

        exportNodes();
        exportArcs();
        exportPropabilities();
    }

    private void exportNodes() {
        final Collection<Node> nodes = bayesNetwork.getNodes();
        for (final Node node : nodes) {
            smileNetwork.addNode(Network.NodeType.Cpt, node.getIdentifier());
            setOutcomes(node);
        }
    }

    private void setOutcomes(final Node node) {
        final String[] states = node.getStates();
        for (int i = 0; i < states.length; i++) {
            if (i < 2) {
                smileNetwork.setOutcomeId(node.getIdentifier(), i, states[i]);
            } else {
                smileNetwork.addOutcome(node.getIdentifier(), states[i]);
            }
        }
    }

    private void exportArcs() {
        final Collection<Node> nodes = bayesNetwork.getNodes();
        for (final Node node : nodes) {
            final Node[] parents = node.getParents();
            for (int i = 0; i < parents.length; i++) {
                smileNetwork.addArc(parents[i].getIdentifier(), node.getIdentifier());
            }
        }
    }

    private void exportPropabilities() {
        final Collection<Node> nodes = bayesNetwork.getNodes();
        for (final Node node : nodes) {
            smileNetwork.setNodeDefinition(node.getIdentifier(), node.getProbabilities());
        }
    }

    public Network getNetwork() {
        return smileNetwork;
    }
}
