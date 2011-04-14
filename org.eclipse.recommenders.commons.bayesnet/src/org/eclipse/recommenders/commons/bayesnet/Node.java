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

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Node implements Serializable {

    private static final long serialVersionUID = 6120294183622148914L;

    private final String identifier;
    private Node[] parents;
    private String[] states;
    private double[] probabilities;

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
        return this.parents;
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

    public double[] getProbabilities() {
        return probabilities;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
