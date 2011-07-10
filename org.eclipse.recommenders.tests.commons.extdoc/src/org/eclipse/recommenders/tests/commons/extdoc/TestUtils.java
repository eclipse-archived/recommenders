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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.mockito.Mockito;

public final class TestUtils {

    private static IType defaultType;
    private static IMethod defaultMethod;

    private TestUtils() {
    }

    public static IType getDefaultType() {
        if (defaultType == null) {
            defaultType = Mockito.mock(IType.class);
            Mockito.when(defaultType.getPrimaryElement()).thenReturn(defaultType);
            Mockito.when(defaultType.getFullyQualifiedName()).thenReturn("org/eclipse/swt/widgets/Composite");
        }
        return defaultType;
    }

    public static IMethod getDefaultMethod() {
        if (defaultMethod == null) {
            defaultMethod = Mockito.mock(IMethod.class);
            Mockito.when(defaultMethod.getPrimaryElement()).thenReturn(defaultMethod);
            final IType declaringType = getDefaultType();
            Mockito.when(defaultMethod.getDeclaringType()).thenReturn(declaringType);
            Mockito.when(defaultMethod.getParameterTypes()).thenReturn(new String[] {});
            try {
                Mockito.when(defaultMethod.getReturnType()).thenReturn("V");
                Mockito.when(defaultMethod.isConstructor()).thenReturn(true);
            } catch (final JavaModelException e) {
                throw new IllegalStateException(e);
            }
        }
        return defaultMethod;
    }

}
