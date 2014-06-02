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
import static org.eclipse.recommenders.utils.Constants.DOT_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eclipse.recommenders.snipmatch.FileSnippetRepository;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
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
import com.google.common.collect.Lists;

public class FileSnippetRepositoryTest {

    private static final List<String> NO_KEYWORDS = Collections.emptyList();
    private static final List<String> NO_TAGS = Collections.emptyList();

    private static final UUID FIRST_UUID = UUID.randomUUID();
    private static final UUID SECOND_UUID = UUID.randomUUID();

    private static final String SNIPPET_NAME = "snippet";

    private FileSnippetRepository sut;

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private File snippetsDir;

    @Before
    public void setUp() throws IOException {
        File baseDir = tempFolder.newFolder();
        snippetsDir = new File(baseDir, "snippets");
        snippetsDir.mkdirs();
        sut = new FileSnippetRepository(baseDir);
    }

    @Test
    public void testDeleteSnippet() throws Exception {
        Snippet snippet = new Snippet(FIRST_UUID, SNIPPET_NAME, "", NO_KEYWORDS, NO_TAGS, "");
        File snippetFile = storeSnippet(snippet);

        sut.open();
        boolean wasDeleted = sut.delete(snippet.getUuid());
        List<Recommendation<ISnippet>> search = sut.search(SNIPPET_NAME);
        sut.close();

        assertThat(wasDeleted, is(true));
        assertThat(snippetFile.exists(), is(false));
        assertThat(search.isEmpty(), is(true));
    }

    @Test
    public void testDontDeleteSnippet() throws Exception {
        Snippet snippet = new Snippet(FIRST_UUID, SNIPPET_NAME, "", NO_KEYWORDS, NO_TAGS, "");
        File snippetFile = storeSnippet(snippet);

        sut.open();
        boolean wasDeleted = sut.delete(SECOND_UUID);
        List<Recommendation<ISnippet>> search = sut.search(SNIPPET_NAME);
        sut.close();

        assertThat(wasDeleted, is(false));
        assertThat(snippetFile.exists(), is(true));
        assertThat(search.get(0).getProposal(), is((ISnippet) snippet));
        assertThat(search.size(), is(1));
    }

    @Test
    public void testDeleteOneKeepOneSnippet() throws Exception {
        Snippet snippetToDelete = new Snippet(FIRST_UUID, SNIPPET_NAME, "", NO_KEYWORDS, NO_TAGS, "");
        Snippet snippetToKeep = new Snippet(SECOND_UUID, SNIPPET_NAME, "", NO_KEYWORDS, NO_TAGS, "");

        File snippetFileToDelete = storeSnippet(snippetToDelete);
        File snippetFileToKeep = storeSnippet(snippetToKeep);

        sut.open();
        boolean wasDeleted = sut.delete(snippetToDelete.getUuid());
        List<Recommendation<ISnippet>> search = sut.search("snippet");
        sut.close();

        assertThat(wasDeleted, is(true));
        assertThat(snippetFileToDelete.exists(), is(false));
        assertThat(snippetFileToKeep.exists(), is(true));
        assertThat(search.get(0).getProposal(), is((ISnippet) snippetToKeep));
        assertThat(search.size(), is(1));
    }

    @Test
    public void testHasSnippetCallForExistingUUID() throws Exception {
        createAndStoreSnippet(FIRST_UUID, SNIPPET_NAME, "", NO_KEYWORDS, NO_TAGS, "");

        sut.open();

        assertThat(sut.hasSnippet(FIRST_UUID), is(true));
    }

    @Test
    public void testHasSnippetCallForNonExistingUUID() throws Exception {
        sut.open();

        assertThat(sut.hasSnippet(UUID.randomUUID()), is(false));
    }

    @Test(expected = IllegalStateException.class)
    public void testCallHasSnippetOnClosedRepo() throws Exception {
        sut.hasSnippet(UUID.randomUUID());
    }

    @Test(expected = IllegalStateException.class)
    public void testCallDeleteOnClosedRepo() throws Exception {
        sut.delete(UUID.randomUUID());
    }

