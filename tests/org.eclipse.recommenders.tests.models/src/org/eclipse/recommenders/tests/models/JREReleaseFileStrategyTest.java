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
package org.eclipse.recommenders.tests.models;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.dependencies.DependencyInfo;
import org.eclipse.recommenders.models.dependencies.DependencyType;
import org.eclipse.recommenders.models.dependencies.IProjectCoordinateResolver;
import org.eclipse.recommenders.models.dependencies.impl.JREReleaseFileStrategy;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Optional;

public class JREReleaseFileStrategyTest {

    private static final ProjectCoordinate EXPECTED_PROJECT_COORDINATE = new ProjectCoordinate("jre", "jre", "1.0.0");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File javaHomeDirectory;
    private File releaseFile;

    private void createJavaHomeDirectory() throws IOException {
        javaHomeDirectory = folder.newFolder("JAVA_HOME");
    }

    private void createReleaseFile() throws IOException {
        releaseFile = folder.newFile("JAVA_HOME" + File.separator + "release");
    }

    private void fillReleaseFileWithVersion() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(releaseFile, true));
        writer.write("JAVA_VERSION=\"1.0.0\"\n");
        writer.close();
    }

    private void fillReleaseFileWithOtherStuff() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(releaseFile, true));
        writer.write("OS_NAME=\"Windows\"\n");
        writer.write("OS_VERSION=\"5.2\"\n");
        writer.write("OS_ARCH=\"amd64\"\n");
        writer.close();
    }

    @Test
    public void testInvalidType() {
        DependencyInfo info = new DependencyInfo(new File(""), DependencyType.JAR);
        IProjectCoordinateResolver sut = new JREReleaseFileStrategy();

        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testValidReleaseFile() throws IOException {
        createJavaHomeDirectory();
        createReleaseFile();
        fillReleaseFileWithVersion();
        fillReleaseFileWithOtherStuff();

        DependencyInfo info = new DependencyInfo(javaHomeDirectory, DependencyType.JRE);
        IProjectCoordinateResolver sut = new JREReleaseFileStrategy();

        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        assertEquals(EXPECTED_PROJECT_COORDINATE, optionalProjectCoordinate.get());
    }

    @Test
    public void testReleaseFileWithoutVersion() throws IOException {
        createJavaHomeDirectory();
        createReleaseFile();
        fillReleaseFileWithOtherStuff();

        DependencyInfo info = new DependencyInfo(javaHomeDirectory, DependencyType.JRE);
        IProjectCoordinateResolver sut = new JREReleaseFileStrategy();

        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testMissingReleaseFile() throws IOException {
        createJavaHomeDirectory();

        DependencyInfo info = new DependencyInfo(javaHomeDirectory, DependencyType.JRE);
        IProjectCoordinateResolver sut = new JREReleaseFileStrategy();

        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

}
