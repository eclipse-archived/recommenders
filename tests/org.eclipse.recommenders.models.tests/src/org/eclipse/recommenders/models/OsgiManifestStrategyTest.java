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

import static com.google.common.base.Optional.*;
import static org.eclipse.recommenders.models.DependencyType.JAR;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.recommenders.models.advisors.OsgiManifestAdvisor;
import org.eclipse.recommenders.utils.Zips.IFileToJarFileConverter;
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
        final Manifest manifest = createManifest(bundleName, bundleVersion);

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

    private Manifest createManifest(String bundleName, String bundleVersion) {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue(OsgiManifestAdvisor.BUNDLE_NAME.toString(), bundleName);
        manifest.getMainAttributes().putValue(OsgiManifestAdvisor.BUNDLE_VERSION.toString(), bundleVersion);
        manifest.getMainAttributes().putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
        return manifest;
    }

    @Test
    public void testOnePartSymbolicNameBecomesGroupId() {
        IFileToJarFileConverter fileToJarFileConverter = createFileToJarFileConverter("example", "1.0.0");
        IProjectCoordinateAdvisor sut = new OsgiManifestAdvisor(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        ProjectCoordinate expected = new ProjectCoordinate("example", "example", "1.0.0");
        assertEquals(expected, optionalProjectCoordinate.get());
    }

    @Test
    public void testTwoPartSymbolicNameBecomesGroupId() {
        IFileToJarFileConverter fileToJarFileConverter = createFileToJarFileConverter("org.example", "1.0.0");
        IProjectCoordinateAdvisor sut = new OsgiManifestAdvisor(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        ProjectCoordinate expected = new ProjectCoordinate("org.example", "org.example", "1.0.0");
        assertEquals(expected, optionalProjectCoordinate.get());
    }

    @Test
    public void testThreePartSymbolicNameBecomesGroupId() {
        IFileToJarFileConverter fileToJarFileConverter = createFileToJarFileConverter("org.example.sample", "1.0.0");
        IProjectCoordinateAdvisor sut = new OsgiManifestAdvisor(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        ProjectCoordinate expected = new ProjectCoordinate("org.example.sample", "org.example.sample", "1.0.0");
        assertEquals(expected, optionalProjectCoordinate.get());
    }

    @Test
    public void testFirstThreePartsOfSymbolicNameBecomesGroupId() {
        IFileToJarFileConverter fileToJarFileConverter = createFileToJarFileConverter("org.example.sample.test",
                "1.0.0");
        IProjectCoordinateAdvisor sut = new OsgiManifestAdvisor(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        ProjectCoordinate expected = new ProjectCoordinate("org.example.sample", "org.example.sample.test", "1.0.0");
        assertEquals(expected, optionalProjectCoordinate.get());
    }

    @Test
    public void extractProjectCoordinateForProjectCorrect() throws IOException {
        File projectFolder = createProjectWithManifestFile("TestProject", "org.example.sample.test", "1.0.0");

        DependencyInfo info = new DependencyInfo(projectFolder, DependencyType.PROJECT);
        ProjectCoordinate expected = new ProjectCoordinate("org.example.sample", "org.example.sample.test", "1.0.0");

        IProjectCoordinateAdvisor sut = new OsgiManifestAdvisor();
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        assertEquals(expected, optionalProjectCoordinate.get());
    }

    @Test
    public void extractProjectCoordinateForProjectCorrectWhenBundleVersionContainsQualifier() throws IOException {
        File projectFolder = createProjectWithManifestFile("TestProject", "org.example.sample.test", "1.0.0.qualifier");

        DependencyInfo info = new DependencyInfo(projectFolder, DependencyType.PROJECT);
        ProjectCoordinate expected = new ProjectCoordinate("org.example.sample", "org.example.sample.test", "1.0.0");

        IProjectCoordinateAdvisor sut = new OsgiManifestAdvisor();
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        assertEquals(expected, optionalProjectCoordinate.get());
    }

    @Test
    public void extractProjectCoordinateForProjectCorrectWhenBundleNameContainsDirective() throws IOException {
        File projectFolder = createProjectWithManifestFile("TestProject", "org.example.sample.test;singleton:=true",
                "1.0.0.qualifier");

        DependencyInfo info = new DependencyInfo(projectFolder, DependencyType.PROJECT);
        ProjectCoordinate expected = new ProjectCoordinate("org.example.sample", "org.example.sample.test", "1.0.0");

        IProjectCoordinateAdvisor sut = new OsgiManifestAdvisor();
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        assertEquals(expected, optionalProjectCoordinate.get());
    }

    @Test
    public void testInvalidVersionResultInAbsent() {
        IFileToJarFileConverter fileToJarFileConverter = createFileToJarFileConverter("org.example.sample", "1.0-Beta");
        IProjectCoordinateAdvisor sut = new OsgiManifestAdvisor(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    private File createProjectWithManifestFile(String projectName, String bundleName, String bundleVersion)
            throws IOException, FileNotFoundException {
        File projectFolder = folder.newFolder(projectName);
        File manifestFile = new File(projectFolder, "META-INF" + File.separator + "MANIFEST.MF");
        manifestFile.getParentFile().mkdir();
        manifestFile.createNewFile();
        Manifest manifest = createManifest(bundleName, bundleVersion);
        FileOutputStream fileOutputStream = new FileOutputStream(manifestFile);
        manifest.write(fileOutputStream);
        fileOutputStream.close();
        return projectFolder;
    }
}
