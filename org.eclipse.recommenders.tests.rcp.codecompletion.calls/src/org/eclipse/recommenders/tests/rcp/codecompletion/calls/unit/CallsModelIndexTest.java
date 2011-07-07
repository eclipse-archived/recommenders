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

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.commons.utils.VersionRange;
import org.eclipse.recommenders.commons.utils.VersionRange.VersionRangeBuilder;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.CallsModelIndex;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.IModelArchive;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.LibraryIdentifier;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.Manifest;
import org.junit.Test;

public class CallsModelIndexTest {

    static final String NAME_ECLIPSE = "org.eclipse";
    Version v23 = Version.create(2, 3);
    Version v24 = Version.create(2, 4);
    Version v35 = Version.create(3, 5);
    Version v36 = Version.create(3, 6);
    Version v362 = Version.create(3, 6, 2);
    Version v37 = Version.create(3, 7);
    Version v38 = Version.create(3, 8);
    Version v39 = Version.create(3, 9);

    VersionRange vi23_e24 = new VersionRangeBuilder().minInclusive(v23).maxExclusive(v24).build();
    VersionRange vi35_e36 = new VersionRangeBuilder().minInclusive(v35).maxExclusive(v36).build();
    VersionRange vi36_e37 = new VersionRangeBuilder().minInclusive(v36).maxExclusive(v37).build();
    VersionRange vi37_e38 = new VersionRangeBuilder().minInclusive(v37).maxExclusive(v38).build();
    VersionRange vi35_e37 = new VersionRangeBuilder().minInclusive(v35).maxExclusive(v37).build();
    VersionRange vi38_e39 = new VersionRangeBuilder().minInclusive(v38).maxExclusive(v39).build();

    IModelArchive androidArchive = createModelArchive("android", vi23_e24, new Date(1));
    IModelArchive eclipseArchive_3_5 = createModelArchive(NAME_ECLIPSE, vi35_e36, new Date(1));
    IModelArchive eclipseArchive_3_6 = createModelArchive(NAME_ECLIPSE, vi36_e37, new Date(1));
    IModelArchive eclipseArchive_3_6_NEWER = createModelArchive(NAME_ECLIPSE, vi36_e37, new Date(100));
    IModelArchive eclipseArchive_3_7 = createModelArchive(NAME_ECLIPSE, vi37_e38, new Date(0));
    IModelArchive eclipseArchive_3_5_3_7 = createModelArchive(NAME_ECLIPSE, vi35_e37, new Date(10));
    IModelArchive eclipseArchive_3_8 = createModelArchive(NAME_ECLIPSE, vi38_e39, new Date(1));

    IPackageFragmentRoot aPackageRoot = createPackageFragmentRoot();
    CallsModelIndex sut = createCallsModelIndex();

    private CallsModelIndex createCallsModelIndex() {
        sut = new CallsModelIndex();
        return sut;
    }

    @Test
    public void testHappyPath() {
        final LibraryIdentifier identifier = createLibraryIdentifier(NAME_ECLIPSE, v36);
        sut.setResolved(aPackageRoot, identifier);
        sut.register(androidArchive);
        sut.register(eclipseArchive_3_6);
        // exercise:
        final IModelArchive actual = sut.getModelArchive(aPackageRoot);
        // verify:
        assertEquals(eclipseArchive_3_6, actual);
    }

    @Test
    public void testUnresolvedFragmentRoot() {
        final LibraryIdentifier identifier = createLibraryIdentifier(NAME_ECLIPSE, v36);
        sut.setResolved(createPackageFragmentRoot(), identifier);
        sut.register(androidArchive);
        sut.register(eclipseArchive_3_6);
        // exercise:
        final IModelArchive actual = sut.getModelArchive(aPackageRoot);
        // verify:
        assertEquals(IModelArchive.NULL, actual);
    }

    @Test
    public void testEclipseLatestVersion() {
        final LibraryIdentifier identifier = createLibraryIdentifier(NAME_ECLIPSE, Version.LATEST);
        sut.setResolved(aPackageRoot, identifier);
        sut.register(androidArchive);
        sut.register(eclipseArchive_3_6);
        // exercise:
        final IModelArchive actual = sut.getModelArchive(aPackageRoot);
        // verify:
        assertEquals(eclipseArchive_3_6, actual);
    }

