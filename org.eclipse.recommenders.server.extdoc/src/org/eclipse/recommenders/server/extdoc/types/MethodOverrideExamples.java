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

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotEmpty;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.Collections;
import java.util.List;

import org.eclipse.recommenders.commons.utils.names.IMethodName;

import com.google.gson.annotations.SerializedName;

public final class MethodOverrideExamples implements IServerType {

    public static MethodOverrideExamples create(final IMethodName method, final List<CodeSnippet> snippets) {
        final MethodOverrideExamples res = new MethodOverrideExamples();
        res.method = method;
        res.snippets = snippets;
        return res;
    }

    @SerializedName("_id")
    private String id;
    @SerializedName("_rev")
    private String rev;

    private final String providerId = getClass().getSimpleName();

    private IMethodName method;

    private List<CodeSnippet> snippets;

    @Override
    public void validate() {
        ensureIsNotNull(method);
        ensureIsNotEmpty(snippets, "empty snippets are not that useful... not allowed.");
    }

    public List<CodeSnippet> getSnippets() {
        if (snippets == null) {
            return Collections.emptyList();
        }
        return snippets;
    }
}
