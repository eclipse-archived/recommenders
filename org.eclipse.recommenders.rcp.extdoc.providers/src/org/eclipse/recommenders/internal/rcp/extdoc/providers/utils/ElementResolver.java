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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ImportContainer;
import org.eclipse.jdt.internal.core.ImportDeclaration;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.recommenders.commons.utils.annotations.Provisional;
import org.eclipse.recommenders.commons.utils.names.IFieldName;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmFieldName;
import org.eclipse.recommenders.commons.utils.names.VmPackageName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public final class ElementResolver {

    @Inject
    private static JavaElementResolver resolver;

    private ElementResolver() {
    }

    public static IName resolveName(final IJavaElement javaElement) {
        if (javaElement instanceof IMethod) {
            return toRecMethod((IMethod) javaElement);
        } else if (javaElement instanceof IType) {
            return toRecType((IType) javaElement);
        } else if (javaElement instanceof ILocalVariable) {
            final IMethodName declaringMethod = toRecMethod((IMethod) javaElement.getParent());
            return Variable.create(javaElement.getElementName(), null, declaringMethod).getName();
        } else if (javaElement instanceof IField) {
            final ITypeName type = VariableResolver.resolveTypeSignature((IField) javaElement);
            return type == null ? null : toRecField((IField) javaElement, type);
        } else if (javaElement instanceof IPackageFragment || javaElement instanceof IPackageDeclaration) {
            return VmPackageName.get(javaElement.getElementName());
        } else if (javaElement instanceof ICompilationUnit) {
            try {
                // TODO: This looks dangerous.
                return toRecType(((ICompilationUnit) javaElement).getTypes()[0]);
            } catch (final JavaModelException e) {
                throw new IllegalStateException(e);
            }
        } else if (javaElement instanceof ImportContainer || javaElement instanceof ImportDeclaration
                || javaElement instanceof JavaProject || javaElement instanceof PackageFragmentRoot
                || javaElement instanceof JarPackageFragmentRoot) {
            return null;
        }
        throw new IllegalArgumentException(javaElement.getClass().toString());
    }

    public static IMethodName toRecMethod(final IMethod jdtMethod) {
        return resolver.toRecMethod(jdtMethod);
    }

    public static IType toJdtType(final ITypeName jdtType) {
        return resolver.toJdtType(jdtType);
    }

    public static ITypeName toRecType(final IType jdtType) {
        return resolver.toRecType(jdtType);
    }

    public static IFieldName toRecField(final IField field, final ITypeName type) {
        return VmFieldName.get(toRecType((IType) field.getParent()).getIdentifier() + "." + field.getElementName()
                + ";" + type.getIdentifier());
    }

    @Provisional
    public static void setJavaElementResolver(final JavaElementResolver elementResolver) {
        resolver = elementResolver;
    }

}
