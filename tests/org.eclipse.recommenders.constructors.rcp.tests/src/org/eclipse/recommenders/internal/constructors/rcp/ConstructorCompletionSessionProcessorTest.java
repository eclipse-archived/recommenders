/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.constructors.rcp;

import static org.eclipse.recommenders.testing.rcp.completion.SimpleProposalProcessorMatcher.processorWithBoostAndLabel;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnArgumentName;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.recommenders.completion.rcp.IProposalNameProvider;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.OverlayImageProposalProcessor;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableJavaCompletionProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProposalProcessorManager;
import org.eclipse.recommenders.completion.rcp.processable.ProposalTag;
import org.eclipse.recommenders.constructors.ConstructorModel;
import org.eclipse.recommenders.constructors.IConstructorModelProvider;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.utils.Nullable;
import org.eclipse.recommenders.utils.Result;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

@SuppressWarnings("restriction")
public class ConstructorCompletionSessionProcessorTest {

    private static final ProjectCoordinate JRE_1_6_0 = new ProjectCoordinate("jre", "jre", "1.6.0");

    private static final ITypeName OBJECT = VmTypeName.get("Ljava/lang/Object");

    private static final IMethodName OBJECT_INIT = VmMethodName.get("Ljava/lang/Object.<init>()V");
    private static final IMethodName STRING_INIT = VmMethodName.get("Ljava/lang/String.<init>()V");

    private static final IType NO_TYPE = null;
    private static final ConstructorModel NO_MODEL = null;

    private final IType objectType = mock(IType.class);

    private final IJavaCompletionProposal objectInitProposal = mock(IJavaCompletionProposal.class);
    private final IJavaCompletionProposal stringInitProposal = mock(IJavaCompletionProposal.class);

    private final CompletionProposal objectInitCoreProposal = mock(CompletionProposal.class);
    private final CompletionProposal stringInitCoreProposal = mock(CompletionProposal.class);

    private final IProcessableProposal objectInitProcessableProposal = mock(ProcessableJavaCompletionProposal.class);
    private final IProcessableProposal stringInitProcessableProposal = mock(ProcessableJavaCompletionProposal.class);

    private IProjectCoordinateProvider pcProvider;
    private IConstructorModelProvider modelProvider;
    private IProposalNameProvider methodNameProvider;
    private IRecommendersCompletionContext context;

    @Before
    public void setUp() {
        when(objectInitCoreProposal.getKind()).thenReturn(CompletionProposal.CONSTRUCTOR_INVOCATION);
        when(stringInitCoreProposal.getKind()).thenReturn(CompletionProposal.CONSTRUCTOR_INVOCATION);

        when(objectInitProcessableProposal.getCoreProposal()).thenReturn(Optional.of(objectInitCoreProposal));
        when(stringInitProcessableProposal.getCoreProposal()).thenReturn(Optional.of(stringInitCoreProposal));
    }

    @Test
    public void testUnsupportedCompletionLocation() {
        setUpCompletionScenario(CompletionOnArgumentName.class, objectType,
                ImmutableMap.of(objectInitProposal, objectInitCoreProposal));

        setUpModelRepository(objectType, new UniqueTypeName(JRE_1_6_0, OBJECT),
                new ConstructorModel(OBJECT, Collections.<IMethodName, Integer>emptyMap()));

        ConstructorCompletionSessionProcessor sut = new ConstructorCompletionSessionProcessor(pcProvider, modelProvider,
                methodNameProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));

