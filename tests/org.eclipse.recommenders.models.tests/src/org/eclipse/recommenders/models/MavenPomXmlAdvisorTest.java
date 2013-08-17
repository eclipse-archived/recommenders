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

import static org.eclipse.recommenders.models.DependencyType.PROJECT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.eclipse.recommenders.models.advisors.MavenPomXmlAdvisor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;

public class MavenPomXmlAdvisorTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private DependencyInfo info;

    @Before
    public void init() throws IOException {
        info = new DependencyInfo(folder.getRoot(), PROJECT);
    }

    @Test
    public void testProjectCoordinateExtractedFromSimplePom() throws IOException {
        writePomFile(folder.newFile("pom.xml"), Charsets.UTF_8, "org.example", "artifact", "1.0.0-SNAPSHOT");

        IProjectCoordinateAdvisor sut = new MavenPomXmlAdvisor();
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        ProjectCoordinate expected = new ProjectCoordinate("org.example", "artifact", "1.0.0");
        assertEquals(expected, optionalProjectCoordinate.get());
    }

    @Test
    public void testProjectCoordinateExtractedFromSimplePomInUtf16() throws IOException {
        writePomFile(folder.newFile("pom.xml"), Charsets.UTF_16, "org.example", "artifact", "1.0.0-SNAPSHOT");

        IProjectCoordinateAdvisor sut = new MavenPomXmlAdvisor();
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        ProjectCoordinate expected = new ProjectCoordinate("org.example", "artifact", "1.0.0");
        assertEquals(expected, optionalProjectCoordinate.get());
    }

    @Test
    public void testVariablesInPomResultInAbsent() throws IOException {
        writePomFile(folder.newFile("pom.xml"), Charsets.UTF_8, "${groupId}", "${artifactId}", "${version}");

        IProjectCoordinateAdvisor sut = new MavenPomXmlAdvisor();
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testMissingValuesInPomWithoutParentResultInAbsent() throws IOException {
        writePomFile(folder.newFile("pom.xml"), Charsets.UTF_8, null, "artifact", null);

        IProjectCoordinateAdvisor sut = new MavenPomXmlAdvisor();
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testMissingValuesAreTakenFromParent() throws IOException {
        writePomFile(folder.newFile("pom.xml"), Charsets.UTF_8, null, "artifact", null, "org.example", "parent",
                "1.0.0-SNAPSHOT");

        IProjectCoordinateAdvisor sut = new MavenPomXmlAdvisor();
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(info);

        ProjectCoordinate expected = new ProjectCoordinate("org.example", "artifact", "1.0.0");
        assertEquals(expected, optionalProjectCoordinate.get());
    }

    private static void writePomFile(File file, Charset charset, String groupId, String artifactId, String version)
            throws IOException {
        writePomFile(file, charset, groupId, artifactId, version, null, null, null);
    }

    private static void writePomFile(File file, Charset charset, String groupId, String artifactId, String version,
            String parentGroupId, String parentArtifactId, String parentVersion) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(out, charset);

        writer.append("<?xml version='1.0' encoding='").append(charset.name()).append("'?>\n");
        writer.append("<project xmlns='http://maven.apache.org/POM/4.0.0'\n")
                .append("xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n")
                .append("xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd'>\n");
        writer.append("<modelVersion>4.0.0</modelVersion>");

        writer.append("<parent>\n");
        if (parentGroupId != null) {
            writer.append("<groupId>").append(parentGroupId).append("</groupId>\n");
        }
        if (parentArtifactId != null) {
            writer.append("<artifactId>").append(parentArtifactId).append("</artifactId>\n");
        }
        if (parentVersion != null) {
            writer.append("<version>").append(parentVersion).append("</version>\n");
        }
        writer.append("</parent>\n");

        if (groupId != null) {
            writer.append("<groupId>").append(groupId).append("</groupId>\n");
        }
        if (artifactId != null) {
            writer.append("<artifactId>").append(artifactId).append("</artifactId>\n");
        }
        if (version != null) {
            writer.append("<version>").append(version).append("</version>\n");
        }
        writer.append("</project>\n");
        writer.close();
    }
}
