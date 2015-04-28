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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.DependencyType;
import org.eclipse.recommenders.coordinates.IProjectCoordinateAdvisor;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class JREExecutionEnvironmentAdvisorTest {

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private File javaHome;

    @Before
    public void init() throws IOException {
        javaHome = tmp.newFolder("JAVA_HOME");
    }

    private static final ProjectCoordinate EXPECTED_PROJECT_COORDINATE = new ProjectCoordinate("jre", "jre", "1.6.0");

    private static Map<String, String> createAttributesMapForExecutionEnvironment(final String executionEnvironment) {
        Map<String, String> attributes = Maps.newHashMap();
        attributes.put(DependencyInfo.EXECUTION_ENVIRONMENT, executionEnvironment);
        return attributes;
    }

    @Test
    public void testNotSupportedType() {
        DependencyInfo info = new DependencyInfo(javaHome, DependencyType.JAR);
        IProjectCoordinateAdvisor sut = new JREExecutionEnvironmentAdvisor();

        sut.suggest(info);
    }

    @Test
    public void testMissingInformation() {
        DependencyInfo info = new DependencyInfo(javaHome, DependencyType.JRE);
        IProjectCoordinateAdvisor sut = new JREExecutionEnvironmentAdvisor();

        Optional<ProjectCoordinate> extractProjectCoordinate = sut.suggest(info);

        assertFalse(extractProjectCoordinate.isPresent());
    }

    @Test
    public void testValidJRE() {
        DependencyInfo info = new DependencyInfo(javaHome, DependencyType.JRE,
                createAttributesMapForExecutionEnvironment("JavaSE-1.6"));
        IProjectCoordinateAdvisor sut = new JREExecutionEnvironmentAdvisor();

        Optional<ProjectCoordinate> projectCoordinate = sut.suggest(info);

        assertEquals(EXPECTED_PROJECT_COORDINATE, projectCoordinate.get());
    }

}
