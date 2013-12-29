package org.eclipse.recommenders.utils;

import static org.eclipse.recommenders.utils.Zips.zip;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
        ITypeName[] types = { VmTypeName.JAVA_LANG_NULL_POINTER_EXCEPTION };
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

    @Rule
    public TemporaryFolder unzippedRootJar = new TemporaryFolder();

    @Rule
    public TemporaryFolder zipped = new TemporaryFolder();

    @Test
    public void testZip() throws IOException {
        new File(unzippedRootJar.newFolder("META-INF"), "MANIFEST.MF").createNewFile();
        new File(unzippedRootJar.newFolder("java", "lang"), "Object.class").createNewFile();

        File rootJar = zipped.newFile("root.jar");
        zip(unzippedRootJar.getRoot(), rootJar);

        ZipFile zipFile = new ZipFile(rootJar);

        assertThat(zipFile.getEntry("META-INF/MANIFEST.MF"), is(notNullValue()));
        assertThat(zipFile.getEntry("java/lang/Object.class"), is(notNullValue()));

        Zips.closeQuietly(zipFile);
    }

}
