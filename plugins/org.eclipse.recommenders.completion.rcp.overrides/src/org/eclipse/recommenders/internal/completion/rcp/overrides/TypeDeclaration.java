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
package org.eclipse.recommenders.internal.completion.rcp.overrides;

import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.IName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.Sets;

public class TypeDeclaration {

    public static TypeDeclaration create() {
        final TypeDeclaration type = new TypeDeclaration();
        return type;
    }

    public static TypeDeclaration create(final ITypeName typeName, final ITypeName superclassName) {
        final TypeDeclaration type = new TypeDeclaration();
        type.name = typeName;
        type.superclass = superclassName;
        return type;
    }

    public IName getName() {
        return name;
    }

    /**
     * use {@link #create(ITypeName, ITypeName)} to create an instance of this class.
     */
    protected TypeDeclaration() {
        // no-one should instantiate this class directly
    }

    public ITypeName name;

    public ITypeName superclass;

    public Set<ITypeName> interfaces = Sets.newHashSet();

    public Set<ITypeName> fields = Sets.newHashSet();

    public Set<MethodDeclaration> methods = Sets.newHashSet();

    public int line;

    public Set<TypeDeclaration> memberTypes = Sets.newHashSet();

    public int modifiers;

    public void clearEmptySets() {
        if (interfaces.isEmpty()) {
            interfaces = null;
        }
        if (fields.isEmpty()) {
            fields = null;
        }
        if (methods.isEmpty()) {
            methods = null;
        }
        if (memberTypes.isEmpty()) {
            memberTypes = null;
        }
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

    public MethodDeclaration findMethod(final IMethodName methodName) {
        for (final MethodDeclaration method : methods) {
            if (method.name == methodName) {
                return method;
            }
        }
        // its not one of our methods declared in here... check our nested types
        // next:
        for (final TypeDeclaration nestedType : memberTypes) {
            final MethodDeclaration res = nestedType.findMethod(methodName);
            if (res != null) {
                return res;
            }
        }
        // its not in one of our nested types. Check each method whether it
        // contains a nested type that declares this
        // method:
        for (final MethodDeclaration method : methods) {
            for (final TypeDeclaration nestedType : method.nestedTypes) {
                final MethodDeclaration res = nestedType.findMethod(methodName);
                if (res != null) {
                    return res;
                }
            }
        }
        // sorry, we couldn't find any matching method.
        return null;
    }
}
