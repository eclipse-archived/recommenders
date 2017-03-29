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
package org.eclipse.recommenders.coordinates.jre;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.DependencyType;
import org.eclipse.recommenders.coordinates.IProjectCoordinateAdvisor;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Optional;

public class JREReleaseFileAdvisorTest {

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private File javaHomeDirectory;
    private File releaseFile;

    private File createDummyFile() throws IOException {
        return tmp.newFolder("dummy.jar");
    }

    private void createJavaHomeDirectory() throws IOException {
        javaHomeDirectory = tmp.newFolder("JAVA_HOME");
    }

    private void createReleaseFile() throws IOException {
        releaseFile = tmp.newFile("JAVA_HOME" + File.separator + "release");
    }

    private void fillReleaseFileWithVersion(CharSequence version) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(releaseFile, true));
        writer.append("JAVA_VERSION=\"").append(version).append("\"\n");
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
    public void testInvalidType() throws IOException {
        DependencyInfo info = new DependencyInfo(createDummyFile(), DependencyType.JAR);
        IProjectCoordinateAdvisor sut = new JREReleaseFileAdvisor();

        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testValidPreJava9ReleaseFile() throws IOException {
        createJavaHomeDirectory();
        createReleaseFile();
        fillReleaseFileWithVersion("1.6.0");
        fillReleaseFileWithOtherStuff();

        DependencyInfo info = new DependencyInfo(javaHomeDirectory, DependencyType.JRE);
        IProjectCoordinateAdvisor sut = new JREReleaseFileAdvisor();

        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        assertEquals(new ProjectCoordinate("jre", "jre", "1.6.0"), optionalProjectCoordinate.get());
    }

    @Test
    public void testValidPostJava9ReleaseFile() throws IOException {
        createJavaHomeDirectory();
        createReleaseFile();
        fillReleaseFileWithVersion("9");
        fillReleaseFileWithOtherStuff();

        DependencyInfo info = new DependencyInfo(javaHomeDirectory, DependencyType.JRE);
        IProjectCoordinateAdvisor sut = new JREReleaseFileAdvisor();

        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        assertEquals(new ProjectCoordinate("jre", "jre", "1.9.0"), optionalProjectCoordinate.get());
    }

    @Test
    public void testReleaseFileWithoutVersion() throws IOException {
        createJavaHomeDirectory();
        createReleaseFile();
        fillReleaseFileWithOtherStuff();

        DependencyInfo info = new DependencyInfo(javaHomeDirectory, DependencyType.JRE);
        IProjectCoordinateAdvisor sut = new JREReleaseFileAdvisor();

        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testMissingReleaseFile() throws IOException {
        createJavaHomeDirectory();

        DependencyInfo info = new DependencyInfo(javaHomeDirectory, DependencyType.JRE);
        IProjectCoordinateAdvisor sut = new JREReleaseFileAdvisor();

        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }
}
