package org.eclipse.recommenders.internal.commons.analysis.analyzers.codemodules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.Test;

public class NameAnalyzerTest {

    // TODO: create a platform independent test suite from this
    final File PLUGINS_DIR = new File("/Applications/Eclipse 3.6.2 64bit/plugins/");

    @Test
    public void testReadNameFromManifest() throws Exception {
        for (final File f : findPluginJars()) {
            final BundleManifestVersionFinder sut = new BundleManifestVersionFinder();
            final String findName = sut.findName(f);
            assertNotNull(findName);
        }
    }

    private File[] findPluginJars() {
        final FileFilter filter = FileFilterUtils.suffixFileFilter(".jar");
        final File[] res = PLUGINS_DIR.listFiles(filter);
        assertFalse(res.length == 0);
        return res;
    }
}
