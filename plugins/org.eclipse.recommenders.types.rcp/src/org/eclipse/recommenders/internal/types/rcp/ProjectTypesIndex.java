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
import static java.lang.Long.parseLong;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.lucene.document.Field.Index.NOT_ANALYZED;
import static org.eclipse.jdt.core.IJavaElement.PACKAGE_FRAGMENT_ROOT;
import static org.eclipse.recommenders.internal.types.rcp.LogMessages.ERROR_ACCESSING_SEARCHINDEX_FAILED;
import static org.eclipse.recommenders.jdt.JavaElementsFinder.*;
import static org.eclipse.recommenders.utils.Logs.log;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
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
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.recommenders.internal.types.rcp.l10n.Messages;
import org.eclipse.recommenders.jdt.JavaElementsFinder;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.Names;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.ListenableFuture;

public class ProjectTypesIndex extends AbstractIdleService {

    private static final int TICKS = 80000;

    private static final String F_PACAKGE_FRAGEMENT_ROOT_TYPE = "pfrType"; //$NON-NLS-1$

    private static final String F_NAME = "name"; //$NON-NLS-1$
    private static final String F_SIMPLE_NAME = "simpleName"; //$NON-NLS-1$
    private static final String F_LAST_MODIFIED = "lastModified"; //$NON-NLS-1$
    private static final String F_LOCATION = "location"; //$NON-NLS-1$
    private static final String F_INSTANCEOF = "instanceof"; //$NON-NLS-1$

    private static final String V_JAVA_LANG_OBJECT = "java.lang.Object"; //$NON-NLS-1$
    private static final String V_ARCHIVE = "archive"; //$NON-NLS-1$

    private IJavaProject project;
    private File indexDir;

    private Directory directory;
    private IndexWriter writer;
    private IndexReader reader;

    private IndexSearcher searcher;

    public ProjectTypesIndex(IJavaProject project, File indexDir) {
        this.project = project;
        this.indexDir = indexDir;
    }

    @Override
    protected void startUp() throws Exception {
        directory = FSDirectory.open(indexDir);
        if (IndexWriter.isLocked(directory)) {
            IndexWriter.unlock(directory);
        }
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, new KeywordAnalyzer());
        writer = new IndexWriter(directory, conf);
        writer.commit();

