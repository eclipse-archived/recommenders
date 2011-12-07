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
package org.eclipse.recommenders.internal.analysis.utils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.ibm.wala.classLoader.IMethod;

public class WalaParameterCallsite {
    public IMethod source;

    public String argumentName;

    public IMethod target;

    public int line;

    public int argumentIndex;

    public static WalaParameterCallsite create(final IMethod sourceMethod, final String argumentName,
            final IMethod targetMethod, final int argumentIndex, final int lineNumber) {
        final WalaParameterCallsite res = new WalaParameterCallsite();
        res.source = sourceMethod;
        res.argumentName = argumentName;
        res.target = targetMethod;
        res.argumentIndex = argumentIndex;
        res.line = lineNumber;
        return res;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
