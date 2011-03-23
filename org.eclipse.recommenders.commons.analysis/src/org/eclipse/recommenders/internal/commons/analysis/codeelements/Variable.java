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
package org.eclipse.recommenders.internal.commons.analysis.codeelements;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmVariableName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey.Kind;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Variable implements Comparable<Variable>, INamedCodeElement {

    public static Variable create(final String name, final ITypeName variableType, final IMethodName declaringMethod) {
        final Variable res = new Variable();
        res.name = name;
        res.type = variableType;
        res.referenceContext = declaringMethod;
        return res;
    }

    private IMethodName referenceContext;

    /**
     * To which instances does this variable point to?
     */
    public Set<ObjectInstanceKey> pointsTo = Sets.newHashSet();

    private String name;

    public ITypeName type;

    public Kind kind;

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

    // TODO rework to use variable names instead...
    @Override
    public IName getName() {
        return VmVariableName.get(referenceContext.getIdentifier() + "#" + name);
    }

    public IMethodName getReferenceContext() {
        return referenceContext;
    }

    public Set<ReceiverCallSite> getReceiverCallsites() {
        final Set<ReceiverCallSite> res = Sets.newHashSet();
        for (final ObjectInstanceKey object : pointsTo) {
            res.addAll(object.receiverCallSites);
        }
        return res;
    }

    public Set<IMethodName> getReceiverCalls() {
        final Set<IMethodName> res = Sets.newHashSet();
        for (final ObjectInstanceKey object : pointsTo) {
            for (final ReceiverCallSite callsite : object.receiverCallSites) {
                res.add(callsite.targetMethod);
            }
        }
        return res;
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

    public boolean fuzzyIsParameter() {
        if (kind == Kind.PARAMETER) {
            return true;
        }
        for (final ObjectInstanceKey obj : pointsTo) {
            if (obj.kind == ObjectInstanceKey.Kind.PARAMETER) {
                return true;
            }
            if (obj.definitionSite != null) {
                if (obj.definitionSite.kind == DefinitionSite.Kind.PARAMETER) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean fuzzyIsDefinedByMethodReturn() {
        if (kind == Kind.RETURN) {
            return true;
        }
        for (final ObjectInstanceKey obj : pointsTo) {
            if (obj.definitionSite != null) {
                if (obj.definitionSite.kind == DefinitionSite.Kind.METHOD_RETURN) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isThis() {
        return "this".equals(name);
    }

    @Override
    public void accept(final CompilationUnitVisitor v) {
        if (v.visit(this)) {
            for (final ReceiverCallSite callsite : getReceiverCallsites()) {
                callsite.accept(v);
            }
        }
    }

}
