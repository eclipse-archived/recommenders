package org.eclipse.recommenders.tests.rcp.providers;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.internal.rcp.providers.JavaSelectionUtils;
import org.eclipse.recommenders.utils.rcp.EclipseLogger;
import org.junit.Test;

public class JavaSelectionUtilsTest {

    @Test
    public void testLogging() throws JavaModelException {
        // setup
        ITypeRoot mock = mock(ITypeRoot.class);
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
}
