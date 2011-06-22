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

import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.IMethodName;

import com.google.gson.annotations.SerializedName;

public final class MethodSelfcallDirectives implements IServerType {

    @SerializedName("_id")
    private String id;
    @SerializedName("_rev")
    private String rev;

    private final String providerId = getClass().getSimpleName();
    private IMethodName method;

    private int numberOfDefinitions;
    private Map<IMethodName, Integer> calls;

    public static MethodSelfcallDirectives create(final IMethodName method, final int numberOfDefinitions,
            final Map<IMethodName, Integer> selfcalls) {
        final MethodSelfcallDirectives res = new MethodSelfcallDirectives();
        res.method = method;
        res.numberOfDefinitions = numberOfDefinitions;
        res.calls = selfcalls;
        return res;
    }

    public int getNumberOfDefinitions() {
        return numberOfDefinitions;
    }

    public Map<IMethodName, Integer> getCalls() {
        return calls;
    }

    @Override
    public void validate() {
        Checks.ensureIsTrue("MethodSelfcallDirectives".equals(providerId));
        Checks.ensureIsNotNull(method);
        Checks.ensureIsGreaterOrEqualTo(numberOfDefinitions, 1, null);
        Checks.ensureIsFalse(calls.isEmpty(), "empty self-calls not allowed.");
    }

}
