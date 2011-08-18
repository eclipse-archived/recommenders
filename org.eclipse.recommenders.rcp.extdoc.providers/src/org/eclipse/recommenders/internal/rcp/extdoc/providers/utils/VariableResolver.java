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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.Names.PrimitiveType;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;

@SuppressWarnings("restriction")
public final class VariableResolver {

    private VariableResolver() {
    }

    public static ITypeName resolveTypeSignature(final ILocalVariable var) {
        try {
            final IType declaringType = (IType) var.getAncestor(IJavaElement.TYPE);
            return resolveTypeSignature(declaringType, var.getTypeSignature());
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ITypeName resolveTypeSignature(final IField var) {
        try {
            return resolveTypeSignature(var.getDeclaringType(), var.getTypeSignature());
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ITypeName resolveTypeSignature(final IType declaringType, final String typeSignature)
            throws JavaModelException {
        final String resolvedTypeName = JavaModelUtil.getResolvedTypeName(typeSignature, declaringType);
        if (resolvedTypeName == null) {
            return null;
        }
        if (PrimitiveType.fromSrc(resolvedTypeName) != null) {
            return resolvePrimitive(resolvedTypeName);
        }
        final IJavaProject javaProject = declaringType.getJavaProject();
        return ElementResolver.toRecType(javaProject.findType(resolvedTypeName));
    }

    private static ITypeName resolvePrimitive(final String primitiveTypeName) {
        return VmTypeName.get(Names.src2vmType(primitiveTypeName));
    }

}
