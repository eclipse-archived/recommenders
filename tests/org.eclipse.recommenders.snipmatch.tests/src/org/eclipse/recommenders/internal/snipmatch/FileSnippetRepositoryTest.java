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
import static java.util.UUID.nameUUIDFromBytes;
import static org.eclipse.recommenders.utils.Constants.DOT_JSON;
import static org.hamcrest.CoreMatchers.is;
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

import com.google.common.collect.Lists;

public class FileSnippetRepositoryTest {

    private static final String SNIPPET_NAME = "snippet";

    private FileSnippetRepository sut;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
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
        Snippet snippet = new Snippet(nameUUIDFromBytes("SnippetToDelete".getBytes()), SNIPPET_NAME, "",
                Collections.<String>emptyList(), Collections.<String>emptyList(), "");
        File snippetFile = storeSnippet(snippet, SNIPPET_NAME);

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
        Snippet snippet = new Snippet(nameUUIDFromBytes("SnippetToKeep".getBytes()), SNIPPET_NAME, "",
                Collections.<String>emptyList(), Collections.<String>emptyList(), "");
        File snippetFile = storeSnippet(snippet, SNIPPET_NAME);

        sut.open();
        boolean wasDeleted = sut.delete(nameUUIDFromBytes("SnippetToDelete".getBytes()));
        List<Recommendation<ISnippet>> search = sut.search(SNIPPET_NAME);
        sut.close();

        assertThat(wasDeleted, is(false));
        assertThat(snippetFile.exists(), is(true));
        assertThat(search.get(0).getProposal(), is((ISnippet) snippet));
        assertThat(search.size(), is(1));
    }

    @Test
    public void testDeleteOneKeepOneSnippet() throws Exception {
        Snippet snippetToDelete = new Snippet(nameUUIDFromBytes("SnippetToDelete".getBytes()), SNIPPET_NAME, "",
                Collections.<String>emptyList(), Collections.<String>emptyList(), "");
        Snippet snippetToKeep = new Snippet(nameUUIDFromBytes("SnippetToKeep".getBytes()), SNIPPET_NAME, "",
                Collections.<String>emptyList(), Collections.<String>emptyList(), "");

        File snippetFileToDelete = storeSnippet(snippetToDelete, "snippetToDelete");
        File snippetFileToKeep = storeSnippet(snippetToKeep, "snippetToKeep");

        sut.open();
        boolean wasDeleted = sut.delete(snippetToDelete.getUuid());
        List<Recommendation<ISnippet>> search = sut.search("snippet*");
        sut.close();

        assertThat(wasDeleted, is(true));
        assertThat(snippetFileToDelete.exists(), is(false));
        assertThat(snippetFileToKeep.exists(), is(true));
        assertThat(search.get(0).getProposal(), is((ISnippet) snippetToKeep));
        assertThat(search.size(), is(1));
    }

    @Test
    public void testHasSnippetCallForExistingUUID() throws Exception {
        UUID uuid = nameUUIDFromBytes("SnippetToKeep".getBytes());
        Snippet snippet = new Snippet(uuid, SNIPPET_NAME, "", Collections.<String>emptyList(),
                Collections.<String>emptyList(), "");
        storeSnippet(snippet, SNIPPET_NAME);

        sut.open();

        assertThat(sut.hasSnippet(uuid), is(true));
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
        ISnippet snippet = new Snippet(nameUUIDFromBytes(snippetName.getBytes()), snippetName, "",
                Collections.<String>emptyList(), Collections.<String>emptyList(), "");
        sut.importSnippet(snippet);

        assertThat(getOnlyElement(sut.getSnippets()).getProposal(), is(snippet));
    }

    @Test
    public void testImportOfNewSnippetIfSnippetWithSameNameAlreadyExists() throws Exception {
        String snippetName = "New Snippet";
        Snippet originalSnippet = new Snippet(nameUUIDFromBytes(snippetName.getBytes()), snippetName, "",
                Collections.<String>emptyList(), Collections.<String>emptyList(), "");
        storeSnippet(originalSnippet, snippetName);
        sut.open();

        Snippet otherSnippet = new Snippet(nameUUIDFromBytes("Other Snippet".getBytes()), snippetName, "",
                Collections.<String>emptyList(), Collections.<String>emptyList(), "");

        sut.importSnippet(otherSnippet);

        assertThat(sut.getSnippets().size(), is(2));
    }

    @Test
    public void testImportSnippetWithModifiedMetaData() throws Exception {
        String snippetName = "New Snippet";
        Snippet originalSnippet1 = new Snippet(nameUUIDFromBytes(snippetName.getBytes()), snippetName, "",
                Collections.<String>emptyList(), Collections.<String>emptyList(), "");
        storeSnippet(originalSnippet1, snippetName);
        Snippet originalSnippet = originalSnippet1;

        sut.open();

        Snippet copiedSnippet = Snippet.copy(originalSnippet);
        copiedSnippet.setKeywords(Lists.newArrayList("Keyword1", "Keyword2"));

        sut.importSnippet(copiedSnippet);

        assertThat(getOnlyElement(sut.getSnippets()).getProposal(), is((ISnippet) copiedSnippet));
    }

    @Test
    public void testImportSnippetWithModifiedCode() throws Exception {
        String snippetName = "New Snippet";
        Snippet originalSnippet1 = new Snippet(nameUUIDFromBytes(snippetName.getBytes()), snippetName, "",
                Collections.<String>emptyList(), Collections.<String>emptyList(), "");
        storeSnippet(originalSnippet1, snippetName);
        Snippet originalSnippet = originalSnippet1;

        sut.open();

        Snippet copiedSnippet = Snippet.copy(originalSnippet);
        copiedSnippet.setCode("Modified Code");
        copiedSnippet.setUUID(nameUUIDFromBytes("ModifiedCodeSnippet".getBytes()));

        sut.importSnippet(copiedSnippet);

        assertThat(sut.getSnippets().size(), is(2));
    }

    private File storeSnippet(Snippet snippet, String name) throws Exception {
        File jsonFile = new File(snippetsDir, name + DOT_JSON);
        GsonUtil.serialize(snippet, jsonFile);
        return jsonFile;
    }
}
