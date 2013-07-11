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

import static com.google.common.base.Optional.fromNullable;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.dependencies.DependencyInfo;
import org.eclipse.recommenders.models.dependencies.DependencyType;
import org.eclipse.recommenders.models.dependencies.IMappingProvider;
import org.eclipse.recommenders.models.dependencies.IProjectCoordinateResolver;
import org.eclipse.recommenders.models.dependencies.impl.MappingProvider;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class ProjectCoordinateProviderTest {

    private static final ProjectCoordinate EXPECTED_PROJECT_COORDINATE = new ProjectCoordinate("example",
            "example.project", "1.0.0");
    private static final ProjectCoordinate ANOTHER_EXPECTED_PROJECT_COORDINATE = new ProjectCoordinate(
            "another.example", "another.example.project", "1.2.3");

    private IProjectCoordinateResolver createMockedStrategy(final ProjectCoordinate projectCoordinate,
            final DependencyType... dependencyTypes) {
        IProjectCoordinateResolver mockedStrategy = Mockito.mock(IProjectCoordinateResolver.class);
        Mockito.when(mockedStrategy.searchForProjectCoordinate(Mockito.any(DependencyInfo.class))).thenReturn(
                fromNullable(projectCoordinate));
        Mockito.when(mockedStrategy.isApplicable(Mockito.any(DependencyType.class))).thenReturn(false);
        for (DependencyType dependencyType : dependencyTypes) {
            Mockito.when(mockedStrategy.isApplicable(dependencyType)).thenReturn(true);
        }
        return mockedStrategy;
    }

    @Test
    public void testMappingProviderWithNoStrategy() {
        IMappingProvider sut = new MappingProvider();
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(new DependencyInfo(
                new File("example.jar"), DependencyType.JAR));

        assertFalse(optionalProjectCoordinate.isPresent());
    }

    @Test
    public void testMappingProviderWithMockedStrategy() {
        IMappingProvider sut = new MappingProvider();
        sut.addStrategy(createMockedStrategy(EXPECTED_PROJECT_COORDINATE));
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(new DependencyInfo(
                new File("example.jar"), DependencyType.JAR));

        assertEquals(EXPECTED_PROJECT_COORDINATE, optionalProjectCoordinate.get());
    }

    @Test
    public void testCorrectOrderOfStrategiesWithAddStrategies() {
        IMappingProvider sut = new MappingProvider();
        sut.addStrategy(createMockedStrategy(EXPECTED_PROJECT_COORDINATE));
        sut.addStrategy(createMockedStrategy(ANOTHER_EXPECTED_PROJECT_COORDINATE));

        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(new DependencyInfo(
                new File("example.jar"), DependencyType.JAR));

        assertEquals(EXPECTED_PROJECT_COORDINATE, optionalProjectCoordinate.get());
    }

    @Test
    public void testSetStrategiesSetStrategiesCorrect() {
        IMappingProvider sut = new MappingProvider();

        List<IProjectCoordinateResolver> strategies = Lists.newArrayList();
        strategies.add(createMockedStrategy(EXPECTED_PROJECT_COORDINATE));
        strategies.add(createMockedStrategy(ANOTHER_EXPECTED_PROJECT_COORDINATE));
        sut.setStrategies(strategies);

        assertEquals(strategies, sut.getStrategies());
    }

    @Test
    public void testIsApplicableWithoutStrategies() {
        IMappingProvider sut = new MappingProvider();
        assertFalse(sut.isApplicable(DependencyType.JAR));
    }

    @Test
    public void testIsApplicableWithStrategies() {
        IMappingProvider sut = new MappingProvider();
        sut.addStrategy(createMockedStrategy(ProjectCoordinate.UNKNOWN, DependencyType.JRE));
        sut.addStrategy(createMockedStrategy(ProjectCoordinate.UNKNOWN, DependencyType.JAR));
        assertTrue(sut.isApplicable(DependencyType.JAR));
    }

    @Test
    public void testCorrectOrderOfStrategiesWithSetStrategies() {
        IMappingProvider sut = new MappingProvider();

        List<IProjectCoordinateResolver> strategies = Lists.newArrayList();
        strategies.add(createMockedStrategy(EXPECTED_PROJECT_COORDINATE));
        strategies.add(createMockedStrategy(ANOTHER_EXPECTED_PROJECT_COORDINATE));
        sut.setStrategies(strategies);

        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(new DependencyInfo(
                new File("example.jar"), DependencyType.JAR));

        assertEquals(EXPECTED_PROJECT_COORDINATE, optionalProjectCoordinate.get());
    }

    @Test
    public void testSecondStrategyWins() {
        IMappingProvider sut = new MappingProvider();

        List<IProjectCoordinateResolver> strategies = Lists.newArrayList();
        strategies.add(createMockedStrategy(null));
        strategies.add(createMockedStrategy(EXPECTED_PROJECT_COORDINATE));
        sut.setStrategies(strategies);

        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(new DependencyInfo(
                new File("example.jar"), DependencyType.JAR));

        assertEquals(EXPECTED_PROJECT_COORDINATE, optionalProjectCoordinate.get());
    }

    @Test
    public void testMappingCacheMissAtFirstTime() {
        MappingProvider sut = new MappingProvider();
        sut.addStrategy(createMockedStrategy(EXPECTED_PROJECT_COORDINATE));
        DependencyInfo dependencyInfo = new DependencyInfo(new File("example.jar"), DependencyType.JAR);
        sut.searchForProjectCoordinate(dependencyInfo);
        sut.getHitCount();
        assertEquals(1, sut.getMissCount());
    }

    @Test
    public void testMappingCacheHitAtSecondTime() {
        MappingProvider sut = new MappingProvider();
        sut.addStrategy(createMockedStrategy(EXPECTED_PROJECT_COORDINATE));
        DependencyInfo dependencyInfo = new DependencyInfo(new File("example.jar"), DependencyType.JAR);
        sut.searchForProjectCoordinate(dependencyInfo);
        sut.searchForProjectCoordinate(dependencyInfo);

        assertEquals(1, sut.getHitCount());
    }

    @Test
    public void testManualMappingIsReturned() {
        MappingProvider sut = new MappingProvider();
        DependencyInfo dependencyInfo = new DependencyInfo(new File("example.jar"), DependencyType.JAR);

        sut.setManualMapping(dependencyInfo, EXPECTED_PROJECT_COORDINATE);
        Optional<ProjectCoordinate> actual = sut.searchForProjectCoordinate(dependencyInfo);

        sut.searchForProjectCoordinate(dependencyInfo);

        assertEquals(EXPECTED_PROJECT_COORDINATE, actual.get());
    }

    @Test
    public void testManualMappingWinsOverStrategies() {
        MappingProvider sut = new MappingProvider();
        sut.addStrategy(createMockedStrategy(EXPECTED_PROJECT_COORDINATE));
        DependencyInfo dependencyInfo = new DependencyInfo(new File("example.jar"), DependencyType.JAR);
        Optional<ProjectCoordinate> actual = sut.searchForProjectCoordinate(dependencyInfo);

        assertEquals(EXPECTED_PROJECT_COORDINATE, actual.get());

        sut.setManualMapping(dependencyInfo, ANOTHER_EXPECTED_PROJECT_COORDINATE);
        actual = sut.searchForProjectCoordinate(dependencyInfo);

        sut.searchForProjectCoordinate(dependencyInfo);

        assertEquals(ANOTHER_EXPECTED_PROJECT_COORDINATE, actual.get());
    }

}
