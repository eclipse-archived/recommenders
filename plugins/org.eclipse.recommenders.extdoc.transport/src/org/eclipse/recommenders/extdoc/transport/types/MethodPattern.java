/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.extdoc.transport.types;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.recommenders.extdoc.rcp.IServerType;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.names.IMethodName;

public final class MethodPattern implements IServerType {

    private int numberOfObservations;
    private Map<IMethodName, Double> methods;

    public static MethodPattern create(final int numberOfObservations, final Map<IMethodName, Double> methods) {
        final MethodPattern res = new MethodPattern();
        res.numberOfObservations = numberOfObservations;
        res.methods = methods;
        res.validate();
        return res;
    }

    public int getNumberOfObservations() {
        return numberOfObservations;
    }

    public Map<IMethodName, Double> getMethods() {
        return methods;
    }

    @Override
    public void validate() {
        Checks.ensureIsTrue(numberOfObservations > 0);
        Checks.ensureIsTrue(!methods.isEmpty());
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
