package org.eclipse.recommenders.models;

import static org.eclipse.recommenders.utils.Artifacts.newArtifact;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.recommenders.utils.Constants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class ModelIndexTest {

    private static final String SYMBOLIC_NAME = "org.example.project";

    private static final ProjectCoordinate EXPECTED = new ProjectCoordinate("org.example", "project", "1.0.0");

    private final ProjectCoordinate expected;
    private final String symbolicName;
    private final Document[] indexContents;

    public ModelIndexTest(ProjectCoordinate expected, String symbolicName, Document... indexContents) {
        this.expected = expected;
        this.symbolicName = symbolicName;
        this.indexContents = indexContents;
    }

    @Parameters
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(scenario(EXPECTED, SYMBOLIC_NAME,
                coordinateWithSymbolicName(newArtifact("org.example:project:1.0.0"), SYMBOLIC_NAME)));
        scenarios.add(scenario(EXPECTED, SYMBOLIC_NAME,
                coordinateWithSymbolicName(newArtifact("org.example:project:1.0"), SYMBOLIC_NAME)));
        scenarios.add(scenario(EXPECTED, SYMBOLIC_NAME,
                coordinateWithSymbolicName(newArtifact("org.example:project:1.0.0.rc1"), SYMBOLIC_NAME)));

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

    private Directory inMemoryIndex(Document... documents) throws Exception {
        RAMDirectory directory = new RAMDirectory();
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, new KeywordAnalyzer());
        IndexWriter writer = new IndexWriter(directory, conf);
        for (Document document : documents) {
            writer.addDocument(document);
        }
        writer.close();
        return directory;
    }

    private static Document coordinateWithSymbolicName(Artifact coordinate, String symbolicName) {
        Document doc = new Document();
        doc.add(newStored(Constants.F_COORDINATE, coordinate.toString()));
        doc.add(newStored(Constants.F_SYMBOLIC_NAMES, symbolicName));
        return doc;
    }

    private static Field newStored(String key, String value) {
        return new Field(key, value, Store.YES, Index.NOT_ANALYZED);
    }
}
