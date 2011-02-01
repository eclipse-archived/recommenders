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
package org.eclipse.recommenders.internal.commons.analysis.utils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.ibm.wala.classLoader.IMethod;

public class WalaReceiverCallsite {
    public IMethod source;

    public String receiver;

    public IMethod target;

    public int line;

    public static WalaReceiverCallsite create(final IMethod sourceMethod, final String receiverName,
            final IMethod targetMethod, final int lineNumber) {
        final WalaReceiverCallsite res = new WalaReceiverCallsite();
        res.source = sourceMethod;
        res.receiver = receiverName;
        res.target = targetMethod;
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
