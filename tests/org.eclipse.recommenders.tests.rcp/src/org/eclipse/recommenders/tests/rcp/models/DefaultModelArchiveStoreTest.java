package org.eclipse.recommenders.tests.rcp.models;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.internal.rcp.models.IModelArchive;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata.ModelArchiveResolutionStatus;
import org.eclipse.recommenders.internal.rcp.models.store.DefaultModelArchiveStore;
import org.eclipse.recommenders.internal.rcp.models.store.IDependenciesFactory;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class DefaultModelArchiveStoreTest {

    @Mock
    File state;

    @Mock
    IModelRepository repository;

    @Mock
    IDependenciesFactory factory;

    @Mock
    IModelArchive<IType, Object> archive;

    @Mock
    IType type;

    @Mock
    IPackageFragmentRoot root;

    @Mock
    File location;

    private DefaultModelArchiveStore<IType, Object> sut;
    private ModelArchiveMetadata<IType, Object> meta;

    @Before
    public void before() throws IOException {
        when(type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT)).thenReturn(root);

        when(location.exists()).thenReturn(true);

        when(factory.newModelArchive(location)).thenReturn(archive);

        when(archive.hasModel(type)).thenReturn(true);
        when(archive.acquireModel(type)).thenReturn(Optional.of(new Object()));

        sut = new DefaultModelArchiveStore<IType, Object>(state, "some", repository, factory) {
            @Override
            protected com.google.common.base.Optional<File> getLocation(IPackageFragmentRoot root) {
                return of(location);
            };
        };
        meta = sut.findOrCreateMetadata(location);
        meta.setModelArchive(archive);
    }

    @Test
    public void testHappyPath() {
        meta.setStatus(ModelArchiveResolutionStatus.RESOLVED);
        meta.setCoordinate("some:some:2.0.0");

        Optional<Object> model = sut.aquireModel(type);
        assertTrue(model.isPresent());
        sut.releaseModel(model.get());
    }

    @Test
    public void testResolutionSkippedIfAlreadyTriggeredBefore() {
        meta.setStatus(ModelArchiveResolutionStatus.UNRESOLVED);
        meta.setCoordinate("some:some:2.0.0");
        meta.setResolutionRequestedSinceStartup(true);
        sut.aquireModel(type);

        verify(factory, times(0)).newResolutionJob(any(ModelArchiveMetadata.class), anyString());
    }

    @Test
    public void testResolutionSkippedIfTriggeredSecondTime() {
        meta.setStatus(ModelArchiveResolutionStatus.UNRESOLVED);
        meta.setCoordinate("some:some:2.0.0");
        sut.aquireModel(type);
        sut.aquireModel(type);
        verify(factory, times(1)).newResolutionJob(any(ModelArchiveMetadata.class), anyString());

    }

    @Test
    public void testDoLoadModel() {
        when(archive.hasModel(type)).thenReturn(true);
        when(archive.acquireModel(type)).thenReturn(Optional.of(new Object()));
        when(repository.location(Mockito.<Artifact> any())).thenReturn(location);
        meta.setStatus(ModelArchiveResolutionStatus.RESOLVED);
        meta.setCoordinate("some:some:2.0.0");
        meta.setModelArchive(null);

        Optional<Object> model = sut.aquireModel(type);
        assertTrue(model.isPresent());
        sut.releaseModel(model.get());
    }

    @Test
    public void testNoCoordinate() {
        meta.setStatus(ModelArchiveResolutionStatus.RESOLVED);
        // meta.setCoordinate("some:some:2.0.0");

        assertEquals(absent(), sut.aquireModel(type));
    }

    @Test
    public void testNoModel() {
        meta.setStatus(ModelArchiveResolutionStatus.RESOLVED);
        meta.setCoordinate("some:some:2.0.0");
        when(archive.hasModel(type)).thenReturn(false);
        assertEquals(absent(), sut.aquireModel(type));
    }

    @Test
    public void testProhibitedResolutionStatus() {
        meta.setStatus(ModelArchiveResolutionStatus.PROHIBITED);
        meta.setCoordinate("some:some:2.0.0");

        assertEquals(absent(), sut.aquireModel(type));
    }

    @Test
    public void testMetadata() throws IOException {
        sut.getMetadata();
    }
}
