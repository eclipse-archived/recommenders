package org.eclipse.recommenders.models.rcp;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.eclipse.recommenders.internal.models.rcp.ManualMappingStrategy;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Optional;

public class ManualMappingStrategyTest {

    private static final ProjectCoordinate EXPECTED_PROJECT_COORDINATE = new ProjectCoordinate("example",
            "example.project", "1.0.0");

    private static final ProjectCoordinate ANOTHER_EXPECTED_PROJECT_COORDINATE = new ProjectCoordinate(
            "another.example", "another.example.project", "1.2.3");

    private static final DependencyInfo EXAMPLE_DEPENDENCY_INFO = new DependencyInfo(new File("example_1.jar"),
            DependencyType.JAR);

    private static final DependencyInfo ANOTHER_EXAMPLE_DEPENDENCY_INFO = new DependencyInfo(new File("example_2.jar"),
            DependencyType.JRE);

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File createPersistanceFile() throws IOException {
        return folder.newFile("manual-mappings.json");
    }

    @Test
    public void returnAbsentWhenNoMappingExist() throws IOException {
        ManualMappingStrategy sut = new ManualMappingStrategy(createPersistanceFile());

        Optional<ProjectCoordinate> projectCoordinate = sut.searchForProjectCoordinate(EXAMPLE_DEPENDENCY_INFO);

        assertFalse(projectCoordinate.isPresent());

        sut.close();
    }

    @Test
    public void returnManualMappingCorrect() throws IOException {
        ManualMappingStrategy sut = new ManualMappingStrategy(createPersistanceFile());

        sut.setManualMapping(EXAMPLE_DEPENDENCY_INFO, EXPECTED_PROJECT_COORDINATE);

        Optional<ProjectCoordinate> projectCoordinate = sut.searchForProjectCoordinate(EXAMPLE_DEPENDENCY_INFO);

        assertEquals(EXPECTED_PROJECT_COORDINATE, projectCoordinate.get());

        sut.close();
    }

    @Test
    public void returnManualMappingsCorrectForMoreMappings() throws IOException {
        ManualMappingStrategy sut = new ManualMappingStrategy(createPersistanceFile());

        sut.setManualMapping(EXAMPLE_DEPENDENCY_INFO, EXPECTED_PROJECT_COORDINATE);
        sut.setManualMapping(ANOTHER_EXAMPLE_DEPENDENCY_INFO, ANOTHER_EXPECTED_PROJECT_COORDINATE);

        Optional<ProjectCoordinate> projectCoordinate = sut.searchForProjectCoordinate(EXAMPLE_DEPENDENCY_INFO);
        assertEquals(EXPECTED_PROJECT_COORDINATE, projectCoordinate.get());

        Optional<ProjectCoordinate> anotherProjectCoordinate = sut
                .searchForProjectCoordinate(ANOTHER_EXAMPLE_DEPENDENCY_INFO);
        assertEquals(ANOTHER_EXPECTED_PROJECT_COORDINATE, anotherProjectCoordinate.get());

        sut.close();
    }

    @Test
    public void storageOfManualMappingsWorksCorrect() throws IOException {
        File persistanceFile = createPersistanceFile();

        ManualMappingStrategy sut = new ManualMappingStrategy(persistanceFile);

        sut.setManualMapping(EXAMPLE_DEPENDENCY_INFO, EXPECTED_PROJECT_COORDINATE);
        sut.setManualMapping(ANOTHER_EXAMPLE_DEPENDENCY_INFO, ANOTHER_EXPECTED_PROJECT_COORDINATE);

        sut.close();

        sut = new ManualMappingStrategy(persistanceFile);

        Optional<ProjectCoordinate> projectCoordinate = sut.searchForProjectCoordinate(EXAMPLE_DEPENDENCY_INFO);
        assertEquals(EXPECTED_PROJECT_COORDINATE, projectCoordinate.get());

        Optional<ProjectCoordinate> anotherProjectCoordinate = sut
                .searchForProjectCoordinate(ANOTHER_EXAMPLE_DEPENDENCY_INFO);
        assertEquals(ANOTHER_EXPECTED_PROJECT_COORDINATE, anotherProjectCoordinate.get());

        sut.close();
    }

}
