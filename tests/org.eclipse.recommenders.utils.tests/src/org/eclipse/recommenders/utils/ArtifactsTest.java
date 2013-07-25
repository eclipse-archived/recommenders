package org.eclipse.recommenders.utils;

import static java.lang.String.format;
import static org.eclipse.recommenders.utils.Artifacts.*;
import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

public class ArtifactsTest {
    String GID = "org.eclipse.recommenders";
    String AID = "org.eclipse.recommenders.core";
    String VERSION = "1.23.0";
    Artifact GID_AID_VER = asArtifact(format("%s:%s:%s", GID, AID, VERSION));
    Artifact GID_AID_EXT_VER = asArtifact(format("%s:%s:jar:%s", GID, AID, VERSION));
    Artifact GID_AID_EXT_CLS_VER = asArtifact(format("%s:%s:jar:sources:%s", GID, AID, VERSION));

    File LOCAL_M2_REPO = new File(SystemUtils.getUserHome(), ".m2/repository");
    String MAVEN_CENTRAL = "http://repo1.maven.org/maven2/";
    String VANDYK_JUNO = "http://vandyk.st.informatik.tu-darmstadt.de/juno/";

    public static Artifact SWT_37_CALLS = new DefaultArtifact("org.eclipse.swt", "org.eclipse.swt", "cr-calls", "zip",
            "3.7.0");

    @Test
    public void testAsCoord() {
        String expected = "org.eclipse.swt:org.eclipse.swt:zip:cr-calls:3.7.0";
        String actual = Artifacts.asCoordinate(SWT_37_CALLS);
        assertEquals(expected, actual);
    }

    @Test
    public void testGuessGid() {
        String expected = "org.eclipse.recommenders";
        String actual = Artifacts.guessGroupId("org.eclipse.recommenders.extdoc.rcp");
        assertEquals(expected, actual);

        expected = "org";
        actual = Artifacts.guessGroupId("org");
        assertEquals(expected, actual);

        expected = "org.eclipse";
        actual = Artifacts.guessGroupId("org.eclipse");
        assertEquals(expected, actual);

        expected = "name.bruch";
        actual = Artifacts.guessGroupId("name.bruch");
        assertEquals(expected, actual);

        expected = "myname";
        actual = Artifacts.guessGroupId("myname.bruch");
        assertEquals(expected, actual);

    }

    @Test
    public void testMatches() {
        Artifact glob = Artifacts.asArtifact("*:*:*");
        assertTrue(matches(GID_AID_VER, glob));
        assertTrue(matches(GID_AID_EXT_VER, glob));
        assertTrue(matches(GID_AID_EXT_CLS_VER, glob));
    }

    @Test
    public void testMatchesQuestionMarkInGid() {
        Artifact glob = Artifacts.asArtifact("???.*:*:*");
        assertTrue(matches(GID_AID_EXT_VER, glob));
    }

    @Test
    public void testMatchesQuestionMarkInVersion() {
        Artifact glob = Artifacts.asArtifact("*:*:1.??.0");
        assertTrue(matches(GID_AID_EXT_VER, glob));
        assertTrue(matches(GID_AID_EXT_CLS_VER, glob));
    }

    /**
     * note that version is matched lexicographically only!
     */
    @Test
    public void testMatchVersion() {
        Artifact glob = Artifacts.asArtifact("*:*:1.*");
        assertTrue(matches(GID_AID_VER, glob));

        glob = Artifacts.asArtifact("*:*:1.20*");
        assertFalse(matches(GID_AID_VER, glob));

        glob = Artifacts.asArtifact("*:*:1.2*");
        assertTrue(matches(GID_AID_VER, glob));
    }

    @Test
    public void testNewArtifact() {
        Artifact actual = Artifacts.newClassifierAndExtension(Artifacts.pom(SWT_37_CALLS),
                SWT_37_CALLS.getClassifier(), SWT_37_CALLS.getExtension());
        assertEquals(SWT_37_CALLS, actual);

        actual = Artifacts.newArtifact(SWT_37_CALLS.toString());
        assertEquals(SWT_37_CALLS, actual);
    }

    @Test
    public void testNewGlob() {
        verifyGlob(createGlobArtifact(""), "", "", "");
        verifyGlob(createGlobArtifact("com.oracle"), "com.oracle", "", "");
        verifyGlob(createGlobArtifact("com.oracle:te?t"), "com.oracle", "te?t", "");
        verifyGlob(createGlobArtifact("com.oracle:test:3?"), "com.oracle", "test", "3?");
        verifyGlob(createGlobArtifact("com.oracle:test:3"), "com.oracle", "test", "3");
        verifyGlob(createGlobArtifact("com.oracle:*:*"), "com.oracle", "*", "*");
    }

    @Test
    public void testNoMatch() {
        Artifact glob = Artifacts.asArtifact("com.oracle.*:*:*");
        assertFalse(matches(GID_AID_VER, glob));
        assertFalse(matches(GID_AID_EXT_VER, glob));
        assertFalse(matches(GID_AID_EXT_CLS_VER, glob));
    }

    @Test
    public void testPom() {
        Artifact expected = new DefaultArtifact("org.eclipse.swt:org.eclipse.swt:pom:3.7.0");
        Artifact actual = Artifacts.pom(SWT_37_CALLS);
        assertEquals(expected, actual);
    }

    @Test
    public void testToArtifactFileName() {
        String actual = Artifacts.toArtifactFileName(SWT_37_CALLS);
        assertEquals("org.eclipse.swt-3.7.0-cr-calls.zip", actual);
    }

    private void verifyGlob(Artifact glob, String gid, String aid, String ver) {
        assertEquals(gid, glob.getGroupId());
        assertEquals(aid, glob.getArtifactId());
        assertEquals(ver, glob.getVersion());
        assertEquals("", glob.getExtension());
    }

}
