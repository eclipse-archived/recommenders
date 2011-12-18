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

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Names;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;

/**
 * Generates the <code>String</code> representation of an {@link IMethod}.
 */
public class MethodFormatter {

    private final JavaElementResolver elementResolver;
    private final Map<String, Integer> argumentCounter = new HashMap<String, Integer>();

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
    public final String format(final IMethodName methodName) throws JavaModelException {
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
    private String getParametersString(final IMethodName methodName) throws JavaModelException {
        final StringBuilder parameters = new StringBuilder(32);
        final IMethod jdtMethod = elementResolver.toJdtMethod(methodName);
        if (jdtMethod == null) {
            throw new JavaModelException(new IllegalStateException(), IJavaModelStatusConstants.CORE_EXCEPTION);
        }
        final String[] parameterNames = jdtMethod.getParameterNames();
        final String[] parameterTypes = jdtMethod.getParameterTypes();
        for (int i = 0; i < parameterNames.length; ++i) {
            final String typeIdentifier = StringUtils.chomp(parameterTypes[i].replace('.', '/'), ";");
            final VmTypeName parameterType = VmTypeName.get(typeIdentifier);
            final String parameterString = getParameterString(parameterNames[i], parameterType);
            parameters.append(parameterString);
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
    private String getParameterString(final String parameterName, final ITypeName parameterType) {
        String appendix = "";
        // TODO: Appendix for more types, add array support.
        if (parameterType.isDeclaredType()) {
            if (!parameterType.getIdentifier().startsWith("Ljava")) {
                final String typeName = Names.vm2srcTypeName(parameterType.getIdentifier());
                appendix = String.format(":var(%s)", typeName);
            }
        } else if (parameterType == VmTypeName.BOOLEAN) {
            appendix = ":link(false, true)";
        } else if (parameterType == VmTypeName.INT || parameterType == VmTypeName.DOUBLE
                || parameterType == VmTypeName.FLOAT || parameterType == VmTypeName.LONG
                || parameterType == VmTypeName.SHORT) {
            appendix = ":link(0)";
        }
        return String.format("${%s%s}", getParameterName(parameterName), appendix);
    }

    /**
     * @param parameterName
     *            The parameter's name as resolved by JDT.
     * @return The unique parameter name, i.e. if there already has been a name
     *         "button" in the current template, the new parameter name will be
     *         "button2".
     */
    private String getParameterName(final String parameterName) {
        final String name = parameterName.length() <= 5 && parameterName.startsWith("arg") ? "arg" : parameterName;
        if (argumentCounter.containsKey(name)) {
            final Integer counter = argumentCounter.get(name);
            argumentCounter.put(name, counter + 1);
            return String.format("%s%s", name, counter + 1);
        } else {
            argumentCounter.put(name, 1);
        }
        return name;
    }

    /**
     * Eclipse templates disallow the use of same names for different
     * parameters. Therefore we count the occurrences of each parameter name, so
     * we can assign unique names, e.g. turn "<code>button</code>" into "
     * <code>button3</code>". This method resets the counter (usually called
     * after the pattern is completed).
     */
    final void resetArgumentCounter() {
        argumentCounter.clear();
    }

}
