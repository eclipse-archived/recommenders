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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.net;

import static org.eclipse.recommenders.commons.utils.Throws.throwIllegalStateException;

import org.eclipse.recommenders.rcp.RecommendersPlugin;

import smile.Network;
import smile.SMILEException;

public class AbstractNode {
    /**
     * The network this node belongs to. In smile the network is a central data
     * structure which participates in may operations.
     */
    protected final Network network;

    /**
     * In smile, each node has a (network-wide) unique id. This id is stored
     * here.
     */
    protected final String nodeId;

    protected AbstractNode(final Network network, final String nodeId) {
        this.network = network;
        this.nodeId = nodeId;
    }

    protected String getNodeId() {
        return nodeId;
    }

    protected void setEvidence(final int outcomeIndex) {
        try {
            network.setEvidence(nodeId, outcomeIndex);
        } catch (final Exception e) {
            RecommendersPlugin.logError(e, "failed to set evidence for availability node");
        }
    }

    protected boolean setEvidence(final String outcomeId) {
        try {
            network.setEvidence(nodeId, outcomeId);
            return true;
        } catch (final SMILEException x) {
            RecommendersPlugin.logError(x, "failed to set evidence for availability node");
            return false;
        }
    }

    /**
     * Clears the selection state of a node (if one). Returns <code>true</code>
     * if previously set and cleared, <code>false</code> otherwise.
     */
    public boolean clearEvidence() {
        if (!isEvidence()) {
            return false;
        }
        network.clearEvidence(nodeId);
        return true;
    }

    /**
     * Returns true iff any of node's states is selected.
     */
    public boolean isEvidence() {
        return network.isEvidence(getNodeId());
    }

    public String getEvidenceId() {
        if (!isEvidence()) {
            throwIllegalStateException("node has no evidence. Call #isEvidence() before you query the evidence state id.");
        }
        return network.getEvidenceId(nodeId);
    }

    /**
     * Returns the definition probabilities of all declared states. Note this is
     * not the current probability for all states based on some observations.
     * Its just the definition.
     */
    public double[] getDefinition() {
        return network.getNodeDefinition(nodeId);
    }

    /**
     * Returns the current probabilities of all states. Call this method
     * <b>after</b> calling {@link ObjectMethodCallsNet#updateBeliefs()}.
     */
    public double[] getValues() {
        return network.getNodeValue(nodeId);
    }

    @Override
    public String toString() {
        return network.getNodeName(nodeId);
    }
}
