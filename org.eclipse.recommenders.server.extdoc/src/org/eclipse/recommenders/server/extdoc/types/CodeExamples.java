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

import org.eclipse.recommenders.commons.utils.names.IMethodName;

import com.google.gson.annotations.SerializedName;

public class CodeExamples {

    @SerializedName("_id")
    private String id;
    @SerializedName("_rev")
    private String rev;

    private final String providerId = getClass().getSimpleName();
    private IMethodName method;
    private CodeSnippet[] examples;

    public static CodeExamples create(final IMethodName method, final CodeSnippet... examples) {
        final CodeExamples result = new CodeExamples();
        result.method = method;
        result.examples = examples;
        return result;
    }

    public CodeSnippet[] getExamples() {
        return examples;
    }
}
