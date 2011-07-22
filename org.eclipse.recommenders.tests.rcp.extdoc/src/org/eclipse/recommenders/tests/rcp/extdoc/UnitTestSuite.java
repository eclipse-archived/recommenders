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
package org.eclipse.recommenders.tests.rcp.extdoc;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.internal.rcp.extdoc.ProviderStoreTest;
import org.eclipse.recommenders.internal.rcp.extdoc.UiManagerTest;
import org.eclipse.recommenders.internal.rcp.extdoc.swt.ExtDocViewTest;
import org.eclipse.recommenders.internal.rcp.extdoc.swt.ProvidersCompositeTest;
import org.eclipse.recommenders.internal.rcp.extdoc.swt.ProvidersTableTest;
import org.eclipse.recommenders.rcp.extdoc.SourceCodeAreaTest;
import org.eclipse.recommenders.rcp.extdoc.features.CommentsDialogTest;
import org.eclipse.recommenders.rcp.extdoc.features.DeleteDialogTest;
import org.eclipse.recommenders.rcp.extdoc.features.FeaturesCompositeTest;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.mockito.Mockito;

@RunWith(Suite.class)
@SuiteClasses({ ProviderStoreTest.class, UiManagerTest.class, ExtDocViewTest.class, ProvidersCompositeTest.class,
        ProvidersTableTest.class, SourceCodeAreaTest.class, CommentsDialogTest.class, DeleteDialogTest.class,
        FeaturesCompositeTest.class })
public final class UnitTestSuite {

    private static IJavaElementSelection selection;
    private static Shell shell;

    public static IJavaElementSelection getSelection() {
        if (selection == null) {
            selection = Mockito.mock(IJavaElementSelection.class);
            final IJavaElement javaElement = Mockito.mock(IJavaElement.class);
            final IEditorPart editorPart = Mockito.mock(IEditorPart.class);

            Mockito.when(selection.getJavaElement()).thenReturn(javaElement);
            Mockito.when(selection.getElementLocation()).thenReturn(JavaElementLocation.METHOD_BODY);
            Mockito.when(selection.getEditor()).thenReturn(editorPart);
        }
        return selection;
    }

    public static Shell getShell() {
        if (shell == null) {
            shell = new MyShell(Display.getDefault());
        }
        return shell;
    }

    private static class MyShell extends Shell {

        MyShell(final Display display) {
            super(display);
        }

        @Override
        protected void checkSubclass() {
        }

    }
}
