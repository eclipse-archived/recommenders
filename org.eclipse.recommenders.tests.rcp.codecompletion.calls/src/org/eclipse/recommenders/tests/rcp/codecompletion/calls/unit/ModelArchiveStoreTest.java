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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;

import org.eclipse.recommenders.commons.lfm.Manifest;
import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.commons.utils.VersionRange;
import org.eclipse.recommenders.commons.utils.VersionRange.VersionRangeBuilder;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.CallsModelIndex;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ModelArchive;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ModelArchiveStore;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class ModelArchiveStoreTest {

    private static final Manifest manifest = createManifest();
    private static File storeLocation = new File("/test/store/location");
    private static File expectedDestinationFile = new File(storeLocation,
            "org.eclipse.test_[3.6.0,3.7.0)_20110612-1230.zip");

    @Test
    public void testOffer() throws IOException {
        // setup:
        final CallsModelIndex index = mockIndex();
        final ModelArchiveStore sut = new ModelArchiveStore(index, storeLocation);
        final File file = mockFile();
        final ModelArchive archive = mockArchive(file);
        // exercise:
        final boolean offerSuccessful = sut.offer(archive);
        // verify:
        assertTrue(offerSuccessful);
        final InOrder inOrder = inOrder(archive, file, index);
        inOrder.verify(archive).close();
        inOrder.verify(file).renameTo(expectedDestinationFile);
        inOrder.verify(archive).open();
        inOrder.verify(index).register(archive);
    }

    private File mockFile() {
        final File file = mock(File.class);
        when(file.exists()).thenReturn(false);
        when(file.renameTo(Mockito.any(File.class))).thenReturn(true);
        return file;
    }

    private ModelArchive mockArchive(final File file) {
        final ModelArchive archive = mock(ModelArchive.class);

        when(archive.getManifest()).thenReturn(manifest);
        when(archive.getFile()).thenReturn(file);

        return archive;
    }

    private static Manifest createManifest() {
        final VersionRange range = new VersionRangeBuilder().minInclusive(Version.create(3, 6))
                .maxExclusive(Version.create(3, 7)).build();
        final GregorianCalendar calendar = new GregorianCalendar(2011, 5, 12, 12, 30);
        final Manifest manifest = new Manifest("org.eclipse.test", range, calendar.getTime());
        return manifest;
    }

    private CallsModelIndex mockIndex() {
        return mock(CallsModelIndex.class);
    }
}
