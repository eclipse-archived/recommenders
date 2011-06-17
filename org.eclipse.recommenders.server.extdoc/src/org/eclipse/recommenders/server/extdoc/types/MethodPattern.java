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
package org.eclipse.recommenders.server.extdoc.types;

import java.util.Map;

import org.eclipse.recommenders.commons.utils.names.IMethodName;

public final class MethodPattern {

    public static MethodPattern create(final int numberOfObservations, final Map<IMethodName, Double> methodGroup) {
        final MethodPattern res = new MethodPattern();
        res.numberOfObservations = numberOfObservations;
        res.methods = methodGroup;
        return res;
    }

    private int numberOfObservations;
    private Map<IMethodName, Double> methods;

    public int getNumberOfObservations() {
        return numberOfObservations;
    }

    public Map<IMethodName, Double> getMethods() {
        return methods;
    }
}
