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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.rcp.utils.JdtUtils;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public final class ContextFactory {

    @Inject
    private static JavaElementResolver elementResolver;

    private ContextFactory() {
    }

    public static MockedIntelligentCompletionContext setNullVariableContext(final IJavaElementSelection selection) {
        return new MockedIntelligentCompletionContext(selection, elementResolver) {
            @Override
            public Variable getVariable() {
                return null;
            };
        };
    }

    public static MockedIntelligentCompletionContext setThisVariableContext(final IJavaElementSelection selection,
            final IMethod enclosingMethod) {
        final IMethodName ctxEnclosingMethod = elementResolver.toRecMethod(enclosingMethod);
        final IMethodName ctxFirstDeclaration = elementResolver.toRecMethod(JdtUtils
                .findFirstDeclaration(enclosingMethod));

        return new MockedIntelligentCompletionContext(selection, elementResolver) {
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
                return Variable.create("this", elementResolver.toRecType(enclosingMethod.getDeclaringType()),
                        elementResolver.toRecMethod(enclosingMethod));
            };
        };
    }

    public static MockedIntelligentCompletionContext setFieldVariableContext(final IJavaElementSelection selection,
            final IField field) {
        final IType declaringType = field.getDeclaringType();
        try {
            final String typeSignature = field.getTypeSignature();
            final String resolvedTypeName = JavaModelUtil.getResolvedTypeName(typeSignature, declaringType);
            final IJavaProject javaProject = field.getJavaProject();
            final IType fieldType = javaProject.findType(resolvedTypeName);
            return setMockedContext(selection, field.getElementName(), fieldType, false);
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    public static MockedIntelligentCompletionContext setLocalVariableContext(final IJavaElementSelection selection,
            final ILocalVariable var) {
        final String name = var.getElementName();
        final IType variableType = VariableResolver.resolveTypeSignature(var);
        return setMockedContext(selection, name, variableType, false);
    }

    public static MockedIntelligentCompletionContext setMockedContext(final IJavaElementSelection selection,
            final String varName, final IType variableType, final boolean isArgument) {
        if (variableType == null) {
            return null;
        }
        return new MockedIntelligentCompletionContext(selection, elementResolver) {
            @Override
            public Variable getVariable() {
                return Variable.create(varName, elementResolver.toRecType(variableType), getEnclosingMethod());
            };
        };
    }

}
