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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.recommenders.completion.rcp.CompletionContextKey;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
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
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

@SuppressWarnings("restriction")
public class ConstructorCompletionSessionProcessorTest {

    private static final ProjectCoordinate JRE_1_6_0 = new ProjectCoordinate("jre", "jre", "1.6.0");

    private static final ITypeName OBJECT = VmTypeName.get("Ljava/lang/Object");
    private static final IType OBJECT_TYPE = mock(IType.class);

    private static final IMethodName OBJECT_INIT = VmMethodName.get("Ljava/lang/Object.<init>()V");
    private static final IJavaCompletionProposal OBJECT_INIT_PROPOSAL = mock(IJavaCompletionProposal.class);
    private static final CompletionProposal OBJECT_INIT_CORE_PROPOSAL = mock(CompletionProposal.class);

    private static final ConstructorModel NULL_MODEL = null;

    private IProjectCoordinateProvider pcProvider;
    private IConstructorModelProvider modelProvider;
    private IRecommendersCompletionContext context;

    @Test
    public void testCompletionWithoutModel() {
        setUp(CompletionOnSingleTypeReference.class, new UniqueTypeName(JRE_1_6_0, OBJECT), OBJECT_TYPE, NULL_MODEL,
                ImmutableMap.of(OBJECT_INIT_PROPOSAL, OBJECT_INIT_CORE_PROPOSAL));

        ConstructorCompletionSessionProcessor sut = new ConstructorCompletionSessionProcessor(pcProvider,
                modelProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));
        verify(modelProvider).releaseModel(NULL_MODEL);
    }

    @Test
    public void testCompletionWithoutRecommendations() {
        ConstructorModel model = new ConstructorModel(OBJECT, Collections.<IMethodName, Integer>emptyMap());
        setUp(CompletionOnSingleTypeReference.class, new UniqueTypeName(JRE_1_6_0, OBJECT), OBJECT_TYPE, model,
                ImmutableMap.of(OBJECT_INIT_PROPOSAL, OBJECT_INIT_CORE_PROPOSAL));

        ConstructorCompletionSessionProcessor sut = new ConstructorCompletionSessionProcessor(pcProvider,
                modelProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));
        verify(modelProvider).releaseModel(model);
    }

    private void setUp(Class<? extends ASTNode> completionType, UniqueTypeName uniqueTypeName,
            @Nullable IType expectedType, @Nullable ConstructorModel model,
            Map<IJavaCompletionProposal, CompletionProposal> proposals) {
        LookupEnvironment lookupEnvironment = mock(LookupEnvironment.class);
        context = mock(IRecommendersCompletionContext.class);
        when(context.get(CompletionContextKey.LOOKUP_ENVIRONMENT)).thenReturn(Optional.of(lookupEnvironment));
        Optional<ASTNode> completionNode = completionType == null ? Optional.<ASTNode>absent() : Optional
                .<ASTNode>of(mock(completionType));
        when(context.getCompletionNode()).thenReturn(completionNode);
        when(context.getExpectedType()).thenReturn(Optional.fromNullable(expectedType));
        when(context.getProposals()).thenReturn(proposals);

        pcProvider = Mockito.mock(IProjectCoordinateProvider.class);
        when(pcProvider.tryToUniqueName(expectedType)).thenReturn(Result.fromNullable(uniqueTypeName));

        modelProvider = Mockito.mock(IConstructorModelProvider.class);

        if (uniqueTypeName != null) {
            when(modelProvider.acquireModel(uniqueTypeName)).thenReturn(Optional.fromNullable(model));
        }
    }

    private ConstructorsRcpPreferences createDefaultPreferences() {
        return createPreferences(0, 7);
    }

    private ConstructorsRcpPreferences createPreferences(int minProposalProbability, int maxNumberOfProposals) {
        ConstructorsRcpPreferences pref = new ConstructorsRcpPreferences();
        pref.maxNumberOfProposals = maxNumberOfProposals;
        pref.minProposalProbability = minProposalProbability;
        return pref;
    }
}
