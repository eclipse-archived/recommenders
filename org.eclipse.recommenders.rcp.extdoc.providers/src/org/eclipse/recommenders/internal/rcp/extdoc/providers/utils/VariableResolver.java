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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

@SuppressWarnings("restriction")
public final class VariableResolver {

    private VariableResolver() {
    }

    public static IType resolveTypeSignature(final ILocalVariable var) {
        try {
            final IType declaringType = (IType) var.getAncestor(IJavaElement.TYPE);
            final String resolvedTypeName = JavaModelUtil.getResolvedTypeName(var.getTypeSignature(), declaringType);
            final IJavaProject javaProject = var.getJavaProject();
            return javaProject.findType(resolvedTypeName);
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    public static IType resolveTypeSignature(final SourceField var) {
        try {
            final IType declaringType = (IType) var.getAncestor(IJavaElement.TYPE);
            final String resolvedTypeName = JavaModelUtil.getResolvedTypeName(var.getTypeSignature(), declaringType);
            final IJavaProject javaProject = var.getJavaProject();
            return javaProject.findType(resolvedTypeName);
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

}
