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
package org.eclipse.recommenders.server.extdoc.types;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

public final class ClassSelfcallDirectives implements IServerType {

    public static ClassSelfcallDirectives create(final ITypeName type, final int numberOfSubclasses,
            final Map<IMethodName, Integer> selfcalls) {
        final ClassSelfcallDirectives res = new ClassSelfcallDirectives();
        res.type = type;
        res.numberOfSubclasses = numberOfSubclasses;
        res.calls = selfcalls;
        return res;
    }

    @SerializedName("_id")
    private String id;
    @SerializedName("_rev")
    private String rev;

    private final String providerId = getClass().getSimpleName();
    private ITypeName type;

    private int numberOfSubclasses;
    private Map<IMethodName, Integer> calls;

    public int getNumberOfSubclasse() {
        return numberOfSubclasses;
    }

    public Map<IMethodName, Integer> getCalls() {
        return calls;
    }

    @Override
    public void validate() {
        Checks.ensureIsTrue("ClassSelfcallDirectives".equals(providerId));
        Checks.ensureIsNotNull(type);
        Checks.ensureIsGreaterOrEqualTo(numberOfSubclasses, 1, null);
        Checks.ensureIsFalse(calls.isEmpty(), "empty self-calls not allowed");
    }

    @Override
    public String toString() {
        return id + " / " + rev + " / " + providerId + " / " + type + " / " + numberOfSubclasses;
    }
}
