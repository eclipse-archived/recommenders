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
package org.eclipse.recommenders.models.advisors;

import static com.google.common.base.Optional.*;
import static org.eclipse.recommenders.models.DependencyType.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.IProjectCoordinateAdvisor;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.utils.Nullable;
import org.eclipse.recommenders.utils.Zips.IFileToJarFileConverter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class OsgiManifestAdvisorTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private final String bundleSymbolicName;
    private final String bundleVersion;
    private final Optional<ProjectCoordinate> expectedCoordinate;

    public OsgiManifestAdvisorTest(String description, String bundleSymbolicName, String bundleVersion,
            @Nullable ProjectCoordinate expectedCoordinate) {
        this.bundleSymbolicName = bundleSymbolicName;
        this.bundleVersion = bundleVersion;
        this.expectedCoordinate = Optional.fromNullable(expectedCoordinate);
    }

    @Parameters(name = "{index}: {0} ({1};bundle-version=\"{2}\")")
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(scenario("One-part Bundle-SymbolicName becomes groupId", "example", "1.0.0",
                new ProjectCoordinate("example", "example", "1.0.0")));
        scenarios.add(scenario("Two-part Bundle-SymbolicName becomes groupId", "org.example", "1.0.0",
                new ProjectCoordinate("org.example", "org.example", "1.0.0")));
        scenarios.add(scenario("Three-part Bundle-SymbolicName becomes groupId", "org.example.project", "1.0.0",
                new ProjectCoordinate("org.example.project", "org.example.project", "1.0.0")));

        scenarios.add(scenario("The groupId is limited to first part of unknown top-level domain", "javax.beans",
                "1.0.0", new ProjectCoordinate("javax", "javax.beans", "1.0.0")));
        scenarios.add(scenario("The groupId is limited to first three parts of Bundle-SymbolicName",
                "org.example.project.test", "1.0.0", new ProjectCoordinate("org.example.project",
                        "org.example.project.test", "1.0.0")));

        scenarios.add(scenario("Qualifier of Bundle-Version is stripped", "org.example.project", "1.0.0.qualifier",
                new ProjectCoordinate("org.example.project", "org.example.project", "1.0.0")));
        scenarios.add(scenario("Expanded qualifier of Bundle-Version is stripped", "org.example.project",
                "1.0.0.v20140814-1000", new ProjectCoordinate("org.example.project", "org.example.project", "1.0.0")));

        scenarios.add(scenario("Directories of Bundle-SymbolicName are stripped", "org.example.project;singleton=true",
                "1.0.0", new ProjectCoordinate("org.example.project", "org.example.project", "1.0.0")));

        scenarios.add(scenario("Invalid Bundle-Version", "org.example.project", "1.0-Beta", null));
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=441751
        scenarios
        .add(scenario("Invalid domain name in Bundle-SymbolicName", "org.example.project._test", "1.0.0", null));

        return scenarios;
    }

    private static Object[] scenario(String description, String bundleSymbolicName, String bundleVersion,
            ProjectCoordinate expectedCoordinate) {
        return new Object[] { description, bundleSymbolicName, bundleVersion, expectedCoordinate };
    }

    @Test
    public void testManifestInJarDependency() throws Exception {
        File jarFile = folder.newFile("dependency.jar");
        DependencyInfo info = new DependencyInfo(jarFile, JAR);
        IFileToJarFileConverter fileToJarFileConverter = createFileToJarFileConverter(bundleSymbolicName, bundleVersion);

        IProjectCoordinateAdvisor sut = new OsgiManifestAdvisor(fileToJarFileConverter);
        Optional<ProjectCoordinate> suggestion = sut.suggest(info);

        assertThat(suggestion, is(equalTo(expectedCoordinate)));
    }

    @Test
    public void testManifestInProjectDependency() throws Exception {
        File projectDirectory = createProjectWithManifestFile("TestProject", bundleSymbolicName, bundleVersion);
        DependencyInfo info = new DependencyInfo(projectDirectory, PROJECT);

        IProjectCoordinateAdvisor sut = new OsgiManifestAdvisor();
        Optional<ProjectCoordinate> suggestion = sut.suggest(info);

        assertThat(suggestion, is(equalTo(expectedCoordinate)));
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

    private File createProjectWithManifestFile(String projectName, String bundleSymbolicName, String bundleVersion)
            throws IOException, FileNotFoundException {
        File projectFolder = folder.newFolder(projectName);
        File metaInfFolder = folder.newFolder(projectName, "META-INF");
        File manifestFile = new File(metaInfFolder, "MANIFEST.MF");
        manifestFile.createNewFile();

        Manifest manifest = createManifest(bundleSymbolicName, bundleVersion);
        FileOutputStream fileOutputStream = new FileOutputStream(manifestFile);
        manifest.write(fileOutputStream);
        fileOutputStream.close();

        return projectFolder;
    }

    private Manifest createManifest(String bundleName, String bundleVersion) {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue(OsgiManifestAdvisor.BUNDLE_NAME.toString(), bundleName);
        manifest.getMainAttributes().putValue(OsgiManifestAdvisor.BUNDLE_VERSION.toString(), bundleVersion);
        manifest.getMainAttributes().putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
        return manifest;
    }
}
