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
import static org.apache.lucene.document.Field.Index.NOT_ANALYZED;
import static org.eclipse.jdt.core.IJavaElement.PACKAGE_FRAGMENT_ROOT;
import static org.eclipse.recommenders.internal.types.rcp.LogMessages.ERROR_ACCESSING_SEARCHINDEX_FAILED;
import static org.eclipse.recommenders.jdt.JavaElementsFinder.*;
import static org.eclipse.recommenders.utils.Logs.log;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.Names;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.AbstractIdleService;

public class ProjectTypesIndex extends AbstractIdleService {

    private static final String F_EXTENDS = "extends";
    private static final String F_IMPLEMENTS = "implements";
    private static final String F_INSTANCEOF = "instanceof";
    private static final String V_JAVA_LANG_OBJECT = "java.lang.Object";
    private static final String F_NAME = "name";
    private static final String F_LOCATION = "location";

    private IJavaProject project;
    private File indexDir;

    private Directory directory;
    private IndexWriter writer;
    private IndexReader reader;

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

    public ImmutableSet<String> subtypes(IType expected) {
        if (expected == null) {
            return ImmutableSet.of();
        } else {
            return subtypes(expected.getFullyQualifiedName());
        }
    }

    public ImmutableSet<String> subtypes(ITypeName expected) {
        return subtypes(Names.vm2srcQualifiedType(expected));
    }

    public ImmutableSet<String> subtypes(String type) {
        ImmutableSet.Builder<String> b = ImmutableSet.builder();
        if (StringUtils.isBlank(type)) {
            return b.build();
        }
        IndexReader reader = getReader();
        try (IndexSearcher searcher = new IndexSearcher(reader)) {
            TopDocs search = searcher.search(new TermQuery(new Term(F_INSTANCEOF, type)), Integer.MAX_VALUE);
            for (ScoreDoc sdoc : search.scoreDocs) {
                Document doc = searcher.doc(sdoc.doc);
                String name = doc.get(F_NAME);
                b.add(name);
            }
        } catch (Exception e) {
            log(LogMessages.ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
        }
        return b.build();
    }

    private synchronized IndexReader getReader() {
        if (reader == null) {
            reader = createReader();
            return reader;
        }
        try {
            IndexReader newReader = IndexReader.openIfChanged(reader);
            if (newReader != null) {
                reader.close();
                reader = newReader;
            }
        } catch (Exception e) {
            log(ERROR_ACCESSING_SEARCHINDEX_FAILED, e);
        }
        return reader;
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

    public void rebuild(IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor);
        progress.beginTask("Indexing", 100000);
        progress.subTask("Finding types");
        ImmutableList<IType> types = findTypes(project);
        progress.setWorkRemaining(types.size());
        int count = 1;
        for (IType type : types) {
            progress.subTask(type.getFullyQualifiedName() + "(" + count++ + " / " + types.size() + ")");
            indexType(type);
            progress.worked(1);
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
        ImmutableSet<String> subtypes = subtypes(type);
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
        delete.add(new TermQuery(new Term(F_LOCATION, location.getAbsolutePath())), Occur.MUST);
        delete.add(new TermQuery(new Term(F_NAME, type.getFullyQualifiedName())), Occur.MUST);
        writer.deleteDocuments(delete);
    }

    public void removePackageFragment(IPackageFragment fragment) throws CorruptIndexException, IOException {
        File location = findPackageFragmentRootLocation(fragment).orNull();
        if (location == null) {
            return;
        }
        BooleanQuery delete = new BooleanQuery();
        delete.add(new TermQuery(new Term(F_LOCATION, location.getAbsolutePath())), Occur.MUST);
        delete.add(new TermQuery(new Term(F_NAME, fragment.getElementName() + "*")), Occur.MUST);
        writer.deleteDocuments(delete);
    }

    public void removePackageFragmentRoot(IPackageFragmentRoot root) throws CorruptIndexException, IOException {
        File location = findLocation(root).orNull();
        if (location == null) {
            return;
        }
        TermQuery delete = new TermQuery(new Term(F_LOCATION, location.getAbsolutePath()));
        writer.deleteDocuments(delete);
    }

    public void indexType(IType type) {
        Document doc = new Document();
        {
            // name:
            doc.add(new Field(F_NAME, type.getFullyQualifiedName(), Store.YES, NOT_ANALYZED));
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
                for (IType superInterface : h.getAllSuperInterfaces(type)) {
                    String fullyQualifiedName = superInterface.getFullyQualifiedName();
                    doc.add(new Field(F_IMPLEMENTS, fullyQualifiedName, Store.NO, NOT_ANALYZED));
                    doc.add(new Field(F_INSTANCEOF, fullyQualifiedName, Store.NO, NOT_ANALYZED));
                }
                for (IType supertypes : h.getAllSupertypes(type)) {
                    String fullyQualifiedName = supertypes.getFullyQualifiedName();
                    if (equal(V_JAVA_LANG_OBJECT, fullyQualifiedName)) {
                        continue;
                    }
                    doc.add(new Field(F_EXTENDS, fullyQualifiedName, Store.NO, NOT_ANALYZED));
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

}
