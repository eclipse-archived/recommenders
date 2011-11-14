/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.tests.analysis.rcp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.eclipse.recommenders.internal.analysis.rcp.cp.BundleManifestVersionFinder;
import org.eclipse.recommenders.utils.Version;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class NameAnalyzerTest {

    // TODO: create a platform independent test suite from this
    final File PLUGINS_DIR = new File("/Applications/Eclipse 3.6.2 64bit/plugins/");

    @Test
    public void testReadNameFromManifest() throws Exception {
        for (final File f : findPluginJars()) {
            final BundleManifestVersionFinder sut = new BundleManifestVersionFinder();
            final Version version = sut.find(f);
            assertNotNull(version);
        }
    }

    private File[] findPluginJars() {
        final FileFilter filter = FileFilterUtils.suffixFileFilter(".jar");
        final File[] res = PLUGINS_DIR.listFiles(filter);
        assertFalse(res.length == 0);
        return res;
    }
}
