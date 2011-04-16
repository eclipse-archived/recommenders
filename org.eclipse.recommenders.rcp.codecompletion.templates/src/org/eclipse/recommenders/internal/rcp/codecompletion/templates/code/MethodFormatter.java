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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;

import com.google.inject.Inject;

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
        this.elementResolver = Checks.ensureIsNotNull(elementResolver);
    }

    /**
     * @param methodName
     *            The method which shall be turned into java code.
     * @return A string containing the java code for the given method.
     */
    public final String format(final IMethodName methodName) {
        String method;
        if (methodName.isInit()) {
            method = "${constructedType}";
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
     */
    private String getParametersString(final IMethodName methodName) {
        final StringBuilder parameters = new StringBuilder(32);
        final IMethod jdtMethod = elementResolver.toJdtMethod(methodName);
        try {
            final String[] parameterNames = jdtMethod.getParameterNames();
            final String[] parameterTypes = jdtMethod.getParameterTypes();
            for (int i = 0; i < parameterNames.length; ++i) {
                final String typeIdentifier = StringUtils.chomp(parameterTypes[i].replace('.', '/'), ";");
                final VmTypeName parameterType = VmTypeName.get(typeIdentifier);
                final String parameterString = getParameterString(parameterNames[i], parameterType);
                parameters.append(parameterString);
                parameters.append(", ");
            }
        } catch (final JavaModelException e) {
            Throws.throwUnhandledException(e);
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
    private String getParameterString(final String parameterName, final ITypeName parameterType) {
        String appendix;
        // TODO: Appendix for more types.
        if (parameterType.isDeclaredType() || parameterType.isArrayType()) {
            final String typeName = Names.vm2srcTypeName(parameterType.getIdentifier());
            appendix = String.format(":var(%s)", typeName);
        } else if (parameterType == VmTypeName.BOOLEAN) {
            appendix = ":link(false, true)";
        } else if (parameterType == VmTypeName.INT || parameterType == VmTypeName.DOUBLE
                || parameterType == VmTypeName.FLOAT || parameterType == VmTypeName.LONG
                || parameterType == VmTypeName.SHORT) {
            appendix = ":link(0)";
        } else {
            appendix = "";
        }
        return String.format("${%s%s}", getParameterName(parameterName), appendix);
    }

    /**
     * @param parameterName
     *            The parameter's name as resolved by JDT.
     * @return The parameter name after it is modified in case it was in "
     *         <code>arg0</code>" format.
     */
    private String getParameterName(final String parameterName) {
        if (parameterName.startsWith("arg")) {
            ++argumentCounter;
            return String.format("arg%d", Integer.valueOf(argumentCounter));
        }
        return parameterName;
    }

    /**
     * Eclipse templates disallow the use of same names for different
     * parameters. If parameter names are unknown they usually are named
     * <code>arg0</code>, <code>arg1</code>, etc. This enumeration starts at 0
     * with each new expression so we have to ensure a continuous enumeration.
     * This method resets the counter (e.g. after the pattern is completed).
     */
    final void resetArgumentCounter() {
        argumentCounter = -1;
    }

}