        verifyZeroInteractions(modelProvider);
    }

    @Test
    public void testNoExpectedType() {
        setUpCompletionScenario(CompletionOnSingleTypeReference.class, NO_TYPE,
                ImmutableMap.of(objectInitProposal, objectInitCoreProposal));

        setUpModelRepository(objectType, new UniqueTypeName(JRE_1_6_0, OBJECT),
                new ConstructorModel(OBJECT, Collections.<IMethodName, Integer>emptyMap()));

        ConstructorCompletionSessionProcessor sut = new ConstructorCompletionSessionProcessor(pcProvider, modelProvider,
                methodNameProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));

        verifyZeroInteractions(modelProvider);
    }

    @Test
    public void testNoModelFound() {
        setUpCompletionScenario(CompletionOnSingleTypeReference.class, objectType,
                ImmutableMap.of(objectInitProposal, objectInitCoreProposal));

        setUpModelRepository(objectType, new UniqueTypeName(JRE_1_6_0, OBJECT), NO_MODEL);

        ConstructorCompletionSessionProcessor sut = new ConstructorCompletionSessionProcessor(pcProvider, modelProvider,
                methodNameProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));
    }

    @Test
    public void testNoRecommendations() {
        setUpCompletionScenario(CompletionOnSingleTypeReference.class, objectType,
                ImmutableMap.of(objectInitProposal, objectInitCoreProposal));

        ConstructorModel model = new ConstructorModel(OBJECT, Collections.<IMethodName, Integer>emptyMap());
        setUpModelRepository(objectType, new UniqueTypeName(JRE_1_6_0, OBJECT), model);

        ConstructorCompletionSessionProcessor sut = new ConstructorCompletionSessionProcessor(pcProvider, modelProvider,
                methodNameProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));

        verify(modelProvider).releaseModel(model);
    }

    @Test
    public void testBogusRecommendationWithZeroFrequency() {
        setUpCompletionScenario(CompletionOnSingleTypeReference.class, objectType,
                ImmutableMap.of(objectInitProposal, objectInitCoreProposal));

        ConstructorModel model = new ConstructorModel(OBJECT, ImmutableMap.of(OBJECT_INIT, 0));
        setUpModelRepository(objectType, new UniqueTypeName(JRE_1_6_0, OBJECT), model);

        ConstructorCompletionSessionProcessor sut = new ConstructorCompletionSessionProcessor(pcProvider, modelProvider,
                methodNameProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));

        verify(modelProvider).releaseModel(model);
    }

    @Test
    public void testRecommendationRelevanceBoostAndDecorations() throws Exception {
        setUpCompletionScenario(CompletionOnSingleTypeReference.class, objectType,
                ImmutableMap.of(objectInitProposal, objectInitCoreProposal));

        ConstructorModel model = new ConstructorModel(OBJECT, ImmutableMap.of(OBJECT_INIT, 1));
        setUpModelRepository(objectType, new UniqueTypeName(JRE_1_6_0, OBJECT), model);

        ConstructorCompletionSessionProcessor sut = new ConstructorCompletionSessionProcessor(pcProvider, modelProvider,
                methodNameProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(true)));

        ProposalProcessorManager manager = mock(ProposalProcessorManager.class);
        when(objectInitProcessableProposal.getProposalProcessorManager()).thenReturn(manager);
        sut.process(objectInitProcessableProposal);

        verify(objectInitProcessableProposal).setTag(ProposalTag.RECOMMENDERS_SCORE, 100.0);
        verify(manager, times(1)).addProcessor(processorWithBoostAndLabel(200, "100%"));
        verify(manager, times(1)).addProcessor(isA(OverlayImageProposalProcessor.class));

        verify(modelProvider).releaseModel(model);
    }

    @Test
    public void testRecommendationWithoutRelevanceBoostOrDecorations() throws Exception {
        setUpCompletionScenario(CompletionOnSingleTypeReference.class, objectType,
                ImmutableMap.of(objectInitProposal, objectInitCoreProposal));

        ConstructorModel model = new ConstructorModel(OBJECT, ImmutableMap.of(OBJECT_INIT, 1));
        setUpModelRepository(objectType, new UniqueTypeName(JRE_1_6_0, OBJECT), model);

        ConstructorsRcpPreferences preferences = createPreferences(0, 1, false, false, false);

        ConstructorCompletionSessionProcessor sut = new ConstructorCompletionSessionProcessor(pcProvider, modelProvider,
                methodNameProvider, preferences, new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(true)));

        ProposalProcessorManager manager = mock(ProposalProcessorManager.class);
        when(objectInitProcessableProposal.getProposalProcessorManager()).thenReturn(manager);
        sut.process(objectInitProcessableProposal);

        verify(objectInitProcessableProposal, never()).setTag(any(ProposalTag.class), anyDouble());
        verifyZeroInteractions(manager);

        verify(modelProvider).releaseModel(model);
    }

    @Test
    public void testOnlySomeRecommendationAboveRelevanceThreshold() throws Exception {
        setUpCompletionScenario(CompletionOnSingleTypeReference.class, objectType, ImmutableMap.of(objectInitProposal,
                objectInitCoreProposal, stringInitProposal, stringInitCoreProposal));

        ConstructorModel model = new ConstructorModel(OBJECT, ImmutableMap.of(OBJECT_INIT, 3, STRING_INIT, 1));
        setUpModelRepository(objectType, new UniqueTypeName(JRE_1_6_0, OBJECT), model);

        ConstructorsRcpPreferences preferences = createPreferences(50, 2, true, true, true);

        ConstructorCompletionSessionProcessor sut = new ConstructorCompletionSessionProcessor(pcProvider, modelProvider,
                methodNameProvider, preferences, new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(true)));

        ProposalProcessorManager objectInitManager = mock(ProposalProcessorManager.class);
        when(objectInitProcessableProposal.getProposalProcessorManager()).thenReturn(objectInitManager);
        sut.process(objectInitProcessableProposal);

        verify(objectInitProcessableProposal).setTag(ProposalTag.RECOMMENDERS_SCORE, 75.0);
        verify(objectInitManager, times(1)).addProcessor(processorWithBoostAndLabel(175, "75%"));
        verify(objectInitManager, times(1)).addProcessor(isA(OverlayImageProposalProcessor.class));

        ProposalProcessorManager stringInitManager = mock(ProposalProcessorManager.class);
        when(stringInitProcessableProposal.getProposalProcessorManager()).thenReturn(stringInitManager);
        sut.process(stringInitProcessableProposal);

        verify(stringInitProcessableProposal, never()).setTag(any(ProposalTag.class), anyDouble());
        verifyZeroInteractions(stringInitManager);

        verify(modelProvider).releaseModel(model);
    }

    private void setUpCompletionScenario(Class<? extends ASTNode> completionType, @Nullable IType expectedType,
            Map<IJavaCompletionProposal, CompletionProposal> proposals) {
        context = mock(IRecommendersCompletionContext.class);
        Optional<ASTNode> completionNode = completionType == null ? Optional.<ASTNode>absent()
                : Optional.<ASTNode>of(mock(completionType));
        when(context.getCompletionNode()).thenReturn(completionNode);
        when(context.getExpectedType()).thenReturn(Optional.fromNullable(expectedType));
        when(context.getProposals()).thenReturn(proposals);

        methodNameProvider = mock(IProposalNameProvider.class);
        when(methodNameProvider.toMethodName(objectInitCoreProposal))
                .thenReturn(Optional.of(OBJECT_INIT));
        when(methodNameProvider.toMethodName(stringInitCoreProposal))
                .thenReturn(Optional.of(STRING_INIT));
    }

    private void setUpModelRepository(@Nullable IType type, @Nullable UniqueTypeName uniqueTypeName,
            @Nullable ConstructorModel model) {
        pcProvider = Mockito.mock(IProjectCoordinateProvider.class);
        when(pcProvider.tryToUniqueName(type)).thenReturn(Result.fromNullable(uniqueTypeName));

        modelProvider = Mockito.mock(IConstructorModelProvider.class);

        if (uniqueTypeName != null) {
            when(modelProvider.acquireModel(uniqueTypeName)).thenReturn(Optional.fromNullable(model));
        }
    }

    private ConstructorsRcpPreferences createDefaultPreferences() {
        return createPreferences(0, 7, true, true, true);
    }

    private ConstructorsRcpPreferences createPreferences(int minProposalProbability, int maxNumberOfProposals,
            boolean changeProposalRelevance, boolean decorateProposalText, boolean decorateProposalIcon) {
        ConstructorsRcpPreferences pref = new ConstructorsRcpPreferences();
        pref.maxNumberOfProposals = maxNumberOfProposals;
        pref.minProposalProbability = minProposalProbability;
        pref.changeProposalRelevance = changeProposalRelevance;
        pref.decorateProposalText = decorateProposalText;
        pref.decorateProposalIcon = decorateProposalIcon;
        return pref;
    }
}
