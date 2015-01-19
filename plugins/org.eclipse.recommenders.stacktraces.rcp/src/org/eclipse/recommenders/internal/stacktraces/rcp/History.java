/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static com.google.common.base.Charsets.UTF_8;
import static org.apache.lucene.index.IndexReader.openIfChanged;
import static org.eclipse.recommenders.internal.stacktraces.rcp.LogMessages.HISTORY_NOT_AVAILABLE;
import static org.eclipse.recommenders.utils.Logs.log;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReports;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.StackTraceElement;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.VisitorImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.AbstractIdleService;

public class History extends AbstractIdleService {

    private static final String F_VERSION = "version";
    private static final String F_IDENTITY = "identity";
    private static final String F_IDENTITY_TRACE = "identity-trace";
    private Directory index;
    private IndexWriter writer;
    private IndexReader reader;
    private IndexSearcher searcher;

    @VisibleForTesting
    protected Directory createIndexDirectory() throws IOException {
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        IPath stateLocation = Platform.getStateLocation(bundle);
        File indexdir = new File(stateLocation.toFile(), "history");
        indexdir.mkdirs();
        return FSDirectory.open(indexdir);
    }

    @Override
    protected void startUp() throws Exception {
        index = createIndexDirectory();
        createWriter();
        createReaderAndSearcher();
    }

    private void createWriter() throws CorruptIndexException, LockObtainFailedException, IOException {
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, new KeywordAnalyzer());
        conf.setOpenMode(OpenMode.CREATE_OR_APPEND);
        writer = new IndexWriter(index, conf);
        // to build an initial index if empty:
        if (writer.numDocs() == 0) {
            buildInitialIndex();
        }
    }

    private void buildInitialIndex() throws CorruptIndexException, IOException {
        Document meta = new Document();
        meta.add(new Field(F_VERSION, Constants.VERSION, Store.YES, Index.NO));
        writer.addDocument(meta);
        writer.commit();
    }

    private void createReaderAndSearcher() throws CorruptIndexException, IOException {
        reader = IndexReader.open(index);
        searcher = new IndexSearcher(reader);
    }

    public boolean seen(ErrorReport report) {
        return seen(new TermQuery(new Term(F_IDENTITY, identity(report))));
    }

    public boolean seenSimilar(ErrorReport report) {
        return seen(new TermQuery(new Term(F_IDENTITY_TRACE, identityTrace(report))));
    }

    private boolean seen(Query q) {
        try {
            renewReaderAndSearcher();
            TopDocs results = searcher.search(q, 1);
            boolean foundIdenticalReport = results.totalHits > 0;
            return foundIdenticalReport;
        } catch (Exception e) {
            log(HISTORY_NOT_AVAILABLE, e);
            return false;
        }
    }

    private String identity(ErrorReport report) {
        ErrorReport copy = ErrorReports.copy(report);
        copy.setEventId(null);
        Settings settings = PreferenceInitializer.readSettings();
        String json = ErrorReports.toJson(copy, settings, false);
        String hash = Hashing.murmur3_128().newHasher().putString(json, UTF_8).hash().toString();
        return hash;
    }

    private String identityTrace(ErrorReport report) {
        final Hasher hasher = Hashing.murmur3_128().newHasher();
        report.accept(new VisitorImpl() {

            @Override
            public void visit(StackTraceElement element) {
                hasher.putString(element.getClassName(), Charsets.UTF_8);
                hasher.putString(element.getMethodName(), Charsets.UTF_8);
                hasher.putInt(element.getLineNumber());
            }
        });
        String hash = hasher.hash().toString();
        return hash;
    }

    private void renewReaderAndSearcher() throws IOException {
        IndexReader tmp = openIfChanged(reader);
        if (tmp != null) {
            IOUtils.close(reader, searcher);
            searcher = new IndexSearcher(tmp);
            reader = tmp;
        }
    };

    public void remember(Iterable<ErrorReport> reports) {
        for (ErrorReport report : reports) {
            remember(report);
        }
    }

    public void remember(ErrorReport report) {
        if (seen(report)) {
            return;
        }
        Document doc = new Document();
        Field field = new Field(F_IDENTITY, identity(report), Store.NO, Index.NOT_ANALYZED);
        doc.add(field);
        if (report.isIgnoreSimilar()) {
            field = new Field(F_IDENTITY_TRACE, identityTrace(report), Store.NO, Index.NOT_ANALYZED);
            doc.add(field);
        }
        try {
            writer.addDocument(doc);
            writer.commit();
        } catch (Exception e) {
            log(HISTORY_NOT_AVAILABLE, e);
        }
    };

    @Override
    protected void shutDown() throws Exception {
        IOUtils.close(searcher, reader, writer, index);
    }
}
