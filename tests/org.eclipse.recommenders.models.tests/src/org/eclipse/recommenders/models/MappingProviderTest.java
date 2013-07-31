package org.eclipse.recommenders.models;

import static com.google.common.base.Optional.fromNullable;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.IMappingProvider;
import org.eclipse.recommenders.models.IProjectCoordinateResolver;
import org.eclipse.recommenders.models.MappingProvider;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class MappingProviderTest {

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

}
