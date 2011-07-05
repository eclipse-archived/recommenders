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

import java.io.ByteArrayInputStream;

import junit.framework.Assert;

import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.internal.commons.analysis.archive.MavenPomJarIdExtractor;
import org.junit.Test;

public class TestPomExtraction {

    @Test
    public void testNoPom() throws Exception {
        final MockJarFileBuilder builder = new MockJarFileBuilder();
        builder.addEntry("xyz/no/pom.txt", null);

        final MavenPomJarIdExtractor extractor = new MavenPomJarIdExtractor();
        extractor.extract(builder.build());

        Assert.assertNull(extractor.getName());
        Assert.assertTrue(extractor.getVersion().isUnknown());
    }

    @Test
    public void testPom() throws Exception {
        final MockJarFileBuilder builder = new MockJarFileBuilder();
        builder.addEntry("xyz/pom.properties", new ByteArrayInputStream(
                "version=1.2.3\ngroupId=test\nartifactId=project.pom".getBytes()));

        final MavenPomJarIdExtractor extractor = new MavenPomJarIdExtractor();
        extractor.extract(builder.build());

        Assert.assertEquals("test.project.pom", extractor.getName());
        Assert.assertEquals(Version.create(1, 2, 3), extractor.getVersion());
    }

    @Test
    public void testNoGroupId() throws Exception {
        final MockJarFileBuilder builder = new MockJarFileBuilder();
        builder.addEntry("xyz/pom.properties",
                new ByteArrayInputStream("version=1.2.3\nartifactId=test.project.pom".getBytes()));

        final MavenPomJarIdExtractor extractor = new MavenPomJarIdExtractor();
        extractor.extract(builder.build());

        Assert.assertEquals("test.project.pom", extractor.getName());
    }

    @Test
    public void testGroupIdRepeatedInArtifactId() throws Exception {
        final MockJarFileBuilder builder = new MockJarFileBuilder();
        builder.addEntry("xyz/pom.properties", new ByteArrayInputStream(
                "version=1.2.3\ngroupId=test\nartifactId=test.project.pom".getBytes()));

        final MavenPomJarIdExtractor extractor = new MavenPomJarIdExtractor();
        extractor.extract(builder.build());

        Assert.assertEquals("test.project.pom", extractor.getName());
    }
}
