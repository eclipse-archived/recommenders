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
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.lucene.queryParser.QueryParser.Operator.AND;
import static org.eclipse.recommenders.snipmatch.Location.*;
import static org.eclipse.recommenders.utils.Constants.DOT_JSON;
import static org.eclipse.recommenders.utils.Urls.mangle;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.eclipse.recommenders.utils.IOUtils;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
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

    private static final int MAX_SEARCH_RESULTS = 100;
    private static final int CACHE_SIZE = 200;

    private static final Set<String> EMPTY_STOPWORDS = emptySet();

    private static final String F_NAME = "name";
    private static final String F_DESCRIPTION = "description";
    private static final String F_EXTRA_SEARCH_TERM = "extra";
    private static final String F_TAG = "tag";
    private static final String F_PATH = "path";
    private static final String F_UUID = "uuid";
    private static final String F_LOCATION = "location";

    private static final float NAME_BOOST = 4.0f;
    private static final float DESCRIPTION_BOOST = 2.0f;
    private static final float EXTRA_SEARCH_TERM_BOOST = DESCRIPTION_BOOST;
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
    private final Similarity similarity;

    private final LoadingCache<File, Snippet> snippetCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE)
            .build(new CacheLoader<File, Snippet>() {

                @Override
                public Snippet load(File file) throws Exception {
                    Snippet snippet;
                    snippet = GsonUtil.deserialize(file, Snippet.class);
                    return snippet;
                }
            });

    public FileSnippetRepository(File basedir) {
        Preconditions.checkArgument(CACHE_SIZE > MAX_SEARCH_RESULTS,
                "The cache size needs to be larger than the maximum number of search results.");
        snippetsdir = new File(basedir, "snippets");
        indexdir = new File(basedir, "index");
        repoUrl = mangle(basedir.getAbsolutePath());

        analyzer = createAnalyzer();
        parser = createParser();
        similarity = new IgnoreDocFrequencySimilarity();

        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
    }

    private Analyzer createAnalyzer() {
        StandardAnalyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_35, EMPTY_STOPWORDS);
        Map<String, Analyzer> analyzers = Maps.newHashMap();
        analyzers.put(F_NAME, standardAnalyzer);
        analyzers.put(F_DESCRIPTION, standardAnalyzer);
        analyzers.put(F_EXTRA_SEARCH_TERM, standardAnalyzer);
        analyzers.put(F_TAG, standardAnalyzer);
        analyzers.put(F_UUID, new KeywordAnalyzer());
        return new PerFieldAnalyzerWrapper(new KeywordAnalyzer(), analyzers);
    }

    private QueryParser createParser() {
        String[] searchFields = new String[] { F_NAME, F_DESCRIPTION, F_EXTRA_SEARCH_TERM, F_TAG };
        Map<String, Float> boosts = ImmutableMap.of(F_NAME, NAME_BOOST, F_DESCRIPTION, DESCRIPTION_BOOST,
                F_EXTRA_SEARCH_TERM, EXTRA_SEARCH_TERM_BOOST, F_TAG, TAG_BOOST);
        QueryParser parser = new MultiFieldPrefixQueryParser(Version.LUCENE_35, searchFields, analyzer, boosts, F_NAME,
                F_DESCRIPTION, F_EXTRA_SEARCH_TERM);
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
            reader = IndexReader.open(directory);
        } finally {
            writeLock.unlock();
        }
    }

    public void index() throws IOException {
        writeLock.lock();
        try {
            File[] snippetFiles = snippetsdir.listFiles((FileFilter) new SuffixFileFilter(DOT_JSON));
            doIndex(snippetFiles);
        } finally {
            writeLock.unlock();
        }
    }

    private void doIndex(File[] snippetFiles) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);
        config.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(directory, config);
        try {
            snippetCache.invalidateAll();
            for (File snippetFile : snippetFiles) {
                try {
                    ISnippet snippet = snippetCache.get(snippetFile);
                    String path = snippetFile.getPath();
                    indexSnippet(writer, snippet, path);
                } catch (Exception e) {
                    log.error("Failed to index snippet in " + snippetFile, e);
                }
            }
        } finally {
            writer.close();
        }
        if (reader != null) {
            reader = IndexReader.openIfChanged(reader);
        }
    }

    private void indexSnippet(IndexWriter writer, ISnippet snippet, String path) throws CorruptIndexException,
            IOException {
        Document doc = new Document();

        doc.add(new Field(F_PATH, path, Store.YES, Index.NO));

        doc.add(new Field(F_UUID, snippet.getUuid().toString(), Store.NO, Index.NOT_ANALYZED));

        String name = snippet.getName();
        doc.add(new Field(F_NAME, name, Store.YES, Index.ANALYZED));

        String description = snippet.getDescription();
        doc.add(new Field(F_DESCRIPTION, description, Store.YES, Index.ANALYZED));

        for (String tag : snippet.getTags()) {
            doc.add(new Field(F_TAG, tag, Store.YES, Index.ANALYZED_NO_NORMS));
        }

        for (String extraSearchTerm : snippet.getExtraSearchTerms()) {
            doc.add(new Field(F_EXTRA_SEARCH_TERM, extraSearchTerm, Store.YES, Index.ANALYZED));
        }

        doc.add(new Field(F_LOCATION, getIndexString(snippet.getLocation()), Store.NO, Index.NOT_ANALYZED));

        writer.addDocument(doc);
    }

    private String getIndexString(Location location) {
        return location.name().toLowerCase().replace('_', '-');
    }

    @VisibleForTesting
    public boolean isOpen() {
        return timesOpened > 0;
    }

    private ImmutableSet<Recommendation<ISnippet>> getSnippets() {
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
    public List<Recommendation<ISnippet>> search(ISearchContext context) {
        if (isBlank(context.getSearchText()) && context.getLocation() == NONE) {
            return ImmutableList.copyOf(getSnippets());
        }
        return doSearch(context, Integer.MAX_VALUE);
    }

    @Override
    public List<Recommendation<ISnippet>> search(ISearchContext context, int maxResults) {
        if (isBlank(context.getSearchText())) {
            return Collections.emptyList();
        }
        return doSearch(context, Math.min(maxResults, MAX_SEARCH_RESULTS));
    }

    private List<Recommendation<ISnippet>> doSearch(ISearchContext context, int maxResults) {
        readLock.lock();
        try {
            Preconditions.checkState(isOpen());
            List<Recommendation<ISnippet>> results = Lists.newLinkedList();

            try {
                Map<File, Float> snippetFiles = searchSnippetFiles(context, maxResults);
                for (Entry<File, Float> entry : snippetFiles.entrySet()) {
                    ISnippet snippet = snippetCache.get(entry.getKey());
                    results.add(Recommendation.newRecommendation(snippet, entry.getValue()));
                }
            } catch (Exception e) {
                log.error("Exception occurred while searching the snippet index.", e);
            }
            return results;
        } finally {
            readLock.unlock();
        }
    }

    private Map<File, Float> searchSnippetFiles(ISearchContext context, int maxResults) {
        Map<File, Float> results = Maps.newLinkedHashMap();
        IndexSearcher searcher = null;
        try {
            Query q = parser.parse(createLuceneQuery(context));

            searcher = new IndexSearcher(reader);
            searcher.setSimilarity(similarity);
            float maxScore = 0;
            for (ScoreDoc hit : searcher.search(q, null, maxResults).scoreDocs) {
                Document doc = searcher.doc(hit.doc);
                results.put(new File(doc.get(F_PATH)), hit.score);
                if (hit.score > maxScore) {
                    maxScore = hit.score;
                }
            }
            return normalizeValues(results, maxScore);
        } catch (ParseException e) {
            // While typing, a user can easily create unparsable queries (temporarily)
            log.info("Failed to parse query", e);
        } catch (Exception e) {
            log.error("Exception occurred while searching the snippet index.", e);
        } finally {
            IOUtils.closeQuietly(searcher);
        }
        return results;
    }

    private String createLuceneQuery(ISearchContext context) {
        StringBuilder sb = new StringBuilder();

        // Remove trailing 'OR' & 'NOT' to prevent pairing with location constraint
        String userQuery = context.getSearchText().trim();
        userQuery = removeEnd(userQuery, " OR");
        userQuery = removeEnd(userQuery, " NOT");
        // Remove trailing ':' to prevent invalid lucene syntax
        userQuery = removeEnd(userQuery, ":");
        sb.append(userQuery);

        if (context.getLocation() == NONE) {
            return sb.toString();
        }

        sb.append(" (");
        sb.append(F_LOCATION);
        sb.append(":");
        sb.append(join(getLocation(context.getLocation()), " OR " + F_LOCATION + ":"));
        sb.append(")");

        return sb.toString();
    }

    private Collection<String> getLocation(Location location) {
        Collection<String> result = Sets.newHashSet(getIndexString(location));

        switch (location) {
        case JAVA_STATEMENTS:
        case JAVA_TYPE_MEMBERS:
            result.add(getIndexString(JAVA));
        case JAVA:
        case JAVADOC:
            result.add(getIndexString(FILE));
            return result;
        default:
            return result;
        }
    }

    private Map<File, Float> normalizeValues(Map<File, Float> results, final float maxScore) {
        return Maps.transformValues(results, new Function<Float, Float>() {

            @Override
            public Float apply(Float input) {
                return input / maxScore;
            }
        });
    }

    @Override
    public boolean hasSnippet(UUID uuid) {
        readLock.lock();
        try {
            Preconditions.checkState(isOpen());

            return !searchSnippetFiles(new SearchContext(F_UUID + ":" + uuid), Integer.MAX_VALUE).isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean delete(UUID uuid) throws IOException {
        writeLock.lock();
        try {
            Preconditions.checkState(isOpen());
            Map<File, Float> snippetFiles = searchSnippetFiles(new SearchContext(F_UUID + ":" + uuid),
                    Integer.MAX_VALUE);
            if (snippetFiles.isEmpty()) {
                return false;
            }
            Iterables.getOnlyElement(snippetFiles.keySet()).delete();
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
    public void close() {
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
            Map<File, Float> snippetFiles = searchSnippetFiles(
                    new SearchContext(F_UUID + ":" + importSnippet.getUuid()), Integer.MAX_VALUE);
            if (snippetFiles.isEmpty()) {
                file = new File(snippetsdir, importSnippet.getUuid() + DOT_JSON);
            } else {
                file = Iterables.getOnlyElement(snippetFiles.keySet());
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

    private static class IgnoreDocFrequencySimilarity extends DefaultSimilarity {

        private static final long serialVersionUID = 6048878092975074153L;

        @Override
        public float tf(float freq) {
            return 1.0f;
        }

        @Override
        public float idf(int docFreq, int numDocs) {
            return 1.0f;
        }
    }
}
