/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.analysis.codeelements;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.utils.names.IMethodName;

public class ReceiverCallSite implements ICodeElement {
    public static ReceiverCallSite create(final String receiver, final IMethodName targetMethod,
            final IMethodName sourceMethod, final int lineNumber) {
        final ReceiverCallSite receiverCallSite = new ReceiverCallSite();
        receiverCallSite.sourceMethod = sourceMethod;
        receiverCallSite.line = lineNumber;
        receiverCallSite.targetMethod = targetMethod;
        receiverCallSite.receiver = receiver;
        return receiverCallSite;
    }

    public String receiver;

    public IMethodName targetMethod;

    public IMethodName sourceMethod;

    public int line;

    /**
     * @see #create(String, IMethodName, IMethodName, int)
     */
    protected ReceiverCallSite() {
        // use create methods instead
    }

    /**
     * Returns true if this callsite's receiver is this.
     */
    public boolean isThis() {
        return "this".equals(receiver);
    }

    @Override
    public void accept(final CompilationUnitVisitor v) {
        v.visit(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
