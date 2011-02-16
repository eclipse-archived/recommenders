/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.templates;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.inject.Provider;

import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsModelStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.ObjectMethodCallsNet;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.PatternNode;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.PatternRecommendation;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IVariableUsageResolver;
import org.junit.Test;
import org.mockito.Mockito;

public final class PatternRecommenderTest {

    @Test
    public void testComputeRecommendations() {
        final IIntelligentCompletionContext context = CompletionTargetVariableBuilderTest.getMockedContext(
                "Button butto = new Button();\nbutto.", "butto", "Button");
        final PatternRecommender recommender = getPatternRecommenderMock(context.getReceiverType());

        final CompletionTargetVariable targetVariable = AllTests.getDefaultMethodCall().getCompletionTargetVariable();
        final Set<PatternRecommendation> recommendations = recommender.computeRecommendations(targetVariable, context);
    }

    protected static PatternRecommender getPatternRecommenderMock(final ITypeName receiverType) {
        final CallsModelStore store = Mockito.mock(CallsModelStore.class);
        final ObjectMethodCallsNet net = getCallsNetMock();
        Mockito.when(Boolean.valueOf(store.hasModel(receiverType))).thenReturn(Boolean.TRUE);
        Mockito.when(store.getModel(receiverType)).thenReturn(net);

        return new PatternRecommender(store, new Provider<Set<IVariableUsageResolver>>() {
            @Override
            public Set<IVariableUsageResolver> get() {
                return Collections.emptySet();
            }
        });
    }

    private static ObjectMethodCallsNet getCallsNetMock() {
        final ObjectMethodCallsNet net = Mockito.mock(ObjectMethodCallsNet.class);
        final PatternNode node = Mockito.mock(PatternNode.class);
        final List<Tuple<String, Double>> patterns = Lists.newArrayList();
        patterns.add(Tuple.create("Pattern 1", 0.5));

        Mockito.when(node.getPatternsWithProbability()).thenReturn(patterns);
        Mockito.when(net.getPatternsNode()).thenReturn(node);
        return net;
    }
}
