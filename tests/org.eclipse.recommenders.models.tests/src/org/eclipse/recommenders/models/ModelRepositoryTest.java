package org.eclipse.recommenders.models;

import static com.google.common.base.Charsets.UTF_8;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.eclipse.recommenders.utils.Fingerprints.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.GetTask;
import org.eclipse.aether.spi.connector.transport.Transporter;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

public class ModelRepositoryTest {

    private static ModelCoordinate COORDINATE = new ModelCoordinate("org.example", "example", "model", "zip", "1.0.0");

    private static final URI METADATA_XML = asUri("org/example/example/1.0.0-SNAPSHOT/maven-metadata.xml");
    private static final URI METADATA_XML_SHA1 = asUri("org/example/example/1.0.0-SNAPSHOT/maven-metadata.xml.sha1");
    private static final URI METADATA_XML_MD5 = asUri("org/example/example/1.0.0-SNAPSHOT/maven-metadata.xml.md5");
    private static final URI EXAMPLE_MODEL_ZIP = asUri("org/example/example/1.0.0-SNAPSHOT/example-1.0.0-20140625.000000-1-model.zip");
    private static final URI EXAMPLE_MODEL_FALLBACK_ZIP = asUri("org/example/example/1.0.0-SNAPSHOT/example-1.0.0-SNAPSHOT-model.zip");
    private static final URI EXAMPLE_MODEL_ZIP_SHA1 = asUri("org/example/example/1.0.0-SNAPSHOT/example-1.0.0-20140625.000000-1-model.zip.sha1");
    private static final URI EXAMPLE_MODEL_ZIP_MD5 = asUri("org/example/example/1.0.0-SNAPSHOT/example-1.0.0-20140625.000000-1-model.zip.md5");

    private static final String REPO_URL = "http://www.example.org/repo";

    private static final String[] CHECKSUM_EXTENSIONS = new String[] { "sha1", "md5" };

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testGetLocationOnEmptyLocalRepository() throws Exception {
        Transporter transporter = mockTransporter(Collections.<URI, Answer<Void>>emptyMap());
        RepositorySystem system = createRepositorySystem(transporter);
        IModelRepository sut = new ModelRepository(system, tmp.getRoot(), REPO_URL);

        Optional<File> resolvedModel = sut.getLocation(COORDINATE, false);

        assertThat(resolvedModel.isPresent(), is(false));
        verify(transporter, never()).get(Mockito.any(GetTask.class));
    }

    @Test
    public void testResolveSucceeds() throws Exception {
        final String metadata = mavenMetadata(COORDINATE);
        final String model = "model data";

        /* @formatter:off */
        Transporter transporter = mockTransporter(ImmutableMap.of(
                METADATA_XML, new SucessfulDownload(metadata),
                METADATA_XML_SHA1, new SucessfulDownload(sha1(metadata)),
                EXAMPLE_MODEL_ZIP, new SucessfulDownload(model),
                EXAMPLE_MODEL_ZIP_SHA1, new SucessfulDownload(sha1(model))));
        /* @formatter:on */

        RepositorySystem system = createRepositorySystem(transporter);
        IModelRepository sut = new ModelRepository(system, tmp.getRoot(), REPO_URL);

        Optional<File> resolvedModel = sut.resolve(COORDINATE, false);

        assertThat(Files.toString(resolvedModel.get(), UTF_8), is(equalTo("model data")));
        verify(transporter).get(argThat(hasLocation(equalTo(METADATA_XML))));
        verify(transporter).get(argThat(hasLocation(equalTo(METADATA_XML_SHA1))));
        verify(transporter).get(argThat(hasLocation(equalTo(EXAMPLE_MODEL_ZIP))));
        verify(transporter).get(argThat(hasLocation(equalTo(EXAMPLE_MODEL_ZIP_SHA1))));
        verify(transporter, times(4)).get(Mockito.any(GetTask.class));

        assertThat(listFiles(tmp.getRoot(), CHECKSUM_EXTENSIONS, true).size(), is(0));
    }

