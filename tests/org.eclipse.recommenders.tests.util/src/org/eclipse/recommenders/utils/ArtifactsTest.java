package org.eclipse.recommenders.utils;

import static java.lang.String.format;
import static org.eclipse.recommenders.utils.Artifacts.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.recommenders.utils.Artifacts;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;

public class ArtifactsTest {
    String GID = "org.eclipse.recommenders";
    String AID = "org.eclipse.recommenders.core";
    String VERSION = "1.23.0";
    Artifact GID_AID_VER = asArtifact(format("%s:%s:%s", GID, AID, VERSION));
    Artifact GID_AID_EXT_VER = asArtifact(format("%s:%s:jar:%s", GID, AID, VERSION));
    Artifact GID_AID_EXT_CLS_VER = asArtifact(format("%s:%s:jar:sources:%s", GID, AID, VERSION));

    @Test
    public void testMatches() {
        Artifact glob = Artifacts.asArtifact("*:*:*");
        assertTrue(matches(GID_AID_VER, glob));
        assertTrue(matches(GID_AID_EXT_VER, glob));
        assertTrue(matches(GID_AID_EXT_CLS_VER, glob));
    }

    @Test
    public void testMatchesQuestionMarkInVersion() {
        Artifact glob = Artifacts.asArtifact("*:*:1.??.0");
        assertTrue(matches(GID_AID_EXT_VER, glob));
        assertTrue(matches(GID_AID_EXT_CLS_VER, glob));
    }

    @Test
    public void testMatchesQuestionMarkInGid() {
        Artifact glob = Artifacts.asArtifact("???.*:*:*");
        assertTrue(matches(GID_AID_EXT_VER, glob));
    }

    @Test
    public void testNoMatch() {
        Artifact glob = Artifacts.asArtifact("com.oracle.*:*:*");
        assertFalse(matches(GID_AID_VER, glob));
        assertFalse(matches(GID_AID_EXT_VER, glob));
        assertFalse(matches(GID_AID_EXT_CLS_VER, glob));
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

    private void verifyGlob(Artifact glob, String gid, String aid, String ver) {
        assertEquals(gid, glob.getGroupId());
        assertEquals(aid, glob.getArtifactId());
        assertEquals(ver, glob.getVersion());
        assertEquals("", glob.getExtension());
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
}
