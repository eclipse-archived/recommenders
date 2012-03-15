package org.eclipse.recommenders.tests.rcp.providers;

import static com.google.common.base.Optional.absent;
import static org.eclipse.recommenders.internal.rcp.providers.JavaSelectionUtils.resolveJavaElementFromEditor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.recommenders.internal.rcp.providers.JavaSelectionUtils;
import org.eclipse.recommenders.utils.rcp.EclipseLogger;
import org.eclipse.ui.IEditorPart;
import org.junit.Test;

public class JavaSelectionUtilsTest {

    @Test
    public void testLogging() throws JavaModelException {
        // setup
        ITypeRoot mock = mock(ITypeRoot.class);
        when(mock.getSourceRange()).thenReturn(new SourceRange(0, 100));
        String expectedHandleName = "myhandle";
        when(mock.getHandleIdentifier()).thenReturn(expectedHandleName);
        JavaModelException x = new JavaModelException(new RuntimeException(), 0);
        doThrow(x).when(mock).codeSelect(anyInt(), anyInt());

        final AtomicReference<String> res = new AtomicReference<String>();
        JavaSelectionUtils.log = new EclipseLogger() {
            @Override
            public void error(String msg, Throwable t) {
                res.set(msg);
            }
        };
        //
        // exercise
        JavaSelectionUtils.resolveJavaElementFromTypeRootInEditor(mock, 0);
        // verify path in log message
        assertTrue("no handle in message", res.get().contains(expectedHandleName));
    }

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
        assertEquals(absent(), JavaSelectionUtils.resolveJavaElementFromTypeRootInEditor(mock, -1));
        assertEquals(absent(), JavaSelectionUtils.resolveJavaElementFromTypeRootInEditor(mock, 0));
        assertEquals(absent(), JavaSelectionUtils.resolveJavaElementFromTypeRootInEditor(mock, Integer.MAX_VALUE));
    }

    @Test
    public void testInvalidSelections03_nullSourceRange() throws JavaModelException {
        ITypeRoot mock = mock(ITypeRoot.class);
        assertEquals(absent(), JavaSelectionUtils.resolveJavaElementFromTypeRootInEditor(mock, -1));
        assertEquals(absent(), JavaSelectionUtils.resolveJavaElementFromTypeRootInEditor(mock, 0));
        assertEquals(absent(), JavaSelectionUtils.resolveJavaElementFromTypeRootInEditor(mock, Integer.MAX_VALUE));
    }

}