    @Test
    public void testResolveSucceedsWithoutSha1Checksums() throws Exception {
        final String metadata = mavenMetadata(COORDINATE);
        final String model = "model data";

        /* @formatter:off */
        Transporter transporter = mockTransporter(ImmutableMap.of(
                METADATA_XML, new SucessfulDownload(metadata),
                METADATA_XML_MD5, new SucessfulDownload(md5(metadata)),
                EXAMPLE_MODEL_ZIP, new SucessfulDownload(model),
                EXAMPLE_MODEL_ZIP_MD5, new SucessfulDownload(md5(model))));
        /* @formatter:on */

        RepositorySystem system = createRepositorySystem(transporter);
        IModelRepository sut = new ModelRepository(system, tmp.getRoot(), REPO_URL);

        Optional<File> resolvedModel = sut.resolve(COORDINATE, false);

        assertThat(Files.toString(resolvedModel.get(), UTF_8), is(equalTo("model data")));
        verify(transporter).get(argThat(hasLocation(equalTo(METADATA_XML))));
        verify(transporter).get(argThat(hasLocation(equalTo(METADATA_XML_SHA1))));
        verify(transporter).get(argThat(hasLocation(equalTo(METADATA_XML_MD5))));
        verify(transporter).get(argThat(hasLocation(equalTo(EXAMPLE_MODEL_ZIP))));
        verify(transporter).get(argThat(hasLocation(equalTo(EXAMPLE_MODEL_ZIP_SHA1))));
        verify(transporter).get(argThat(hasLocation(equalTo(EXAMPLE_MODEL_ZIP_MD5))));
        verify(transporter, times(6)).get(Mockito.any(GetTask.class));

        assertThat(listFiles(tmp.getRoot(), CHECKSUM_EXTENSIONS, true).size(), is(0));
    }

    @Test
    public void testGetLocationSucceedsAfterSuccessfulResolve() throws Exception {
        final String metadata = mavenMetadata(COORDINATE);
        final String model = "model data";

        /* @formatter:off */
        Transporter transporter = mockTransporter(ImmutableMap.of(
                METADATA_XML, new SucessfulDownload(metadata),
                METADATA_XML_SHA1, new SucessfulDownload(sha1(metadata)),
                EXAMPLE_MODEL_ZIP, new SucessfulDownload(model),
                EXAMPLE_MODEL_ZIP_SHA1, new SucessfulDownload(sha1(model))));
        /* @formatter:on */

        RepositorySystem system = createRepositorySystem(transporter);
        IModelRepository sut = new ModelRepository(system, tmp.getRoot(), REPO_URL);

        Optional<File> resolvedModel = sut.resolve(COORDINATE, false);
        Optional<File> cachedModel = sut.getLocation(COORDINATE, false);

        assertThat(cachedModel, is(equalTo(resolvedModel)));
        assertThat(Files.toString(cachedModel.get(), UTF_8), is(equalTo("model data")));
    }

    @Test
    public void testGetLocationDoesNotBlockDuringResolve() throws Exception {
        final String metadata = mavenMetadata(COORDINATE);
        final String model = "model data";

        final CountDownLatch startDownload = new CountDownLatch(1);
        final CountDownLatch finishDownload = new CountDownLatch(1);

        /* @formatter:off */
        Transporter transporter = mockTransporter(ImmutableMap.of(
                METADATA_XML, new SucessfulDownload(metadata),
                METADATA_XML_SHA1, new SucessfulDownload(sha1(metadata)),
                EXAMPLE_MODEL_ZIP, new LongRunningDownload(model, startDownload, finishDownload),
                EXAMPLE_MODEL_ZIP_SHA1, new SucessfulDownload(sha1(model))));
        /* @formatter:on */

        RepositorySystem system = createRepositorySystem(transporter);
        final IModelRepository sut = new ModelRepository(system, tmp.getRoot(), REPO_URL);
        ExecutorService executors = Executors.newFixedThreadPool(2);
        Future<Optional<File>> resolvedModel = executors.submit(new Callable<Optional<File>>() {
            @Override
            public Optional<File> call() {
                return sut.resolve(COORDINATE, false);
            };
        });
        Future<Optional<File>> cachedModel = executors.submit(new Callable<Optional<File>>() {
            @Override
            public Optional<File> call() throws InterruptedException {
                startDownload.await();
                Optional<File> preResolveLocation = sut.getLocation(COORDINATE, false);
                finishDownload.countDown();
                return preResolveLocation;
            };
        });

        assertThat(resolvedModel.get().isPresent(), is(true));
        assertThat(cachedModel.get().isPresent(), is(false));
    }

