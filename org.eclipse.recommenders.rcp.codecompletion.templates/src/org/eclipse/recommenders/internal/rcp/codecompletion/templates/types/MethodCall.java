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

    private final CompletionTargetVariable completionTargetVariable;
    private final IMethodName invokedMethod;

    /**
     * @param completionTargetVariable
     *            The variable on which the given method shall be invoked or
     *            which shall be initiated if the method is a constructor call.
     * @param invokedMethod
     *            A handler containing information about the method which shall
     *            be invoked on the given variable.
     */
    public MethodCall(final CompletionTargetVariable completionTargetVariable, final IMethodName invokedMethod) {
        this.completionTargetVariable = Checks.ensureIsNotNull(completionTargetVariable);
        this.invokedMethod = Checks.ensureIsNotNull(invokedMethod);
    }

    /**
     * @return The variable on which the given method shall be invoked or which
     *         shall be initiated if the method is a constructor call.
     */
    public CompletionTargetVariable getCompletionTargetVariable() {
        return completionTargetVariable;
    }

    /**
     * @return A handler containing information about the method which shall be
     *         invoked on the given variable.
     */
    public IMethodName getInvokedMethod() {
        return invokedMethod;
    }
}
