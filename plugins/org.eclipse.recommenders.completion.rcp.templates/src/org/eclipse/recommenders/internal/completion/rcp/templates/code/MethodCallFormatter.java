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
package org.eclipse.recommenders.internal.completion.rcp.templates.code;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.internal.completion.rcp.templates.types.MethodCall;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Names;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.inject.Inject;

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
        this.methodFormatter = Checks.ensureIsNotNull(methodFormatter);
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
    public String format(final MethodCall methodCall) throws JavaModelException {
        String invocationPrefix = "";
        final IMethodName invokedMethod = methodCall.getInvokedMethod();
        if (invokedMethod.isInit()) {
            invocationPrefix = "new ";
        } else {
            String variableName = methodCall.getVariable().getName();
            if (variableName.isEmpty() && !methodCall.getVariable().isThis()) {
                variableName = "${unconstructed}";
            }
            if (!variableName.isEmpty()) {
                invocationPrefix = String.format("%s.", variableName);
            }
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
        final IMethodName invokedMethod = methodCall.getInvokedMethod();
        if (!invokedMethod.isVoid() || invokedMethod.isInit()) {
            final String variableType = getNewVariableTypeString(invokedMethod);
            final String variableName = getNewVariableName(methodCall);
            return String.format("%s %s = ", variableType, variableName);
        }
        return "";
    }

    /**
     * @param invokedMethod
     *            The method which will return or construct the new variable.
     * @return How the type of the new variable is declared, as part of the
     *         variable declaration, e.g. " <code>Button</code>".
     */
    private static String getNewVariableTypeString(final IMethodName invokedMethod) {
        if (invokedMethod.isInit()) {
            return String.format("${constructedType:newType(%s)}", getTypeIdentifier(invokedMethod.getDeclaringType()));
        }
        final ITypeName returnType = invokedMethod.getReturnType();
        if (returnType.isPrimitiveType()) {
            return Names.vm2srcSimpleTypeName(returnType);
        } else {
            final String typeIdentifier = getTypeIdentifier(returnType);
            final String returnTypeTemplateVariable = typeIdentifier.replaceAll("\\W", "_");
            return String.format("${%s:newType(%s)}%s", returnTypeTemplateVariable, typeIdentifier,
                    returnType.isArrayType() ? StringUtils.repeat("[]", returnType.getArrayDimensions()) : "");
        }
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
            variableName = methodCall.getVariable().getName();
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
        final IMethodName invokedMethod = methodCall.getInvokedMethod();
        if (invokedMethod.isInit()) {
            variableName = String.format("${unconstructed:newName(%s)}",
                    getTypeIdentifier(invokedMethod.getDeclaringType()));
        } else {
            if (invokedMethod.getName().startsWith("get")) {
                variableName = StringUtils.uncapitalize(invokedMethod.getName().substring(3));
            } else {
                variableName = StringUtils.uncapitalize(invokedMethod.getReturnType().getClassName());
            }
            if (variableName.equals(methodCall.getVariable().getName())) {
                variableName = getNewVariableNameFromReturnType(invokedMethod.getReturnType());
            }
        }
        return variableName;
    }

    /**
     * @param returnType
     *            The type as returned by the invoked method.
     * @return The template code which generates a new variable name based on
     *         the new variable type (as given by the return type).
     */
    private static String getNewVariableNameFromReturnType(final ITypeName returnType) {
        return String.format("${returned:newName(%s)}", getTypeIdentifier(returnType));
    }

    /**
     * @param typeName
     *            The type name to format.
     * @return The fully qualified type name, suiting the templates format.
     */
    private static String getTypeIdentifier(final ITypeName typeName) {
        return typeName.isPrimitiveType() ? Names.vm2srcSimpleTypeName(typeName) : typeName.getIdentifier()
                .replace('/', '.').substring(1);
    }

    /**
     * Eclipse templates disallow the use of same names for different
     * parameters. Therefore we count the occurrences of each parameter name, so
     * we can assign unique names, e.g. turn "<code>button</code>" into "
     * <code>button3</code>". This method resets the counter (usually called
     * after the pattern is completed).
     */
    public void resetArgumentCounter() {
        methodFormatter.resetArgumentCounter();
    }
}