        if (needsRebuild()) {
            rebuild();
        }
    }

    public boolean needsRebuild() {
        StringBuilder sb = new StringBuilder();
        try {
            List<IPackageFragmentRoot> roots = findArchivePackageFragmentRoots();
            Map<File, Long> indexedRoots = getSavedState();

            for (IPackageFragmentRoot root : roots) {
                File location = JavaElementsFinder.findLocation(root).orNull();
                if (indexedRoots.remove(location) == null) {
                    // this root was unknown:
                    sb.append("  [+] ").append(location).append('\n');
                } else if (!isCurrent(root)) {
                    // this root's timestamp is different to what we indexed before:
                    sb.append("  [*] ").append(location).append('\n');
                }
            }
            if (!indexedRoots.isEmpty()) {
                // there is a root that we did not index before:
                for (File file : indexedRoots.keySet()) {
                    sb.append("  [-] ").append(file.getAbsolutePath()).append('\n');
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

    private Map<File, Long> getSavedState() throws IOException {
        Map<File, Long> res = Maps.newHashMap();
        IndexSearcher searcher = getSearcher();
        TopDocs topDocs = searcher.search(new TermQuery(termPackageFragmentRootType()), Integer.MAX_VALUE);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            File location = new File(doc.get(F_LOCATION));
            Long lastModified = parseLong(doc.get(F_LAST_MODIFIED));
            res.put(location, lastModified);
        }
        return res;
    }

    private Term termPackageFragmentRootType() {
        return new Term(F_PACAKGE_FRAGEMENT_ROOT_TYPE, V_ARCHIVE);
    }

    @SuppressWarnings("unused")
    private boolean isIndexed(IPackageFragmentRoot root) throws IOException {
        File rootLocation = JavaElementsFinder.findLocation(root).orNull();
        TermQuery query = new TermQuery(termLocation(rootLocation));
        IndexSearcher searcher = getSearcher();
        return searcher.search(query, 1).totalHits > 0;
    }

    private boolean isCurrent(IPackageFragmentRoot root) throws IOException {
        File rootLocation = JavaElementsFinder.findLocation(root).orNull();

        BooleanQuery query = new BooleanQuery();
        query.add(new TermQuery(termLocation(rootLocation)), Occur.MUST);
        query.add(NumericRangeQuery.newLongRange(F_LAST_MODIFIED, rootLocation.lastModified(),
                rootLocation.lastModified(), true, true), Occur.MUST);

        IndexSearcher searcher = getSearcher();
        return searcher.search(query, 1).totalHits > 0;
    }

    private Term termLocation(File rootLocation) {
        return new Term(F_LOCATION, rootLocation.getAbsolutePath());
    }

    public boolean isEmpty() {
        try {
            return writer.numDocs() == 0;
        } catch (Exception e) {
            log(ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
            return true;
        }
    }

    @Override
    protected void shutDown() throws Exception {
        IOUtils.close(reader, writer, directory);
    }

    public ImmutableSet<String> subtypes(IType expected, String prefix) {
        if (expected == null) {
            return ImmutableSet.of();
        } else {
            return subtypes(expected.getFullyQualifiedName(), prefix);
        }
    }

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

    public ImmutableSet<String> subtypes(String type, String prefix) {
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

    public void clear() {
        try {
            writer.deleteAll();
        } catch (Exception e) {
            log(ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
        }
    }

    private JobFuture active = null;

    private boolean rebuildAfterNextAccess;

    public ListenableFuture<IStatus> rebuild() {
        if (!(active == null || active.isDone() || active.isCancelled())) {
            active.cancel(true);
        }
        final JobFuture res = new JobFuture();
        active = res;
        Job job = new Job(MessageFormat.format(Messages.JOB_NAME_INDEXING, project.getElementName())) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                SubMonitor progress = SubMonitor.convert(monitor, Messages.MONITOR_NAME_INDEXING + project.getElementName(), TICKS);
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
                    res.setResult(new Status(IStatus.ERROR, "org.eclipse.recommenders.types.rcp", e.getMessage(), e)); //$NON-NLS-1$
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
        return res;
    }

    private synchronized void rebuild(SubMonitor progress) {
        List<IPackageFragmentRoot> roots = findArchivePackageFragmentRoots();
        for (IPackageFragmentRoot root : roots) {
            progress.subTask(root.getElementName());
            ImmutableList<IType> types = findTypes(root);
            for (IType type : types) {
                if (progress.isCanceled()) {
                    throw new OperationCanceledException();
                }
                indexType(type);
                progress.worked(1);
            }
            File location = JavaElementsFinder.findLocation(root).orNull();
            if (location != null) {
                registerArchivePackageFragmentRoot(location);
            }
            commit();
        }
        progress.done();
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

    public void commit() {
        try {
            writer.commit();
        } catch (Exception e) {
            log(ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
        }
    }

    public void compact() {
        try {
            writer.forceMerge(1, true);
        } catch (IOException e) {
            log(ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
        }
    }

    public void refresh(IType type) {
        ImmutableSet<String> subtypes = subtypes(type, ""); //$NON-NLS-1$
        removeSubtypes(type);
        indexType(type);
        for (String subtypeName : subtypes) {
            IType subtype = findType(subtypeName, getProject()).orNull();
            if (subtype != null) {
                indexType(subtype);
            }
        }
    }

    private void removeSubtypes(IType type) {
        File location = findPackageFragmentRoot(type).orNull();
        if (location == null) {
            return;
        }
        TermQuery query = new TermQuery(new Term(F_INSTANCEOF, type.getFullyQualifiedName()));
        try {
            writer.deleteDocuments(query);
        } catch (Exception e) {
            log(ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
        }
    }

    public void removeType(IType type) throws CorruptIndexException, IOException {
        File location = findPackageFragmentRoot(type).orNull();
        if (location == null) {
            return;
        }
        BooleanQuery delete = new BooleanQuery();
        delete.add(new TermQuery(termLocation(location)), Occur.MUST);
        delete.add(new TermQuery(new Term(F_NAME, type.getFullyQualifiedName())), Occur.MUST);
        writer.deleteDocuments(delete);
    }

    public void removePackageFragment(IPackageFragment fragment) throws CorruptIndexException, IOException {
        File location = findPackageFragmentRootLocation(fragment).orNull();
        if (location == null) {
            return;
        }
        BooleanQuery delete = new BooleanQuery();
        delete.add(new TermQuery(termLocation(location)), Occur.MUST);
        delete.add(new TermQuery(new Term(F_NAME, fragment.getElementName() + "*")), Occur.MUST); //$NON-NLS-1$
        writer.deleteDocuments(delete);
    }

    public void removePackageFragmentRoot(IPackageFragmentRoot root) throws CorruptIndexException, IOException {
        File location = findLocation(root).orNull();
        if (location == null) {
            return;
        }
        TermQuery delete = new TermQuery(termLocation(location));
        writer.deleteDocuments(delete);
    }

    public void indexType(IType type) {
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
        addDocument(doc);
    }

    private void addDocument(Document doc) {
        try {
            writer.addDocument(doc);
        } catch (Exception e) {
            log(ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
        }
    }

    private Optional<File> findPackageFragmentRoot(IType type) {
        IPackageFragmentRoot ancestor = (IPackageFragmentRoot) type.getAncestor(PACKAGE_FRAGMENT_ROOT);
        return findLocation(ancestor);
    }

    private Optional<File> findPackageFragmentRootLocation(IPackageFragment fragment) {
        IPackageFragmentRoot ancestor = (IPackageFragmentRoot) fragment.getAncestor(PACKAGE_FRAGMENT_ROOT);
        return findLocation(ancestor);
    }

    public void indexTypeRoot(ITypeRoot root) {
        if (root instanceof ICompilationUnit) {
            indexCompilationUnit((ICompilationUnit) root);
        } else if (root instanceof IClassFile) {
            indexClassFile((IClassFile) root);
        }
    }

    public void indexCompilationUnit(ICompilationUnit cu) {
        try {
            for (IType type : cu.getTypes()) {
                indexType(type);
            }
        } catch (Exception e) {
            log(ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
        }
    }

    public void indexClassFile(IClassFile root) {
        indexType(root.getType());
    }

    public void removeCompilationUnit(ICompilationUnit cu) {
        try {
            for (IType type : cu.getTypes()) {
                removeType(type);
            }
        } catch (Exception e) {
            log(ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
        }
    }

    public IJavaProject getProject() {
        return project;
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
            if (job != null)
                return job.cancel();
            return false;
        }

    }

    public void setRebuildAfterNextAccess(boolean value) {
        rebuildAfterNextAccess = value;
    }

    public boolean isRebuildAfterNextAccess() {
        return rebuildAfterNextAccess;
    }
}