    @Test
    public void testResolveFailsOnChecksumMismatchForMetadata() throws Exception {
        final String metadata = mavenMetadata(COORDINATE);
        final String model = "model data";

        /* @formatter:off */
        Transporter transporter = mockTransporter(ImmutableMap.of(
                METADATA_XML, new SucessfulDownload(metadata),
                METADATA_XML_SHA1, new SucessfulDownload("0000000000000000000000000000000000000000"),
                EXAMPLE_MODEL_ZIP, new SucessfulDownload(model),
                EXAMPLE_MODEL_ZIP_SHA1, new SucessfulDownload(sha1(model))));
        /* @formatter:on */

        RepositorySystem system = createRepositorySystem(transporter);
        IModelRepository sut = new ModelRepository(system, tmp.getRoot(), REPO_URL);

        Optional<File> resolvedModel = sut.resolve(COORDINATE, false);

        assertThat(resolvedModel.isPresent(), is(false));
        // Aether retries download once on checksum mismatch
        verify(transporter, times(2)).get(argThat(hasLocation(equalTo(METADATA_XML))));
        verify(transporter, times(2)).get(argThat(hasLocation(equalTo(METADATA_XML_SHA1))));
        // Aether falls back on literal "SNAPSHOT" URI
        verify(transporter).get(argThat(hasLocation(equalTo(EXAMPLE_MODEL_FALLBACK_ZIP))));
        verify(transporter, times(5)).get(Mockito.any(GetTask.class));
    }

    @Test
    public void testResolveFailsOnMissingChecksumForMetadata() throws Exception {
        final String metadata = mavenMetadata(COORDINATE);
        final String model = "model data";

        /* @formatter:off */
        Transporter transporter = mockTransporter(ImmutableMap.of(
                METADATA_XML, new SucessfulDownload(metadata),
                EXAMPLE_MODEL_ZIP, new SucessfulDownload(model),
                EXAMPLE_MODEL_ZIP_SHA1, new SucessfulDownload(sha1(model))));
        /* @formatter:on */

        RepositorySystem system = createRepositorySystem(transporter);
        IModelRepository sut = new ModelRepository(system, tmp.getRoot(), REPO_URL);

        Optional<File> resolvedModel = sut.resolve(COORDINATE, false);

        assertThat(resolvedModel.isPresent(), is(false));
        verify(transporter).get(argThat(hasLocation(equalTo(METADATA_XML))));
        verify(transporter).get(argThat(hasLocation(equalTo(METADATA_XML_SHA1))));
        verify(transporter).get(argThat(hasLocation(equalTo(METADATA_XML_MD5))));
        // Aether falls back on literal "SNAPSHOT" URI
        verify(transporter).get(argThat(hasLocation(equalTo(EXAMPLE_MODEL_FALLBACK_ZIP))));
        verify(transporter, times(4)).get(Mockito.any(GetTask.class));
    }

