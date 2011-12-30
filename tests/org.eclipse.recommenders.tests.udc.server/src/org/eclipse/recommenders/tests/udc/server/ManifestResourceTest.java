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
package org.eclipse.recommenders.tests.udc.server;

import static org.eclipse.recommenders.tests.udc.server.VersionConstants.v36;
import static org.eclipse.recommenders.tests.udc.server.VersionConstants.vi36_e37;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;

import org.eclipse.recommenders.commons.udc.ClasspathDependencyInformation;
import org.eclipse.recommenders.commons.udc.LibraryIdentifier;
import org.eclipse.recommenders.commons.udc.ManifestMatchResult;
import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.internal.server.udc.CouchDBAccessService;
import org.eclipse.recommenders.server.udc.resources.MetaDataResource;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ManifestResourceTest {

    private static String JFACE_SYMBOLIC_NAME = "org.eclipse.jface";
    private static ModelSpecification jfaceModel_3_6 = new ModelSpecification(JFACE_SYMBOLIC_NAME, new String[0],
            vi36_e37, new Date(100), new HashSet<String>());
    private static ClasspathDependencyInformation jface_3_6 = ClasspathDependencyInformation.create(
            JFACE_SYMBOLIC_NAME, v36, "123456789");
    private static ClasspathDependencyInformation jface_unknown = ClasspathDependencyInformation.create(
            JFACE_SYMBOLIC_NAME, null, "123456789");
    private static ClasspathDependencyInformation unknownFingerprint = ClasspathDependencyInformation.create(null,
            null, "123456789");

    @Test
    public void testHappyPath() {
        // setup:
        final CouchDBAccessService dataAccess = createDataAccessMock();
        final MetaDataResource sut = createSut(dataAccess);
        when(dataAccess.getLibraryIdentifierForFingerprint(jface_3_6.jarFileFingerprint)).thenReturn(
                createLibraryIdentifier(jface_3_6));
        when(dataAccess.getModelSpecificationsByNameOrAlias(JFACE_SYMBOLIC_NAME)).thenReturn(
                Lists.newArrayList(jfaceModel_3_6));
        // exercise:
        final ManifestMatchResult matchResult = sut.searchManifest(jface_3_6);
        // verify:
        assertNotNull(matchResult.bestMatch);
        assertEquals(JFACE_SYMBOLIC_NAME, matchResult.bestMatch.getName());
    }

    @Test
    public void testNoMatch() {
        // setup:
        final CouchDBAccessService dataAccess = createDataAccessMock();
        final MetaDataResource sut = createSut(dataAccess);
        when(dataAccess.getLibraryIdentifierForFingerprint(jface_3_6.jarFileFingerprint)).thenReturn(
                createLibraryIdentifier(jface_3_6));
        when(dataAccess.getModelSpecificationsByNameOrAlias(JFACE_SYMBOLIC_NAME)).thenReturn(
                new LinkedList<ModelSpecification>());
        // exercise:
        final ManifestMatchResult matchResult = sut.searchManifest(jface_3_6);
        // verify:
        assertNull(matchResult.bestMatch);
    }

    @Test
    public void testUnknownLibraryIdentifier() {
        // setup:
        final CouchDBAccessService dataAccess = createDataAccessMock();
        final MetaDataResource sut = createSut(dataAccess);
        when(dataAccess.getLibraryIdentifierForFingerprint(jface_3_6.jarFileFingerprint)).thenReturn(null);
        when(dataAccess.getModelSpecificationsByNameOrAlias(JFACE_SYMBOLIC_NAME)).thenReturn(
                new LinkedList<ModelSpecification>());
        // exercise:
        final ManifestMatchResult matchResult = sut.searchManifest(jface_3_6);
        // verify:
        assertNull(matchResult.bestMatch);
        verify(dataAccess).save(createLibraryIdentifier(jface_3_6));
    }

    @Test
    public void testUnknownVersion() {
        // setup:
        final CouchDBAccessService dataAccess = createDataAccessMock();
        final MetaDataResource sut = createSut(dataAccess);
        when(dataAccess.getLibraryIdentifierForFingerprint(jface_unknown.jarFileFingerprint)).thenReturn(null);
        when(dataAccess.getModelSpecificationsByNameOrAlias(JFACE_SYMBOLIC_NAME)).thenReturn(
                Lists.newArrayList(jfaceModel_3_6));
        // exercise:
        final ManifestMatchResult matchResult = sut.searchManifest(jface_unknown);
        // verify:
        assertNotNull(matchResult.bestMatch);
        assertEquals(JFACE_SYMBOLIC_NAME, matchResult.bestMatch.getName());
    }

    @Test
    public void testUnknownSymbolicName() {
        // setup:
        final CouchDBAccessService dataAccess = createDataAccessMock();
        final MetaDataResource sut = createSut(dataAccess);
        when(dataAccess.getLibraryIdentifierForFingerprint(unknownFingerprint.jarFileFingerprint)).thenReturn(null);
        when(dataAccess.getModelSpecificationsByNameOrAlias("")).thenReturn(new LinkedList<ModelSpecification>());
        // exercise:
        final ManifestMatchResult matchResult = sut.searchManifest(unknownFingerprint);
        // verify:
        assertNull(matchResult.bestMatch);
    }

    private LibraryIdentifier createLibraryIdentifier(final ClasspathDependencyInformation dependencyInfo) {
        return new LibraryIdentifier(dependencyInfo.symbolicName, dependencyInfo.version,
                dependencyInfo.jarFileFingerprint);
    }

    private CouchDBAccessService createDataAccessMock() {
        final CouchDBAccessService dataAccess = mock(CouchDBAccessService.class);
        return dataAccess;
    }

    private MetaDataResource createSut(final CouchDBAccessService dataAccess) {
        final Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(CouchDBAccessService.class).toInstance(dataAccess);
            }
        });
        return injector.getInstance(MetaDataResource.class);
    }
}
