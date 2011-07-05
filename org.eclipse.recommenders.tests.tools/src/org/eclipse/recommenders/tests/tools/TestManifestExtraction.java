/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.tools;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import junit.framework.Assert;

import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.internal.commons.analysis.archive.OsgiManifestJarIdExtractor;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Constants;

public class TestManifestExtraction {

    @Test
    public void testEmptyManifest() throws IOException {
        final JarFile jarFile = Mockito.mock(JarFile.class);
        Mockito.when(jarFile.getManifest()).thenReturn(new Manifest());

        final OsgiManifestJarIdExtractor extractor = new OsgiManifestJarIdExtractor();
        extractor.extract(jarFile);

        Assert.assertNull(extractor.getName());
        Assert.assertTrue(extractor.getVersion().isUnknown());
    }

    @Test
    public void testNoManifest() throws IOException {
        final JarFile jarFile = Mockito.mock(JarFile.class);
        Mockito.when(jarFile.getManifest()).thenReturn(null);

        final OsgiManifestJarIdExtractor extractor = new OsgiManifestJarIdExtractor();
        extractor.extract(jarFile);

        Assert.assertNull(extractor.getName());
        Assert.assertTrue(extractor.getVersion().isUnknown());
    }

    @Test
    public void testManifest() throws IOException {
        final Manifest manifest = Mockito.mock(Manifest.class);
        final Attributes attributes = new Attributes();
        attributes.putValue(Constants.BUNDLE_SYMBOLICNAME, "test.bundle.name");
        attributes.putValue(Constants.BUNDLE_VERSION, "1.2.3");
        Mockito.when(manifest.getMainAttributes()).thenReturn(attributes);

        final JarFile jarFile = Mockito.mock(JarFile.class);
        Mockito.when(jarFile.getManifest()).thenReturn(manifest);

        final OsgiManifestJarIdExtractor extractor = new OsgiManifestJarIdExtractor();
        extractor.extract(jarFile);

        Assert.assertEquals("test.bundle.name", extractor.getName());
        Assert.assertEquals(Version.create(1, 2, 3), extractor.getVersion());
    }

    @Test
    public void testSingletonManifest() throws IOException {
        final Manifest manifest = Mockito.mock(Manifest.class);
        final Attributes attributes = new Attributes();
        attributes.putValue(Constants.BUNDLE_SYMBOLICNAME, "test.bundle.name;singleton:=true");
        attributes.putValue(Constants.BUNDLE_VERSION, "1.2.3");
        Mockito.when(manifest.getMainAttributes()).thenReturn(attributes);

        final JarFile jarFile = Mockito.mock(JarFile.class);
        Mockito.when(jarFile.getManifest()).thenReturn(manifest);

        final OsgiManifestJarIdExtractor extractor = new OsgiManifestJarIdExtractor();
        extractor.extract(jarFile);

        Assert.assertEquals("test.bundle.name", extractor.getName());
        Assert.assertEquals(Version.create(1, 2, 3), extractor.getVersion());
    }
}
