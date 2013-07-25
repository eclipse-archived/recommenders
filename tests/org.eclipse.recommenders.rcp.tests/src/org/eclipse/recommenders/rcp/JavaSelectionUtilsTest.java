package org.eclipse.recommenders.rcp;

import static com.google.common.base.Optional.absent;
import static org.eclipse.recommenders.internal.rcp.JavaElementSelections.resolveJavaElementFromEditor;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.recommenders.internal.rcp.JavaElementSelections;
import org.eclipse.ui.IEditorPart;
import org.junit.Test;

public class JavaSelectionUtilsTest {

    @Test
    public void testInvalidSelections01_editor() throws JavaModelException {
        IEditorPart mock = mock(IEditorPart.class);
        assertEquals(absent(), resolveJavaElementFromEditor(mock, new TextSelection(-1, -1)));
        assertEquals(absent(), resolveJavaElementFromEditor(mock, new TextSelection(0, 0)));
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
