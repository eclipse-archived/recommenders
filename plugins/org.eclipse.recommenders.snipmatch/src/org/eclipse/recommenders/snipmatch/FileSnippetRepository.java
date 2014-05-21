/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - introduce importSnippet()
 */
package org.eclipse.recommenders.snipmatch;

import static com.google.common.collect.ImmutableSet.copyOf;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.lucene.queryParser.QueryParser.Operator.AND;
import static org.eclipse.recommenders.utils.Constants.DOT_JSON;
import static org.eclipse.recommenders.utils.Urls.mangle;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.eclipse.recommenders.utils.IOUtils;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class FileSnippetRepository implements ISnippetRepository {

    private static final Set<String> EMPTY_STOPWORDS = emptySet();

    private static final String F_NAME = "name";
    private static final String F_DESCRIPTION = "description";
    private static final String F_KEYWORD = "keyword";
    private static final String F_TAG = "tag";
    private static final String F_PATH = "path";
    private static final String F_UUID = "uuid";

    private static final float NAME_BOOST = 1.2f;
    private static final float DESCRIPTION_BOOST = 1.1f;
    private static final float KEYWORD_BOOST = DESCRIPTION_BOOST;
    private static final float TAG_BOOST = 1.0f;

    private Logger log = LoggerFactory.getLogger(getClass());

    private volatile int timesOpened = 0;

    private final Lock readLock;
    private final Lock writeLock;

    private final File snippetsdir;
    private final File indexdir;
    private final String repoUrl;

    private Directory directory;
    private IndexReader reader;

    private final Analyzer analyzer;
    private final QueryParser parser;

    private LoadingCache<File, Snippet> snippetCache = CacheBuilder.newBuilder().maximumSize(200)
            .build(new CacheLoader<File, Snippet>() {

                @Override
                public Snippet load(File file) throws Exception {
                    Snippet snippet;
                    snippet = GsonUtil.deserialize(file, Snippet.class);
                    return snippet;
                }
            });

    public FileSnippetRepository(File basedir) {
        snippetsdir = new File(basedir, "snippets");
        indexdir = new File(basedir, "index");
        repoUrl = mangle(basedir.getAbsolutePath());

        analyzer = createAnalyzer();
        parser = createParser();

        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
    }

    private Analyzer createAnalyzer() {
        StandardAnalyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_35, EMPTY_STOPWORDS);
        Map<String, Analyzer> analyzers = Maps.newHashMap();
        analyzers.put(F_NAME, standardAnalyzer);
        analyzers.put(F_DESCRIPTION, standardAnalyzer);
        analyzers.put(F_KEYWORD, standardAnalyzer);
        analyzers.put(F_TAG, standardAnalyzer);
        analyzers.put(F_UUID, new KeywordAnalyzer());
        return new PerFieldAnalyzerWrapper(new KeywordAnalyzer(), analyzers);
    }

    private QueryParser createParser() {
        String[] searchFields = new String[] { F_NAME, F_DESCRIPTION, F_KEYWORD, F_TAG };
        Map<String, Float> boosts = ImmutableMap.of(F_NAME, NAME_BOOST, F_DESCRIPTION, DESCRIPTION_BOOST, F_KEYWORD,
                KEYWORD_BOOST, F_TAG, TAG_BOOST);
        QueryParser parser = new MultiFieldPrefixQueryParser(Version.LUCENE_35, searchFields, analyzer, boosts, F_NAME,
                F_DESCRIPTION, F_KEYWORD, F_TAG);
        parser.setDefaultOperator(AND);
        return parser;
    }

    @Override
    public void open() throws IOException {
        writeLock.lock();
        try {
            timesOpened++;
            if (timesOpened > 1) {
                return;
            }
            snippetsdir.mkdirs();
            indexdir.mkdirs();
            directory = FSDirectory.open(indexdir);
            index();
            reader = IndexReader.open(FSDirectory.open(indexdir));
        } finally {
            writeLock.unlock();
        }
    }

    public void index() throws IOException {
        writeLock.lock();
        try {
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);
            config.setOpenMode(OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(directory, config);
            snippetCache.invalidateAll();

            for (File fSnippet : snippetsdir.listFiles((FileFilter) new SuffixFileFilter(DOT_JSON))) {
                try {
                    Snippet snippet = snippetCache.get(fSnippet);
                    Document doc = new Document();

                    doc.add(new Field(F_PATH, fSnippet.getPath(), Store.YES, Index.NO));

                    doc.add(new Field(F_UUID, snippet.getUuid().toString(), Store.NO, Index.NOT_ANALYZED));

                    String name = snippet.getName();
                    doc.add(new Field(F_NAME, name, Store.YES, Index.ANALYZED));

                    String description = snippet.getDescription();
                    doc.add(new Field(F_DESCRIPTION, description, Store.YES, Index.ANALYZED));

                    for (String tag : snippet.getTags()) {
                        doc.add(new Field(F_TAG, tag, Store.YES, Index.ANALYZED));
                    }

                    for (String keyword : snippet.getKeywords()) {
                        doc.add(new Field(F_KEYWORD, keyword, Store.YES, Index.ANALYZED));
                    }

                    writer.addDocument(doc);
                } catch (Exception e) {
                    log.error("Failed to index snippet in " + fSnippet, e);
                }
            }
            writer.close();
            if (reader != null) {
                reader = IndexReader.openIfChanged(reader);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @VisibleForTesting
    public boolean isOpen() {
        return timesOpened > 0;
    }

    @Override
    public ImmutableSet<Recommendation<ISnippet>> getSnippets() {
        readLock.lock();
        try {
            Preconditions.checkState(isOpen());
            // TODO MB: this is a costly operation that works only well with small repos.
            Set<Recommendation<ISnippet>> res = Sets.newHashSet();
            for (File fSnippet : snippetsdir.listFiles((FileFilter) new SuffixFileFilter(DOT_JSON))) {
                try {
                    ISnippet snippet = snippetCache.get(fSnippet);
                    res.add(Recommendation.newRecommendation(snippet, 0));
                } catch (Exception e) {
                    log.error("Error while loading snippet from file {}", fSnippet.getAbsolutePath(), e);
                }
            }
            return copyOf(res);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Recommendation<ISnippet>> search(String query) {
        readLock.lock();
        try {
            Preconditions.checkState(isOpen());
            List<Recommendation<ISnippet>> results = Lists.newLinkedList();

            if (isBlank(query)) {
                return ImmutableList.copyOf(getSnippets());
            }

            try {
                for (File file : searchSnippetFiles(query)) {
                    if (file.exists()) {
                        ISnippet snippet = snippetCache.get(file);
                        results.add(Recommendation.newRecommendation(snippet, 0));
                    }
                }
            } catch (Exception e) {
                log.error("Exception occurred while searching the snippet index.", e);
            }
            return results;
        } finally {
            readLock.unlock();
        }
    }

    private List<File> searchSnippetFiles(String query) {
        List<File> results = Lists.newLinkedList();
        IndexSearcher searcher = null;
        try {
            Query q = parser.parse(query);

            searcher = new IndexSearcher(reader);
            for (ScoreDoc hit : searcher.search(q, null, 100).scoreDocs) {
                Document doc = searcher.doc(hit.doc);
                results.add(new File(doc.get(F_PATH)));
            }
        } catch (ParseException e) {
            log.error("Failed to parse query", e);
        } catch (Exception e) {
            log.error("Exception occurred while searching the snippet index.", e);
        } finally {
            IOUtils.closeQuietly(searcher);
        }
        return results;
    }

    @Override
    public boolean hasSnippet(UUID uuid) {
        readLock.lock();
        try {
            Preconditions.checkState(isOpen());
            List<File> search = searchSnippetFiles(F_UUID + ":" + uuid);
            return search.size() >= 1;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean delete(UUID uuid) throws IOException {
        writeLock.lock();
        try {
            Preconditions.checkState(isOpen());
            List<File> files = searchSnippetFiles(F_UUID + ":" + uuid);
            if (files.isEmpty()) {
                return false;
            }
            Iterables.getOnlyElement(files).delete();
            index();
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean isDeleteSupported() {
        return true;
    }

    @Override
    public String getRepositoryLocation() {
        return repoUrl;
    }

    @Override
    public void close() throws IOException {
        writeLock.lock();
        try {
            if (timesOpened == 0) {
                return;
            } else if (timesOpened > 1) {
                timesOpened--;
                return;
            } else if (timesOpened == 1) {
                timesOpened = 0;
                IOUtils.closeQuietly(reader);
                IOUtils.closeQuietly(directory);
                reader = null;
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void importSnippet(ISnippet snippet) throws IOException {
        writeLock.lock();
        try {
            Preconditions.checkState(isOpen());
            Snippet importSnippet = checkTypeAndConvertSnippet(snippet);

            File file;
            List<File> files = searchSnippetFiles(F_UUID + ":" + importSnippet.getUuid());
            if (files.isEmpty()) {
                file = new File(snippetsdir, importSnippet.getUuid() + DOT_JSON);
            } else {
                file = Iterables.getOnlyElement(files);
            }

            FileWriter writer = new FileWriter(file);
            writer.write(GsonUtil.serialize(importSnippet));
            writer.flush();
            writer.close();

            index();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean isImportSupported() {
        return true;
    }

    private Snippet checkTypeAndConvertSnippet(ISnippet snippet) {
        if (snippet instanceof Snippet) {
            return (Snippet) snippet;
        } else {
            return Snippet.copy(snippet);
        }
    }
}
