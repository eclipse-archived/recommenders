/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.templates.types;

import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.IMethodName;

/**
 * Models a call of a certain method on a given variable.
 */
public final class MethodCall {

    private final String variableName;
    private final IMethodName invokedMethod;

    /**
     * @param invokedMethod
     *            A handler containing information about the method which shall
     *            be invoked on the given variable.
     */
    public MethodCall(final String variableName, final IMethodName invokedMethod) {
        this.variableName = variableName;
        this.invokedMethod = Checks.ensureIsNotNull(invokedMethod);
    }

    public String getVariableName() {
        return variableName;
    }

    /**
     * @return A handler containing information about the method which shall be
     *         invoked on the given variable.
     */
    public IMethodName getInvokedMethod() {
        return invokedMethod;
    }
}
