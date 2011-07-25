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

import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.rcp.extdoc.IServerType;

public final class CodeExamples implements IServerType {

    @SuppressWarnings("unused")
    private final String providerId = getClass().getSimpleName();
    private ITypeName type;

    private IMethodName method;
    private CodeSnippet[] examples;

    public static CodeExamples create(final ITypeName type, final IMethodName method, final CodeSnippet... examples) {
        final CodeExamples result = new CodeExamples();
        result.type = type;
        result.method = method;
        result.examples = examples;
        result.validate();
        return result;
    }

    public CodeSnippet[] getExamples() {
        return examples == null ? new CodeSnippet[0] : examples.clone();
    }

    @Override
    public void validate() {
        Checks.ensureIsNotNull(type);
        Checks.ensureIsNotNull(method);
        Checks.ensureIsTrue(examples.length > 0);
    }
}
