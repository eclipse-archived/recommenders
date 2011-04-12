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

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Node {

    private String identifier;
    private String[] parentIds;
    private transient Node[] parents;
    private String[] states;
    private double[] propabilities;

    protected Node() {
    }

    public Node(final String identifier) {
        this.identifier = identifier;
    }

    public void setStates(final String[] states) {
        this.states = states;
    }

    public int numberOfStates() {
        return states.length;
    }

    public void setParents(final Node[] parents) {
        this.parents = parents;
        this.parentIds = new String[parents.length];
        for (int i = 0; i < parents.length; i++) {
            this.parentIds[i] = parents[i].getIdentifier();
        }
    }

    public int numberOfParents() {
        if (parentIds == null) {
            return 0;
        } else {
            return parentIds.length;
        }
    }

    public Node[] getParents() {
        return this.parents;
    }

    public void setPropabilities(final double[] propabilities) {
        this.propabilities = propabilities;
    }

    public boolean isValid() {
        if (parents.length > 0) {
            int parentStates = 1;
            for (final Node parent : parents) {
                parentStates *= parent.numberOfStates();
            }
            if (propabilities.length != parentStates * states.length) {
                return false;
            }
        } else if (propabilities.length != states.length) {
            return false;
        }

        return true;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void restore(final BayesianNetwork network) {
        if (parentIds == null) {
            this.parents = null;
        } else {
            this.parents = new Node[parentIds.length];
            for (int i = 0; i < parentIds.length; i++) {
                this.parents[i] = network.getNode(parentIds[i]);
            }
        }
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
