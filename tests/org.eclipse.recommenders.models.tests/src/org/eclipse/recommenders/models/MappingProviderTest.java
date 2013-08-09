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

import static com.google.common.base.Optional.fromNullable;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.recommenders.models.advisors.ProjectCoordinateAdvisorService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class MappingProviderTest {

    private static final ProjectCoordinate EXPECTED_PROJECT_COORDINATE = new ProjectCoordinate("example",
            "example.project", "1.0.0");
    private static final ProjectCoordinate ANOTHER_EXPECTED_PROJECT_COORDINATE = new ProjectCoordinate(
            "another.example", "another.example.project", "1.2.3");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    ProjectCoordinateAdvisorService sut = new ProjectCoordinateAdvisorService();

    File exampleFile;

    private IProjectCoordinateAdvisor createMockedStrategy(final ProjectCoordinate projectCoordinate,
            final DependencyType... dependencyTypes) {
        IProjectCoordinateAdvisor mockedStrategy = Mockito.mock(IProjectCoordinateAdvisor.class);
        Mockito.when(mockedStrategy.suggest(Mockito.any(DependencyInfo.class))).thenReturn(
                fromNullable(projectCoordinate));
        return mockedStrategy;
    }

    @Before
    public void init() throws IOException {
        exampleFile = folder.newFile("example.jar");
    }

    @Test
    public void testMappingProviderWithNoStrategy() throws IOException {
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(new DependencyInfo(exampleFile,
                DependencyType.JAR));

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testMappingProviderWithMockedStrategy() throws IOException {
        sut.addAdvisor(createMockedStrategy(EXPECTED_PROJECT_COORDINATE));
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(new DependencyInfo(exampleFile,
                DependencyType.JAR));

        assertEquals(EXPECTED_PROJECT_COORDINATE, optionalProjectCoordinate.get());
    }

    @Test
    public void testCorrectOrderOfStrategiesWithAddStrategies() throws IOException {
        ProjectCoordinateAdvisorService sut = new ProjectCoordinateAdvisorService();
        sut.addAdvisor(createMockedStrategy(EXPECTED_PROJECT_COORDINATE));
        sut.addAdvisor(createMockedStrategy(ANOTHER_EXPECTED_PROJECT_COORDINATE));

        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(new DependencyInfo(exampleFile,
                DependencyType.JAR));

        assertEquals(EXPECTED_PROJECT_COORDINATE, optionalProjectCoordinate.get());
    }

    @Test
    public void testSetStrategiesSetStrategiesCorrect() {
        List<IProjectCoordinateAdvisor> strategies = Lists.newArrayList();
        strategies.add(createMockedStrategy(EXPECTED_PROJECT_COORDINATE));
        strategies.add(createMockedStrategy(ANOTHER_EXPECTED_PROJECT_COORDINATE));
        sut.setAdvisors(strategies);

        assertEquals(strategies, sut.getAdvisors());
    }

    @Test
    public void testCorrectOrderOfStrategiesWithSetStrategies() throws IOException {
        sut.addAdvisor(createMockedStrategy(EXPECTED_PROJECT_COORDINATE));
        sut.addAdvisor(createMockedStrategy(ANOTHER_EXPECTED_PROJECT_COORDINATE));

        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(new DependencyInfo(exampleFile,
                DependencyType.JAR));

        assertEquals(EXPECTED_PROJECT_COORDINATE, optionalProjectCoordinate.get());
    }

    @Test
    public void testSecondStrategyWins() throws IOException {
        sut.addAdvisor(createMockedStrategy(null));
        sut.addAdvisor(createMockedStrategy(EXPECTED_PROJECT_COORDINATE));

        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(new DependencyInfo(exampleFile,
                DependencyType.JAR));
        assertEquals(EXPECTED_PROJECT_COORDINATE, optionalProjectCoordinate.get());
    }

}
