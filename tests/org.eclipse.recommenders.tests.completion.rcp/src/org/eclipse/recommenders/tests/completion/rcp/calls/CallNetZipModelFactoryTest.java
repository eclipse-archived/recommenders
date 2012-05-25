package org.eclipse.recommenders.tests.completion.rcp.calls;

import static org.eclipse.recommenders.utils.names.VmTypeName.OBJECT;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.util.zip.ZipFile;

import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.CallNetZipModelFactory;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.junit.Test;
import org.mockito.Mockito;

public class CallNetZipModelFactoryTest {

    @Test
    public void test() throws Exception {
        ZipFile zip = Mockito.mock(ZipFile.class);
        JavaElementResolver resolver = Mockito.mock(JavaElementResolver.class);
        when(resolver.toRecType((IType) Mockito.any())).thenReturn(OBJECT);

        CallNetZipModelFactory sut = new CallNetZipModelFactory(zip, resolver);
        assertFalse(sut.hasModel(null));
    }
}
