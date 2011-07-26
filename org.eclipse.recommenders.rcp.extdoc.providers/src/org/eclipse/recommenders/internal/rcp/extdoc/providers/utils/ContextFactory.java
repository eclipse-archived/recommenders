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
package org.eclipse.recommenders.internal.rcp.extdoc.providers.utils;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.rcp.utils.JdtUtils;

public final class ContextFactory {

    private ContextFactory() {
    }

    public static MockedIntelligentCompletionContext setNullVariableContext(final IJavaElementSelection selection) {
        return new MockedIntelligentCompletionContext(selection) {
            @Override
            public Variable getVariable() {
                return null;
            };
        };
    }

    public static MockedIntelligentCompletionContext setThisVariableContext(final IJavaElementSelection selection,
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

    public static MockedIntelligentCompletionContext setFieldVariableContext(final IJavaElementSelection selection,
            final IField field) {
        return setMockedContext(selection, field.getElementName(), VariableResolver.resolveTypeSignature(field), false);
    }

    public static MockedIntelligentCompletionContext setLocalVariableContext(final IJavaElementSelection selection,
            final ILocalVariable var) {
        final String name = var.getElementName();
        final ITypeName variableType = VariableResolver.resolveTypeSignature(var);
        return setMockedContext(selection, name, variableType, false);
    }

    private static MockedIntelligentCompletionContext setMockedContext(final IJavaElementSelection selection,
            final String variableName, final ITypeName variableType, final boolean isArgument) {
        if (variableType == null) {
            return null;
        }
        return new MockedIntelligentCompletionContext(selection) {
            @Override
            public Variable getVariable() {
                return Variable.create(variableName, variableType, getEnclosingMethod());
            };
        };
    }

}
