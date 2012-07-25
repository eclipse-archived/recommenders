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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Node implements Serializable {

    private static final long serialVersionUID = 6120294183622148914L;

    private static final Node[] EMPTY_PARENTS = new Node[0];
    private static final String[] EMPTY_STATES = new String[0];
    private static final double[] EMPTY_PROBABILITIES = new double[0];

    private final String identifier;
    private Node[] parents = EMPTY_PARENTS;
    private String[] states = EMPTY_STATES;
    private double[] probabilities = EMPTY_PROBABILITIES;

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
    }

    public int numberOfParents() {
        if (parents == null) {
            return 0;
        }
        return parents.length;
    }

    public Node[] getParents() {
        return parents;
    }

    public void setProbabilities(final double[] probabilities) {
        this.probabilities = probabilities;
    }

    public boolean isValid() {
        if (parents.length > 0) {
            int parentStates = 1;
            for (final Node parent : parents) {
                parentStates *= parent.numberOfStates();
            }
            if (probabilities.length != parentStates * states.length) {
                return false;
            }
        } else if (probabilities.length != states.length) {
            return false;
        }

        for (int i = 0; i < probabilities.length; i++) {
            if (Double.isInfinite(probabilities[i])) {
                return false;
            }
            if (Double.isNaN(probabilities[i])) {
                return false;
            }
        }

        return true;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String[] getStates() {
        return states;
    }

    /**
     * Returns the probabilities of this node column wise
     */
    public double[] getProbabilities() {
        return probabilities;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public boolean equals(Object arg0) {
        return EqualsBuilder.reflectionEquals(this, arg0);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
