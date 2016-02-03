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
package org.eclipse.recommenders.internal.types.rcp;

import static org.eclipse.recommenders.internal.types.rcp.TypesCompletionSessionProcessor.BOOST;
import static org.eclipse.recommenders.testing.rcp.completion.SimpleProposalProcessorMatcher.processorWithBoost;
import static org.eclipse.recommenders.utils.names.VmTypeName.INT;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.OverlayImageProposalProcessor;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableJavaCompletionProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProposalProcessorManager;
import org.eclipse.recommenders.completion.rcp.processable.ProposalTag;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

public class TypesCompletionSessionProcessorTest {

    private static final ITypeName COLLECTION = VmTypeName.get("Ljava/util/Collection");
    private static final ITypeName LIST_ARRAY = VmTypeName.get("[Ljava/util/List");
    private static final ITypeName LIST = VmTypeName.get("Ljava/util/List");
    private static final ITypeName SET = VmTypeName.get("Ljava/util/Set");

    private static final String ARRAY_LIST_SIGNATURE = "Ljava.util.ArrayList;";
    private static final String LINKED_LIST_SIGNATURE = "Ljava.util.LinkedList;";
    private static final String ABSTRACT_SET_SIGNATURE = "Ljava.util.AbstractSet;";

    @Test
    public void testNoExpectedTypes() {
        ITypesIndexService service = mockTypesIndexServer(ImmutableSetMultimap.<ITypeName, String>of());
        IRecommendersCompletionContext context = mockCompletionContextWithExpectedTypeNames();
        TypesCompletionSessionProcessor sut = new TypesCompletionSessionProcessor(service, new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));

