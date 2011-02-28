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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;

/**
 * Generates the <code>String</code> representation of an {@link IMethod}.
 */
public class MethodFormatter {

    private final JavaElementResolver elementResolver;
    private int argumentCounter = -1;

    /**
     * @param elementResolver
     *            Is responsible for converting an {@link IMethodName} observed
     *            within the context into an {@link IMethod}.
     */
    @Inject
    public MethodFormatter(final JavaElementResolver elementResolver) {
        this.elementResolver = elementResolver;
    }

    /**
     * @param methodName
     *            The method which shall be turned into java code.
     * @return A string containing the java code for the given method.
     * @throws JavaModelException
     *             When the method could not be resolved by JDT.
     */
    public String format(final IMethodName methodName) throws JavaModelException {
        String method = "";
        if (methodName.isInit()) {
            method = methodName.getDeclaringType().getClassName();
        } else {
            method = methodName.getName();
        }
        return String.format("%s(%s)", method, getParametersString(methodName));
    }

    /**
     * @param methodName
     *            The method for which to get the parameter names.
     * @return The method's parameters as template code, e.g.
     *         <code>${string}, ${selected:link(false, true)}</code>.
     * @throws JavaModelException
     *             When the method could not be resolved by JDT.
     */
    private String getParametersString(final IMethodName methodName) throws JavaModelException {
        final StringBuilder parameters = new StringBuilder(32);
        final IMethod jdtMethod = elementResolver.toJdtMethod(methodName);
        final String[] parameterNames = jdtMethod.getParameterNames();
        final String[] parameterTypes = jdtMethod.getParameterTypes();
        for (int i = 0; i < parameterNames.length; ++i) {
            parameters.append(getParameterString(parameterNames[i], parameterTypes[i]));
            parameters.append(", ");
        }
        return StringUtils.chomp(parameters.toString(), ", ");
    }

    /**
     * @param parameterName
     *            The parameter's name as resolved by JDT.
     * @param parameterType
     *            The parameter's type as resolved by JDT.
     * @return The template code for a single parameter, e.g.
     *         <code>${listener:var(org.eclipse.swt.events.SelectionListener)}</code>
     *         .
     */
    private String getParameterString(final String parameterName, final String parameterType) {
        final StringBuilder parameter = new StringBuilder(32);
        parameter.append(getParameterName(parameterName));
        if ("I".equals(parameterType)) {
            parameter.append(":link(0)");
        } else if ("Z".equals(parameterType)) {
            parameter.append(":link(false, true)");
        } else if (parameterType.endsWith(";") && !parameterType.startsWith("Ljava")) {
            parameter.append(String.format(":var(%s)", parameterType.substring(1, parameterType.length() - 1)));
        }
        return String.format("${%s}", parameter);
    }

    /**
     * @param parameterName
     *            The parameter's name as resolved by JDT.
     * @return The parameter name after it is modified in case it was in "
     *         <code>arg0</code>" format.
     */
    private String getParameterName(final String parameterName) {
        String name = parameterName;
        if (parameterName.startsWith("arg")) {
            ++argumentCounter;
            name = String.format("arg%d", Integer.valueOf(argumentCounter));
        }
        return name;
    }

    /**
     * Eclipse templates disallow the use of same names for different
     * parameters. If parameter names are unknown they usually are named
     * <code>arg0</code>, <code>arg1</code>, etc. This enumeration starts at 0
     * with each new expression so we have to ensure a continuous enumeration.
     * This method resets the counter (e.g. after the pattern is completed).
     */
    void resetArgumentCounter() {
        argumentCounter = -1;
    }

}
