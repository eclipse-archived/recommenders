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

import java.util.Set;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.commons.bayesnet.Node;
import org.eclipse.recommenders.commons.utils.BidirectionalHashMap;

import smile.Network;

public class SmileImporter {

    private final Network smileNetwork;
    private final BidirectionalHashMap<Node, String> nodeToSmileIdMapping;
    private final BayesianNetwork bayesNetwork;

    public SmileImporter(final Network smileNetwork) {
        this.smileNetwork = smileNetwork;
        this.nodeToSmileIdMapping = new BidirectionalHashMap<Node, String>();
        this.bayesNetwork = new BayesianNetwork();

        importNodes();
        importArcs();
        importPropabilities();
    }

    private void importNodes() {
        final String[] nodeIds = smileNetwork.getAllNodeIds();
        for (int i = 0; i < nodeIds.length; i++) {
            importNode(nodeIds[i]);
        }
    }

    private void importNode(final String nodeId) {
        final String name = smileNetwork.getNodeName(nodeId);
        if (name != null && name.length() > 0) {
            createNode(name, nodeId);
        } else {
            createNode(nodeId, nodeId);
        }
    }

    private void createNode(final String identifier, final String smileId) {
        final Node node = new Node(identifier);
        final int outcomeCount = smileNetwork.getOutcomeCount(smileId);
        for (int i = 0; i < outcomeCount; i++) {
            node.addState(smileNetwork.getOutcomeLabel(smileId, i));
        }
        bayesNetwork.addNode(node);
        insertToMapping(node, smileId);
    }

    private void insertToMapping(final Node node, final String smileId) {
        if (nodeToSmileIdMapping.containsValue(smileId)) {
            throw new IllegalStateException("Assumption that node names are unique is disproved.");
        } else {
            nodeToSmileIdMapping.put(node, smileId);
        }
    }

    private void importArcs() {
        final Set<Node> nodes = nodeToSmileIdMapping.keySet();
        for (final Node node : nodes) {
            final String smileId = nodeToSmileIdMapping.getValue(node);
            final String[] parentIds = smileNetwork.getParentIds(smileId);
            for (final String parentId : parentIds) {
                node.addParent(nodeToSmileIdMapping.getKey(parentId));
            }
        }
    }

    private void importPropabilities() {
        final Set<Node> nodes = nodeToSmileIdMapping.keySet();
        for (final Node node : nodes) {
            final String smileId = nodeToSmileIdMapping.getValue(node);
            final double[] propabilities = smileNetwork.getNodeValue(smileId);
            node.setPropabilities(propabilities);
        }
    }

}
