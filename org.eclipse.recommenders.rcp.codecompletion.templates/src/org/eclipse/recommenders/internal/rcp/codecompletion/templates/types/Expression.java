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
// TODO: Review name too generic
public final class Expression {

    private final CompletionTargetVariable completionTargetVariable;
    private final IMethodName invokedMethod;

    public Expression(final CompletionTargetVariable completionTargetVariable, final IMethodName invokedMethod) {
        this.completionTargetVariable = Checks.ensureIsNotNull(completionTargetVariable);
        this.invokedMethod = Checks.ensureIsNotNull(invokedMethod);
    }

    public CompletionTargetVariable getCompletionTargetVariable() {
        return completionTargetVariable;
    }

    public IMethodName getInvokedMethod() {
        return invokedMethod;
    }
}