    @Test
    public void testUnknownVersion() {
        final LibraryIdentifier identifier = createLibraryIdentifier(NAME_ECLIPSE, Version.UNKNOWN);
        sut.setResolved(aPackageRoot, identifier);
        sut.register(androidArchive);
        sut.register(eclipseArchive_3_6);
        sut.register(eclipseArchive_3_7);
        // exercise:
        final IModelArchive actual = sut.getModelArchive(aPackageRoot);
        // verify:
        assertEquals(eclipseArchive_3_7, actual);
    }

    @Test
    public void testUpdatedArchive() {
        sut.register(androidArchive);
        sut.register(eclipseArchive_3_6);
        final LibraryIdentifier identifier = createLibraryIdentifier(NAME_ECLIPSE, v362);
        sut.setResolved(aPackageRoot, identifier);
        final IModelArchive updatedEclipseArchive = createModelArchive(NAME_ECLIPSE, vi36_e37, new Date(100));
        // exercise:
        sut.register(updatedEclipseArchive);
        final IModelArchive actual = sut.getModelArchive(aPackageRoot);
        // verify:
        assertEquals(updatedEclipseArchive, actual);
    }

    @Test
    public void testUnknownVersionForUpdatedArchive() {
        final LibraryIdentifier identifier = createLibraryIdentifier(NAME_ECLIPSE, Version.UNKNOWN);
        sut.setResolved(aPackageRoot, identifier);
        sut.register(androidArchive);
        sut.register(eclipseArchive_3_6);
        sut.register(eclipseArchive_3_6_NEWER);
        // exercise:
        final IModelArchive actual = sut.getModelArchive(aPackageRoot);
        // verify:
        assertEquals(eclipseArchive_3_6_NEWER, actual);
    }

    @Test
    public void testVersionBetweenRanges() {
        final LibraryIdentifier identifier = createLibraryIdentifier(NAME_ECLIPSE, v36);
        sut.setResolved(aPackageRoot, identifier);
        sut.register(eclipseArchive_3_5);
        sut.register(eclipseArchive_3_7);
        sut.register(eclipseArchive_3_8);
        // exercise:
        final IModelArchive actual = sut.getModelArchive(aPackageRoot);
        // verify:
        assertEquals(eclipseArchive_3_7, actual);
    }

    @Test
    public void testOnlyLowerVersionsAvailable() {
        final LibraryIdentifier identifier = createLibraryIdentifier(NAME_ECLIPSE, v38);
        sut.register(eclipseArchive_3_5);
        sut.register(eclipseArchive_3_5_3_7);
        // exercise:
        sut.setResolved(aPackageRoot, identifier);
        final IModelArchive actual = sut.getModelArchive(aPackageRoot);
        // verify:
        assertEquals(eclipseArchive_3_5_3_7, actual);
    }

    @Test
    public void testOnlyLowerVersionsAvailableEqualUpperBound() {
        final LibraryIdentifier identifier = createLibraryIdentifier(NAME_ECLIPSE, v38);
        sut.setResolved(aPackageRoot, identifier);
        sut.register(eclipseArchive_3_6);
        // i35-e37 is newer than i36-e37 and has same range as i36...:
        sut.register(eclipseArchive_3_5_3_7);
        // exercise:
        final IModelArchive actual = sut.getModelArchive(aPackageRoot);
        // verify:
        assertEquals(eclipseArchive_3_5_3_7, actual);
    }

    private LibraryIdentifier createLibraryIdentifier(final String name, final Version version) {
        return new LibraryIdentifier(name, version);
    }

    private IPackageFragmentRoot createPackageFragmentRoot() {
        final IPackageFragmentRoot root = mock(IPackageFragmentRoot.class);
        return root;
    }

    private IModelArchive createModelArchive(final String name, final VersionRange versionRange, final Date timestamp) {
        final IModelArchive archive = mock(IModelArchive.class);
        final Manifest eclipseManifest = new Manifest(name, versionRange, timestamp);
        when(archive.getManifest()).thenReturn(eclipseManifest);
        return archive;
    }
}
