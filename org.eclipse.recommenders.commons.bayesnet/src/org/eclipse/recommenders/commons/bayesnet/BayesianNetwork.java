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
package org.eclipse.recommenders.commons.bayesnet;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class BayesianNetwork {

    private final Collection<Node> nodes;
    private final HashMap<String, Node> nodeById;

    public BayesianNetwork() {
        this.nodes = new LinkedList<Node>();
        this.nodeById = new HashMap<String, Node>();
    }

    public void addNode(final Node node) {
        if (nodeById.containsKey(node.getIdentifier())) {
            throw new IllegalArgumentException(
                    "A node with that identifier already exists in this network. Identifier: " + node.getIdentifier());
        }

        this.nodes.add(node);
        this.nodeById.put(node.getIdentifier(), node);
    }

    public void restore() {
        for (final Node node : nodes) {
            node.restore(this);
        }
    }

    public boolean isValid() {
        for (final Node node : nodes) {
            if (!node.isValid()) {
                return false;
            }
        }

        return true;
    }

    public Node getNode(final String nodeId) {
        return nodeById.get(nodeId);
    }
}
