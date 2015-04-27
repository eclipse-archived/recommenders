package org.eclipse.recommenders.models;

import static org.eclipse.recommenders.models.ModelIndexTestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class ModelIndexSuggestProjectCoordinateByArtifactIdTest {

    private static final String SYMBOLIC_NAME = "org.example.project";

    private static final ProjectCoordinate EXPECTED = new ProjectCoordinate("org.example", "project", "1.0.0");

    private final ProjectCoordinate expected;
    private final String symbolicName;
    private final Document[] indexContents;

    public ModelIndexSuggestProjectCoordinateByArtifactIdTest(ProjectCoordinate expected, String symbolicName, Document... indexContents) {
        this.expected = expected;
        this.symbolicName = symbolicName;
        this.indexContents = indexContents;
    }

    @Parameters
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(scenario(EXPECTED, SYMBOLIC_NAME,
                coordinateWithSymbolicName(new DefaultArtifact("org.example:project:1.0.0"), SYMBOLIC_NAME)));
        scenarios.add(scenario(EXPECTED, SYMBOLIC_NAME,
                coordinateWithSymbolicName(new DefaultArtifact("org.example:project:1.0"), SYMBOLIC_NAME)));
        scenarios.add(scenario(EXPECTED, SYMBOLIC_NAME,
                coordinateWithSymbolicName(new DefaultArtifact("org.example:project:1.0.0.rc1"), SYMBOLIC_NAME)));

        return scenarios;
    }

    private static Object[] scenario(ProjectCoordinate expected, String symbolicName, Document... indexContents) {
        return new Object[] { expected, symbolicName, indexContents };
    }

    @Test
    public void test() throws Exception {
        Directory index = inMemoryIndex(indexContents);

        IModelIndex sut = new ModelIndex(index);
        sut.open();
        ProjectCoordinate pc = sut.suggestProjectCoordinateByArtifactId(symbolicName).get();
        sut.close();

        assertThat(pc, is(equalTo(expected)));
    }
}
