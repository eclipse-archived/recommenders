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
package org.eclipse.recommernders.tests.server.udc;

import static junit.framework.Assert.assertEquals;
import static org.eclipse.recommernders.tests.server.udc.VersionConstants.v36;
import static org.eclipse.recommernders.tests.server.udc.VersionConstants.v362;
import static org.eclipse.recommernders.tests.server.udc.VersionConstants.v38;
import static org.eclipse.recommernders.tests.server.udc.VersionConstants.vi23_e24;
import static org.eclipse.recommernders.tests.server.udc.VersionConstants.vi35_e36;
import static org.eclipse.recommernders.tests.server.udc.VersionConstants.vi35_e37;
import static org.eclipse.recommernders.tests.server.udc.VersionConstants.vi36_e37;
import static org.eclipse.recommernders.tests.server.udc.VersionConstants.vi37_e38;
import static org.eclipse.recommernders.tests.server.udc.VersionConstants.vi38_e39;

import java.util.Date;

import org.eclipse.recommenders.commons.udc.LibraryIdentifier;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.internal.server.udc.ManifestMatcher;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ManifestMatcherTest {

    static final String NAME_ECLIPSE = "org.eclipse";

    Manifest androidArchive = new Manifest("android", vi23_e24, new Date(1));
    Manifest eclipseArchive_3_5 = new Manifest(NAME_ECLIPSE, vi35_e36, new Date(1));
    Manifest eclipseArchive_3_6 = new Manifest(NAME_ECLIPSE, vi36_e37, new Date(1));
    Manifest eclipseArchive_3_6_NEWER = new Manifest(NAME_ECLIPSE, vi36_e37, new Date(100));
    Manifest eclipseArchive_3_7 = new Manifest(NAME_ECLIPSE, vi37_e38, new Date(0));
    Manifest eclipseArchive_3_5_3_7 = new Manifest(NAME_ECLIPSE, vi35_e37, new Date(10));
    Manifest eclipseArchive_3_8 = new Manifest(NAME_ECLIPSE, vi38_e39, new Date(1));

    private ManifestMatcher createSut(final LibraryIdentifier libraryIdentifier, final Manifest... manifests) {
        return new ManifestMatcher(Lists.newArrayList(manifests), libraryIdentifier, true);
    }

    @Test
    public void testHappyPath() {
        final LibraryIdentifier libId = new LibraryIdentifier(NAME_ECLIPSE, v36, null);
        final ManifestMatcher sut = createSut(libId, androidArchive, eclipseArchive_3_6);
        assertEquals(eclipseArchive_3_6, sut.getBestMatch());
    }

    @Test
    public void testEclipseLatestVersion() {
        final LibraryIdentifier libId = new LibraryIdentifier(NAME_ECLIPSE, Version.LATEST, null);
        final ManifestMatcher sut = createSut(libId, androidArchive, eclipseArchive_3_6);
        assertEquals(eclipseArchive_3_6, sut.getBestMatch());
    }

    @Test
    public void testUnknownVersion() {
        final LibraryIdentifier libId = new LibraryIdentifier(NAME_ECLIPSE, Version.UNKNOWN, null);
        final ManifestMatcher sut = createSut(libId, androidArchive, eclipseArchive_3_6, eclipseArchive_3_7);
        assertEquals(eclipseArchive_3_7, sut.getBestMatch());
    }

    @Test
    public void testUpdatedArchive() {
        final LibraryIdentifier libId = new LibraryIdentifier(NAME_ECLIPSE, v362, null);
        final Manifest updatedEclipseManifest = new Manifest(NAME_ECLIPSE, vi36_e37, new Date(100));
        final ManifestMatcher sut = createSut(libId, androidArchive, eclipseArchive_3_6, updatedEclipseManifest);
        assertEquals(updatedEclipseManifest, sut.getBestMatch());
    }

    @Test
    public void testUnknownVersionForUpdatedArchive() {
        final LibraryIdentifier libId = new LibraryIdentifier(NAME_ECLIPSE, Version.UNKNOWN, null);
        final ManifestMatcher sut = createSut(libId, androidArchive, eclipseArchive_3_6, eclipseArchive_3_6_NEWER);
        assertEquals(eclipseArchive_3_6_NEWER, sut.getBestMatch());
    }

    @Test
    public void testVersionBetweenRanges() {
        final LibraryIdentifier libId = new LibraryIdentifier(NAME_ECLIPSE, v36, null);
        final ManifestMatcher sut = createSut(libId, eclipseArchive_3_5, eclipseArchive_3_7, eclipseArchive_3_8);
        assertEquals(eclipseArchive_3_7, sut.getBestMatch());
    }

    @Test
    public void testOnlyLowerVersionsAvailable() {
        final LibraryIdentifier libId = new LibraryIdentifier(NAME_ECLIPSE, v38, null);
        final ManifestMatcher sut = createSut(libId, eclipseArchive_3_5, eclipseArchive_3_5_3_7);
        assertEquals(eclipseArchive_3_5_3_7, sut.getBestMatch());
    }

    @Test
    public void testOnlyLowerVersionsAvailableEqualUpperBound() {
        final LibraryIdentifier libId = new LibraryIdentifier(NAME_ECLIPSE, v38, null);
        final ManifestMatcher sut = createSut(libId, eclipseArchive_3_6, eclipseArchive_3_5_3_7);
        assertEquals(eclipseArchive_3_5_3_7, sut.getBestMatch());
    }
}
