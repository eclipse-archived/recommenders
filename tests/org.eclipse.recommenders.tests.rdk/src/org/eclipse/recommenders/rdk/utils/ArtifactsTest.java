package org.eclipse.recommenders.rdk.utils;

import static java.lang.String.format;
import static org.eclipse.recommenders.rdk.utils.Artifacts.asArtifact;
import static org.eclipse.recommenders.rdk.utils.Artifacts.matches;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void testNoMatch() {
        Artifact glob = Artifacts.asArtifact("com.oracle.*:*:*");
        assertFalse(matches(GID_AID_VER, glob));
        assertFalse(matches(GID_AID_EXT_VER, glob));
        assertFalse(matches(GID_AID_EXT_CLS_VER, glob));
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
