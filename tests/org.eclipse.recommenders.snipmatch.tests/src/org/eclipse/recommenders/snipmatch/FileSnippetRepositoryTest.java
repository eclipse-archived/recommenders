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
package org.eclipse.recommenders.snipmatch;

import static com.google.common.collect.Iterables.*;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.eclipse.recommenders.snipmatch.FileSnippetRepository.NO_FILENAME_RESTRICTION;
import static org.eclipse.recommenders.snipmatch.Location.*;
import static org.eclipse.recommenders.testing.RecommendationMatchers.recommendation;
import static org.eclipse.recommenders.utils.Constants.DOT_JSON;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

public class FileSnippetRepositoryTest {

    private static final Set<ProjectCoordinate> EMPTY_CLASSPATH = Collections.<ProjectCoordinate>emptySet();
    private static final String FILENAME = "Test.java";

    private static final UUID A_UUID = randomUUID();
    private static final UUID ANOTHER_UUID = randomUUID();
    private static final UUID THIRD_UUID = randomUUID();

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
        File snippetFile = storeSnippet(createSnippet(A_UUID, "name"));
        sut.open();

        boolean wasDeleted = sut.delete(A_UUID);
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
        ISnippet snippet = createSnippet(A_UUID, "name");
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
        File snippetFileToDelete = storeSnippet(createSnippet(A_UUID, "name"));
        ISnippet snippetToKeep = createSnippet(ANOTHER_UUID, "name");
        File snippetFileToKeep = storeSnippet(snippetToKeep);
        sut.open();

        boolean wasDeleted = sut.delete(A_UUID);
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
        storeSnippet(createSnippet(A_UUID, "name"));
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
        storeSnippet(createSnippet(A_UUID, "name"));
        sut.open();
        boolean hasSnippet = sut.hasSnippet(ANOTHER_UUID);

        assertThat(hasSnippet, is(false));

