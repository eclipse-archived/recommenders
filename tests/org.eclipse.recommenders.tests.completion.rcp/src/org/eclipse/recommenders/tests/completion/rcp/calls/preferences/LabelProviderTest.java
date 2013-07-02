package org.eclipse.recommenders.tests.completion.rcp.calls.preferences;

import static com.google.common.base.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.recommenders.internal.completion.rcp.calls.preferences.ModelLabelProvider;
import org.eclipse.recommenders.internal.completion.rcp.calls.preferences.PackageFragmentRootLabelProvider;
import org.eclipse.recommenders.internal.completion.rcp.calls.preferences.VersionLabelProvider;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.utils.Pair;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class LabelProviderTest {

    @Mock
    IPackageFragmentRoot root;
    @Mock
    ModelArchiveMetadata<?, ?> meta;
    @Mock
    Artifact someArtifact;
    @Mock
    IModelRepository repository;

    Pair<IPackageFragmentRoot, ?> data;

    Image IMG_FOUND = new Image(Display.getDefault(), 1, 1);

    Image IMG_NOT_FOUND = new Image(Display.getDefault(), 2, 2);
    File path = new File("a/path/test.jar");

    PackageFragmentRootLabelProvider pkgSut = new PackageFragmentRootLabelProvider();
    VersionLabelProvider versionSut = new VersionLabelProvider(IMG_NOT_FOUND, IMG_FOUND);
    ModelLabelProvider modelSut;

    @Before
    public void before() {
        data = Pair.newPair(root, meta);
        modelSut = new ModelLabelProvider(repository, IMG_FOUND, IMG_NOT_FOUND);
    }

    @Test
    public void testPkgText() {
        when(meta.getLocation()).thenReturn(path);
        assertEquals(path.getName(), pkgSut.getText(data));
    }

    @Test
    public void testPkgTooltip() {
        when(meta.getLocation()).thenReturn(path);
        assertEquals(path.getAbsolutePath(), pkgSut.getToolTipText(data));
    }

    @Test
    public void testModelTooltip01() {
        when(meta.getArtifact()).thenReturn(Optional.<Artifact> absent());
        assertSame(IMG_NOT_FOUND, modelSut.getImage(data));
        assertSame(ModelLabelProvider.MODEL_NOT_AVAILABLE, modelSut.getToolTipText(data));
    }

    @Test
    public void testModelTooltip02() {
        when(meta.getArtifact()).thenReturn(of(someArtifact));
        when(repository.location(someArtifact)).thenReturn(new File("non-exist"));
        assertSame(IMG_NOT_FOUND, modelSut.getImage(data));
        assertSame(ModelLabelProvider.MODEL_NOT_AVAILABLE, modelSut.getToolTipText(data));
    }

    @Test
    public void testModelTooltip03() {
        when(meta.getArtifact()).thenReturn(of(someArtifact));
        when(repository.location(someArtifact)).thenReturn(SystemUtils.getUserDir());
        assertSame(IMG_FOUND, modelSut.getImage(data));
        assertSame(ModelLabelProvider.MODEL_AVAILABLE, modelSut.getToolTipText(data));
    }

}
