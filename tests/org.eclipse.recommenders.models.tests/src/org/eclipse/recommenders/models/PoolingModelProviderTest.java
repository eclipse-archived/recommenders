package org.eclipse.recommenders.models;

import static com.google.common.base.Optional.of;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Optional;

public class PoolingModelProviderTest {

    private static File zip;

    @BeforeClass
    public static void beforeClass() throws IOException {
        zip = File.createTempFile("tmp_", ".zip");
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));
        zos.putNextEntry(new ZipEntry("/"));
        zos.closeEntry();
        zos.close();
    }

    PoolingModelProvider<IBasedName<String>, String> sut = new PoolingModelProviderStub(new Mock(), "call");

    @Test
    public void testAquireRelease() {
        Optional<String> last = null;
        for (int i = 0; i < 200; i++) {
            last = sut.acquireModel(new SomeName("name"));
            sut.releaseModel(last.get());
        }
        assertTrue("pool exhausted but returned all models properly", last.isPresent());
    }

    @Test
    public void testNoReleaseExhaustsPool() {

        Optional<String> last = null;
        for (int i = 0; i < 300; i++) {
            last = sut.acquireModel(new SomeName("name"));
        }
        assertFalse("pool did not get exhausted", last.isPresent());
    }

    private final class PoolingModelProviderStub extends PoolingModelProvider<IBasedName<String>, String> {
        private PoolingModelProviderStub(IModelRepository repository, String modelType) {
            super(repository, modelType);
        }

        @Override
        protected Optional<String> loadModel(ZipFile zip, IBasedName<String> key) throws Exception {
            // return a "simple" model
            return of(new String(""));
        }
    }

    private static class Mock implements IModelRepository {

        @Override
        public Optional<File> getLocation(ModelArchiveCoordinate coordinate) {
            return of(zip);
        }

        @Override
        public Optional<ModelArchiveCoordinate> findBestModelArchive(ProjectCoordinate coordinate, String modelType) {
            // always return a model coordinate (for now)
            return of(ModelArchiveCoordinate.UNKNOWN);
        }

        @Override
        public void resolve(ModelArchiveCoordinate model) throws Exception {

        }

        @Override
        public Collection<ModelArchiveCoordinate> listModels(String classifier) {
            return null;
        }
    }

    private final class SomeName implements IBasedName<String> {
        private String name;

        public SomeName(String string) {
            name = string;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ProjectCoordinate getBase() {
            return ProjectCoordinate.UNKNOWN;
        }
    }
}
