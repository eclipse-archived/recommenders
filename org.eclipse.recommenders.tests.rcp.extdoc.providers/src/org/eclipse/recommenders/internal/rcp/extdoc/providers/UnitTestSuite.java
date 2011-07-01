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
package org.eclipse.recommenders.internal.rcp.extdoc.providers;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.ui.IEditorPart;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.mockito.Mockito;

@RunWith(Suite.class)
@SuiteClasses({ ExamplesProviderTest.class, JavadocProviderTest.class, WikiProviderTest.class })
public final class UnitTestSuite {

    private static IJavaElementSelection selection;

    public static IJavaElementSelection getSelection() {
        if (selection == null) {
            selection = Mockito.mock(IJavaElementSelection.class);

            final IJavaElement javaElement = Mockito.mock(IJavaElement.class);
            Mockito.when(javaElement.getElementName()).thenReturn("TestElement");
            Mockito.when(javaElement.getHandleIdentifier()).thenReturn("TestIdentifier");
            final IEditorPart editorPart = Mockito.mock(IEditorPart.class);

            Mockito.when(selection.getJavaElement()).thenReturn(javaElement);
            Mockito.when(selection.getElementLocation()).thenReturn(JavaElementLocation.METHOD_BODY);
            Mockito.when(selection.getEditor()).thenReturn(editorPart);
        }
        return selection;
    }
}
