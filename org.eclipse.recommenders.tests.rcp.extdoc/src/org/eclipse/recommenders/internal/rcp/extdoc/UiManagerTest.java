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
package org.eclipse.recommenders.internal.rcp.extdoc;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.extdoc.swt.ExtDocView;
import org.eclipse.ui.IEditorPart;

import org.junit.Test;
import org.mockito.Mockito;

public final class UiManagerTest {

    @Test
    public void testSelectionChanged() {
        final ExtDocView view = Mockito.mock(ExtDocView.class);

        final IJavaElementSelection selection = Mockito.mock(IJavaElementSelection.class);
        final IJavaElement javaElement = Mockito.mock(IJavaElement.class);
        final IEditorPart editorPart = Mockito.mock(IEditorPart.class);
        Mockito.when(selection.getJavaElement()).thenReturn(javaElement);
        Mockito.when(selection.getEditor()).thenReturn(editorPart);

        final UiManager manager = new UiManager(view);
        manager.selectionChanged(selection);
        manager.selectionChanged(selection);

        Mockito.verify(view, Mockito.times(1)).selectionChanged(Mockito.any(IJavaElementSelection.class));
    }
}
