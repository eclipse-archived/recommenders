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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.internal.completion.rcp.calls.ModelStoreCleanup;
import org.eclipse.recommenders.internal.completion.rcp.calls.store.ModelArchive;
import org.eclipse.recommenders.utils.Version;
import org.eclipse.recommenders.utils.VersionRange;
import org.eclipse.recommenders.utils.VersionRange.VersionRangeBuilder;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ModelStoreCleanupTest {

    private final Version v36 = Version.create(3, 6);
    private final Version v37 = Version.create(3, 7);
    private final VersionRange range_i36_e37 = new VersionRangeBuilder().minInclusive(v36).maxExclusive(v37).build();
    private final List<ModelArchive> archives = Lists.newLinkedList();

    @Test
    public void testRegisterArchives() throws CoreException {
        final ModelStoreCleanup sut = createSut();
        final ModelArchive archive = createAndAddArchive("org.eclipse.test", range_i36_e37, 1);
        sut.initializeModelIndex();

        verify(archive.getFile(), never()).delete();
    }

    @Test
    public void testDetectDeprecatedArchives() {
        final ModelStoreCleanup sut = createSut();
        final ModelArchive oldArchive = createAndAddArchive("org.eclipse.test", range_i36_e37, 1);
        final ModelArchive newArchive = createAndAddArchive("org.eclipse.test", range_i36_e37, 10);
        sut.initializeModelIndex();

        verify(newArchive.getFile(), never()).delete();
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

    private ModelStoreCleanup createSut() {
        return new MockInitializer();
    }

    private class MockInitializer extends ModelStoreCleanup {

        public MockInitializer() {
            super(new File(""));
        }

        @Override
        protected List<ModelArchive> loadArchives() {
            return archives;
        }
    }
}
