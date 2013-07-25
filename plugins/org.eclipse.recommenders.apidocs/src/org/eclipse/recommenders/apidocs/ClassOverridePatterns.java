/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.apidocs;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.annotations.Beta;

public final class ClassOverridePatterns {

    public String _id;
    public String _rev;
    @SuppressWarnings("unused")
    private final String providerId = getClass().getSimpleName();
    private ITypeName type;

    private MethodPattern[] patterns;

    public static ClassOverridePatterns create(final ITypeName type, final MethodPattern[] patterns) {
        final ClassOverridePatterns res = new ClassOverridePatterns();
        res.type = type;
        res.patterns = patterns.clone();
        // res.validate();
        return res;
    }

    @Beta
    public MethodPattern[] getPatterns() {
        return patterns == null ? new MethodPattern[0] : patterns.clone();
    }

    // @Override
    // public void validate() {
    // Checks.ensureIsNotNull(type);
    // Checks.ensureIsNotNull(patterns);
    // }

    public ITypeName getType() {
        return type;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
