package org.eclipse.recommenders.tests.rcp.models;

import static com.google.common.base.Optional.of;
import static org.apache.commons.lang3.SystemUtils.getJavaHome;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.internal.rcp.models.store.ModelArchiveResolutionJob;
import org.eclipse.recommenders.rcp.ClasspathEntryInfo;
import org.eclipse.recommenders.rcp.IClasspathEntryInfoProvider;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.eclipse.recommenders.utils.Version;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class ModelArchiveResolutionJobTest {

    Optional<Artifact> NO_ARTIFACT = Optional.<Artifact> absent();

    String CLASSIFIER = "class";
    @Mock
    File location;

    ClasspathEntryInfo info = ClasspathEntryInfo.create("symbolic", Version.create(1, 1), "sha1", location);

    Artifact MATCH = new DefaultArtifact("match:match:0.0.0");
    ModelArchiveMetadata metadata;

    @Mock
    IClasspathEntryInfoProvider cpeInfoProvider;
    @Mock
    IModelRepository repository;
    @Mock
    IModelRepositoryIndex index;

    @Mock
    IProgressMonitor monitor;

    ModelArchiveResolutionJob sut;

    @Before
    public void before() throws DependencyResolutionException {

        when(location.getName()).thenReturn("some.file");
        metadata = new ModelArchiveMetadata();
        metadata.setLocation(location);

        sut = new ModelArchiveResolutionJob(metadata, cpeInfoProvider, repository, index, CLASSIFIER);

        // define the default behavior:

        // repository's find methods: no match at all
        when(cpeInfoProvider.getInfo(Mockito.<File> any())).thenReturn(Optional.<ClasspathEntryInfo> absent());
        when(repository.findHigestVersion(anyArtifact())).thenReturn(NO_ARTIFACT);
        when(repository.findLowestVersion(anyArtifact())).thenReturn(NO_ARTIFACT);

        // but if it's match, the return some existing file (don't care which one exactly)
        when(repository.resolve(MATCH, monitor)).thenReturn(getJavaHome());

        // search by default returns absent() only:
        when(index.searchByArtifactId(anyString(), anyString())).thenReturn(NO_ARTIFACT);
        when(index.searchByFingerprint(anyString(), anyString())).thenReturn(NO_ARTIFACT);
    }

    private Artifact anyArtifact() {
        return Mockito.<Artifact> any();
    }

    private String anyString() {
        return Mockito.<String> any();
    }

    @Test
    public void testIncompletionInfo() {
        assertFalse(sut.run(monitor).isOK());
        when(cpeInfoProvider.getInfo(location)).thenReturn(of(info));
        info.setFingerprint("sha");
        info.setSymbolicName("someid");
        sut.run(monitor);
        assertNull(metadata.getCoordinate());
    }

    @Test
    public void testMatchJarByFingerprint() {
        when(cpeInfoProvider.getInfo(location)).thenReturn(of(info));
        when(index.searchByFingerprint("sha1", CLASSIFIER)).thenReturn(of(MATCH));

        sut.run(monitor);

        assertEquals(MATCH.toString(), metadata.getCoordinate());
    }

    @Test
    public void testMatchJarByFingerprintUnknownVersion() {
        when(cpeInfoProvider.getInfo(location)).thenReturn(of(info));
        when(index.searchByFingerprint("sha1", CLASSIFIER)).thenReturn(of(MATCH));
        info.setVersion(Version.UNKNOWN);

        sut.run(monitor);
        assertEquals(MATCH.toString(), metadata.getCoordinate());
    }

    @Test
    public void testMatchJarBySymbolicName() {
        when(cpeInfoProvider.getInfo(location)).thenReturn(of(info));
        when(index.searchByFingerprint(info.getFingerprint(), CLASSIFIER)).thenReturn(NO_ARTIFACT);
        when(index.searchByArtifactId(info.getSymbolicName(), CLASSIFIER)).thenReturn(of(MATCH));

        sut.run(monitor);

        assertEquals(MATCH.toString(), metadata.getCoordinate());
    }

    @Test
    public void testMatchSrcBySymbolicName() {
        info.setFingerprint(null);
        when(cpeInfoProvider.getInfo(location)).thenReturn(of(info));
        when(index.searchByArtifactId(info.getSymbolicName(), CLASSIFIER)).thenReturn(of(MATCH));

        sut.run(monitor);

        assertEquals(MATCH.toString(), metadata.getCoordinate());
    }

}
