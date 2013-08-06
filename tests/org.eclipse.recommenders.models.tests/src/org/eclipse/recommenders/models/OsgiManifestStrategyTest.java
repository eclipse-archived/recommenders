/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.models;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.eclipse.recommenders.models.DependencyType.JAR;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Optional;

public class OsgiManifestStrategyTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private File jarFileExample;

    private DependencyInfo info;

    @Before
    public void init() throws IOException {
        jarFileExample = folder.newFile("example.jar");
        info = new DependencyInfo(jarFileExample, JAR);
    }

    private IFileToJarFileConverter createFileToJarFileConverter(String bundleName, String bundleVersion) {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue(OsgiManifestStrategy.BUNDLE_NAME.toString(), bundleName);
        manifest.getMainAttributes().putValue(OsgiManifestStrategy.BUNDLE_VERSION.toString(), bundleVersion);

        IFileToJarFileConverter fileToJarFileConverter = new IFileToJarFileConverter() {

            @Override
            public Optional<JarFile> createJarFile(File file) {
                JarFile jarFileMock = mock(JarFile.class);
                try {
                    when(jarFileMock.getManifest()).thenReturn(manifest);
                } catch (IOException e) {
                    return absent();
                }
                return of(jarFileMock);
            }
        };
        return fileToJarFileConverter;
    }

    @Test
    public void testOnePartSymbolicNameBecomesGroupId() {
        IFileToJarFileConverter fileToJarFileConverter = createFileToJarFileConverter("example", "1.0.0");
        IProjectCoordinateResolver sut = new OsgiManifestStrategy(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        ProjectCoordinate expected = new ProjectCoordinate("example", "example", "1.0.0");
        assertEquals(expected, optionalProjectCoordinate.get());
    }

    @Test
    public void testTwoPartSymbolicNameBecomesGroupId() {
        IFileToJarFileConverter fileToJarFileConverter = createFileToJarFileConverter("org.example", "1.0.0");
        IProjectCoordinateResolver sut = new OsgiManifestStrategy(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        ProjectCoordinate expected = new ProjectCoordinate("org.example", "org.example", "1.0.0");
        assertEquals(expected, optionalProjectCoordinate.get());
    }

    @Test
    public void testThreePartSymbolicNameBecomesGroupId() {
        IFileToJarFileConverter fileToJarFileConverter = createFileToJarFileConverter("org.example.sample", "1.0.0");
        IProjectCoordinateResolver sut = new OsgiManifestStrategy(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        ProjectCoordinate expected = new ProjectCoordinate("org.example.sample", "org.example.sample", "1.0.0");
        assertEquals(expected, optionalProjectCoordinate.get());
    }

    @Test
    public void testFirstThreePartsOfSymbolicNameBecomesGroupId() {
        IFileToJarFileConverter fileToJarFileConverter = createFileToJarFileConverter("org.example.sample.test",
                "1.0.0");
        IProjectCoordinateResolver sut = new OsgiManifestStrategy(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        ProjectCoordinate expected = new ProjectCoordinate("org.example.sample", "org.example.sample.test", "1.0.0");
        assertEquals(expected, optionalProjectCoordinate.get());
    }

}
