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
package org.eclipse.recommenders.internal.types.rcp;

import static com.google.common.base.Objects.equal;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.lucene.document.Field.Index.NOT_ANALYZED;
import static org.apache.lucene.search.NumericRangeQuery.newLongRange;
import static org.eclipse.jdt.core.IJavaElement.PACKAGE_FRAGMENT_ROOT;
import static org.eclipse.recommenders.internal.types.rcp.l10n.LogMessages.ERROR_ACCESSING_SEARCHINDEX_FAILED;
import static org.eclipse.recommenders.jdt.JavaElementsFinder.*;
import static org.eclipse.recommenders.utils.Logs.log;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.recommenders.internal.types.rcp.l10n.LogMessages;
import org.eclipse.recommenders.internal.types.rcp.l10n.Messages;
import org.eclipse.recommenders.jdt.JavaElementsFinder;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.Names;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.AbstractIdleService;

public class ProjectTypesIndex extends AbstractIdleService implements IProjectTypesIndex {

    private static final int TICKS = 80000;

    private static final String F_PACAKGE_FRAGEMENT_ROOT_TYPE = "pfrType"; //$NON-NLS-1$

    private static final String F_NAME = "name"; //$NON-NLS-1$
    private static final String F_SIMPLE_NAME = "simpleName"; //$NON-NLS-1$
    private static final String F_LAST_MODIFIED = "lastModified"; //$NON-NLS-1$
    private static final String F_LOCATION = "location"; //$NON-NLS-1$
    private static final String F_INSTANCEOF = "instanceof"; //$NON-NLS-1$

    private static final String V_JAVA_LANG_OBJECT = "java.lang.Object"; //$NON-NLS-1$
    private static final String V_ARCHIVE = "archive"; //$NON-NLS-1$

    private static final TermQuery TERM_QUERY_PACKAGE_FRAGMENT_ROOT_TYPE = new TermQuery(
            new Term(F_PACAKGE_FRAGEMENT_ROOT_TYPE, V_ARCHIVE));

    private final IJavaProject project;
    private final File indexDir;

    private Directory directory;
    private IndexWriter writer;
    private IndexReader reader;

    private IndexSearcher searcher;

    private JobFuture activeRebuild = null;
    private boolean rebuildAfterNextAccess;

    public ProjectTypesIndex(IJavaProject project, File indexDir) {
        this(project, indexDir, true);
    }

    @VisibleForTesting
    ProjectTypesIndex(IJavaProject project, File indexDir, boolean startService) {
        this.project = project;
        this.indexDir = indexDir;
        if (startService) {
            startAsync();
        }
    }

    @Override
    protected void startUp() throws Exception {
        initialize();
        if (needsRebuild()) {
            rebuild();
        }
    }

