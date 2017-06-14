/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.internal.models.rcp;

import static com.google.common.base.Optional.of;
import static org.eclipse.recommenders.utils.Constants.EXT_ZIP;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.utils.Pair;
import org.eclipse.recommenders.utils.Uris;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;

@SuppressWarnings("unchecked")
public class EclipseModelIndexTest {

    public static final ProjectCoordinate PC1 = new ProjectCoordinate("org.example", "one", "1.0.0");
    public static final ProjectCoordinate PC2 = new ProjectCoordinate("org.example", "two", "2.0.0");
    public static final ProjectCoordinate PC3 = new ProjectCoordinate("org.example", "three", "3.0.0");
    public static final ProjectCoordinate PC4 = new ProjectCoordinate("org.example", "four", "4.0.0");

    public static final ModelCoordinate MC1 = new ModelCoordinate("org.example", "one", "call", EXT_ZIP, "1.0.0");
    public static final ModelCoordinate MC2 = new ModelCoordinate("org.example", "two", "call", EXT_ZIP, "2.0.0");
    public static final ModelCoordinate MC3 = new ModelCoordinate("org.example", "three", "call", EXT_ZIP, "3.0.0");
    public static final ModelCoordinate MC4 = new ModelCoordinate("org.example", "four", "call", EXT_ZIP, "4.0.0");

    public static final Map<ModelCoordinate, ProjectCoordinate> MC_TO_PC_MAPPING = ImmutableMap.of(MC1, PC1, MC2, PC2,
            MC3, PC3, MC4, PC4);

    public static final Pair<String, ModelCoordinate[]> REPO_1 = Pair.newPair("http://www.example.com/repo1",
            new ModelCoordinate[] { MC1 });
    public static final Pair<String, ModelCoordinate[]> REPO_2 = Pair.newPair("http://www.example.org/repo2",
            new ModelCoordinate[] { MC2 });
    public static final Pair<String, ModelCoordinate[]> REPO_3 = Pair.newPair("http://www.example.com/repo3",
            new ModelCoordinate[] { MC1, MC3 });

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private IModelIndex createMockedModelIndex(final ModelCoordinate... models) {
        IModelIndex mock = mock(IModelIndex.class);

        when(mock.getKnownModels("call")).thenReturn(ImmutableSet.copyOf(models));
        when(mock.suggest(Mockito.any(ProjectCoordinate.class), Mockito.eq("call")))
                .thenReturn(Optional.<ModelCoordinate>absent());

        for (ModelCoordinate mc : models) {
            ProjectCoordinate pc = MC_TO_PC_MAPPING.get(mc);
            when(mock.suggest(Mockito.eq(pc), Mockito.eq("call"))).thenReturn(of(mc));
        }

        return mock;
    }

    private EclipseModelIndex createSUT(Pair<String, ModelCoordinate[]>... configuration) throws IOException {
        File basedir = temporaryFolder.newFolder();
        EventBus bus = mock(EventBus.class);
        ModelsRcpPreferences prefs = new ModelsRcpPreferences(bus);

        final Map<String, IModelIndex> map = new HashMap<>();
        String[] remotes = new String[configuration.length];
        for (int i = 0; i < configuration.length; i++) {
            Pair<String, ModelCoordinate[]> pair = configuration[i];
            String uri = configuration[i].getFirst();
            map.put(Uris.mangle(Uris.toUri(uri)), createMockedModelIndex(pair.getSecond()));
            remotes[i] = uri;
        }

        prefs.remotes = remotes;
        IModelRepository repository = mock(IModelRepository.class);

        EclipseModelIndex sut = spy(new EclipseModelIndex(basedir, prefs, repository, bus));
        sut.startAsync();
        sut.awaitRunning();

        final ArgumentCaptor<File> captor = ArgumentCaptor.forClass(File.class);
        when(sut.createModelIndex(captor.capture())).thenAnswer(new Answer<IModelIndex>() {

            @Override
            public IModelIndex answer(InvocationOnMock invocation) throws Throwable {
                return map.get(captor.getValue().getName());
            }
        });

        sut.openForTesting();
        return sut;
    }

    @Test
    public void testSingleRepositoryContainsSearchedModel() throws IOException {
        EclipseModelIndex sut = createSUT(REPO_1);
        ModelCoordinate mc = sut.suggest(PC1, "call").orNull();
        assertEquals(REPO_1.getFirst(), mc.getHint(ModelCoordinate.HINT_REPOSITORY_URL).orNull());
    }

    @Test
    public void testSingleRepositoryDoesNotContainSearchedModel() throws IOException {
        EclipseModelIndex sut = createSUT(REPO_1);
        Optional<ModelCoordinate> omc = sut.suggest(PC2, "call");
        assertFalse(omc.isPresent());
    }

    @Test
    public void testFirstRepositoryContainsSearchedModel() throws IOException {
        EclipseModelIndex sut = createSUT(REPO_2, REPO_1);
        ModelCoordinate mc = sut.suggest(PC2, "call").orNull();
        assertEquals(REPO_2.getFirst(), mc.getHint(ModelCoordinate.HINT_REPOSITORY_URL).orNull());
    }

    @Test
    public void testSecondRepositoryContainsSearchedModel() throws IOException {
        EclipseModelIndex sut = createSUT(REPO_1, REPO_2);
        ModelCoordinate mc = sut.suggest(PC2, "call").orNull();
        assertEquals(REPO_2.getFirst(), mc.getHint(ModelCoordinate.HINT_REPOSITORY_URL).orNull());
    }

    @Test
    public void testFirstAndSecondRepositoryContainsSearchedModel1() throws IOException {
        EclipseModelIndex sut = createSUT(REPO_1, REPO_3);
        ModelCoordinate mc = sut.suggest(PC1, "call").orNull();
        assertEquals(REPO_1.getFirst(), mc.getHint(ModelCoordinate.HINT_REPOSITORY_URL).orNull());
    }

    @Test
    public void testFirstAndSecondRepositoryContainsSearchedModel2() throws IOException {
        EclipseModelIndex sut = createSUT(REPO_3, REPO_1);
        ModelCoordinate mc = sut.suggest(PC1, "call").orNull();
        assertEquals(REPO_3.getFirst(), mc.getHint(ModelCoordinate.HINT_REPOSITORY_URL).orNull());
    }

    @Test
    public void testAllRepositoriesDoNotContainSearchedModel() throws IOException {
        EclipseModelIndex sut = createSUT(REPO_1, REPO_2, REPO_3);
        Optional<ModelCoordinate> omc = sut.suggest(PC4, "call");
        assertFalse(omc.isPresent());
    }

    @Test
    public void testThirdRepositoryContainsSearchedModel() throws IOException {
        EclipseModelIndex sut = createSUT(REPO_1, REPO_3, REPO_2);
        ModelCoordinate mc = sut.suggest(PC2, "call").orNull();
        assertEquals(REPO_2.getFirst(), mc.getHint(ModelCoordinate.HINT_REPOSITORY_URL).orNull());
    }
}
