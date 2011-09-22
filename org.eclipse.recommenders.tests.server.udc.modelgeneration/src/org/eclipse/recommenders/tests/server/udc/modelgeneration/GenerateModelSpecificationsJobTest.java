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
package org.eclipse.recommenders.tests.server.udc.modelgeneration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.recommenders.commons.udc.LibraryIdentifier;
import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.commons.utils.VersionRange;
import org.eclipse.recommenders.commons.utils.VersionRange.VersionRangeBuilder;
import org.eclipse.recommenders.internal.server.udc.CouchDBAccessService;
import org.eclipse.recommernders.server.udc.model.generation.GenerateModelSpecificationsJob;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class GenerateModelSpecificationsJobTest {

    private static Version v36 = Version.create(3, 6);
    private static Version v37 = Version.create(3, 7);
    private static VersionRange vi36_e37 = new VersionRangeBuilder().minInclusive(v36).maxExclusive(v37).build();

    private static String JFACE_SYMBOLIC_NAME = "org.eclipse.jface";
    private static String SWT_SYMBOLIC_NAME = "org.eclipse.swt";
    private static String SWT_WIN_SYMBOLIC_NAME = "org.eclipse.swt.win32.win32.x86_64";

    private static LibraryIdentifier jfaceLibId = new LibraryIdentifier(JFACE_SYMBOLIC_NAME, v36, "123456789");
    private static LibraryIdentifier jfaceUnknownVersionLibId = new LibraryIdentifier(JFACE_SYMBOLIC_NAME,
            Version.UNKNOWN, "890570");
    private static LibraryIdentifier swtWinLibId = new LibraryIdentifier(SWT_WIN_SYMBOLIC_NAME, v36, "abef");

    private static ModelSpecification jfaceModel_3_6 = new ModelSpecification(JFACE_SYMBOLIC_NAME, new String[0],
            vi36_e37, null, new HashSet<String>());
    private static ModelSpecification swtModel_3_6 = new ModelSpecification(SWT_SYMBOLIC_NAME,
            new String[] { SWT_WIN_SYMBOLIC_NAME }, vi36_e37, null, new HashSet<String>());

    private final CouchDBAccessService dataAccess = mock(CouchDBAccessService.class);

    @Test
    public void testHappyPath() {
        // setup:
        final GenerateModelSpecificationsJob sut = createSut();
        when(dataAccess.getModelSpecifications()).thenReturn(new LinkedList<ModelSpecification>());
        when(dataAccess.getLibraryIdentifiers()).thenReturn(Lists.newArrayList(jfaceLibId));
        // exercise:
        sut.execute();
        // verify:
        jfaceModel_3_6.addFingerprint(jfaceLibId.fingerprint);
        verify(dataAccess).save(jfaceModel_3_6);
    }

    @Test
    public void testUnknownVersion() {
        // setup:
        final GenerateModelSpecificationsJob sut = createSut();
        when(dataAccess.getModelSpecifications()).thenReturn(new LinkedList<ModelSpecification>());
        when(dataAccess.getLibraryIdentifiers()).thenReturn(Lists.newArrayList(jfaceUnknownVersionLibId));
        // exercise:
        sut.execute();
        // verify:
        final Set<String> fingerprints = Sets.newHashSet(jfaceUnknownVersionLibId.fingerprint);
        final ModelSpecification modelSpec = new ModelSpecification(jfaceUnknownVersionLibId.name, new String[0],
                VersionRange.EMPTY, null, fingerprints);
        verify(dataAccess).save(modelSpec);
    }

    @Test
    public void testKnownSpecification() {
        // setup:
        final GenerateModelSpecificationsJob sut = createSut();
        when(dataAccess.getModelSpecifications()).thenReturn(Lists.newArrayList(jfaceModel_3_6));
        when(dataAccess.getLibraryIdentifiers()).thenReturn(Lists.newArrayList(jfaceLibId));
        // exercise:
        sut.execute();
        // verify:
        jfaceModel_3_6.addFingerprint(jfaceLibId.fingerprint);
        verify(dataAccess).save(jfaceModel_3_6);
    }

    @Test
    public void testKnownAlias() {
        // setup:
        final GenerateModelSpecificationsJob sut = createSut();
        when(dataAccess.getModelSpecifications()).thenReturn(Lists.newArrayList(swtModel_3_6));
        when(dataAccess.getLibraryIdentifiers()).thenReturn(Lists.newArrayList(swtWinLibId));
        // exercise:
        sut.execute();
        // verify:
        swtModel_3_6.addFingerprint(swtWinLibId.fingerprint);
        verify(dataAccess).save(swtModel_3_6);
    }

    private GenerateModelSpecificationsJob createSut() {
        final Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(CouchDBAccessService.class).toInstance(dataAccess);
            }
        });
        return injector.getInstance(GenerateModelSpecificationsJob.class);
    }
}
