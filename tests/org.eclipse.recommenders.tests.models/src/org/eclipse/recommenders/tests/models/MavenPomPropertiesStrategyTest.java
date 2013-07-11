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

import java.io.File;
import java.util.Properties;

import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.dependencies.DependencyInfo;
import org.eclipse.recommenders.models.dependencies.DependencyType;
import org.eclipse.recommenders.models.dependencies.IProjectCoordinateResolver;
import org.eclipse.recommenders.models.dependencies.impl.MavenPomPropertiesStrategy;
import org.eclipse.recommenders.models.dependencies.impl.MavenPomPropertiesStrategy.IFileToJarFileConverter;
import org.eclipse.recommenders.tests.models.utils.IFileToJarFileConverterMockBuilder;
import org.junit.Test;

import com.google.common.base.Optional;

public class MavenPomPropertiesStrategyTest {

    private static final File JAR_FILE_EXAMPLE = new File("example.jar");
    private static final Properties INPUT_PROPERTIES = createProperties("org.example", "example", "1.0.0");
    private static final ProjectCoordinate EXPECTED_PROJECT_COORDINATE = new ProjectCoordinate("org.example",
            "example", "1.0.0");

    private static Properties createProperties(final String groupId, final String artifactId, final String version) {
        Properties properties = new Properties();
        properties.put(MavenPomPropertiesStrategy.PROPERTY_KEY_GROUP_ID, groupId);
        properties.put(MavenPomPropertiesStrategy.PROPERTY_KEY_ARTIFACT_ID, artifactId);
        properties.put(MavenPomPropertiesStrategy.PROPERTY_KEY_VERSION, version);
        return properties;
    }

    private IFileToJarFileConverter createIFileToJarFileConverter(final String propertiesFileName,
            final Properties properties) {
        return new IFileToJarFileConverterMockBuilder().put(propertiesFileName, properties).build();
    }

