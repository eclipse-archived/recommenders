package org.eclipse.recommenders.tests.rcp.repo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.recommenders.internal.rcp.repo.ModelRepository;
import org.eclipse.recommenders.internal.rcp.repo.RepositoryUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.google.common.base.Optional;

public class ModelResolverTests {

    private static ModelRepository sut;

    @BeforeClass
    public static void beforeClass() throws Exception {
        sut = new ModelRepository(RepositoryUtilsTest.LOCAL_M2_REPO, RepositoryUtilsTest.VANDYK_JUNO);
    }

    @Test
    public void testFind() {
        DefaultArtifact SWT = (DefaultArtifact) RepositoryUtils
                .newArtifact("org.eclipse.swt:org.eclipse.swt.cocoa.macosx.x86_64:zip:call:3.0.0");

        SWT = (DefaultArtifact) SWT.setVersion("[3,5)");
        Optional<Artifact> version = sut.findHigestVersion(SWT);
        assertTrue(version.isPresent());
    }

    @Test
    public void testDontFind() {
        DefaultArtifact SWT = (DefaultArtifact) RepositoryUtils
                .newArtifact("org.eclipse.swt:org.eclipse.swt.cocoa.macosx.x86_64:zip:call:3.0.0");
        SWT = (DefaultArtifact) SWT.setVersion("[4,5)");
        Optional<Artifact> version = sut.findHigestVersion(SWT);
        assertFalse(version.isPresent());
    }

}
