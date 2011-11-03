/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.tests.mining.calls;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.recommenders.commons.utils.Version.create;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.eclipse.recommenders.commons.udc.LibraryIdentifier;
import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.commons.utils.VersionRange;
import org.eclipse.recommenders.mining.calls.data.couch.CouchDbDataAccess;
import org.eclipse.recommenders.mining.calls.data.couch.NewObjectUsagesAvailablePredicate;
import org.junit.Test;

public class NewObjectUsagesAvailablePredicateTest {

    private static final Date BEFORE_SPEC_DATE = new Date(0);
    private static final Date SPEC_DATE = new Date(100);
    private static final Date AFTER_SPEC_DATE = new Date(200);

    final ModelSpecification spec = new ModelSpecification("name", new String[0], VersionRange.create("[0.0.0,3.3.3)"),
            SPEC_DATE, new HashSet<String>());

    final List<LibraryIdentifier> libIds = newArrayList(new LibraryIdentifier("", create(2, 0), "dummy-fp"));

    @Test
    public void testForce() {
        final CouchDbDataAccess db = mock(CouchDbDataAccess.class);
        final NewObjectUsagesAvailablePredicate sut = new NewObjectUsagesAvailablePredicate(db, true);
        assertTrue(sut.apply(spec));
    }

    @Test
    public void testNoMatchingLibIdentifiers() {
        final CouchDbDataAccess db = mock(CouchDbDataAccess.class);
        final NewObjectUsagesAvailablePredicate sut = new NewObjectUsagesAvailablePredicate(db, false);
        assertFalse(sut.apply(spec));
    }

    @Test
    public void testWithOneMatchingLibIdentifierWithNewData() {
        final CouchDbDataAccess db = mock(CouchDbDataAccess.class);
        when(db.getLibraryIdentifiersForSymbolicName(anyString())).thenReturn(libIds);
        when(db.getLatestTimestampForFingerprint("dummy-fp")).thenReturn(AFTER_SPEC_DATE);

        final NewObjectUsagesAvailablePredicate sut = new NewObjectUsagesAvailablePredicate(db, false);

        assertTrue(sut.apply(spec));
    }

    @Test
    public void testWithOneMatchingLibIdentifierButNoNewData() {
        final CouchDbDataAccess db = mock(CouchDbDataAccess.class);
        when(db.getLibraryIdentifiersForSymbolicName(anyString())).thenReturn(libIds);
        when(db.getLatestTimestampForFingerprint("dummy-fp")).thenReturn(BEFORE_SPEC_DATE);

        final NewObjectUsagesAvailablePredicate sut = new NewObjectUsagesAvailablePredicate(db, false);

        assertFalse(sut.apply(spec));
    }
}
