package org.eclipse.recommenders.tests.rcp.models;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.zip.ZipFile;

import org.eclipse.recommenders.internal.rcp.models.archive.ZipPoolableModelFactory;
import org.junit.Test;

public class ZipPoolableModelFactoryTest {

    private ZipFile zip;
    private Object MODEL = new Object();
    private String KEY = "key";

    @Test
    public void testZipModelFactory() throws IOException {

        zip = mock(ZipFile.class);
        ZipPoolableModelFactory<String, Object> sut = new ZipPoolableModelFactory<String, Object>(zip) {

            @Override
            public boolean hasModel(String key) {
                return false;
            }

            @Override
            public Object createModel(String key) throws Exception {
                return MODEL;
            }
        };
        sut.open();
        sut.activateModel(KEY, MODEL);
        sut.destroyModel(KEY, MODEL);
        sut.passivateModel(KEY, MODEL);
        sut.validateModel(KEY, MODEL);

        sut.close();
        verify(zip).close();
        doThrow(new IOException()).when(zip).close();
        sut.close();
    }

}