    @Test
    public void testResolveFailsOnChecksumMismatchForModel() throws Exception {
        final String metadata = mavenMetadata(COORDINATE);
        final String model = "model data";

        /* @formatter:off */
        Transporter transporter = mockTransporter(ImmutableMap.of(
                METADATA_XML, new SucessfulDownload(metadata),
                METADATA_XML_SHA1, new SucessfulDownload(sha1(metadata)),
                EXAMPLE_MODEL_ZIP, new SucessfulDownload(model),
                EXAMPLE_MODEL_ZIP_SHA1, new SucessfulDownload("0000000000000000000000000000000000000000")));
        /* @formatter:on */

        RepositorySystem system = createRepositorySystem(transporter);
        IModelRepository sut = new ModelRepository(system, tmp.getRoot(), REPO_URL);

        Optional<File> resolvedModel = sut.resolve(COORDINATE, false);

        assertThat(resolvedModel.isPresent(), is(false));
        verify(transporter).get(argThat(hasLocation(equalTo(METADATA_XML))));
        verify(transporter).get(argThat(hasLocation(equalTo(METADATA_XML_SHA1))));
        // Aether retries download once on checksum mismatch
        verify(transporter, times(2)).get(argThat(hasLocation(equalTo(EXAMPLE_MODEL_ZIP))));
        verify(transporter, times(2)).get(argThat(hasLocation(equalTo(EXAMPLE_MODEL_ZIP_SHA1))));
        verify(transporter, times(6)).get(Mockito.any(GetTask.class));
    }

    @Test
    public void testResolveFailsOnMissingChecksumForModel() throws Exception {
        final String metadata = mavenMetadata(COORDINATE);
        final String model = "model data";

        /* @formatter:off */
        Transporter transporter = mockTransporter(ImmutableMap.of(
                METADATA_XML, new SucessfulDownload(metadata),
                METADATA_XML_SHA1, new SucessfulDownload(sha1(metadata)),
                EXAMPLE_MODEL_ZIP, new SucessfulDownload(model)));
        /* @formatter:on */

        RepositorySystem system = createRepositorySystem(transporter);
        IModelRepository sut = new ModelRepository(system, tmp.getRoot(), REPO_URL);

        Optional<File> resolvedModel = sut.resolve(COORDINATE, false);

        assertThat(resolvedModel.isPresent(), is(false));
        verify(transporter).get(argThat(hasLocation(equalTo(METADATA_XML))));
        verify(transporter).get(argThat(hasLocation(equalTo(METADATA_XML_SHA1))));
        verify(transporter).get(argThat(hasLocation(equalTo(EXAMPLE_MODEL_ZIP))));
        verify(transporter).get(argThat(hasLocation(equalTo(EXAMPLE_MODEL_ZIP_SHA1))));
        verify(transporter).get(argThat(hasLocation(equalTo(EXAMPLE_MODEL_ZIP_MD5))));
        verify(transporter, times(5)).get(Mockito.any(GetTask.class));
    }

    @Test
    public void testResolveFailsOnConnectionFailureOnModel() throws Exception {
        final String metadata = mavenMetadata(COORDINATE);
        final String model = "model data";

        /* @formatter:off */
        Transporter transporter = mockTransporter(ImmutableMap.of(
                METADATA_XML, new SucessfulDownload(metadata),
                METADATA_XML_SHA1, new SucessfulDownload(sha1(metadata)),
                EXAMPLE_MODEL_ZIP, new ConnectionFailure(),
                EXAMPLE_MODEL_ZIP_SHA1, new SucessfulDownload(sha1(model))));
        /* @formatter:on */

        RepositorySystem system = createRepositorySystem(transporter);
        IModelRepository sut = new ModelRepository(system, tmp.getRoot(), REPO_URL);

        Optional<File> resolvedModel = sut.resolve(COORDINATE, false);

        assertThat(resolvedModel.isPresent(), is(false));
        verify(transporter).get(argThat(hasLocation(equalTo(METADATA_XML))));
        verify(transporter).get(argThat(hasLocation(equalTo(METADATA_XML_SHA1))));
        verify(transporter).get(argThat(hasLocation(equalTo(EXAMPLE_MODEL_ZIP))));
        verify(transporter, times(3)).get(Mockito.any(GetTask.class));

        Optional<File> reresolvedModel = sut.resolve(COORDINATE, false);

        assertThat(reresolvedModel.isPresent(), is(false));
        // Aether makes no new request but caches negative results...
        verify(transporter, times(3)).get(Mockito.any(GetTask.class));

        Optional<File> forcedModel = sut.resolve(COORDINATE, true);

        assertThat(forcedModel.isPresent(), is(false));
        // ...unless forced
        verify(transporter, times(1 + 1)).get(argThat(hasLocation(equalTo(METADATA_XML))));
        verify(transporter, times(1 + 1)).get(argThat(hasLocation(equalTo(METADATA_XML_SHA1))));
        verify(transporter, times(1 + 1)).get(argThat(hasLocation(equalTo(EXAMPLE_MODEL_ZIP))));
        verify(transporter, times(3 + 3)).get(Mockito.any(GetTask.class));
    }

