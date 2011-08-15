/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc.providers.utils;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.IProjectModelFacade;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ProjectServices;
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.commons.extdoc.TestJavaElementSelection;
import org.eclipse.recommenders.tests.commons.extdoc.TestTypeUtils;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public final class CallsAdapterTest {

    private final CallsAdapter adapter = new CallsAdapter(createProjectServices(), null);
    private final TestJavaElementSelection selection = ExtDocUtils.getSelection();

    @Test
    public void testGetProposalsFromSingleMethods() {
        final IField field = TestTypeUtils.getDefaultField();
        final MockedIntelligentCompletionContext context = ContextFactory.createFieldVariableContext(selection, field);
        final SortedSet<Tuple<IMethodName, Tuple<IMethodName, Double>>> proposals = adapter
                .getProposalsFromSingleMethods(selection, field, context);
    }

    @Test
    public void testGetMethodsDeclaringType() {
        final ITypeName declaringType = adapter.getMethodsDeclaringType(TestTypeUtils.getDefaultJavaMethod(), null);
        // Assert.assertNotNull(declaringType);
    }

    @Test
    public void testResolveCalledMethods() {
        final MockedIntelligentCompletionContext context = ContextFactory.createNullVariableContext(selection);
        // final Set<IMethodName> resolvedCalls =
        // adapter.resolveCalledMethods(context);
    }

    @Test
    public void testComputeRecommendations() {

    }

    @Test
    public void testGetModelFacade() {

    }

    public static ProjectServices createProjectServices() {
        final IProjectModelFacade store = Mockito.mock(IProjectModelFacade.class);
        Mockito.when(store.hasModel(Matchers.any(ITypeName.class))).thenReturn(true);
        final IObjectMethodCallsNet model = Mockito.mock(IObjectMethodCallsNet.class);
        Mockito.when(store.acquireModel(Matchers.any(ITypeName.class))).thenReturn(model);

        final Tuple<IMethodName, Double> call = Tuple.create(TestTypeUtils.getDefaultMethod(), 0.0);
        final SortedSet<Tuple<IMethodName, Double>> calls = new TreeSet<Tuple<IMethodName, Double>>();
        calls.add(call);
        Mockito.when(model.getRecommendedMethodCalls(Matchers.anyDouble(), Matchers.anyInt())).thenReturn(calls);

        final ProjectServices services = Mockito.mock(ProjectServices.class);
        Mockito.when(services.getModelFacade(Matchers.any(IJavaProject.class))).thenReturn(store);
        return services;
    }
}
