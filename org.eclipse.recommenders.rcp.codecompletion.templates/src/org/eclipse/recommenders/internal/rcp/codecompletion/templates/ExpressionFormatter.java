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
package org.eclipse.recommenders.internal.rcp.codecompletion.templates;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.Expression;

/**
 * Generates the <code>String</code> representation of an
 * <code>Expression</code>.
 */
final class ExpressionFormatter {

    private final MethodFormatter methodFormatter;

    @Inject
    public ExpressionFormatter(final MethodFormatter methodFormatter) {
        this.methodFormatter = methodFormatter;
    }

    String format(final Expression expression) throws JavaModelException {
        String invocationPrefix;
        final IMethodName invokedMethod = expression.getInvokedMethod();
        if (invokedMethod.isInit()) {
            invocationPrefix = "new ";
        } else {
            final String variableName = expression.getCompletionTargetVariable().getName();
            invocationPrefix = String.format("%s.", variableName == null ? "${someVar}" : variableName);
        }
        return String.format("%s%s%s;", getTargetVariableString(expression), invocationPrefix,
                methodFormatter.format(invokedMethod));
    }

    /**
     * @param expression
     *            {@link Expression} holding information about the method call
     *            to format and on which variable it is invoked.
     * @return The left side of an assignment, e.g. "<code>Button b = </code>".
     */
    private static String getTargetVariableString(final Expression expression) {
        final StringBuilder variableString = new StringBuilder(32);
        final IMethodName invokedMethod = expression.getInvokedMethod();

        if (!invokedMethod.isVoid() && !invokedMethod.isInit()) {
            variableString.append(String.format("%s ", Names.vm2srcSimpleTypeName(invokedMethod.getReturnType())));
        }
        if (!invokedMethod.isVoid() || invokedMethod.isInit()) {
            variableString.append(String.format("%s = ", getTargetVariableName(expression)));
        }
        return variableString.toString();
    }

    private static String getTargetVariableName(final Expression expression) {
        final IMethodName invokedMethod = expression.getInvokedMethod();
        String variableName = null;
        if (invokedMethod.isInit()) {
            variableName = expression.getCompletionTargetVariable().getName();
        }
        if (variableName == null) {
            variableName = getVariableNameFromMethod(invokedMethod);
        }
        return variableName;
    }

    private static String getVariableNameFromMethod(final IMethodName methodName) {
        String variableName;
        if (methodName.getName().startsWith("get")) {
            variableName = methodName.getName().substring(3);
        } else {
            variableName = methodName.getReturnType().getClassName();
        }
        return StringUtils.uncapitalize(variableName);
    }

    public void resetArgumentCounter() {
        methodFormatter.resetArgumentCounter();
    }
}
