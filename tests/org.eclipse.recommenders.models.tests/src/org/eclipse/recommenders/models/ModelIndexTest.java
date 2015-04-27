package org.eclipse.recommenders.models;

import static org.eclipse.recommenders.models.ModelIndexTestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class ModelIndexTest {

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private static final ProjectCoordinate PC_1 = new ProjectCoordinate("org.example", "project", "1.0.0");
    private static final ProjectCoordinate PC_2 = new ProjectCoordinate("org.example", "extended.project", "2.0.0");
    private static final ProjectCoordinate PC_3 = new ProjectCoordinate("org.example", "example", "1.0.0");
    private static final ProjectCoordinate PC_4 = new ProjectCoordinate("com.example", "tutorial", "1.0.0");

    private final List<ProjectCoordinate> oldIndex;
    private final List<ProjectCoordinate> newIndex;

    public ModelIndexTest(List<ProjectCoordinate> oldIndex, List<ProjectCoordinate> newIndex) throws Exception {
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
    }

    @Parameters
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(scenario(Lists.newArrayList(PC_1, PC_3, PC_4), Lists.newArrayList(PC_2, PC_4)));
        scenarios.add(scenario(Lists.newArrayList(PC_2, PC_4), Lists.<ProjectCoordinate>newArrayList()));
        scenarios.add(scenario(Lists.<ProjectCoordinate>newArrayList(), Lists.newArrayList(PC_2, PC_4)));

        return scenarios;
    }

    private static Object[] scenario(List<ProjectCoordinate> oldIndex, List<ProjectCoordinate> newIndex) {
        return new Object[] { oldIndex, newIndex };
    }

    @Test
    public void test() throws Exception {
        Directory oldIndexDirectory = createInMemeoryIndex(oldIndex);
        File newIndexLocation = createOnDiskIndex(newIndex);

        IModelIndex sut = new ModelIndex(oldIndexDirectory);
        sut.open();

        for (ProjectCoordinate expected : oldIndex) {
            ProjectCoordinate actual = sut.suggestProjectCoordinateByArtifactId(expected.getArtifactId()).get();
            assertThat(actual, is(equalTo(expected)));
        }

        for (ProjectCoordinate expected : newIndex) {
            if (!oldIndex.contains(expected)) {
                assertThat(sut.suggestProjectCoordinateByArtifactId(expected.getArtifactId()).isPresent(), is(false));
            }
        }

        sut.updateIndex(newIndexLocation);

        for (ProjectCoordinate expected : newIndex) {
            ProjectCoordinate actual = sut.suggestProjectCoordinateByArtifactId(expected.getArtifactId()).get();
            assertThat(actual, is(equalTo(expected)));
        }

        for (ProjectCoordinate expected : oldIndex) {
            if (!newIndex.contains(expected)) {
                assertThat(sut.suggestProjectCoordinateByArtifactId(expected.getArtifactId()).isPresent(), is(false));
            }
        }

        sut.close();
    }

    private Directory createInMemeoryIndex(Collection<ProjectCoordinate> projectCoordinates) throws Exception {
        Document[] documents = createDocuments(projectCoordinates);
        return inMemoryIndex(documents);
    }

    private File createOnDiskIndex(Collection<ProjectCoordinate> projectCoordinates) throws Exception {
        File location = tmp.newFolder();
        Document[] documents = createDocuments(projectCoordinates);
        Directory directory = onDiskIndex(location, documents);
        directory.close();
        return location;
    }

    private Document[] createDocuments(Collection<ProjectCoordinate> projectCoordinates) {
        Document[] documents = new Document[projectCoordinates.size()];

        int index = 0;
        for (ProjectCoordinate pc : projectCoordinates) {
            documents[index] = coordinateWithSymbolicName(new DefaultArtifact(pc.toString()), pc.getArtifactId());
            index++;
        }
        return documents;
    }

}
