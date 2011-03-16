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
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
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
        final Set<ITypeName> imports = Sets.newHashSetWithExpectedSize(8);
        for (final IMethodName method : methods) {
            try {
                code.append(methodCallFormatter.format(new MethodCall(targetVariableName, method)));
                code.append(lineSeparator);
            } catch (final JavaModelException e) {
                Throws.throwUnhandledException(e);
            }
            addTypeToImports(method, imports);
        }
        // appendImports(imports, code);
        methodCallFormatter.resetArgumentCounter();
        return String.format("%s${cursor}", code);
    }

    private void addTypeToImports(final IMethodName returningMethod, final Set<ITypeName> imports) {
        if (!returningMethod.isVoid()) {
            imports.add(returningMethod.getReturnType());
        } else if (returningMethod.isInit()) {
            imports.add(returningMethod.getDeclaringType());
        }
    }

    private void appendImports(final Set<ITypeName> imports, final StringBuilder code) {
        if (!imports.isEmpty()) {
            code.append("${imp:import(");
            for (final ITypeName importType : imports) {
                code.append(Names.vm2srcTypeName(importType.getIdentifier()));
                code.append(", ");
            }
            code.replace(code.length() - 3, code.length() - 1, ")}");
        }
    }

}