    @Test
    public void testValidPomProperties() {
        final String propertiesFileName = "META-INF/maven/org.example/example/pom.properties";

        IFileToJarFileConverter fileToJarFileConverter = createIFileToJarFileConverter(propertiesFileName,
                INPUT_PROPERTIES);

        DependencyInfo info = new DependencyInfo(JAR_FILE_EXAMPLE, DependencyType.JAR);

        IProjectCoordinateResolver sut = new MavenPomPropertiesStrategy(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        assertEquals(EXPECTED_PROJECT_COORDINATE, optionalProjectCoordinate.get());
    }

    @Test
    public void testValidPomPropertiesInWrongDirectoryStructure() {
        final String propertiesFileName = "pom.properties";

        IFileToJarFileConverter fileToJarFileConverter = createIFileToJarFileConverter(propertiesFileName,
                INPUT_PROPERTIES);

        DependencyInfo info = new DependencyInfo(JAR_FILE_EXAMPLE, DependencyType.JAR);

        IProjectCoordinateResolver sut = new MavenPomPropertiesStrategy(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testValidPomPropertiesNotLocatedInMetaInfDirectory() {
        final String propertiesFileName = "maven/org.example/example/pom.properties";

        IFileToJarFileConverter fileToJarFileConverter = createIFileToJarFileConverter(propertiesFileName,
                INPUT_PROPERTIES);

        DependencyInfo info = new DependencyInfo(JAR_FILE_EXAMPLE, DependencyType.JAR);

        IProjectCoordinateResolver sut = new MavenPomPropertiesStrategy(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testValidPomPropertiesInWrongDirectory() {
        final String propertiesFileName = "nested/META-INF/maven/org.eclipse.group/org.eclipse.artifact/pom.properties";

        IFileToJarFileConverter fileToJarFileConverter = createIFileToJarFileConverter(propertiesFileName,
                INPUT_PROPERTIES);

        DependencyInfo info = new DependencyInfo(JAR_FILE_EXAMPLE, DependencyType.JAR);

        IProjectCoordinateResolver sut = new MavenPomPropertiesStrategy(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testValidPomPropertiesMavenDirectoryMissing() {
        final String propertiesFileName = "META-INF/org.example/example/pom.properties";

        IFileToJarFileConverter fileToJarFileConverter = createIFileToJarFileConverter(propertiesFileName,
                INPUT_PROPERTIES);

        DependencyInfo info = new DependencyInfo(JAR_FILE_EXAMPLE, DependencyType.JAR);

        IProjectCoordinateResolver sut = new MavenPomPropertiesStrategy(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testInvalidFileNameEnding() {
        final String propertiesFileName = "META-INF/maven/org.example/example/pom.invalid";

        IFileToJarFileConverter fileToJarFileConverter = createIFileToJarFileConverter(propertiesFileName,
                INPUT_PROPERTIES);

        DependencyInfo info = new DependencyInfo(JAR_FILE_EXAMPLE, DependencyType.JAR);

        IProjectCoordinateResolver sut = new MavenPomPropertiesStrategy(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testInvalidArtifactIdInPomPropertiesFileName() {

        final String propertiesFileName = "META-INF/maven/org.example/invalid/pom.properties";

        IFileToJarFileConverter fileToJarFileConverter = createIFileToJarFileConverter(propertiesFileName,
                INPUT_PROPERTIES);

        DependencyInfo info = new DependencyInfo(JAR_FILE_EXAMPLE, DependencyType.JAR);

        IProjectCoordinateResolver sut = new MavenPomPropertiesStrategy(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testInvalidArtifactIdAndGroudIdInFileName() {
        final String propertiesFileName = "META-INF/maven/invalid/example/pom.properties";

        IFileToJarFileConverter fileToJarFileConverter = createIFileToJarFileConverter(propertiesFileName,
                INPUT_PROPERTIES);

        DependencyInfo info = new DependencyInfo(JAR_FILE_EXAMPLE, DependencyType.JAR);

        IProjectCoordinateResolver sut = new MavenPomPropertiesStrategy(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testNotApplicableTyp() {
        final String propertiesFileName = "META-INF/maven/org.example/example/pom.properties";

        IFileToJarFileConverter fileToJarFileConverter = createIFileToJarFileConverter(propertiesFileName,
                INPUT_PROPERTIES);

        DependencyInfo info = new DependencyInfo(JAR_FILE_EXAMPLE, DependencyType.PROJECT);

        IProjectCoordinateResolver sut = new MavenPomPropertiesStrategy(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testApplicabaleTypButNoFile() {
        IFileToJarFileConverter fileToJarFileConverter = IFileToJarFileConverterMockBuilder
                .createEmptyIFileToJarFileConverter();

        DependencyInfo info = new DependencyInfo(new File(""), DependencyType.JAR);

        IProjectCoordinateResolver sut = new MavenPomPropertiesStrategy(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testJarContainMoreOneCorrectAndOneWrongPomPropertiesFindCorrectOneFirst() {
        final String propertiesFileName1 = "META-INF/maven/org.example/example/pom.properties";
        final String propertiesFileName2 = "META-INF/maven/org.example/invalid/pom.properties";

        IFileToJarFileConverter fileToJarFileConverter = new IFileToJarFileConverterMockBuilder()
                .put(propertiesFileName2, INPUT_PROPERTIES).put(propertiesFileName1, INPUT_PROPERTIES).build();

        DependencyInfo info = new DependencyInfo(JAR_FILE_EXAMPLE, DependencyType.JAR);

        IProjectCoordinateResolver sut = new MavenPomPropertiesStrategy(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        assertEquals(EXPECTED_PROJECT_COORDINATE, optionalProjectCoordinate.get());
    }

    @Test
    public void testJarContainMoreOneCorrectAndOneWrongPomPropertiesFindWrongOneFirst() {
        final String propertiesFileName1 = "META-INF/maven/org.example/invalid/pom.properties";
        final String propertiesFileName2 = "META-INF/maven/org.example/example/pom.properties";

        IFileToJarFileConverter fileToJarFileConverter = new IFileToJarFileConverterMockBuilder()
                .put(propertiesFileName1, INPUT_PROPERTIES).put(propertiesFileName2, INPUT_PROPERTIES).build();

        DependencyInfo info = new DependencyInfo(JAR_FILE_EXAMPLE, DependencyType.JAR);

        IProjectCoordinateResolver sut = new MavenPomPropertiesStrategy(fileToJarFileConverter);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(info);

        assertEquals(EXPECTED_PROJECT_COORDINATE, optionalProjectCoordinate.get());
    }

}
