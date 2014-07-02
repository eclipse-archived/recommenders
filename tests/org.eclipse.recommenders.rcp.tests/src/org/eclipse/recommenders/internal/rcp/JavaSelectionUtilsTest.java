/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp;

import static com.google.common.base.Optional.absent;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorPart;
import org.junit.Test;

public class JavaSelectionUtilsTest {

    @Test
    public void testInvalidSelections01_editor() throws JavaModelException {
        IEditorPart mock = mock(IEditorPart.class);
        assertEquals(absent(), JavaElementSelections.resolveJavaElementFromEditor(mock, new TextSelection(-1, -1)));
        assertEquals(absent(), JavaElementSelections.resolveJavaElementFromEditor(mock, new TextSelection(0, 0)));
    }

    @Test
    public void testInvalidSelections02_withSourceRange() throws JavaModelException {
        ITypeRoot mock = mock(ITypeRoot.class);
        when(mock.getSourceRange()).thenReturn(new SourceRange(0, 100));
        assertEquals(absent(), JavaElementSelections.resolveJavaElementFromTypeRootInEditor(mock, -1));
        assertEquals(absent(), JavaElementSelections.resolveJavaElementFromTypeRootInEditor(mock, 0));
        assertEquals(absent(), JavaElementSelections.resolveJavaElementFromTypeRootInEditor(mock, Integer.MAX_VALUE));
    }

    @Test
    public void testInvalidSelections03_nullSourceRange() throws JavaModelException {
        ITypeRoot mock = mock(ITypeRoot.class);
        assertEquals(absent(), JavaElementSelections.resolveJavaElementFromTypeRootInEditor(mock, -1));
        assertEquals(absent(), JavaElementSelections.resolveJavaElementFromTypeRootInEditor(mock, 0));
        assertEquals(absent(), JavaElementSelections.resolveJavaElementFromTypeRootInEditor(mock, Integer.MAX_VALUE));
    }
}
