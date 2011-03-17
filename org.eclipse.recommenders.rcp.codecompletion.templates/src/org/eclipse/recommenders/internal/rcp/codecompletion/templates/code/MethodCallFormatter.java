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

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.MethodCall;

/**
 * Generates the <code>String</code> representation of a {@link MethodCall}.
 */
public final class MethodCallFormatter {

    private final MethodFormatter methodFormatter;

    /**
     * @param methodFormatter
     *            The formatter which is responsible for transforming a method
     *            call into a java code string.
     */
    @Inject
    public MethodCallFormatter(final MethodFormatter methodFormatter) {
        this.methodFormatter = methodFormatter;
    }

    /**
     * @param methodCall
     *            {@link MethodCall} holding information about the method call
     *            to format and on which variable it is invoked.
     * @return A string representation of the whole expression, i.e. the
     *         definition of a new variable if something is returned and the
     *         variable with the invoked method, e.g. "
     *         <code>String text = button.getText()</code>".
     */
    public String format(final MethodCall methodCall) {
        String invocationPrefix;
        final IMethodName invokedMethod = methodCall.getInvokedMethod();
        if (invokedMethod.isInit()) {
            invocationPrefix = "new ";
        } else {
            String variableName = methodCall.getVariableName();
            if (variableName == null || variableName.isEmpty()) {
                variableName = "${unconstructed}";
            }
            invocationPrefix = String.format("%s.", variableName);
        }
        return String.format("%s%s%s;", getNewVariableString(methodCall), invocationPrefix,
                methodFormatter.format(invokedMethod));
    }

    /**
     * @param methodCall
     *            {@link MethodCall} holding information about the method call
     *            to format and on which variable it is invoked.
     * @return The left side of an assignment, e.g. "<code>Button b = </code>".
     */
    private static String getNewVariableString(final MethodCall methodCall) {
        String variableString = "";
        final IMethodName invokedMethod = methodCall.getInvokedMethod();
        if (!invokedMethod.isVoid() || invokedMethod.isInit()) {
            final String variableType = getNewVariableTypeString(invokedMethod);
            final String variableName = getNewVariableName(methodCall);
            variableString = String.format("%s %s = ", variableType, variableName);
        }
        return variableString;
    }

    private static String getNewVariableTypeString(final IMethodName invokedMethod) {
        String typeString;
        if (invokedMethod.isInit()) {
            typeString = String.format("${constructedType:newType(%s)}",
                    Names.vm2srcQualifiedType(invokedMethod.getDeclaringType()));
        } else {
            typeString = String.format("${returnedType:newType(%s)}",
                    Names.vm2srcSimpleTypeName(invokedMethod.getReturnType()));
        }
        return typeString;
    }

    /**
     * @param methodCall
     *            {@link MethodCall} holding information about the method call
     *            to format and on which variable it is invoked.
     * @return The name of the new variable created from the method's returned
     *         type.
     */
    private static String getNewVariableName(final MethodCall methodCall) {
        String variableName = null;
        if (methodCall.getInvokedMethod().isInit()) {
            variableName = methodCall.getVariableName();
        }
        if (variableName == null || variableName.isEmpty()) {
            variableName = getNewVariableNameFromMethod(methodCall);
        }
        return variableName;
    }

    /**
     * @param methodCall
     *            {@link MethodCall} holding information about the method call
     *            to format and on which variable it is invoked.
     * @return A variable name from the given method's name (e.g. "
     *         <code>getText</code>" to "<code>text</code>") or its return type
     *         (e.g. "<code>someMethod : String</code>" to "<code>string</code>
     *         ").
     */
    private static String getNewVariableNameFromMethod(final MethodCall methodCall) {
        String variableName;
        final IMethodName methodName = methodCall.getInvokedMethod();
        if (methodName.isInit()) {
            final String type = Names.vm2srcTypeName(methodName.getDeclaringType().getIdentifier());
            variableName = String.format("${unconstructed:newName(%s)}", type);
        } else {
            if (methodName.getName().startsWith("get")) {
                variableName = StringUtils.uncapitalize(methodCall.getInvokedMethod().getName().substring(3));
            } else {
                variableName = StringUtils.uncapitalize(methodName.getReturnType().getClassName());
            }
            if (variableName.equals(methodCall.getVariableName())) {
                variableName = getNewVariableNameFromReturnType(methodCall);
            }
        }
        return variableName;
    }

    private static String getNewVariableNameFromReturnType(final MethodCall methodCall) {
        final String type = Names.vm2srcTypeName(methodCall.getInvokedMethod().getReturnType().getIdentifier());
        return String.format("${returned:newName(%s)}", type);
    }

    /**
     * Eclipse templates disallow the use of same names for different
     * parameters. If parameter names are unknown they usually are named
     * <code>arg0</code>, <code>arg1</code>, etc. This enumeration starts at 0
     * with each new expression so we have to ensure a continuous enumeration.
     * This method resets the counter (e.g. after the pattern is completed).
     */
    public void resetArgumentCounter() {
        methodFormatter.resetArgumentCounter();
    }
}