    @Test(expected = IllegalStateException.class)
    public void testCallSearchOnClosedRepo() throws Exception {
        sut.search("");
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
    public void testImportOfNewSnippet() throws Exception {
        sut.open();

        String snippetName = "New Snippet";
        ISnippet snippet = new Snippet(FIRST_UUID, snippetName, "", NO_KEYWORDS, NO_TAGS, "");
        sut.importSnippet(snippet);

        assertThat(getOnlyElement(sut.search("")).getProposal(), is(snippet));
    }

    @Test
    public void testImportOfNewSnippetIfSnippetWithSameNameAlreadyExists() throws Exception {
        String snippetName = "New Snippet";
        Snippet originalSnippet = new Snippet(FIRST_UUID, snippetName, "", NO_KEYWORDS, NO_TAGS, "");
        storeSnippet(originalSnippet);
        sut.open();

        Snippet otherSnippet = new Snippet(SECOND_UUID, snippetName, "", NO_KEYWORDS, NO_TAGS, "");

        sut.importSnippet(otherSnippet);

        assertThat(sut.search("").size(), is(2));
    }

    @Test
    public void testImportSnippetWithModifiedMetaData() throws Exception {
        String snippetName = "New Snippet";
        Snippet originalSnippet1 = new Snippet(FIRST_UUID, snippetName, "", NO_KEYWORDS, NO_TAGS, "");
        storeSnippet(originalSnippet1);
        Snippet originalSnippet = originalSnippet1;

        sut.open();

        Snippet copiedSnippet = Snippet.copy(originalSnippet);
        copiedSnippet.setKeywords(Lists.newArrayList("Keyword1", "Keyword2"));

        sut.importSnippet(copiedSnippet);

        assertThat(getOnlyElement(sut.search("")).getProposal(), is((ISnippet) copiedSnippet));
    }

    @Test
    public void testImportSnippetWithModifiedCode() throws Exception {
        String snippetName = "New Snippet";
        Snippet originalSnippet1 = new Snippet(FIRST_UUID, snippetName, "", NO_KEYWORDS, NO_TAGS, "");
        storeSnippet(originalSnippet1);
        Snippet originalSnippet = originalSnippet1;

        sut.open();

        Snippet copiedSnippet = Snippet.copy(originalSnippet);
        copiedSnippet.setCode("Modified Code");
        copiedSnippet.setUUID(SECOND_UUID);

        sut.importSnippet(copiedSnippet);

        assertThat(sut.search("").size(), is(2));
    }

    @Test
    public void testSearchByName() throws Exception {
        String snippetName = "The snippet";
        ISnippet snippet = createAndStoreSnippet(FIRST_UUID, snippetName, "", NO_KEYWORDS, NO_TAGS, "");

        sut.open();

        assertThat(getOnlyElement(sut.search("name:" + "s")).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search("name:" + "sn")).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search("name:" + "snippet")).getProposal(), is(snippet));
        assertThat(sut.search("name:" + "a").isEmpty(), is(true));
    }

    @Test
    public void testSearchByDescription() throws Exception {
        String snippetName = "New Snippet";
        String snippetDescription = "description";
        ISnippet snippet = createAndStoreSnippet(FIRST_UUID, snippetName, "description", NO_KEYWORDS, NO_TAGS, "");

        sut.open();

        assertThat(getOnlyElement(sut.search("description:" + snippetDescription)).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search("description:" + "d")).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search("description:" + "de")).getProposal(), is(snippet));
        assertThat(sut.search("name:" + snippetDescription).isEmpty(), is(true));
    }

    @Test
    public void testSearchByKeyword() throws Exception {
        String snippetName = "New Snippet";
        List<String> keywords = ImmutableList.of("foo", "bar");
        ISnippet snippet = createAndStoreSnippet(FIRST_UUID, snippetName, "", keywords, NO_TAGS, "");

        sut.open();

        assertThat(getOnlyElement(sut.search("keyword:" + "f")).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search("keyword:" + "foo")).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search("keyword:" + "bar")).getProposal(), is(snippet));
        assertThat(sut.search("keyword:" + "quz").isEmpty(), is(true));
    }

    @Test
    public void testSearchByTag() throws Exception {
        String snippetName = "New Snippet";
        List<String> tags = ImmutableList.of("foo", "bar");
        ISnippet snippet = createAndStoreSnippet(FIRST_UUID, snippetName, "", NO_KEYWORDS, tags, "");

        sut.open();

        assertThat(sut.search("tag:" + "f").isEmpty(), is(true));
        assertThat(getOnlyElement(sut.search("tag:" + "foo")).getProposal(), is(snippet));
        assertThat(getOnlyElement(sut.search("tag:" + "bar")).getProposal(), is(snippet));
        assertThat(sut.search("tag:" + "quz").isEmpty(), is(true));
    }

    @Test
    public void testPreferNameMatchesOverDescription() throws Exception {
        createAndStoreSnippet(FIRST_UUID, "first", "", NO_KEYWORDS, NO_TAGS, "");
        createAndStoreSnippet(SECOND_UUID, "second", "first", NO_KEYWORDS, NO_TAGS, "");

        sut.open();
        List<Recommendation<ISnippet>> result = sut.search("first");

        Recommendation<ISnippet> forFirst = Iterables.tryFind(result, new UuidPredicate(FIRST_UUID)).get();
        Recommendation<ISnippet> forSecond = Iterables.tryFind(result, new UuidPredicate(SECOND_UUID)).get();
        assertThat(forFirst.getRelevance(), is(greaterThan(forSecond.getRelevance())));
    }

    @Test
    public void testNoPreferenceBetweenDescriptionAndKeywords() throws Exception {
        createAndStoreSnippet(FIRST_UUID, "first", "searchword", NO_KEYWORDS, NO_TAGS, "");
        createAndStoreSnippet(SECOND_UUID, "second", "", ImmutableList.of("searchword"), NO_TAGS, "");

        sut.open();
        List<Recommendation<ISnippet>> result = sut.search("searchword");

        Recommendation<ISnippet> forFirst = Iterables.tryFind(result, new UuidPredicate(FIRST_UUID)).get();
        Recommendation<ISnippet> forSecond = Iterables.tryFind(result, new UuidPredicate(SECOND_UUID)).get();
        assertThat(forFirst.getRelevance(), is(equalTo(forSecond.getRelevance())));
    }

    @Test
    public void testPreferDescriptionMatchesOverTags() throws Exception {
        createAndStoreSnippet(FIRST_UUID, "first", "searchword", NO_KEYWORDS, NO_TAGS, "");
        createAndStoreSnippet(SECOND_UUID, "second", "", NO_KEYWORDS, ImmutableList.of("searchword"), "");

        sut.open();
        List<Recommendation<ISnippet>> result = sut.search("searchword");

        Recommendation<ISnippet> forFirst = Iterables.tryFind(result, new UuidPredicate(FIRST_UUID)).get();
        Recommendation<ISnippet> forSecond = Iterables.tryFind(result, new UuidPredicate(SECOND_UUID)).get();
        assertThat(forFirst.getRelevance(), is(greaterThan(forSecond.getRelevance())));
    }

    @Test
    public void testRelevanceDoesntExceedOne() throws Exception {
        createAndStoreSnippet(FIRST_UUID, "searchword", "searchword", ImmutableList.of("searchword"),
                ImmutableList.of("searchword"), "");
        createAndStoreSnippet(SECOND_UUID, "searchword", "", NO_KEYWORDS, NO_TAGS, "");

        sut.open();
        List<Recommendation<ISnippet>> result = sut.search("searchword");
        Recommendation<ISnippet> forFirst = Iterables.tryFind(result, new UuidPredicate(FIRST_UUID)).get();
        Recommendation<ISnippet> forSecond = Iterables.tryFind(result, new UuidPredicate(SECOND_UUID)).get();
        assertThat(forFirst.getRelevance(), is(greaterThan(forSecond.getRelevance())));
    }

    @Test
    public void testEmptyQueryReturnsAllSnippetsOnOneParameterSearch() throws Exception {
        createAndStoreSnippet(FIRST_UUID, SNIPPET_NAME, "", NO_KEYWORDS, NO_TAGS, "");
        createAndStoreSnippet(SECOND_UUID, SNIPPET_NAME, "", NO_KEYWORDS, NO_TAGS, "");

        sut.open();
        List<Recommendation<ISnippet>> result = sut.search("");
        Optional<Recommendation<ISnippet>> forFirst = Iterables.tryFind(result, new UuidPredicate(FIRST_UUID));
        Optional<Recommendation<ISnippet>> forSecond = Iterables.tryFind(result, new UuidPredicate(SECOND_UUID));

        assertThat(forFirst.isPresent(), is(true));
        assertThat(forSecond.isPresent(), is(true));
    }

    @Test
    public void testEmptyQueryReturnsAllSnippetsOnTwoParametersSearch() throws Exception {
        createAndStoreSnippet(FIRST_UUID, SNIPPET_NAME, "", NO_KEYWORDS, NO_TAGS, "");
        createAndStoreSnippet(SECOND_UUID, SNIPPET_NAME, "", NO_KEYWORDS, NO_TAGS, "");

        sut.open();
        List<Recommendation<ISnippet>> result = sut.search("", 2);
        Optional<Recommendation<ISnippet>> forFirst = Iterables.tryFind(result, new UuidPredicate(FIRST_UUID));
        Optional<Recommendation<ISnippet>> forSecond = Iterables.tryFind(result, new UuidPredicate(SECOND_UUID));

        assertThat(forFirst.isPresent(), is(false));
        assertThat(forSecond.isPresent(), is(false));
    }

    private ISnippet createAndStoreSnippet(UUID uuid, String name, String description, List<String> keywords,
            List<String> tags, String code) throws Exception {
        Snippet snippet = new Snippet(uuid, name, description, keywords, tags, code);
        storeSnippet(snippet);
        return snippet;
    }

    private File storeSnippet(Snippet snippet) throws Exception {
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
