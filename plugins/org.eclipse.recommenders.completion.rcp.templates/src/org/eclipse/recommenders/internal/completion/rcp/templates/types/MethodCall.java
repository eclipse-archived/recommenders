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
package org.eclipse.recommenders.internal.completion.rcp.templates.types;

import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.names.IMethodName;

/**
 * Models a call of a certain method on a given variable name. The type of the
 * variable is extracted from the method.
 */
public final class MethodCall {

    private final CompletionTargetVariable variable;
    private final IMethodName invokedMethod;

    /**
     * @param variable
     *            The variable on which the method shall be invoked.
     * @param invokedMethod
     *            A handler containing information about the method which shall
     *            be invoked on the given variable name.
     */
    public MethodCall(final CompletionTargetVariable variable, final IMethodName invokedMethod) {
        this.variable = Checks.ensureIsNotNull(variable);
        this.invokedMethod = Checks.ensureIsNotNull(invokedMethod);
    }

    /**
     * @return The variable on which the method shall be invoked.
     */
    public CompletionTargetVariable getVariable() {
        return variable;
    }

    /**
     * @return A handler containing information about the method which shall be
     *         invoked on the given variable.
     */
    public IMethodName getInvokedMethod() {
        return invokedMethod;
    }
}