        sut.close();
    }

    @Test(expected = IllegalStateException.class)
    public void testHasSnippetWhenRepositoryClosed() throws Exception {
        assertThat(sut.isOpen(), is(false));

        sut.hasSnippet(randomUUID());
    }

    @Test(expected = IllegalStateException.class)
    public void testDeleteWhenRepositoryClosed() throws Exception {
        assertThat(sut.isOpen(), is(false));

        sut.delete(randomUUID());
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
        ISnippet snippet = createSnippet(A_UUID, "name");
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
        storeSnippet(createSnippet(A_UUID, "name"));

        ISnippet snippetB = createSnippet(ANOTHER_UUID, "name");

        sut.open();

        sut.importSnippet(snippetB);
        List<Recommendation<ISnippet>> searchByName = sut.search(new SearchContext("name"));
        List<Recommendation<ISnippet>> blanketSearch = sut.search(new SearchContext(""));

        assertThat(searchByName.size(), is(2));
        assertThat(blanketSearch.size(), is(2));

        sut.close();
    }

    @Test
    public void testImportSnippetWithModifiedMetaData() throws Exception {
        ISnippet originalSnippet = createSnippet(A_UUID, "name");
        storeSnippet(originalSnippet);

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
        ISnippet originalSnippet = createSnippet(A_UUID, "name");
        storeSnippet(originalSnippet);

        sut.open();

        Snippet modifiedSnippet = Snippet.copy(originalSnippet);
        modifiedSnippet.setUuid(ANOTHER_UUID);
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
        ISnippet snippet = createSnippet(A_UUID, "name");
        storeSnippet(snippet);

        sut.open();

        assertThat(getOnlyElement(sut.search(new SearchContext("name:n"))).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search(new SearchContext("name:na"))).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search(new SearchContext("name:name"))).getProposal(), is(snippet));
        assertThat(sut.search(new SearchContext("name:description")).isEmpty(), is(true));

        sut.close();
    }

    @Test
    public void testSearchByDescription() throws Exception {
        ISnippet snippet = createSnippetWithDescription(A_UUID, "name", "description");
        storeSnippet(snippet);
        sut.open();

        assertThat(getOnlyElement(sut.search(new SearchContext("description:d"))).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search(new SearchContext("description:desc"))).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search(new SearchContext("description:description"))).getProposal(), is(snippet));
        assertThat(sut.search(new SearchContext("description:name")).isEmpty(), is(true));

        sut.close();
    }

    @Test
    public void testSearchByExtraSearchTerm() throws Exception {
        ISnippet snippet = createSnippetWithExtraSearchTerms(A_UUID, "name", "term1", "term2");
        storeSnippet(snippet);
        sut.open();

        assertThat(getOnlyElement(sut.search(new SearchContext("extra:term1"))).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search(new SearchContext("extra:term2"))).getProposal(), is(snippet));
        assertThat(sut.search(new SearchContext("extra:name")).isEmpty(), is(true));

        sut.close();
    }

    @Test
    public void testSearchByTag() throws Exception {
        ISnippet snippet = createSnippetWithTags(A_UUID, "name", "tag1", "tag2");
        storeSnippet(snippet);
        sut.open();

        assertThat(sut.search(new SearchContext("tag:tag")).isEmpty(), is(true));
        assertThat(getOnlyElement(sut.search(new SearchContext("tag:tag1"))).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search(new SearchContext("tag:tag2"))).getProposal(), is(snippet));
        assertThat(sut.search(new SearchContext("tag:name")).isEmpty(), is(true));

        sut.close();
    }

    @Test
    public void testSearchByLocation() throws Exception {
        ISnippet fileSnippet = createSnippetWithLocation(randomUUID(), "file snippet", FILE);
        storeSnippet(fileSnippet);
        ISnippet javaFileSnippet = createSnippetWithLocation(randomUUID(), "java file snippet", JAVA_FILE);
        storeSnippet(javaFileSnippet);
        ISnippet javaSnippet = createSnippetWithLocation(randomUUID(), "java snippet", JAVA);
        storeSnippet(javaSnippet);
        ISnippet javaStatementsSnippet = createSnippetWithLocation(randomUUID(), "java statements snippet",
                JAVA_STATEMENTS);
        storeSnippet(javaStatementsSnippet);
        ISnippet javaTypeMembersSnippet = createSnippetWithLocation(randomUUID(), "java type member snippet",
                JAVA_TYPE_MEMBERS);
        storeSnippet(javaTypeMembersSnippet);
        ISnippet javadocSnippet = createSnippetWithLocation(randomUUID(), "javadoc snippet", JAVADOC);
        storeSnippet(javadocSnippet);
        sut.open();

        List<Recommendation<ISnippet>> noneSearch = sut
                .search(new SearchContext("snippet", NONE, FILENAME, EMPTY_CLASSPATH));
        assertThat(noneSearch, hasItem(recommendation(fileSnippet, 1.0)));
        assertThat(noneSearch, hasItem(recommendation(javaFileSnippet, 1.0)));
        assertThat(noneSearch, hasItem(recommendation(javaSnippet, 1.0)));
        assertThat(noneSearch, hasItem(recommendation(javaStatementsSnippet, 1.0)));
        assertThat(noneSearch, hasItem(recommendation(javaTypeMembersSnippet, 1.0)));
        assertThat(noneSearch, hasItem(recommendation(javadocSnippet, 1.0)));
        assertThat(noneSearch.size(), is(6));

        List<Recommendation<ISnippet>> fileSearch = sut
                .search(new SearchContext("snippet", FILE, FILENAME, EMPTY_CLASSPATH));
        assertThat(fileSearch, hasItem(recommendation(fileSnippet, 1.0)));
        assertThat(fileSearch.size(), is(1));

        List<Recommendation<ISnippet>> javaFileSearch = sut
                .search(new SearchContext("snippet", JAVA_FILE, FILENAME, EMPTY_CLASSPATH));
        assertThat(fileSearch, hasItem(recommendation(fileSnippet, 1.0)));
        assertThat(javaFileSearch, hasItem(recommendation(javaFileSnippet, 1.0)));
        assertThat(javaFileSearch.size(), is(2));

        List<Recommendation<ISnippet>> javaSearch = sut
                .search(new SearchContext("snippet", JAVA, FILENAME, EMPTY_CLASSPATH));
        assertThat(javaSearch, hasItem(recommendation(fileSnippet, 1.0)));
        assertThat(javaFileSearch, hasItem(recommendation(javaFileSnippet, 1.0)));
        assertThat(javaSearch, hasItem(recommendation(javaSnippet, 1.0)));
        assertThat(javaSearch.size(), is(3));

        List<Recommendation<ISnippet>> javaStatementsSearch = sut
                .search(new SearchContext("snippet", JAVA_STATEMENTS, FILENAME, EMPTY_CLASSPATH));
        assertThat(javaStatementsSearch, hasItem(recommendation(fileSnippet, 1.0)));
        assertThat(javaFileSearch, hasItem(recommendation(javaFileSnippet, 1.0)));
        assertThat(javaStatementsSearch, hasItem(recommendation(javaSnippet, 1.0)));
        assertThat(javaStatementsSearch, hasItem(recommendation(javaStatementsSnippet, 1.0)));
        assertThat(javaStatementsSearch.size(), is(4));

        List<Recommendation<ISnippet>> javaTypeMembersSearch = sut
                .search(new SearchContext("snippet", JAVA_TYPE_MEMBERS, FILENAME, EMPTY_CLASSPATH));
        assertThat(javaTypeMembersSearch, hasItem(recommendation(fileSnippet, 1.0)));
        assertThat(javaFileSearch, hasItem(recommendation(javaFileSnippet, 1.0)));
        assertThat(javaTypeMembersSearch, hasItem(recommendation(javaSnippet, 1.0)));
        assertThat(javaTypeMembersSearch, hasItem(recommendation(javaTypeMembersSnippet, 1.0)));
        assertThat(javaTypeMembersSearch.size(), is(4));

        List<Recommendation<ISnippet>> javadocSearch = sut
                .search(new SearchContext("snippet", JAVADOC, FILENAME, EMPTY_CLASSPATH));
        assertThat(javadocSearch, hasItem(recommendation(fileSnippet, 1.0)));
        assertThat(javaFileSearch, hasItem(recommendation(javaFileSnippet, 1.0)));
        assertThat(javadocSearch, hasItem(recommendation(javadocSnippet, 1.0)));
        assertThat(javadocSearch.size(), is(3));

        sut.close();
    }

    @Test
    public void testSearchWithFilenameRestriction() throws Exception {
        ISnippet exactNameMatchSnippet = createSnippetWithFilenameRestrictions(A_UUID, "searchword", "pom.xml");
        storeSnippet(exactNameMatchSnippet);
        ISnippet extensionMatchSnippet = createSnippetWithFilenameRestrictions(ANOTHER_UUID, "searchword", ".xml");
        storeSnippet(extensionMatchSnippet);
        ISnippet unrestrictedSnippet = createSnippet(THIRD_UUID, "searchword");
        storeSnippet(unrestrictedSnippet);
        sut.open();

        List<Recommendation<ISnippet>> searchWithExactMatch = sut
                .search(new SearchContext("searchword", Location.FILE, "pom.xml", EMPTY_CLASSPATH));

        Recommendation<ISnippet> exactNameRecommendation = find(searchWithExactMatch, new UuidPredicate(A_UUID));
        Recommendation<ISnippet> extensionRecommendation = find(searchWithExactMatch, new UuidPredicate(ANOTHER_UUID));
        Recommendation<ISnippet> unrestrictedRecommendation = find(searchWithExactMatch, new UuidPredicate(THIRD_UUID));

        assertThat(exactNameRecommendation.getProposal(), is(equalTo(exactNameMatchSnippet)));
        assertThat(extensionRecommendation.getProposal(), is(equalTo(extensionMatchSnippet)));
        assertThat(unrestrictedRecommendation.getProposal(), is(equalTo(unrestrictedSnippet)));
        assertThat(exactNameRecommendation.getRelevance(), is(greaterThan(extensionRecommendation.getRelevance())));
        assertThat(extensionRecommendation.getRelevance(), is(greaterThan(unrestrictedRecommendation.getRelevance())));
        assertThat(searchWithExactMatch.size(), is(3));

        List<Recommendation<ISnippet>> searchWithExtensionMatch = sut
                .search(new SearchContext("searchword", Location.FILE, "foo.xml", EMPTY_CLASSPATH));

        extensionRecommendation = find(searchWithExtensionMatch, new UuidPredicate(ANOTHER_UUID));
        unrestrictedRecommendation = find(searchWithExtensionMatch, new UuidPredicate(THIRD_UUID));

        assertThat(extensionRecommendation.getProposal(), is(equalTo(extensionMatchSnippet)));
        assertThat(unrestrictedRecommendation.getProposal(), is(equalTo(unrestrictedSnippet)));
        assertThat(extensionRecommendation.getRelevance(), is(greaterThan(unrestrictedRecommendation.getRelevance())));
        assertThat(searchWithExtensionMatch.size(), is(2));

        sut.close();
    }

    @Test
    public void testSearchWithFilenameRestrictionInUppercaseFile() throws Exception {
        ISnippet exactNameMatchSnippet = createSnippetWithFilenameRestrictions(A_UUID, "searchword", "manifest.mf");
        storeSnippet(exactNameMatchSnippet);
        ISnippet extensionMatchSnippet = createSnippetWithFilenameRestrictions(ANOTHER_UUID, "searchword", ".mf");
        storeSnippet(extensionMatchSnippet);
        ISnippet unrestrictedSnippet = createSnippet(THIRD_UUID, "searchword");
        storeSnippet(unrestrictedSnippet);
        sut.open();

        List<Recommendation<ISnippet>> searchWithExactMatch = sut
                .search(new SearchContext("searchword", Location.FILE, "MANIFEST.MF", EMPTY_CLASSPATH));

        Recommendation<ISnippet> exactNameRecommendation = find(searchWithExactMatch, new UuidPredicate(A_UUID));
        Recommendation<ISnippet> extensionRecommendation = find(searchWithExactMatch, new UuidPredicate(ANOTHER_UUID));
        Recommendation<ISnippet> unrestrictedRecommendation = find(searchWithExactMatch, new UuidPredicate(THIRD_UUID));

        assertThat(exactNameRecommendation.getProposal(), is(equalTo(exactNameMatchSnippet)));
        assertThat(extensionRecommendation.getProposal(), is(equalTo(extensionMatchSnippet)));
        assertThat(unrestrictedRecommendation.getProposal(), is(equalTo(unrestrictedSnippet)));
        assertThat(exactNameRecommendation.getRelevance(), is(greaterThan(extensionRecommendation.getRelevance())));
        assertThat(extensionRecommendation.getRelevance(), is(greaterThan(unrestrictedRecommendation.getRelevance())));
        assertThat(searchWithExactMatch.size(), is(3));

        List<Recommendation<ISnippet>> searchWithExtensionMatch = sut
                .search(new SearchContext("searchword", Location.FILE, "Foo.MF", EMPTY_CLASSPATH));

        extensionRecommendation = find(searchWithExtensionMatch, new UuidPredicate(ANOTHER_UUID));
        unrestrictedRecommendation = find(searchWithExtensionMatch, new UuidPredicate(THIRD_UUID));

        assertThat(extensionRecommendation.getProposal(), is(equalTo(extensionMatchSnippet)));
        assertThat(unrestrictedRecommendation.getProposal(), is(equalTo(unrestrictedSnippet)));
        assertThat(extensionRecommendation.getRelevance(), is(greaterThan(unrestrictedRecommendation.getRelevance())));
        assertThat(searchWithExtensionMatch.size(), is(2));

        sut.close();
    }

    @Test
    public void testSearchWithFilenameRestrictionInJavaFile() throws Exception {
        ISnippet nonMatchingSnippet = createSnippetWithFilenameRestrictions(A_UUID, "searchword", ".xml");
        storeSnippet(nonMatchingSnippet);
        ISnippet extensionMatchSnippet = createSnippetWithFilenameRestrictions(ANOTHER_UUID, "searchword", ".java");
        storeSnippet(extensionMatchSnippet);
        Snippet javaSnippetWithUnusedFileRestriction = createSnippetWithFilenameRestrictions(THIRD_UUID, "searchword",
                ".xml");
        javaSnippetWithUnusedFileRestriction.setLocation(JAVA_STATEMENTS);
        storeSnippet(javaSnippetWithUnusedFileRestriction);
        sut.open();

        List<Recommendation<ISnippet>> searchWithExactMatch = sut
                .search(new SearchContext("searchword", JAVA_STATEMENTS, "Example.java", EMPTY_CLASSPATH));

        Recommendation<ISnippet> extensionRecommendation = find(searchWithExactMatch, new UuidPredicate(ANOTHER_UUID));
        Recommendation<ISnippet> javaRecommendation = find(searchWithExactMatch, new UuidPredicate(THIRD_UUID));

        assertThat(extensionRecommendation.getProposal(), is(equalTo(extensionMatchSnippet)));
        assertThat(javaRecommendation.getProposal(), is(equalTo((ISnippet) javaSnippetWithUnusedFileRestriction)));
        assertThat(searchWithExactMatch.size(), is(2));

        sut.close();
    }

    @Test
    public void testPreferNameMatchesOverDescription() throws Exception {
        storeSnippet(createSnippet(A_UUID, "first"));
        storeSnippet(createSnippetWithDescription(ANOTHER_UUID, "second", "first"));
        sut.open();

        List<Recommendation<ISnippet>> result = sut.search(new SearchContext("first"));

        Recommendation<ISnippet> forFirst = find(result, new UuidPredicate(A_UUID));
        Recommendation<ISnippet> forSecond = find(result, new UuidPredicate(ANOTHER_UUID));
        assertThat(forFirst.getRelevance(), is(greaterThan(forSecond.getRelevance())));

        sut.close();
    }

    @Test
    public void testNoPreferenceBetweenDescriptionAndExtraSearchTerms() throws Exception {
        storeSnippet(createSnippetWithDescription(A_UUID, "first", "searchword"));
        storeSnippet(createSnippetWithExtraSearchTerms(ANOTHER_UUID, "second", "searchword"));
        sut.open();

        List<Recommendation<ISnippet>> result = sut.search(new SearchContext("searchword"));

        Recommendation<ISnippet> forFirst = find(result, new UuidPredicate(A_UUID));
        Recommendation<ISnippet> forSecond = find(result, new UuidPredicate(ANOTHER_UUID));
        assertThat(forFirst.getRelevance(), is(equalTo(forSecond.getRelevance())));

        sut.close();
    }

    @Test
    public void testPreferDescriptionMatchesOverTags() throws Exception {
        storeSnippet(createSnippetWithDescription(A_UUID, "first", "searchword"));
        storeSnippet(createSnippetWithTags(ANOTHER_UUID, "second", "searchword"));

        sut.open();

        List<Recommendation<ISnippet>> result = sut.search(new SearchContext("searchword"));

        Recommendation<ISnippet> forFirst = find(result, new UuidPredicate(A_UUID));
        Recommendation<ISnippet> forSecond = find(result, new UuidPredicate(ANOTHER_UUID));
        assertThat(forFirst.getRelevance(), is(greaterThan(forSecond.getRelevance())));

        sut.close();
    }

    @Test
    public void testRelevanceDoesntExceedOne() throws Exception {
        storeSnippet(createSnippet(A_UUID, "searchword"));
        storeSnippet(new Snippet(ANOTHER_UUID, "searchword", "searchWord", ImmutableList.of("searchword"),
                ImmutableList.of("searchword"), "searchWord", Location.FILE, ImmutableList.of("searchword"),
                Collections.<ProjectCoordinate>emptySet()));
        sut.open();

        List<Recommendation<ISnippet>> result = sut.search(new SearchContext("searchword"));
        Recommendation<ISnippet> forFirst = find(result, new UuidPredicate(A_UUID));
        Recommendation<ISnippet> forSecond = find(result, new UuidPredicate(ANOTHER_UUID));

        assertThat(forSecond.getRelevance(), is(Matchers.lessThanOrEqualTo(1.0)));
        assertThat(forSecond.getRelevance(), is(greaterThan(forFirst.getRelevance())));

        sut.close();
    }

    @Test
    public void testEmptyQueryReturnsAllSnippetsOnOneParameterSearch() throws Exception {
        ISnippet firstSnippet = createSnippet(A_UUID, "first");
        ISnippet secondSnippet = createSnippet(ANOTHER_UUID, "second");
        storeSnippet(firstSnippet);
        storeSnippet(secondSnippet);
        sut.open();

        List<Recommendation<ISnippet>> result = sut.search(new SearchContext(""));
        Recommendation<ISnippet> forFirst = find(result, new UuidPredicate(A_UUID));
        Recommendation<ISnippet> forSecond = find(result, new UuidPredicate(ANOTHER_UUID));

        assertThat(forFirst.getProposal(), is(equalTo(firstSnippet)));
        assertThat(forSecond.getProposal(), is(equalTo(secondSnippet)));
        assertThat(result.size(), is(2));

        sut.close();
    }

    @Test
    public void testNumberOfTagsDoesntAffectRelevance() throws Exception {
        storeSnippet(createSnippetWithTags(A_UUID, "first", "tag1"));
        storeSnippet(createSnippetWithTags(ANOTHER_UUID, "second", "tag1", "tag2"));
        sut.open();

        List<Recommendation<ISnippet>> result = sut.search(new SearchContext("tag:tag1"));

        Recommendation<ISnippet> forFirst = find(result, new UuidPredicate(A_UUID));
        Recommendation<ISnippet> forSecond = find(result, new UuidPredicate(ANOTHER_UUID));
        assertThat(forFirst.getRelevance(), is(closeTo(forSecond.getRelevance(), 0.01)));

        sut.close();
    }

    private Snippet createSnippet(UUID uuid, String name) {
        return createSnippetWithLocation(uuid, name, Location.FILE);
    }

    private Snippet createSnippetWithDescription(UUID uuid, String name, String description) {
        return new Snippet(uuid, name, description, Collections.<String>emptyList(), Collections.<String>emptyList(),
                "code", Location.FILE, Collections.<String>emptyList(), Collections.<ProjectCoordinate>emptySet());
    }

    private Snippet createSnippetWithExtraSearchTerms(UUID uuid, String name, String... extraSearchTerms) {
        return new Snippet(uuid, name, "", Arrays.asList(extraSearchTerms), Collections.<String>emptyList(), "code",
                Location.FILE, Collections.<String>emptyList(), Collections.<ProjectCoordinate>emptySet());
    }

    private Snippet createSnippetWithTags(UUID uuid, String name, String... tags) {
        return new Snippet(uuid, name, "", Collections.<String>emptyList(), Arrays.asList(tags), "code", Location.FILE,
                Collections.<String>emptyList(), Collections.<ProjectCoordinate>emptySet());
    }

    private Snippet createSnippetWithLocation(UUID uuid, String name, Location location) {
        List<String> filenameRestrictions = new LinkedList<>();
        filenameRestrictions.add(NO_FILENAME_RESTRICTION);
        return new Snippet(uuid, name, "", Collections.<String>emptyList(), Collections.<String>emptyList(), "code",
                location, filenameRestrictions, Collections.<ProjectCoordinate>emptySet());
    }

    private Snippet createSnippetWithFilenameRestrictions(UUID uuid, String name, String... filenameRestrictions) {
        return new Snippet(uuid, name, "", Collections.<String>emptyList(), Collections.<String>emptyList(), "code",
                Location.FILE, new LinkedList<>(Arrays.asList(filenameRestrictions)),
                Collections.<ProjectCoordinate>emptySet());
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
