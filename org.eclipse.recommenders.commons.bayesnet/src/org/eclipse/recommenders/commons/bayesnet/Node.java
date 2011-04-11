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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Node {

    private String identifier;
    private final List<String> parentIds;
    private transient final List<Node> parents;
    private final List<String> states;
    private double[] propabilities;

    protected Node() {
        parentIds = new ArrayList<String>();
        parents = new ArrayList<Node>();
        states = new ArrayList<String>();
    }

    public Node(final String identifier) {
        this();
        this.identifier = identifier;
    }

    public void addState(final String stateName) {
        states.add(stateName);
    }

    public int numberOfStates() {
        return states.size();
    }

    public void addParent(final Node parent) {
        parents.add(parent);
        parentIds.add(parent.getIdentifier());
    }

    public int numberOfParents() {
        return parentIds.size();
    }

    public List<Node> getParents() {
        return Collections.unmodifiableList(parents);
    }

    public void setPropabilities(final double[] propabilities) {
        this.propabilities = propabilities;
    }

    public boolean isValid() {
        if (parents.size() > 0) {
            int parentStates = 1;
            for (final Node parent : parents) {
                parentStates *= parent.numberOfStates();
            }
            if (propabilities.length != parentStates * states.size()) {
                return false;
            }
        } else if (propabilities.length != states.size()) {
            return false;
        }

        return true;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void restore(final BayesianNetwork network) {
        parents.clear();
        for (final String parentId : parentIds) {
            parents.add(network.getNode(parentId));
        }
    }
}
