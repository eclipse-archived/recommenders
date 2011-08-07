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
package org.eclipse.recommenders.tests.commons.extdoc;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;

import org.mockito.Matchers;
import org.mockito.Mockito;

public final class TestTypeUtils {

    private static ITypeName defaultType;
    private static IMethodName defaultMethod;
    private static IMethodName defaultConstructor;

    private static IType defaultJavaType;
    private static IMethod defaultJavaMethod;

    private static IField defaultField;
    private static ILocalVariable defaultVariable;

    private static JavaProject defaultProject = Mockito.mock(JavaProject.class);

    static {
        defaultType = VmTypeName.get("Lorg/eclipse/swt/widgets/Button");
        defaultMethod = VmMethodName.get("Lorg/eclipse/swt/widgets/Button.getText()Ljava/lang/String;");
        defaultConstructor = VmMethodName
                .get("Lorg/eclipse/swt/widgets/Button.<init>(Lorg/eclipse/swt/widgets/Composite;I)V");

        getDefaultJavaType();
        try {
            Mockito.when(defaultProject.findType(Matchers.anyString())).thenReturn(defaultJavaType);
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    private TestTypeUtils() {
    }

    public static ITypeName getDefaultType() {
        return defaultType;
    }

    public static IMethodName getDefaultMethod() {
        return defaultMethod;
    }

    public static IMethodName getDefaultConstructor() {
        return defaultConstructor;
    }

    public static IName[] getDefaultNames() {
        return new IName[] { defaultType, defaultMethod, defaultConstructor };
    }

    public static IType getDefaultJavaType() {
        if (defaultJavaType == null) {
            defaultJavaType = Mockito.mock(IType.class);
            Mockito.when(defaultJavaType.getPrimaryElement()).thenReturn(defaultJavaType);
            Mockito.when(defaultJavaType.getElementName()).thenReturn("Composite");
            Mockito.when(defaultJavaType.getHandleIdentifier()).thenReturn("TestIdentifier");
            Mockito.when(defaultJavaType.getFullyQualifiedName()).thenReturn("org/eclipse/swt/widgets/Button");
            Mockito.when(defaultJavaType.getJavaProject()).thenReturn(getDefaultProject());
            try {
                final ITypeHierarchy hierarchy = Mockito.mock(ITypeHierarchy.class);
                Mockito.when(hierarchy.getSuperInterfaces(Matchers.any(IType.class))).thenReturn(new IType[0]);
                Mockito.when(defaultJavaType.newSupertypeHierarchy(Matchers.any(IProgressMonitor.class))).thenReturn(
                        hierarchy);
            } catch (final JavaModelException e) {
                throw new IllegalStateException(e);
            }
        }
        return defaultJavaType;
    }

    public static IMethod getDefaultJavaMethod() {
        if (defaultJavaMethod == null) {
            defaultJavaMethod = Mockito.mock(IMethod.class);
            Mockito.when(defaultJavaMethod.getPrimaryElement()).thenReturn(defaultJavaMethod);
            Mockito.when(defaultJavaMethod.getElementName()).thenReturn("getText");
            Mockito.when(defaultJavaMethod.getHandleIdentifier()).thenReturn("TestIdentifier");
            Mockito.when(defaultJavaMethod.getDeclaringType()).thenReturn(getDefaultJavaType());
            Mockito.when(defaultJavaMethod.getParameterTypes()).thenReturn(new String[0]);
            try {
                Mockito.when(defaultJavaMethod.getReturnType()).thenReturn("Ljava/lang/String;");
            } catch (final JavaModelException e) {
                throw new IllegalStateException(e);
            }
        }
        return defaultJavaMethod;
    }

    public static IJavaElement[] getDefaultElements() {
        return new IJavaElement[] { getDefaultJavaType(), getDefaultJavaMethod(), getDefaultField(),
                getDefaultVariable() };
    }

    public static IField getDefaultField() {
        if (defaultField == null) {
            defaultField = Mockito.mock(IField.class);
            Mockito.when(defaultField.getDeclaringType()).thenReturn(TestTypeUtils.getDefaultJavaType());
            Mockito.when(defaultField.getParent()).thenReturn(TestTypeUtils.getDefaultJavaType());
            try {
                Mockito.when(defaultField.getTypeSignature()).thenReturn("Lorg/eclipse/swt/widgets/Button;");
            } catch (final JavaModelException e) {
                throw new IllegalStateException(e);
            }
        }
        return defaultField;
    }

    public static ILocalVariable getDefaultVariable() {
        if (defaultVariable == null) {
            defaultVariable = Mockito.mock(ILocalVariable.class);
            Mockito.when(defaultVariable.getAncestor(IJavaElement.TYPE)).thenReturn(TestTypeUtils.getDefaultJavaType());
            Mockito.when(defaultVariable.getTypeSignature()).thenReturn("Lorg/eclipse/swt/widgets/Button;");
        }
        return defaultVariable;
    }

    public static IJavaProject getDefaultProject() {
        return defaultProject;
    }

}
