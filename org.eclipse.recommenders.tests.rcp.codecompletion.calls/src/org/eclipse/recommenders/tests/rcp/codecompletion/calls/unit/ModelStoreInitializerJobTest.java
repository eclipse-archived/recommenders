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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.recommenders.commons.lfm.Manifest;
import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.commons.utils.VersionRange;
import org.eclipse.recommenders.commons.utils.VersionRange.VersionRangeBuilder;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.ModelStoreInitializer;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.CallsModelIndex;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ModelArchive;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ModelStoreInitializerJobTest {

    private final Version v36 = Version.create(3, 6);
    private final Version v37 = Version.create(3, 7);
    private final VersionRange range_i36_e37 = new VersionRangeBuilder().minInclusive(v36).maxExclusive(v37).build();
    private final CallsModelIndex index = mock(CallsModelIndex.class);
    private final List<ModelArchive> archives = Lists.newLinkedList();

    @Test
    public void testRegisterArchives() throws CoreException {
        final ModelStoreInitializer sut = createSut();
        final ModelArchive archive = createAndAddArchive("org.eclipse.test", range_i36_e37, 1);
        sut.initializeModelIndex();

        verify(index).register(archive);
    }

    @Test
    public void testDetectDeprecatedArchives() {
        final ModelStoreInitializer sut = createSut();
        final ModelArchive oldArchive = createAndAddArchive("org.eclipse.test", range_i36_e37, 1);
        final ModelArchive newArchive = createAndAddArchive("org.eclipse.test", range_i36_e37, 10);
        sut.initializeModelIndex();

        verify(index).register(newArchive);
        verify(index, never()).register(oldArchive);
        verify(oldArchive.getFile()).delete();
    }

    private ModelArchive createAndAddArchive(final String name, final VersionRange range, final int timestamp) {
        final ModelArchive archive = mock(ModelArchive.class);
        final Manifest manifest = new Manifest(name, range, new Date(timestamp));
        when(archive.getManifest()).thenReturn(manifest);

        final File file = mock(File.class);
        when(archive.getFile()).thenReturn(file);

        archives.add(archive);
        return archive;
    }

    private ModelStoreInitializer createSut() {
        return new MockInitializer();
    }

    private class MockInitializer extends ModelStoreInitializer {

        public MockInitializer() {
            super(new File(""), index);
        }

        @Override
        protected List<ModelArchive> loadArchives() {
            return archives;
        }
    }
}
