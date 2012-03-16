package org.eclipse.recommenders.tests.rcp.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.recommenders.internal.rcp.repo.ModelRepository;
import org.eclipse.recommenders.internal.rcp.repo.RepositoryUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.google.common.io.Files;

public class RepositoryTests {

    private static ModelRepository sut;

    public static Artifact SWT_37_CALLS = new DefaultArtifact("org.eclipse.swt", "org.eclipse.swt", "cr-calls", "zip",
            "3.7.0");

    @BeforeClass
    public static void beforeClass() throws Exception {
        sut = new ModelRepository(Files.createTempDir(), "http://vandyk.st.informatik.tu-darmstadt.de/maven");
    }

    @Test
    public void utilsAsCoord() {
        String expected = "org.eclipse.swt:org.eclipse.swt:zip:cr-calls:3.7.0";
        String actual = RepositoryUtils.asCoordinate(SWT_37_CALLS);
        assertEquals(expected, actual);
    }

    @Test
    public void utilsToArtifactFileName() {
        String actual = RepositoryUtils.toArtifactFileName(SWT_37_CALLS);
        assertEquals("org.eclipse.swt-3.7.0-cr-calls.zip", actual);
    }

    @Test
    public void utilsNewArtifact() {
        Artifact actual = RepositoryUtils.newClassifierAndExtension(RepositoryUtils.pom(SWT_37_CALLS),
                SWT_37_CALLS.getClassifier(), SWT_37_CALLS.getExtension());
        assertEquals(SWT_37_CALLS, actual);
        
        actual = RepositoryUtils.newArtifact(SWT_37_CALLS.toString());
        assertEquals(SWT_37_CALLS, actual);
    }

    @Test
    public void utilsPom() {
        Artifact expected = new DefaultArtifact("org.eclipse.swt:org.eclipse.swt:pom:3.7.0");
        Artifact actual = RepositoryUtils.pom(SWT_37_CALLS);
        assertEquals(expected, actual);
    }

    @Test
    public void utilsGid() {
        String expected = "org.eclipse.recommenders";
        String actual = RepositoryUtils.guessGroupId("org.eclipse.recommenders.extdoc.rcp");
        assertEquals(expected, actual);

        expected = "org";
        actual = RepositoryUtils.guessGroupId("org");
        assertEquals(expected, actual);

        expected = "org.eclipse";
        actual = RepositoryUtils.guessGroupId("org.eclipse");
        assertEquals(expected, actual);

        expected = "name.bruch";
        actual = RepositoryUtils.guessGroupId("name.bruch");
        assertEquals(expected, actual);

        expected = "myname";
        actual = RepositoryUtils.guessGroupId("myname.bruch");
        assertEquals(expected, actual);

    }

    @Test
    public void repoPhases() throws Exception {
        // check not available on startup
        File location = sut.location(SWT_37_CALLS);
        assertFalse(location.exists());

        // no model - it can't be the latest
        assertFalse(sut.isLatest(SWT_37_CALLS));

        // check resolving works
        File file = sut.resolve(SWT_37_CALLS, new NullProgressMonitor());
        assertTrue(file.exists());

        // is this one the latest version we have?
        // we just downloaded it, right?
        assertTrue(sut.isLatest(SWT_37_CALLS));

        // prepare the stage for install
        File move = File.createTempFile(file.getName(), ".zip");
        location.renameTo(move);
        assertFalse(location.exists());

        // how do we deal with non-existent model file? we ignore that fact:
        assertTrue(sut.isLatest(SWT_37_CALLS));

        // check install works
        sut.install(SWT_37_CALLS.setFile(move));
        assertTrue(location.exists());

        // check delete works
        sut.delete(SWT_37_CALLS.setFile(move));
        assertFalse(location.exists());

        // is 'null' the latest:
        assertFalse(sut.isLatest(SWT_37_CALLS));

    }

    @Test
    public void repoSmoketest() {
        sut.toString();
        sut.findHigestVersion(SWT_37_CALLS);
        sut.findLowestVersion(SWT_37_CALLS);
    }
}
