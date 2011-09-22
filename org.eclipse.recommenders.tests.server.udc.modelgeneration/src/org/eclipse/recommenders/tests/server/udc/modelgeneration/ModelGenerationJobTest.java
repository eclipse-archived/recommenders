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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.commons.udc.LibraryIdentifier;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.commons.udc.ObjectUsage;
import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.commons.utils.VersionRange;
import org.eclipse.recommenders.commons.utils.VersionRange.VersionRangeBuilder;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.server.udc.CouchDBAccessService;
import org.eclipse.recommernders.server.udc.model.generation.IModelArchiveWriter;
import org.eclipse.recommernders.server.udc.model.generation.ModelGenerationJob;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ModelGenerationJobTest {

    private static Version v36 = Version.create(3, 6);
    private static Version v37 = Version.create(3, 7);
    private static VersionRange vi36_e37 = new VersionRangeBuilder().minInclusive(v36).maxExclusive(v37).build();

    private static String JFACE_SYMBOLIC_NAME = "org.eclipse.jface";
    private static String SWT_SYMBOLIC_NAME = "org.eclipse.swt";
    private static String SWT_WIN_SYMBOLIC_NAME = "org.eclipse.swt.win32.win32.x86_64";

    private static ModelSpecification jfaceModel_3_6 = new ModelSpecification(JFACE_SYMBOLIC_NAME, new String[0],
            vi36_e37, new Date(100), new HashSet<String>());
    private static ModelSpecification swtModel_3_6 = new ModelSpecification(SWT_SYMBOLIC_NAME,
            new String[] { SWT_WIN_SYMBOLIC_NAME }, vi36_e37, null, new HashSet<String>());

    private static LibraryIdentifier jfaceLibId = new LibraryIdentifier(JFACE_SYMBOLIC_NAME, v36, "123456789");
    private static LibraryIdentifier swtLibId = new LibraryIdentifier(SWT_SYMBOLIC_NAME, v36, "987654");
    private static LibraryIdentifier swtWinLibId = new LibraryIdentifier(SWT_WIN_SYMBOLIC_NAME, v36, "abef");

    private static String typeDialog = "Lorg/eclipse/jface/dialogs/Dialog";
    private static String typeButton = "Lorg/eclipse/swt/widgets/Button";

    private static List<ObjectUsage> jfaceObjectUsages = createJFaceObjectUsages();
    private static List<ObjectUsage> swtObjectUsages = createSwtObjectUsages();

    private final CouchDBAccessService dataAccess = mock(CouchDBAccessService.class);
    private final IModelArchiveWriter writer = mock(IModelArchiveWriter.class);

    @Test
    public void testHappyPath() throws IOException {
        // setup:
        final ModelGenerationJob sut = createSut();
        when(dataAccess.getModelSpecifications()).thenReturn(Lists.newArrayList(jfaceModel_3_6));
        when(dataAccess.getLibraryIdentifiersForSymbolicName(JFACE_SYMBOLIC_NAME)).thenReturn(
                Lists.newArrayList(jfaceLibId));
        final HashSet<String> fingerprints = Sets.newHashSet(jfaceLibId.fingerprint);
        when(dataAccess.getLatestTimestampForFingerprints(fingerprints)).thenReturn(new Date(200));
        when(dataAccess.getObjectUsages(fingerprints)).thenReturn(jfaceObjectUsages);
        // exercise:
        sut.createModels();
        // verify:
        verify(dataAccess).save(any(ModelSpecification.class));
        verify(writer).consume(any(Manifest.class));
        verify(writer).consume(eq(VmTypeName.get(typeDialog)), any(BayesianNetwork.class));
        verify(writer).close();
    }

    @Test
    public void testNoNewObjectUsages() throws IOException {
        // setup:
        final ModelGenerationJob sut = createSut();
        when(dataAccess.getModelSpecifications()).thenReturn(Lists.newArrayList(jfaceModel_3_6));
        when(dataAccess.getLibraryIdentifiersForSymbolicName(JFACE_SYMBOLIC_NAME)).thenReturn(
                Lists.newArrayList(jfaceLibId));
        final HashSet<String> fingerprints = Sets.newHashSet(jfaceLibId.fingerprint);
        when(dataAccess.getLatestTimestampForFingerprints(fingerprints))
                .thenReturn(jfaceModel_3_6.getLastBuilt().get());
        // exercise:
        sut.createModels();
        // verify:
        verify(dataAccess, never()).getObjectUsages(fingerprints);
        verify(dataAccess, never()).save(any(ModelSpecification.class));
        verify(writer, never()).consume(any(Manifest.class));
    }

    @Test
    public void testNeverBuiltBeforeAndNoObjectUsages() throws IOException {
        // setup:
        final ModelGenerationJob sut = createSut();
        when(dataAccess.getModelSpecifications()).thenReturn(Lists.newArrayList(swtModel_3_6));
        when(dataAccess.getLibraryIdentifiersForSymbolicName(SWT_SYMBOLIC_NAME)).thenReturn(
                Lists.newArrayList(swtLibId));
        final HashSet<String> fingerprints = Sets.newHashSet(swtLibId.fingerprint);
        when(dataAccess.getLatestTimestampForFingerprints(fingerprints)).thenReturn(new Date(0));
        when(dataAccess.getObjectUsages(fingerprints)).thenReturn(new LinkedList<ObjectUsage>());
        // exercise:
        sut.createModels();
        // verify:
        verify(dataAccess, never()).save(any(ModelSpecification.class));
        verify(writer, never()).consume(any(Manifest.class));
    }

    @Test
    public void testAliases() throws IOException {
        // setup:
        final ModelGenerationJob sut = createSut();
        when(dataAccess.getModelSpecifications()).thenReturn(Lists.newArrayList(swtModel_3_6));
        when(dataAccess.getLibraryIdentifiersForSymbolicName(SWT_SYMBOLIC_NAME)).thenReturn(
                Lists.newArrayList(swtLibId));
        when(dataAccess.getLibraryIdentifiersForSymbolicName(SWT_WIN_SYMBOLIC_NAME)).thenReturn(
                Lists.newArrayList(swtWinLibId));
        final HashSet<String> fingerprints = Sets.newHashSet(swtLibId.fingerprint, swtWinLibId.fingerprint);
        when(dataAccess.getLatestTimestampForFingerprints(fingerprints)).thenReturn(new Date(200));
        when(dataAccess.getObjectUsages(fingerprints)).thenReturn(swtObjectUsages);
        // exercise:
        sut.createModels();
        // verify:
        verify(dataAccess).save(any(ModelSpecification.class));
        verify(writer).consume(any(Manifest.class));
        verify(writer).consume(eq(VmTypeName.get(typeButton)), any(BayesianNetwork.class));
        verify(writer).close();
    }

    private static List<ObjectUsage> createJFaceObjectUsages() {
        final List<ObjectUsage> result = Lists.newLinkedList();
        result.add(new ObjectUsageBuilder(typeDialog).addSameTypeCalls("cancelPressed()V", "buttonPressed(I)V").build());
        return result;
    }

    private static List<ObjectUsage> createSwtObjectUsages() {
        final List<ObjectUsage> result = Lists.newLinkedList();
        result.add(new ObjectUsageBuilder(typeButton).addSameTypeCalls("setText(Ljava/lang/String;)V").build());
        return result;
    }

    private ModelGenerationJob createSut() {
        final ModelGenerationJob sut = new ModelGenerationJob(new File("dummy"), false) {
            @Override
            protected IModelArchiveWriter createModelWriter(final Manifest manifest) throws IOException {
                return writer;
            };

            @Override
            protected void deletePreviousModelFile() {
            }
        };
        final Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(CouchDBAccessService.class).toInstance(dataAccess);
            }
        });
        injector.injectMembers(sut);
        return sut;
    }
}
