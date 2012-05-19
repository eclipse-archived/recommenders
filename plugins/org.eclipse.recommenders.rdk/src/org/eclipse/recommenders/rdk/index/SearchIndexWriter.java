/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rdk.index;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.lucene.document.Field.Index.NOT_ANALYZED;
import static org.apache.lucene.document.Field.Store.NO;
import static org.apache.lucene.document.Field.Store.YES;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.eclipse.recommenders.internal.rcp.repo.ModelRepositoryIndex;
import org.eclipse.recommenders.rdk.index.ModelDocuments.ModelDocument;
import org.eclipse.recommenders.rdk.utils.Artifacts;
import org.eclipse.recommenders.rdk.utils.Commands.CommandProvider;
import org.eclipse.recommenders.utils.Zips;
import org.eclipse.recommenders.utils.annotations.Provisional;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.io.Files;

@CommandProvider
@Provisional
public class SearchIndexWriter {
    private Logger log = LoggerFactory.getLogger(getClass());

    private final File workingDir;

    private final File in;

    private final File out;

    private IndexWriter writer;

    public static void main(String[] args) throws Exception {
        File in = new File("/Volumes/usb/juno-m6/m2/metadata.json");
        File out = new File("/Volumes/usb/juno-m6/m2/org/eclipse/recommenders/index/0.0.0/index-0.0.0.zip");
        new SearchIndexWriter(in, out).run();
    }

    public SearchIndexWriter(File in, File out) throws Exception {
        this(in, out, new File(Files.createTempDir(), "index"));
    }

    public SearchIndexWriter(File in, File out, File workingDir) throws Exception {
        this.in = in;
        this.out = out;
        this.workingDir = workingDir;
    }

    public void run() throws Exception {
        createIndexer();
        writeIndex();
        zipAndMoveIndex();
    }

    private void createIndexer() throws IOException {
        FileUtils.deleteDirectory(workingDir);

        FSDirectory directory = SimpleFSDirectory.open(workingDir);
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, new KeywordAnalyzer());
        writer = new IndexWriter(directory, conf);
    }

    private void writeIndex() throws CorruptIndexException, IOException {

        ModelDocuments contents = GsonUtil.deserialize(in, ModelDocuments.class);
        for (ModelDocument mDoc : contents.entries) {
            if (mDoc.models.isEmpty())
                continue;

            Document lDoc = new Document();
            lDoc.add(newStored(ModelRepositoryIndex.F_COORDINATE, mDoc.coordinate));

            for (String fingerprint : mDoc.fingerprints)
                lDoc.add(newSearchable(ModelRepositoryIndex.F_FINGERPRINTS, fingerprint));

            for (String symbolicName : mDoc.symbolicNames)
                lDoc.add(newSearchable(ModelRepositoryIndex.SYMBOLIC_NAMES, symbolicName));

            for (String modeCoordinate : mDoc.models) {
                Artifact a = Artifacts.asArtifact(modeCoordinate);
                lDoc.add(newSearchable(ModelRepositoryIndex.F_CLASSIFIER, a.getClassifier()));
                lDoc.add(newStored(a.getClassifier(), modeCoordinate));
            }
            writer.addDocument(lDoc);
        }
        closeQuietly(writer);
//        closeQuietly(writer.getDirectory());
        log.debug("Wrote index to {}.", workingDir);
    }

    private void zipAndMoveIndex() throws IOException {
        Files.createParentDirs(out);
        Zips.zip(workingDir, out);
        log.info("Saved zipped index to {}.", out);
    }

    private Field newSearchable(String name, String value) {
        return new Field(name, value, NO, NOT_ANALYZED);
    }

    private Field newStored(String fieldname, String fieldvalue) {
        return new Field(fieldname, fieldvalue, YES, NOT_ANALYZED);
    }
}
