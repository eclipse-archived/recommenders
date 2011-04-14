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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class BayesianNetwork implements Serializable {

    private static final long serialVersionUID = 8910268803395952578L;

    private final Collection<Node> nodes = new LinkedList<Node>();
    private transient boolean initialized = false;
    private transient HashMap<String, Node> nodeById = new HashMap<String, Node>();

    public void addNode(final Node node) {
        initialize();
        if (nodeById.containsKey(node.getIdentifier())) {
            throw new IllegalArgumentException(
                    "A node with that identifier already exists in this network. Identifier: " + node.getIdentifier());
        }

        this.nodes.add(node);
        this.nodeById.put(node.getIdentifier(), node);
    }

    private void initialize() {
        if (!initialized) {
            nodeById = new HashMap<String, Node>();
            for (final Node node : nodes) {
                nodeById.put(node.getIdentifier(), node);
            }
            initialized = true;
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
        initialize();
        return nodeById.get(nodeId);
    }

    public Collection<Node> getNodes() {
        return nodes;
    }

    public void setNodes(final Collection<Node> nodes) {
        this.nodes.clear();
        this.nodeById.clear();
        for (final Node node : nodes) {
            addNode(node);
        }
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
