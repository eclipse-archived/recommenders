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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.mockito.Matchers;
import org.mockito.Mockito;

public final class TestUtils {

    private static ITypeName defaultType;
    private static IMethodName defaultMethod;
    private static IMethodName defaultConstructor;

    private static IType defaultJavaType;
    private static IMethod defaultJavaMethod;

    static {
        defaultType = VmTypeName.get("Lorg/eclipse/swt/widgets/Button");
        defaultMethod = VmMethodName.get("Lorg/eclipse/swt/widgets/Button.getText()Ljava/lang/String;");
        defaultConstructor = VmMethodName
                .get("Lorg/eclipse/swt/widgets/Button.<init>(Lorg/eclipse/swt/widgets/Composite;I)V");
    }

    private TestUtils() {
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
            Mockito.when(defaultJavaType.getFullyQualifiedName()).thenReturn("org/eclipse/swt/widgets/Composite");
            final IJavaProject javaProject = Mockito.mock(IJavaProject.class);
            Mockito.when(defaultJavaType.getJavaProject()).thenReturn(javaProject);
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
            Mockito.when(defaultJavaMethod.getElementName()).thenReturn("SomeMethod");
            Mockito.when(defaultJavaMethod.getHandleIdentifier()).thenReturn("TestIdentifier");
            Mockito.when(defaultJavaMethod.getDeclaringType()).thenReturn(getDefaultJavaType());
        }
        return defaultJavaMethod;
    }

    public static IJavaElement[] getDefaultElements() {
        return new IJavaElement[] { getDefaultJavaType(), getDefaultJavaMethod() };
    }

}
