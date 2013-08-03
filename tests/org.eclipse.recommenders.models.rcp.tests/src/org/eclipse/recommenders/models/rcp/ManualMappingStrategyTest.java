package org.eclipse.recommenders.models.rcp;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.eclipse.recommenders.internal.models.rcp.ManualMappingStrategy;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Optional;

public class ManualMappingStrategyTest {

    private static final ProjectCoordinate EXPECTED_PROJECT_COORDINATE = new ProjectCoordinate("example",
            "example.project", "1.0.0");

    private static final ProjectCoordinate ANOTHER_EXPECTED_PROJECT_COORDINATE = new ProjectCoordinate(
            "another.example", "another.example.project", "1.2.3");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File persistenceFile;
    private DependencyInfo exampleDependencyInfo;
    private DependencyInfo anotherExampleDependencyInfo;

    @Before
    public void init() throws IOException {
        persistenceFile = folder.newFile("manual-mappings.json");
        exampleDependencyInfo = new DependencyInfo(folder.newFile("example_1.jar"), DependencyType.JAR);
        anotherExampleDependencyInfo = new DependencyInfo(folder.newFile("example_s.jar"), DependencyType.JRE);
    }

    @Test
    public void returnAbsentWhenNoMappingExist() throws IOException {
        ManualMappingStrategy sut = new ManualMappingStrategy(persistenceFile);

        Optional<ProjectCoordinate> projectCoordinate = sut.searchForProjectCoordinate(exampleDependencyInfo);

        assertFalse(projectCoordinate.isPresent());

        sut.close();
    }

    @Test
    public void returnManualMappingCorrect() throws IOException {
        ManualMappingStrategy sut = new ManualMappingStrategy(persistenceFile);

        sut.setManualMapping(exampleDependencyInfo, EXPECTED_PROJECT_COORDINATE);

        Optional<ProjectCoordinate> projectCoordinate = sut.searchForProjectCoordinate(exampleDependencyInfo);

        assertEquals(EXPECTED_PROJECT_COORDINATE, projectCoordinate.get());

        sut.close();
    }

    @Test
    public void returnManualMappingsCorrectForMoreMappings() throws IOException {
        ManualMappingStrategy sut = new ManualMappingStrategy(persistenceFile);

        sut.setManualMapping(exampleDependencyInfo, EXPECTED_PROJECT_COORDINATE);
        sut.setManualMapping(anotherExampleDependencyInfo, ANOTHER_EXPECTED_PROJECT_COORDINATE);

        Optional<ProjectCoordinate> projectCoordinate = sut.searchForProjectCoordinate(exampleDependencyInfo);
        assertEquals(EXPECTED_PROJECT_COORDINATE, projectCoordinate.get());

        Optional<ProjectCoordinate> anotherProjectCoordinate = sut
                .searchForProjectCoordinate(anotherExampleDependencyInfo);
        assertEquals(ANOTHER_EXPECTED_PROJECT_COORDINATE, anotherProjectCoordinate.get());

        sut.close();
    }

    @Test
    public void storageOfManualMappingsWorksCorrect() throws IOException {
        ManualMappingStrategy sut = new ManualMappingStrategy(persistenceFile);

        sut.setManualMapping(exampleDependencyInfo, EXPECTED_PROJECT_COORDINATE);
        sut.setManualMapping(anotherExampleDependencyInfo, ANOTHER_EXPECTED_PROJECT_COORDINATE);

        sut.close();

        sut = new ManualMappingStrategy(persistenceFile);
        sut.open();

        Optional<ProjectCoordinate> projectCoordinate = sut.searchForProjectCoordinate(exampleDependencyInfo);
        assertEquals(EXPECTED_PROJECT_COORDINATE, projectCoordinate.get());

        Optional<ProjectCoordinate> anotherProjectCoordinate = sut
                .searchForProjectCoordinate(anotherExampleDependencyInfo);
        assertEquals(ANOTHER_EXPECTED_PROJECT_COORDINATE, anotherProjectCoordinate.get());

        sut.close();
    }

}
