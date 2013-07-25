/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.apidocs;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

public final class ClassSelfcallDirectives {

    private final String providerId = getClass().getSimpleName();
    private ITypeName type;

    private int numberOfSubclasses;
    private Map<IMethodName, Integer> calls;

    public static ClassSelfcallDirectives create(final ITypeName type, final int numberOfSubclasses,
            final Map<IMethodName, Integer> selfcalls) {
        final ClassSelfcallDirectives res = new ClassSelfcallDirectives();
        res.type = type;
        res.numberOfSubclasses = numberOfSubclasses;
        res.calls = selfcalls;
        res.validate();
        return res;
    }

    public ITypeName getType() {
        return type;
    }

    public int getNumberOfSubclasses() {
        return numberOfSubclasses;
    }

    public Map<IMethodName, Integer> getCalls() {
        return calls;
    }

    public void validate() {
        Checks.ensureIsTrue("ClassSelfcallDirectives".equals(providerId));
        Checks.ensureIsNotNull(type);
        Checks.ensureIsGreaterOrEqualTo(numberOfSubclasses, 1, null);
        Checks.ensureIsFalse(calls.isEmpty(), "empty self-calls not allowed");
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
