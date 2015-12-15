/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Arrays.asList;
import static org.eclipse.recommenders.snipmatch.Location.*;
import static org.eclipse.recommenders.testing.RecommendationMatchers.recommendation;
import static org.eclipse.recommenders.utils.Constants.DOT_JSON;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.snipmatch.FileSnippetRepository;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.snipmatch.Location;
import org.eclipse.recommenders.snipmatch.SearchContext;
import org.eclipse.recommenders.snipmatch.Snippet;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class FileSnippetRepositoryTest {

    private static final Set<ProjectCoordinate> EMPTY_CLASSPATH = Collections.<ProjectCoordinate>emptySet();
    private static final List<String> NO_EXTRA_SEARCH_TERMS = Collections.emptyList();
    private static final List<String> NO_TAGS = Collections.emptyList();

    private static final UUID A_UUID = UUID.randomUUID();
    private static final UUID ANOTHER_UUID = UUID.randomUUID();
    private static final UUID THIRD_UUID = UUID.randomUUID();

    private static final String SNIPPET_NAME = "snippet";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private FileSnippetRepository sut;

    private File snippetsDir;

    @Before
    public void setUp() throws IOException {
        File baseDir = tmp.getRoot();
        snippetsDir = new File(baseDir, "snippets");
        snippetsDir.mkdirs();
        sut = new FileSnippetRepository("id", baseDir);
    }

    @Test
    public void testDeleteSnippetFoundInRepository() throws Exception {
        Snippet snippet = new Snippet(A_UUID, "name", "description", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "code", FILE);
        File snippetFile = storeSnippet(snippet);
        sut.open();

        boolean wasDeleted = sut.delete(snippet.getUuid());
        List<Recommendation<ISnippet>> searchByName = sut.search(new SearchContext("name"));
        List<Recommendation<ISnippet>> blanketSearch = sut.search(new SearchContext(""));

        assertThat(wasDeleted, is(true));
        assertThat(snippetFile.exists(), is(false));
        assertThat(searchByName.isEmpty(), is(true));
        assertThat(blanketSearch.isEmpty(), is(true));

        sut.close();
    }

    @Test
    public void testDeleteSnippetNotFoundInRepository() throws Exception {
        ISnippet snippet = new Snippet(A_UUID, "name", "description", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "code", FILE);
        File snippetFile = storeSnippet(snippet);
        sut.open();

        boolean wasDeleted = sut.delete(ANOTHER_UUID);
        List<Recommendation<ISnippet>> searchByName = sut.search(new SearchContext("name"));
        List<Recommendation<ISnippet>> blanketSearch = sut.search(new SearchContext(""));

        assertThat(wasDeleted, is(false));
        assertThat(snippetFile.exists(), is(true));
        assertThat(getOnlyElement(searchByName).getProposal(), is(equalTo(snippet)));
        assertThat(getOnlyElement(blanketSearch).getProposal(), is(equalTo(snippet)));

        sut.close();
    }

    @Test
    public void testDeleteSnippetInRepositoryWithAnotherSnippetToKeep() throws Exception {
        ISnippet snippetToDelete = new Snippet(A_UUID, "name", "description", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "code",
                FILE);
        ISnippet snippetToKeep = new Snippet(ANOTHER_UUID, "name", "description", NO_EXTRA_SEARCH_TERMS, NO_TAGS,
                "code", FILE);
        File snippetFileToDelete = storeSnippet(snippetToDelete);
        File snippetFileToKeep = storeSnippet(snippetToKeep);
        sut.open();

        boolean wasDeleted = sut.delete(snippetToDelete.getUuid());
        List<Recommendation<ISnippet>> searchByName = sut.search(new SearchContext("name"));
        List<Recommendation<ISnippet>> blanketSearch = sut.search(new SearchContext(""));

        assertThat(wasDeleted, is(true));
        assertThat(snippetFileToDelete.exists(), is(false));
        assertThat(snippetFileToKeep.exists(), is(true));
        assertThat(getOnlyElement(searchByName).getProposal(), is(equalTo(snippetToKeep)));
        assertThat(getOnlyElement(blanketSearch).getProposal(), is(equalTo(snippetToKeep)));

        sut.close();
    }

    @Test
    public void testHasSnippetFoundInRepository() throws Exception {
        createAndStoreSnippet(A_UUID, "name", "description", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "code", FILE);
        sut.open();

        boolean hasSnippet = sut.hasSnippet(A_UUID);

        assertThat(hasSnippet, is(true));

        sut.close();
    }

    @Test
    public void testHasSnippetNotFoundInRepository() throws Exception {
        sut.open();

        boolean hasSnippet = sut.hasSnippet(A_UUID);

        assertThat(hasSnippet, is(false));

        sut.close();
    }

    @Test
    public void testHasSnippetNotFoundInRepositoryWithAnotherSnippet() throws Exception {
        createAndStoreSnippet(ANOTHER_UUID, "name", "description", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "code", FILE);
        sut.open();
        boolean hasSnippet = sut.hasSnippet(A_UUID);

        assertThat(hasSnippet, is(false));

        sut.close();
    }

    @Test(expected = IllegalStateException.class)
    public void testHasSnippetWhenRepositoryClosed() throws Exception {
        assertThat(sut.isOpen(), is(false));

        sut.hasSnippet(UUID.randomUUID());
    }

    @Test(expected = IllegalStateException.class)
    public void testDeleteWhenRepositoryClosed() throws Exception {
        assertThat(sut.isOpen(), is(false));

        sut.delete(UUID.randomUUID());
    }

    @Test(expected = IllegalStateException.class)
    public void testSearchWhenRepositoryClosed() throws Exception {
        assertThat(sut.isOpen(), is(false));

        sut.search(new SearchContext(" "));
    }

    @Test
    public void testRepoIsClosedWhenNumberOfCloseCallsIsEqualsToNumberOfOpenCalls() throws Exception {
        ISnippetRepository thread1 = sut;
        ISnippetRepository thread2 = sut;

        thread1.open();
        assertThat(sut.isOpen(), is(true));

        thread2.open();
        assertThat(sut.isOpen(), is(true));

        thread1.close();
        assertThat(sut.isOpen(), is(true));

        thread2.close();
        assertThat(sut.isOpen(), is(false));
    }

    @Test
    public void testMultipleCallsOfOpenAreLegal() throws Exception {
        sut.open();
        sut.open();
        assertThat(sut.isOpen(), is(true));
    }

    @Test
    public void testMultipleCallsOfCloseAreLegal() throws Exception {
        sut.open();
        assertThat(sut.isOpen(), is(true));
        sut.close();
        assertThat(sut.isOpen(), is(false));
        sut.close();
        assertThat(sut.isOpen(), is(false));
    }

    @Test
    public void testRepoCanBeReopened() throws Exception {
        sut.open();
        sut.close();
        assertThat(sut.isOpen(), is(false));
        sut.open();
        assertThat(sut.isOpen(), is(true));
    }

    @Test
    public void testImportSnippet() throws Exception {
        ISnippet snippet = new Snippet(A_UUID, "name", "description", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "code", FILE);
        sut.open();

        sut.importSnippet(snippet);
        List<Recommendation<ISnippet>> searchByName = sut.search(new SearchContext("name"));
        List<Recommendation<ISnippet>> blanketSearch = sut.search(new SearchContext(""));

        assertThat(getOnlyElement(searchByName).getProposal(), is(equalTo(snippet)));
        assertThat(getOnlyElement(blanketSearch).getProposal(), is(equalTo(snippet)));

        sut.close();
    }

    @Test
    public void testImportSnippetWhenSnippetWithSameNameAlreadyInRepository() throws Exception {
        createAndStoreSnippet(A_UUID, "name", "description", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "code", FILE);
        ISnippet otherSnippet = new Snippet(ANOTHER_UUID, "name", "description", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "code",
                FILE);
        sut.open();

        sut.importSnippet(otherSnippet);
        List<Recommendation<ISnippet>> searchByName = sut.search(new SearchContext("name"));
        List<Recommendation<ISnippet>> blanketSearch = sut.search(new SearchContext(""));

        assertThat(searchByName.size(), is(2));
        assertThat(blanketSearch.size(), is(2));

        sut.close();
    }

    @Test
    public void testImportSnippetWithModifiedMetaData() throws Exception {
        ISnippet originalSnippet = createAndStoreSnippet(A_UUID, "name", "description", NO_EXTRA_SEARCH_TERMS, NO_TAGS,
                "code", FILE);
        sut.open();

        Snippet modifiedSnippet = Snippet.copy(originalSnippet);
        modifiedSnippet.setExtraSearchTerms(asList("term1", "term2"));
        sut.importSnippet(modifiedSnippet);
        List<Recommendation<ISnippet>> searchByName = sut.search(new SearchContext("name"));
        List<Recommendation<ISnippet>> blanketSearch = sut.search(new SearchContext(""));

        assertThat(getOnlyElement(searchByName).getProposal(), is(equalTo((ISnippet) modifiedSnippet)));
        assertThat(getOnlyElement(blanketSearch).getProposal(), is(equalTo((ISnippet) modifiedSnippet)));

        sut.close();
    }

    @Test
    public void testImportSnippetWithModifiedCodeUnderDifferentUuid() throws Exception {
        ISnippet originalSnippet = createAndStoreSnippet(A_UUID, "name", "description", NO_EXTRA_SEARCH_TERMS, NO_TAGS,
                "code", FILE);
        sut.open();

        Snippet modifiedSnippet = Snippet.copy(originalSnippet);
        modifiedSnippet.setUUID(ANOTHER_UUID);
        modifiedSnippet.setCode("modified code");
        sut.importSnippet(modifiedSnippet);
        List<Recommendation<ISnippet>> searchByName = sut.search(new SearchContext("name"));
        List<Recommendation<ISnippet>> blanketSearch = sut.search(new SearchContext(""));

        assertThat(searchByName.size(), is(2));
        assertThat(blanketSearch.size(), is(2));

        sut.close();
    }

    @Test
    public void testSearchByName() throws Exception {
        ISnippet snippet = createAndStoreSnippet(A_UUID, "name", "description", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "code",
                FILE);
        sut.open();

        assertThat(getOnlyElement(sut.search(new SearchContext("name:n"))).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search(new SearchContext("name:na"))).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search(new SearchContext("name:name"))).getProposal(), is(snippet));
        assertThat(sut.search(new SearchContext("name:description")).isEmpty(), is(true));

        sut.close();
    }

    @Test
    public void testSearchByDescription() throws Exception {
        ISnippet snippet = createAndStoreSnippet(A_UUID, "name", "description", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "code",
                FILE);
        sut.open();

        assertThat(getOnlyElement(sut.search(new SearchContext("description:d"))).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search(new SearchContext("description:desc"))).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search(new SearchContext("description:description"))).getProposal(), is(snippet));
        assertThat(sut.search(new SearchContext("description:name")).isEmpty(), is(true));

        sut.close();
    }

    @Test
    public void testSearchByExtraSearchTerm() throws Exception {
        List<String> extraSearchTerms = ImmutableList.of("term1", "term2");
        ISnippet snippet = createAndStoreSnippet(A_UUID, "name", "description", extraSearchTerms, NO_TAGS, "", FILE);
        sut.open();

        assertThat(getOnlyElement(sut.search(new SearchContext("extra:term"))).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search(new SearchContext("extra:term1"))).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search(new SearchContext("extra:term2"))).getProposal(), is(snippet));
        assertThat(sut.search(new SearchContext("extra:name")).isEmpty(), is(true));
        assertThat(sut.search(new SearchContext("extra:description")).isEmpty(), is(true));

        sut.close();
    }

    @Test
    public void testSearchByTag() throws Exception {
        List<String> tags = ImmutableList.of("tag1", "tag2");
        ISnippet snippet = createAndStoreSnippet(A_UUID, "name", "description", NO_EXTRA_SEARCH_TERMS, tags, "", FILE);
        sut.open();

        assertThat(sut.search(new SearchContext("tag:tag")).isEmpty(), is(true));
        assertThat(getOnlyElement(sut.search(new SearchContext("tag:tag1"))).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search(new SearchContext("tag:tag2"))).getProposal(), is(snippet));
        assertThat(sut.search(new SearchContext("tag:name")).isEmpty(), is(true));
        assertThat(sut.search(new SearchContext("tag:description")).isEmpty(), is(true));

        sut.close();
    }

    @Test
    public void testSearchByLocation() throws Exception {
        ISnippet fileSnippet = createAndStoreSnippet(UUID.randomUUID(), "file snippet", "", NO_EXTRA_SEARCH_TERMS,
                NO_TAGS, "", FILE);
        ISnippet javaFileSnippet = createAndStoreSnippet(UUID.randomUUID(), "java file snippet", "",
                NO_EXTRA_SEARCH_TERMS, NO_TAGS, "", JAVA_FILE);
        ISnippet javaSnippet = createAndStoreSnippet(UUID.randomUUID(), "java snippet", "", NO_EXTRA_SEARCH_TERMS,
                NO_TAGS, "", JAVA);
        ISnippet javaStatementsSnippet = createAndStoreSnippet(UUID.randomUUID(), "statement snippet", "",
                NO_EXTRA_SEARCH_TERMS, NO_TAGS, "", JAVA_STATEMENTS);
        ISnippet javaTypeMembersSnippet = createAndStoreSnippet(UUID.randomUUID(), "type member snippet", "",
                NO_EXTRA_SEARCH_TERMS, NO_TAGS, "", JAVA_TYPE_MEMBERS);
        ISnippet javadocSnippet = createAndStoreSnippet(UUID.randomUUID(), "javadoc snippet", "", NO_EXTRA_SEARCH_TERMS,
                NO_TAGS, "", JAVADOC);
        sut.open();

        List<Recommendation<ISnippet>> noneSearch = sut.search(new SearchContext("snippet", NONE, EMPTY_CLASSPATH));
        assertThat(noneSearch, hasItem(recommendation(fileSnippet, 1.0)));
        assertThat(noneSearch, hasItem(recommendation(javaFileSnippet, 1.0)));
        assertThat(noneSearch, hasItem(recommendation(javaSnippet, 1.0)));
        assertThat(noneSearch, hasItem(recommendation(javaStatementsSnippet, 1.0)));
        assertThat(noneSearch, hasItem(recommendation(javaTypeMembersSnippet, 1.0)));
        assertThat(noneSearch, hasItem(recommendation(javadocSnippet, 1.0)));
        assertThat(noneSearch.size(), is(6));

        List<Recommendation<ISnippet>> fileSearch = sut.search(new SearchContext("snippet", FILE, EMPTY_CLASSPATH));
        assertThat(fileSearch, hasItem(recommendation(fileSnippet, 1.0)));
        assertThat(fileSearch.size(), is(1));

        List<Recommendation<ISnippet>> javaFileSearch = sut
                .search(new SearchContext("snippet", JAVA_FILE, EMPTY_CLASSPATH));
        assertThat(fileSearch, hasItem(recommendation(fileSnippet, 1.0)));
        assertThat(javaFileSearch, hasItem(recommendation(javaFileSnippet, 1.0)));
        assertThat(javaFileSearch.size(), is(2));

        List<Recommendation<ISnippet>> javaSearch = sut.search(new SearchContext("snippet", JAVA, EMPTY_CLASSPATH));
        assertThat(javaSearch, hasItem(recommendation(fileSnippet, 1.0)));
        assertThat(javaFileSearch, hasItem(recommendation(javaFileSnippet, 1.0)));
        assertThat(javaSearch, hasItem(recommendation(javaSnippet, 1.0)));
        assertThat(javaSearch.size(), is(3));

        List<Recommendation<ISnippet>> javaStatementsSearch = sut
                .search(new SearchContext("snippet", JAVA_STATEMENTS, EMPTY_CLASSPATH));
        assertThat(javaStatementsSearch, hasItem(recommendation(fileSnippet, 1.0)));
        assertThat(javaFileSearch, hasItem(recommendation(javaFileSnippet, 1.0)));
        assertThat(javaStatementsSearch, hasItem(recommendation(javaSnippet, 1.0)));
        assertThat(javaStatementsSearch, hasItem(recommendation(javaStatementsSnippet, 1.0)));
        assertThat(javaStatementsSearch.size(), is(4));

        List<Recommendation<ISnippet>> javaTypeMembersSearch = sut
                .search(new SearchContext("snippet", JAVA_TYPE_MEMBERS, EMPTY_CLASSPATH));
        assertThat(javaTypeMembersSearch, hasItem(recommendation(fileSnippet, 1.0)));
        assertThat(javaFileSearch, hasItem(recommendation(javaFileSnippet, 1.0)));
        assertThat(javaTypeMembersSearch, hasItem(recommendation(javaSnippet, 1.0)));
        assertThat(javaTypeMembersSearch, hasItem(recommendation(javaTypeMembersSnippet, 1.0)));
        assertThat(javaTypeMembersSearch.size(), is(4));

        List<Recommendation<ISnippet>> javadocSearch = sut
                .search(new SearchContext("snippet", JAVADOC, EMPTY_CLASSPATH));
        assertThat(javadocSearch, hasItem(recommendation(fileSnippet, 1.0)));
        assertThat(javaFileSearch, hasItem(recommendation(javaFileSnippet, 1.0)));
        assertThat(javadocSearch, hasItem(recommendation(javadocSnippet, 1.0)));
        assertThat(javadocSearch.size(), is(3));

        sut.close();
    }

    @Test
    public void testPreferNameMatchesOverDescription() throws Exception {
        createAndStoreSnippet(A_UUID, "first", "", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "", FILE);
        createAndStoreSnippet(ANOTHER_UUID, "second", "first", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "", FILE);
        sut.open();

        List<Recommendation<ISnippet>> result = sut.search(new SearchContext("first"));

        Recommendation<ISnippet> forFirst = Iterables.tryFind(result, new UuidPredicate(A_UUID)).get();
        Recommendation<ISnippet> forSecond = Iterables.tryFind(result, new UuidPredicate(ANOTHER_UUID)).get();
        assertThat(forFirst.getRelevance(), is(greaterThan(forSecond.getRelevance())));

        sut.close();
    }

    @Test
    public void testNoPreferenceBetweenDescriptionAndExtraSearchTerms() throws Exception {
        createAndStoreSnippet(A_UUID, "first", "searchword", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "", FILE);
        createAndStoreSnippet(ANOTHER_UUID, "second", "", ImmutableList.of("searchword"), NO_TAGS, "", FILE);
        sut.open();

        List<Recommendation<ISnippet>> result = sut.search(new SearchContext("searchword"));

        Recommendation<ISnippet> forFirst = Iterables.tryFind(result, new UuidPredicate(A_UUID)).get();
        Recommendation<ISnippet> forSecond = Iterables.tryFind(result, new UuidPredicate(ANOTHER_UUID)).get();
        assertThat(forFirst.getRelevance(), is(equalTo(forSecond.getRelevance())));

        sut.close();
    }

    @Test
    public void testPreferDescriptionMatchesOverTags() throws Exception {
        createAndStoreSnippet(A_UUID, "addlistener", "add a listener to a Widget", NO_EXTRA_SEARCH_TERMS,
                ImmutableList.of("eclipse", "swt", "ui"), "", FILE);
        createAndStoreSnippet(ANOTHER_UUID, "Browser", "new Browser", NO_EXTRA_SEARCH_TERMS,
                ImmutableList.of("eclipse", "swt", "widget"), "", FILE);
        createAndStoreSnippet(THIRD_UUID, "Third", "something", NO_EXTRA_SEARCH_TERMS,
                ImmutableList.of("eclipse", "swt", "widget"), "", FILE);
        sut.open();

        List<Recommendation<ISnippet>> result = sut.search(new SearchContext("widget"));

        Recommendation<ISnippet> forFirst = Iterables.tryFind(result, new UuidPredicate(A_UUID)).get();
        Recommendation<ISnippet> forSecond = Iterables.tryFind(result, new UuidPredicate(ANOTHER_UUID)).get();
        Recommendation<ISnippet> forThird = Iterables.tryFind(result, new UuidPredicate(THIRD_UUID)).get();
        assertThat(forFirst.getRelevance(), is(greaterThan(forSecond.getRelevance())));
        assertThat(forFirst.getRelevance(), is(greaterThan(forThird.getRelevance())));

        sut.close();
    }

    @Test
    public void testRelevanceDoesntExceedOne() throws Exception {
        createAndStoreSnippet(A_UUID, "searchword", "searchword", ImmutableList.of("searchword"),
                ImmutableList.of("searchword"), "", FILE);
        createAndStoreSnippet(ANOTHER_UUID, "searchword", "", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "", FILE);
        sut.open();

        List<Recommendation<ISnippet>> result = sut.search(new SearchContext("searchword"));
        Recommendation<ISnippet> forFirst = Iterables.tryFind(result, new UuidPredicate(A_UUID)).get();
        Recommendation<ISnippet> forSecond = Iterables.tryFind(result, new UuidPredicate(ANOTHER_UUID)).get();
        assertThat(forFirst.getRelevance(), is(greaterThan(forSecond.getRelevance())));

        sut.close();
    }

    @Test
    public void testEmptyQueryReturnsAllSnippetsOnOneParameterSearch() throws Exception {
        createAndStoreSnippet(A_UUID, SNIPPET_NAME, "", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "", FILE);
        createAndStoreSnippet(ANOTHER_UUID, SNIPPET_NAME, "", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "", FILE);
        sut.open();

        List<Recommendation<ISnippet>> result = sut.search(new SearchContext(""));
        Optional<Recommendation<ISnippet>> forFirst = Iterables.tryFind(result, new UuidPredicate(A_UUID));
        Optional<Recommendation<ISnippet>> forSecond = Iterables.tryFind(result, new UuidPredicate(ANOTHER_UUID));

        assertThat(forFirst.isPresent(), is(true));
        assertThat(forSecond.isPresent(), is(true));

        sut.close();
    }

    @Test
    public void testEmptyQueryReturnsAllSnippetsOnTwoParametersSearch() throws Exception {
        createAndStoreSnippet(A_UUID, SNIPPET_NAME, "", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "", FILE);
        createAndStoreSnippet(ANOTHER_UUID, SNIPPET_NAME, "", NO_EXTRA_SEARCH_TERMS, NO_TAGS, "", FILE);
        sut.open();

        List<Recommendation<ISnippet>> result = sut.search(new SearchContext("", FILE, EMPTY_CLASSPATH), 2);
        Optional<Recommendation<ISnippet>> forFirst = Iterables.tryFind(result, new UuidPredicate(A_UUID));
        Optional<Recommendation<ISnippet>> forSecond = Iterables.tryFind(result, new UuidPredicate(ANOTHER_UUID));

        assertThat(forFirst.isPresent(), is(false));
        assertThat(forSecond.isPresent(), is(false));

        sut.close();
    }

    @Test
    public void testNumberOfTagsDoesntAffectRelevance() throws Exception {
        createAndStoreSnippet(A_UUID, "first", "", NO_EXTRA_SEARCH_TERMS, ImmutableList.of("tag1"), "", FILE);
        createAndStoreSnippet(ANOTHER_UUID, "second", "", NO_EXTRA_SEARCH_TERMS, ImmutableList.of("tag1", "tag2"), "",
                FILE);
        sut.open();

        List<Recommendation<ISnippet>> result = sut.search(new SearchContext("tag:tag1"));

        Recommendation<ISnippet> forFirst = Iterables.tryFind(result, new UuidPredicate(A_UUID)).get();
        Recommendation<ISnippet> forSecond = Iterables.tryFind(result, new UuidPredicate(ANOTHER_UUID)).get();
        assertThat(forFirst.getRelevance(), is(closeTo(forSecond.getRelevance(), 0.01)));

        sut.close();
    }

    private ISnippet createAndStoreSnippet(UUID uuid, String name, String description, List<String> extraSearchTerms,
            List<String> tags, String code, Location locationConstraint) throws Exception {
        Snippet snippet = new Snippet(uuid, name, description, extraSearchTerms, tags, code, locationConstraint);
        storeSnippet(snippet);
        return snippet;
    }

    private File storeSnippet(ISnippet snippet) throws Exception {
        File jsonFile = new File(snippetsDir, snippet.getUuid() + DOT_JSON);
        GsonUtil.serialize(snippet, jsonFile);
        return jsonFile;
    }

    private static final class UuidPredicate implements Predicate<Recommendation<? extends ISnippet>> {

        private final UUID uuid;

        public UuidPredicate(UUID uuid) {
            this.uuid = uuid;
        }

        @Override
        public boolean apply(Recommendation<? extends ISnippet> snippet) {
            return uuid.equals(snippet.getProposal().getUuid());
        }
    }
}
