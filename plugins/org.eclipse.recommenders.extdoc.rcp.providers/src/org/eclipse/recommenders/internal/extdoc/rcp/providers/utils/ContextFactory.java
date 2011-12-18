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
package org.eclipse.recommenders.internal.extdoc.rcp.providers.utils;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.recommenders.extdoc.rcp.selection.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.analysis.codeelements.Variable;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.rcp.JdtUtils;

public final class ContextFactory {

    private ContextFactory() {
    }

    public static MockedIntelligentCompletionContext createNullVariableContext(final IJavaElementSelection selection) {
        return new MockedIntelligentCompletionContext(selection) {
            @Override
            public Variable getVariable() {
                return null;
            };
        };
    }

    public static MockedIntelligentCompletionContext createThisVariableContext(final IJavaElementSelection selection,
            final IMethod enclosingMethod) {
        final IMethodName ctxEnclosingMethod = ElementResolver.toRecMethod(enclosingMethod);
        final IMethodName ctxFirstDeclaration = ElementResolver.toRecMethod(JdtUtils
                .findFirstDeclaration(enclosingMethod));

        return new MockedIntelligentCompletionContext(selection) {
            @Override
            public IMethodName getEnclosingMethod() {
                return ctxEnclosingMethod;
            };

            @Override
            public IMethodName getEnclosingMethodsFirstDeclaration() {
                return ctxFirstDeclaration;
            };

            @Override
            public Variable getVariable() {
                return Variable.create("this", ElementResolver.toRecType(enclosingMethod.getDeclaringType()),
                        ElementResolver.toRecMethod(enclosingMethod));
            };
        };
    }

    public static MockedIntelligentCompletionContext createFieldVariableContext(final IJavaElementSelection selection,
            final IField field) {
        return createMockedContext(selection, field.getElementName(), VariableResolver.resolveTypeSignature(field),
                null);
    }

    public static MockedIntelligentCompletionContext createLocalVariableContext(final IJavaElementSelection selection,
            final ILocalVariable local) {
        return createMockedContext(selection, local.getElementName(), VariableResolver.resolveTypeSignature(local),
                null);
    }

    static MockedIntelligentCompletionContext createLocalVariableContext(final IJavaElementSelection selection,
            final String variableName, final ITypeName variableType, final IMethodName enclosingMethod) {
        return createMockedContext(selection, variableName, variableType, enclosingMethod);
    }

    private static MockedIntelligentCompletionContext createMockedContext(final IJavaElementSelection selection,
            final String variableName, final ITypeName variableType, final IMethodName enclosingMethod) {
        if (variableType == null) {
            return null;
        }
        return new MockedIntelligentCompletionContext(selection) {
            @Override
            public Variable getVariable() {
                return Variable.create(variableName, variableType, enclosingMethod == null ? getEnclosingMethod()
                        : enclosingMethod);
            };
        };
    }

}
