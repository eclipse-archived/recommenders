package org.eclipse.recommenders.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Test;

public class ZipsTest {

    @Test
    public void testMethods() {
        IMethodName[] methods = { VmMethodName.NULL,
                VmMethodName.get("Lorg/eclipse.test(LString;ICC[[J)Ljava/lang/String;") };
        for (IMethodName m : methods) {
            String path = Zips.path(m, ".json");
            ZipEntry zip = new ZipEntry(path);
            assertEquals(m, Zips.method(zip, ".json"));
        }
    }

    @Test
    public void testTypes() {
        ITypeName[] types = { VmTypeName.JavaLangNullPointerException };
        for (ITypeName t : types) {
            String path = Zips.path(t, ".json");
            ZipEntry zip = new ZipEntry(path);
            assertEquals(t, Zips.type(zip, ".json"));
        }
    }

    @Test
    public void testAppend() throws IOException {
        ZipOutputStream zos = mock(ZipOutputStream.class);
        Zips.append(zos, "/path.json", "test");
        verify(zos).putNextEntry(any(ZipEntry.class));
        verify(zos).write(any(byte[].class));
        verify(zos).closeEntry();
    }

}
