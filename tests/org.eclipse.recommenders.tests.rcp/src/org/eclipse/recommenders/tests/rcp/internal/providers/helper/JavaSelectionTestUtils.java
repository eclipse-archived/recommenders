/**
 * Copyright (c) 2011 Sebastian Proksch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.tests.rcp.internal.providers.helper;

import static org.eclipse.recommenders.rcp.events.JavaSelection.JavaSelectionLocation.METHOD_BODY;
import static org.eclipse.recommenders.rcp.events.JavaSelection.JavaSelectionLocation.METHOD_DECLARATION;
import static org.eclipse.recommenders.rcp.events.JavaSelection.JavaSelectionLocation.METHOD_DECLARATION_THROWS;
import static org.eclipse.recommenders.rcp.events.JavaSelection.JavaSelectionLocation.TYPE_DECLARATION;
import static org.eclipse.recommenders.rcp.events.JavaSelection.JavaSelectionLocation.TYPE_DECLARATION_EXTENDS;
import static org.eclipse.recommenders.rcp.events.JavaSelection.JavaSelectionLocation.TYPE_DECLARATION_IMPLEMENTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.rcp.events.JavaSelection;
import org.eclipse.recommenders.rcp.events.JavaSelection.JavaSelectionLocation;

public class JavaSelectionTestUtils {

    public static final JavaSelection TYPE_IN_TYPE_DECLARATION = mockJavaSelection(IType.class, TYPE_DECLARATION);
    public static final JavaSelection TYPE_IN_TYPE_DECLARATION_EXTENDS = mockJavaSelection(IType.class,
            TYPE_DECLARATION_EXTENDS);
    public static final JavaSelection TYPE_IN_TYPE_DECLARATION_IMPLEMENTS = mockJavaSelection(IType.class,
            TYPE_DECLARATION_IMPLEMENTS);
    public static final JavaSelection TYPE_IN_METHOD_BODY = mockJavaSelection(IType.class, METHOD_BODY);
    public static final JavaSelection TYPE_IN_METHOD_DECLARATION_THROWS = mockJavaSelection(IType.class,
            METHOD_DECLARATION_THROWS);
    public static final JavaSelection TYPE_IN_METHOD_DECLARATION_PARAMS = mockJavaSelection(IType.class,
            JavaSelectionLocation.METHOD_DECLARATION_PARAMETER);
    public static final JavaSelection ANNOTATION_IN_METHOD_DECLARATION = mockJavaSelection(IAnnotation.class,
            METHOD_DECLARATION);
    public static final JavaSelection METHOD_IN_METHOD_DECLARATION = mockJavaSelection(IMethod.class,
            METHOD_DECLARATION);
    public static final JavaSelection METHOD_IN_METHOD_BODY = mockJavaSelection(IMethod.class, METHOD_BODY);

    public static JavaSelection mockJavaSelection(final Class<? extends IJavaElement> clazz,
            final JavaSelectionLocation location) {
        final IJavaElement element = mock(clazz);
        final JavaSelection selection = mock(JavaSelection.class);
        when(selection.getElement()).thenReturn(element);
        when(selection.getLocation()).thenReturn(location);
        return selection;
    }

}