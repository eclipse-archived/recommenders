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

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

public class TypeReference implements Comparable<TypeReference> {

    public final CodeElementKind kind = CodeElementKind.TYPE_REFERENCE;

    public ITypeName name;

    public String fingerprint;

    public static TypeReference create(final ITypeName name, final String fingerprint) {
        ensureIsNotNull(name);
        ensureIsNotNull(fingerprint);
        final TypeReference res = new TypeReference();
        res.name = name;
        res.fingerprint = fingerprint;
        return res;
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
    public int compareTo(final TypeReference o) {
        final int nameComparison = o.name.compareTo(name);
        return nameComparison != 0 ? nameComparison : o.fingerprint.compareTo(fingerprint);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