    @VisibleForTesting
    void initialize() throws IOException {
        directory = FSDirectory.open(indexDir);
        if (IndexWriter.isLocked(directory)) {
            IndexWriter.unlock(directory);
        }
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, new KeywordAnalyzer());
        writer = new IndexWriter(directory, conf);
        writer.commit();
    }

    private boolean needsRebuild() {
        List<IPackageFragmentRoot> roots = findArchivePackageFragmentRoots();
        StringBuilder sb = new StringBuilder();
        try {
            Set<File> indexedRoots = getIndexedRoots();

            for (IPackageFragmentRoot root : roots) {
                File location = JavaElementsFinder.findLocation(root).orNull();
                if (!indexedRoots.remove(location)) {
                    // this root was unknown:
                    sb.append("  [+] ").append(location).append('\n'); //$NON-NLS-1$
                } else if (!isCurrent(root)) {
                    // this root's timestamp is different to what we indexed before:
                    sb.append("  [*] ").append(location).append('\n'); //$NON-NLS-1$
                }
            }
            if (!indexedRoots.isEmpty()) {
                // there is a root that we did not index before:
                for (File file : indexedRoots) {
                    sb.append("  [-] ").append(file.getAbsolutePath()).append('\n'); //$NON-NLS-1$
                }

            }
        } catch (IOException e) {
            Logs.log(LogMessages.ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
        }
        if (sb.length() > 0) {
            Logs.log(LogMessages.INFO_REINDEXING_REQUIRED, sb.toString());
            return true;
        }
        return false;
    }

    private List<IPackageFragmentRoot> findArchivePackageFragmentRoots() {
        Iterable<IPackageFragmentRoot> filter = Iterables.filter(JavaElementsFinder.findPackageFragmentRoots(project),
                new ArchiveFragmentRootsOnlyPredicate());
        return Ordering.usingToString().sortedCopy(filter);
    }

    private Set<File> getIndexedRoots() throws IOException {
        Set<File> res = Sets.newHashSet();
        IndexSearcher searcher = getSearcher();
        TopDocs topDocs = searcher.search(TERM_QUERY_PACKAGE_FRAGMENT_ROOT_TYPE, Integer.MAX_VALUE);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            File location = new File(doc.get(F_LOCATION));
            res.add(location);
        }
        return res;
    }

    private boolean isCurrent(IPackageFragmentRoot root) throws IOException {
        File rootLocation = JavaElementsFinder.findLocation(root).orNull();

        BooleanQuery query = new BooleanQuery();
        query.add(new TermQuery(termLocation(rootLocation)), Occur.MUST);
        query.add(newLongRange(F_LAST_MODIFIED, rootLocation.lastModified(), rootLocation.lastModified(), true, true),
                Occur.MUST);

        IndexSearcher searcher = getSearcher();
        return searcher.search(query, 1).totalHits > 0;
    }

    private Term termLocation(File rootLocation) {
        return new Term(F_LOCATION, rootLocation.getAbsolutePath());
    }

    @Override
    public void close() throws IOException {
        stopAsync();
        awaitTerminated();
    }

    @Override
    protected void shutDown() throws Exception {
        cancelRebuild();
        IOUtils.close(reader, writer, directory);
    }

    @Override
    public ImmutableSet<String> subtypes(ITypeName expected, String prefix) {
        try {
            return subtypes(Names.vm2srcQualifiedType(expected), prefix);
        } catch (Exception e) {
            // temporary workaround for
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=464925
            log(LogMessages.ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
            return ImmutableSet.of();
        }
    }

    private ImmutableSet<String> subtypes(String type, String prefix) {
        ImmutableSet.Builder<String> b = ImmutableSet.builder();
        if (!isRunning() || isBlank(type)) {
            return b.build();
        }

        IndexSearcher searcher = getSearcher();
        BooleanQuery query = new BooleanQuery();
        query.add(new TermQuery(new Term(F_INSTANCEOF, type)), Occur.MUST);
        if (isNotBlank(prefix)) {
            query.add(new WildcardQuery(new Term(F_SIMPLE_NAME, prefix + '*')), Occur.MUST);
        }
        try {
            TopDocs search = searcher.search(query, Integer.MAX_VALUE);
            for (ScoreDoc sdoc : search.scoreDocs) {
                Document doc = searcher.doc(sdoc.doc);
                String name = doc.get(F_NAME);
                b.add(name);
            }
        } catch (Exception e) {
            log(LogMessages.ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
        }

        // check whether the index was flagged as 'needs a rebuild':
        if (isRebuildAfterNextAccess()) {
            setRebuildAfterNextAccess(false);
            rebuild();
        }

        return b.build();
    }

    private IndexSearcher getSearcher() {
        if (reader == null) {
            reader = createReader();
            searcher = new IndexSearcher(reader);
            return searcher;
        }
        try {
            IndexReader newReader = IndexReader.openIfChanged(reader);
            if (newReader != null) {
                reader.close();
                reader = newReader;
                searcher = new IndexSearcher(reader);
            }
        } catch (Exception e) {
            log(ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
        }
        return searcher;
    }

    private IndexReader createReader() {
        try {
            return IndexReader.open(directory);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private void clear() {
        try {
            writer.deleteAll();
        } catch (Exception e) {
            log(ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
        }
    }

    private void rebuild() {
        cancelRebuild();
        final JobFuture res = new JobFuture();
        activeRebuild = res;
        Job job = new Job(MessageFormat.format(Messages.JOB_NAME_INDEXING, project.getElementName())) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                SubMonitor progress = SubMonitor.convert(monitor,
                        MessageFormat.format(Messages.MONITOR_NAME_INDEXING, project.getElementName()), TICKS);
                Thread thread = Thread.currentThread();
                int priority = thread.getPriority();
                try {
                    thread.setPriority(Thread.MIN_PRIORITY);
                    clear();
                    rebuild(progress);
                    commit();
                } catch (OperationCanceledException e) {
                    res.setException(e);
                    res.setResult(Status.CANCEL_STATUS);
                } catch (Exception e) {
                    res.setException(e);
                    res.setResult(new Status(IStatus.ERROR, Constants.BUNDLE_ID, e.getMessage(), e));
                } finally {
                    thread.setPriority(priority);
                    monitor.done();
                }
                res.setResult(Status.OK_STATUS);
                return Status.OK_STATUS;
            }
        };
        res.setJob(job);
        job.schedule(2000);
    }

    private synchronized void rebuild(SubMonitor progress) {
        List<IPackageFragmentRoot> roots = findArchivePackageFragmentRoots();
        for (IPackageFragmentRoot root : roots) {
            progress.subTask(root.getElementName());
            ImmutableList<IType> types = findTypes(root);
            for (IType type : types) {
                if (progress.isCanceled()) {
                    setRebuildAfterNextAccess(true);
                    throw new OperationCanceledException();
                }
                indexType(type, progress.newChild(1));
            }
            File location = JavaElementsFinder.findLocation(root).orNull();
            if (location != null) {
                registerArchivePackageFragmentRoot(location);
            }
            commit();
        }
        progress.done();
    }

    private void cancelRebuild() {
        if (!(activeRebuild == null || activeRebuild.isDone() || activeRebuild.isCancelled())) {
            activeRebuild.cancel(true);
        }
    }

    private void registerArchivePackageFragmentRoot(File location) {
        Document doc = new Document();
        doc.add(new Field(F_PACAKGE_FRAGEMENT_ROOT_TYPE, V_ARCHIVE, Store.NO, Index.NOT_ANALYZED));
        doc.add(new Field(F_LOCATION, location.getAbsolutePath(), Store.YES, Index.NOT_ANALYZED));
        doc.add(new NumericField(F_LAST_MODIFIED, Store.YES, true).setLongValue(location.lastModified()));
        try {
            writer.addDocument(doc);
        } catch (Exception e) {
            Logs.log(ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
        }
    }

    private void commit() {
        try {
            writer.commit();
        } catch (Exception e) {
            log(ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
        }
    }

    private void indexType(IType type, IProgressMonitor monitor) {
        Document doc = new Document();
        {
            // name:
            doc.add(new Field(F_NAME, type.getFullyQualifiedName(), Store.YES, NOT_ANALYZED));
            doc.add(new Field(F_SIMPLE_NAME, type.getElementName(), Store.NO, NOT_ANALYZED));
        }
        {
            // location:
            File location = findPackageFragmentRoot(type).orNull();
            if (location != null) {
                doc.add(new Field(F_LOCATION, location.getAbsolutePath(), Store.NO, Index.NOT_ANALYZED));
            }
        }
        {
            doc.add(new Field(F_INSTANCEOF, type.getFullyQualifiedName(), Store.NO, NOT_ANALYZED));

        }
        {
            // extends:
            try {
                ITypeHierarchy h = type.newSupertypeHierarchy(null);
                for (IType supertypes : h.getAllSupertypes(type)) {
                    String fullyQualifiedName = supertypes.getFullyQualifiedName();
                    if (equal(V_JAVA_LANG_OBJECT, fullyQualifiedName)) {
                        continue;
                    }
                    doc.add(new Field(F_INSTANCEOF, fullyQualifiedName, Store.NO, NOT_ANALYZED));
                }
            } catch (Exception e) {
                log(ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
            }
        }
        addDocument(doc, monitor);
    }

    private void addDocument(Document doc, IProgressMonitor monitor) {
        try {
            if (!monitor.isCanceled()) {
                writer.addDocument(doc);
            }
        } catch (Exception e) {
            log(ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
        } finally {
            monitor.done();
        }
    }

    private Optional<File> findPackageFragmentRoot(IType type) {
        IPackageFragmentRoot ancestor = (IPackageFragmentRoot) type.getAncestor(PACKAGE_FRAGMENT_ROOT);
        return findLocation(ancestor);
    }

    @Override
    public void suggestRebuild() {
        setRebuildAfterNextAccess(needsRebuild());
    }

    private void setRebuildAfterNextAccess(boolean value) {
        rebuildAfterNextAccess = value;
    }

    private boolean isRebuildAfterNextAccess() {
        return rebuildAfterNextAccess;
    }

    @Override
    public void delete() {
        stopAsync();
        awaitTerminated();
        deleteQuietly(indexDir);
    }

    private static final class ArchiveFragmentRootsOnlyPredicate implements Predicate<IPackageFragmentRoot> {

        @Override
        public boolean apply(IPackageFragmentRoot input) {
            if (input == null) {
                return false;
            }
            if (!input.isArchive()) {
                return false;
            }
            File location = JavaElementsFinder.findLocation(input).orNull();
            if (location == null) {
                return false;
            }
            return true;
        }
    }

    private static final class JobFuture extends AbstractFuture<IStatus> {

        private Job job;

        public void setJob(Job job) {
            this.job = job;
        }

        public boolean setResult(IStatus value) {
            return super.set(value);
        }

        @Override
        public boolean setException(Throwable throwable) {
            return super.setException(throwable);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (job != null) {
                return job.cancel();
            }
            return false;
        }

    }
}
