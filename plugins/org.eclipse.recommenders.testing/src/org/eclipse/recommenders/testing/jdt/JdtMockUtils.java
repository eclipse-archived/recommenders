/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.testing.jdt;

import static org.eclipse.jdt.core.IJavaElement.*;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

public class JdtMockUtils {

    public static IType someType() {
        return mockJavaElement(IType.class, TYPE);
    }

    public static IMethod someMethod() {
        return mockJavaElement(IMethod.class, METHOD);
    }

    public static IField someField() {
        return mockJavaElement(IField.class, FIELD);
    }

    public static IJavaModel someJavaModel() {
        return mockJavaElement(IJavaModel.class, JAVA_MODEL);
    }

    public static ILocalVariable someLocalVariable() {
        return mockJavaElement(ILocalVariable.class, LOCAL_VARIABLE);
    }

    private static <T extends IJavaElement> T mockJavaElement(final Class<T> clazz, final int elementType) {
        final T mock = mock(clazz);
        when(mock.getElementType()).thenReturn(elementType);
        // when(mock.equals(mock)).thenReturn(true);
        return mock;
    }
}
