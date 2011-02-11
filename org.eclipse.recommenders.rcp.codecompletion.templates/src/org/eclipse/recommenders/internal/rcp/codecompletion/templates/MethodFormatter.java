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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;

/**
 * Generates the <code>String</code> representation of an {@link IMethod}.
 */
class MethodFormatter {

    private final JavaElementResolver resolver = new JavaElementResolver();
    private int argumentCounter = -1;

    String format(final IMethodName methodName) throws JavaModelException {
        String method = "";
        if (methodName.isInit()) {
            method = methodName.getDeclaringType().getClassName();
        } else {
            method = methodName.getName();
        }
        return String.format("%s(%s)", method, getParametersString(methodName));
    }

    String getParametersString(final IMethodName methodName) throws JavaModelException {
        final StringBuilder parameters = new StringBuilder(32);
        final IMethod jdtMethod = resolver.toJdtMethod(methodName);
        final String[] parameterNames = jdtMethod.getParameterNames();
        final String[] parameterTypes = jdtMethod.getParameterTypes();
        for (int i = 0; i < parameterNames.length; ++i) {
            parameters.append(getParameterString(parameterNames[i], parameterTypes[i]));
            parameters.append(", ");
        }
        return StringUtils.chomp(parameters.toString(), ", ");
    }

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

    private String getParameterName(final String parameterName) {
        String name = parameterName;
        if (parameterName.startsWith("arg")) {
            ++argumentCounter;
            name = String.format("arg%d", Integer.valueOf(argumentCounter));
        }
        return name;
    }

    void resetArgumentCounter() {
        argumentCounter = -1;
    }

}
