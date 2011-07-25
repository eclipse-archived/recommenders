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

import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.rcp.extdoc.IServerType;

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
}
