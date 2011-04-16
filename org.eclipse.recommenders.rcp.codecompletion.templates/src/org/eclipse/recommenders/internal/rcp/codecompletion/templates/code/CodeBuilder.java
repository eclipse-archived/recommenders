/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.templates.code;

import java.util.List;

import com.google.inject.Inject;

import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.MethodCall;

/**
 * Builds an Eclipse templates code from a list of method calls on a given
 * variable name.
 */
public final class CodeBuilder {

    private final MethodCallFormatter methodCallFormatter;
    private final String lineSeparator = System.getProperty("line.separator");

    /**
     * @param methodCallFormatter
     *            The formatter which will turn the given {@link MethodCall}s on
     *            a specified variable name into java code which will be
     *            inserted when the completion is selected.
     */
    @Inject
    public CodeBuilder(final MethodCallFormatter methodCallFormatter) {
        this.methodCallFormatter = methodCallFormatter;
    }

    /**
     * @param methods
     *            The pattern's method to be included in the template.
     * @param targetVariableName
     *            The name of the variable on which the proposed methods shall
     *            be invoked.
     * @return The code to be inserted into the document, built from the
     *         recommended method calls and the given target variable.
     */
    public String buildCode(final List<IMethodName> methods, final String targetVariableName) {
        final StringBuilder code = new StringBuilder(methods.size() * 16);
        for (final IMethodName method : methods) {
            MethodCall methodCall = new MethodCall(targetVariableName, method);
            String statement = methodCallFormatter.format(methodCall);
            code.append(statement);
            code.append(lineSeparator);
        }
        methodCallFormatter.resetArgumentCounter();
        return String.format("%s${cursor}", code);
    }
}
