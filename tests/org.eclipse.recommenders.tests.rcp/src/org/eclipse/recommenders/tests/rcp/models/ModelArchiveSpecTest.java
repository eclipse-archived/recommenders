package org.eclipse.recommenders.tests.rcp.models;

import static com.google.common.base.Optional.absent;
import static org.eclipse.recommenders.utils.Tuple.newTuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.internal.rcp.models.IModelArchive;
import org.eclipse.recommenders.internal.rcp.models.archive.CachingModelArchive;
import org.eclipse.recommenders.internal.rcp.models.archive.IModelFactory;
import org.eclipse.recommenders.internal.rcp.models.archive.NullModelArchive;
import org.eclipse.recommenders.internal.rcp.models.archive.PoolingModelArchive;
import org.eclipse.recommenders.utils.Tuple;
import org.junit.Before;
import org.junit.Test;

public class ModelArchiveSpecTest {

    String KEY_WITH_MODEL = "key";
    Object MODEL = "model";
    String KEY_NO_MODEL = "key-but-no-model";
    String KEY_UNKNOWN = "unknown";

    Tuple<IModelArchive<String, Object>, IModelFactory>[] suts;

    @Before
    public void before() throws Exception {

        IModelFactory f1 = newModelFactoryMock();
        IModelArchive a1 = new CachingModelArchive(f1);

        IModelFactory f2 = newModelFactoryMock();
        IModelArchive a2 = new PoolingModelArchive(f2);

        suts = new Tuple[] { newTuple(a1, f1), newTuple(a2, f2) };
    }

    @Test
    public void testAquireModel() throws Exception {

        for (Tuple<IModelArchive<String, Object>, ?> p : suts) {
            IModelArchive<String, Object> sut = p.getFirst();
            assertTrue(sut.hasModel(KEY_WITH_MODEL));
            assertTrue(sut.hasModel(KEY_NO_MODEL));
            assertFalse(sut.hasModel(KEY_UNKNOWN));
            assertFalse(sut.hasModel(null));

            assertTrue(sut.acquireModel(KEY_WITH_MODEL).isPresent());
            assertFalse(sut.acquireModel(KEY_NO_MODEL).isPresent());
            assertFalse(sut.acquireModel(KEY_UNKNOWN).isPresent());
            assertFalse(sut.acquireModel(null).isPresent());

            sut.releaseModel(MODEL);
            sut.releaseModel(new Object());
            sut.releaseModel(null);
        }

    }

    @Test
    public void testOpenClose() throws IOException {
        for (Tuple<IModelArchive<String, Object>, IModelFactory> p : suts) {
            IModelArchive sut = p.getFirst();
            IModelFactory f = p.getSecond();
            sut.open();
            sut.close();
            verify(f).close();
            verify(f).open();
        }
    }

    private IModelFactory newModelFactoryMock() throws Exception {
        IModelFactory mock = mock(IModelFactory.class);
        when(mock.hasModel(KEY_WITH_MODEL)).thenReturn(true);
        when(mock.createModel(KEY_WITH_MODEL)).thenReturn(MODEL);
        when(mock.hasModel(KEY_NO_MODEL)).thenReturn(true);
        return mock;
    }

    @Test
    public void testNull() throws IOException {
        IModelArchive<IType, IMethod> sut = NullModelArchive.empty();
        assertEquals(absent(), sut.acquireModel(null));
        assertFalse(sut.hasModel(null));
        sut.close();
        sut.open();
        sut.releaseModel(null);
    }

}
