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

import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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