    private String mavenMetadata(ModelCoordinate coordinate) {
        /* @formatter:off */
        final String metadata = String.format("<?xml version='1.0' encoding='UTF-8'?>"
                + "<metadata modelVersion='1.1.0'>"
                + "<groupId>%1$s</groupId>"
                + "<artifactId>%2$s</artifactId>"
                + "<version>%3$s-SNAPSHOT</version>"
                + "<versioning>"
                + "<snapshot>"
                + "<timestamp>20140625.000000</timestamp>"
                + "<buildNumber>1</buildNumber>"
                + "</snapshot>"
                + "<lastUpdated>20140625000000</lastUpdated>"
                + "<snapshotVersions>"
                + "<snapshotVersion>"
                + "<classifier>%4$s</classifier>"
                + "<extension>%5$s</extension>"
                + "<value>%3$s-20140625.000000-1</value>"
                + "<updated>20140625000000</updated>"
                + "</snapshotVersion>"
                + "</snapshotVersions>"
                + "</versioning>"
                + "</metadata>",
                coordinate.getGroupId(), coordinate.getArtifactId(), coordinate.getVersion(), coordinate.getClassifier(),
                coordinate.getExtension());
        /* @formatter:on */
        return metadata;
    }

    private Transporter mockTransporter(Map<URI, ? extends Answer<Void>> resources) throws Exception {
        Transporter transporter = mock(Transporter.class);

        when(transporter.classify(Mockito.any(FileNotFoundException.class))).thenReturn(Transporter.ERROR_NOT_FOUND);

        doAnswer(new MissingResource()).when(transporter).get(Mockito.any(GetTask.class));
        for (Entry<URI, ? extends Answer<Void>> resource : resources.entrySet()) {
            doAnswer(resource.getValue()).when(transporter).get(argThat(hasLocation(equalTo(resource.getKey()))));
        }

        return transporter;
    }

    private static RepositorySystem createRepositorySystem(Transporter transporter) throws Exception {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();

        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        TransporterFactory transporterFactory = mock(TransporterFactory.class);
        when(
                transporterFactory.newInstance(Mockito.any(RepositorySystemSession.class),
                        Mockito.any(RemoteRepository.class))).thenReturn(transporter);
        locator.setServices(TransporterFactory.class, transporterFactory);

        return locator.getService(RepositorySystem.class);
    }

    private static class SucessfulDownload implements Answer<Void> {

        private final String contents;

        public SucessfulDownload(String contents) {
            this.contents = contents;
        }

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            GetTask task = (GetTask) invocation.getArguments()[0];
            OutputStream out = task.newOutputStream();
            byte[] bytes = contents.getBytes(UTF_8);
            task.getListener().transportStarted(0, bytes.length);
            out.write(bytes);
            task.getListener().transportProgressed(ByteBuffer.wrap(bytes));
            out.close();
            return null;
        }
    }

    private static class LongRunningDownload extends SucessfulDownload {

        private CountDownLatch startDownload;
        private CountDownLatch finishDownload;

        public LongRunningDownload(String contents, CountDownLatch startDownload, CountDownLatch finishDownload) {
            super(contents);
            this.startDownload = startDownload;
            this.finishDownload = finishDownload;
        }

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            startDownload.countDown();
            super.answer(invocation);
            finishDownload.await();
            return null;
        }
    }

    private static class MissingResource implements Answer<Void> {

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            throw new FileNotFoundException();
        }
    }

    private static class ConnectionFailure implements Answer<Void> {

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            throw new IOException();
        }
    }

    private static Matcher<GetTask> hasLocation(Matcher<URI> matcher) {
        return hasProperty("location", matcher);
    }

    private static URI asUri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }
}
