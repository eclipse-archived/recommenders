/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.completion.rcp.calls;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.ManifestResolutionFinished;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.ModelArchiveDownloadFinished;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.ModelArchiveDownloadRequested;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.ModelArchiveRegistered;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.classpath.ManifestResolverInfo;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.models.IModel;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.models.IModelArchive;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.models.ModelArchive;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.models.ModelArchiveStore;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.models.NullModelArchive;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;

public class ModelArchiveStoreTest {

    static final Manifest MANIFEST = new Manifest() {
    };
    static final ManifestResolverInfo MANIFEST_RESOLVER_INFO = new ManifestResolverInfo(MANIFEST, false);

    EventBus bus = Mockito.mock(EventBus.class);

    @Test
    public void testDownloadRequestOnNewManifest() {
        ManifestResolutionFinished e = new ManifestResolutionFinished();
        e.manifestResolverInfo = MANIFEST_RESOLVER_INFO;
        ModelArchiveStore<IModel> sut = createSimpleStore();
        sut.onEvent(e);
        Mockito.verify(bus).post(Mockito.any(ModelArchiveDownloadRequested.class));
    }

    private ModelArchiveStore<IModel> createSimpleStore() {
        ModelArchiveStore<IModel> store = new ModelArchiveStore<IModel>(Files.createTempDir(), null, bus) {
            @Override
            protected ModelArchive registerArchive(final ModelArchiveDownloadFinished event) {
                return mock(ModelArchive.class);
            }
        };
        return store;
    }

    private ModelArchiveStore<IModel> createSimpleStore(final Map<Manifest, IModelArchive> index) {
        ModelArchiveStore<IModel> store = new ModelArchiveStore<IModel>(index, null, bus) {
            @Override
            protected ModelArchive registerArchive(final ModelArchiveDownloadFinished event) {
                return mock(ModelArchive.class);
            }
        };
        return store;
    }

    @Test
    public void testNoDownloadRequestOnKnownManifest() {

        ModelArchiveStore<IModel> sut = createSimpleStore(new HashMap<Manifest, IModelArchive>() {
            {
                put(MANIFEST, mock(IModelArchive.class));
            }
        });

        ManifestResolutionFinished e = new ManifestResolutionFinished();
        e.manifestResolverInfo = MANIFEST_RESOLVER_INFO;
        sut.onEvent(e);

        verifyZeroInteractions(bus);
    }

    @Test
    public void testRegistrationEvent() {
        ModelArchiveStore<IModel> sut = createSimpleStore();
        ModelArchiveDownloadFinished e = new ModelArchiveDownloadFinished();

        sut.onEvent(e);

        verify(bus).post(Mockito.any(ModelArchiveRegistered.class));
    }

    @Test
    public void testGetModelForUnknownManifest() {
        ModelArchiveStore<IModel> sut = createSimpleStore();
        IModelArchive actual = sut.getModelArchive(MANIFEST);

        assertEquals(NullModelArchive.NULL, actual);
        verifyZeroInteractions(bus);
    }

}
