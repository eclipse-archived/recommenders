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
package org.eclipse.recommenders.internal.utils.codestructs;

import java.util.Collections;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.Lists;

public class Variable implements Comparable<Variable> {

    public static Variable create(final String name, final ITypeName variableType, final IMethodName declaringMethod) {
        final Variable res = new Variable();
        res.name = name;
        res.type = variableType;
        res.referenceContext = declaringMethod;
        return res;
    }

    private IMethodName referenceContext;

    private String name;

    public ITypeName type;

    /**
     * @see #create(String, ITypeName, IMethodName)
     */
    protected Variable() {
        // see create(..)
    }

    public ITypeName getType() {
        return type;
    }

    public String getNameLiteral() {
        return name;
    }

    public IMethodName getReferenceContext() {
        return referenceContext;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, Collections.singleton("type"));
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, Collections.singleton("type"));
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public int compareTo(final Variable other) {
        return CompareToBuilder.reflectionCompare(this, other, Lists.newArrayList("type", "pointsTo"));
    }

    public boolean isThis() {
        return "this".equals(name);
    }

}
