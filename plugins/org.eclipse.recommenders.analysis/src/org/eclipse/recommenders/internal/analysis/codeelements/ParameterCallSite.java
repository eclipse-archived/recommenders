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

public class ParameterCallSite implements ICodeElement {
    public static ParameterCallSite create(final String argumentName, final IMethodName targetMethod,
            final int argumentIndex, final IMethodName sourceMethod, final int lineNumber) {
        final ParameterCallSite res = new ParameterCallSite();
        res.argumentName = argumentName;
        res.targetMethod = targetMethod;
        res.argumentIndex = argumentIndex;
        res.sourceMethod = sourceMethod;
        res.lineNumber = lineNumber;
        return res;
    }

    public IMethodName targetMethod;

    public String argumentName;

    public int argumentIndex;

    public IMethodName sourceMethod;

    public int lineNumber;

    /**
     * @see #create(String, IMethodName, int, IMethodName, int)
     */
    protected ParameterCallSite() {
        // use create methods instead
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

    @Override
    public void accept(final CompilationUnitVisitor v) {
        v.visit(this);
    }
}
