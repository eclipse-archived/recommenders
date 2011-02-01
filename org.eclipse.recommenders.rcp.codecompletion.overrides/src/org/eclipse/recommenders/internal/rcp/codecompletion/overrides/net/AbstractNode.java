/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.overrides.net;

import smile.Network;

public class AbstractNode {
    /**
     * The network this node belongs to. In smile the network is a central data structure which participates in may
     * operations.
     */
    protected final Network network;

    /**
     * In smile, each node has a (network-wide) unique id. This id is stored here.
     */
    protected final String nodeId;

    protected AbstractNode(final Network network, final String nodeId) {
        this.network = network;
        this.nodeId = nodeId;
    }

    protected String getNodeId() {
        return nodeId;
    }

    /**
     * Clears the selection state of a node (if one).
     */
    public void clearEvidence() {
        if (network.isEvidence(nodeId)) {
            network.clearEvidence(nodeId);
        }
    }

    /**
     * Returns true iff any of node's states is selected.
     */
    public boolean isEvidence() {
        return network.isEvidence(getNodeId());
    }

    /**
     * Returns the probabilities of all declared states.
     */
    public double[] getProbabilities() {
        return network.getNodeDefinition(nodeId);
    }

    @Override
    public String toString() {
        return network.getNodeName(nodeId);
    }
}
