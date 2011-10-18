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
package org.eclipse.recommenders.tests.rcp.codecompletion.calls.unit;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.udc.ClasspathDependencyInformation;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.commons.utils.Option;
import org.eclipse.recommenders.commons.utils.VersionRange;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.CallsModelResolver;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.CallsModelResolver.OverridePolicy;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ClasspathDependencyStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.IModelArchive;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ModelArchiveStore;
import org.junit.Test;

public class CallsModelResolverTest {

    private static ClasspathDependencyInformation dependencyInfo = new ClasspathDependencyInformation();
    private static File tempModelFile = new File("test-model-dummy.zip");
    private static File jarFile = new File("test.jar");
    private static Manifest manifest = new Manifest("custom", VersionRange.EMPTY, new Date(0));

    @Test
    public void testNoOverride() {
        final ClasspathDependencyStore dependencyStore = createDependencyStore(true, true);
        final ModelArchiveStore modelStore = createModelStore(true);
        final CallsModelResolver sut = createSut(dependencyStore, modelStore);
        sut.resolve(jarFile, OverridePolicy.NONE);

        verify(dependencyStore, never()).putClasspathDependencyInfo(eq(jarFile),
                any(ClasspathDependencyInformation.class));
    }

    @Test
    public void testDontOverrideDependencyInfo() throws IOException {
        final ClasspathDependencyStore dependencyStore = createDependencyStore(true, true);
        final ModelArchiveStore modelStore = createModelStore(true);
        final CallsModelResolver sut = createSut(dependencyStore, modelStore);
        sut.resolve(jarFile, OverridePolicy.MANIFEST);

        verify(dependencyStore, never()).putClasspathDependencyInfo(eq(jarFile),
                any(ClasspathDependencyInformation.class));
        verify(dependencyStore).putManifest(jarFile, manifest);
        verify(modelStore).register(tempModelFile);
    }

    @Test
    public void testOverrideAll() throws IOException {
        final ClasspathDependencyStore dependencyStore = createDependencyStore(true, true);
        final ModelArchiveStore modelStore = createModelStore(true);
        final CallsModelResolver sut = createSut(dependencyStore, modelStore);
        sut.resolve(jarFile, OverridePolicy.ALL);

        verify(dependencyStore).putClasspathDependencyInfo(jarFile, dependencyInfo);
        verify(dependencyStore).putManifest(jarFile, manifest);
        verify(modelStore).register(tempModelFile);
    }

    private ClasspathDependencyStore createDependencyStore(final boolean withDependencyMatch,
            final boolean withManifestMatch) {
        final ClasspathDependencyStore dependencyStore = mock(ClasspathDependencyStore.class);
        when(dependencyStore.containsClasspathDependencyInfo(jarFile)).thenReturn(withDependencyMatch);
        if (withDependencyMatch) {
            when(dependencyStore.getClasspathDependencyInfo(jarFile)).thenReturn(new ClasspathDependencyInformation());
        }
        when(dependencyStore.containsManifest(jarFile)).thenReturn(withManifestMatch);
        if (withManifestMatch) {
            when(dependencyStore.getManifest(jarFile)).thenReturn(manifest);
        }
        return dependencyStore;
    }

    private ModelArchiveStore createModelStore(final boolean withMatch) {
        final ModelArchiveStore modelStore = mock(ModelArchiveStore.class);
        if (withMatch) {
            final IModelArchive archive = mock(IModelArchive.class);
            when(modelStore.getModelArchive(manifest)).thenReturn(archive);
        } else {
            when(modelStore.getModelArchive(manifest)).thenReturn(IModelArchive.NULL);
        }
        return modelStore;
    }

    private CallsModelResolver createSut(final ClasspathDependencyStore dependencyStore,
            final ModelArchiveStore modelStore) {
        final ClientConfiguration config = ClientConfiguration.create("http://test.url");
        return new CallsModelResolverMock(dependencyStore, modelStore, config);
    }

    private class CallsModelResolverMock extends CallsModelResolver {
        public CallsModelResolverMock(final ClasspathDependencyStore dependencyStore,
                final ModelArchiveStore modelStore, final ClientConfiguration config) {
            super(dependencyStore, modelStore, config);
        }

        @Override
        protected Option<Manifest> findManifest(final ClasspathDependencyInformation dependencyInfo) {
            return Option.wrap(manifest);
        }

        @Override
        protected File downloadModel(final Manifest manifest) throws IOException {
            return tempModelFile;
        }

        @Override
        public Option<ClasspathDependencyInformation> tryExtractClasspathDependencyInfo(final File file) {
            return Option.wrap(dependencyInfo);
        }
    }
}