        // Should not run expensive searches
        verifyZeroInteractions(service);
    }

    @Test
    public void testPrimitiveTypeExpected() {
        ITypesIndexService service = mockTypesIndexServer(ImmutableSetMultimap.<ITypeName, String>of());
        IRecommendersCompletionContext context = mockCompletionContextWithExpectedTypeNames(INT);
        TypesCompletionSessionProcessor sut = new TypesCompletionSessionProcessor(service, new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));

        // Should not run expensive searches
        verifyZeroInteractions(service);
    }

    @Test
    public void testArrayTypeExpected() {
        ITypesIndexService service = mockTypesIndexServer(ImmutableSetMultimap.of(LIST, "java.util.ArrayList"));
        IRecommendersCompletionContext context = mockCompletionContextWithExpectedTypeNames(LIST_ARRAY);
        TypesCompletionSessionProcessor sut = new TypesCompletionSessionProcessor(service, new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));

        // Should not run expensive searches
        verifyZeroInteractions(service);
    }

    @Test
    public void testNoSubtypesKnown() throws Exception {
        ITypesIndexService service = mockTypesIndexServer(ImmutableSetMultimap.<ITypeName, String>of());
        IRecommendersCompletionContext context = mockCompletionContextWithExpectedTypeNames(LIST);
        TypesCompletionSessionProcessor sut = new TypesCompletionSessionProcessor(service, new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));
    }

    @Test
    public void testSingleTypeRefProposal() throws Exception {
        ITypesIndexService service = mockTypesIndexServer(ImmutableSetMultimap.of(LIST, "java.util.ArrayList"));
        IRecommendersCompletionContext context = mockCompletionContextWithExpectedTypeNames(LIST);

        TypesCompletionSessionProcessor sut = new TypesCompletionSessionProcessor(service, new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(true)));

        IProcessableProposal arrayListProcessableProposal = mockProcessableProposal(CompletionProposal.TYPE_REF,
                ARRAY_LIST_SIGNATURE);

        sut.process(arrayListProcessableProposal);

        verifyWasBoosted(arrayListProcessableProposal);
    }

    @Test
    public void testMultipleTypeRefProposals() throws Exception {
        ITypesIndexService service = mockTypesIndexServer(
                ImmutableSetMultimap.of(LIST, "java.util.ArrayList", LIST, "java.util.LinkedList"));
        IRecommendersCompletionContext context = mockCompletionContextWithExpectedTypeNames(LIST, LIST_ARRAY, INT);

        TypesCompletionSessionProcessor sut = new TypesCompletionSessionProcessor(service, new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(true)));

        IProcessableProposal arrayListProcessableProposal = mockProcessableProposal(CompletionProposal.TYPE_REF,
                ARRAY_LIST_SIGNATURE);

        IProcessableProposal linkedListProcessableProposal = mockProcessableProposal(CompletionProposal.TYPE_REF,
                LINKED_LIST_SIGNATURE);

        sut.process(arrayListProcessableProposal);

        verifyWasBoosted(arrayListProcessableProposal);

        sut.process(linkedListProcessableProposal);

        verifyWasBoosted(linkedListProcessableProposal);
    }

    @Test
    public void testMultipleTypeRefProposalsForDifferentTypes() throws Exception {
        ITypesIndexService service = mockTypesIndexServer(
                ImmutableSetMultimap.of(LIST, "java.util.ArrayList", SET, "java.util.AbstractSet"));
        IRecommendersCompletionContext context = mockCompletionContextWithExpectedTypeNames(LIST, SET);

        TypesCompletionSessionProcessor sut = new TypesCompletionSessionProcessor(service, new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(true)));

        IProcessableProposal arrayListProcessableProposal = mockProcessableProposal(CompletionProposal.TYPE_REF,
                ARRAY_LIST_SIGNATURE);

        IProcessableProposal abstractSetProcessableProposal = mockProcessableProposal(CompletionProposal.TYPE_REF,
                ABSTRACT_SET_SIGNATURE);

        sut.process(arrayListProcessableProposal);

        verifyWasBoosted(arrayListProcessableProposal);

        sut.process(abstractSetProcessableProposal);

        verifyWasBoosted(abstractSetProcessableProposal);
    }

    @Test
    public void testConstructorInvocationProposalWithGenerics() throws Exception {
        ITypesIndexService service = mockTypesIndexServer(ImmutableSetMultimap.of(LIST, "java.util.ArrayList"));
        IRecommendersCompletionContext context = mockCompletionContextWithExpectedTypeNames(LIST);

        TypesCompletionSessionProcessor sut = new TypesCompletionSessionProcessor(service, new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(true)));

        IProcessableProposal genericArrayListProcessableProposal = mockProcessableProposal(
                CompletionProposal.CONSTRUCTOR_INVOCATION, "Ljava.util.ArrayList<TE;>;");

        sut.process(genericArrayListProcessableProposal);

        verifyWasBoosted(genericArrayListProcessableProposal);
    }

    @Test
    public void testAnonymousClassConstructorInvocationProposal() throws Exception {
        ITypesIndexService service = mockTypesIndexServer(ImmutableSetMultimap.of(SET, "java.util.AbstractSet"));
        IRecommendersCompletionContext context = mockCompletionContextWithExpectedTypeNames(SET);

        TypesCompletionSessionProcessor sut = new TypesCompletionSessionProcessor(service, new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(true)));

        IProcessableProposal abstractSetProcessableProposal = mockProcessableProposal(
                CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, ABSTRACT_SET_SIGNATURE);

        sut.process(abstractSetProcessableProposal);

        verifyWasBoosted(abstractSetProcessableProposal);
    }

    @Test
    public void testMethodRefProposal() throws Exception {
        ITypesIndexService service = mockTypesIndexServer(ImmutableSetMultimap.of(COLLECTION, "java.util.List"));
        IRecommendersCompletionContext context = mockCompletionContextWithExpectedTypeNames(COLLECTION);

        TypesCompletionSessionProcessor sut = new TypesCompletionSessionProcessor(service, new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(true)));

        IProcessableProposal arraysAsListProcessableProposal = mockProcessableProposal(
                // Signature of Arrays.asList(T...)
                CompletionProposal.METHOD_REF, "<T:Ljava.lang.Object;>([TT;)Ljava.util.List<TT;>;");

        sut.process(arraysAsListProcessableProposal);

        verifyWasBoosted(arraysAsListProcessableProposal);
    }

    @Test
    public void testMethodRefWithCastedReceiverProposal() throws Exception {
        ITypesIndexService service = mockTypesIndexServer(ImmutableSetMultimap.of(COLLECTION, "java.util.List"));
        IRecommendersCompletionContext context = mockCompletionContextWithExpectedTypeNames(COLLECTION);

        TypesCompletionSessionProcessor sut = new TypesCompletionSessionProcessor(service, new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(true)));

        IProcessableProposal arraysAsListProcessableProposal = mockProcessableProposal(
                // Receiver Signature of List.set(Object)
                CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER, "Ljava.util.List;");

        sut.process(arraysAsListProcessableProposal);

        verifyWasBoosted(arraysAsListProcessableProposal);
    }

    private IRecommendersCompletionContext mockCompletionContextWithExpectedTypeNames(ITypeName... expectedTypeNames) {
        IRecommendersCompletionContext context = mock(IRecommendersCompletionContext.class);

        when(context.getExpectedTypeNames()).thenReturn(ImmutableSet.copyOf(expectedTypeNames));

        IJavaProject project = mock(IJavaProject.class);
        when(context.getProject()).thenReturn(project);

        return context;
    }

    private ITypesIndexService mockTypesIndexServer(SetMultimap<ITypeName, String> index) {
        ITypesIndexService service = mock(ITypesIndexService.class);

        for (ITypeName typeName : index.keySet()) {
            Set<String> subtypes = index.get(typeName);
            when(service.subtypes(eq(typeName), any(IJavaProject.class))).thenReturn(subtypes);
        }

        return service;
    }

    private IProcessableProposal mockProcessableProposal(int coreProposalKind, String coreProposalSig) {
        IProcessableProposal processableProposal = mock(ProcessableJavaCompletionProposal.class);
        ProposalProcessorManager manager = mock(ProposalProcessorManager.class);
        when(processableProposal.getProposalProcessorManager()).thenReturn(manager);

        CompletionProposal coreProposal = mock(CompletionProposal.class);
        when(coreProposal.getKind()).thenReturn(coreProposalKind);
        when(processableProposal.getCoreProposal()).thenReturn(Optional.fromNullable(coreProposal));

        switch (coreProposalKind) {
        case CompletionProposal.FIELD_REF:
        case CompletionProposal.LOCAL_VARIABLE_REF:
        case CompletionProposal.METHOD_REF:
        case CompletionProposal.TYPE_REF:
            when(coreProposal.getSignature()).thenReturn(coreProposalSig.toCharArray());
            break;
        case CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION:
        case CompletionProposal.CONSTRUCTOR_INVOCATION:
            when(coreProposal.getDeclarationSignature()).thenReturn(coreProposalSig.toCharArray());
            break;
        case CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER:
        case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
            when(coreProposal.getReceiverSignature()).thenReturn(coreProposalSig.toCharArray());
            break;
        }

        return processableProposal;
    }

    private void verifyWasBoosted(IProcessableProposal processableProposal) {
        ProposalProcessorManager manager = processableProposal.getProposalProcessorManager();
        verify(processableProposal).setTag(ProposalTag.RECOMMENDERS_SCORE, BOOST);
        verify(manager, times(1)).addProcessor(processorWithBoost(BOOST));
        verify(manager, times(1)).addProcessor(isA(OverlayImageProposalProcessor.class));
        verifyNoMoreInteractions(manager);
    }
}
